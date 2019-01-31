package jmri.jmrix.can.cbus;

import java.util.ArrayList;
import java.util.List;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanFrame;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.swing.CbusFilterFrame;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to implement filtering of CBUS frames.
 * Long event OPCs are not altered for a node number of 0
 * @author Steve Young (C) 2018
 */
public class CbusFilter {
    private ArrayList<Boolean> filters;
    private CbusFilterFrame _filterFrame;
    private ArrayList<ArrayList<String>> tips;
    private ArrayList<Integer> _nodes;
    private int _evMin = 0;
    private int _evMax = 0;
    private int _ndMin = 0;
    private int _ndMax = 0;
    
    public static final int CFIN = 0;
    public static final int CFOUT = 1;
    public static final int CFEVENT = 2;
    public static final int CFON = 3;
    public static final int CFOF = 4;
    public static final int CFSHORT = 5;
    public static final int CFLONG = 6;
    public static final int CFSTD = 7;    
    public static final int CFREQUEST = 8;
    public static final int CFRESPONSE = 9;
    public static final int CFED0 = 10;
    public static final int CFED1 = 11;
    public static final int CFED2 = 12;
    public static final int CFED3 = 13;
    public static final int CFDATA = 14;
    public static final int CFACDAT = 15;
    public static final int CFDDES = 16;
    public static final int CFRQDAT = 17;
    public static final int CFARDAT = 18;
    public static final int CFDDRS = 19;
    public static final int CFRQDDS = 20;
    public static final int CFCABDAT = 21;
    public static final int CFCS = 22;
    public static final int CFCSAQRL = 23;
    public static final int CFCSKA = 24;
    public static final int CFCSDSPD = 25;
    public static final int CFCSFUNC = 26;
    public static final int CFCSPROG = 27;
    public static final int CFCSLC = 28;
    public static final int CFCSC = 29;
    public static final int CFNDCONFIG = 30;
    public static final int CFNDSETUP = 31;
    public static final int CFNDVAR = 32;
    public static final int CFNDEV = 33;
    public static final int CFNDNUM = 34;
    public static final int CFMISC = 35;
    public static final int CFNETWK = 36;
    public static final int CFCLOCK = 37;
    public static final int CFOTHER = 38;
    public static final int CFUNKNOWN = 39;

    public static final int CFNODE = 40;
    public static final int CFEVENTMIN = 41;
    public static final int CFEVENTMAX = 42;
    public static final int CFNODEMIN = 43;
    public static final int CFNODEMAX = 44;    

    public static final int CFMAXCATS =45;
    public static final int CFMAX_NODES = 200;
    
    /**
     * Creates a new instance of CbusFilter
     */
    public CbusFilter(CbusFilterFrame filterFrame) {
        log.debug("new cbusfilter instance");
        _filterFrame = filterFrame;
        initTips();
    }

    /**
     * Filter, based on boolean array settings.
     *
     */
    public int filter(CanMessage m) {
        CanFrame test = m;
        if (filters.get(1)){
            return 1;
        }
        incrementCount(1);
        return mainfilter(test);
    }

    public int filter(CanReply r) {
        if (filters.get(0)){
            return 0;
        }
        CanFrame test = r;
        incrementCount(0);
        return mainfilter(test);
    }
    
