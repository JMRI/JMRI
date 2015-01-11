// PortNameMapper.java
package jmri.util;

import at.jta.Key;
import at.jta.Regor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Class used to provide a mapping between port numbers and 'friendly'
 * names, aimed at users of Microsoft Windows.
 * <p>
 * Typically, most USB-Serial adapters have an alternate descriptive
 * name as well as the more usual technical COMx name.
 * <p>
 * This class attempts to provide a mapping between the technical COMx
 * name and the 'friendly' descriptive name which is stored within the
 * Windows registry
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author      Kevin Dickerson  Copyright (C) 2011
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 */
public class PortNameMapper {
    
    private static HashMap<String, SerialPortFriendlyName> serialPortNames = new HashMap<String, SerialPortFriendlyName>();
    
    private static boolean portsRetrieved = false;
    
    static {
        getWindowsSerialPortNames();
    }
    
    /*
     * We only go through the windows registry once looking for friendly names
     * if a new device is added then the new friendly name will not be picked up.
     */
    private static synchronized void getWindowsSerialPortNames(){
        if(portsRetrieved)
            return;
        /* Retrieving the friendly name is only available to windows clients 
           so if the OS is not windows, we make the portsRetrieved as completed
           and exit out.
        */
        if(!SystemType.isWindows()){
            portsRetrieved = true;
            return;
        }
        
        try {
            Regor reg = new Regor();
            getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\FTDIBUS\\", reg);
            getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\KEYSPAN\\", reg);
            getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\USB\\", reg);
            //some modems are assigned in the HDAUDIO se we retrieve these
            getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\HDAUDIO\\", reg);
            //some PCI software devices are located here
            getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\PCI\\", reg);
            //some hardware devices are located here
            getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\ACPI\\", reg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        portsRetrieved = true;
    }
    
    private static void getDetailsFromWinRegistry(String path, Regor reg){
        ArrayList<String> friendlyName = new ArrayList<String>();
        try {
            List<?> regentry = reg.listKeys(Regor.HKEY_LOCAL_MACHINE,path);
            if(regentry==null)
                return;
            for(int i = 0; i<regentry.size(); i++){
                List<?> regSubEntry = reg.listKeys(Regor.HKEY_LOCAL_MACHINE, path + regentry.get(i));
                if(regSubEntry!=null){
                    if(regSubEntry.size()>0){
                        String name = null;
                        String port = null;
                        List<?> values = reg.listValueNames(Regor.HKEY_LOCAL_MACHINE,path + regentry.get(i)+"\\"+regSubEntry.get(0));
                        if(values.contains("Class")){
                            Key pathKey = reg.openKey(Regor.HKEY_LOCAL_MACHINE, path + regentry.get(i)+"\\"+regSubEntry.get(0), Regor.KEY_READ);
                            String deviceClass = reg.readValueAsString(pathKey,"Class");
                            if(deviceClass.equals("Ports") || deviceClass.equals("Modem")){
                                name = reg.readValueAsString(pathKey,"FriendlyName");
                                Key pathKey2 = reg.openKey(Regor.HKEY_LOCAL_MACHINE, path + regentry.get(i)+"\\"+regSubEntry.get(0)+"\\Device Parameters", Regor.KEY_READ);
                                port = reg.readValueAsString(pathKey2, "PortName");
                                reg.closeKey(pathKey2);
                            }
                            reg.closeKey(pathKey);
                        }
                        if((name!=null) && (port!=null)){
                            serialPortNames.put(port, new SerialPortFriendlyName(port, name));
                        } else if(name!=null)
                            friendlyName.add(name);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i<friendlyName.size(); i++){
            int commst = friendlyName.get(i).lastIndexOf('(')+1;
            int commls = friendlyName.get(i).lastIndexOf(')');
            String commPort = friendlyName.get(i).substring(commst, commls);
            serialPortNames.put(commPort, new SerialPortFriendlyName(commPort, friendlyName.get(i)));
        }
    }
    
    public static String getPortFromName(String name){
        if (!portsRetrieved)
            getWindowsSerialPortNames();
        for(Entry<String, SerialPortFriendlyName> en : serialPortNames.entrySet()){
            if(en.getValue().getDisplayName().equals(name))
                return en.getKey();
        }
        return "";
    }
    
    public static HashMap<String, SerialPortFriendlyName> getPortNameMap() {
        if (!portsRetrieved)
            getWindowsSerialPortNames();
        return serialPortNames;
    }
    
    public static class SerialPortFriendlyName{
        
        String serialPortFriendly = "";
        boolean valid = false;
        
        public SerialPortFriendlyName(String port, String Friendly){
            serialPortFriendly = Friendly;
            if(serialPortFriendly==null)
                serialPortFriendly=port;
            else if(!serialPortFriendly.contains(port)){
                serialPortFriendly = Friendly+" ("+port+")";
            }
        }
        
        public String getDisplayName(){
            return serialPortFriendly;
        }
        
        public boolean isValidPort(){ return valid; }
        
        public void setValidPort(boolean boo) { valid = boo; }
    
    }
    
}
