package jmri.jmrix.can.cbus;

import javax.annotation.Nonnull;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.swing.TextAreaFIFO;
import jmri.util.ThreadingUtil;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to send CAN Frames.
 * <p>
 * Auto adds CBUS priority.
 * 
 * @author Steve Young (C) 2019
 */
public class CbusSend {
    
    private TrafficController tc;
    private TextAreaFIFO ta;
    private String newLine = System.getProperty("line.separator");
    
    public CbusSend( @Nonnull CanSystemConnectionMemo memo, TextAreaFIFO txta){
        tc = memo.getTrafficController();
        ta = txta;
    }

    public CbusSend(CanSystemConnectionMemo memo){
        if (memo!=null) {
            tc = memo.getTrafficController();
        }
        ta = null;
    }
    
    /**
     * Sends an outgoing CanMessage or incoming CanReply from a CanReply with a specified delay
     @param r A CanReply Can Frame which will be sent
     @param sendReply true to send as incoming CcanReply
     @param sendMessage true to send as outgoing CanMessage
     @param delay delay in ms
     */
    public void sendWithDelay( CanReply r, Boolean sendReply, Boolean sendMessage, int delay ){
        CbusMessage.setId(r, tc.getCanid() );
        CbusMessage.setPri(r, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        ThreadingUtil.runOnLayoutDelayed( () -> {
            if (sendReply) {
                tc.sendCanReply(r, null);
            }
            if (sendMessage) {
                CanMessage m = new CanMessage(r);
                tc.sendCanMessage(m, null);
            }
        },delay );
    }

    /**
     * Sends NNULN OPC , node exit learn mode
     @param nn Node Number
     *
     */
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
    
    /**
     * Sends NNLRN OPC , node enter learn mode
     @param nn Node Number
     *
     */
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

    /**
     * Sends EVLRN OPC , Event Learn
     * when a node is in learn mode
     @param newvalnd event variable node
     @param newevent event variable event
     @param varindex event variable index
     @param newval  event variable value
     *
     */
    public void nodeTeachEventLearnMode(int newvalnd, int newevent, int varindex, int newval) {
        CanMessage m = new CanMessage(tc.getCanid());
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setNumDataElements(7);
        m.setElement(0, CbusConstants.CBUS_EVLRN);
        m.setElement(1, newvalnd >> 8);
        m.setElement(2, newvalnd & 0xff);
        m.setElement(3, newevent >> 8);
        m.setElement(4, newevent & 0xff);
        m.setElement(5, varindex);
        m.setElement(6, newval);
        tc.sendCanMessage(m, null);
    }
    
    /**
     * Sends EVULN OPC , Event Unlearn
     * when a node is in learn mode
     @param newvalnd event variable node
     @param newevent event variable event
     *
     */
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
    
    
    /**
     * Sends REVAL OPC , Request for read of an event variable
     @param nodeinsetup Node Number
     @param nextev event index number
     @param nextevvar event variable number
     *
     */
    public void rEVAL( int nodeinsetup, int nextev, int nextevvar ){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(5);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_REVAL);
        m.setElement(1, nodeinsetup >> 8);
        m.setElement(2, nodeinsetup & 0xff);
        m.setElement(3, nextev);
        m.setElement(4, nextevvar);
        tc.sendCanMessage(m, null);
    }    

    /**
     * Sends RQNPN OPC , Request read of a node parameter by index
     @param nodeinsetup Node Number
     @param nextnodeparam parameter index number
     *
     */
    public void rQNPN( int nodeinsetup, int nextnodeparam ) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(4);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RQNPN);
        m.setElement(1, nodeinsetup >> 8);
        m.setElement(2, nodeinsetup & 0xff);
        m.setElement(3, nextnodeparam); // 0 gets total parameters for this module
        tc.sendCanMessage(m, null);
    }

    /**
     * Sends CanMessage QNN to get all nodes
     *
     */
     public void searchForNodes() {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_QNN);
        tc.sendCanMessage(m, null);

    }
    
    /**
     * Sends a message to request details of any connected command stations.
     * Responses are received by the CBUS node table
     *
     */
    public void searchForCommandStations(){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RSTAT);
        tc.sendCanMessage(m, null);
    }
    
    /**
     * Sends NVRD OPC , Request read of a node variable
     @param nodeinsetup Node Number
     @param nextnodenv variable number
     *
     */
    public void nVRD( int nodeinsetup, int nextnodenv ) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(4);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NVRD);
        m.setElement(1, nodeinsetup >> 8);
        m.setElement(2, nodeinsetup & 0xff);
        m.setElement(3, nextnodenv); // get total parameters for this module
        tc.sendCanMessage(m, null);
    }

    /**
     * Sends NVSET OPC , Node set individual NV
     @param nodeinsetup Node Number
     @param nv Node variable number
     @param newval Node variable number value
     *
     */
    public void nVSET(int nodeinsetup,int nv,int newval ) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(5);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NVSET);
        m.setElement(1, nodeinsetup >> 8);
        m.setElement(2, nodeinsetup & 0xff);
        m.setElement(3, nv);
        m.setElement(4, newval);
        tc.sendCanMessage(m, null);           
    }

    /**
     * Sends RQEVN OPC , Read number of stored events in node
     * <p>
     * nb, NOT max events capable
     @param nodeinsetup Node Number
     *
     */
    public void rQEVN( int nodeinsetup ) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RQEVN);
        m.setElement(1, nodeinsetup >> 8);
        m.setElement(2, nodeinsetup & 0xff);
        tc.sendCanMessage(m, null);
    }

    /**
     * Sends NERD OPC , Request to read all node events
     @param nodeinsetup Node Number
     *
     */
    public void nERD(int nodeinsetup ){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NERD);
        m.setElement(1, nodeinsetup >> 8);
        m.setElement(2, nodeinsetup & 0xff);
        tc.sendCanMessage(m, null);  
    }

    /**
     * Sends a Sysstem Reset ARST OPC
     * Full system reset
     *
     */
    public void aRST(){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_ARST);
        tc.sendCanMessage(m, null);  
    }

    /**
     * Sends ENUM OPC , Force a self enumeration cycle for use with CAN
     @param nodeinsetup Node Number
     *
     */
    public void eNUM(int nodeinsetup ){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_ENUM);
        m.setElement(1, nodeinsetup >> 8);
        m.setElement(2, nodeinsetup & 0xff);
        tc.sendCanMessage(m, null);  
    }

    /**
     * Sends CANID OPC , Teach node a specific CANID
     @param nodeinsetup Node Number
     @param canid new CAN ID ( min 1, max 99 )
     *
     */
    public void cANID(int nodeinsetup, int canid ){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(4);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_CANID);
        m.setElement(1, nodeinsetup >> 8);
        m.setElement(2, nodeinsetup & 0xff);
        m.setElement(3, canid & 0xff);
        tc.sendCanMessage(m, null);  
    }
    
    
    /**
     * Sends NNCLR OPC , Clear all events from a node
     * <p>
     * Node must be in Learn Mode to take effect
     @param nodeinsetup Node Number
     *
     */
    public void nNCLR(int nodeinsetup ){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NNCLR);
        m.setElement(1, nodeinsetup >> 8);
        m.setElement(2, nodeinsetup & 0xff);
        tc.sendCanMessage(m, null);  
    }
    
    /**
     * Sends RQMN OPC , Request name from node
     * <p>
     * Node must be in Setup Mode to take effect
     *
     */
    public void rQmn(){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RQMN);
        tc.sendCanMessage(m, null);  
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusSend.class);
}
