package jmri.jmrix.can.cbus.node;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Static Methods relating to nodes ( modules ).
 *
 * @author Steve Young (C) 2019
 */
public class CbusNodeConstants {

    /**
     * Node Parameters
     *
     * Para 0 Number of parameters
     * Para 1 The manufacturer ID
     * Para 2 Minor code version as an alphabetic character (ASCII)
     * Para 3 Manufacturer module identifier as a HEX numeric
     * Para 4 Number of supported events as a HEX numeric
     * Para 5 Number of Event Variables per event as a HEX numeric
     * Para 6 Number of supported Node Variables as a HEX numeric
     * Para 7 Major version
     * Para 8 Node flags
     * Para 9 Processor type
     * Para 10 Bus type
     * Para 11 load address, 4 bytes
     * Para 15 CPU manufacturer's id as read from the chip config space, 4 bytes
     * Para 19 CPU manufacturer code
     * Para 20 Beta revision (numeric), or 0 if release
     *                
     */

    /**
     * Set traits for a node where there is a minor deviance to MERG CBUS protocol
     * or provide extra info. which is missing for a known module firmware.
     * @param node The CbusNode object we are setting the traits for
     */
    public static void setTraits( CbusNode node ){
        
        // defaults
        node.setsendsWRACKonNVSET(true);
        
        if ( node.getParameter(1) == 165 ) { // MERG MODULE
            if ( node.getParameter(3) == 29 ) { // CANPAN
                node.setsendsWRACKonNVSET(false);
            }
            if ( node.getParameter(3) == 10 // CANCMD
                || node.getParameter(3) ==  55 // or CANCSB 
                || node.getParameter(3) == 12 // or CANBC
            ) { 
                if ( node.getParameter(7) == 4 ) { // v4 Firmware
                    node.resetNodeEvents(); // sets num events to 0 as does not respond to RQEVN
                    node.setStatResponseFlagsAccurate(false);
                }
            }
        }
    }
    
