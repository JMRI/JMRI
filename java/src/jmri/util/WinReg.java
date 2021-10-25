package jmri.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Sample Java Application to access to windows registry through 
 * the windows commandline application reg.exe
 * 
 * Just a sample application: 
 * 
 * Open your command prompt and enter REG /? to add missing features
 * 
 * This class is fetched from this web page by Sven Piehl:
 * https://www.codeproject.com/Answers/1239694/How-can-I-access-windows-registry-by-java#answer1
 */
public class WinReg {

    /**
     * Success status code
     */
    public static final int REG_SUCCESS = 0;

    /**
     * Failure status code
     */
    public static final int REG_FAILURE = 1;

    /**
     * Implemented root-keys<br>
     * HKLM HKEY_LOCAL_MACHINE elevated privileges needed<br>
     * HKCU HKEY_CURRENT_USER <br>
     * HKCR HKEY_CLASSES_ROOT elevated privileges needed<br>
     * HKU HKEY_USER  <br>
     * HKCC HKEY_CURRENT_CONFIG elevated privileges needed<br>
     */
    public enum WRKey {
        HKLM,  HKCU , HKCR , HKU , HKCC
    }

    /**
     * Registry data-types
     */
    public enum WRType {
        REG_SZ, REG_MULTI_SZ, REG_EXPAND_SZ,
        REG_DWORD, REG_QWORD, REG_BINARY, REG_NONE
    }

    /**
     * Creates a new string for the registry cli.
     * 
     * @param hkey the  root key [ HKLM , HKCU ] 
     * @param key the name of the key  SOFTWARE\WINDOWS
     * @param valueName the name of the value
     * @param data 
     * @param type 
     * @param force override an existing key ?
     * 
     * @see WRKey
     * @see WRType
     * @return the string value to the registry cli
     */
    public String createRegString(WRKey hkey, String key, String valueName, byte[] data,  WRType type, boolean force) {
        String keyString = " "+hkey+"\\" + key;
        String valueString = valueName!=null 	? " /v "+ valueName : "" ;
        String dataString =  data != null 		? (" " + ( data.length>0 ? " /d " + new String(data) : "")):"";
        String typeString = type != null 		? " /t " + type : "";

//        return keyString + valueString + dataString + typeString +  (force ? " /f" : "");
        return keyString + valueString + dataString + typeString;
    }

    /**
     * Shows a registry value
     * @param hkey The root-key [ HKLM , HKCU ] 
     * @param key the key name to open eg. SOFTWARE\TEST\ABCD
     * @param valueName the name of the value 
     * @return true on success
     * @throws IOException
     * @throws InterruptedException
     * 
     * @see WRKey
     */
    public String[] getValue(WRKey hkey, String key, String valueName)  throws IOException, InterruptedException{
        String regString = createRegString(hkey,key,valueName,null,null,true);
        System.out.format("getValue: '%s'%n", "REG QUERY " + regString);
        Process proc = Runtime.getRuntime().exec("REG QUERY " + regString);
        proc.waitFor();

        List<String> list = new ArrayList<>();
        if(proc.exitValue() == REG_SUCCESS) {

            try (Scanner sc = new Scanner(proc.getInputStream())) {
                String str;
                do {
                    str = sc.nextLine();
                    System.out.println(str);
                    String keyStr = "HKEY_LOCAL_MACHINE\\"+key;
                    if (!keyStr.endsWith("\\")) keyStr += "\\";
                    if (!str.trim().isEmpty()) list.add(str.substring(keyStr.length()));
                } while (sc.hasNext());
            }
            
            return list.toArray(new String[0]);
        }
        else {
            System.err.format("Query failure..: %d%n", proc.exitValue());
            return null;
        }

//        return proc.exitValue() == REG_SUCCESS;
    }

    /**
     * 
     * @param hkey The root-key [ HKLM , HKCU ] 
     * @param key the key name to open eg. SOFTWARE\TEST\ABCD
     * @param valueName the name of the value 
     * @param withChildren view all subdirectories
     * @return true on success
     * @throws IOException
     * @throws InterruptedException
     * 
     * @see WRKey
     */
    public boolean getAllValues(WRKey hkey, String key, String valueName, boolean withChildren) throws IOException, InterruptedException {
        String regString = createRegString(hkey,key,null,null,null,false);
        Process proc = Runtime.getRuntime().exec("REG QUERY " + regString + "\\" + valueName + " " + (withChildren? " /s" :" "));
        proc.waitFor();


        if(proc.exitValue()==REG_SUCCESS) {
            try (Scanner sc = new Scanner(proc.getInputStream())) {
                String str;
                do {
                    str = sc.nextLine();
                    System.out.println(str);
                } while(sc.hasNext());
            }
        } else {
            System.err.println("Query failure..\n" + regString);
        }

        return proc.exitValue() == REG_SUCCESS;
    }


    /**
     * Sample program to access the windows registry
     * 
     * <br>
     * NOTE: To create, modify or delete entries on HKEY_LOCAL_MACHINE you'll need elevated privileges
     * 
     * @param args
     */
    public static void main(String ... args) {

        WinReg reg = new WinReg();

        try {
            // list all registry values at HKEY_CURRENT_USER\\SOFTWARE\AAAA  with subdirectories
            if(! reg.getAllValues(WRKey.HKCU, "SOFTWARE\\AAAA", "", true) ) {
                System.err.println("Error: could not show the values");
            }
        }
        catch( InterruptedException | IOException e) {
            System.err.println("An error occurred. " + e.getMessage() );
        }
    }

}