package jmri.jmrit.cabsignals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.Block;
import jmri.BlockManager;
import jmri.CabSignalManager;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Path;
import jmri.SignalMast;
import jmri.jmrit.roster.RosterEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Table data model for display of Cab Signaling information.
 *
 * @author Steve Young (c) 2018
 * @author Paul Bender (c) 2018
 * @see CabSignalPane
 * @since 4.13.4
 */
public class CabSignalTableModel extends javax.swing.table.AbstractTableModel {

    private CabSignalManager cabSignalManager;
    
    // column order needs to match list in columnToolTips

    static public final int LOCO_ID_COLUMN = 0;
    static public final int SEND_CABSIG_COLUMN = 1;
    static public final int CURRENT_BLOCK = 2;
    static public final int BLOCK_DIR = 3;
    static public final int REVERSE_BLOCK_DIR_BUTTON_COLUMN = 4;
    static public final int NEXT_BLOCK = 5;
    static public final int NEXT_SIGNAL = 6;
    static public final int NEXT_ASPECT = 7;
    static public final int NEXT_ASPECT_ICON = 8;
    
    static public final int MAX_COLUMN = 9;
   
    static protected final int[] startupColumns = {0,1,2,3,4,5,6,7,8};
 
    CabSignalTableModel(int row, int column) {
        cabSignalManager = InstanceManager.getNullableDefault(CabSignalManager.class); 
        if(cabSignalManager == null){
           log.info("creating new DefaultCabSignalManager");
           InstanceManager.store(new jmri.managers.DefaultCabSignalManager(), CabSignalManager.class);
           cabSignalManager = InstanceManager.getNullableDefault(CabSignalManager.class); 
        }
    }

    // order needs to match column list top of dtabledatamodel
    static protected final String[] columnToolTips = {
        null, // loco id
        Bundle.getMessage("CabsigCheckboxTip"),
        Bundle.getMessage("BlockUserName"),
        Bundle.getMessage("BlockDirectionTip"),
        null, // block lookup button
        Bundle.getMessage("NextBlockTip"),
        Bundle.getMessage("NextSignalTip"),
        Bundle.getMessage("NextAspectTip"),
        Bundle.getMessage("NextAspectTip"), // aspect icon

    }; // Length = number of items in array should (at least) match number of columns
    
    /**
     * Return the number of rows to be displayed.
     */
    @Override
    public int getRowCount() {
        return cabSignalManager.getCabSignalList().size();
    }

    @Override
    public int getColumnCount() {
        return MAX_COLUMN;
    }

    /**
     * Returns String of column name from column int
     * used in table header.
     *
     * @param col int col number
     */
    @Override
    public String getColumnName(int col) { // not in any order
        switch (col) {
            case LOCO_ID_COLUMN:
                return Bundle.getMessage("LocoID");
            case CURRENT_BLOCK:
                return Bundle.getMessage("Block");
            case BLOCK_DIR:
                return Bundle.getMessage("BlockDirection");
            case REVERSE_BLOCK_DIR_BUTTON_COLUMN:
                return Bundle.getMessage("BlockButton");
            case NEXT_BLOCK:
                return Bundle.getMessage("NextBlock");
            case NEXT_SIGNAL:
                return Bundle.getMessage("NextSignal");
            case NEXT_ASPECT:
                return Bundle.getMessage("NextAspect");
            case SEND_CABSIG_COLUMN:
                return Bundle.getMessage("SigDataOn");
            case NEXT_ASPECT_ICON:
                return Bundle.getMessage("NextAspect");
            default:
                return "unknown"; // NOI18N
        }
    }

    /**
     * Returns int of startup column widths.
     *
     * @param col int col number
     */
    public static int getPreferredWidth(int col) {
        switch (col) {
            case LOCO_ID_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case CURRENT_BLOCK:
                return new JTextField(8).getPreferredSize().width;
            case BLOCK_DIR:
                return new JTextField(6).getPreferredSize().width;
            case REVERSE_BLOCK_DIR_BUTTON_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case NEXT_BLOCK:
                return new JTextField(8).getPreferredSize().width;
            case NEXT_SIGNAL:
                return new JTextField(6).getPreferredSize().width;
            case NEXT_ASPECT:
                return new JTextField(10).getPreferredSize().width;
            case SEND_CABSIG_COLUMN:
                return new JTextField(3).getPreferredSize().width;
            case NEXT_ASPECT_ICON:
                return new JTextField(3).getPreferredSize().width;
            default:
                log.warn("no width found col {}",col);
                return new JTextField(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }

    /**
     * Returns column class type.
     */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case LOCO_ID_COLUMN:
                return LocoAddress.class;
            case CURRENT_BLOCK:
                return String.class;
            case BLOCK_DIR:
                return String.class;
            case REVERSE_BLOCK_DIR_BUTTON_COLUMN:
                return javax.swing.JButton.class;
            case NEXT_BLOCK:
                return String.class;
            case NEXT_SIGNAL:
                return String.class;
            case NEXT_ASPECT:
                return String.class;
            case SEND_CABSIG_COLUMN:
                return Boolean.class;
            case NEXT_ASPECT_ICON:
                return String.class;
            default:
                log.error("no column class located");
                return null;
        }
    }
    
    /**
     * Boolean return to edit table cell or not.
     *
     * @return boolean
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case SEND_CABSIG_COLUMN:
            case REVERSE_BLOCK_DIR_BUTTON_COLUMN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Configure a table to have our standard rows and columns.
     * <p>
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     */
    public void configureTable(JTable cmdStatTable) {
        // allow reordering of the columns
        cmdStatTable.getTableHeader().setReorderingAllowed(true);

        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        cmdStatTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < cmdStatTable.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            cmdStatTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
       // cmdStatTable.sizeColumnsToFit(-1);
    }

