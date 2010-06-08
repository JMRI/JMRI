
package jmri.jmrit.withrottle;

/**
 *  DeviceServer.java
 *
 *  WiThrottle
 *
 *	@author Brett Hoffman   Copyright (C) 2009
 *	@author Created by Brett Hoffman on:
 *	@author 7/20/09.
 *	@version $Revision: 1.14 $
 *
 *	Thread with input and output streams for each connected device.
 *	Creates an invisible throttle window for each.
 *
 *      Sorting codes:
 *      'T'hrottle - sends to throttleController
 *      'S'econdThrottle - sends to secondThrottleController
 *      'C' - Not used anymore except to provide backward compliance, same as 'T'
 *      'N'ame of device
 *      'H' hardware info - followed by:
 *          'U' UDID - unique device identifier
 *      'P' panel - followed by:
 *          'P' track power
 *          'T' turnouts
 *          'R' routes
 *      'R' roster - followed by:
 *          'C' consists
 *      'Q'uit - device has quit, close its throttleWindow
 *      '*' - heartbeat from client device ('*+' starts, '*-' stops)
 * 
 *      Out to client, all newline terminated:
 *      
 *      Track power: 'PPA' + '0' (off), '1' (on), '2' (unknown)
 *      Minimum package length of 4 char.
 *
 *      Send Info on routes to devices, not specific to any one route.
 *      Format:  PRT]\[value}|{routeKey]\[value}|{ActiveKey]\[value}|{InactiveKey
 *      Send list of routes
 *      Format:  PRL]\[SysName}|{UsrName}|{CurrentState]\[SysName}|{UsrName}|{CurrentState
 *      States:  1 - UNKNOWN, 2 - ACTIVE, 4 - INACTIVE (based on turnoutsAligned sensor, if used)
 *
 *      Send Info on turnouts to devices, not specific to any one turnout.
 *      Format:  PTT]\[value}|{turnoutKey]\[value}|{closedKey]\[value}|{thrownKey
 *      Send list of turnouts
 *      Format:  PTL]\[SysName}|{UsrName}|{CurrentState]\[SysName}|{UsrName}|{CurrentState
 *      States:  1 - UNKNOWN, 2 - CLOSED, 4 - THROWN
 *
 *      Roster is sent formatted: ]\[ separates roster entries, }|{ separates info in each entry
 *      e.g.  RL###]\[RVRR1201}|{1201}|{L]\[Limited}|{8165}|{L]\[
 * 
 *      Function labels: RF## first throttle, or RS## second throttle, each label separated by ]\[
 *      e.g.  RF29]\[Light]\[Bell]\[Horn]\[Short Horn]\[ &etc.
 *
 *      RSF 'R'oster 'P'roperties 'F'unctions
 *
 * 
 *      Heartbeat send '*0' to tell device to stop heartbeat, '*#' # = number of seconds until eStop
 *      This class sends initial to device, but does not start monitoring until it gets a response of '*+'
 *      Device should send heartbeat to server in shorter time than eStop
 *
 *      Alert message: 'HM' + message to display.
 *      Cannot have newlines in body of text, only at end of message.
 *
 */


import java.net.Socket;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleWindow;
import jmri.jmrit.throttle.AddressPanel;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

public class DeviceServer implements Runnable, ThrottleControllerListener, ControllerInterface {

    //  Manually increment as features are added
    private static final String versionNumber = "1.4";

    private Socket device;
    String newLine = System.getProperty("line.separator");
    BufferedReader in = null;
    PrintWriter out = null;
    ArrayList<DeviceListener> listeners;
    String deviceName = "Unknown";
    String deviceUDID;
    String deviceAddress = "Not Set";
    String secondDeviceAddress = "Not Set";