    // reset a Dummy Node to its virgin condition
    public static void setDummyNodeParameters( CbusDummyNode thisNode, int manu, int type ){
        
        if ( manu == 165 ) { // MERG MODULE
            if ( type == 29 ) { // CANPAN
                
                int[] _params = new int[]{ 
                20, /* 0 num parameters   */
                165, /* 1 manufacturer ID   */
                89, /* 2 Minor code version   */
                29, /* 3 Manufacturer module identifier   */
                128, /* 4 Number of supported events   */
                13, /* 5 Number of Event Variables per event   */
                1, /* 6 Number of Node Variables   */
                1, /* 7 Major version   */
                13, /* 8 Node flags   */ 
                13, /* 9 Processor type   */
                1, /* 10 Bus type   */
                0, /* 11 load address, 1/4 bytes   */
                8, /* 12 load address, 2/4 bytes   */
                0, /* 13 load address, 3/4 bytes   */
                0, /* 14 load address, 4/4 bytes   */
                0, /* 15 CPU manufacturer's id 1/4  */
                0, /* 16 CPU manufacturer's id 2/4  */
                0, /* 17 CPU manufacturer's id 3/4  */
                0, /* 18 CPU manufacturer's id 4/4  */
                1, /* 19 CPU manufacturer code   */
                1, /* 20 Beta revision   */
                };
                
                thisNode.setParameters(_params);
                thisNode.setNVs( new int[]{ 1 , 0 } ); // 1 NV, NV1 set at 0
                thisNode.setNodeNameFromName("PAN");
            }
            
            else if ( type == 255 ) {
                
                // 255 parameters could have unintended future
                // consequences so staying with the standard 20
                // for now
                int[] _params = new int[]{ 
                20, /* 0 num parameters   */
                165, /* 1 manufacturer ID   */
                89, /* 2 Minor code version   */
                255, /* 3 Manufacturer module identifier   */
                255, /* 4 Number of supported events   */
                255, /* 5 Number of Event Variables per event   */
                255, /* 6 Number of Node Variables   */
                1, /* 7 Major version   */
                13, /* 8 Node flags   */ 
                13, /* 9 Processor type   */
                1, /* 10 Bus type   */
                0, /* 11 load address, 1/4 bytes   */
                8, /* 12 load address, 2/4 bytes   */
                0, /* 13 load address, 3/4 bytes   */
                0, /* 14 load address, 4/4 bytes   */
                0, /* 15 CPU manufacturer's id 1/4  */
                0, /* 16 CPU manufacturer's id 2/4  */
                0, /* 17 CPU manufacturer's id 3/4  */
                0, /* 18 CPU manufacturer's id 4/4  */
                1, /* 19 CPU manufacturer code   */
                1, /* 20 Beta revision   */
                };
                
                thisNode.setParameters(_params);
                
                int[] nvArray = new int[256];
                nvArray[0]=255;
                for (int i = 1; i < nvArray.length; i++) {
                    nvArray[i] = i;
                }
                
                // create array of event variables
                int[] evVarArray = new int[255];
                for (int i = 0; i < evVarArray.length; i++) {
                    evVarArray[i] = i+1;
                }
                
                for (int i = 1; i < 256; i++) {
                    
                    CbusNodeEvent singleEv = new CbusNodeEvent(i,i,-1,i,255);
                    singleEv.setEvArr(evVarArray);
                    thisNode.addNewEvent(singleEv);
                    
                }
                
                thisNode.setNVs( nvArray );
                thisNode.setNodeNameFromName("TSTMAXND");
                
            }
            
            else {
            
                // default MERG module in SLiM mode
                thisNode.setParameters( new int[]{ 8,165,0,0,0,0,0,0,0 } );
                thisNode.setNVs( new int[]{ 0 } );
            }
        }
        else {
            thisNode.setParameters( new int[]{ 8,165,0,0,0,0,0,0,0 } );
            thisNode.setNVs( new int[]{ 0 } );
        }
        
        setTraits( thisNode );
        
    }
    
    /**
     * Return a string representation of a decoded Module Manufacturer
     * @param man manufacturer int
     * @return decoded CBUS message
     */
    public static String getManu(int man) {
        if (man < 1 ) {
            return ("");
        }
        // look for the manufacturer
        String format = manMap.get(man);
        if (format == null) {
            return "Manufacturer " + man;
        } else {
            return format; 
        }
    }
    
    /**
     * Hashmap for decoding Module Manufacturers
     */
    private static final Map<Integer, String> manMap = createManMap();

    /*
     * Populate hashmap with format strings
     *
     */
    private static Map<Integer, String> createManMap() {
        Map<Integer, String> result = new HashMap<>();
        result.put(70, "ROCRAIL"); // NOI18N
        result.put(80, "SPECTRUM"); // NOI18N
        result.put(165, "MERG"); // NOI18N
        return Collections.unmodifiableMap(result);
    }
    
    /**
     * Return a string representation of a decoded Bus Type
     * @param type Bus type
     * @return decoded CBUS message
     */
    public static String getBusType(int type) {
        // look for the opcode
        String format = busMap.get(type);
        if (format == null) {
            return "Unknown";
        } else {
            return format; 
        }
    }
    
    /**
     * Hashmap for decoding Bus Type
     */
    private static final Map<Integer, String> busMap = createBusMap();

