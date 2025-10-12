package jmri.jmrix.can.cbus;

import java.util.EnumSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.cbus.swing.CbusFilterTreePane;


/**
 * Class to implement filtering of CBUS frames.
 * Long event OPCs are not altered for a node number of 0
 * @author Steve Young (C) 2018, 2020
 */
public class CbusFilter {

    private final java.util.List<FilterHolder> list = new java.util.concurrent.CopyOnWriteArrayList<>();

    private final CbusFilterTreePane filterTreePane;
    private int _evMin = 0;
    private int _evMax = 65535;
    private int _ndMin = 0;
    private int _ndMax = 65535;

    private long filteredMessage = 0;
    private long filteredReply = 0;
    private long passedMessage = 0;
    private long passedReply = 0;

    public static final int CFMAXCATS =CbusFilterType.values().length;

    public static final String ROOT_NODE_TEXT = Bundle.getMessage("FilterAllFrames");

    /**
     * Creates a new instance of CbusFilter
     * @param filterPane The Instance Pane
     */
    public CbusFilter(CbusFilterTreePane filterPane) {
        filterTreePane = filterPane;
        getTree();
    }

    /**
     * Filter CanMessage or CanReply.
     *
     * @param test Message to Test
     * @return Filter number which failed, else -1
     */
    public int filter( @Nonnull AbstractMessage test) {

        int nodeNum = CbusMessage.getNodeNumber(test);
        if ( nodeNum > 0 ) {
            int nodeFilter = checknode(nodeNum);
            if ( nodeFilter > -1 ) {
                incrementTotals(test, nodeFilter);
                return nodeFilter;
            }
        }

        for (CbusFilterType singleFilter : CbusFilterType.allFilters(test.getElement(0))) {
            int toReturn = singleFilter.action(test,this);
            if ( toReturn>-1){
                incrementTotals(test, toReturn);
                return toReturn;
            }
            else if ( toReturn==-2){ // Extended or RTR CAN Frame, No OPC's to filter.
                break;
            }
        }
        incrementTotals(test, -1);
        return -1;
    }

    private void incrementTotals( @Nonnull AbstractMessage test, int result){
        incrementCount(result);
        if ( test instanceof jmri.jmrix.can.CanReply ) {
            if ( result > -1 ) {
                this.filteredReply++;
            } else {
                this.passedReply++;
            }
        } else {
            if ( result > -1 ) {
                this.filteredMessage++;
            } else {
                this.passedMessage++;
            }
        }
    }

    public void setFiltersByName( java.util.Set<String> activeFilters ) {
        list.forEach( f -> f.setActive(activeFilters.contains(f.node.toString())));
    }

    public boolean isFilterActive(int filterNum) {
        return list.get(filterNum).active;
    }

    /**
     * Perform Node checks for a given node number.
     * If a new Node Number is found, is added to the main Node List
     * and a new filter created.
     *
     * @param node Node Number
     * @return Filter number which failed, else -1
     */
    private int checknode(int node ) {

        String nodeNum = String.valueOf(node);

        FilterHolder fh = getFilterHolder(nodeNum);
        if ( fh == null ) {

            DefaultMutableTreeNode snode = new DefaultMutableTreeNode(nodeNum);
            fh = new FilterHolder(snode, node);
            list.add(fh);

            DefaultMutableTreeNode ndnd = this.getTreeNode(CbusFilterType.CFNODE);
            ndnd.add(snode);

            if (filterTreePane !=null) {
                filterTreePane.reset();
            }
        }

        if ( list.get(CbusFilterType.CFNODE.ordinal()).active){
            return CbusFilterType.CFNODE.ordinal();
        }

        if ( fh.active ){
            return list.indexOf(fh);
        }
        return -1;
    }

    @CheckForNull
    private FilterHolder getFilterHolder( @Nonnull String treeNodeName) {
        for ( FilterHolder fh : list ) {
            if ( treeNodeName.equals(fh.node.toString()) ) {
                return fh;
            }
        }
        return null;
    }

    /**
     * Increment Filter count for a given filter ID.
     * @param filternum Filter ID
     */
    private void incrementCount(int filternum ){
        if ( filternum >= 0 ) {
            FilterHolder node = list.get(filternum);
            if ( node != null ) {
                node.filterCount++;
            }
        }
    }

    /**
     * Set a single Filter to pass or filter.
     * @param id Filter ID
     * @param trueorfalse true to filter, false to pass through.
     */
    public void setFilter(int id, boolean trueorfalse) {
        list.get(id).setActive(trueorfalse);
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

    public long getFilteredMessage(){
        return filteredMessage;
    }

    public long getFilteredReply(){
        return filteredReply;
    }

    public long getPassedMessage(){
        return passedMessage;
    }

    public long getPassedReply(){
        return passedReply;
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

    private DefaultMutableTreeNode root;

    public final synchronized DefaultMutableTreeNode getTree(){
        if ( root == null ) {
            root = new DefaultMutableTreeNode(ROOT_NODE_TEXT);
            EnumSet.allOf(CbusFilterType.class).forEach( singleFilter -> {
                DefaultMutableTreeNode snode = new DefaultMutableTreeNode(singleFilter.getName());
                var category = singleFilter.getCategory();
                if (  category == null ) {
                    root.add(snode);
                } else {
                    getTreeNode(category).add(snode);
                }
                list.add( new FilterHolder(snode) );

            });

        }
        return root;
    }

    @Nonnull
    private DefaultMutableTreeNode getTreeNode( @Nonnull CbusFilterType type ) {
        for ( FilterHolder f : list) {
            if ( type.getName().equals(f.node.toString() ) ) {
                return f.node;
            }
        }
        throw new IllegalArgumentException("type Not found in list of nodes");
    }

    public void resetCounts() {
        list.forEach( FilterHolder::resetCount);
    }

    @CheckForNull
    public JLabel getNumberFilteredLabel( DefaultMutableTreeNode node ) {
        FilterHolder f = getFilterHolder(node.toString());
        if ( f != null ) {
            return f.getCountLabel();
        }
        return null;
    }

    public int getNodeNumber( DefaultMutableTreeNode node ) {
        FilterHolder f = getFilterHolder(node.toString());
        if ( f != null ) {
            return f.nodeNum;
        }
        return -1;
    }

    private static class FilterHolder {

        final DefaultMutableTreeNode node;
        boolean active;
        int filterCount;
        final int nodeNum;
        private static final java.awt.Color COUNT_COLOUR = new java.awt.Color(139,0,0);

        private FilterHolder(DefaultMutableTreeNode node) {
            this.node = node;
            nodeNum = -1;
        }

        private FilterHolder(DefaultMutableTreeNode node, int nodeNumber) {
            this.node = node;
            this.nodeNum = nodeNumber;
        }

        void resetCount() {
            filterCount = 0;
        }

        JLabel getCountLabel() {
            JLabel toRet = new JLabel( " " + filterCount + " ");
            toRet.setForeground( COUNT_COLOUR );
            return toRet;
        }

        void setActive( boolean newVal) {
            active = newVal;
        }

    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusFilter.class);

}
