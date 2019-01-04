package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.swing.TextAreaFIFO;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to send can messages
 * 
 * @author Steve Young (C) 2019
 * 
 */
public class CbusSend {
    
    private TrafficController tc;
    private TextAreaFIFO ta;
    private String newLine = System.getProperty("line.separator");
    
    public CbusSend(CanSystemConnectionMemo memom, TextAreaFIFO txta){
        tc = memom.getTrafficController();
        ta = txta;
    }

    public void nodeExitLearnEvMode( int nn ) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NNULN);
        m.setElement(1, nn >> 8);
        m.setElement(2, nn & 0xff);
        tc.sendCanMessage(m, null);
        if (ta != null){
            ta.append(newLine + Bundle.getMessage("NdReqExitLearn", nn)); // future lookup from node table
        }
    }
    
    public void nodeEnterLearnEvMode( int nn ) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NNLRN);
        m.setElement(1, nn >> 8);
        m.setElement(2, nn & 0xff);
        tc.sendCanMessage(m, null);
        if (ta != null){
            ta.append(newLine + Bundle.getMessage("NdReqEnterLearn", nn));
        }
    }
    
    public void nodeSetNodeNumber( int nn ) {
        CanMessage mn = new CanMessage(tc.getCanid());
        mn.setNumDataElements(3);
        CbusMessage.setPri(mn, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        mn.setElement(0, CbusConstants.CBUS_SNN);
        mn.setElement(1, nn >> 8);
        mn.setElement(2, nn & 0xff);
        tc.sendCanMessage(mn, null);
    }
    
    public void nodeRequestParamSetup() {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RQNP);
        tc.sendCanMessage(m, null);
    }

    public void nodeTeachEventLearnMode(int newvalnd,int newevent,int nextsetevvar,int newval) {
        CanMessage m = new CanMessage(tc.getCanid());
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setNumDataElements(7);
        m.setElement(0, CbusConstants.CBUS_EVLRN);
        m.setElement(1, newvalnd >> 8);
        m.setElement(2, newvalnd & 0xff);
        m.setElement(3, newevent >> 8);
        m.setElement(4, newevent & 0xff);
        m.setElement(5, nextsetevvar);
        m.setElement(6, newval);
        tc.sendCanMessage(m, null);
    }
    
    public void nodeUnlearnEvent( int newvalnd, int newevent ){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(5);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_EVULN);
        m.setElement(1, newvalnd >> 8);
        m.setElement(2, newvalnd & 0xff);
        m.setElement(3, newevent >> 8);
        m.setElement(4, newevent & 0xff);
        tc.sendCanMessage(m, null);
    }
    
    public void rEVAL( int _nodeinsetup, int _nextev, int _nextevvar ){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(5);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_REVAL);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        m.setElement(3, _nextev);
        m.setElement(4, _nextevvar);
        tc.sendCanMessage(m, null);
    }    

    public void rQNPN( int _nodeinsetup, int _nextnodeparam ) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(4);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RQNPN);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        m.setElement(3, _nextnodeparam); // 0 gets total parameters for this module
        tc.sendCanMessage(m, null);
    }

    public void searchForNodes() {
        CanMessage n = new CanMessage(tc.getCanid());
        n.setNumDataElements(1);
        CbusMessage.setPri(n, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        n.setElement(0, CbusConstants.CBUS_QNN);
        tc.sendCanMessage(n, null);
        if (ta != null){
            ta.append(newLine + Bundle.getMessage("NodeSearchStart"));
        }
    }

    public void nVRD( int _nodeinsetup, int _nextnodenv ) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(4);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NVRD);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        m.setElement(3, _nextnodenv); // get total parameters for this module
        tc.sendCanMessage(m, null);
    }

    public void nVSET(int _nodeinsetup,int nv,int newval ) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(5);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NVSET);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        m.setElement(3, nv);
        m.setElement(4, newval);
        tc.sendCanMessage(m, null);           
    }

    public void rQEVN( int _nodeinsetup ) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RQEVN);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        tc.sendCanMessage(m, null);
    }

    public void nERD(int _nodeinsetup ){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NERD);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        tc.sendCanMessage(m, null);  
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusSend.class);
}
