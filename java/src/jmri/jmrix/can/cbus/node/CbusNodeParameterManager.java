package jmri.jmrix.can.cbus.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a Processing of CAN Frames for a CbusNode.
 *
 * @author Steve Young Copyright (C) 2019,2020
 */
public class CbusNodeParameterManager {
    private final CbusBasicNodeWithManagers _node;
    private int[] _parameters;
    private boolean _commandStationIdentified;
    private boolean _nodeTraitsSet;

    /**
     * Create a new CbusNodeCanListener
     *
     * @param node The Node
     */
    public CbusNodeParameterManager ( CbusBasicNodeWithManagers node ){
        _node = node;
        _parameters = null;
        _commandStationIdentified = false;
        _nodeTraitsSet = false;
    }

    /**
     * Set Node Parameters.
     * <p>
     * Para 0 Number of parameters
     * <p>
     * Para 1 The manufacturer ID
     * <p>
     * Para 2 Minor code version as an alphabetic character (ASCII)
     * <p>
     * Para 3 Manufacturer module identifier as a HEX numeric
     * <p>
     * Para 4 Number of supported events as a HEX numeric
     * <p>
     * Para 5 Number of Event Variables per event as a HEX numeric
     * <p>
     * Para 6 Number of supported Node Variables as a HEX numeric
     * <p>
     * Para 7 Major version
     * <p>
     * Para 8 Node flags
     * <p>
     * Para 9 Processor type
     * <p>
     * Para 10 Bus type
     * <p>
     * Para 11-14 load address, 4 bytes
     * <p>
     * Para 15-18 CPU manufacturer's id as read from the chip config space, 4 bytes
     * <p>
     * Para 19 CPU manufacturer code
     * <p>
     * Para 20 Beta revision (numeric), or 0 if release
     *
     * @param newparams set the node parameters
     *
     */
    public void setParameters( int[] newparams ) {

        //  log.warn("new params {}",newparams);
        _parameters = new int [(newparams[0]+1)];
        for (int i = 0; i < _parameters.length; i++) {
            setParameter(i,newparams[i]);
        }

        if ( getParameter(6) > -1 ) {
            int [] myarray = new int[(getParameter(6)+1)]; // +1 to account for index 0 being the NV count
            java.util.Arrays.fill(myarray, -1);
            myarray[0] = getParameter(6);
            _node.getNodeNvManager().setNVs(myarray);
        }
    }

    /**
     * Set a Single Node Parameter.
     * Parameter array should be initialised before calling.
     * Notifies PropertyChangeListener "PARAMETER"
     *
     * @param index Parameter Index,
     * @param newval New Parameter Value, 0-255
     */
    public void setParameter( int index, int newval ) {
        if ( _parameters == null ){
            log.error("Parameter set before array initiaised");
            return;
        }
        log.debug("set parameter tot:{} index:{} newval:{}",_parameters.length,index,newval);
        if ( index <= _parameters.length ) {

            _parameters[index] = newval;
            _node.notifyPropertyChangeListener("PARAMETER", null, null);
        }
    }

