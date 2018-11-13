package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
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
 * Table data model for display of Cbus events
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
    
    private int cmndstat_fw =0;
    
    
    private ArrayList<Integer> cmndstatarr;
    private ArrayList<Integer> sessionidarr;
    private ArrayList<Integer> throttlecountarr;
    private ArrayList<String> functionarr;
    private ArrayList<Integer> locoidarr;
    private ArrayList<Boolean> locolongarr;
    private ArrayList<String> namearr;
    private ArrayList<Integer> directionarr;
    private ArrayList<Integer> speedarr;
    private ArrayList<String> speedsteparr;
    private ArrayList<String> alttdarr;
    private ArrayList<String> currblockarr;
    private ArrayList<String> blockdirarr;
    private ArrayList<Block> blockarr;
    private ArrayList<String> nextblockarr;
    private ArrayList<String> nextsignalarr;
    private ArrayList<String> nextaspectarr;
    private ArrayList<PropertyChangeListener> mBlockListeners;
    private ArrayList<SignalMast> sigListeners;
    private ArrayList<Block> mBlockList;
    
    CanSystemConnectionMemo memo;
    TrafficController tc;
    
    // column order needs to match list in column tooltips

    static public final int SESSION_ID_COLUMN = 0; 

    static public final int LOCO_ID_COLUMN = 1;
    static public final int LOCO_ID_LONG_COLUMN = 2; 
    static public final int LOCO_COMMANDED_SPEED_COLUMN = 3;    
    static public final int LOCO_DIRECTION_COLUMN = 4;
    static public final int SPEED_STEP_COLUMN = 5;
    static public final int NUM_THROTTLES = 97;
    static public final int FUNCTION_LIST = 96;
    static public final int ALT_TD = 6;
    static public final int CURRENT_BLOCK = 7;
    static public final int BLOCK_DIR = 8;
    static public final int NEXT_BLOCK = 9;
    static public final int NEXT_SIGNAL = 10;
    static public final int NEXT_ASPECT = 11;

    static public final int LOCO_NAME_COLUMN = 98;
    static public final int CMND_STATION_ID_COLUMN = 99;
    
    static public final int MAX_COLUMN = 12;
    
    CbusSlotMonitorDataModel(CanSystemConnectionMemo memo, int row, int column) {
        
        cmndstatarr = new ArrayList<Integer>();
        sessionidarr = new ArrayList<Integer>();
        throttlecountarr = new ArrayList<Integer>();
        functionarr = new ArrayList<String>();
        locoidarr = new ArrayList<Integer>();
        locolongarr = new ArrayList<Boolean>();
        namearr = new ArrayList<String>();
        directionarr = new ArrayList<Integer>();
        speedarr = new ArrayList<Integer>();
        speedsteparr = new ArrayList<String>();
        alttdarr = new ArrayList<String>();
        currblockarr = new ArrayList<String>();
        blockdirarr = new ArrayList<String>();
        blockarr = new ArrayList<Block>();
        nextblockarr = new ArrayList<String>();
        nextsignalarr = new ArrayList<String>();
        nextaspectarr = new ArrayList<String>();
        sigListeners = new ArrayList<SignalMast>();
        mBlockListeners = new ArrayList<PropertyChangeListener>();
        
        // connect to the CanInterface
        tc = memo.getTrafficController();
        tc.addCanListener(this);
        
        initblocks();
        
        getcmdstatversion();
    }

    // order needs to match column list top of dtabledatamodel
    static protected final String[] columnToolTips = {
        ("tt c1"),
        ("tt c2"),
        ("tt c3"),
        ("tt c4"),
        ("tt c5"),
        ("tt c6"),
        ("tt c7"),
        ("tt c8"),
        ("tt c9"),
        ("tt c10"),
        ("tt c11"),
        ("tt c12"),
        ("tt c13"),
        ("tt c14"),
        ("tt c15")

    }; // Length = number of items in array should (at least) match number of columns
    
    /**
     * Return the number of rows to be displayed.
     */
    @Override
    public int getRowCount() {
        return cmndstatarr.size();
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
            case LOCO_NAME_COLUMN:
                return ("Name");
            case LOCO_DIRECTION_COLUMN:
                return ("Direction");
            case LOCO_COMMANDED_SPEED_COLUMN:
                return ("Speed (Commanded)");   
            case SPEED_STEP_COLUMN:
                return ("Steps");
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
            case NEXT_BLOCK:
                return("Next Block");
            case NEXT_SIGNAL:
                return("Next Signal");
            case NEXT_ASPECT:
                return("Next Aspect");
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
            case LOCO_NAME_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case LOCO_DIRECTION_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case LOCO_COMMANDED_SPEED_COLUMN:
                return new JTextField(3).getPreferredSize().width;
            case SPEED_STEP_COLUMN:
                return new JTextField(3).getPreferredSize().width;
            case NUM_THROTTLES:
                return new JTextField(3).getPreferredSize().width;
            case FUNCTION_LIST:
                return new JTextField(6).getPreferredSize().width;
            case ALT_TD:
                return new JTextField(4).getPreferredSize().width;
            case CURRENT_BLOCK:
                return new JTextField(6).getPreferredSize().width;
            case BLOCK_DIR:
                return new JTextField(6).getPreferredSize().width;
            case NEXT_BLOCK:
                return new JTextField(6).getPreferredSize().width;
            case NEXT_SIGNAL:
                return new JTextField(6).getPreferredSize().width;
            case NEXT_ASPECT:
                return new JTextField(6).getPreferredSize().width;
            default:
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
            case LOCO_NAME_COLUMN:
                return String.class;
            case LOCO_DIRECTION_COLUMN:
                return String.class;
            case LOCO_COMMANDED_SPEED_COLUMN:
                return Integer.class;
            case NUM_THROTTLES:
                return Integer.class;
            case FUNCTION_LIST:
                return String.class;
            case SPEED_STEP_COLUMN:
                return String.class;
            case ALT_TD:
                return String.class;
            case CURRENT_BLOCK:
                return String.class;
            case BLOCK_DIR:
                return String.class;
            case NEXT_BLOCK:
                return String.class;
            case NEXT_SIGNAL:
                return String.class;
            case NEXT_ASPECT:
                return String.class;
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
        cmdStatTable.sizeColumnsToFit(-1);
    }

    /**
     * Return table values
     * @param row int row number
     * @param col int col number
     */
    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case CMND_STATION_ID_COLUMN:
                if ( cmndstatarr.get(row) > -1 ) {
                    return cmndstatarr.get(row);
                } else {
                    return null;
                }
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
            case LOCO_NAME_COLUMN:
                return namearr.get(row);
            case LOCO_DIRECTION_COLUMN:  // on or off event  1 is on, 0 is off, null unknown
                if ( directionarr.get(row) > -1 ) {
                    if ( directionarr.get(row) == 1 ) {
                        return("Forward");
                    } else {
                        return("Reverse");
                    }
                } else {
                    return "";
                }
            case LOCO_COMMANDED_SPEED_COLUMN:
                int speed=speedarr.get(row);
                if ( speed > -1 ) {
                    return speed;
                } else {
                    return null;
                }
                
            case NUM_THROTTLES:
                return throttlecountarr.get(row);
            case FUNCTION_LIST:
                return functionarr.get(row);
            case SPEED_STEP_COLUMN:
                if ( (speedsteparr.get(row)).isEmpty() ) {
                    return "128";
                }
                else {
                    return speedsteparr.get(row);
                } 
            case ALT_TD:
                return alttdarr.get(row);
            case CURRENT_BLOCK:
                return currblockarr.get(row);
            case BLOCK_DIR:
                return blockdirarr.get(row);
            case NEXT_BLOCK:
                return nextblockarr.get(row);
            case NEXT_SIGNAL:
                return nextsignalarr.get(row);
            case NEXT_ASPECT:
                return nextaspectarr.get(row);
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
            cmndstatarr.set(row, (Integer) value);
            Runnable r = new Notify(-1, this);
            javax.swing.SwingUtilities.invokeLater(r);
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
        else if (col == LOCO_NAME_COLUMN) {
            namearr.set(row, (String) value);
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
            
            int olddir = directionarr.get(row);
            int newdir = Integer.parseInt(String.valueOf(speedflags.charAt(0)));
            
            directionarr.set(row, newdir);
            if (olddir != newdir) {
                log.debug("direction changed");
                // potentially use in editing current block direction ?
            }
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == NUM_THROTTLES) {
            throttlecountarr.set(row, (Integer) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
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
            currblockarr.set(row, (String) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);            
        }
        else if (col == BLOCK_DIR) {
            blockdirarr.set(row, (String) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);            
        }
        else if (col == NEXT_BLOCK) {
            nextblockarr.set(row, (String) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);            
        }
        else if (col == NEXT_SIGNAL) {
            nextsignalarr.set(row, (String) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);            
        }
        else if (col == NEXT_ASPECT) {
            nextaspectarr.set(row, (String) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);            
        }
    }


    /**
     * Remove Row from table
     * @param row int row number
     */    
    void removeRow(int row) {

        _context = Bundle.getMessage("TableConfirmDelete");
        
        cmndstatarr.remove(row);
        sessionidarr.remove(row);
        locolongarr.remove(row);
        locoidarr.remove(row); 
        namearr.remove(row);
        directionarr.remove(row);
        speedarr.remove(row);
        speedsteparr.remove(row);
        alttdarr.remove(row);
        currblockarr.remove(row);
        blockdirarr.remove(row);
        nextblockarr.remove(row);
        nextsignalarr.remove(row);
        nextaspectarr.remove(row);
        sigListeners.remove(row);
        throttlecountarr.remove(row);
        functionarr.remove(row);
        
        Runnable r = new Notify(row, this);
        javax.swing.SwingUtilities.invokeLater(r);
        addToLog(3,_context);
    }
    
    public int createnewrow(int locoid, Boolean islong){
        // log.warn("createnewrow {}",locoid);
        locoidarr.add(locoid);
        locolongarr.add(islong);
        cmndstatarr.add(-1);
        sessionidarr.add(-1);        
        namearr.add("");
        directionarr.add(-1);
        speedarr.add(-1);
        speedsteparr.add("");
        alttdarr.add("");
        currblockarr.add("");
        blockdirarr.add("");
        nextblockarr.add("");
        blockarr.add(null);
        nextsignalarr.add(null);
        nextaspectarr.add(null);
        sigListeners.add(null);
        throttlecountarr.add(0);
        functionarr.add("");
        
        Runnable r = new Notify(getRowCount(), this);   // -1 in first arg means all
        javax.swing.SwingUtilities.invokeLater(r);
        
        return getRowCount();
    }
    
    // return row number for a loco address, creates new row if no existing
    public int gettablerow(int locoid, Boolean islong){
        for (int i = 0; i < getRowCount(); i++) {
            // log.warn("check {} for locoid {}",i,locoid);
            if (locoid==locoidarr.get(i))  {
                return i;
            }
        }
        return createnewrow(locoid,islong);
    }
    
    public int getrowfromblock(String blockval){
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
    
    public int getrowfromsession(int sessionid){
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
    }

    // ploc sent from a command station to a throttle
    public void processploc(boolean messagein, int session, int locoid, Boolean islong, int speeddir, int fa, int fb, int fc) {
        _context = ( Bundle.getMessage("CBUS_CMND_BR") + Bundle.getMessage("CNFO_PLOC",session) + locoid);
        addToLog(1,_context); 
        int row=gettablerow(locoid,islong);
        if (row < 0 ) {
            log.error("Invalid Row");
        } else {
            setValueAt(session, row, SESSION_ID_COLUMN);
            setValueAt(speeddir, row, LOCO_COMMANDED_SPEED_COLUMN);
            setValueAt(speeddir, row, LOCO_DIRECTION_COLUMN);
            // int currthrottles = throttlecountarr.get(gettablerow(locoid))+1;
            // setValueAt(currthrottles, gettablerow(locoid), NUM_THROTTLES);
        }
    }
    
    // kloc sent from throttle to command station to release loco, which will continue at current speed
    public void processkloc(boolean messagein, int session) {
        String messagedir;
        if (messagein){ // external throttle
            messagedir = Bundle.getMessage("CBUS_IN_CAB");
        } else { // jmri throttle
            messagedir = Bundle.getMessage("CBUS_OUT_CMD");
        }
        _context = (messagedir + Bundle.getMessage("CNFO_KLOC",session));
        addToLog(1,_context);
        int row=getrowfromsession(session);
        if ( row > -1 ) {
            setValueAt(0, row, SESSION_ID_COLUMN); // Session restored by sending QLOC if v4 firmware
           // int currthrottles = throttlecountarr.get(row)-1;
           // setValueAt(currthrottles, row, NUM_THROTTLES);
            
            // version 4 fw maintains version number, so to check this request session details from command station
            // if this is sent with the v3 firmware then a popup error comes up from cbus throttlemanager when 
            // errStr is populated in the switch error clauses in canreply.
            // check if version 4
            if ( ( cmndstat_fw > 3 ) && ( speedarr.get(row) > 0 )) {
                _context = (Bundle.getMessage("CBUS_OUT_CMD") + Bundle.getMessage("QuerySession8a",session));
                addToLog(1,_context);
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
    public void processrloc(boolean messagein, int address, boolean islong) {
        int row = gettablerow(address,islong); // add to table if not already
        if (log.isDebugEnabled()) {
            log.debug ("processrloc row {}",row);
        }
        String messagedir;
        if (messagein){ // external throttle
            messagedir = Bundle.getMessage("CBUS_IN_CAB");
        } else { // jmri throttle
            messagedir = Bundle.getMessage("CBUS_OUT_CMD");
        }
        _context = (messagedir + Bundle.getMessage("CNFO_RLOC") + address);
        addToLog(1,_context);
    }
    
    // gloc sent from throttle to command station to get loco
    public void processgloc(boolean messagein, int address, Boolean islong, int flags) {
        int row = gettablerow(address,islong); // add to table if not already
        if (log.isDebugEnabled()) {
            log.debug ("processgloc row {}",row);
        }
        String messagedir;
        if (messagein){ // external throttle
            messagedir = Bundle.getMessage("CBUS_IN_CAB");
        } else { // jmri throttle
            messagedir = Bundle.getMessage("CBUS_OUT_CMD");
        }

        boolean stealmode = ((flags >> 0 ) & 1) != 0;
        boolean sharemode = ((flags >> 1 ) & 1) != 0;
        // log.debug("stealmode {} sharemode {} ",stealmode,sharemode);
        if (stealmode){
            _context = (messagedir + Bundle.getMessage("CNFO_GLOC_ST") + address );
        }
        else if (sharemode){
            _context = (messagedir + Bundle.getMessage("CNFO_GLOC_SH") + address );
        }
        else {
            _context = (messagedir + Bundle.getMessage("CNFO_GLOC") + address );
        }
        addToLog(1,_context);
    }
    
    // stmod sent from throttle to cmmnd station if speed steps not 128 / set service mode / sound mode
    public void processstmod(boolean messagein, int session, int flags) {
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
            _context = (messagedir + Bundle.getMessage("CNFO_STMOD",session,speedstep,servicemode,soundmode));
            addToLog(1,_context);
            setValueAt(speedstep, row, SPEED_STEP_COLUMN);
        }
    }

    // DKEEP sent as keepalive from throttle to command station 
    public void processdkeep(boolean messagein, int session) {
        // log.warn("processing dkeep");
        int row=getrowfromsession(session);
        if ( row < 0 ) {
            _context = ("Requesting loco details for session " + session );
            addToLog(1,_context);
        }
    }
    
    // DSPD sent from throttle to command station , speed / direction
    public void processdspd(boolean messagein, int session, int speeddir) {
        // log.warn("processing dspd");
        int row=getrowfromsession(session);
        if ( row > -1 ) {
            setValueAt(speeddir, row, LOCO_COMMANDED_SPEED_COLUMN);
            setValueAt(speeddir, row, LOCO_DIRECTION_COLUMN);
        }
    }

    // DFLG sent from throttle to command station to notify engine change in flags
    public void processdflg(boolean messagein, int high, int low, int flags) {
        log.warn("processing dflg");   
    }

    // DFNON Sent by a cab to turn on a specific loco function, alternative method to DFUN
    public void processdfnon(boolean messagein, int high, int low, int flags) {
        log.warn("processing dfnon");   
    }    
    
    // DFNOF Sent by a cab to turn on a specific loco function, alternative method to DFUN
    public void processdfnof(boolean messagein, int high, int low, int flags) {
        log.warn("processing dfnof");   
    }

    // DFUN Sent by a cab to trigger loco function
    public void processdfun(boolean messagein, int high, int low, int flags) {
        log.warn("processing dfun");   
    }
    
    // ERR sent by command station
    public void processerr(boolean messagein, int one, int two, int errnum) {
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
    
    // RDCC3 RDCC4 RDCC5 RDCC6
    
    public void getcmdstatversion(){
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RSTAT);
        tc.sendCanMessage(m, null);
    }
    
    // Adds changelistener to blocks
    public void initblocks(){
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
        
        if ((e.getPropertyName().equals("state")) || (e.getPropertyName().equals("direction"))) {
            Object val = b.getValue();
            if (val == null) {
                return; 
            }
            String strval = val.toString();
            int row = getrowfromblock(strval);
            if (row > -1 ) {
                blockarr.set(row,b);
                String blockname = b.getUserName();
                String directionstr = Path.decodeDirection(b.getDirection());
                setValueAt(( directionstr ), row, BLOCK_DIR);
                setValueAt(( blockname ), row, CURRENT_BLOCK);
                updateblocksforrow(row);
            }
        }
    }
    
    void updateblocksforrow(int row){
        List<Block> routelist = new ArrayList<>();
        if (sigListeners.get(row) != null) {
            sigListeners.get(row).removePropertyChangeListener(_cconSignalMastListener);
        }
        sigListeners.set(row,null);
        
        Block b = blockarr.get(row);
        Block nB;
        SignalMast sm = null;
        LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        
        int dir = b.getDirection();
        int blockstep = 0;
        
        routelist.add(b);
        pFromDir[0] = dir;
        
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
            if ( sm == null) {
                setValueAt(( "" ), row, NEXT_SIGNAL);
                setValueAt(( "" ), row, NEXT_ASPECT);
            } else {
                setValueAt(( sm.getDisplayName() ), row, NEXT_SIGNAL);
                setValueAt(( sm.getAspect() ), row, NEXT_ASPECT);
                // add signal changelistener
                sm.addPropertyChangeListener(_cconSignalMastListener = (PropertyChangeEvent e) -> {
                    updateblocksforrow(row);
                });
                sigListeners.set(row,sm);
            }
        } else {
            // no direction
            setValueAt(( "" ), row, NEXT_SIGNAL);
            setValueAt(( "" ), row, NEXT_ASPECT);
            setValueAt("", row, NEXT_BLOCK);
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
                    _context = ("method 1 exiting with direction " + Path.decodeDirection(dirtoTest));
                    addToLog(1,_context);
                    return blockTest;
                }
                if (((fromdirection & dirftTest)) == 0) { // less reliable
                    pFromDir[(step+1)] = dirtoTest;
                    _context = ("method 2 exiting with direction " + Path.decodeDirection(dirtoTest));
                    addToLog(1,_context);
                    return blockTest;
                }
                if ((fromdirection != dirftTest)){ // least reliable but copes with 180 degrees 
                    pFromDir[(step+1)] = dirtoTest;
                    _context = ("method 3 exiting with direction " + Path.decodeDirection(dirtoTest));
                    addToLog(1,_context);
                    return blockTest;
                }
            }
        }
      return null;
    }
    
    /**
     * Add to Slot Monitor Console Log
     * @param cbuserror int
     * @param cbustext String console message
     */
    public static void addToLog(int cbuserror, String cbustext){
        final int senderror=cbuserror;
        final String sendtext= cbustext;
        CbusSlotMonitorPane.updateLogFromModel( senderror, sendtext );
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
        
        if (tc != null) {
            tc.removeCanListener(this);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusSlotMonitorDataModel.class);
}