    private int mainfilter (CanFrame test) {
        // log.debug("mainfilter canFrame {}",test);
        int opc = test.getElement(0);
        int nodeNum = (test.getElement(1) * 256 + test.getElement(2));
        int eventNum = (test.getElement(3) * 256 + test.getElement(4));
        // log.debug("test opc 0 is {}",opc);
        
        switch (opc) {
            // 0 data opc
            case CbusConstants.CBUS_ACK:
            case CbusConstants.CBUS_NAK:
            case CbusConstants.CBUS_HLT:
            case CbusConstants.CBUS_BON:
            case CbusConstants.CBUS_ARST:
                if ( filters.get(CFMISC) ){ return CFMISC; } else { incrementCount(CFMISC); }
                if ( filters.get(CFNETWK) ){ return CFNETWK; } else { incrementCount(CFNETWK); }
                break;
            case CbusConstants.CBUS_TON:
            case CbusConstants.CBUS_TOF:
            case CbusConstants.CBUS_ESTOP:
            case CbusConstants.CBUS_RTON:
            case CbusConstants.CBUS_RTOF:
            case CbusConstants.CBUS_RESTP:
            case CbusConstants.CBUS_RDCC3:
            case CbusConstants.CBUS_RDCC4:
            case CbusConstants.CBUS_RDCC5:
                if ( filters.get(CFCS) ){ return CFCS; } else { incrementCount(CFCS); }
                if ( filters.get(CFCSLC) ){ return CFCSLC; } else { incrementCount(CFCSLC); }
                break;
            case CbusConstants.CBUS_RSTAT:
            case CbusConstants.CBUS_SSTAT:
            case CbusConstants.CBUS_ERR:
                if ( filters.get(CFCS) ){ return CFCS; } else { incrementCount(CFCS); }
                if ( filters.get(CFCSC) ){ return CFCSC; } else { incrementCount(CFCSC); }
                break;
            case CbusConstants.CBUS_STAT:
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                if ( filters.get(CFCS) ){ return CFCS; } else { incrementCount(CFCS); }
                if ( filters.get(CFCSC) ){ return CFCSC; } else { incrementCount(CFCSC); }
                break;
            case CbusConstants.CBUS_RQNP:
            case CbusConstants.CBUS_RQMN:
            case CbusConstants.CBUS_NAME: 
            case CbusConstants.CBUS_PARAMS: 
                if ( filters.get(CFNDCONFIG) ){ return CFNDCONFIG; } else { incrementCount(CFNDCONFIG); }
                if ( filters.get(CFNDSETUP) ){ return CFNDSETUP; } else { incrementCount(CFNDSETUP); }
                break;            
            case CbusConstants.CBUS_NNLRN: 
            case CbusConstants.CBUS_NNULN: 
            case CbusConstants.CBUS_WRACK: 
            case CbusConstants.CBUS_BOOTM: 
            case CbusConstants.CBUS_ENUM: 
            case CbusConstants.CBUS_CMDERR: 
            case CbusConstants.CBUS_RQNPN: 
            case CbusConstants.CBUS_CANID: 
            case CbusConstants.CBUS_PARAN: 
            case CbusConstants.CBUS_PNN:
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                if ( filters.get(CFNDCONFIG) ){ return CFNDCONFIG; } else { incrementCount(CFNDCONFIG); }
                if ( filters.get(CFNDSETUP) ){ return CFNDSETUP; } else { incrementCount(CFNDSETUP); }
                break;
            case CbusConstants.CBUS_QNN:
                if ( filters.get(CFNDCONFIG) ){ return CFNDCONFIG; } else { incrementCount(CFNDCONFIG); }
                if ( filters.get(CFNDNUM) ){ return CFNDNUM; } else { incrementCount(CFNDNUM); }
                break;
            case CbusConstants.CBUS_SNN:
            case CbusConstants.CBUS_RQNN:
            case CbusConstants.CBUS_NNREL: 
            case CbusConstants.CBUS_NNACK:
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                if ( filters.get(CFNDCONFIG) ){ return CFNDCONFIG; } else { incrementCount(CFNDCONFIG); }
                if ( filters.get(CFNDNUM) ){ return CFNDNUM; } else { incrementCount(CFNDNUM); }
                break;
            case CbusConstants.CBUS_KLOC:
            case CbusConstants.CBUS_QLOC:
            case CbusConstants.CBUS_RLOC:
            case CbusConstants.CBUS_QCON:
            case CbusConstants.CBUS_STMOD:
            case CbusConstants.CBUS_PCON:
            case CbusConstants.CBUS_KCON:
            case CbusConstants.CBUS_DFLG:
            case CbusConstants.CBUS_GLOC:
            case CbusConstants.CBUS_PLOC:
                if ( filters.get(CFCS) ){ return CFCS; } else { incrementCount(CFCS); }
                if ( filters.get(CFCSAQRL) ){ return CFCSAQRL; } else { incrementCount(CFCSAQRL); }
                break;
            case CbusConstants.CBUS_DKEEP:
                if ( filters.get(CFCS) ){ return CFCS; } else { incrementCount(CFCS); }
                if ( filters.get(CFCSKA) ){ return CFCSKA; } else { incrementCount(CFCSKA); }
                break;
            case CbusConstants.CBUS_ALOC:
            case CbusConstants.CBUS_DFNON:
            case CbusConstants.CBUS_DFNOF:
            case CbusConstants.CBUS_DFUN:
                if ( filters.get(CFCS) ){ return CFCS; } else { incrementCount(CFCS); }
                if ( filters.get(CFCSFUNC) ){ return CFCSFUNC; } else { incrementCount(CFCSFUNC); }
                break;
            case CbusConstants.CBUS_DSPD:
                if ( filters.get(CFCS) ){ return CFCS; } else { incrementCount(CFCS); }
                if ( filters.get(CFCSDSPD) ){ return CFCSDSPD; } else { incrementCount(CFCSDSPD); }
                break;
            case CbusConstants.CBUS_NNCLR:
            case CbusConstants.CBUS_NNEVN:
            case CbusConstants.CBUS_NERD: 
            case CbusConstants.CBUS_RQEVN:
            case CbusConstants.CBUS_EVNLF:
            case CbusConstants.CBUS_NENRD:
            case CbusConstants.CBUS_NUMEV:
            case CbusConstants.CBUS_EVULN:
            case CbusConstants.CBUS_REVAL:
            case CbusConstants.CBUS_REQEV:
            case CbusConstants.CBUS_NEVAL:
            case CbusConstants.CBUS_EVLRN:
            case CbusConstants.CBUS_EVANS:
            case CbusConstants.CBUS_ENRSP:
            case CbusConstants.CBUS_EVLRNI:
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                if ( filters.get(CFNDCONFIG) ){ return CFNDCONFIG; } else { incrementCount(CFNDCONFIG); }
                if ( filters.get(CFNDEV) ){ return CFNDEV; } else { incrementCount(CFNDEV); }
                break;
            case CbusConstants.CBUS_RQDAT:
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                if ( filters.get(CFDATA) ){ return CFDATA; } else { incrementCount(CFDATA); }
                if ( filters.get(CFRQDAT) ){ return CFRQDAT; } else { incrementCount(CFRQDAT); }
                break;
            case CbusConstants.CBUS_RQDDS:
                if ( checkevent(nodeNum) > -1 ) { return checkevent(nodeNum); } // byte 1 and 2 are device number
                if ( filters.get(CFDATA) ){ return CFDATA; } else { incrementCount(CFDATA); }
                if ( filters.get(CFRQDDS) ){ return CFRQDDS; } else { incrementCount(CFRQDDS); }
                break;
            case CbusConstants.CBUS_NVRD:
            case CbusConstants.CBUS_NVSET:
            case CbusConstants.CBUS_NVANS:
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                if ( filters.get(CFNDCONFIG) ){ return CFNDCONFIG; } else { incrementCount(CFNDCONFIG); }
                if ( filters.get(CFNDVAR) ){ return CFNDVAR; } else { incrementCount(CFNDVAR); }
                break;
            case CbusConstants.CBUS_WCVO:
            case CbusConstants.CBUS_WCVB:
            case CbusConstants.CBUS_QCVS:
            case CbusConstants.CBUS_PCVS:
            case CbusConstants.CBUS_WCVS:
            case CbusConstants.CBUS_WCVOA:
                if ( filters.get(CFCS) ){ return CFCS; } else { incrementCount(CFCS); }
                if ( filters.get(CFCSPROG) ){ return CFCSPROG; } else { incrementCount(CFCSPROG); }
                break;
            case CbusConstants.CBUS_ACON:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED0) ){ return CFED0; } else { incrementCount(CFED0); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ACOF:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED0) ){ return CFED0; } else { incrementCount(CFED0); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_AREQ:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFREQUEST) ){ return CFREQUEST; } else { incrementCount(CFREQUEST); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ARON:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_AROF:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ASON:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFSHORT) ){ return CFSHORT; } else { incrementCount(CFSHORT); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED0) ){ return CFED0; } else { incrementCount(CFED0); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ASOF:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFSHORT) ){ return CFSHORT; } else { incrementCount(CFSHORT); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED0) ){ return CFED0; } else { incrementCount(CFED0); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ASRQ:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFSHORT) ){ return CFSHORT; } else { incrementCount(CFSHORT); }
                if ( filters.get(CFREQUEST) ){ return CFREQUEST; } else { incrementCount(CFREQUEST); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ARSON:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFSHORT) ){ return CFSHORT; } else { incrementCount(CFSHORT); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ARSOF:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFSHORT) ){ return CFSHORT; } else { incrementCount(CFSHORT); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ACON1:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED1) ){ return CFED1; } else { incrementCount(CFED1); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ACOF1:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED1) ){ return CFED1; } else { incrementCount(CFED1); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ARON1:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( filters.get(CFED1) ){ return CFED1; } else { incrementCount(CFED1); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_AROF1:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( filters.get(CFED1) ){ return CFED1; } else { incrementCount(CFED1); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ASON1:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFSHORT) ){ return CFSHORT; } else { incrementCount(CFSHORT); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED1) ){ return CFED1; } else { incrementCount(CFED1); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ASOF1:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFSHORT) ){ return CFSHORT; } else { incrementCount(CFSHORT); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED1) ){ return CFED1; } else { incrementCount(CFED1); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ARSON1:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFSHORT) ){ return CFSHORT; } else { incrementCount(CFSHORT); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( filters.get(CFED1) ){ return CFED1; } else { incrementCount(CFED1); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ARSOF1:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFSHORT) ){ return CFSHORT; } else { incrementCount(CFSHORT); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( filters.get(CFED1) ){ return CFED1; } else { incrementCount(CFED1); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_CABDAT:                
                if ( filters.get(CFDATA) ){ return CFDATA; } else { incrementCount(CFDATA); }
                if ( filters.get(CFCABDAT) ){ return CFCABDAT; } else { incrementCount(CFCABDAT); }
                break;
            case CbusConstants.CBUS_FCLK:
                if ( filters.get(CFMISC) ){ return CFMISC; } else { incrementCount(CFMISC); }
                if ( filters.get(CFCLOCK) ){ return CFCLOCK; } else { incrementCount(CFCLOCK); }
                break;
            case CbusConstants.CBUS_ACON2:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED2) ){ return CFED2; } else { incrementCount(CFED2); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ACOF2:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED2) ){ return CFED2; } else { incrementCount(CFED2); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ARON2:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( filters.get(CFED2) ){ return CFED2; } else { incrementCount(CFED2); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_AROF2:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( filters.get(CFED2) ){ return CFED2; } else { incrementCount(CFED2); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ACON3:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED3) ){ return CFED3; } else { incrementCount(CFED3); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ACOF3:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFSTD) ){ return CFSTD; } else { incrementCount(CFSTD); }
                if ( filters.get(CFED3) ){ return CFED3; } else { incrementCount(CFED3); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ARON3:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFON) ){ return CFON; } else { incrementCount(CFON); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( filters.get(CFED3) ){ return CFED3; } else { incrementCount(CFED3); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_AROF3:
                if ( checkevent(eventNum) > -1 ) { return checkevent(eventNum); }
                if ( filters.get(CFOF) ){ return CFOF; } else { incrementCount(CFOF); }
                if ( filters.get(CFLONG) ){ return CFLONG; } else { incrementCount(CFLONG); }
                if ( filters.get(CFRESPONSE) ){ return CFRESPONSE; } else { incrementCount(CFRESPONSE); }
                if ( filters.get(CFED3) ){ return CFED3; } else { incrementCount(CFED3); }
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                break;
            case CbusConstants.CBUS_ACDAT:
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                if ( filters.get(CFDATA) ){ return CFDATA; } else { incrementCount(CFDATA); }
                if ( filters.get(CFACDAT) ){ return CFACDAT; } else { incrementCount(CFACDAT); }
                break;
            case CbusConstants.CBUS_ARDAT:
                if ( checknode(nodeNum) > -1 ) { return checknode(nodeNum); }
                if ( filters.get(CFDATA) ){ return CFDATA; } else { incrementCount(CFDATA); }
                if ( filters.get(CFARDAT) ){ return CFARDAT; } else { incrementCount(CFARDAT); }
                break;
            case CbusConstants.CBUS_DDES:
                if ( checkevent(nodeNum) > -1 ) { return checkevent(nodeNum); } // byte 1 and 2 are device num
                if ( filters.get(CFDATA) ){ return CFDATA; } else { incrementCount(CFDATA); }
                if ( filters.get(CFDDES) ){ return CFDDES; } else { incrementCount(CFDDES); }
                break;
            case CbusConstants.CBUS_DDRS:
                if ( checkevent(nodeNum) > -1 ) { return checkevent(nodeNum); } // byte 1 and 2 are device num
                if ( filters.get(CFDATA) ){ return CFDATA; } else { incrementCount(CFDATA); }
                if ( filters.get(CFDDRS) ){ return CFDDRS; } else { incrementCount(CFDDRS); }
                break;
            case CbusConstants.CBUS_DBG1:
            case CbusConstants.CBUS_EXTC:
            case CbusConstants.CBUS_EXTC1:
            case CbusConstants.CBUS_EXTC2:
            case CbusConstants.CBUS_EXTC3:
            case CbusConstants.CBUS_EXTC4:
                if ( filters.get(CFMISC) ){ return CFMISC; } else { incrementCount(CFMISC); }
                if ( filters.get(CFOTHER) ){ return CFOTHER; } else { incrementCount(CFOTHER); }
                break;
            default:
                if ( filters.get(CFMISC) ){ return CFMISC; } else { incrementCount(CFMISC); }
                if ( filters.get(CFUNKNOWN) ){ return CFUNKNOWN; } else { incrementCount(CFUNKNOWN); }
                break;
            }
        // log.debug("got to end of main filter");
        return -1;
    }
    
    private int positionInNodeList(int node){
        for ( int i=0 ; (i < _nodes.size()) ; i++) {
            if (_nodes.get(i)==node) {
                return i;
            }
        }
        log.error("no node in list");
        return -1;
    }
    
    private int checknode(int node) {
        log.debug("checking node {}",node);
        
        if (!_nodes.contains(node)){
            _nodes.add(node);
            if (_filterFrame !=null) {
                _filterFrame.addNode(node,(positionInNodeList(node)+ CFMAXCATS));
            }
        }
        
        if ( filters.get(CFNODE) && ( node > 0 ) ){ return CFNODE; } else { incrementCount(CFNODE); }
        if (( filters.get(CFNODEMIN) ) && ( node < _ndMin)){ return CFNODEMIN; } else { incrementCount(CFNODEMIN); }
        if (( filters.get(CFNODEMAX) ) && ( node > _ndMax)){ return CFNODEMAX; } else { incrementCount(CFNODEMAX); }
        
        log.debug("node order in list is {}",positionInNodeList(node) );
        log.debug("boolean need to check is  {}",positionInNodeList(node)+ CFMAXCATS );        
        
        if ( filters.get(positionInNodeList(node) + CFMAXCATS) ){
            return positionInNodeList(node)+ CFMAXCATS; } else { incrementCount(positionInNodeList(node)+ CFMAXCATS); }
        
        return -1;
    }
    
    private int checkevent(int event) {
        // log.debug("checking event {}",event);
        if ( filters.get(CFEVENT) ){ return CFEVENT; } else { incrementCount(CFEVENT); }
        if (( filters.get(CFEVENTMIN) ) && ( event < _evMin)){ return CFEVENTMIN; } else { incrementCount(CFEVENTMIN); }
        if (( filters.get(CFEVENTMAX) ) && ( event > _evMax)){ return CFEVENTMAX; } else { incrementCount(CFEVENTMAX); }
        return -1;
    }
    
    // initialise  values
    public void initTips(){
        _nodes = new ArrayList<Integer>();
        filters = new ArrayList<Boolean>();
        for ( int i=0 ; (i < CFMAXCATS + CFMAX_NODES) ; i++){
            filters.add(false);
        }
        tips = new ArrayList<ArrayList<String>>();
        for ( int i=0 ; (i < CFMAXCATS) ; i++){
            tips.add(new ArrayList<String>());
        }

        tips.get(CFNETWK).add("ACK");
        tips.get(CFNETWK).add("NAK");
        tips.get(CFNETWK).add("HLT");
        tips.get(CFNETWK).add("BON");
        tips.get(CFCSLC).add("TON");
        tips.get(CFCSLC).add("TOF");        
        tips.get(CFCSLC).add("ESTOP");  
        tips.get(CFNETWK).add("ARST");
        tips.get(CFCSLC).add("RTON");
        tips.get(CFCSLC).add("RTOF");
        tips.get(CFCSLC).add("RESTP");
        tips.get(CFCSC).add("RSTAT");
        tips.get(CFNDNUM).add("QNN");
        tips.get(CFNDSETUP).add("RQNP");
        tips.get(CFNDSETUP).add("RQMN");
        tips.get(CFCSAQRL).add("KLOC");
        tips.get(CFCSKA).add("DKEEP");
        tips.get(CFOTHER).add("DBG1");        
        tips.get(CFOTHER).add("EXTC");

        tips.get(CFCSAQRL).add("RLOC");        
        tips.get(CFCSAQRL).add("QCON");
        tips.get(CFNDNUM).add("SNN");        
        tips.get(CFCSFUNC).add("ALOC");
        tips.get(CFCSAQRL).add("STMOD");         
        tips.get(CFCSAQRL).add("PCON"); 
        tips.get(CFCSAQRL).add("KCON");
        tips.get(CFCSDSPD).add("DSPD"); 
        tips.get(CFCSAQRL).add("DFLG"); 
        tips.get(CFCSFUNC).add("DFNON");
        tips.get(CFCSFUNC).add("DFNOF");
        tips.get(CFCSC).add("SSTAT");
        tips.get(CFNDNUM).add("RQNN"); 
        tips.get(CFNDNUM).add("NNREL"); 
        tips.get(CFNDNUM).add("NNACK"); 
        tips.get(CFNDSETUP).add("NNLRN"); 
        tips.get(CFNDSETUP).add("NNULN"); 
        tips.get(CFNDEV).add("NNCLR");        
        tips.get(CFNDEV).add("NNEVN");  
        tips.get(CFNDEV).add("NERD");  
        tips.get(CFNDEV).add("RQEVN");  
        tips.get(CFNDSETUP).add("WRACK");
        tips.get(CFRQDAT).add("RQDAT");
        tips.get(CFRQDDS).add("RQDDS");
        tips.get(CFNDSETUP).add("BOOTM");
        tips.get(CFNDSETUP).add("ENUM");        
        tips.get(CFOTHER).add("EXTC1");
        
        tips.get(CFCSFUNC).add("DFUN");
        tips.get(CFCSAQRL).add("GLOC");
        tips.get(CFCSC).add("ERR");
        tips.get(CFNDSETUP).add("CMDERR"); 
        tips.get(CFNDEV).add("EVNLF");
        tips.get(CFNDVAR).add("NVRD"); 
        tips.get(CFNDEV).add("NENRD"); 
        tips.get(CFNDSETUP).add("RQNPN"); 
        tips.get(CFNDEV).add("NUMEV"); 
        tips.get(CFNDSETUP).add("CANID"); 
        tips.get(CFOTHER).add("EXTC2");

        tips.get(CFCSLC).add("RDCC3");
        tips.get(CFCSPROG).add("WCVO");
        tips.get(CFCSPROG).add("WCVB");
        tips.get(CFCSPROG).add("QCVS");
        tips.get(CFCSPROG).add("PCVS");
        tips.get(CFON).add("ACON");  
        tips.get(CFLONG).add("ACON");
        tips.get(CFSTD).add("ACON");
        tips.get(CFED0).add("ACON");
        tips.get(CFOF).add("ACOF");  
        tips.get(CFLONG).add("ACOF");
        tips.get(CFSTD).add("ACOF");
        tips.get(CFED0).add("ACOF");
        tips.get(CFLONG).add("AREQ");
        tips.get(CFREQUEST).add("AREQ");
        tips.get(CFON).add("ARON");  
        tips.get(CFLONG).add("ARON");
        tips.get(CFRESPONSE).add("ARON");
        tips.get(CFED0).add("ARON");
        tips.get(CFOF).add("AROF");  
        tips.get(CFLONG).add("AROF");
        tips.get(CFRESPONSE).add("AROF");
        tips.get(CFED0).add("AROF");
        tips.get(CFNDEV).add("EVULN"); 
        tips.get(CFNDVAR).add("NVSET"); 
        tips.get(CFNDVAR).add("NVANS"); 
        tips.get(CFON).add("ASON");  
        tips.get(CFSHORT).add("ASON");
        tips.get(CFSTD).add("ASON");
        tips.get(CFED0).add("ASON");
        tips.get(CFOF).add("ASOF");  
        tips.get(CFSHORT).add("ASOF");
        tips.get(CFSTD).add("ASOF");
        tips.get(CFED0).add("ASOF");
        tips.get(CFSHORT).add("ASRQ");
        tips.get(CFREQUEST).add("ASRQ");
        tips.get(CFNDSETUP).add("PARAN"); 
        tips.get(CFNDEV).add("REVAL"); 
        tips.get(CFON).add("ARSON");  
        tips.get(CFSHORT).add("ARSON");
        tips.get(CFRESPONSE).add("ARSON");
        tips.get(CFED0).add("ARSON");        
        tips.get(CFOF).add("ARSOF");  
        tips.get(CFSHORT).add("ARSOF");
        tips.get(CFRESPONSE).add("ARSOF");
        tips.get(CFED0).add("ARSOF"); 
        tips.get(CFOTHER).add("EXTC3");
        
        tips.get(CFCSLC).add("RDCC4");
        tips.get(CFCSPROG).add("WCVS");
        tips.get(CFON).add("ACON1");  
        tips.get(CFLONG).add("ACON1");
        tips.get(CFSTD).add("ACON1");
        tips.get(CFED1).add("ACON1");        
        tips.get(CFOF).add("ACOF1");  
        tips.get(CFLONG).add("ACOF1");
        tips.get(CFSTD).add("ACOF1");
        tips.get(CFED1).add("ACOF1");         
        tips.get(CFNDEV).add("REQEV"); 
        tips.get(CFON).add("ARON1");  
        tips.get(CFLONG).add("ARON1");
        tips.get(CFRESPONSE).add("ARON1");
        tips.get(CFED1).add("ARON1");
        tips.get(CFOF).add("AROF1");  
        tips.get(CFLONG).add("AROF1");
        tips.get(CFRESPONSE).add("AROF1");
        tips.get(CFED1).add("AROF1");
        tips.get(CFNDEV).add("NEVAL");
        tips.get(CFNDSETUP).add("PNN");
        tips.get(CFON).add("ASON1");  
        tips.get(CFSHORT).add("ASON1");
        tips.get(CFSTD).add("ASON1");
        tips.get(CFED1).add("ASON1");
        tips.get(CFOF).add("ASOF1");  
        tips.get(CFSHORT).add("ASOF1");
        tips.get(CFSTD).add("ASOF1");
        tips.get(CFED1).add("ASOF1");
        tips.get(CFON).add("ARSON1");
        tips.get(CFSHORT).add("ARSON1");
        tips.get(CFRESPONSE).add("ARSON1");
        tips.get(CFED1).add("ARSON1");
        tips.get(CFOF).add("ARSOF1");  
        tips.get(CFSHORT).add("ARSOF1");
        tips.get(CFRESPONSE).add("ARSOF1"); 
        tips.get(CFED1).add("ARSOF1");
        tips.get(CFOTHER).add("EXTC4");
        
        tips.get(CFCSLC).add("RDCC5");
        tips.get(CFCSPROG).add("WCVOA");
        tips.get(CFCABDAT).add("CABDAT");
        tips.get(CFCLOCK).add("FCLK");
        tips.get(CFON).add("ACON2");
        tips.get(CFLONG).add("ACON2");
        tips.get(CFSTD).add("ACON2");
        tips.get(CFED2).add("ACON2");
        tips.get(CFOF).add("ACOF2");
        tips.get(CFLONG).add("ACOF2");
        tips.get(CFSTD).add("ACOF2");
        tips.get(CFED2).add("ACOF2");
        tips.get(CFNDEV).add("EVLRN");
        tips.get(CFNDEV).add("EVANS");
        tips.get(CFON).add("ARON2");  
        tips.get(CFLONG).add("ARON2");
        tips.get(CFRESPONSE).add("ARON2");
        tips.get(CFED2).add("ARON2");
        tips.get(CFOF).add("AROF2");  
        tips.get(CFLONG).add("AROF2");
        tips.get(CFRESPONSE).add("AROF2");
        tips.get(CFED2).add("AROF2");

        tips.get(CFON).add("ASON2");  
        tips.get(CFSHORT).add("ASON2");
        tips.get(CFSTD).add("ASON2");
        tips.get(CFED2).add("ASON2");
        tips.get(CFOF).add("ASOF2");  
        tips.get(CFSHORT).add("ASOF2");
        tips.get(CFSTD).add("ASOF2");
        tips.get(CFED2).add("ASOF2");
        tips.get(CFON).add("ARSON2");
        tips.get(CFSHORT).add("ARSON2");
        tips.get(CFRESPONSE).add("ARSON2");
        tips.get(CFED2).add("ARSON2");
        tips.get(CFOF).add("ARSOF2");  
        tips.get(CFSHORT).add("ARSOF2");
        tips.get(CFRESPONSE).add("ARSOF2"); 
        tips.get(CFED2).add("ARSOF2");
        tips.get(CFOTHER).add("EXTC5");

        tips.get(CFCSLC).add("RDCC6");
        tips.get(CFCSAQRL).add("PLOC");
        tips.get(CFNDSETUP).add("NAME");
        tips.get(CFCSC).add("STAT");
        tips.get(CFNDSETUP).add("PARAMS");
        tips.get(CFON).add("ACON3");
        tips.get(CFLONG).add("ACON3");
        tips.get(CFSTD).add("ACON3");
        tips.get(CFED3).add("ACON3");
        tips.get(CFOF).add("ACOF3");
        tips.get(CFLONG).add("ACOF3");
        tips.get(CFSTD).add("ACOF3");
        tips.get(CFED3).add("ACOF3");        
        tips.get(CFNDEV).add("ENRSP");
        tips.get(CFON).add("ARON3");  
        tips.get(CFLONG).add("ARON3");
        tips.get(CFRESPONSE).add("ARON3");
        tips.get(CFED3).add("ARON3");
        tips.get(CFOF).add("AROF3");  
        tips.get(CFLONG).add("AROF3");
        tips.get(CFRESPONSE).add("AROF3");
        tips.get(CFED3).add("AROF3");
        tips.get(CFNDEV).add("EVLRNI");
        tips.get(CFACDAT).add("ACDAT");
        tips.get(CFARDAT).add("ARDAT");
        tips.get(CFON).add("ASON3");  
        tips.get(CFSHORT).add("ASON3");
        tips.get(CFSTD).add("ASON3");
        tips.get(CFED3).add("ASON3");
        tips.get(CFOF).add("ASOF3");  
        tips.get(CFSHORT).add("ASOF3");
        tips.get(CFSTD).add("ASOF3");
        tips.get(CFED3).add("ASOF3");
        tips.get(CFDDES).add("DDES");
        tips.get(CFDDRS).add("DDRS");
        tips.get(CFON).add("ARSON3");
        tips.get(CFSHORT).add("ARSON3");
        tips.get(CFRESPONSE).add("ARSON3");
        tips.get(CFED3).add("ARSON3");
        tips.get(CFOF).add("ARSOF3");  
        tips.get(CFSHORT).add("ARSOF3");
        tips.get(CFRESPONSE).add("ARSOF3"); 
        tips.get(CFED3).add("ARSOF3");
        tips.get(CFOTHER).add("EXTC6");
        
    }
    
    public String getTtip(int category) {
        if ( category >= tips.size() ){
            return null;
        }
        List tiplist=tips.get(category);
        StringBuilder t = new StringBuilder();
        t.append("<html>");
        for ( int i=0 ; (i < tiplist.size()) ; i++){
            t.append(tiplist.get(i));
            t.append(" : ");
            t.append(Bundle.getMessage("CBUS_" + tiplist.get(i)));
            t.append(" : ");
            t.append(Bundle.getMessage("CTIP_" + tiplist.get(i)));
            t.append("<br>");
        }
        if ( tiplist.size() > 0 ){
            t.append("</html>");
            return t.toString();
        }
        return null;  // for some categories a tooltip is pointless
    }
    
    protected void incrementCount(int filternum){
        // log.debug("increment count {}",filternum);
        if (_filterFrame != null ) {
            ThreadingUtil.runOnGUIEventually( ()->{
                _filterFrame.passIncrement(filternum);
            });
        }
    }

    // filter values
    public void setFilter(int id, Boolean trueorfalse) {
        filters.set(id, trueorfalse);
        log.debug("set filter id {} {}",id,trueorfalse);
    }

    public void setEvMin(int val){
        _evMin = val;
    }
    
    public void setEvMax(int val){
        _evMax = val;
    }
    
    public void setNdMin(int val){
        _ndMin = val;
    }
    
    public void setNdMax(int val){
        _ndMax = val;
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusFilter.class);
}
