package jmri.jmrix.can.cbus;

import java.util.HashMap;
import javax.annotation.Nonnull;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.cbus.swing.CbusFilterFrame;
import jmri.util.ThreadingUtil;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Class to implement filtering of CBUS frames.
 * Long event OPCs are not altered for a node number of 0
 * @author Steve Young (C) 2018, 2020
 */
public class CbusFilter {
    private final HashMap<Integer, Boolean> _boolean_hash_map;
    private final CbusFilterFrame _filterFrame;
    private final HashMap<Integer, Integer> _nodes_hashmap;
    private int _evMin = 0;
    private int _evMax = 0;
    private int _ndMin = 0;
    private int _ndMax = 0;

    public static final int CFMAXCATS =CbusFilterType.values().length;
    public static final int CFMAX_NODES = 100;

    /**
     * Creates a new instance of CbusFilter
     * @param filterFrame The Instance Frame
     */
    public CbusFilter(CbusFilterFrame filterFrame) {
        _filterFrame = filterFrame;
        _boolean_hash_map = new HashMap<>(getHMapSize(CFMAXCATS + CFMAX_NODES));
        _nodes_hashmap = new HashMap<>(getHMapSize(CFMAX_NODES));
        for ( int i=0 ; (i < CFMAXCATS + CFMAX_NODES) ; i++){
            _boolean_hash_map.put(i,false);
        }
    }

    /**
     * Filter CanMessage or CanReply.
     *
     * @param test Message to Test
     * @return Filter number which failed, else -1
     */
    public int filter( @Nonnull AbstractMessage test) {
        for (CbusFilterType singleFilter : CbusFilterType.allFilters(test.getElement(0))) {
            int _toReturn = singleFilter.action(test,this);
            if (_toReturn>-1){
                return _toReturn;
            }
            else if (_toReturn==-2){ // Extended or RTR CAN Frame, No OPC's to filter.
                break;
            }
        }
        return -1;
    }

    /**
     * Get position in Node List for a given Node Number
     * @param node Node Number
     * @param nodes Main Node map.
     * @return ID used in main Boolean filter list / CbusFilterPanel ID
     */
    private static int positionInNodeList(int node, @Nonnull HashMap<Integer,Integer> nodes){
        for (var o : nodes.entrySet()) {
            if (o.getValue().equals(node)) {
                return o.getKey();
            }
        }
        return -1;
    }

    /**
     * Get the main Filter Frame.
     * @return CbusFilterFrame instance
     */
    protected CbusFilterFrame getCbusFilterFrame() {
        return _filterFrame;
    }

    /**
     * Get Map of Filters.
     * @return Map of Boolean values.
     */
    protected final HashMap<Integer,Boolean> getActiveFilters(){
        return _boolean_hash_map;
    }

    /**
     * Get Map of Nodes.
     * @return Map of Node Numbers.
     */
    protected final HashMap<Integer,Integer> getNodes() {
        return _nodes_hashmap;
    }

    /**
     * Perform Node checks for a given node number.
     * If a new Node Number is found, is added to the main Node List
     * and a new filter created.
     *
     * @param node Node Number
     * @param cf CbusFilter instance
     * @return Filter number which failed, else -1
     */
    protected static int checknode(int node,@Nonnull CbusFilter cf) {

        if (!cf.getNodes().containsValue(node)){
            cf.getNodes().put(cf.getNodes().size()+ CFMAXCATS,node);
            // log.info("added new node {} to position {}",node,positionInNodeList(node,cf.getNodes()));
            if (cf.getCbusFilterFrame() !=null) {
                cf.getCbusFilterFrame().addNode(node,(positionInNodeList(node,cf.getNodes())));
            }
        }

        if ( cf.getActiveFilters().get(CbusFilterType.CFNODE.ordinal())){
            return CbusFilterType.CFNODE.ordinal();
        } else {
            incrementCount(CbusFilterType.CFNODE.ordinal(),cf); }
        if ( cf.getActiveFilters().get(positionInNodeList(node,cf.getNodes())) ){
            return positionInNodeList(node,cf.getNodes());
        } else {
            incrementCount(positionInNodeList(node,cf.getNodes()),cf); }
        return -1;
    }

    /**
     * Increment Filter count for a given filter ID.
     * @param filternum Filter ID
     * @param cf CbusFilter instance
     */
    protected static void incrementCount(int filternum, @Nonnull CbusFilter cf){
        // log.debug("increment count {}",filternum);
        if (cf.getCbusFilterFrame() != null ) {
            ThreadingUtil.runOnGUIEventually( ()->{
                cf.getCbusFilterFrame().passIncrement(filternum);
            });
        }
    }

    /**
     * Set a single Filter to pass or filter.
     * @param id Filter ID
     * @param trueorfalse true to filter, false to pass through.
     */
    public void setFilter(int id, boolean trueorfalse) {
        _boolean_hash_map.put(id, trueorfalse);
    }

    /**
     * Set the event or node min and max values.
     *
     * @param filter CFEVENTMIN, CFEVENTMAX, CFNODEMIN or CFNODEMAX
     * @param val min or max value
     */
    public void setMinMax(@Nonnull CbusFilterType filter, int val){
        switch (filter) {
            case CFEVENTMIN:
                _evMin = val;
                break;
            case CFEVENTMAX:
                _evMax = val;
                break;
            case CFNODEMIN:
                _ndMin = val;
                break;
            case CFNODEMAX:
                _ndMax = val;
                break;
            default:
                break;
        }
    }

    /**
     * Get Minimum Event Number.
     * @return Minimum Event
     */
    public int getEvMin() {
        return _evMin;
    }

    /**
     * Get Maximum Event Number.
     * @return Maximum Event
     */
    public int getEvMax() {
        return _evMax;
    }

    /**
     * Get Minimum Node Number.
     * @return Minimum Node
     */
    public int getNdMin() {
        return _ndMin;
    }

    /**
     * Get Maximum Node Number.
     * @return Maximum Node
     */
    public int getNdMax() {
        return _ndMax;
    }

    /**
     * Get optimum HashMap Size.
     * @param reqdCapacity Required finite capacity
     * @return value to use on creation
     */
    public static final int getHMapSize(int reqdCapacity){
        return ((reqdCapacity*4+2)/3);
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusFilter.class);
}
