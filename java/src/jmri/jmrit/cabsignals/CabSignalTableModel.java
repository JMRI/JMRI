package jmri.jmrit.cabsignals;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.Path;


import jmri.CabSignalManager;
import jmri.SignalMast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Table data model for display of Cab Signaling information.
 *
 * @author Steve Young (c) 2018
 * @author Paul Bender (c) 2018
 * @see CabSignalPane 
 * 
 */
public class CabSignalTableModel extends javax.swing.table.AbstractTableModel {

    private CabSignalManager cabSignalManager;

    public Boolean autoreverseblockdir = true;	
    public Boolean masterSendCabData = true;
 
    static private int MAX_LINES = 5000;
    TextAreaFIFO tablefeedback;
    
    // column order needs to match list in column tooltips

    static public final int LOCO_ID_COLUMN = 0;
    static public final int CURRENT_BLOCK = 1;
    static public final int BLOCK_DIR = 2;
    static public final int REVERSE_BLOCK_DIR_BUTTON_COLUMN = 3;
    static public final int NEXT_BLOCK = 4;
    static public final int NEXT_SIGNAL = 5;
    static public final int NEXT_ASPECT = 6;
    static public final int SEND_CABSIG_COLUMN = 7;
    
    static public final int MAX_COLUMN = 8;
   
    static protected final int[] startupColumns = {0,1,2,3,4,5,6,7};
 
    CabSignalTableModel(int row, int column) {
        cabSignalManager = InstanceManager.getNullableDefault(CabSignalManager.class); 
        if(cabSignalManager == null){
           log.info("creating new DefaultCabSignalManager");
           InstanceManager.store(new jmri.managers.DefaultCabSignalManager(),CabSignalManager.class);
           cabSignalManager = InstanceManager.getNullableDefault(CabSignalManager.class); 
        }
        tablefeedback = new TextAreaFIFO(MAX_LINES);
        
    }
    
    TextAreaFIFO tablefeedback(){
        return tablefeedback;
    }

    // order needs to match column list top of dtabledatamodel
    static protected final String[] columnToolTips = {
        null, // loco id
        ("Block Username"),
        ("North / South / East / West, 8 point block direction"),
        null, // block button
        ("Next block in direction from current block"),
        ("Next signal found"),
        ("Aspect of next signal"),
        ("Chceckbox overridden by master send CabData button switched off")

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
     * used in table header
     * @param col int col number
     */
    @Override
    public String getColumnName(int col) { // not in any order
        switch (col) {
            case LOCO_ID_COLUMN:
                return ("Loco ID");
            case CURRENT_BLOCK:
                return("Block");
            case BLOCK_DIR:
                return("Direction of Block");
            case REVERSE_BLOCK_DIR_BUTTON_COLUMN:
                return("Block Button");
            case NEXT_BLOCK:
                return("Next Block");
            case NEXT_SIGNAL:
                return("Next Signal");
            case NEXT_ASPECT:
                return("Next Aspect");
            case SEND_CABSIG_COLUMN:
                return(Bundle.getMessage("SigDataOn"));
            default:
                return "unknown"; // NOI18N
        }
    }


    /**
    * Returns int of startup column widths
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
                return new JTextField(4).getPreferredSize().width;
            case NEXT_BLOCK:
                return new JTextField(8).getPreferredSize().width;
            case NEXT_SIGNAL:
                return new JTextField(6).getPreferredSize().width;
            case NEXT_ASPECT:
                return new JTextField(10).getPreferredSize().width;
            case SEND_CABSIG_COLUMN:
                return new JTextField(3).getPreferredSize().width;
            default:
                log.warn("no width found row {}",col);
                return new JLabel(" <unknown> ").getPreferredSize().width; // NOI18N
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
                return JButton.class;
            case NEXT_BLOCK:
                return String.class;
            case NEXT_SIGNAL:
                return String.class;
            case NEXT_ASPECT:
                return String.class;
            case SEND_CABSIG_COLUMN:
                return Boolean.class;
            default:
                log.error("no column class located");
                return null;
        }
    }
    
    /**
    * Boolean return to edit table cell or not
    * @return boolean
    */
    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case SEND_CABSIG_COLUMN:
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
     * </p>
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
       tablefeedback.setEditable ( false );
    }

