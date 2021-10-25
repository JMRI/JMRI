package jmri.util;

// import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

import java.util.TreeMap;

/**
 * This class simulates the com.sun.jna.platform.win32.Advapi32Util class.
 *
 * @author Daniel Bergqvist (C) 2021
 */
public class Advapi32Util {
    
    private static jmri.util.WinReg reg = new jmri.util.WinReg();
    
    public static String[] registryGetKeys(String keyPath, boolean getNew) {
        String[] list;
        try {
            System.out.format("%n%n");
            list = reg.getValue(jmri.util.WinReg.WRKey.HKLM, keyPath, null);
            System.out.format("%n%n");
        } catch (java.io.IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        
jmri.util.FileUtilSupport a;
jmri.util.PortNameMapper b;
jmri.util.com.sun.TransferActionListener c;
jmri.jmrit.beantable.oblock.TableFrames d;
jmri.implementation.JmriConfigurationManager e;


        String[] other = com.sun.jna.platform.win32.Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, keyPath);
        
        System.out.format("Length: %d :: %d%n", list.length, other.length);
        for (int i=0; i < list.length; i++) {
            if (i < other.length)
                System.out.format("%s :: %s%n", list[i], other[i]);
            else
                System.out.format("%s :: %s%n", list[i], "------");
        }
        
        if (getNew) return list;
        else return other;
    }

    public static boolean registryKeyExists(String key) {
        return com.sun.jna.platform.win32.Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, key);
    }

    public static TreeMap<String, Object> registryGetValues(String keyPath) {
        return com.sun.jna.platform.win32.Advapi32Util.registryGetValues(WinReg.HKEY_LOCAL_MACHINE, keyPath);
    }

    public static String registryGetStringValue(String key, String value) {
        return com.sun.jna.platform.win32.Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, key, value);
    }

}
