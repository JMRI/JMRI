// DCCManufacturerList.java

package jmri.jmrix;

/**
 * Returns a list of DCC Manufactureres that JMRI Supports
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision: 1.3 $
 *
 */
public class DCCManufacturerList {
    
    public static final String NONE = "None";  // internal only
    public static final String LENZ = "Lenz";
    public static final String HORNBY = "Hornby";
    public static final String ESU = "ESU";
    public static final String DIGITRAX = "Digitrax";
    public static final String ATLAS = "Atlas";
    public static final String NCE = "NCE";
    public static final String CMRI = "C/MRI";
    public static final String CTI = "CTI Electronics";
    public static final String EASYDCC = "Easy DCC";
    public static final String DCCSPEC = "DCC Specialties";
    public static final String FLEISHMANN = "Fleishmann";
    public static final String LIONEL = "Lionel TMCC";
    public static final String MAPLE = "Maple Systems";
    public static final String MERG = "MERG";
    public static final String NAC = "NAC Services";
    public static final String OAK = "Oak Tree Systems";
    public static final String OTHER = "Others";
    public static final String PROTRAK = "Protrak";
    public static final String QSI = "QSI Solutions";
    public static final String RAIL = "RailDriver";
    public static final String ROCO = "Roco";
    public static final String SPROG = "Sprog";
    public static final String SRCP = "SRCP";
    public static final String TRACTRONICS = "TracTronics";
    public static final String UHLEN = "Uhlenbrock";
    public static final String WANGROW = "Wangrow";
    public static final String X10 = "X10";
    public static final String ZIMO = "Zimo";
    public static final String ZTC = "ZTC";    
    
    final static private String[] systemNames = new String[]{
          
          NONE,
          ATLAS,
          CMRI,
          CTI,
          DIGITRAX,
          DCCSPEC,
          EASYDCC,
          ESU,
          FLEISHMANN,
          HORNBY,
          LENZ,
          LIONEL,
          MAPLE,
          MERG,
          NCE,
          NAC,
          OAK,
          OTHER,
          PROTRAK,
          QSI,
          ROCO,
          SPROG,
          SRCP,
          TRACTRONICS,
          UHLEN,
          WANGROW,
          X10,
          ZIMO,
          ZTC
    };
    
    public static String[] getSystemNames() {
        return systemNames;
    }

    public static String[] getConnectionList(String System) {
        if(System.equals(NONE)) { return new jmri.jmrix.internal.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(LENZ)) { return new jmri.jmrix.lenz.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(HORNBY)) { return new jmri.jmrix.lenz.hornbyelite.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(ESU)) { return new jmri.jmrix.ecos.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(DIGITRAX)) { return new jmri.jmrix.loconet.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(ATLAS)) { return new jmri.jmrix.lenz.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(CMRI)) { return new jmri.jmrix.cmri.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(CTI)) { return new jmri.jmrix.acela.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(MERG)) { return new jmri.jmrix.can.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(EASYDCC)) { return new jmri.jmrix.easydcc.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(PROTRAK)) { return new jmri.jmrix.grapevine.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(OAK)) { return new jmri.jmrix.oaktree.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(X10)) { return new jmri.jmrix.powerline.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(QSI)) { return new jmri.jmrix.qsi.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(LIONEL)) { return new jmri.jmrix.tmcc.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(WANGROW)) { return new jmri.jmrix.wangrow.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(ZIMO)) { return new jmri.jmrix.zimo.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(SRCP)) { return new jmri.jmrix.srcp.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(ROCO)) { return new jmri.jmrix.lenz.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(MAPLE)) { return new jmri.jmrix.maple.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(SPROG)) { return new jmri.jmrix.sprog.ConnectionTypeList().getAvailableProtocolClasses();  }
        if(System.equals(NCE)) { return new jmri.jmrix.nce.ConnectionTypeList().getAvailableProtocolClasses();   }
        if(System.equals(NAC)) { return new jmri.jmrix.rps.ConnectionTypeList().getAvailableProtocolClasses();   }
        if(System.equals(UHLEN)) { return new jmri.jmrix.loconet.Intellibox.ConnectionTypeList().getAvailableProtocolClasses();   }
        if(System.equals(ZTC)) { return new jmri.jmrix.lenz.ztc640.ConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(OTHER)) { return new jmri.jmrix.OtherConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(FLEISHMANN)) { return new jmri.jmrix.loconet.Intellibox.FleischmannConnectionTypeList().getAvailableProtocolClasses(); }
        if(System.equals(TRACTRONICS)) { return new jmri.jmrix.secsi.ConnectionTypeList().getAvailableProtocolClasses(); }
        return new jmri.jmrix.lenz.ConnectionTypeList().getAvailableProtocolClasses();
    }
    
    public String getDCCSystemFromType(char a) {
        if (a=='I') return "Internal";
        else if (a=='X') return "XPressNet";
        else if (a=='S') return "Sprog";
        return "Unknown";
    }
}