    ThrottleFrame throttleFrame;
    ThrottleFrame secondThrottleFrame;
    ThrottleWindow throttleWindow;
    AddressPanel addressPanel;
    AddressPanel secondAddressPanel;
    ThrottleController throttleController;
    ThrottleController secondThrottleController;
    private boolean keepReading;
    private boolean isUsingHeartbeat = false;
    private boolean heartbeat = true;
    private int pulseInterval = 16; // seconds til disconnect
    private Timer ekg;

    private TrackPowerController trackPower = null;
    private boolean isTrackPowerAllowed;
    private TurnoutController turnoutC = null;
    private RouteController routeC = null;
    private boolean isTurnoutAllowed;
    private boolean isRouteAllowed;
    private ConsistController consistC = null;
    private boolean isConsistAllowed;

    List <RosterEntry> rosterList;

    
    DeviceServer(Socket socket){
        this.device = socket;
        if (listeners == null){
            listeners = new ArrayList<DeviceListener>(2);
        }

        // Create a throttle instance for each device connected
        if (jmri.InstanceManager.throttleManagerInstance() != null){
            throttleWindow = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleWindow();
            throttleFrame = throttleWindow.getCurentThrottleFrame();
            throttleController = new ThrottleController(throttleFrame);
            throttleController.setWhichThrottle("T");
            addressPanel = throttleFrame.getAddressPanel();
            throttleController.addThrottleControllerListener(this);
            throttleController.addControllerListener(this);

        }
        try{
            in = new BufferedReader(new InputStreamReader(device.getInputStream(),"UTF8"));
            out = new PrintWriter(device.getOutputStream(),true);

        } catch (IOException e){
            log.error("Stream creation failed (DeviceServer)");
            return;
        }
        out.println("VN"+getWiTVersion()+newLine);
        out.println(sendRoster());
        addControllers();
        if (WiThrottleManager.withrottlePreferencesInstance().isUseEStop()){
            pulseInterval = WiThrottleManager.withrottlePreferencesInstance().getEStopDelay();
            out.println("*"+pulseInterval+newLine); //  Turn on heartbeat, if used
        }
        
    }

    public void run(){
        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            log.debug("Notify Device Add");
            l.notifyDeviceConnected(this);

        }
        String inPackage = null;

        keepReading = true;	//	Gets set to false when device sends 'Q'uit
                