    /**
     * Return table values
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
                final JButton chngblockbutton = new JButton("Chng Direction");
                chngblockbutton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        cabSignalManager.getCabSignalArray()[row].setBlock();
                        chngblockdir(row);
                    }
                });
                if (cabSignalManager.getCabSignalArray()[row].getBlock()==null){
                    // log.warn("block dir button null block");
                    chngblockbutton.setText("Block Lookup");
                }
                return chngblockbutton;
            case NEXT_BLOCK:
                return cabSignalManager.getCabSignalArray()[row].getNextBlock();
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
            log.warn("rev dir button row {}",row);
            // handled by listener on button
        }
        else if (col == NEXT_BLOCK) {
            BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
            Block b = bmgr.getBlock((String)value);
            cabSignalManager.getCabSignalArray()[row].setBlock(b);
        }
        else if (col == NEXT_SIGNAL) {          
        }
        else if (col == NEXT_ASPECT) {          
        }
        else if (col == SEND_CABSIG_COLUMN) {
            if ((Boolean)value==true){
                cabSignalManager.getCabSignalArray()[row].setCabSignalActive((Boolean) value);
            }
            else {
                cabSignalManager.getCabSignalArray()[row].disableCabSignal();
            }
        }
    }

    /**
     * Reverse the direction on the block associated with
     * the row.  Changes to the cab signal happen when the block's 
     * properties change.
     */
    private void chngblockdir(int row){
        log.debug("changing block direction for row {}",row);
        StringBuilder buf = new StringBuilder();
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
            buf.append("Block set to {} direction {} " + b.getUserName() + " " + (String.valueOf(olddirection)) );
        }
        
        buf.append(" Direction to reverse : " + Path.decodeDirection(olddirection) );
        
        if (olddirection==0){
            buf.append("No direction found, setting North East.");
            b.setDirection(80);
        } else {
            buf.append(" direction found, setting reverse.");
            b.setDirection(Path.reverseDirection(olddirection));
        }
        log.debug("{}",buf);
    }
    
    protected void masterSendCabDataButton(Boolean but){
        for (int i = 0; i < getRowCount(); i++) {
            if (but){
                cabSignalManager.getCabSignalArray()[i].getNextMast();
            } else {
                cabSignalManager.getCabSignalArray()[i].disableCabSignal();
            }
        }
    }
    
    
    /**
     * Add to Slot Monitor Console Log
     * @param cbuserror int
     * @param cbustext String console message
     */
    public void addToLog(int cbuserror, String cbustext){
        tablefeedback.append( "\n"+cbustext);
    }


    /**
     * Keeps the message log windows to a reasonable length
     * https://community.oracle.com/thread/1373400
     */
    private static class TextAreaFIFO extends JTextArea implements DocumentListener {
        private int maxLines;
    
        public TextAreaFIFO(int lines) {
            maxLines = lines;
            getDocument().addDocumentListener( this );
        }
    
        public void insertUpdate(DocumentEvent e) {
            javax.swing.SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    removeLines();
                }
            });
        }
        public void removeUpdate(DocumentEvent e) {}
        public void changedUpdate(DocumentEvent e) {}
        public void removeLines()
        {
            Element root = getDocument().getDefaultRootElement();
            while (root.getElementCount() > maxLines) {
                Element firstLine = root.getElement(0);
                try {
                    getDocument().remove(0, firstLine.getEndOffset());
                } catch(BadLocationException ble) {
                    System.out.println(ble);
                }
            }
        setCaretPosition( getDocument().getLength() );
        }
    }
    
    public void dispose() {
    }

    private final static Logger log = LoggerFactory.getLogger(CabSignalTableModel.class);
}
