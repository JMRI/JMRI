package jmri.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to provide a mapping between port numbers and 'friendly' names,
 * aimed at users of Microsoft Windows.
 * <p>
 * Typically, most USB-Serial adapters have an alternate descriptive name as
 * well as the more usual technical COMx name.
 * <p>
 * This class attempts to provide a mapping between the technical COMx name and
 * the 'friendly' descriptive name which is stored within the Windows registry
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Matthew Harris Copyright (C) 2011
 */
public class PortNameMapper {

    private static final boolean getNew = true;

    private static final HashMap<String, SerialPortFriendlyName> SERIAL_PORT_NAMES = new HashMap<String, SerialPortFriendlyName>();

    private static boolean portsRetrieved = false;

    /*
     * We only go through the windows registry once looking for friendly names
     * if a new device is added then the new friendly name will not be picked up.
     */
    private static synchronized void getWindowsSerialPortNames() {
        if (portsRetrieved) {
            return;
        }
        /* Retrieving the friendly name is only available to windows clients 
         so if the OS is not windows, we make the portsRetrieved as completed
         and exit out.
         */
        if (!SystemType.isWindows()) {
            portsRetrieved = true;
            return;
        }

        getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\FTDIBUS\\");
        getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\KEYSPAN\\");
        getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\USB\\");
        //some modems are assigned in the HDAUDIO se we retrieve these
        getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\HDAUDIO\\");
        //some PCI software devices are located here
        getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\PCI\\");
        //some hardware devices are located here
        getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\ACPI\\");

        portsRetrieved = true;
    }

    private static void getDetailsFromWinRegistry(String path) {
        ArrayList<String> friendlyName = new ArrayList<>();
        System.out.format("Daniel: path: %s%n", path);
        if (!Advapi32Util.registryKeyExists(path)) {
            System.out.format("Daniel: reg key does not exists: path: %s%n", path);
            return;
        }
        String[] regEntries = Advapi32Util.registryGetKeys(path, getNew);
        if (regEntries == null) {
            System.out.format("Daniel: no register entries: path: %s%n", path);
            return;
        }
        System.out.format("Daniel: scan reg entries: %d%n", regEntries.length);
        for (String regEntry : regEntries) {
            String[] subRegEntries = Advapi32Util.registryGetKeys(path + regEntry, getNew);
            if (subRegEntries != null) {
                if (subRegEntries.length > 0) {
                    String name = null;
                    String port = null;
                    TreeMap<String, Object> values = Advapi32Util.registryGetValues(path + regEntry + "\\" + subRegEntries[0]);
                    if (values.containsKey("Class")) {
                        String pathKey = path + regEntry + "\\" + subRegEntries[0];
                        String deviceClass = Advapi32Util.registryGetStringValue(pathKey, "Class");
                        if (deviceClass.equals("Ports") || deviceClass.equals("Modem")) {
                            try {
                                name = Advapi32Util.registryGetStringValue(pathKey, "FriendlyName");
                                System.out.format("Daniel: friendly name: %s%n", name);
                            }
//                            catch (Win32Exception | NullPointerException e) {
                            catch (RuntimeException e) {
                                    log.warn("'FriendlyName' not found while querying 'HKLM.{}`.  JMRI cannot use the device, so will skip it.", pathKey );
                                    System.out.format("Daniel: friendly name not found: %s%n", pathKey);
                                    }
                            try {
                                String pathKey2 = path + regEntry + "\\" + subRegEntries[0] + "\\Device Parameters";
                                port = Advapi32Util.registryGetStringValue(pathKey2, "PortName");
                                System.out.format("Daniel: pathKey2: %s, port: %n", pathKey2, port);
//                            } catch (Win32Exception | NullPointerException e) {
                            } catch (RuntimeException e) {
                                // ...\\Device Parameters does not exist for some odd-ball Windows 
                                // serial devices, so cannot get the "PortName" from there.
                                // Instead, leave port as null and ignore the exception
                                System.out.format("Daniel: pathKey2 doesn't work%n");
                            }
                        }
                    }
                    if ((name != null) && (port != null)) {
                        System.out.format("Daniel: found name %s and port: %s%n", name, port);
                        SERIAL_PORT_NAMES.put(port, new SerialPortFriendlyName(port, name));
                    } else if (name != null) {
                        System.out.format("Daniel: found name: %s%n", name);
                        friendlyName.add(name);
                    }
                }
            }
        }
        System.out.format("Daniel: get comm ports%n");
        for (int i = 0; i < friendlyName.size(); i++) {
            int commst = friendlyName.get(i).lastIndexOf('(') + 1;
            int commls = friendlyName.get(i).lastIndexOf(')');
            String commPort = friendlyName.get(i).substring(commst, commls);
            System.out.format("Daniel: commPort: %s%n", commPort);
            SERIAL_PORT_NAMES.put(commPort, new SerialPortFriendlyName(commPort, friendlyName.get(i)));
        }
    }

    public static String getPortFromName(String name) {
        if (!portsRetrieved) {
            getWindowsSerialPortNames();
        }
        for (Entry<String, SerialPortFriendlyName> en : SERIAL_PORT_NAMES.entrySet()) {
            if (en.getValue().getDisplayName().equals(name)) {
                return en.getKey();
            }
        }
        return "";
    }

    public static HashMap<String, SerialPortFriendlyName> getPortNameMap() {
        if (!portsRetrieved) {
            getWindowsSerialPortNames();
        }
        return SERIAL_PORT_NAMES;
    }

    public static class SerialPortFriendlyName {

        String serialPortFriendly = "";
        boolean valid = false;

        public SerialPortFriendlyName(String port, String Friendly) {
            serialPortFriendly = Friendly;
            if (serialPortFriendly == null) {
                serialPortFriendly = port;
            } else if (!serialPortFriendly.contains(port)) {
                serialPortFriendly = Friendly + " (" + port + ")";
            }
        }

        public String getDisplayName() {
            return serialPortFriendly;
        }

        public boolean isValidPort() {
            return valid;
        }

        public void setValidPort(boolean boo) {
            valid = boo;
        }

    }
        private final static Logger log = LoggerFactory.getLogger(PortNameMapper.class);


}