        do{
            try{
                inPackage = in.readLine();

                if (inPackage != null){
                    heartbeat = true;   //  Any contact will keep alive
                    if (log.isDebugEnabled()) log.debug("Recieved: " + inPackage);
                    switch (inPackage.charAt(0)){
                        case 'T':{
                            keepReading = throttleController.sort(inPackage.substring(1));
                            break;
                        }

                        case 'S':{
                            if (secondThrottleController == null){
                                secondThrottleFrame = throttleWindow.addThrottleFrame();
                                throttleWindow.nextThrottleFrame();
                                secondThrottleController = new ThrottleController(secondThrottleFrame);
                                secondThrottleController.setWhichThrottle("S");
                                secondAddressPanel = secondThrottleFrame.getAddressPanel();
                                secondThrottleController.addThrottleControllerListener(this);
                                secondThrottleController.addControllerListener(this);
                            }
                            keepReading = secondThrottleController.sort(inPackage.substring(1));
                            break;
                        }

                        case '*':{  //  Heartbeat only

                            if (inPackage.length() > 1){
                                switch (inPackage.charAt(1)){
                                    
                                    case '+':{  //  trigger, turns on timed monitoring
                                        if (!isUsingHeartbeat){
                                            startEKG();
                                        }
                                        break;
                                    }
                                    
                                    case '-':{  //  turns off
                                        if (isUsingHeartbeat){
                                            stopEKG();
                                        }
                                        break;
                                    }
                                }
                                
                            }
                            
                            break;
                        }   //  end heartbeat block

                        case 'C':{  //  Prefix for confirmed package
                            switch (inPackage.charAt(1)){
                                case 'T':{
                                    keepReading = throttleController.sort(inPackage.substring(2));
                                    
                                    break;
                                }

                                default:{
                                    log.warn("Received unknown network package: "+inPackage);
                                    
                                    break;
                                }
                            }
                            
                            break;
                        }

                        case 'N':{  //  Prefix for deviceName
                            deviceName = inPackage.substring(1);
                            log.info("Received Name: "+deviceName);
                            break;
                        }

                        case 'H':{  //  Hardware
                            switch (inPackage.charAt(1)){
                                case 'U':{
                                    deviceUDID = inPackage.substring(2);
                                    for (int i = 0; i < listeners.size(); i++) {
                                        DeviceListener l = listeners.get(i);
                                        l.notifyDeviceInfoChanged(this);
                                    }
                                    break;
                                }


                            }
                            

                            break;
                        }   //  end hardware block
                        
                        case 'P':{  //  Start 'P'anel case
                            switch (inPackage.charAt(1)){
                                case 'P':{
                                    if (isTrackPowerAllowed){
                                        trackPower.handleMessage(inPackage.substring(2));
                                    }
                                    break;
                                }
                                case 'T':{
                                    if (isTurnoutAllowed){
                                        turnoutC.handleMessage(inPackage.substring(2));
                                    }
                                    break;
                                }
                                case 'R':{
                                    if (isRouteAllowed){
                                        routeC.handleMessage(inPackage.substring(2));
                                    }
                                    break;
                                }
                            }
                            break;
                        }   //  end panel block

                        case 'R':{  //  Start 'R'oster case
                            switch (inPackage.charAt(1)){
                                case 'C':{
                                    if (isConsistAllowed){
                                        consistC.handleMessage(inPackage.substring(2));
                                    }
                                    break;
                                }
                            }

                            break;
                        }   //  end roster block

                        case 'Q':{
                            if (secondThrottleController != null){
                                secondThrottleController.sort(inPackage);
                            }
                            keepReading = throttleController.sort(inPackage);
                            break;
                        }

                        default:{   //  If an unknown makes it through, do nothing.
                            log.warn("Received unknown network package: "+inPackage);
                            break;
                        }

                    }   //End of charAt(0) switch block

                    inPackage = null;
                }

            } catch (IOException exa){
                if (keepReading) log.error("readLine from device failed");
            } catch (IndexOutOfBoundsException exb){
                log.warn("Bad message \""+inPackage+"\" from device: "+getName());
            }
            try{    //  Some layout connections cannot handle rapid inputs
                Thread.sleep(20);
            }catch (java.lang.InterruptedException ex){}
        }while (keepReading);	//	'til we tell it to stop
        log.debug("Ending thread run loop for device");
        closeThrottles();

    }

    public void closeThrottles(){
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
        throttleWindow.dispose();
        throttleController = null;
        secondThrottleController = null;
        if (trackPower != null){
            trackPower.removeControllerListener(this);
        }
        if (turnoutC != null){
            turnoutC.removeControllerListener(this);
        }
        if (routeC != null){
            routeC.removeControllerListener(this);
        }
        if (consistC != null){
            consistC.removeControllerListener(this);
        }

        closeSocket();
        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            l.notifyDeviceDisconnected(this);

        }
    }
    

    public void closeSocket(){

        keepReading = false;
        try{
                device.close();
        }catch (IOException e){
                log.error("device socket won't close");
        }
    }
    
    public void startEKG(){
        isUsingHeartbeat = true;
        ekg = new Timer();
        TimerTask task = new TimerTask(){
            public void run(){  //  Drops on second pass
                if (!heartbeat){
                    //  Send eStop to each throttle
                    if (log.isDebugEnabled()) log.debug("Lost signal from: "+getName()+", sending eStop");
                    if (throttleController != null){
                        throttleController.sort("X");
                    }
                    if (secondThrottleController != null){
                        secondThrottleController.sort("X");
                    }
                }
                heartbeat = false;
            }

        };
        ekg.scheduleAtFixedRate(task, (long)(pulseInterval * 900), (long)(pulseInterval * 900));
    }
    
    public void stopEKG(){
        isUsingHeartbeat = false;
        if (ekg != null){
            ekg.cancel();
        }
        
    }

    private void addControllers(){
        if (isTrackPowerAllowed = WiThrottleManager.withrottlePreferencesInstance().isAllowTrackPower()){  //  Check prefs
            trackPower = WiThrottleManager.trackPowerControllerInstance();
            if (trackPower.isValid){
                if (log.isDebugEnabled()) log.debug("Track Power valid.");
                trackPower.addControllerListener(this);
                trackPower.sendCurrentState();
            }
        }
        if (isTurnoutAllowed = WiThrottleManager.withrottlePreferencesInstance().isAllowTurnout()){
            turnoutC = WiThrottleManager.turnoutControllerInstance();
            if (turnoutC.verifyCreation()){
                if (log.isDebugEnabled()) log.debug("Turnout Controller valid.");
                turnoutC.addControllerListener(this);
                turnoutC.sendTitles();
                turnoutC.sendList();
            }
        }
        if (isRouteAllowed = WiThrottleManager.withrottlePreferencesInstance().isAllowRoute()){
            routeC = WiThrottleManager.routeControllerInstance();
            if (routeC.verifyCreation()){
                if (log.isDebugEnabled()) log.debug("Route Controller valid.");
                routeC.addControllerListener(this);
                routeC.sendTitles();
                routeC.sendList();
            }
        }
        
        //  Consists can be selected regardless of pref, as long as there is a ConsistManager.
        consistC = WiThrottleManager.consistControllerInstance();
        if (consistC.verifyCreation()){
            if (log.isDebugEnabled()) log.debug("Consist Controller valid.");
            isConsistAllowed = WiThrottleManager.withrottlePreferencesInstance().isAllowConsist();
            consistC.addControllerListener(this);
            consistC.setIsConsistAllowed(isConsistAllowed);
            consistC.sendConsistListType();

            consistC.sendAllConsistData();
        }

        //  This checks prefs to see if make & break consists is allowed.
        if (isConsistAllowed){
        }
    }

    public String getUDID(){
        return deviceUDID;
    }

    public String getName(){
        return deviceName;
    }

    public String getCurrentAddress(){
        if (secondThrottleFrame != null) return (deviceAddress +", "+secondDeviceAddress);
        return deviceAddress;
    }

    public static String getWiTVersion(){
        return versionNumber;
    }

    public void sendPacketToDevice(String message){
        if (message == null) return; //  Do not send a null.
        if (log.isDebugEnabled()) log.debug("Sent: "+message);
        out.println(message + newLine);
    }

    /**
     * Add a DeviceListener
     * @param l
     */
    public void addDeviceListener(DeviceListener l) {
        if (listeners == null)
                listeners = new ArrayList<DeviceListener>(2);
        if (!listeners.contains(l))
                listeners.add(l);
    }

    /**
     * Remove a DeviceListener
     * @param l
     */
    public void removeDeviceListener(DeviceListener l) {
        if (listeners == null)
                return;
        if (listeners.contains(l))
                listeners.remove(l);
    }

    
    public void notifyControllerAddressFound(ThrottleController TC){
        //deviceAddress = throttle.getLocoAddress().toString();
        if (TC == secondThrottleController){    //  prefix with S to indicate second address
            secondDeviceAddress = secondAddressPanel.getCurrentAddress().toString();
            out.println("S" + secondDeviceAddress+newLine); //  response

            //  Send function labels for this roster entry
            if (secondAddressPanel.getRosterEntry() != null){
                out.println(sendFunctionLabels(secondAddressPanel.getRosterEntry()));
            }
            for (int i = 0; i < listeners.size(); i++) {
                DeviceListener l = listeners.get(i);
                l.notifyDeviceAddressChanged(secondAddressPanel.getCurrentAddress());
                if (log.isDebugEnabled()) log.debug("Notify DeviceListener: " + l.getClass() + " secondAddress: "+secondDeviceAddress);
            }
        }else{
            deviceAddress = addressPanel.getCurrentAddress().toString();
            out.println("T" + deviceAddress+newLine); //  response
        
            //  Send function labels for this roster entry
            if (addressPanel.getRosterEntry() != null){
                out.println(sendFunctionLabels(addressPanel.getRosterEntry()));
            }
            for (int i = 0; i < listeners.size(); i++) {
                DeviceListener l = listeners.get(i);
                l.notifyDeviceAddressChanged(addressPanel.getCurrentAddress());
                if (log.isDebugEnabled()) log.debug("Notify DeviceListener: " + l.getClass() + " address: "+deviceAddress);
            }
        }
        
    }
    
    public void notifyControllerAddressReleased(ThrottleController TC){
        
        if (TC == secondThrottleController){
            secondDeviceAddress = "Not Set";
            if (log.isDebugEnabled()) log.debug("Second address: " + secondDeviceAddress);
            out.println("S" + secondDeviceAddress+newLine);
            for (int i = 0; i < listeners.size(); i++) {
                DeviceListener l = listeners.get(i);
                l.notifyDeviceAddressChanged(secondAddressPanel.getCurrentAddress());
                if (log.isDebugEnabled()) log.debug("Notify DeviceListener: " + l.getClass() + " secondAddress: "+secondDeviceAddress);
            }
        }else{
            deviceAddress = "Not Set";
            out.println("T" + deviceAddress+newLine);
            for (int i = 0; i < listeners.size(); i++) {
                DeviceListener l = listeners.get(i);
                l.notifyDeviceAddressChanged(addressPanel.getCurrentAddress());
                if (log.isDebugEnabled()) log.debug("Notify DeviceListener: " + l.getClass() + " address: "+deviceAddress);
            }
        }
        
    }
    
    /**
     *  Format a package to be sent to the device for roster list selections.
     * @return String containing a formatted list of some of each RosterEntry's info.
     *          Include a header with the length of the string to be received.
     */
    public String sendRoster(){
        if (rosterList == null) rosterList = new ArrayList <RosterEntry>();
        List <RosterEntry> list = Roster.instance().matchingList(null, null, null, null, null, null, null);
        for (int i = 0; i < list.size(); i++) {
            RosterEntry roster = list.get(i);
            if(Roster.getRosterGroup()!=null){
                if(roster.getAttribute(Roster.getRosterGroupWP())!=null){
                    if(roster.getAttribute(Roster.getRosterGroupWP()).equals("yes"))
                        rosterList.add(roster);
                }
            } else rosterList.add(roster);
        }
        StringBuilder rosterString = new StringBuilder(rosterList.size()*25);
        for (int i=0;i<rosterList.size();i++){
            RosterEntry entry = rosterList.get(i);
            StringBuilder entryInfo = new StringBuilder(entry.getId()); //  Start with name
            entryInfo.append("}|{" + entry.getDccAddress());    //  Append address #
            if (entry.isLongAddress()){ //  Append length value
                entryInfo.append("}|{L");
            }else entryInfo.append("}|{S");
            
            rosterString.append("]\\["+entryInfo);  //  Put this info in as an item

        }
        rosterString.trimToSize();

        return ("RL" + rosterList.size() + rosterString + newLine);
    }
    
    public String sendFunctionLabels(RosterEntry rosterEntry){
        StringBuilder functionString = new StringBuilder();
        int i;  //  Used for # of labels sent
        for (i = 0; i<29; i++){
            functionString.append("]\\[");
            if (rosterEntry.getFunctionLabel(i) != null) functionString.append(rosterEntry.getFunctionLabel(i));
        }
        if ((secondAddressPanel != null) && (rosterEntry == secondAddressPanel.getRosterEntry())) {
            return ("RS" + i + functionString + newLine);
        }
        return ("RF" + i + functionString + newLine);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DeviceServer.class.getName());

}
