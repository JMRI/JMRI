package jmri.jmrit.withrottle;

/**
 * WiThrottle
 *
 * @author Brett Hoffman Copyright (C) 2009, 2010
 * @author Created by Brett Hoffman on:
 * @author 7/20/09.
 *
 * Thread with input and output streams for each connected device. Creates an
 * invisible throttle window for each.
 *
 * Sorting codes:
 *  'T'hrottle - sends to throttleController
 *  'S'econdThrottle - sends to secondThrottleController
 *  'C' - Not used anymore except to provide backward compliance, same as 'T'
 *  'N'ame of device
 *  'H' hardware info - followed by:
 *      'U' UDID - unique device identifier
 *  'P' panel - followed by:
 *      'P' track power
 *      'T' turnouts
 *      'R' routes
 *  'R' roster - followed by:
 *      'C' consists
 *  'Q'uit - device has quit, close its throttleWindow
 *  '*' - heartbeat from client device ('*+' starts, '*-' stops)
 *
 * Added in v2.0: 'M'ultiThrottle - forwards to MultiThrottle class, see notes
 * there for use. Followed by id character to create or control appropriate
 * DccThrottle. Stored as HashTable for access to 'T' and 'S' throttles.
 *
 * 'D'irect byte packet to rails Followed by one digit for repeats, then
 * followed by hex pairs, (single spaced) including pair for error byte. D200 90
 * 90 - Send '00 90 90' twice, with error byte '90'
 *
 *
 * Out to client, all newline terminated:
 *
 * Track power: 'PPA' + '0' (off), '1' (on), '2' (unknown) Minimum package
 * length of 4 char.
 *
 * Send Info on routes to devices, not specific to any one route. Format:
 * PRT]\[value}|{routeKey]\[value}|{ActiveKey]\[value}|{InactiveKey 
 *
 * Send list of routes Format:
 * PRL]\[SysName}|{UsrName}|{CurrentState]\[SysName}|{UsrName}|{CurrentState
 * States: 1 - UNKNOWN, 2 - ACTIVE, 4 - INACTIVE (based on turnoutsAligned
 * sensor, if used)
 *
 * Send Info on turnouts to devices, not specific to any one turnout. Format:
 * PTT]\[value}|{turnoutKey]\[value}|{closedKey]\[value}|{thrownKey 
 * Send list of turnouts Format:
 * PTL]\[SysName}|{UsrName}|{CurrentState]\[SysName}|{UsrName}|{CurrentState
 * States: 1 - UNKNOWN, 2 - CLOSED, 4 - THROWN
 * 
 * Send time or time&rate:
 * 'PFT' + UTCAdjustedTimeSeconds
 *     -OR-
 * 'PFT' + UTCAdjustedTimeSeconds + "<;>" + RateMultipier
 * Set rate to 0.0 for stop, float value to run.
 *
 * Web server port: 'PW' + {port#}
 *
 * Roster is sent formatted: ]\[ separates roster entries, }|{ separates info in
 * each entry e.g. RL###]\[RVRR1201}|{1201}|{L]\[Limited}|{8165}|{L]\[
 *
 * Function labels: RF## first throttle, or RS## second throttle, each label
 * separated by ]\[ e.g. RF29]\[Light]\[Bell]\[Horn]\[Short Horn]\[ &etc.
 *
 * RSF 'R'oster 'P'roperties 'F'unctions
 *
 *
 * Heartbeat send '*0' to tell device to stop heartbeat, '*#' # = number of
 * seconds until eStop. This class sends initial to device, but does not start
 * monitoring until it gets a response of '*+' Device should send heartbeat to
 * server in shorter time than eStop
 *
 * Alert message: 'HM' + message to display. Cannot have newlines in body of
 * text, only at end of message.
 * Info message: 'Hm' + message to display. Same as HM, but informational only.
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import jmri.CommandStation;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.ThreadingUtil;
import jmri.web.server.WebServerPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceServer implements Runnable, ThrottleControllerListener, ControllerInterface {

    //  Manually increment as features are added
    private static final String VERSION_NUMBER = "2.0";

    private Socket device;
    private final CommandStation cmdStation = jmri.InstanceManager.getNullableDefault(CommandStation.class);
    String newLine = System.getProperty("line.separator");
    BufferedReader in = null;
    PrintStream out = null;
    private final ArrayList<DeviceListener> listeners = new ArrayList<>();
    String deviceName = "Unknown";
    String deviceUDID;

    ThrottleController throttleController;
    ThrottleController secondThrottleController;
    HashMap<Character, MultiThrottle> multiThrottles;
    private boolean keepReading;
    private boolean isUsingHeartbeat = false;
    private boolean heartbeat = true;
    private int pulseInterval = 16; // seconds til disconnect
    private TimerTask ekgTask;
    private int stopEKGCount;

    private TrackPowerController trackPower = null;
    final boolean isTrackPowerAllowed = InstanceManager.getDefault(WiThrottlePreferences.class).isAllowTrackPower();
    private TurnoutController turnoutC = null;
    private RouteController routeC = null;
    final boolean isTurnoutAllowed = InstanceManager.getDefault(WiThrottlePreferences.class).isAllowTurnout();
    final boolean isRouteAllowed = InstanceManager.getDefault(WiThrottlePreferences.class).isAllowRoute();
    private ConsistController consistC = null;
    private boolean isConsistAllowed;
    private FastClockController fastClockC = null;
    final boolean isClockDisplayed = InstanceManager.getDefault(WiThrottlePreferences.class).isDisplayFastClock();

    private DeviceManager manager;

    DeviceServer(Socket socket, DeviceManager manager) {
        this.device = socket;
        this.manager = manager;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating input  stream reader for " + device.getRemoteSocketAddress());
            }
            in = new BufferedReader(new InputStreamReader(device.getInputStream(), "UTF8"));
            if (log.isDebugEnabled()) {
                log.debug("Creating output stream writer for " + device.getRemoteSocketAddress());
            }
            out = new PrintStream(device.getOutputStream(), true, "UTF8");

        } catch (IOException e) {
            log.error("Stream creation failed (DeviceServer)");
            return;
        }
        sendPacketToDevice("VN" + getWiTVersion());
        sendPacketToDevice(sendRoster());
        addControllers();
        sendPacketToDevice("PW" + getWebServerPort());

    }

    @Override
    public void run() {
        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            log.debug("Notify Device Add");
            l.notifyDeviceConnected(this);

        }
        String inPackage = null;

        keepReading = true; // Gets set to false when device sends 'Q'uit
        int consecutiveErrors = 0;

        do {
            try {
                inPackage = in.readLine();

                if (inPackage != null) {
                    heartbeat = true;   //  Any contact will keep alive
                    consecutiveErrors = 0;  //reset error counter
                    if (log.isDebugEnabled()) {
                        String s = inPackage + "                    "; //pad output so messages form columns
                        s = s.substring(0, Math.max(inPackage.length(), 20));
                        log.debug("Rcvd: " + s + " from " + getName() + device.getRemoteSocketAddress());
                    }

                    switch (inPackage.charAt(0)) {
                        case 'T': {
                            if (throttleController == null) {
                                throttleController = new ThrottleController('T', this, this);
                            }
                            keepReading = throttleController.sort(inPackage.substring(1));
                            break;
                        }

                        case 'S': {
                            if (secondThrottleController == null) {
                                secondThrottleController = new ThrottleController('S', this, this);
                            }
                            keepReading = secondThrottleController.sort(inPackage.substring(1));
                            break;
                        }

                        case 'M': {  //  MultiThrottle M(id character)('A'ction '+' or '-')(message)
                            if (multiThrottles == null) {
                                multiThrottles = new HashMap<>(1);
                            }
                            char id = inPackage.charAt(1);
                            if (!multiThrottles.containsKey(id)) {   //  Create a MT if this is a new id
                                multiThrottles.put(id, new MultiThrottle(id, this, this));
                            }

                            // Strips 'M' and id, forwards rest
                            multiThrottles.get(id).handleMessage(inPackage.substring(2));

                            break;
                        }

                        case 'D': {
                            if (log.isDebugEnabled()) {
                                log.debug("Sending hex packet: " + inPackage.substring(2) + " to command station.");
                            }
                            int repeats = Character.getNumericValue(inPackage.charAt(1));
                            byte[] packet = jmri.util.StringUtil.bytesFromHexString(inPackage.substring(2));
                            cmdStation.sendPacket(packet, repeats);
                            break;
                        }

                        case '*': {  //  Heartbeat only

                            if (inPackage.length() > 1) {
                                switch (inPackage.charAt(1)) {

                                    case '+': {  //  trigger, turns on timed monitoring
                                        if (!isUsingHeartbeat) {
                                            startEKG();
                                        }
                                        break;
                                    }

                                    case '-': {  //  turns off
                                        if (isUsingHeartbeat) {
                                            stopEKG();
                                        }
                                        break;
                                    }
                                    default:
                                        log.warn("Unhandled code: {}", inPackage.charAt(1));
                                        break;
                                }

                            }

                            break;
                        }   //  end heartbeat block

                        case 'C': {  //  Prefix for confirmed package
                            switch (inPackage.charAt(1)) {
                                case 'T': {
                                    keepReading = throttleController.sort(inPackage.substring(2));

                                    break;
                                }

                                default: {
                                    log.warn("Received unknown network package: {}", inPackage);

                                    break;
                                }
                            }

                            break;
                        }

                        case 'N': {  //  Prefix for deviceName
                            deviceName = inPackage.substring(1);
                            log.info("Received Name: {}", deviceName);

                            if (InstanceManager.getDefault(WiThrottlePreferences.class).isUseEStop()) {
                                pulseInterval = InstanceManager.getDefault(WiThrottlePreferences.class).getEStopDelay();
                                sendPacketToDevice("*" + pulseInterval); //  Turn on heartbeat, if used
                            }
                            break;
                        }

                        case 'H': {  //  Hardware
                            switch (inPackage.charAt(1)) {
                                case 'U':
                                    deviceUDID = inPackage.substring(2);
                                    for (int i = 0; i < listeners.size(); i++) {
                                        DeviceListener l = listeners.get(i);
                                        l.notifyDeviceInfoChanged(this);
                                    }
                                    break;
                                default:
                                    log.warn("Unhandled code: {}", inPackage.charAt(1));
                                    break;
                            }

                            break;
                        }   //  end hardware block

                        case 'P': {  //  Start 'P'anel case
                            switch (inPackage.charAt(1)) {
                                case 'P': {
                                    if (isTrackPowerAllowed) {
                                        trackPower.handleMessage(inPackage.substring(2), this);
                                    }
                                    break;
                                }
                                case 'T': {
                                    if (isTurnoutAllowed) {
                                        turnoutC.handleMessage(inPackage.substring(2), this);
                                    }
                                    break;
                                }
                                case 'R': {
                                    if (isRouteAllowed) {
                                        routeC.handleMessage(inPackage.substring(2), this);
                                    }
                                    break;
                                }
                                default:
                                    log.warn("Unhandled code: {}", inPackage.charAt(1), this);
                                    break;
                            }
                            break;
                        }   //  end panel block

                        case 'R': {  //  Start 'R'oster case
                            switch (inPackage.charAt(1)) {
                                case 'C':
                                    if (isConsistAllowed) {
                                        consistC.handleMessage(inPackage.substring(2), this);
                                    }
                                    break;
                                default:
                                    log.warn("Unhandled code: {}", inPackage.charAt(1));
                                    break;
                            }

                            break;
                        }   //  end roster block

                        case 'Q': {
                            keepReading = false;
                            break;
                        }

                        default: {   //  If an unknown makes it through, do nothing.
                            log.warn("Received unknown network package: {}", inPackage);
                            break;
                        }

                    }   //End of charAt(0) switch block

                    inPackage = null;
                } else { //in.readLine() IS null
                    consecutiveErrors += 1;
                    log.warn("null readLine() from device '{}', consecutive error # {}", getName(), consecutiveErrors);
                }

            } catch (IOException exa) {
                consecutiveErrors += 1;
                log.warn("readLine from device '{}' failed, consecutive error # {}", getName(), consecutiveErrors);
            } catch (IndexOutOfBoundsException exb) {
                log.warn("Bad message '{}' from device '{}'", inPackage, getName());
            }
            if (consecutiveErrors > 0) { //a read error was encountered
                if (consecutiveErrors < 25) { //pause thread to give time for reconnection
                    try {
                        Thread.sleep(200);
                    } catch (java.lang.InterruptedException ex) {
                    }
                } else {
                    keepReading = false;
                    log.error("readLine failure limit exceeded, ending thread run loop for device '{}'", getName());
                }
            }
        } while (keepReading); // 'til we tell it to stop
        log.debug("Ending thread run loop for device '{}'", getName());
        closeThrottles();

    }

    public void closeThrottles() {
        stopEKG();
        if (throttleController != null) {
            throttleController.shutdownThrottle();
            throttleController.removeThrottleControllerListener(this);
            throttleController.removeControllerListener(this);
        }
        if (secondThrottleController != null) {
            secondThrottleController.shutdownThrottle();
            secondThrottleController.removeThrottleControllerListener(this);
            secondThrottleController.removeControllerListener(this);
        }
        if (multiThrottles != null) {
            for (char key : multiThrottles.keySet()) {
                log.debug("Closing throttles for key: {} for device: {}", key, getName());
                multiThrottles.get(key).dispose();
            }
        }
        if (multiThrottles != null) {
            multiThrottles.clear();
            multiThrottles = null;
        }
        throttleController = null;
        secondThrottleController = null;
        if (trackPower != null) {
            trackPower.removeControllerListener(this);
        }
        if (turnoutC != null) {
            turnoutC.removeControllerListener(this);
        }
        if (routeC != null) {
            routeC.removeControllerListener(this);
        }
        if (consistC != null) {
            consistC.removeControllerListener(this);
        }
        if (fastClockC != null) {
            fastClockC.removeControllerListener(this);
        }

        closeSocket();
        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            l.notifyDeviceDisconnected(this);

        }
    }

    public void closeSocket() {

        keepReading = false;
        try {
            if (device.isClosed()) {
                if (log.isDebugEnabled()) {
                    log.debug("device socket {}{} already closed.", getName(), device.getRemoteSocketAddress());
                }
            } else {
                device.close();
                if (log.isDebugEnabled()) {
                    log.debug("device socket {}{} closed.", getName(), device.getRemoteSocketAddress());
                }
            }
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("device socket {}{} close failed with IOException.", getName(), device.getRemoteSocketAddress());
            }
        }
    }

    public void startEKG() {
        log.debug("starting heartbeat EKG for '{}' with interval: {}", getName(), pulseInterval);
        isUsingHeartbeat = true;
        stopEKGCount = 0;
        ekgTask = new TimerTask() {
            @Override
            public void run() {  //  Drops on second pass
                ThreadingUtil.runOnLayout(() -> {
                    if (!heartbeat) {
                        stopEKGCount++;
                        //  Send eStop to each throttle
                        if (log.isDebugEnabled()) {
                            log.debug("Lost signal from: " + getName() + ", sending eStop");
                        }
                        if (throttleController != null) {
                            throttleController.sort("X");
                        }
                        if (secondThrottleController != null) {
                            secondThrottleController.sort("X");
                        }
                        if (multiThrottles != null) {
                            for (char key : multiThrottles.keySet()) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Sending eStop to MT key: " + key);
                                }
                                multiThrottles.get(key).eStop();
                            }

                        }
                        if (stopEKGCount > 2) {
                            closeThrottles();
                        }
                    }
                    heartbeat = false;
                });
            }

        };
        jmri.util.TimerUtil.scheduleAtFixedRate(ekgTask, pulseInterval * 900L, pulseInterval * 900L);
    }

    public void stopEKG() {
        isUsingHeartbeat = false;
        if (ekgTask != null) {
            ekgTask.cancel();
        }

    }

    private void addControllers() {
        if (isTrackPowerAllowed) {
            trackPower = InstanceManager.getDefault(WiThrottleManager.class).getTrackPowerController();
            if (trackPower.isValid) {
                if (log.isDebugEnabled()) {
                    log.debug("Track Power valid.");
                }
                trackPower.addControllerListener(this);
                trackPower.sendCurrentState();
            }
        }
        if (isTurnoutAllowed) {
            turnoutC = InstanceManager.getDefault(WiThrottleManager.class).getTurnoutController();
            if (turnoutC.verifyCreation()) {
                if (log.isDebugEnabled()) {
                    log.debug("Turnout Controller valid.");
                }
                turnoutC.addControllerListener(this);
                turnoutC.sendTitles();
                turnoutC.sendList();
            }
        }
        if (isRouteAllowed) {
            routeC = InstanceManager.getDefault(WiThrottleManager.class).getRouteController();
            if (routeC.verifyCreation()) {
                if (log.isDebugEnabled()) {
                    log.debug("Route Controller valid.");
                }
                routeC.addControllerListener(this);
                routeC.sendTitles();
                routeC.sendList();
            }
        }

        //  Consists can be selected regardless of pref, as long as there is a ConsistManager.
        consistC = InstanceManager.getDefault(WiThrottleManager.class).getConsistController();
        if (consistC.verifyCreation()) {
            if (log.isDebugEnabled()) {
                log.debug("Consist Controller valid.");
            }
            isConsistAllowed = InstanceManager.getDefault(WiThrottlePreferences.class).isAllowConsist();
            consistC.addControllerListener(this);
            consistC.setIsConsistAllowed(isConsistAllowed);
            consistC.sendConsistListType();

            consistC.sendAllConsistData();
        }
        if (isClockDisplayed) {
            fastClockC = InstanceManager.getDefault(WiThrottleManager.class).getFastClockController();
            if (fastClockC.verifyCreation()) {
                if (log.isDebugEnabled()) {
                    log.debug("Fast Clock Controller valid.");
                }
                fastClockC.addControllerListener(this);
                fastClockC.sendFastRate();
            }
        }
    }

    public String getUDID() {
        return deviceUDID;
    }

    public String getName() {
        return deviceName;
    }

    public String getCurrentAddressString() {
        StringBuilder s = new StringBuilder("");
        if (throttleController != null) {
            s.append(throttleController.getCurrentAddressString());
            s.append(" ");
        }
        if (secondThrottleController != null) {
            s.append(secondThrottleController.getCurrentAddressString());
            s.append(" ");
        }
        if (multiThrottles != null) {
            for (MultiThrottle mt : multiThrottles.values()) {
                if (mt.throttles != null) {
                    for (MultiThrottleController mtc : mt.throttles.values()) {
                        s.append(mtc.getCurrentAddressString());
                        s.append(" ");
                    }
                }
            }
        }
        return s.toString();
    }

    /**
     * since 4.15.4
     */
    public String getCurrentRosterIdString() {
        StringBuilder s = new StringBuilder("");
        if (throttleController != null) {
            s.append(throttleController.getCurrentRosterIdString());
            s.append(" ");
        }
        if (secondThrottleController != null) {
            s.append(secondThrottleController.getCurrentRosterIdString());
            s.append(" ");
        }
        if (multiThrottles != null) {
            for (MultiThrottle mt : multiThrottles.values()) {
                if (mt.throttles != null) {
                    for (MultiThrottleController mtc : mt.throttles.values()) {
                        s.append(mtc.getCurrentRosterIdString());
                        s.append(" ");
                    }
                }
            }
        }
        return s.toString();
    }

    public static String getWiTVersion() {
        return VERSION_NUMBER;
    }

    public static String getWebServerPort() {
        return Integer.toString(InstanceManager.getDefault(WebServerPreferences.class).getPort());
    }

    /**
     * Called by various Controllers to send a string message to a connected
     * device. Appends a newline to the end.
     *
     * @param message The string to send.
     */
    @Override
    public void sendPacketToDevice(String message) {
        if (message == null) {
            return; //  Do not send a null.
        }
        out.println(message + newLine);
        if (log.isDebugEnabled()) {
            String s = message + "                    "; //pad output so messages form columns
            s = s.substring(0, Math.max(message.length(), 20));
            log.debug("Sent: " + s + "  to  " + getName() + device.getRemoteSocketAddress());
        }
    }
    /**
     * Send an Alert message (simple text string) to this client
     * <p>
     * @param message 
     * Format: HMmessage
     */
    @Override
    public void sendAlertMessage(String message) {        
        sendPacketToDevice("HM" + message);
    }

    /**
     * Send an Info message (simple text string) to this client
     * <p>
     * @param message 
     * Format: Hmmessage
     */
    @Override
    public void sendInfoMessage(String message) {
        sendPacketToDevice("Hm" + message);
    }
   
    

    /**
     * Add a DeviceListener
     *
     * @param l handle for listener to add
     *
     */
    public void addDeviceListener(DeviceListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Remove a DeviceListener
     *
     * @param l listener to remove
     *
     */
    public void removeDeviceListener(DeviceListener l) {
        if (listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    @Override
    public void notifyControllerAddressFound(ThrottleController TC) {

        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            l.notifyDeviceAddressChanged(this);
            if (log.isDebugEnabled()) {
                log.debug("Notify DeviceListener: " + l.getClass() + " address: " + TC.getCurrentAddressString());
            }
        }
    }

    @Override
    public void notifyControllerAddressReleased(ThrottleController TC) {

        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            l.notifyDeviceAddressChanged(this);
            if (log.isDebugEnabled()) {
                log.debug("Notify DeviceListener: " + l.getClass() + " address: " + TC.getCurrentAddressString());
            }
        }

    }

    /**
     * System has declined the address request, may be an in-use address. Need
     * to clear the address from the proper multiThrottle.
     *
     * @param tc      The throttle controller that was listening for a response
     *                to an address request
     * @param address The address to send a cancel to
     * @param reason  The reason the request was declined, to be sent back to client
     */
    @Override
    public void notifyControllerAddressDeclined(ThrottleController tc, DccLocoAddress address, String reason) {
        log.warn("notifyControllerAddressDeclined: "+ reason);
        sendAlertMessage(reason); // let the client know why the request failed
        if (multiThrottles != null) {   //  Should exist by this point
            jmri.InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, tc);
            multiThrottles.get(tc.whichThrottle).canceledThrottleRequest(tc.locoKey);
        }
    }

    /**
     * Format a package to be sent to the device for roster list selections.
     *
     * @return String containing a formatted list of some of each RosterEntry's
     *         info. Include a header with the length of the string to be
     *         received.
     */
    public String sendRoster() {
        List<RosterEntry> rosterList;
        rosterList = Roster.getDefault().getEntriesInGroup(manager.getSelectedRosterGroup());
        StringBuilder rosterString = new StringBuilder(rosterList.size() * 25);
        for (RosterEntry entry : rosterList) {
            StringBuilder entryInfo = new StringBuilder(entry.getId()); //  Start with name
            entryInfo.append("}|{");
            entryInfo.append(entry.getDccAddress());
            if (entry.isLongAddress()) { //  Append length value
                entryInfo.append("}|{L");
            } else {
                entryInfo.append("}|{S");
            }

            rosterString.append("]\\[");  //  Put this info in as an item
            rosterString.append(entryInfo);

        }
        rosterString.trimToSize();

        return ("RL" + rosterList.size() + rosterString);
    }

    private final static Logger log = LoggerFactory.getLogger(DeviceServer.class);

}