    /**
     * Return table values.
     *
     * @param row int row number
     * @param col int col number
     */
    @Override
    public Object getValueAt(int row, int col) {
        SignalMast mast;
        Block b;
        switch (col) {
            case LOCO_ID_COLUMN:
                cabSignalManager.getCabSignalArray()[row].addPropertyChangeListener( (PropertyChangeEvent e) -> {
                   if(e.getSource() instanceof jmri.CabSignal){
                      fireTableDataChanged();
                   }
                });
                return cabSignalManager.getCabSignalArray()[row].getCabSignalAddress();
            case CURRENT_BLOCK:
                b = cabSignalManager.getCabSignalArray()[row].getBlock();
                if ( b != null){
                    return b.getDisplayName();
                } else {
                    return "";
                }
            case BLOCK_DIR:
                b = cabSignalManager.getCabSignalArray()[row].getBlock();
                if ( b != null){
                    return Path.decodeDirection(b.getDirection());
                } else {
                    return "";
                }
            case REVERSE_BLOCK_DIR_BUTTON_COLUMN:
                if (cabSignalManager.getCabSignalArray()[row].getBlock()==null){
                    return Bundle.getMessage("BlockLookup");
                } else {
                    return Bundle.getMessage("ChngDirection");
                }
            case NEXT_BLOCK:
                Block nextBl = cabSignalManager.getCabSignalArray()[row].getNextBlock();
                if ( nextBl != null){
                    return nextBl.getDisplayName();
                } else {
                    return "";
                }
            case NEXT_SIGNAL:
                mast = cabSignalManager.getCabSignalArray()[row].getNextMast();
                if (mast!=null) {
                    return mast.getDisplayName();
                }
                return "";
            case NEXT_ASPECT:
                mast = cabSignalManager.getCabSignalArray()[row].getNextMast();
                if (mast!=null) {
                    return mast.getAspect();
                }
                return "";
            case NEXT_ASPECT_ICON:
                mast = cabSignalManager.getCabSignalArray()[row].getNextMast();
                if (mast!=null) {
                    String imageLink = mast.getAppearanceMap().getProperty(mast.getAspect(), "imagelink");
                    log.debug("imagelink is {}", imageLink);
                    if ( imageLink != null ) {
                        String newlink = imageLink.replace("../", "");  // replace is immutable
                        // should start at the resources directory
                        return newlink;
                    }
                    else {
                        return "";
                    }
                }
                return "";
            case SEND_CABSIG_COLUMN:
                return cabSignalManager.getCabSignalArray()[row].isCabSignalActive();
            default:
                log.error("internal state inconsistent with table request for row {} col {}", row, col);
                return null;
        }
    }
    
    /**
     * @param value object value
     * @param row int row number
     * @param col int col number
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == LOCO_ID_COLUMN) {
        }
        else if (col == CURRENT_BLOCK) {          
        }
        else if (col == BLOCK_DIR) {
        }
        else if (col == REVERSE_BLOCK_DIR_BUTTON_COLUMN) {
            cabSignalManager.getCabSignalArray()[row].setBlock();
            chngblockdir(row);
        }
        else if (col == NEXT_BLOCK) {
            jmri.util.ThreadingUtil.runOnLayout( ()->{
                BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
                Block b = bmgr.getBlock((String)value);
                cabSignalManager.getCabSignalArray()[row].setBlock(b);
            });
        }
        else if (col == NEXT_SIGNAL) {          
        }
        else if (col == NEXT_ASPECT) {          
        }
        else if (col == SEND_CABSIG_COLUMN) {
            cabSignalManager.getCabSignalArray()[row].setCabSignalActive((Boolean) value);
        }
    }

    /**
     * Reverse the direction on the block associated with
     * the row.  Changes to the cab signal happen when the block's 
     * properties change.
     */
    private void chngblockdir(int row){
        log.debug("changing block direction for row {}", row);
        int olddirection = 0;
        Block b = cabSignalManager.getCabSignalArray()[row].getBlock();
        if (b == null){
            cabSignalManager.getCabSignalArray()[row].setBlock();
            b = cabSignalManager.getCabSignalArray()[row].getBlock();
            if (b==null){
                return;
            } else {
                cabSignalManager.getCabSignalArray()[row].setBlock(b);
                olddirection=b.getDirection();
            }
        }
        else {
            olddirection=b.getDirection();
            log.debug("Block {} set to direction {} ", b.getUserName(), (String.valueOf(olddirection)) );
        }
        
        log.debug(" Direction to reverse :{}", Path.decodeDirection(olddirection) );
        
        if (olddirection==0){
            log.debug("No direction found, setting to North, East");
            b.setDirection(80);
        } else {
            log.debug(" direction found, setting reverse.");
            b.setDirection(Path.reverseDirection(olddirection));
        }
        jmri.util.ThreadingUtil.runOnGUI( ()->{
            fireTableDataChanged();
        });
        log.debug("block {} now has direction {}", b.getUserName(), b.getDirection());
    }
    
    protected void setPanelPauseButton(boolean isPaused){
        for (int i = 0; i < getRowCount(); i++) {
            cabSignalManager.getCabSignalArray()[i].setMasterCabSigPauseActive(isPaused);
        }
    }

    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(CabSignalTableModel.class);

}
