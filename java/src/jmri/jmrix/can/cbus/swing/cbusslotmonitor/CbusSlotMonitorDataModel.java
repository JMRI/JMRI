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
import jmri.jmrit.display.layoutEditor.LayoutBlock;
// import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
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

    private ArrayList<Integer> cmndstatarr;
    private ArrayList<Integer> sessionidarr;
 //   private ArrayList<Integer> shortarr;
    private ArrayList<Integer> locoidarr;
    private ArrayList<Boolean> locolongarr;
    private ArrayList<String> namearr;
    private ArrayList<Integer> directionarr;
    private ArrayList<Integer> speedarr;
    private ArrayList<Integer> speedsteparr;
    private ArrayList<String> alttdarr;
    private ArrayList<String> currblockarr;
    
    private static List<PropertyChangeListener> mBlockListeners = new ArrayList<>();
    private static List<Block> mBlockList = new ArrayList<>();
    
    CanSystemConnectionMemo memo;
    TrafficController tc;
    
    // column order needs to match list in column tooltips
    static public final int CMND_STATION_ID_COLUMN = 99; 
    static public final int SESSION_ID_COLUMN = 0; 
    static public final int LOCO_ID_LONG_COLUMN = 2; 
    static public final int LOCO_ID_COLUMN = 1;
    static public final int LOCO_NAME_COLUMN = 98;
    static public final int LOCO_DIRECTION_COLUMN = 4;
    static public final int LOCO_COMMANDED_SPEED_COLUMN = 3;
    static public final int SPEED_STEP_COLUMN = 97;
    static public final int ALT_TD = 5;
    static public final int CURRENT_BLOCK = 6;
    static public final int MAX_COLUMN = 7;
    
    CbusSlotMonitorDataModel(CanSystemConnectionMemo memo, int row, int column) {
        
        cmndstatarr = new ArrayList<Integer>();
        sessionidarr = new ArrayList<Integer>();
        locoidarr = new ArrayList<Integer>();
        locolongarr = new ArrayList<Boolean>();
        namearr = new ArrayList<String>();
        directionarr = new ArrayList<Integer>();
        speedarr = new ArrayList<Integer>();
        speedsteparr = new ArrayList<Integer>();
        alttdarr = new ArrayList<String>();
        currblockarr = new ArrayList<String>();
        
        
        
        // connect to the CanInterface
        tc = memo.getTrafficController();
        tc.addCanListener(this);
        
        initblocks();
        
    }

    // order needs to match column list top of dtabledatamodel
    static protected final String[] columnToolTips = {
        ("tt c1"),("tt c2"),("tt c3"),("tt c4"),("tt c5"),("tt c6"),("tt c7"),("tt c8"),("tt c9"),("tt c10")

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
                return ("Long Add");
            case LOCO_NAME_COLUMN:
                return ("Name");
            case LOCO_DIRECTION_COLUMN:
                return ("Direction");
            case LOCO_COMMANDED_SPEED_COLUMN:
                return ("Commanded Speed");   
            case SPEED_STEP_COLUMN:
                return ("Steps");
            case ALT_TD:
                return("TD Alt");
            case CURRENT_BLOCK:
                return("Block");
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
                return new JTextField(6).getPreferredSize().width;
            case LOCO_NAME_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case LOCO_DIRECTION_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case LOCO_COMMANDED_SPEED_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case SPEED_STEP_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case ALT_TD:
                return new JTextField(4).getPreferredSize().width;
            case CURRENT_BLOCK:
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
            case SPEED_STEP_COLUMN:
                return Integer.class;
            case ALT_TD:
                return String.class;
            case CURRENT_BLOCK:
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
            //    if (locolongarr.get(row)) {
            //        return("L");
            //    } else {
            //        return("S");
            //    }
                return locolongarr.get(row);
            case LOCO_NAME_COLUMN:
                return namearr.get(row);
            case LOCO_DIRECTION_COLUMN:  // on or off event  1 is on, 0 is off, null unknown
                if ( speedarr.get(row) > -1 ) {
                    
                   // String  teststring = Integer.toBinaryString(speedarr.get(row));
                    
                    String speedflags = String.format("%8s", 
                    Integer.toBinaryString(speedarr.get(row) & 0xFF)).replace(' ', '0');
                    
                   // log.warn("value at 0 is {} {} ",speedflags, String.valueOf(speedflags.charAt(0)));
                    
                    if (Objects.equals("1",String.valueOf(speedflags.charAt(0)))) {
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
                    String speedflags = String.format("%8s", 
                    Integer.toBinaryString(speedarr.get(row) & 0xFF)).replace(' ', '0');                    
                    
                    int decimal = Integer.parseInt((speedflags.substring(1)), 2); 
                    return decimal;
                    
                } else {
                    return null;
                }
            case SPEED_STEP_COLUMN:
                if ( speedsteparr.get(row) > -1 ) {
                    return speedsteparr.get(row);
                } else {
                    return 128;
                }
            case ALT_TD:
                return alttdarr.get(row);
            case CURRENT_BLOCK:
                return currblockarr.get(row);
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
            speedarr.set(row, (Integer) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == SPEED_STEP_COLUMN) {
            speedsteparr.set(row, (Integer) value);
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
    }


    /**
     * Remove Row from table
     * @param row int row number
     */    
    void removeRow(int row) {
        // log.warn("322 delete row {} with max rows rowcount as {} ", row, _rowCount);

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
        Runnable r = new Notify(row, this);
        javax.swing.SwingUtilities.invokeLater(r);
        addToLog(3,_context);
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
     * @param m canmessage
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
        // log.debug("296 Received new message: {} getevent: {} ", m, CbusMessage.getEvent(m) );
        
        // see CbusThrottleManager
        int opc = CbusMessage.getOpcode(m);
        
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
            // log.warn(" kick dkeep ");
            processdkeep(true,m.getElement(1));
        }
        else if (opc==CbusConstants.CBUS_KLOC) {
            processkloc(true,m.getElement(1));
        }
    }

    /**
     * @param m canmessage
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
        
        int opc = CbusMessage.getOpcode(m);

       // log.warn(" opc {}",opc);
        
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
    }

    
    // return row number for a loco address, creates new row if no existing
    public int gettablerow(int locoid){
        for (int i = 0; i < getRowCount(); i++) {          
            if (locoid==locoidarr.get(i))  {
                return i;
            }
        }
        return createnewrow(locoid);
    }
    
    public int gettablerowfromblock(String blockval){
        for (int i = 0; i < getRowCount(); i++) {
            
            String altd = alttdarr.get(i);
            String locoidstr = locoidarr.get(i).toString();
            
            log.debug("blockval {} i {} {} {} ",blockval,i,altd,locoidstr);
            
            
            
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
    
    public int createnewrow(int locoid){
        
        locoidarr.add(locoid);
        locolongarr.add(null);
        cmndstatarr.add(-1);
        sessionidarr.add(-1);        
        namearr.add("");
        speedarr.add(-1);
        speedsteparr.add(-1);
        alttdarr.add("");
        currblockarr.add("");
        
        Runnable r = new Notify(getRowCount(), this);   // -1 in first arg means all
        javax.swing.SwingUtilities.invokeLater(r);
        
        return getRowCount();
    }
    
    // ploc sent from a command station to a throttle
    public void processploc(boolean messagein, int session, int locoid, Boolean islong, int speeddir, int fa, int fb, int fc) {
        _context = ("PLOC Session allocated for locoid  "+ session + " " + locoid);
        addToLog(1,_context); 
        int row=gettablerow(locoid);
        if (row < 0 ) {
            log.error("Invalid Row");
        }
        setValueAt(islong, gettablerow(locoid), LOCO_ID_LONG_COLUMN);
        setValueAt(session, gettablerow(locoid), SESSION_ID_COLUMN);
        setValueAt(speeddir, gettablerow(locoid), LOCO_COMMANDED_SPEED_COLUMN);
    }
    
    // kloc sent from throttle to command station to release loco, which will continue at current speed
    public void processkloc(boolean messagein, int session) {
        log.warn("processing kloc");
        _context = ("KLOC Throttle releasing session " + session );
        addToLog(1,_context);
        int row=getrowfromsession(session);
        if ( row > -1 ) {
            setValueAt(0, row, SESSION_ID_COLUMN);
        }        
    }    

    // rloc sent from throttle to command station to get loco, deprecated? use gloc
    public void processrloc(boolean messagein, int address, boolean islong) {
        int row = gettablerow(address);
        _context = ("RLOC Throttle requesting session for islong address  " + row + " " + islong + " " + address );
        addToLog(1,_context);
        locolongarr.set(gettablerow(address),islong);
    }
    
    // gloc sent from throttle to command station to get loco
    public void processgloc(boolean messagein, int high, int low, int flags) {
        log.warn("processing gloc");   
    }
    
    // stmod sent from throttle to cmmnd station if speed steps not 128 / set service mode / sound mode
    public void processstmod(boolean messagein, int high, int low, int flags) {
        log.warn("processing stmod");   
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
        log.warn("processing dspd");
        int row=getrowfromsession(session);
        if ( row > -1 ) {
            setValueAt(speeddir, row, LOCO_COMMANDED_SPEED_COLUMN);
        }
    }

    // STAT sent from command station in response to RSTAT
    public void processstat(boolean messagein, int high, int low, int flags) {
        log.warn("processing rstat");
    }
    
    // DFLG sent from throttle to command station to notify engine change in flags
    public void processdflg(boolean messagein, int high, int low, int flags) {
        log.warn("processing dflg");   
    }

    // DFNON Sent by a cab to turn on a specific loco function, alternative method to DFUN
    public void processdfnon(boolean messagein, int high, int low, int flags) {
        log.warn("processing dfnon");   
    }    
    
    // DFNON Sent by a cab to turn on a specific loco function, alternative method to DFUN
    public void processdfnof(boolean messagein, int high, int low, int flags) {
        log.warn("processing dfnon");   
    }

    // DFUN Sent by a cab to trigger loco function
    public void processdfun(boolean messagein, int high, int low, int flags) {
        log.warn("processing dfnon");   
    }
    
    // ERR sent by command station
    public void processerr(boolean messagein, int high, int low, int flags) {
        log.warn("processing dfnon");   
    }
    
    // RDCC3 RDCC4 RDCC5 RDCC6
    
    
    
    public void initblocks(){
        
      //  log.warn("initblocks");

        // List<String> blocks = blockManager.getSystemNameList();
        
        // Handle Blocks
        BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        Set<Block> blockSet = bmgr.getNamedBeanSet();
       // Object[][] blockTable = new Object[blockSet.size()][6];
      //  blockMap = new HashMap<>();
        int i = 0;
        for (Block b : blockSet) {
         //   log.warn("block found {}",i);
            mBlockList.add(b);
            final int index = i; 
            PropertyChangeListener listener = (PropertyChangeEvent e) -> {
         //       log.warn("pcl handle index {} {}",index,e);
                handleBlockChange(index,e);
            };
            b.addPropertyChangeListener(listener);
            mBlockListeners.add(listener);
            i++;
        }
        
      //  log.warn("finished initblocks");
        
    }
    
    
    /**
     * Handle tasks when block changes
     *
     * @param e propChgEvent
     */
    private void handleBlockChange(int index, PropertyChangeEvent e) {
            
        log.debug("block changed at index {} {}",index, e);
        
        if (e.getPropertyName().equals("value")) {
            Block b = mBlockList.get(index);
            Object val = b.getValue();
            
            if (val == null) {
                return; 
            }
            
            String blockname = b.getUserName();
            String strval = val.toString();
            
         //   log.warn("block {} value changed to {}",blockname,strval);
            
            int row = gettablerowfromblock(strval);
            if (row > -1 ) {
                setValueAt(blockname, row, CURRENT_BLOCK);
             //   getpathfromblock(b);
            }
        }
    }
    
    
  //  private void getpathfromblock(Block b){
      //  List<Block> currentPath = new ArrayList<>();
     //   log.warn("getting path for block {}",b);
        
      //  LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
      //  LayoutBlock lb = lbm.getLayoutBlock(b);
      //  SignalMast getFacingSignalMast = lbm.getFacingSignalMast(b,null,null);
        
     //   log.warn("path list {}",getFacingSignalMast);
   // }
    
    
    
    
    
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