    /*
     * Populate hashmap with format strings
     *
     */
    private static Map<Integer, String> createBusMap() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "CAN"); // NOI18N
        result.put(2, "ETH"); // NOI18N
        result.put(3, "MIWI"); // NOI18N
        return Collections.unmodifiableMap(result);
    }
    
    // manufacturer specific stuff from here down
    // do not rely on these as defs, may be moved to module config file.
    
    // getModuleTypeExtra
    // getModuleSupportLink

    /**
     * Return a string representation of a decoded Module Name for
     * manufacturer 165 MERG.
     * @param man int manufacturer
     * @param type module type int
     * @return decoded String module type name else empty string
     */
    public static String getModuleType(int man, int type) {
        String format="";
        if (man == 165) {
            format = type165Map.get(type);
        }
        else if (man == 70) {
            format = type70Map.get(type);
        }
        else if (man == 80) {
            format = type80Map.get(type);
        }
        
        if ( format == null ){
            return ("");
        }
        else {
            return format;
        }
    }
    
    /**
     * Hashmap for decoding Module Names
     */
    private static final Map<Integer, String> type165Map = createType165Map();
    private static final Map<Integer, String> type70Map = createType70Map();
    private static final Map<Integer, String> type80Map = createType80Map();
    
    /*
     * Populate hashmap with format strings for manufacturer 165 MERG
     */
    private static Map<Integer, String> createType165Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(0, "SLIM"); // NOI18N
        result.put(1, "CANACC4"); // NOI18N
        result.put(2, "CANACC5"); // NOI18N
        result.put(3, "CANACC8"); // NOI18N
        result.put(4, "CANACE3"); // NOI18N
        result.put(5, "CANACE8C"); // NOI18N
        result.put(6, "CANLED"); // NOI18N
        result.put(7, "CANLED64"); // NOI18N
        result.put(8, "CANACC4_2"); // NOI18N
        result.put(9, "CANCAB"); // NOI18N
        result.put(10, "CANCMD"); // NOI18N
        result.put(11, "CANSERVO"); // NOI18N
        result.put(12, "CANBC"); // NOI18N
        result.put(13, "CANRPI"); // NOI18N
        result.put(14, "CANTTCA"); // NOI18N
        result.put(15, "CANTTCB"); // NOI18N
        result.put(16, "CANHS"); // NOI18N
        result.put(17, "CANTOTI"); // NOI18N
        result.put(18, "CAN8I8O"); // NOI18N
        result.put(19, "CANSERVO8C"); // NOI18N
        result.put(20, "CANRFID"); // NOI18N
        result.put(21, "CANTC4"); // NOI18N
        result.put(22, "CANACE16C"); // NOI18N
        result.put(23, "CANIO8"); // NOI18N
        result.put(24, "CANSNDX"); // NOI18N
        result.put(25, "CANEther"); // NOI18N
        result.put(26, "CANSIG64"); // NOI18N
        result.put(27, "CANSIG8"); // NOI18N
        result.put(28, "CANCOND8C"); // NOI18N
        result.put(29, "CANPAN"); // NOI18N
        result.put(30, "CANACE3C"); // NOI18N
        result.put(31, "CANPanel"); // NOI18N
        result.put(32, "CANMIO"); // NOI18N
        result.put(33, "CANACE8MIO"); // NOI18N
        result.put(34, "CANSOL"); // NOI18N
        result.put(35, "CANBIP"); // NOI18N
        result.put(36, "CANCDU"); // NOI18N
        result.put(37, "CANACC4CDU"); // NOI18N
        result.put(38, "CANWiBase"); // NOI18N
        result.put(39, "WiCAB"); // NOI18N
        result.put(40, "CANWiFi"); // NOI18N
        result.put(41, "CANFTT"); // NOI18N
        result.put(42, "CANHNDST"); // NOI18N
        result.put(43, "CANTCHNDST"); // NOI18N
        result.put(44, "CANRFID8"); // NOI18N
        result.put(45, "CANmchRFID"); // NOI18N
        result.put(46, "CANPiWi"); // NOI18N
        result.put(47, "CAN4DC"); // NOI18N
        result.put(48, "CANELEV"); // NOI18N
        result.put(49, "CANSCAN"); // NOI18N
        result.put(50, "CANMIO_SVO"); // NOI18N
        result.put(51, "CANMIO_INP"); // NOI18N
        result.put(52, "CANMIO_OUT"); // NOI18N
        result.put(53, "CANBIP_OUT"); // NOI18N
        result.put(54, "CANASTOP"); // NOI18N
        result.put(55, "CANCSB"); // NOI18N
        result.put(56, "CANMAGOT"); // NOI18N
        result.put(57, "CANACE16CMIO"); // NOI18N
        result.put(58, "CANPiNODE"); // NOI18N
        result.put(59, "CANDISP"); // NOI18N
        result.put(60, "CANCOMPUTE"); // NOI18N
        
        result.put(253, "CANUSB"); // NOI18N
        result.put(254, "EMPTY"); // NOI18N
        result.put(255, "CAN_SW"); // NOI18N
        return Collections.unmodifiableMap(result);
    }

    /*
     * Populate hashmap with format strings
     *
     */
    private static Map<Integer, String> createType70Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "CANGC1"); // NOI18N
        result.put(2, "CANGC2"); // NOI18N
        result.put(3, "CANGC3"); // NOI18N
        result.put(4, "CANGC4"); // NOI18N
        result.put(5, "CANGC5"); // NOI18N
        result.put(6, "CANGC6"); // NOI18N
        result.put(7, "CANGC7"); // NOI18N
        result.put(11, "CANGC1e"); // NOI18N
        return Collections.unmodifiableMap(result);
    }

    
    /*
     * Populate hashmap with format strings
     *
     */
    private static Map<Integer, String> createType80Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "AMCTRLR"); // NOI18N
        result.put(2, "DUALCAB"); // NOI18N
        return Collections.unmodifiableMap(result);
    }
    
    
    /**
     * Return a string representation of extra module info
     * @param man int manufacturer code
     * @param type int module type
     * @return string value of extra module info
     */
    public static String getModuleTypeExtra(int man, int type) {
        String format="";
        if (man == 165) {
            format = extra165Map.get(type);
        }
        else if (man == 70) {
            format = extra70Map.get(type);
        }
        else if (man == 80) {
            format = extra80Map.get(type);
        }
        return format;
    }
    
    /**
     * Hashmap for decoding Module extra info
     */
    private static final Map<Integer, String> extra165Map = createExtra165Map();
    private static final Map<Integer, String> extra70Map = createExtra70Map();
    private static final Map<Integer, String> extra80Map = createExtra80Map();
    
    /*
     * Populate hashmap with format strings
     */
    private static Map<Integer, String> createExtra165Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(0, "Default for SLiM nodes");
        result.put(1, "Solenoid point driver");
        result.put(2, "Motorised point driver");
        result.put(3, "8 digital outputs ( + 8 inputs if modded)");
        result.put(4, "Control panel switch/button encoder");
        result.put(5, "8 digital inputs");
        result.put(6, "64 led driver");
        result.put(7, "64 led driver (multi leds per event)");
        result.put(8, "12v version of CANACC4 Solenoid point driver");
        result.put(9, "CANCAB hand throttle");
        result.put(10, "Command Station");
        result.put(11, "8 servo driver (on canacc8 or similar hardware)");
        result.put(12, "BC1a Command Station");
        result.put(13, "RPI and RFID interface");
        result.put(14, "Turntable controller (turntable end)");
        result.put(15, "Turntable controller (control panel end)");
        result.put(16, "Handset controller for old BC1a type handsets");
        result.put(17, "Track occupancy detector");
        result.put(18, "8 inputs 8 outputs");
        result.put(19, "Canservo with servo position feedback");
        result.put(20, "RFID input");
        result.put(21, "CANTC4");
        result.put(22, "16 inputs");
        result.put(23, "8 way I/O");
        result.put(24, "CANSNDX");
        result.put(25, "Ethernet interface");
        result.put(26, "Multiple aspect signalling for CANLED module");
        result.put(27, "Multiple aspect signalling for CANACC8 module");
        result.put(28, "Conditional event generation");
        result.put(29, "Control panel 32 Outputs + 32 Inputs");
        result.put(30, "Newer version of CANACE3 firmware");
        result.put(31, "Control panel 64 Inputs / 64 Outputs");
        result.put(32, "Multiple I/O");
        result.put(33, "Multiple IO module emulating ACE8C");
        result.put(34, "Solenoid driver module");
        result.put(35, "Bipolar IO module with additional 8 I/O pins");
        result.put(36, "Solenoid driver module with additional 6 I/O pins");
        result.put(37, "CANACC4 firmware ported to CANCDU");
        result.put(38, "CAN to MiWi base station");
        result.put(39, "Wireless cab using MiWi protocol");
        result.put(40, "CAN to WiFi connection with Withrottle to CBUS protocol conversion");
        result.put(41, "Turntable controller configured using FLiM");
        result.put(42, "Handset (alternative to CANCAB)");
        result.put(43, "Touchscreen handset");
        result.put(44, "multi-channel RFID reader");
        result.put(45, "either a 2ch or 8ch RFID reader");
        result.put(46, "Raspberry Pi based module for WiFi");
        result.put(47, "DC train controller");
        result.put(48, "Nelevator controller");
        result.put(49, "128 switch inputs");
        result.put(50, "16MHz 25k80 version of CANSERVO8c");
        result.put(51, "16MHz 25k80 version of CANACE8MIO");
        result.put(52, "16MHz 25k80 version of CANACC8");
        result.put(53, "16MHz 25k80 version of CANACC5");
        result.put(54, "DCC stop generator");
        result.put(55, "Command Station with 3A booster");
        result.put(56, "Magnet on Track detector");
        result.put(57, "16 input equivaent to CANACE8C");
        result.put(58, "CBUS module based on Raspberry Pi");
        result.put(59, "25K80 version of CANLED64");
        result.put(60, "Event processing engine");
        
        result.put(253, "USB interface");
        result.put(254, "Empty module, bootloader only");
        result.put(255, "Software nodes");
        return Collections.unmodifiableMap(result);
    }
    
    /*
     * Populate hashmap with format strings
     * extra text for Rocrail Modules
     */
    private static Map<Integer, String> createExtra70Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "RS232 PC interface.");
        result.put(2, "16 I/O.");
        result.put(3, "Command station (derived from cancmd).");
        result.put(4, "8 channel RFID reader.");        
        result.put(5, "Cab for fixed panels (derived from cancab).");        
        result.put(6, "4 channel servo controller.");        
        result.put(7, "Fast clock module.");        
        result.put(11, "CAN Ethernet interface.");
        return Collections.unmodifiableMap(result);
    }    
    
    /*
     * Populate hashmap with format strings
     * extra text for Animated Modeller module types
     */
    private static Map<Integer, String> createExtra80Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "Animation controller (firmware derived from cancmd).");
        result.put(2, "Dual cab based on cancab.");
        return Collections.unmodifiableMap(result);
    }   

    
    /**
     * Return a string representation of Module Support Link
     * @param man int manufacturer ID
     * @param type int module type ID
     * @return string module support link, else empty string
     */
    public static String getModuleSupportLink(int man, int type) {
        String format="";
        if (man == 165) {
            format = link165Map.get(type);
        }
        else if (man == 70) {
            format = link70Map.get(type);
        }
        if ( format == null ){
            return ("");
        }
        return format;
    }
    
    private static final Map<Integer, String> link165Map = createLink165Map();
    private static final Map<Integer, String> link70Map = createLink70Map();
    
    /*
     * Populate hashmap with merg module support links
     */
    private static Map<Integer, String> createLink165Map() {
        Map<Integer, String> result = new HashMap<>();
        
        result.put(1, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canacc4"); // NOI18N
        result.put(2, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canacc5"); // NOI18N
        result.put(3, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canacc8"); // NOI18N
        result.put(4, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canace3"); // NOI18N
        result.put(5, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canace8c"); // NOI18N
        // result.put(6, "CANLED"); // NOI18N
        result.put(7, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canled64"); // NOI18N
        result.put(8, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canacc4"); // NOI18N
        result.put(9, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cancab"); // NOI18N
        result.put(10, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cancmd"); // NOI18N
        // result.put(11, "CANSERVO"); // NOI18N
        // result.put(12, "CANBC"); // NOI18N
        // result.put(13, "CANRPI"); // NOI18N
        result.put(14, "https://www.merg.org.uk/merg_wiki/doku.php?id=other_download:turntable"); // NOI18N
        result.put(15, "https://www.merg.org.uk/merg_wiki/doku.php?id=other_download:turntable"); // NOI18N
        // result.put(16, "CANHS"); // NOI18N
        result.put(17, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canace8c"); // NOI18N
        // result.put(18, "CAN8I8O"); // NOI18N
        result.put(19, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canservo8"); // NOI18N
        result.put(20, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canrfid"); // NOI18N
        // result.put(21, "CANTC4"); // NOI18N
        // result.put(22, "CANACE16C"); // NOI18N
        // result.put(23, "CANIO8"); // NOI18N
        // result.put(24, "CANSNDX"); // NOI18N
        result.put(25, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canether"); // NOI18N
        result.put(26, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cansig"); // NOI18N
        result.put(27, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cansig"); // NOI18N
        result.put(28, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canccond8c"); // NOI18N
        result.put(29, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canpan"); // NOI18N
        result.put(30, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canace3"); // NOI18N
        result.put(31, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canpanel"); // NOI18N
        result.put(32, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canmio"); // NOI18N
        // result.put(33, "CANACE8MIO"); // NOI18N
        result.put(34, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cansol"); // NOI18N
        result.put(35, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canbip"); // NOI18N
        result.put(36, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cancdu"); // NOI18N
        // result.put(37, "CANACC4CDU"); // NOI18N
        // result.put(38, "CANWiBase"); // NOI18N
        // result.put(39, "WiCAB"); // NOI18N
        // result.put(40, "CANWiFi"); // NOI18N
        // result.put(41, "CANFTT"); // NOI18N
        // result.put(42, "CANHNDST"); // NOI18N
        // result.put(43, "CANTCHNDST"); // NOI18N
        result.put(44, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canrfid8"); // NOI18N
        result.put(45, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canmchrfid"); // NOI18N
        result.put(46, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canwi"); // NOI18N
        result.put(47, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:can4dc"); // NOI18N
        // result.put(48, "CANELEV"); // NOI18N
        result.put(49, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canscan"); // NOI18N
        result.put(50, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canmio"); // NOI18N
        result.put(51, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canmio"); // NOI18N
        result.put(52, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canmio"); // NOI18N
        // result.put(53, "CANBIP_OUT"); // NOI18N
        result.put(54, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:canastop"); // NOI18N
        result.put(55, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cancsb"); // NOI18N
        // result.put(56, "CANMAGOT"); // NOI18N
        // result.put(57, "CANACE16CMIO"); // NOI18N
        // result.put(58, "CANPiNODE"); // NOI18N
        result.put(59, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:candisp"); // NOI18N
        result.put(60, "https://www.merg.org.uk/merg_wiki/doku.php?id=cbus:cancompute"); // NOI18N
        
        // result.put(253, "CANUSB"); // NOI18N
        // result.put(254, "EMPTY"); // NOI18N
        // result.put(255, "CAN_SW"); // NOI18N        
        
        return Collections.unmodifiableMap(result);
    }
    
    /*
     * Populate hashmap with rocrail module support links
     */
    private static Map<Integer, String> createLink70Map() {
        Map<Integer, String> result = new HashMap<>();
        result.put(1, "https://wiki.rocrail.net/doku.php?id=can-gca1-en"); // NOI18N
        result.put(2, "https://wiki.rocrail.net/doku.php?id=can-gca2-en"); // NOI18N
        result.put(3, "https://wiki.rocrail.net/doku.php?id=can-gc3-en"); // NOI18N
        result.put(4, "https://wiki.rocrail.net/doku.php?id=can-gc4-en"); // NOI18N
        result.put(5, "https://wiki.rocrail.net/doku.php?id=can-gca5-en"); // NOI18N
        result.put(6, "https://wiki.rocrail.net/doku.php?id=can-gc6-en"); // NOI18N
        result.put(7, "https://wiki.rocrail.net/doku.php?id=can-gc7-en"); // NOI18N
        result.put(11, "https://wiki.rocrail.net/doku.php?id=can-gca1e-en"); // NOI18N
        return Collections.unmodifiableMap(result);
    }
    
    
    /**
     * Return a string representation of avreserved node number
     * @param modnum node number
     * @return reserved node number reason
     */
    public static String getReservedModule(int modnum) {
        // look for the opcode
        String format = resMod.get(modnum);
        if (format == null) {
            return "";
        } else {
            return format; 
        }
    }
    
    /**
     * Hashmap for fixed Module Numbers
     */
    private static final Map<Integer, String> resMod = createModMap();

    /*
     * Populate hashmap with format strings
     *
     */
    private static Map<Integer, String> createModMap() {
        Map<Integer, String> result = new HashMap<>();
        // Opcodes with no data
        result.put(100, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(101, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(102, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(103, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(104, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(105, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(106, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(107, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(108, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(109, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(110, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(111, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(112, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(113, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(114, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(115, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(116, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(117, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(118, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(119, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(120, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(121, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(122, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(123, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(124, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(125, Bundle.getMessage("NdNumReserveFixed")); // NOI18N
        result.put(126, "Reserved for CAN_RS Modules");
        result.put(127, "Reserved for CAN_USB Modules");
        result.put(65534, "Reserved for Command Station");
        result.put(65535, "Reserved, used by all CABS");
        return Collections.unmodifiableMap(result);
    }
    
    private static final Map<String, BackupType> nameIndex =
            new HashMap<String, BackupType>(BackupType.values().length);
    static {
        for (BackupType t : BackupType.values()) {
            nameIndex.put(t.name(), t);
        }
    }
    
    private static final Map<BackupType, String> displayPhraseIndex =
            new HashMap<BackupType, String>(BackupType.values().length);
    static {
        displayPhraseIndex.put(BackupType.INCOMPLETE, Bundle.getMessage("BackupIncomplete"));
        displayPhraseIndex.put(BackupType.COMPLETE, Bundle.getMessage("BackupComplete"));
        displayPhraseIndex.put(BackupType.COMPLETEDWITHERROR, Bundle.getMessage("BackupCompleteError"));
        displayPhraseIndex.put(BackupType.NOTONNETWORK, Bundle.getMessage("BackupNotOnNetwork"));
        displayPhraseIndex.put(BackupType.OUTSTANDING, Bundle.getMessage("BackupOutstanding"));
        displayPhraseIndex.put(BackupType.SLIM, Bundle.getMessage("NodeInSlim"));
    }
    
    /*
     * Get the display phrase for an enum value
     * <p>
     * eg. displayPhrase(BackupType.INCOMPLETE) will return "Backup InComplete"
     *
     * @param type The enum to translate
     * @return The phrase
     *
     */
    public static String displayPhrase(BackupType type) {
        return displayPhraseIndex.get(type);
    }
    
    /*
     * Get the enum type for a String value
     * <p>
     * eg. lookupByName("Complete") will return BackupType.COMPLETE
     *
     * @param name The String to lookup
     * @return The BackupType enum, else null
     *
     */
    public static BackupType lookupByName(String name) {
        return nameIndex.get(name);
    }
    
    /*
     * enum to represent Node Backup Conditions in a CBUS Node XML File
     *
     */
    public enum BackupType{
        INCOMPLETE(0),
        COMPLETE(1),
        COMPLETEDWITHERROR(2),
        NOTONNETWORK(3),
        OUTSTANDING(4),
        SLIM(5);
        
        private final int v;

        private BackupType(final int v) {
            this.v = v;
        }
    
        public int getValue() {
            return v;
        }
    
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeConstants.class);
}
