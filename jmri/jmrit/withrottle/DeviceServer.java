
package jmri.jmrit.withrottle;

/**
 *  DeviceServer.java
 *
 *  WiThrottle
 *
 *	@author Brett Hoffman   Copyright (C) 2009
 *	@author Created by Brett Hoffman on:
 *	@author 7/20/09.
 *	@version $Revision: 1.8 $
 *
 *	Thread with input and output streams for each connected device.
 *	Creates an invisible throttle window for each.
 *
 *      Sorting codes:
 *      'T'hrottle - sends to throttleController
 *      'S'econdThrottle - sends to secondThrottleController
 *      'C' - Not used anymore except to provide backward compliance, same as 'T'
 *      'N'ame of device
 *      'Q'uit - device has quit, close its throttleWindow
 *
 */


import java.net.Socket;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleWindow;
import jmri.jmrit.throttle.AddressPanel;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

public class DeviceServer implements Runnable, ThrottleControllerListener {

    //  Manually increment as features are added
    private static final String versionNumber = "1.1";

    private Socket device;
    String newLine = System.getProperty("line.separator");
    BufferedReader in = null;
    PrintWriter out = null;
    ArrayList<DeviceListener> listeners;
    String deviceName = "Unknown";
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
            addressPanel = throttleFrame.getAddressPanel();
            throttleController.addThrottleControllerListener(this);

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
                                secondAddressPanel = secondThrottleFrame.getAddressPanel();
                                secondThrottleController.addThrottleControllerListener(this);
                            }
                            keepReading = secondThrottleController.sort(inPackage.substring(1));
                            break;
                        }

                        case 'C':{  //  Prefix for confirmed package
                            switch (inPackage.charAt(1)){
                                case 'T':{
                                    keepReading = throttleController.sort(inPackage.substring(2));
                                    
                                    break;
                                }

                                default:{
                                    log.warn("Received unknown network package.");
                                    
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

                        case 'Q':{
                            if (secondThrottleController != null){
                                secondThrottleController.sort(inPackage);
                            }
                            keepReading = throttleController.sort(inPackage);
                            break;
                        }

                        default:{   //  If an unknown makes it through, do nothing.
                            log.warn("Received unknown network package.");
                            break;
                        }

                    }   //End of charAt(0) switch block

                    inPackage = null;
                }

            } catch (IOException e){
                if (keepReading) log.error("readLine from device failed");
            }
        }while (keepReading);	//	'til we tell it to stop
        log.debug("Ending thread run loop for device");
        if (throttleController != null) {
            throttleController.shutdownThrottle();
            throttleController.removeThrottleControllerListener(this);
        }
        if (secondThrottleController != null) {
            secondThrottleController.shutdownThrottle();
            secondThrottleController.removeThrottleControllerListener(this);
        }
        throttleWindow.dispose();
        throttleController = null;
        secondThrottleController = null;
        closeSocket();
        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            l.notifyDeviceDisconnected(this);

        }

        //addressPanel.removeAddressListener(this);
    }

    

    public void closeSocket(){

        keepReading = false;
        try{
                device.close();
        }catch (IOException e){
                log.error("device socket won't close");
        }
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
