package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

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
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.TrafficController;
import jmri.Path;
import jmri.SignalMast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus Command Station Sessions and various Tools
 * Created with Notepad++
 *
 * @author Steve Young (c) 2018
 * @see CbusSlotMonitorPane
 * 
 */
public class CbusSlotMonitorDataModel extends javax.swing.table.AbstractTableModel implements CanListener  {

    protected int _contype=0; //  pane console message type
    protected String _context=null; // pane console text

    private PropertyChangeListener _cconSignalMastListener = null;
    private int pFromDir[] = new int[50];
    
    private int cmndstat_fw =0; // command station firmware
    
    // private ArrayList<Integer> cmndstatarr;
    private ArrayList<Integer> sessionidarr; // session id given by command station
    // private ArrayList<Integer> throttlecountarr;
    private ArrayList<String> functionarr; // 
    private ArrayList<Integer> locoidarr; // loco id without long flag as seen in can message
    private ArrayList<Boolean> locolongarr; // long address on / off as seen in can message
    // private ArrayList<String> namearr;
    private ArrayList<Integer> directionarr; // loco direction
    private ArrayList<Integer> speedarr; // loco speed
    private ArrayList<String> speedsteparr;  // loco speed steps
    private ArrayList<String> alttdarr; // alternative value for block following
    private ArrayList<Block> blockarr;   // current block
    private ArrayList<String> nextblockarr;
    private ArrayList<PropertyChangeListener> mBlockListeners; // ??
    private ArrayList<SignalMast> curmastarr; // updates table when changed
    private ArrayList<Block> mBlockList; // master block list
    private ArrayList<Boolean[]> funcarray; // array of boolean function values 
    private ArrayList<Integer> consistarr; // consist id
    private ArrayList<String> flagsarr;  // loco flags
    private ArrayList<Boolean> cabsigarr; // on or off
    private ArrayList<Integer> cabsigvalarr; // cabsig value int speed, aspect1, aspect2
    public Boolean autoreverseblockdir = true;
    public Boolean masterSendCabData = true;
    protected int cabspeedtype=0; // initially set to disabled
    private Timer estopTimer;
    private Timer powerTimer;
    static private int MAX_LINES = 5000;
    TextAreaFIFO tablefeedback;
    CanSystemConnectionMemo memo;
    TrafficController tc;
    
    // column order needs to match list in column tooltips

    static public final int SESSION_ID_COLUMN = 0; 

    static public final int LOCO_ID_COLUMN = 1;
    static public final int ESTOP_COLUMN = 2;
    static public final int LOCO_ID_LONG_COLUMN = 3;

    static public final int LOCO_COMMANDED_SPEED_COLUMN = 4;    
    static public final int LOCO_DIRECTION_COLUMN = 5;
    static public final int FUNCTION_LIST = 6;
    static public final int SPEED_STEP_COLUMN = 7;
    static public final int LOCO_CONSIST_COLUMN = 8;
    static public final int FLAGS_COLUMN = 9;

    static public final int ALT_TD = 10;
    static public final int CURRENT_BLOCK = 11;
    static public final int BLOCK_DIR = 12;
    static public final int REVERSE_BLOCK_DIR_BUTTON_COLUMN = 13;
    static public final int NEXT_BLOCK = 14;
    static public final int NEXT_SIGNAL = 15;
    static public final int NEXT_ASPECT = 16;
    static public final int SEND_CABSIG_COLUMN = 17;
    
    // spare partially coded
    static public final int NUM_THROTTLES = 97;
    static public final int LOCO_NAME_COLUMN = 98;
    static public final int CMND_STATION_ID_COLUMN = 99;
    
    static public final int MAX_COLUMN = 18;
    
    static protected final int[] startupColumns = {0,1,2,4,5,11,13,16};
    
