package jmri.jmrix.marklin;

/**
 * Constants to represent values seen in Marklin traffic.
 * @see <a href="https://www.maerklin.de/fileadmin/media/produkte/CS2_can-protokoll_1-0.pdf">CS2 CAN Protocol 1.0</a>
 * @see <a href="https://streaming.maerklin.de/public-media/cs2/cs2CAN-Protokoll-2_0.pdf">CS2 CAN Protocol 2.0</a>
 * @author Kevin Dickerson Copyright (C) 2012
 */
public final class MarklinConstants {

    // Utility class, only supplies static methods.
    private MarklinConstants() {}

    /* various bit masks */
    // Priority 2+2bit
    // Need to work these out correctly

    /**
     * Priority 1: Stop / go / short message
     */
    public static final int PRIO_1 = 0x00;

    public static final int PRIO_2 = 0x01;  /* Priority 2: Feedback    */

    public static final int PRIO_3 = 0x02;  /* Priority 3: Engine Stop    */

    public static final int PRIO_4 = 0x03;  /* Priority 4: Engine / accessory command    */

    // As of spec 2.0 - Commands

    // System Commands
    public static final int SYSCOMMANDNO = 1;
    public static final int SYSCOMMANDSTART = 0x00;
    public static final int SYSCOMMANDEND = 0x00;

    // Management Commands
    public static final int MANCOMMANDNO = 8;
    public static final int MANCOMMANDSTART = 0x01;
    public static final int MANCOMMANDEND = 0x0A;

    // Accessory Commands
    public static final int ACCCOMMANDNO = 2;
    public static final int ACCCOMMANDSTART = 0x0B;
    public static final int ACCCOMMANDEND = 0x0D;

    // Software commands
    public static final int SOFCOMMANDNO = 6;
    public static final int SOFCOMMANDSTART = 0x18;
    public static final int SOFCOMMANDEND = 0x1C;

    // GUI Commands
    public static final int GUICOMMANDNO = 3;
    public static final int GUICOMMANDSTART = 0x20;
    public static final int GUICOMMANDEND = 0x22;

    // Automation Commnads
    public static final int AUTCOMMANDSTART = 0x30;
    public static final int AUTCOMMANDEND = 0xFF;

    // Feedback Commands
    public static final int FEECOMMANDSTART = 0x10;
    public static final int FEECOMMANDEND = 0x12;

    public static final int HASHBYTE1 = 0x47;
    public static final int HASHBYTE2 = 0x11;

    public static final int CMDSTOPSYS = 0x00;
    public static final int CMDGOSYS = 0x01;
    public static final int CMDHALTSYS = 0x02;

    // Location within the UDP packet of the address bytes
    public static final int CANADDRESSBYTE1 = 0x05;
    public static final int CANADDRESSBYTE2 = 0x06;
    public static final int CANADDRESSBYTE3 = 0x07;
    public static final int CANADDRESSBYTE4 = 0x08;

    public static final int PROTOCOL_UNKNOWN = 0x00;
    public static final int PROTOCOL_DCC = 0x02;
    public static final int PROTOCOL_SX = 0x04;
    public static final int PROTOCOL_MM2 = 0x08;

    // CAN ADDRESS Ranges, lower two bytes of the address, upper - 0x0000
    // 0x03FF MM1, 2 locomotives and function decoder (20 & 40 kHz, 80 & 255 addresses)
    public static final int MM1START = 0x0000;
    public static final int MM1END = 0x03FF;

    //Res. for MM1, 2 function decoder F1 - F4 (40 kHz, 80 & 255 addresses)
    public static final int MM1FUNCTSTART = 0x1000;
    public static final int MM1FUNCTEND = 0x13FF;

    //Res. for MM1, 2 locomotive decoder (20 kHz, 80 & 256 addresses) for MM1
    public static final int MM1LOCOSTART = 0x2000;
    public static final int MM1LOCOEND = 0x23FF;

    // SX1
    public static final int SX1START = 0x0800;
    public static final int SX1END = 0x0BFF;

    // SX1 - accessories (extension)
    public static final int SX1ACCSTART = 0x2800;
    public static final int SX1ACCEND = 0x2BFF;

    // MM1 2 accessories article decoder (40 kHz, 320 & 1024 addresses)
    public static final int MM1ACCSTART = 0x3000;
    public static final int MM1ACCEND = 0x33FF;

    // DCC accessories article decoder (40 kHz, 320 & 1024 addresses)
    public static final int DCCACCSTART = 0x3800;
    public static final int DCCACCEND = 0x3FFF;

    // MFX Decoders
    public static final int MFXSTART = 0x4000;
    public static final int MFXEND = 0x7FFF;

    // Selectrix 2
    public static final int SX2START = 0x8000;
    public static final int SX2END = 0xBFFF;

    // DCC locomotives
    public static final int DCCSTART = 0xC000;
    public static final int DCCEND = 0xFFFF;

    /**
     * These CAN bus ranges do not translate to track signals
     * They are free for adressing equipment by 3rd parties,
     * i.e. sending commands to accessory or providing firmware updates.
     */
    // Free for clubs and individuals
    public static final int CLUBRANGESTART = 0x1800;
    public static final int CLUBRANGEEND = 0x1BFF;
    // Free for 3rd Party Vendors - Probably the range used by CdB products
    public static final int VENDORRANGESTART = 0x1C00;
    public static final int VENDORRANGEEND = 0x1FFF;




    public static final int LOCOEMERGENCYSTOP = 0x03;
    public static final int LOCOSPEED = 0x04;
    public static final int LOCODIRECTION = 0x05;
    public static final int LOCOFUNCTION = 0x06;

    public static final int STEPSHORT28 = 0x00;
    public static final int STEPSHORT14 = 0x01;
    public static final int STEPSHORT128 = 0x02;
    public static final int STEPLONG28 = 0x03;
    public static final int STEPLONG128 = 0x04;

    public static final int S88EVENT = 0x11;
    public static final int MCAN_BROADCAST = 0x00000000;
    public static final int MCAN_UNINITIALIZED = 0xFFFFFFFF;
}
