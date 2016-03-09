// DCCManufacturerList.java
package jmri.jmrix;

/**
 * Maintains lists equipment manufacturers that JMRI Supports.
 * <P>
 * If you add to this, please add your new one in all sections if possible.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class DCCManufacturerList {

    public static final String NONE = "None";  // internal only
    public static final String LENZ = "Lenz";
    public static final String HORNBY = "Hornby";
    public static final String BACHRUS = "Bachrus";
    public static final String ESU = "ESU";
    public static final String DCCPP = "DCC++";
    public static final String DIGITRAX = "Digitrax";
    public static final String ATLAS = "Atlas";
    public static final String NCE = "NCE";
    public static final String CMRI = "C/MRI";
    public static final String CTI = "CTI Electronics";
    public static final String EASYDCC = "Easy DCC";
    public static final String DCC4PC = "DCC4PC";
    public static final String DCCSPEC = "DCC Specialties";
    public static final String IEEE802154 = "IEEE 802.15.4";
    public static final String XBee = "XBee (API Mode)";
    public static final String JMRI = "JMRI (Network)";
    public static final String LIONEL = "Lionel TMCC";
    public static final String MAPLE = "Maple Systems";
    public static final String MERG = "MERG";
    public static final String MRC = "MRC";
    public static final String MARKLIN = "Marklin";
    public static final String NAC = "NAC Services";
    public static final String OAK = "Oak Tree Systems";
    public static final String OPENLCB = "OpenLCB";
    public static final String OTHER = "Others";
    public static final String POWERLINE = "Powerline";
    public static final String PROTRAK = "Protrak";
    public static final String QSI = "QSI Solutions";
    public static final String RAIL = "RailDriver";
    public static final String RFID = "RFID";
    public static final String PI = "Raspberry Pi Foundation";
    public static final String ROCO = "Roco";
    public static final String SPROG = "SPROG DCC";
    public static final String SRCP = "SRCP";
    public static final String TAMS = "Tams";
    public static final String TRACTRONICS = "TracTronics";
    public static final String UHLEN = "Uhlenbrock";
    public static final String WANGROW = "Wangrow";
    public static final String XBEE = "Digi XBee";
    public static final String ZIMO = "Zimo";
    public static final String ZTC = "ZTC";

    final static private String[] systemNames = new String[]{
        NONE,
        ATLAS,
        BACHRUS,
        CMRI,
        CTI,
	DCCPP,
        DIGITRAX,
        DCCSPEC,
        DCC4PC,
        EASYDCC,
        ESU,
        HORNBY,
        IEEE802154,
        JMRI,
        LENZ,
        LIONEL,
        MAPLE,
        MARKLIN,
        MERG,
        MRC,
        NCE,
        NAC,
        OAK,
        OPENLCB,
        OTHER,
        POWERLINE,
        PROTRAK,
        QSI,
        RFID,
        PI,
        ROCO,
        SPROG,
        SRCP,
        TAMS,
        TRACTRONICS,
        UHLEN,
        WANGROW,
        ZIMO,
        ZTC
    };

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"EI_EXPOSE_REP", "MS_EXPOSE_REP"}) // OK until Java 1.6 allows return of cheap array copy
    public static String[] getSystemNames() {
        return systemNames;
    }

    public static String[] getConnectionList(String System) {
        if (System.equals(NONE)) {
            return new jmri.jmrix.internal.InternalConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(ATLAS)) {
            return new jmri.jmrix.lenz.LenzConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(BACHRUS)) {
            return new jmri.jmrix.bachrus.SpeedoConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(CMRI)) {
            return new jmri.jmrix.cmri.CMRIConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(CTI)) {
            return new jmri.jmrix.acela.AcelaConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(DCCPP)) {
            return new jmri.jmrix.dccpp.DCCppConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(DIGITRAX)) {
            return new jmri.jmrix.loconet.LnConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(DCC4PC)) {
            return new jmri.jmrix.dcc4pc.Dcc4PcConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(EASYDCC)) {
            return new jmri.jmrix.easydcc.EasyDccConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(DCC4PC)) {
            return new jmri.jmrix.dcc4pc.Dcc4PcConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(ESU)) {
            return new jmri.jmrix.ecos.EcosConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(HORNBY)) {
            return new jmri.jmrix.lenz.hornbyelite.EliteConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(IEEE802154)) {
            return new jmri.jmrix.ieee802154.SerialConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(LENZ)) {
            return new jmri.jmrix.lenz.LenzConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(LIONEL)) {
            return new jmri.jmrix.tmcc.SerialConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(MAPLE)) {
            return new jmri.jmrix.maple.SerialConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(MERG)) {
            return new jmri.jmrix.merg.MergConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(MARKLIN)) {
            return new jmri.jmrix.marklin.MarklinConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(MRC)) {
            return new jmri.jmrix.mrc.MrcConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(NAC)) {
            return new jmri.jmrix.rps.RpsConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(NCE)) {
            return new jmri.jmrix.nce.NceConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(OAK)) {
            return new jmri.jmrix.oaktree.SerialConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(OPENLCB)) {
            return new jmri.jmrix.openlcb.OlcbConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(PI)) { 
            return new jmri.jmrix.pi.RaspberryPiConnectionTypeList().getAvailableProtocolClasses(); 
        }
        if (System.equals(POWERLINE)) {
            return new jmri.jmrix.powerline.SerialConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(PROTRAK)) {
            return new jmri.jmrix.grapevine.SerialConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(QSI)) {
            return new jmri.jmrix.qsi.QSIConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(RFID)) {
            return new jmri.jmrix.rfid.RfidConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(ROCO)) {
            return new jmri.jmrix.roco.RocoConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(SPROG)) {
            return new jmri.jmrix.sprog.SprogConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(SRCP)) {
            return new jmri.jmrix.srcp.SRCPConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(TAMS)) {
            return new jmri.jmrix.tams.TamsConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(TRACTRONICS)) {
            return new jmri.jmrix.secsi.SerialConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(UHLEN)) {
            return new jmri.jmrix.loconet.uhlenbrock.UhlenbrockConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(WANGROW)) {
            return new jmri.jmrix.wangrow.WangrowConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(ZIMO)) {
            return new jmri.jmrix.zimo.Mx1ConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(ZTC)) {
            return new jmri.jmrix.lenz.ztc640.ZTC640ConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(OTHER)) {
            return new jmri.jmrix.OtherConnectionTypeList().getAvailableProtocolClasses();
        }
        if (System.equals(JMRI)) {
            return new jmri.jmrix.jmriclient.JMRIClientConnectionTypeList().getAvailableProtocolClasses();
        }
        return new jmri.jmrix.internal.InternalConnectionTypeList().getAvailableProtocolClasses();
    }

    // TODO: Add DCC++ to the next two systems (or not)

    //Some of these are now redundant if the connection has been converted to SystemConnectionMemo
    public static String getDCCSystemFromType(char a) {
        if (a == 'I') {
            return "Internal";
        } else if (a == 'A') {
            return "Acela";
        } else if (a == 'B') {
            return "DCC Direct";
        } else if (a == 'C') {
            return "C/MRI";
        } else if (a == 'D') {
            return "SRCP";
        } else if (a == 'E') {
            return "EasyDCC";
        } else if (a == 'F') {
            return "RFID";
        } else if (a == 'G') {
            return "Grapevine";
        } else if (a == 'K') {
            return "Maple";
        } else if (a == 'L') {
            return "LocoNet";
        } else if (a == 'N') {
            return "NCE";
        } else if (a == 'O') {
            return "Oak Tree";
        } else if (a == 'M') {
            return "OpenLCB";  // duplicates MERG?
        } else if (a == 'M') {
            return "MERG";
        } else if (a == 'P') {
            return "PowerLine";
        } else if (a == 'Q') {
            return "QSI";
        } else if (a == 'R') {
            return "RPS";
        } else if (a == 'S') {
            return "Sprog";
        } else if (a == 'T') {
            return "Lionel TMCC";
        } else if (a == 'U') {
            return "ECoS";
        } else if (a == 'V') {
            return "SECSI";
        } else if (a == 'W') {
            return "Wangrow";
        } else if (a == 'X') {
            return "XpressNet";
        } else if (a == 'Z') {
            return "Zimo";
        }
        return "Unknown";
    }

    //Some of these are now redundant if the connection has been converted to SystemConnectionMemo
    public static char getTypeFromDCCSystem(String a) {
        if (a.equals("Internal")) {
            return 'I';
        } else if (a.equals("Acela")) {
            return 'A';
        } else if (a.equals("DCC Direct")) {
            return 'B';
        } else if (a.equals("C/MRI")) {
            return 'C';
        } else if (a.equals("SRCP")) {
            return 'D';
        } else if (a.equals("EasyDCC")) {
            return 'E';
        } else if (a.equals("RFID")) {
            return 'F';
        } else if (a.equals("Grapevine")) {
            return 'G';
        } else if (a.equals("Maple")) {
            return 'K';
        } else if (a.equals("LocoNet")) {
            return 'L';
        } else if (a.equals("MERG")) {
            return 'M';
        } else if (a.equals("OpenLCB")) {
            return 'M';
        } else if (a.equals("NCE")) {
            return 'N';
        } else if (a.equals("Oak Tree")) {
            return 'O';
        } else if (a.equals("PowerLine")) {
            return 'P';
        } else if (a.equals("QSI")) {
            return 'Q';
        } else if (a.equals("RPS")) {
            return 'R';
        } else if (a.equals("Sprog")) {
            return 'S';
        } else if (a.equals("Lionel TMCC")) {
            return 'T';
        } else if (a.equals("ECoS")) {
            return 'U';
        } else if (a.equals("SECSI")) {
            return 'V';
        } else if (a.equals("Wangrow")) {
            return 'W';
        } else if (a.equals("XpressNet")) {
            return 'X';
        } else if (a.equals("Zimo")) {
            return 'Z';
        }
        return '\0';
    }
}
