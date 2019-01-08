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
import javax.swing.Timer;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.Path;
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

    protected int _contype=0; //  pane console message type
    protected String _context=null; // pane console text

    private PropertyChangeListener _cconSignalMastListener = null;
    private int pFromDir[] = new int[50];
    
    private ArrayList<LocoAddress> locoidarr; // loco id without long flag as seen in can message
    private ArrayList<Integer> directionarr; // loco direction
    private ArrayList<Block> blockarr;   // current block
    private ArrayList<String> nextblockarr;
    private ArrayList<PropertyChangeListener> mBlockListeners; // ??
    private ArrayList<SignalMast> curmastarr; // updates table when changed
    private ArrayList<Block> mBlockList; // master block list
    private ArrayList<Boolean> cabsigarr; // on or off
    private ArrayList<Integer> cabsigvalarr; // cabsig value int speed, aspect1, aspect2
    public Boolean autoreverseblockdir = true;
    public Boolean masterSendCabData = true;
    protected int cabspeedtype=0; // initially set to disabled
    static private int MAX_LINES = 5000;
    TextAreaFIFO tablefeedback;
    
    // column order needs to match list in column tooltips

    static public final int LOCO_ID_COLUMN = 0;
    static public final int LOCO_DIRECTION_COLUMN = 1;
    static public final int CURRENT_BLOCK = 2;
    static public final int BLOCK_DIR = 3;
    static public final int REVERSE_BLOCK_DIR_BUTTON_COLUMN = 4;
    static public final int NEXT_BLOCK = 5;
    static public final int NEXT_SIGNAL = 6;
    static public final int NEXT_ASPECT = 7;
    static public final int SEND_CABSIG_COLUMN = 8;
    
    static public final int MAX_COLUMN = 9;
    
    static protected final int[] startupColumns = {0,1,2,3,4,5,6,7,8};
    
    CabSignalTableModel(int row, int column) {
        
        locoidarr = new ArrayList<LocoAddress>();
        directionarr = new ArrayList<Integer>();
        blockarr = new ArrayList<Block>();
        nextblockarr = new ArrayList<String>();
        curmastarr = new ArrayList<SignalMast>();
        mBlockListeners = new ArrayList<PropertyChangeListener>();
        cabsigarr = new ArrayList<Boolean>();
        cabsigvalarr= new ArrayList<Integer>();
        tablefeedback = new TextAreaFIFO(MAX_LINES);
        initblocks();
        
    }
    
    TextAreaFIFO tablefeedback(){
        return tablefeedback;
    }

    // order needs to match column list top of dtabledatamodel
    static protected final String[] columnToolTips = {
        ("Session ID"),
        null, // loco id
        null, // estop
        ("If Loco ID heard by long address format"),
        ("Speed Commanded by throttle / CAB"),
        ("Forward or Reverse"),
        ("Any Functions set to ON"),
        ("Speed Steps"),
        null, // consist id
        null, // flags
        ("Alternative Train Describer block value to use ( editable )"),
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
        return locoidarr.size();
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
            case LOCO_DIRECTION_COLUMN:
                return ("Direction");
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
            case LOCO_DIRECTION_COLUMN:
                return new JTextField(8).getPreferredSize().width;
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
                return Integer.class;
            case LOCO_DIRECTION_COLUMN:
                return String.class;
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
                return locoidarr.get(row);
            case LOCO_DIRECTION_COLUMN: 
                if ( directionarr.get(row) > -1 ) {
                    if ( directionarr.get(row) == 1 ) {
                        return(Bundle.getMessage("FWD"));
                    } else {
                        return(Bundle.getMessage("REV"));
                    }
                } else {
                    return "";
                }
            case CURRENT_BLOCK:
                b = blockarr.get(row);
                if ( b != null){
                    return b.getUserName();
                } else {
                    return "";
                }
                // return currblockarr.get(row);
            case BLOCK_DIR:
                // return blockdirarr.get(row);
                b = blockarr.get(row);
                if ( b != null){
                    return Path.decodeDirection(b.getDirection());
                } else {
                    return "";
                }
            case REVERSE_BLOCK_DIR_BUTTON_COLUMN:
                final JButton chngblockbutton = new JButton("Chng Direction");
                chngblockbutton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        resetblock(row);
                        chngblockdir(row);
                    }
                });
                if (blockarr.get(row)==null){
                    // log.warn("block dir button null block");
                    chngblockbutton.setText("Block Lookup");
                }
                return chngblockbutton;
            case NEXT_BLOCK:
                return nextblockarr.get(row);
            case NEXT_SIGNAL:
                mast = curmastarr.get(row);
                if (mast!=null) {
                    return mast.getDisplayName();
                }
                return "";
            case NEXT_ASPECT:
                mast = curmastarr.get(row);
                if (mast!=null) {
                    return mast.getAspect();
                }
                return "";
            case SEND_CABSIG_COLUMN:
                return cabsigarr.get(row);
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
            locoidarr.set(row,(LocoAddress)  value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == LOCO_DIRECTION_COLUMN) {
            String speedflags = String.format("%8s", 
            Integer.toBinaryString((Integer) value & 0xFF)).replace(' ', '0');
            int decimal = Integer.parseInt((speedflags.substring(1)), 2);
            int olddir = directionarr.get(row);
            int newdir = Integer.parseInt(String.valueOf(speedflags.charAt(0)));
            if ( decimal == 1 ) {
                newdir=-1;
            }
            directionarr.set(row, newdir);
            if ( autoreverseblockdir && (olddir != newdir)) {
                // log.debug("loco direction changed");
                chngblockdir(row);
            }
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
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
            nextblockarr.set(row, (String) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == NEXT_SIGNAL) {          
        }
        else if (col == NEXT_ASPECT) {          
        }
        else if (col == SEND_CABSIG_COLUMN) {
            cabsigarr.set(row, (Boolean) value);
            
            if ((Boolean)value==true){
                updateblocksforrow(row);
            }
            else {
                cancelcabsig( row);
            }
            
        }
    }

    private synchronized int createnewrow(LocoAddress locoid){
        locoidarr.add(locoid);
        directionarr.add(-1);
        nextblockarr.add("");
        blockarr.add(null);
        curmastarr.add(null);
        cabsigarr.add(true);
        cabsigvalarr.add(0);
        
        Runnable r = new Notify(getRowCount(), this);   // -1 in first arg means all
        javax.swing.SwingUtilities.invokeLater(r);
        return getRowCount()-1;
    }
    
    synchronized void addRow(LocoAddress locoid){
        for (int i = 0; i < getRowCount(); i++) {
            // log.warn("check {} for locoid {}",i,locoid);
            if (locoid==locoidarr.get(i))  {
                return;
            }
        }
        createnewrow(locoid);
    }
    
    // takes a string returns row if matches locoid or alt td
    private int getrowfromstringval(String blockval){
        for (int i = 0; i < getRowCount(); i++) {
            String locoidstr = locoidarr.get(i).toString();
            if (Objects.equals(blockval,locoidstr)) {
                return i;
            }
        }
        return -1;
    }    
    
    private int getrowfromblock( Block blocktotest ){
        for (int i = 0; i < getRowCount(); i++) {
            Block b = blockarr.get(i);
            if ( ( b != null ) && (b.equals(blocktotest)) ){
                return i;
            }
        }
        return -1;
    }

    private void resetblock(int row) {
        blockarr.set(row,(findblockforrow(row)));
        updateblocksforrow(row);
       // Runnable r = new Notify(row, this);
       // javax.swing.SwingUtilities.invokeLater(r);        
    }
    
    // Adds changelistener to blocks
    private void initblocks(){
        mBlockList=null;
        mBlockList = new ArrayList<>();
        BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        Set<Block> blockSet = bmgr.getNamedBeanSet();
        int i = 0;
        for (Block b : blockSet) {
            mBlockList.add(b);
            final int index = i; 
            PropertyChangeListener listener = (PropertyChangeEvent e) -> {
                handleBlockChange(index,e);
            };
            b.addPropertyChangeListener(listener);
            mBlockListeners.add(listener);
            i++;
        }
    }
    
    /**
     * Handle tasks when block changes
     *
     * @param e propChgEvent
     */
    private void handleBlockChange(int index, PropertyChangeEvent e) {
        Block b = mBlockList.get(index);
        Object val = b.getValue();
        log.debug("block {} change e {}",b,e);
        int arow = -1;
        if ( val != null ) {
            String strval = val.toString();
            arow = getrowfromstringval(strval);
        }        

        if (e.getPropertyName().equals("value")){
            // log.warn("val {}",val);
            // check if block is attached to a row
            int row = getrowfromblock(b);
            if (( row > -1 ) && ( row !=arow )){
                // log.warn("need to reset block for row {}",row);
                resetblock(row);
            }
            return;
        }
        
        // block value is changed before direction is set
        if ((e.getPropertyName().equals("state")) || (e.getPropertyName().equals("direction"))) {
            if (arow > -1 ) {
                blockarr.set(arow,b);
                updateblocksforrow(arow);
            }
        }
    }
    
    private void updateblocksforrow(int row){
        Runnable r;
        List<Block> routelist = new ArrayList<>();
        if (curmastarr.get(row) != null) {
            curmastarr.get(row).removePropertyChangeListener(_cconSignalMastListener);
        }
        curmastarr.set(row,null);
        // update table row
        r = new Notify(row, this);
        javax.swing.SwingUtilities.invokeLater(r);
        
        Block b = blockarr.get(row);
        Block nB;
        SignalMast sm = null;
        LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        
        int dir = 0;
        int blockstep = 0;
        
        if ( b != null ) {
            dir = b.getDirection();
            routelist.add(b);
            pFromDir[0] = dir;
        }
        
        if ( dir > 0 ) {
            nB = getnextblock(blockstep,(routelist.get(blockstep)),pFromDir[blockstep]);
            routelist.add(nB);
            while (sm == null && nB != null) {
                sm = lbm.getFacingSignalMast(b, nB);
                if (sm == null) {
                    blockstep++;                                
                    b = nB;
                    nB = getnextblock(blockstep,(routelist.get(blockstep)),pFromDir[blockstep]);
                    routelist.add(nB);
                }
            }
            if ( sm == null) {
                sm = lbm.getSignalMastAtEndBumper(routelist.get(blockstep),null);
            }
            if ( sm != null) {
                // add signal changelistener
                sm.addPropertyChangeListener(_cconSignalMastListener = (PropertyChangeEvent e) -> {
                    updateblocksforrow(row);
                });
                curmastarr.set(row,sm);
                // update table row
                r = new Notify(row, this);
                javax.swing.SwingUtilities.invokeLater(r);
            }
        
            if ( routelist.size()==1 ) {
                setValueAt("", row, NEXT_BLOCK);
            } else {
                if ( routelist.get(1)==null) {
                    setValueAt("", row, NEXT_BLOCK);
                } else {
                    setValueAt(routelist.get(1).getUserName(), row, NEXT_BLOCK);
                }
            }
        } else {
            // no direction
            setValueAt("", row, NEXT_BLOCK);
        }
        
        calculatecabsig(row);
    }
    
    private Block getnextblock(int step, Block b, int fromdirection){
        List<Path> thispaths =b.getPaths();
        for (final Path testpath : thispaths) {
            if (testpath.checkPathSet()) {
                Block blockTest = testpath.getBlock();
                int dirftTest = testpath.getFromBlockDirection();
                int dirtoTest = testpath.getToBlockDirection();
                if ((((fromdirection & Path.NORTH) != 0) && ((dirtoTest & Path.NORTH) != 0)) ||
                    (((fromdirection & Path.SOUTH) != 0) && ((dirtoTest & Path.SOUTH) != 0)) ||
                    (((fromdirection & Path.EAST) != 0) && ((dirtoTest & Path.EAST) != 0)) ||
                    (((fromdirection & Path.WEST) != 0) && ((dirtoTest & Path.WEST) != 0)) ||
                    (((fromdirection & Path.CW) != 0) && ((dirtoTest & Path.CW) != 0)) ||
                    (((fromdirection & Path.CCW) != 0) && ((dirtoTest & Path.CCW) != 0)) ||
                    (((fromdirection & Path.LEFT) != 0) && ((dirtoTest & Path.LEFT) != 0)) ||
                    (((fromdirection & Path.RIGHT) != 0) && ((dirtoTest & Path.RIGHT) != 0)) ||
                    (((fromdirection & Path.UP) != 0) && ((dirtoTest & Path.UP) != 0)) ||
                    (((fromdirection & Path.DOWN) != 0) && ((dirtoTest & Path.DOWN) != 0)))
                { // most reliable
                    pFromDir[(step+1)] = dirtoTest;
                    return blockTest;
                }
                if (((fromdirection & dirftTest)) == 0) { // less reliable
                    pFromDir[(step+1)] = dirtoTest;
                    return blockTest;
                }
                if ((fromdirection != dirftTest)){ // least reliable but copes with 180 degrees 
                    pFromDir[(step+1)] = dirtoTest;
                    return blockTest;
                }
            }
        }
      return null;
    }

    // returns block for a given row
    // loops through blocklist, compares each block value to row loco id + alternative td
    private Block findblockforrow(int row) {
        // log.warn("total blocks {} ",(mBlockList.size()) );
        for (Block tb : mBlockList) {
            Object val = tb.getValue();
            if ( val != null ) {
                String strval = val.toString();
                int testrow = getrowfromstringval(strval); // checks loco id and alt td
                if (testrow==row){
                    // log.warn("Block found {} ",tb.getUserName());
                    return tb;
                }
            }
        }
        return null;
    }

    private void chngblockdir(int row){
        // log.debug("changing block direction for row {}",row);
        StringBuilder buf = new StringBuilder();
        int olddirection = 0;
        Block b = blockarr.get(row); 
        if (b == null){
            buf.append("Searching for block");
            b=findblockforrow(row);
            if (b==null){
                return;
            } else {
                blockarr.set(row,b);
                olddirection=b.getDirection();
            }
        }
        else {
            olddirection=b.getDirection();
            buf.append("Block set to {} direction {} " + b.getUserName() + " " + (String.valueOf(olddirection)) );
        }
        
        // String directionstr = Path.decodeDirection(b.getDirection());
        // log.warn("olddirection {} ",olddirection);
        buf.append(" Direction to reverse : " + Path.decodeDirection(olddirection) );
        
        if (olddirection==0){
            buf.append("No direction found, setting North East.");
            b.setDirection(80);
        } else {
            buf.append(" direction found, setting reverse.");
            b.setDirection(Path.reverseDirection(olddirection));
        }
        log.debug("{}",buf);
        updateblocksforrow(row);        
    }
    

    private int calculatecabspeed(int row){
        if (cabspeedtype==0){
           return 0xff;
        }
        return 0xff;
    }

    private void calculatecabsig(int row){
        // log.warn("calculatecabsig for row {}",row);
        int speed=calculatecabspeed(row);
        int bothaspects=0xff;  // default to no value  aspect1 

        SignalMast mast = curmastarr.get(row);
        if (mast!=null) {
            String aspect = mast.getAspect();
            bothaspects = getSigType(aspect);
        }
        
        int finalval=(((bothaspects)*256))+speed;
        
        int oldval = cabsigvalarr.get(row);
        
        if (oldval != finalval){
            debugcabsig(finalval);
            cabsigvalarr.set(row,finalval);
            sendcabsig(row);
        }
        return;
    }
    
    public void sendcabsig(int row){
        if (!masterSendCabData || !cabsigarr.get(row) ){
            return;
        }
       
        StringBuilder buf = new StringBuilder();
        buf.append("Sending Cabdata Cabsig");
        addToLog(0,buf.toString());
        
        // log.warn("send cabsig");
        int cabsigint = cabsigvalarr.get(row);
        LocoAddress locoaddr = locoidarr.get(row);
        
        // TODO: implement forwarding cab signal data 

        log.debug("cab {} signal {}",locoaddr,cabsigint);
        
    }

    public void debugcabsig(int val){// aspect2 hi, aspect1 low
        
        StringBuilder buf = new StringBuilder();        
        // buf.append("debugcabsig debugging val: " + val);
        
        int aspectone = ( val >> 8);
        int aspecttwo = ( val >> 16);
        int speed = ( val & 0xff);
        
        buf.append(" Aspect 1:" + aspectone);
        buf.append(" Aspect 2:" + aspecttwo);
        buf.append(" speed:" + speed);  
        
        buf.append(" \n Aspect 1 ");
        buf.append("bit0:");
        buf.append(((aspectone >> 0 ) & 1)); // bit0
        buf.append(" bit1:");        
        buf.append(((aspectone >> 1 ) & 1)); // bit1  
        buf.append(" bit2:");        
        buf.append(((aspectone >> 2 ) & 1)); // bit2
        buf.append(" bit3:");        
        buf.append(((aspectone >> 3 ) & 1)); // bit3       
        buf.append(" bit4:");        
        buf.append(((aspectone >> 4 ) & 1)); // bit4      
        buf.append(" bit5:");        
        buf.append(((aspectone >> 5 ) & 1)); // bit5
        buf.append(" bit6:");        
        buf.append(((aspectone >> 6 ) & 1)); // bit6
        buf.append(" bit7:");
        buf.append(((aspectone >> 7 ) & 1)); // bit7
        
        buf.append(" 2bit aspect code:");
        buf.append(((aspectone >> 1 ) & 1)); // bit1  
        buf.append(((aspectone >> 0 ) & 1)); // bit0
        
        buf.append(" \n Aspect 2");
        buf.append("bit0:");
        buf.append(((aspecttwo >> 0 ) & 1)); // bit0
        buf.append(" bit1:");        
        buf.append(((aspecttwo >> 1 ) & 1)); // bit1  
        buf.append(" bit2:");        
        buf.append(((aspecttwo >> 2 ) & 1)); // bit2
        buf.append(" bit3:");        
        buf.append(((aspecttwo >> 3 ) & 1)); // bit3       
        buf.append(" bit4:");        
        buf.append(((aspecttwo >> 4 ) & 1)); // bit4      
        buf.append(" bit5:");        
        buf.append(((aspecttwo >> 5 ) & 1)); // bit5
        buf.append(" bit6:");        
        buf.append(((aspecttwo >> 6 ) & 1)); // bit6
        buf.append(" bit7:");
        buf.append(((aspecttwo >> 7 ) & 1)); // bit7   

        addToLog(0,buf.toString());

    }
    
    public int getSigType(String aspect) {
        // look for the opcode
        if (cabSigMap.get(aspect)==null){
            log.warn("Cabsig unable to translate aspect {} Not Found",aspect);
            return 0xff;
        }
        else {
            return cabSigMap.get(aspect);
        }
    }
    
    public Map<String, Integer> cabSigMap = createCabSigMap();

    private Map<String, Integer> createCabSigMap() {
        Map<String, Integer> result = new HashMap<>();
        result.put("Danger",0); // NOI18N
        result.put("Caution",1); // NOI18N
        result.put("Preliminary Caution",2); // NOI18N
        result.put("Proceed", 3); // NOI18N
        result.put("Flash Caution", 257); // NOI18N
        result.put("Flash Preliminary Caution", 258); // NOI18N
        result.put("Off", 4); // NOI18N
        result.put("On", 0); // NOI18N
        return Collections.unmodifiableMap(result);
    }
    
    public void cancelcabsig(int row){
        // log.warn("cancel cabsig row {}",row);
        cabsigvalarr.set(0,row);
        LocoAddress locoaddr = locoidarr.get(row);
        StringBuilder buf = new StringBuilder();
        buf.append("Cancelling Cabdata for loco " + locoaddr);
        addToLog(0,buf.toString());

        // send cancel cab signal.
    }
    
    protected void masterSendCabDataButton(Boolean but){
        for (int i = 0; i < getRowCount(); i++) {
            if (but){
                calculatecabsig(i);
            } else {
                cancelcabsig(i);
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
    
    static class Notify implements Runnable {
        public int _row;
        javax.swing.table.AbstractTableModel _model;
        public Notify(int row, javax.swing.table.AbstractTableModel model) {
            _row = row;
            _model = model;
        }

        @Override
        public void run() {
              _model.fireTableDataChanged();
        }
    }

    /**
     * disconnect from the CBUS
     */
    public void dispose() {
        // eventTable.removeAllElements();
        // eventTable = null;
        
        for (int i = 0; i < getRowCount(); i++) {
            if (curmastarr.get(i) != null) {
                curmastarr.get(i).removePropertyChangeListener(_cconSignalMastListener);
            }
        }

        masterSendCabDataButton(false); // send data off message to cabs
        
    }

    private final static Logger log = LoggerFactory.getLogger(CabSignalTableModel.class);
}
