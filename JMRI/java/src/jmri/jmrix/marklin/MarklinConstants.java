package jmri.jmrix.marklin;

/**
 * Constants to represent values seen in Marklin traffic.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public final class MarklinConstants {

    /* various bit masks */
    //Priority 2+2bit
    //Need to work these out correctly
    public final static int PRIO_1 = 0x00;  /* Priority 1: Stop / go / short message    */

    public final static int PRIO_2 = 0x01;  /* Priority 2: feedback    */

    public final static int PRIO_3 = 0x02;  /* Priority 3: Engine Stop    */

    public final static int PRIO_4 = 0x03;  /* Priority 4: Engine/acessory command    */

    //System Commands
    public final static int SYSCOMMANDNO = 1;
    public final static int SYSCOMMANDSTART = 0x00;
    public final static int SYSCOMMANDEND = 0x00;

    //Management Commands
    public final static int MANCOMMANDNO = 8;
    public final static int MANCOMMANDSTART = 0x01;
    public final static int MANCOMMANDEND = 0x0A;

    //Accessory Commands
    public final static int ACCCOMMANDNO = 2;
    public final static int ACCCOMMANDSTART = 0x0B;
    public final static int ACCCOMMANDEND = 0x0D;

    //software commands
    public final static int SOFCOMMANDNO = 6;
    public final static int SOFCOMMANDSTART = 0x18;
    public final static int SOFCOMMANDEND = 0x1C;

    //GUI Commands
    public final static int GUICOMMANDNO = 3;
    public final static int GUICOMMANDSTART = 0x20;
    public final static int GUICOMMANDEND = 0x22;

    //Automation Commnads
    public final static int AUTCOMMANDSTART = 0x30;
    public final static int AUTCOMMANDEND = 0xFF;

    //Feedback Commands
    public final static int FEECOMMANDSTART = 0x10;
    public final static int FEECOMMANDEND = 0x12;

    public final static int HASHBYTE1 = 0x47;
    public final static int HASHBYTE2 = 0x11;

    public final static int CMDSTOPSYS = 0x00;
    public final static int CMDGOSYS = 0x01;
    public final static int CMDHALTSYS = 0x02;

    // Location within the UDP packet of the address bytes
    public final static int CANADDRESSBYTE1 = 0x05;
    public final static int CANADDRESSBYTE2 = 0x06;
    public final static int CANADDRESSBYTE3 = 0x07;
    public final static int CANADDRESSBYTE4 = 0x08;

    public final static int PROTOCOL_UNKNOWN = 0x00;
    public final static int PROTOCOL_DCC = 0x02;
    public final static int PROTOCOL_SX = 0x04;
    public final static int PROTOCOL_MM2 = 0x08;

    //CAN ADDRESS Ranages, lower two bytes of the address, upper - 0x0000
    //0x03FF MM1, 2 locomotives and function decoder (20 & 40 kHz, 80 & 255 addresses)
    public final static int MM1START = 0x0000;
    public final static int MM1END = 0x03FF;

    //Res. for MM1, 2 function decoder F1 - F4 (40 kHz, 80 & 255 addresses)
    public final static int MM1FUNCTSTART = 0x1000;
    public final static int MM1FUNCTEND = 0x13FF;

    //Res. for MM1, 2 locomotive decoder (20 kHz, 80 & 256 addresses) for MM1
    public final static int MM1LOCOSTART = 0x2000;
    public final static int MM1LOCOEND = 0x23FF;

    public final static int SX1START = 0x0800;
    public final static int SX1END = 0x0BFF;

    //SX1 - accessories (extension)
    public final static int SX1ACCSTART = 0x2800;
    public final static int SX1ACCEND = 0x2BFF;

    //MM1 2 accessories article decoder (40 kHz, 320 & 1024 addresses)
    public final static int MM1ACCSTART = 0x3000;
    public final static int MM1ACCEND = 0x33FF;

    //MM1 2 accessories article decoder (40 kHz, 320 & 1024 addresses)
    public final static int DCCACCSTART = 0x3800;
    public final static int DCCACCEND = 0x3FFF;

    public final static int MFXSTART = 0x4000;
    public final static int MFXEND = 0x7FFF;

    public final static int SX2START = 0x8000;
    public final static int SX2END = 0xBFFF;

    public final static int DCCSTART = 0xC000;
    public final static int DCCEND = 0xFFFF;

    public final static int LOCOEMERGENCYSTOP = 0x03;
    public final static int LOCOSPEED = 0x04;
    public final static int LOCODIRECTION = 0x05;
    public final static int LOCOFUNCTION = 0x06;

    public final static int STEPSHORT28 = 0x00;
    public final static int STEPSHORT14 = 0x01;
    public final static int STEPSHORT128 = 0x02;
    public final static int STEPLONG28 = 0x03;
    public final static int STEPLONG128 = 0x04;

    public final static int S88EVENT = 0x11;
}