    /**
     * Get Number of outstanding unknown Parameters to be fetched from a CbusNode
     *
     * @return Number of outstanding Parameters, else 8
     */
    public int getOutstandingParams(){

        if (_parameters == null){
            return 8; // CBUS Spec minimum 8 parameters, likely value 20
        }

        int count = 0;
        for (int i = 1; i < _parameters.length; i++) {
            if ( _parameters[i] == -1 ) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get a Single Parameter value
     * <p>
     * eg. for param. array [3,1,2,3] index 2 returns 2
     *
     * @param index of which parameter, 0 gives the total parameters
     * @return Full Parameter value for a particular index, -1 if unknown
     */
    public int getParameter(int index) {
        if ( _parameters == null ) {
            return -1;
        }
        try  {
            return _parameters[index];
        }
        catch (java.lang.ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    /**
     * Get array of All parameters
     *
     * @return Full Parameter array, index 0 is total parameters
     */
    public int[] getParameters() {
        return _parameters;
    }

    /**
     * Get the Parameter String in Hex Byte Format
     * <p>
     * eg. for param. array [3,1,2,3] returns "010203"
     *
     * @return Full Parameter String WITHOUT leading number of parameters
     */
    public String getParameterHexString() {
        if (getParameters()==null) {
            return "";
        } else {
            return jmri.util.StringUtil.hexStringFromInts(getParameters()).replaceAll("\\s","").substring(2);
        }
    }

    protected void clearParameters() {
        _parameters = null;
        _nodeTraitsSet = false;
    }

    /**
     * Get the Node Type
     *
     * @return eg. MERG Command Station CANCMD Firmware 4d Node 65534
     */
    public String getNodeTypeString(){
        StringBuilder n = new StringBuilder(100);
        n.append (CbusNodeConstants.getManu(getParameter(1)))
        .append (" ")
        .append( CbusNodeConstants.getModuleTypeExtra(getParameter(1),getParameter(3)))
        .append(" ")
        .append( CbusNodeConstants.getModuleType(getParameter(1),getParameter(3)))
        .append (" ")
        .append (Bundle.getMessage("FirmwareVer",getParameter(7),Character.toString((char) getParameter(2) )));
        if ((getParameter(0)>19) && (getParameter(20)>0) ){
            n.append (Bundle.getMessage("FWBeta"))
            .append (getParameter(20))
            .append (" ");
        }
        n.append (Bundle.getMessage("CbusNode"))
        .append (_node.getNodeNumber());
        return n.toString();
    }

    public void requestEventTot() {
        if ( _node.getNodeTimerManager().hasActiveTimers() ){
            return;
        }
        _node.getNodeTimerManager().setNumEvTimeout();
        _node.send.rQEVN( _node.getNodeNumber() );
    }

    /**
     * Request a single Parameter from a Physical Node
     * <p>
     * Will not send the request if there are existing active timers.
     * Starts Parameter timeout
     *
     * @param param Parameter Index Number, Index 0 is total parameters
     */
    public void requestParam(int param){
        if ( _node.getNodeTimerManager().hasActiveTimers() ){
            return;
        }
        _node.getNodeTimerManager().setAllParamTimeout(param);
        _node.send.rQNPN( _node.getNodeNumber(), param );
    }

    private boolean sentParamRequest(int paramToCheck){
        if ( getParameter(paramToCheck) < 0 ) {
            requestParam(paramToCheck);
            return true;
        }
        return false;
    }

    /**
     * Send a request for the next unknown parameter to the physical node
     *
     */
    protected void sendRequestNextParam(){
        if ( _parameters == null ) {
            requestParam(0);
            return;
        }
        if ( sentParamRequest(1)  // Manufacturer ID
            || ( sentParamRequest(3) )  // Module ID
            || ( sentParamRequest(6) ) ) { // initialise NV's
            return;
        }

        if ( sentParamRequest(5) // get number event variables per event
            || ( sentParamRequest(7) ) // get firmware pt1
            || ( sentParamRequest(2) ) ) { // get firmware pt2
            return;
        }

        finishedWhenGotMainParams();

        for (int i = 1; i < _parameters.length; i++) {
            if ( sentParamRequest(i) ) {
                return;
            }
        }
    }

    private void finishedWhenGotMainParams(){

        if (!( _node instanceof CbusNode)){
            return;
        }

        if (( ( (CbusNode) _node).getCsNum() > -1 ) && ( _commandStationIdentified == false ) ) {
            // notify command station located
            log.info("Node type: {}",getNodeTypeString() );
            _commandStationIdentified = true;
        }

        // set node traits, eg CANPAN v1 send wrack on nv set, CANCMD v4 numevents 0
        // only do this once
        if (!_nodeTraitsSet ) {
            CbusNodeConstants.setTraits((CbusNode) _node);
            _nodeTraitsSet = true;
        }

        // now traits are known request num. of events
        if ( _node.getNodeEventManager().getTotalNodeEvents()<0 ){
            requestEventTot();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(CbusNodeParameterManager.class);

}