    CbusSlotMonitorDataModel(CanSystemConnectionMemo memo, int row, int column) {
        
        // cmndstatarr = new ArrayList<Integer>();
        sessionidarr = new ArrayList<Integer>();
        //  throttlecountarr = new ArrayList<Integer>();
        functionarr = new ArrayList<String>();
        locoidarr = new ArrayList<Integer>();
        locolongarr = new ArrayList<Boolean>();
        // namearr = new ArrayList<String>();
        directionarr = new ArrayList<Integer>();
        speedarr = new ArrayList<Integer>();
        speedsteparr = new ArrayList<String>();
        alttdarr = new ArrayList<String>();
        blockarr = new ArrayList<Block>();
        nextblockarr = new ArrayList<String>();
        curmastarr = new ArrayList<SignalMast>();
        mBlockListeners = new ArrayList<PropertyChangeListener>();
        funcarray = new ArrayList<Boolean[]>();
        consistarr = new ArrayList<Integer>();
        flagsarr = new ArrayList<String>();
        cabsigarr = new ArrayList<Boolean>();
        cabsigvalarr= new ArrayList<Integer>();
        // connect to the CanInterface
        tc = memo.getTrafficController();
        tc.addCanListener(this);
        tablefeedback = new TextAreaFIFO(MAX_LINES);
        initblocks();
        
        getcmdstatversion();
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
        return sessionidarr.size();
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
            case CMND_STATION_ID_COLUMN:
                // return Bundle.getMessage("CanID");
                return ("Cmnd Station");
            case SESSION_ID_COLUMN:
                return ("Session");
            case LOCO_ID_COLUMN:
                return ("Loco ID");
            case LOCO_ID_LONG_COLUMN:
                return ("Long");
            case LOCO_CONSIST_COLUMN:
                return ("Consist ID");
            case LOCO_NAME_COLUMN:
                return ("Name");
            case LOCO_DIRECTION_COLUMN:
                return ("Direction");
            case LOCO_COMMANDED_SPEED_COLUMN:
                return ("Speed - Commanded");
            case ESTOP_COLUMN:
                return ("E-Stop");
            case SPEED_STEP_COLUMN:
                return ("Steps");
            case FLAGS_COLUMN:
                return ("Flags");
            case NUM_THROTTLES:
                return ("Num. Throttles");
            case FUNCTION_LIST:
                return("Functions");
            case ALT_TD:
                return("TD Alt");
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
            case CMND_STATION_ID_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case SESSION_ID_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case LOCO_ID_LONG_COLUMN:
                return new JTextField(3).getPreferredSize().width;
            case LOCO_ID_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case LOCO_CONSIST_COLUMN:
                return new JTextField(3).getPreferredSize().width;
            case FLAGS_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case LOCO_NAME_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case LOCO_DIRECTION_COLUMN:
                return new JTextField(8).getPreferredSize().width;
            case LOCO_COMMANDED_SPEED_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case ESTOP_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case SPEED_STEP_COLUMN:
                return new JTextField(3).getPreferredSize().width;
            case NUM_THROTTLES:
                return new JTextField(3).getPreferredSize().width;
            case FUNCTION_LIST:
                return new JTextField(6).getPreferredSize().width;
            case ALT_TD:
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
            case CMND_STATION_ID_COLUMN:
                return Integer.class;
            case SESSION_ID_COLUMN:
                return Integer.class;
            case LOCO_ID_LONG_COLUMN:
                return Boolean.class;
            case LOCO_ID_COLUMN:
                return Integer.class;
            case LOCO_CONSIST_COLUMN:
                return Integer.class;
            case LOCO_NAME_COLUMN:
                return String.class;
            case LOCO_DIRECTION_COLUMN:
                return String.class;
            case LOCO_COMMANDED_SPEED_COLUMN:
                return Integer.class;
            case ESTOP_COLUMN:
                return JButton.class;
            case NUM_THROTTLES:
                return Integer.class;
            case FUNCTION_LIST:
                return String.class;
            case SPEED_STEP_COLUMN:
                return String.class;
            case FLAGS_COLUMN:
                return String.class;
            case ALT_TD:
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
            case ALT_TD:
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
            case CMND_STATION_ID_COLUMN:
                //    return cmndstatarr.get(row);
                    return null;
            case SESSION_ID_COLUMN:
                if ( sessionidarr.get(row) > 0 ) {
                    return sessionidarr.get(row);
                } else {
                    return null;
                }
            case LOCO_ID_COLUMN:
                return locoidarr.get(row);
            case LOCO_ID_LONG_COLUMN:
                return locolongarr.get(row);
            case LOCO_CONSIST_COLUMN:
                if (consistarr.get(row)>0){
                    return consistarr.get(row);
                } else {
                    return null;
                }
            case FLAGS_COLUMN:
                return flagsarr.get(row);
            case LOCO_NAME_COLUMN:
                // return namearr.get(row);
                return "";
            case LOCO_DIRECTION_COLUMN: 
                if (speedarr.get(row) == 1){
                    return("E Stop");
                }
                if ( directionarr.get(row) > -1 ) {
                    if ( directionarr.get(row) == 1 ) {
                        return(Bundle.getMessage("FWD"));
                    } else {
                        return(Bundle.getMessage("REV"));
                    }
                } else {
                    return "";
                }
            case LOCO_COMMANDED_SPEED_COLUMN:
                if ( speedarr.get(row) > 1 ) {
                    return speedarr.get(row);
                } else {
                    return null;
                }
            case ESTOP_COLUMN:
                final JButton button = new JButton();
                if ( sessionidarr.get(row) > 0 ) {
                    button.setIcon(new NamedIcon("resources/icons/throttles/RedPowerLED.gif", "resources/icons/throttles/RedPowerLED.gif"));
                    }
                else {
                    button.setIcon(null);
                }
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        int direction = directionarr.get(row);
                        int stopspeed=1;
                        if ( ( direction > 0 ) && ( (speedsteparr.get(row)).isEmpty() )){
                            stopspeed=129;
                        } 
                        
                        CanMessage m = new CanMessage(tc.getCanid());
                        m.setNumDataElements(3);
                        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
                        m.setElement(0, CbusConstants.CBUS_DSPD);
                        m.setElement(1, sessionidarr.get(row));
                        m.setElement(2, stopspeed);
                        tc.sendCanMessage(m, null);
                    }
                });
                return button;
            case NUM_THROTTLES:
                // return throttlecountarr.get(row);
                return 0;
            case FUNCTION_LIST:
                return functionarr.get(row);
            case SPEED_STEP_COLUMN:
                if ( (speedsteparr.get(row)).isEmpty() ) {
                    return "128"; // default
                }
                else {
                    return speedsteparr.get(row);
                } 
            case ALT_TD:
                return alttdarr.get(row);
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
        // log.debug("427 set valueat called row: {} col: {}", row, col);
        if (col == CMND_STATION_ID_COLUMN) {
            // cmndstatarr.set(row, (Integer) value);
            // Runnable r = new Notify(-1, this);
            // javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == SESSION_ID_COLUMN) {
            sessionidarr.set(row, (Integer) value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == LOCO_ID_LONG_COLUMN) {
            locolongarr.set(row, (Boolean) value);
        }
        else if (col == LOCO_ID_COLUMN) {
            locoidarr.set(row, (Integer) value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == LOCO_CONSIST_COLUMN) {
            consistarr.set(row, (Integer) value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == LOCO_NAME_COLUMN) {
            // namearr.set(row, (String) value);
            // Runnable r = new Notify(row, this); 
            // javax.swing.SwingUtilities.invokeLater(r);
            }
        else if (col == FLAGS_COLUMN) {
            flagsarr.set(row, (String) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
            }
        else if (col == LOCO_COMMANDED_SPEED_COLUMN) {
            String speedflags = String.format("%8s", 
            Integer.toBinaryString((Integer) value & 0xFF)).replace(' ', '0');
            int decimal = Integer.parseInt((speedflags.substring(1)), 2);
            speedarr.set(row, decimal);
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
        else if (col == ESTOP_COLUMN) {
            // handled by listener on button
        }
        else if (col == NUM_THROTTLES) {
           // throttlecountarr.set(row, (Integer) value);
           // Runnable r = new Notify(row, this); 
           // javax.swing.SwingUtilities.invokeLater(r);
            }
        else if (col == FUNCTION_LIST) {
            functionarr.set(row, (String) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == SPEED_STEP_COLUMN) {
            speedsteparr.set(row, (String) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == ALT_TD) {
            alttdarr.set(row, (String) value);
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
            
            
            // Runnable r = new Notify(row, this); 
            // javax.swing.SwingUtilities.invokeLater(r);            
        }
    }

    private synchronized int createnewrow(int locoid, Boolean islong){
        // log.warn("createnewrow {}",locoid);
        locoidarr.add(locoid);
        locolongarr.add(islong);
        // cmndstatarr.add(-1);
        sessionidarr.add(-1);        
        // namearr.add("");
        directionarr.add(-1);
        speedarr.add(-1);
        speedsteparr.add("");
        alttdarr.add("");
        nextblockarr.add("");
        blockarr.add(null);
        curmastarr.add(null);
        // throttlecountarr.add(0);
        functionarr.add(""); // function string
        funcarray.add(new Boolean[29]); // function list
        consistarr.add(-1);
        flagsarr.add("");
        cabsigarr.add(true);
        cabsigvalarr.add(0);
        
        Runnable r = new Notify(getRowCount(), this);   // -1 in first arg means all
        javax.swing.SwingUtilities.invokeLater(r);
        return getRowCount()-1;
    }
    
    // return row number for a loco address, creates new row if no existing
    private synchronized int gettablerow(int locoid, Boolean islong){
        for (int i = 0; i < getRowCount(); i++) {
            // log.warn("check {} for locoid {}",i,locoid);
            if (locoid==locoidarr.get(i))  {
                return i;
            }
        }
        return createnewrow(locoid,islong);
    }
    
    // takes a string returns row if matches locoid or alt td
    private int getrowfromstringval(String blockval){
        for (int i = 0; i < getRowCount(); i++) {
            String altd = alttdarr.get(i);
            String locoidstr = locoidarr.get(i).toString();
            if (Objects.equals(blockval,altd)) {
                return i;
            }
            if (Objects.equals(blockval,locoidstr)) {
                return i;
            }
        }
        return -1;
    }    
    
    private int getrowfromsession(int sessionid){
        for (int i = 0; i < getRowCount(); i++) {
            if (sessionid==sessionidarr.get(i))  {
                return i;
            }
        }
        // no row so request session details from command station
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(2);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_QLOC);
        m.setElement(1, sessionid);
        tc.sendCanMessage(m, null);
        // should receive a PLOC response with loco id etc.
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
    
    /**
     * @param m canmessage
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
        // see CbusThrottleManager
        int opc = CbusMessage.getOpcode(m);
        // process is false as outgoing message
        
        if (opc==CbusConstants.CBUS_PLOC) {
            int rcvdIntAddr = (m.getElement(2) & 0x3f) * 256 + m.getElement(3);
            boolean rcvdIsLong = (m.getElement(2) & 0xc0) != 0;
            processploc(false,m.getElement(1),rcvdIntAddr,rcvdIsLong,m.getElement(4),
            m.getElement(5),m.getElement(6),m.getElement(7));
        }
        else if (opc==CbusConstants.CBUS_RLOC) {
            int rcvdIntAddr = (m.getElement(1) & 0x3f) * 256 + m.getElement(2);
            boolean rcvdIsLong = (m.getElement(1) & 0xc0) != 0;
            processrloc(false,rcvdIntAddr,rcvdIsLong);
        }
        else if (opc==CbusConstants.CBUS_DSPD) {
            processdspd(false,m.getElement(1),m.getElement(2));
        }
        else if (opc==CbusConstants.CBUS_DKEEP) {
            // log.warn(" kick dkeep ");
            processdkeep(false,m.getElement(1));
        }
        else if (opc==CbusConstants.CBUS_KLOC) {
            processkloc(false,m.getElement(1));
        }
        else if (opc==CbusConstants.CBUS_GLOC) {
            int rcvdIntAddr = (m.getElement(1) & 0x3f) * 256 + m.getElement(2);
            boolean rcvdIsLong = (m.getElement(1) & 0xc0) != 0;
            processgloc(false,rcvdIntAddr,rcvdIsLong,m.getElement(3));
        }
        else if (opc==CbusConstants.CBUS_ERR) {
            processerr(false,m.getElement(1),m.getElement(2),m.getElement(3));
        }
        else if (opc==CbusConstants.CBUS_STMOD) {
            processstmod(false,m.getElement(1),m.getElement(2));
        }
        else if (opc==CbusConstants.CBUS_DFUN) {
            processdfun(false,m.getElement(1),m.getElement(2),m.getElement(3));
        }
        else if (opc==CbusConstants.CBUS_DFNON) {
            processdfnon(false,m.getElement(1),m.getElement(2),true);
        }
        else if (opc==CbusConstants.CBUS_DFNOF) {
            processdfnon(false,m.getElement(1),m.getElement(2),false); // same routine as DFNON
        }
        else if (opc==CbusConstants.CBUS_PCON) {
            processpcon(false,m.getElement(1),m.getElement(2));
        }
        else if (opc==CbusConstants.CBUS_KCON) {
            processpcon(false,m.getElement(1),0); // same routine as PCON
        }
        else if (opc==CbusConstants.CBUS_DFLG) {
            processdflg(false,m.getElement(1),m.getElement(2));
        }
        else if (opc==CbusConstants.CBUS_ESTOP) {
            processestop(false);
        }
        else if (opc==CbusConstants.CBUS_RTON) {
            processrton(false);
        }
        else if (opc==CbusConstants.CBUS_RTOF) {
            processrtof(false);
        }
        else if (opc==CbusConstants.CBUS_TON) {
            processton(false);
        }
        else if (opc==CbusConstants.CBUS_TOF) {
            processtof(false);
        }
    }

    /**
     * @param m canmessage
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
        int opc = CbusMessage.getOpcode(m);
        // log.warn(" opc {}",opc);
        // process is true as incoming message
        
        if (opc==CbusConstants.CBUS_STAT) {
            // todo more on this when finished tested v3 firmware with all opcs
            // for now, if a stat opc is received then it's v4
            // no stat received when < v4 Firmware
            cmndstat_fw = 4;
        }
        
        if (opc==CbusConstants.CBUS_PLOC) {
            int rcvdIntAddr = (m.getElement(2) & 0x3f) * 256 + m.getElement(3);
            boolean rcvdIsLong = (m.getElement(2) & 0xc0) != 0;
            processploc(true,m.getElement(1),rcvdIntAddr,rcvdIsLong,m.getElement(4),
            m.getElement(5),m.getElement(6),m.getElement(7));
        }
        else if (opc==CbusConstants.CBUS_RLOC) {
            int rcvdIntAddr = (m.getElement(1) & 0x3f) * 256 + m.getElement(2);
            boolean rcvdIsLong = (m.getElement(1) & 0xc0) != 0;
            processrloc(true,rcvdIntAddr,rcvdIsLong);
        }
        else if (opc==CbusConstants.CBUS_DSPD) {
            processdspd(true,m.getElement(1),m.getElement(2));
        }
        else if (opc==CbusConstants.CBUS_DKEEP) {
            processdkeep(true,m.getElement(1));
        }
        else if (opc==CbusConstants.CBUS_KLOC) {
            processkloc(true,m.getElement(1));
        }
        else if (opc==CbusConstants.CBUS_GLOC) {
            int rcvdIntAddr = (m.getElement(1) & 0x3f) * 256 + m.getElement(2);
            boolean rcvdIsLong = (m.getElement(1) & 0xc0) != 0;
            processgloc(true,rcvdIntAddr,rcvdIsLong,m.getElement(3));
        }
        else if (opc==CbusConstants.CBUS_ERR) {
            processerr(true,m.getElement(1),m.getElement(2),m.getElement(3));
        }
        else if (opc==CbusConstants.CBUS_STMOD) {
            processstmod(true,m.getElement(1),m.getElement(2));
        }        
        else if (opc==CbusConstants.CBUS_DFUN) {
            processdfun(true,m.getElement(1),m.getElement(2),m.getElement(3));
        }
        else if (opc==CbusConstants.CBUS_DFNON) {
            processdfnon(true,m.getElement(1),m.getElement(2),true);
        }
        else if (opc==CbusConstants.CBUS_DFNOF) {
            processdfnon(true,m.getElement(1),m.getElement(2),false);  // same routine as DFNON
        }
        else if (opc==CbusConstants.CBUS_PCON) {
            processpcon(true,m.getElement(1),m.getElement(2));
        }
        else if (opc==CbusConstants.CBUS_KCON) {
            processpcon(true,m.getElement(1),0); // same routine as PCON
        }
        else if (opc==CbusConstants.CBUS_DFLG) {
            processdflg(true,m.getElement(1),m.getElement(2));
        }
        else if (opc==CbusConstants.CBUS_ESTOP) {
            processestop(true);
        }
        else if (opc==CbusConstants.CBUS_RTON) {
            processrton(true);
        }
        else if (opc==CbusConstants.CBUS_RTOF) {
            processrtof(true);
        }
        else if (opc==CbusConstants.CBUS_TON) {
            processton(true);
        }
        else if (opc==CbusConstants.CBUS_TOF) {
            processtof(true);
        }
    }
    // ploc sent from a command station to a throttle
    private synchronized void processploc(boolean messagein, int session, int locoid, Boolean islong, int speeddir, int fa, int fb, int fc) {
        log.debug( Bundle.getMessage("CBUS_CMND_BR") + Bundle.getMessage("CNFO_PLOC",session,locoid));
        int row=gettablerow(locoid,islong);
        if (row < 0 ) {
            log.error("Invalid Row");
        } else {
            setValueAt(session, row, SESSION_ID_COLUMN);
            setValueAt(speeddir, row, LOCO_COMMANDED_SPEED_COLUMN);
            setValueAt(speeddir, row, LOCO_DIRECTION_COLUMN);
            processdfun( messagein, session, 1, fa);
            processdfun( messagein, session, 2, fb);
            processdfun( messagein, session, 3, fc);
            // int currthrottles = throttlecountarr.get(gettablerow(locoid))+1;
            // setValueAt(currthrottles, gettablerow(locoid), NUM_THROTTLES);
        }
    }
    
    // kloc sent from throttle to command station to release loco, which will continue at current speed
    private void processkloc(boolean messagein, int session) {
        int row=getrowfromsession(session);
        String messagedir;
        if (messagein){ // external throttle
            messagedir = Bundle.getMessage("CBUS_IN_CAB");
        } else { // jmri throttle
            messagedir = Bundle.getMessage("CBUS_OUT_CMD");
        }
        log.debug("{} {}",messagedir,Bundle.getMessage("CNFO_KLOC",session));
        if ( row > -1 ) {
            setValueAt(0, row, SESSION_ID_COLUMN); // Session restored by sending QLOC if v4 firmware
           // int currthrottles = throttlecountarr.get(row)-1;
           // setValueAt(currthrottles, row, NUM_THROTTLES);
            
            // version 4 fw maintains version number, so to check this request session details from command station
            // if this is sent with the v3 firmware then a popup error comes up from cbus throttlemanager when 
            // errStr is populated in the switch error clauses in canreply.
            // check if version 4
            if ( ( cmndstat_fw > 3 ) && ( speedarr.get(row) > 0 )) {
                log.debug("{} {}",Bundle.getMessage("CBUS_OUT_CMD"),Bundle.getMessage("QuerySession8a",session));
                CanMessage m = new CanMessage(tc.getCanid());
                m.setNumDataElements(2);
                CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
                m.setElement(0, CbusConstants.CBUS_QLOC);
                m.setElement(1, session);
                tc.sendCanMessage(m, null);
            }
        }        
    }    

    // rloc sent from throttle to command station to get loco
    private void processrloc(boolean messagein, int address, boolean islong) {
        int row = gettablerow(address,islong); // add to table if not already
        // log.debug ("processrloc row {}",row);
        String messagedir;
        if (messagein){ // external throttle
            messagedir = Bundle.getMessage("CBUS_IN_CAB");
        } else { // jmri throttle
            messagedir = Bundle.getMessage("CBUS_OUT_CMD");
        }
        log.debug("rloc {} {} {} {}",row, messagedir, Bundle.getMessage("CNFO_RLOC"),address);
    }
    
    // gloc sent from throttle to command station to get loco
    private void processgloc(boolean messagein, int address, Boolean islong, int flags) {
        int row = gettablerow(address,islong); // add to table if not already
        log.debug ("processgloc row {}",row);
        StringBuilder flagstring = new StringBuilder();
        if (messagein){ // external throttle
            flagstring.append(Bundle.getMessage("CBUS_IN_CAB"));
        } else { // jmri throttle
            flagstring.append(Bundle.getMessage("CBUS_OUT_CMD"));
        }

        boolean stealmode = ((flags >> 0 ) & 1) != 0;
        boolean sharemode = ((flags >> 1 ) & 1) != 0;
        // log.debug("stealmode {} sharemode {} ",stealmode,sharemode);
        if (stealmode){
            flagstring.append(Bundle.getMessage("CNFO_GLOC_ST") + address );
        }
        else if (sharemode){
            flagstring.append(Bundle.getMessage("CNFO_GLOC_SH") + address );
        }
        else {
            flagstring.append(Bundle.getMessage("CNFO_GLOC") + address );
        }
        addToLog(1,flagstring.toString());
    }
    
    // stmod sent from throttle to cmmnd station if speed steps not 128 / set service mode / sound mode
    private void processstmod(boolean messagein, int session, int flags) {
        int row=getrowfromsession(session);
        if ( row > -1 ) {
            String messagedir;
            if (messagein){ // external throttle
                messagedir=( Bundle.getMessage("CBUS_IN_CAB"));
            } else { // jmri throttle
                messagedir=( Bundle.getMessage("CBUS_OUT_CMD"));
            }            
            
            boolean sm0 = ((flags >> 0 ) & 1) != 0;
            boolean sm1 = ((flags >> 1 ) & 1) != 0;
            boolean servicemode = ((flags >> 2 ) & 1) != 0;
            boolean soundmode = ((flags >> 3 ) & 1) != 0;
            
            String speedstep="";
            if ((!sm0) && (!sm1)){
                speedstep="128";
            }
            else if ((!sm0) && (sm1)){
                speedstep="14";
            }        
            else if ((sm0) && (!sm1)){
                speedstep="28 Interleave";
            }        
            else if ((sm0) && (sm1)){
                speedstep="28";
            }
            log.debug("{} {}",messagedir,Bundle.getMessage("CNFO_STMOD",session,speedstep,servicemode,soundmode));
            setValueAt(speedstep, row, SPEED_STEP_COLUMN);
        }
    }

    // DKEEP sent as keepalive from throttle to command station 
    private void processdkeep(boolean messagein, int session) {
        int row=getrowfromsession(session);
        if ( row < 0 ) {
            log.debug("Requesting loco details for session {}.",session );
        }
    }
    
    // DSPD sent from throttle to command station , speed / direction
    private void processdspd(boolean messagein, int session, int speeddir) {
        // log.warn("processing dspd");
        int row=getrowfromsession(session);
        if ( row > -1 ) {
            setValueAt(speeddir, row, LOCO_COMMANDED_SPEED_COLUMN);
            setValueAt(speeddir, row, LOCO_DIRECTION_COLUMN);
        }
    }

    // DFLG sent from throttle to command station to notify engine change in flags
    void processdflg(boolean messagein, int session, int flags) {
        // log.debug("processing dflg session {} flag int {}",session,flags);
        int row=getrowfromsession(session);
        if ( row>-1 ) {
            StringBuilder buf = new StringBuilder();
            StringBuilder flagstring = new StringBuilder();
            if (messagein){ // external throttle
                buf.append( Bundle.getMessage("CBUS_IN_CAB"));
            } else { // jmri throttle
                buf.append( Bundle.getMessage("CBUS_OUT_CMD"));
            }
            
            boolean esa = ((flags >> 4 ) & 1) != 0; // bit4
            boolean esb = ((flags >> 5 ) & 1) != 0; // bit5
            flagstring.append("Engine State:");
            if ((!esa) && (!esb)){
                flagstring.append("Active");
            }
            else if ((!esa) && (esb)){
                flagstring.append("Consisted");
            }        
            else if ((esa) && (!esb)){
                flagstring.append("Consist master");
            }        
            else if ((esa) && (esb)){
                flagstring.append("Inactive");
            }            
            flagstring.append(" ");            
            flagstring.append("Lights:");
            flagstring.append(((flags >> 2 ) & 1)); // bit2
            flagstring.append(" ");
            flagstring.append("Rel Direction:");
            flagstring.append(((flags >> 3 ) & 1)); // bit3
            flagstring.append(" ");
            boolean sm0 = ((flags >> 0 ) & 1) != 0;
            boolean sm1 = ((flags >> 1 ) & 1) != 0;
            String speedstep="";
            if ((!sm0) && (!sm1)){
                speedstep="128";
            }
            else if ((!sm0) && (sm1)){
                speedstep="14";
            }        
            else if ((sm0) && (!sm1)){
                speedstep="28 Interleave";
            }        
            else if ((sm0) && (sm1)){
                speedstep="28";
            }
            _context = (buf.toString() + flagstring.toString());
            log.debug(_context);
            setValueAt(speedstep, row, SPEED_STEP_COLUMN);
            setValueAt((flagstring.toString()), row, FLAGS_COLUMN);
        }            
    }

    // DFNON Sent by a cab to turn on a specific loco function, alternative method to DFUN
    // also used to process function responses from DFNOF
    private void processdfnon(boolean messagein, int session, int function, boolean trueorfalse) {
        // log.warn("processing dfnon");
        int row=getrowfromsession(session);
        if ( row>-1 && function>-1 && function<29 ) {
            funcarray.get(row)[function] = trueorfalse;
            updatefunctionstr(row);
        }
    }    

    // DFUN Sent by a cab to trigger loco function
    // also used to process function responses from PLOC
    private void processdfun(boolean messagein, int session, int range, int functionbyte) {
        //  log.warn("processing dfun, session {} range {} functionbyte {}",session,range,functionbyte);
        int row=getrowfromsession(session);
        if ( row > -1 ) {
            if ( range == 1 ) {
                funcarray.get(row)[0] = ((functionbyte & CbusConstants.CBUS_F0) == CbusConstants.CBUS_F0);
                funcarray.get(row)[1] = ((functionbyte & CbusConstants.CBUS_F1) == CbusConstants.CBUS_F1);
                funcarray.get(row)[2] = ((functionbyte & CbusConstants.CBUS_F2) == CbusConstants.CBUS_F2);
                funcarray.get(row)[3] = ((functionbyte & CbusConstants.CBUS_F3) == CbusConstants.CBUS_F3);
                funcarray.get(row)[4] = ((functionbyte & CbusConstants.CBUS_F4) == CbusConstants.CBUS_F4);
            }
            else if ( range == 2 ) {
                funcarray.get(row)[5] = ((functionbyte & CbusConstants.CBUS_F5) == CbusConstants.CBUS_F5);
                funcarray.get(row)[6] = ((functionbyte & CbusConstants.CBUS_F6) == CbusConstants.CBUS_F6);
                funcarray.get(row)[7] = ((functionbyte & CbusConstants.CBUS_F7) == CbusConstants.CBUS_F7);
                funcarray.get(row)[8] = ((functionbyte & CbusConstants.CBUS_F8) == CbusConstants.CBUS_F8);
            }
            else if ( range == 3 ) {
                funcarray.get(row)[9] = ((functionbyte & CbusConstants.CBUS_F9) == CbusConstants.CBUS_F9);
                funcarray.get(row)[10] = ((functionbyte & CbusConstants.CBUS_F10) == CbusConstants.CBUS_F10);
                funcarray.get(row)[11] = ((functionbyte & CbusConstants.CBUS_F11) == CbusConstants.CBUS_F11);
                funcarray.get(row)[12] = ((functionbyte & CbusConstants.CBUS_F12) == CbusConstants.CBUS_F12);
            }
            else if ( range == 4 ) {
                funcarray.get(row)[13] = ((functionbyte & CbusConstants.CBUS_F13) == CbusConstants.CBUS_F13);
                funcarray.get(row)[14] = ((functionbyte & CbusConstants.CBUS_F14) == CbusConstants.CBUS_F14);
                funcarray.get(row)[15] = ((functionbyte & CbusConstants.CBUS_F15) == CbusConstants.CBUS_F15);
                funcarray.get(row)[16] = ((functionbyte & CbusConstants.CBUS_F16) == CbusConstants.CBUS_F16);
                funcarray.get(row)[17] = ((functionbyte & CbusConstants.CBUS_F17) == CbusConstants.CBUS_F17);
                funcarray.get(row)[18] = ((functionbyte & CbusConstants.CBUS_F18) == CbusConstants.CBUS_F18);
                funcarray.get(row)[19] = ((functionbyte & CbusConstants.CBUS_F19) == CbusConstants.CBUS_F19);
                funcarray.get(row)[20] = ((functionbyte & CbusConstants.CBUS_F20) == CbusConstants.CBUS_F20);
            }
            else if ( range == 5 ) {
                funcarray.get(row)[21] = ((functionbyte & CbusConstants.CBUS_F21) == CbusConstants.CBUS_F21);
                funcarray.get(row)[22] = ((functionbyte & CbusConstants.CBUS_F22) == CbusConstants.CBUS_F22);
                funcarray.get(row)[23] = ((functionbyte & CbusConstants.CBUS_F23) == CbusConstants.CBUS_F23);
                funcarray.get(row)[24] = ((functionbyte & CbusConstants.CBUS_F24) == CbusConstants.CBUS_F24);
                funcarray.get(row)[25] = ((functionbyte & CbusConstants.CBUS_F25) == CbusConstants.CBUS_F25);
                funcarray.get(row)[26] = ((functionbyte & CbusConstants.CBUS_F26) == CbusConstants.CBUS_F26);
                funcarray.get(row)[27] = ((functionbyte & CbusConstants.CBUS_F27) == CbusConstants.CBUS_F27);
                funcarray.get(row)[28] = ((functionbyte & CbusConstants.CBUS_F28) == CbusConstants.CBUS_F28);
            }
            updatefunctionstr(row);
        }
    }
    
    private void updatefunctionstr(int row){
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<29; i++) {
            //   log.warn(" i {} {} ",i,(funcarray.get(row)[i]) );
            if ((funcarray.get(row)[i] !=null ) && ( funcarray.get(row)[i] ==true )){
                buf.append(i);
                buf.append(" ");
            }
        }
        setValueAt(buf.toString(), row, FUNCTION_LIST);
    }
    
    // ERR sent by command station
    private void processerr(boolean messagein, int one, int two, int errnum) {
        // log.warn("processing err");
        int rcvdIntAddr = (one & 0x3f) * 256 + two;
        boolean rcvdIsLong = (one & 0xc0) != 0;        
        StringBuilder buf = new StringBuilder();
        if (messagein){ // external throttle
            buf.append( Bundle.getMessage("CBUS_CMND_BR"));
        } else { // jmri throttle
            buf.append( Bundle.getMessage("CBUS_OUT_CMD"));
        }
        
        switch (errnum) {
            case 1:
                buf.append(Bundle.getMessage("ERR_LOCO_STACK_FULL"));
                buf.append(rcvdIntAddr);
                break;
            case 2:
                buf.append(Bundle.getMessage("ERR_LOCO_ADDRESS_TAKEN"));
                buf.append(rcvdIntAddr);
                break;
            case 3:
                buf.append(Bundle.getMessage("ERR_SESSION_NOT_PRESENT"));
                buf.append(one);
                break;
            case 4:
                buf.append(Bundle.getMessage("ERR_CONSIST_EMPTY"));
                buf.append(one);
                break;
            case 5:
                buf.append(Bundle.getMessage("ERR_LOCO_NOT_FOUND"));
                buf.append(one);
                break;
            case 6:
                buf.append(Bundle.getMessage("ERR_CAN_BUS_ERROR"));
                break;
            case 7:
                buf.append(Bundle.getMessage("ERR_INVALID_REQUEST"));
                buf.append(rcvdIntAddr);
                break;
            case 8:
                buf.append(Bundle.getMessage("ERR_SESSION_CANCELLED"));
                buf.append(one);
                // cancel session number in table
                int row = gettablerow(rcvdIntAddr,rcvdIsLong);
                if ( row > -1 ) {
                    setValueAt(0, row, SESSION_ID_COLUMN);
                }
                break;
            default:
                break;
        }
        _context = buf.toString();
        addToLog(1,_context);
    }
    
    // PCON sent by throttle to add to consist
    // also used to process remove from consist KCON
    private void processpcon(boolean messagein, int session, int consist){
        log.debug("processing pcon");
        int row=getrowfromsession(session);
        if ( row>-1 ) {
            StringBuilder buf = new StringBuilder();
            if (messagein){ // external throttle
                buf.append( Bundle.getMessage("CBUS_IN_CAB"));
            } else { // jmri throttle
                buf.append( Bundle.getMessage("CBUS_OUT_CMD"));
            }
            int consistaddr = (consist & 0x7f);
            setValueAt(consistaddr, row, FUNCTION_LIST);
            buf.append( Bundle.getMessage("CNFO_PCON",session,consistaddr));
            if ((consist & 0x80) == 0x80){
                buf.append( Bundle.getMessage("FWD"));
            } else {
                buf.append( Bundle.getMessage("REV"));
            }
            log.debug("{}",buf.toString());
        }
    }
    
    private void processestop(boolean messagein){
        addToLog(1,"Command station acknowledges estop");
        estopTimer.stop();
        estopTimer=null;
    }
    
    private void processrton(boolean messagein){
        ActionListener tonTimeOut = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){           
                addToLog(1,(" Request track on No Response received from command station.")); 
            }
        };
        powerTimer = new Timer( 2000, tonTimeOut);
        powerTimer.setRepeats( false );
        powerTimer.start();  
    }
    
    private void processrtof(boolean messagein){
        ActionListener tofTimeOut = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){           
                addToLog(1,("Request track off No Response received from command station.")); 
            }
        };
        powerTimer = new Timer( 2000, tofTimeOut);
        powerTimer.setRepeats( false );
        powerTimer.start();  
    }
    
    private void processton(boolean messagein){
        powerTimer.stop();
        powerTimer=null;
        log.debug("Track on confirmed from command station.");
    }

    private void processtof(boolean messagein){
        powerTimer.stop();
        powerTimer=null;
        log.debug("Track off confirmed from command station.");
    }
    
    // should be moved to more generic place
    public void sendcbusestop(){
        log.info("Sending Command Station e-stop");
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RESTP);
        tc.sendCanMessage(m, null);
        
        // start a timer to monitor if timeout, ie if command station connected
        
        ActionListener estopTimeOut = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){           
                addToLog(1,("Send Estop No Response received from command station.")); 
            }
        };
        estopTimer = new Timer( 2000, estopTimeOut);
        estopTimer.setRepeats( false );
        estopTimer.start();
    }
    
    private void getcmdstatversion(){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RSTAT);
        tc.sendCanMessage(m, null);
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
        int locoaddr = locoidarr.get(row);
        
        if (locolongarr.get(row)) {
            locoaddr = locoaddr | 0xC000;
        }
        
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(7);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, 0xc2); // experimental cabdata opc
        m.setElement(1, (locoaddr / 256)); // addr hi
        m.setElement(2, locoaddr & 0xff);  // addr low
        m.setElement(3, 1); // datcode type
        m.setElement(4, ( cabsigint >> 8)); // aspect 1
        m.setElement(5, ( cabsigint >> 16)); // aspect 2
        m.setElement(6, ( cabsigint & 0xff)); // speed
        tc.sendCanMessage(m, null);
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
        int locoaddr = locoidarr.get(row);
        StringBuilder buf = new StringBuilder();
        buf.append("Cancelling Cabdata for loco " + locoaddr);
        addToLog(0,buf.toString());
        if (locolongarr.get(row)) {
            locoaddr = locoaddr | 0xC000;
        }
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(7);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, 0xc2); // experimental cabdata opc
        m.setElement(1, (locoaddr / 256)); // addr hi
        m.setElement(2, locoaddr & 0xff);  // addr low
        m.setElement(3, 1); // datcode type
        m.setElement(4, ( 0xff )); // aspect 1
        m.setElement(5, ( 0 )); // aspect 2
        m.setElement(6, ( 0xff )); // speed
        tc.sendCanMessage(m, null);
        
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
        
        if (tc != null) {
            tc.removeCanListener(this);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusSlotMonitorDataModel.class);
}
