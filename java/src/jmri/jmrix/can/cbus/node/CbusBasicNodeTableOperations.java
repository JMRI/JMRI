package jmri.jmrix.can.cbus.node;

import java.util.ArrayList;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of CBUS Nodes
 *
 * @author Steve Young (c) 2019
 * 
 */
public class CbusBasicNodeTableOperations extends CbusBasicNodeTable {

    public CbusBasicNodeTableOperations(@Nonnull CanSystemConnectionMemo memo, int row, int column) {
        super(memo,row,column);        
    }
    
    /**
     * Register new node to table
     * @param node The CbusNode to add to the table
     */
    public void addNode(CbusNode node) {
        _mainArray.add(node);
        
        if (this instanceof CbusNodeTableDataModel) {
            node.setTableModel( (CbusNodeTableDataModel)this);
            node.addPropertyChangeListener((CbusNodeTableDataModel)this);
            ((CbusNodeTableDataModel) this).startBackgroundFetch();
        }
        if (_mainArray.size()==1){
            setRequestNodeDisplay(node.getNodeNumber());
        }
        // notify the JTable object that a row has changed; do that in the Swing thread!
        fireTableDataChanged();
    }
    
    /**
     * Returns an existing command station by cs number, NOT node number
     * @param csnum The Command Station Number ( the default in CBUS is 0 )
     * @return the Node which has the command station number, else null
     */
    @CheckForNull
    public CbusNode getCsByNum(int csnum) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getCsNum() == csnum ) {
                return _mainArray.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a new or existing command station by cs number, NOT node number
     * 
     * @param csnum The Command Station Number to provide by
     * @param nodenum if existing CS sets node num to this, else node with this number and starts param lookup
     * 
     * @return the Node which has the command station number
     */
    @Nonnull
    protected CbusNode provideCsByNum(int csnum, int nodenum) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getCsNum() == csnum ) {
                _mainArray.get(i).setNodeNumber(nodenum);
                return _mainArray.get(i);
            }
        }
        CbusNode cs = provideNodeByNodeNum( nodenum);
        cs.setCsNum(csnum);
        return cs;
    }
    
    /**
     * Returns a new or existing node by node number
     * 
     * @param nodenum number to search nodes by, else creates node with this number and starts param lookup
     * 
     * @return the Node which has the node number
     */
    @Nonnull
    public CbusNode provideNodeByNodeNum(int nodenum ) {
        if ( nodenum < 1 || nodenum > 65535 ) {
            throw new IllegalArgumentException("Node number should be between 1 and 65535");
        }
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i);
            }
        }
        CbusNode cs = new CbusNode(_memo, nodenum);
        addNode(cs);
        return cs;        
    }
    
    /**
     * Returns an existing node by table row number
     * @param rowNum The Row Number
     * @return the Node
     */
    public CbusNode getNodeByRowNum(int rowNum) {
        return _mainArray.get(rowNum);
    }
    
    /**
     * Returns the table row number by node number
     * @param nodenum The Node Number ( min 1, max 65535 )
     * @return the Model Row which has the node number, else -1
     */    
    public int getNodeRowFromNodeNum(int nodenum) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return i;
            }
        }
        return -1;
    }

    /**
     * For a given CAN ID, if in use, return formatted Node Name and number
     * else returns zero length string
     * @param canId the CAN ID to search the table for
     * @return Node Number and name
     */
    public String getNodeNameFromCanId (int canId) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeCanId() == canId ) {
                return _mainArray.get(i).getNodeStats().getNodeNumberName();
            }
        }
        return ("");
    }

    /**
     * Returns Node number of any node currently in Learn Mode
     * @return Node Num, else -1 if no nodes known to be in learn mode
     */ 
    public int getAnyNodeInLearnMode(){
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeInLearnMode() ) {
                return _mainArray.get(i).getNodeNumber();
            }
        }
        return -1;
    }
    
    /**
     * Returns an existing node by node number
     * @param nodenum The Node Number ( min 1, max 65535 )
     * @return the Node which has the node number, else null
     */
    @CheckForNull
    public CbusNode getNodeByNodeNum( int nodenum ) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i);
            }
        }
        return null;        
    }
    
    /**
     * Remove Row from table and dispose of it
     * @param row int row number
     * @param removeXml true to also remove the Node xml file
     */
    public void removeRow(int row, boolean removeXml) {
        CbusNode toRemove = getNodeByNodeNum( _mainArray.get(row).getNodeNumber() );
        _mainArray.remove(row);
        if (toRemove != null) {
            if (this instanceof CbusNodeTableDataModel) {
                toRemove.removePropertyChangeListener((CbusNodeTableDataModel)this );
            }
            if (removeXml) {
                // delete xml file
                if (!(toRemove.getNodeBackupManager().removeNode(true))){
                    log.error("Unable to delete node xml file");
                }
            }
            ThreadingUtil.runOnGUI( ()->{ fireTableRowsDeleted(row,row); });
            toRemove.dispose();
        }
    }
    
    /**
     * Returns the next available Node Number
     * @param higherthan Node Number
     * @return calculated next available number, else original value
     */
    public int getNextAvailableNodeNumber( int higherthan ) {
        if ( getRowCount() > 0 ) {
            for (int i = 0; i < getRowCount(); i++) {
                // log.debug("get next available i {} rowcount {}",i,getRowCount() );
                if ( _mainArray.get(i).getNodeNumber() < 65534 ) {
                    if ( _mainArray.get(i).getNodeNumber() >= higherthan ) {
                        higherthan = _mainArray.get(i).getNodeNumber() + 1;
                    }
                }
            }
        }
        return higherthan;
    }
    
    /**
     * Returns a string ArrayList of all Node Number and User Names on the table
     * @return Node Number + either node model or Username.
     */
    @Nonnull
    public ArrayList<String> getListOfNodeNumberNames(){
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            list.add( _mainArray.get(i).getNodeStats().getNodeNumberName() );
        }
        return list;
    }
    
    /**
     * Returns formatted Node Number and User Name by node number
     * @param nodenum The Node Number ( min 1, max 65535 )
     * @return Node Number + either node model or Username.
     */    
    public String getNodeNumberName( int nodenum ) {
        for (int i = 0; i < getRowCount(); i++) {
            if ( _mainArray.get(i).getNodeNumber() == nodenum ) {
                return _mainArray.get(i).getNodeStats().getNodeNumberName();
            }
        }
        return ("");
    }
    
    /**
     * Single Node User Name
     * @param nn Node Number, NOT row number
     * @return Node Username, if unset returns node type name, else empty String
     */
    @Nonnull
    public String getNodeName( int nn ) {
        int rownum = getNodeRowFromNodeNum(nn);
        if ( rownum < 0 ) {
            return "";
        }
        if ( !_mainArray.get(rownum).getUserName().isEmpty() ) {
            return _mainArray.get(rownum).getUserName();
        }
        if ( !_mainArray.get(rownum).getNodeStats().getNodeTypeName().isEmpty() ) {
            return _mainArray.get(rownum).getNodeStats().getNodeTypeName();
        }        
        return "";
    }
    
    private int requestNodeToDisplay = 0;
    
    public int getRequestNodeRowToDisplay(){
        return getNodeRowFromNodeNum(requestNodeToDisplay);
    }
    
    public void setRequestNodeDisplay(int nodeNumber){
        requestNodeToDisplay = nodeNumber;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusBasicNodeTableOperations.class);
}
