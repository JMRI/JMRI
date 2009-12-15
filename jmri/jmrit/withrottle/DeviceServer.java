
package jmri.jmrit.withrottle;

/**
 *  DeviceServer.java
 *  JmriPlugin
 *
 *  WiThrottle
 *
 *	@author Brett Hoffman   Copyright (C) 2009
 *	@author Created by Brett Hoffman on:
 *	@author 7/20/09.
 *	@version $Revision: 1.5 $
 *
 *	Thread with input and output streams for each connected device.
 *	Creates an invisible throttle window for each.
 *
 */


import java.net.Socket;
import java.io.*;
import java.util.ArrayList;

import jmri.DccThrottle;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleWindow;
import jmri.jmrit.throttle.AddressListener;

public class DeviceServer implements Runnable, AddressListener {
    private Socket device;
    String newLine = System.getProperty("line.separator");
    BufferedReader in = null;
    PrintWriter out = null;
    ArrayList<DeviceListener> listeners;
    String deviceName = "Unknown";
    String deviceAddress = "Not Set";

    ThrottleFrame throttleFrame;
    ThrottleWindow throttleWindow;
    ThrottleController throttleController;
    private boolean keepReading;



    DeviceServer(Socket socket){
        this.device = socket;
        if (listeners == null){
            listeners = new ArrayList<DeviceListener>(2);
        }
        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            log.debug("Notify Device Add");
            l.notifyDeviceConnected(this);

        }

        // Create a throttle instance for each device connected
        if (jmri.InstanceManager.throttleManagerInstance() != null){
            throttleWindow = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleWindow();
            throttleFrame = throttleWindow.getCurentThrottleFrame();
            throttleController = new ThrottleController(throttleFrame);
            throttleFrame.getAddressPanel().addAddressListener(this);

        }
        try{
            in = new BufferedReader(new InputStreamReader(device.getInputStream()));
            out = new PrintWriter(device.getOutputStream(),true);

        } catch (IOException e){
            log.error("Stream creation failed (DeviceServer)");
            return;
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
                    if (log.isDebugEnabled()) log.debug("Recieved: " + inPackage);
                    switch (inPackage.charAt(0)){
                        case 'T':{
                            keepReading = throttleController.sort(inPackage.substring(1));
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
        if (throttleController != null) throttleController.shutdownThrottle();
        throttleWindow.dispose();
        throttleController = null;
        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            l.notifyDeviceDisconnected(this);

        }

        throttleFrame.getAddressPanel().removeAddressListener(this);
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
        
        return deviceAddress;
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


    public void notifyAddressChosen(int newAddress, boolean isLong){

    }


    public void notifyAddressReleased(int address, boolean isLong){
        deviceAddress = "Not Set";
        
        out.println("T" + deviceAddress+newLine);
        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            log.debug("Notify Address released");
            l.notifyDeviceAddressChanged(throttleFrame.getAddressPanel().getCurrentAddress());

        }
    }


    public void notifyAddressThrottleFound(DccThrottle throttle){
        deviceAddress = throttle.getLocoAddress().toString();
        
        out.println("T" + deviceAddress+newLine); //  response
        for (int i = 0; i < listeners.size(); i++) {
            DeviceListener l = listeners.get(i);
            log.debug("Notify Address Throttle Found");
            l.notifyDeviceAddressChanged(throttleFrame.getAddressPanel().getCurrentAddress());

        }
        
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DeviceServer.class.getName());

}
