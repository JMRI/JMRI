package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.Date;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import java.util.List;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.xml.XMLUtil;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusLight;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.CbusSensor;
import jmri.jmrix.can.cbus.CbusTurnout;
import jmri.jmrix.can.TrafficController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Cbus events
 *
 * @author Andrew Crosland (C) 2009
 * @author Steve Young (c) 2018
 * @see CbusEventTablePane
 * 
 */
public class CbusEventTableDataModel extends javax.swing.table.AbstractTableModel implements CanListener {

    private File _saveFile = null;
    private String _saveFileName = null;
    private boolean _saved = false;
    private boolean sessionConfirmDeleteRow=true; // display confirm popup
    private int _defaultFeedback= 0;
    protected int _contype=0; // event table pane console message type
    protected String _context=null; // event table pane console text
    private int _defaultfeedbackdelay = 4000;
    static private int MAX_LINES = 500; // tablefeedback screen log size
    final JFileChooser fileChooser = new JFileChooser(FileUtil.getUserFilesPath());
    private ActionListener eventFeedbackListener;

    private ArrayList<Integer> eventarr;
    private ArrayList<Integer> nodearr;
    private ArrayList<Integer> canidarr;
    private ArrayList<Integer> typearr;
    private ArrayList<String> namearr;
    private ArrayList<String> nodenamearr;
    private ArrayList<String> commentarr;
    private ArrayList<Integer> sessiononarr;
    private ArrayList<Integer> sessionoffarr;
    private ArrayList<Integer> sessioninarr;
    private ArrayList<Integer> sessionoutarr;
    private ArrayList<Integer> feedbackreqdarr;    
    private ArrayList<Integer> feedbackoutstandingarr;    
    private ArrayList<Integer> feedbackeventarr;    
    private ArrayList<Integer> feedbacknodearr;
    private ArrayList<Integer> feedbacktimeoutarr;    
    private ArrayList<Date> latesttimestamparr;   
    private ArrayList<Integer> lfbarr;
    private ArrayList<Timer> mytimersarr;
    private ArrayList<String> stlonarr;
    private ArrayList<String> stloffarr;
    
    TextAreaFIFO tablefeedback;
    CanSystemConnectionMemo memo;
    TrafficController tc;
    
    // column order needs to match list in column tooltips
    static public final int EVENT_COLUMN = 0; 
    static public final int NODE_COLUMN = 1; 
    static public final int NAME_COLUMN = 2; 
    static public final int NODENAME_COLUMN = 3;
    static public final int COMMENT_COLUMN = 4;
    static public final int TYPE_COLUMN = 5;
    static public final int TOGGLE_BUTTON_COLUMN = 6;
    static public final int ON_BUTTON_COLUMN = 7; 
    static public final int OFF_BUTTON_COLUMN = 8; 
    static public final int CANID_COLUMN = 9;
    static public final int LATEST_TIMESTAMP_COLUMN = 10;
    static public final int STATUS_REQUEST_BUTTON_COLUMN = 11;
    static public final int SESSION_TOTAL_COLUMN = 12;
    static public final int SESSION_ON_COLUMN = 13;
    static public final int SESSION_OFF_COLUMN = 14;
    static public final int SESSION_IN_COLUMN = 15;
    static public final int SESSION_OUT_COLUMN = 16;
    static public final int DELETE_BUTTON_COLUMN = 17;
    static public final int LASTFEEDBACK_COLUMN = 18;
    static public final int FEEDBACKOUTSTANDING_COLUMN = 19;
    static public final int FEEDBACKREQUIRED_COLUMN = 20;
    static public final int FEEDBACKTIMEOUT_COLUMN = 21;
    static public final int FEEDBACKEVENT_COLUMN = 22;
    static public final int FEEDBACKNODE_COLUMN = 23;
    static public final int STLR_ON_COLUMN = 24;
    static public final int STLR_OFF_COLUMN = 25;
    static public final int STLR_ICON_COLUMN = 99;
    
    static public final int MAX_COLUMN = 26;
    
    // order + which columns to use when saving
    protected int[] saveColumns = {0,1,2,3,10,4}; // will need to change CVS file order if changed
    protected int[] whichPrintColumns = {0,1,2,3,4}; // no changes needed to other files if changed    

    CbusEventTableDataModel(CanSystemConnectionMemo memo, int row, int column) {
        
        eventarr = new ArrayList<Integer>();
        nodearr = new ArrayList<Integer>();
        canidarr = new ArrayList<Integer>();
        typearr = new ArrayList<Integer>();
        namearr = new ArrayList<String>();
        nodenamearr = new ArrayList<String>();
        commentarr = new ArrayList<String>();
        sessiononarr = new ArrayList<Integer>();
        sessionoffarr = new ArrayList<Integer>();
        sessioninarr = new ArrayList<Integer>();
        sessionoutarr = new ArrayList<Integer>();
        feedbackreqdarr = new ArrayList<Integer>();
        feedbackoutstandingarr = new ArrayList<Integer>();
        feedbackeventarr = new ArrayList<Integer>();
        feedbacknodearr = new ArrayList<Integer>();
        feedbacktimeoutarr = new ArrayList<Integer>();
        latesttimestamparr = new ArrayList<Date>();
        lfbarr = new ArrayList<Integer>();
        mytimersarr = new ArrayList<Timer>();
        stlonarr = new ArrayList<String>();
        stloffarr = new ArrayList<String>();
        tablefeedback = new TextAreaFIFO(MAX_LINES);
        
        // connect to the CanInterface
        tc = memo.getTrafficController();
        tc.addCanListener(this);
    }

    
    // order needs to match column list top of dtabledatamodel
    static protected final String[] columnToolTips = {
        Bundle.getMessage("EventColTip"),
        Bundle.getMessage("NodeColTip"),
        Bundle.getMessage("NameColTip"),
        Bundle.getMessage("CbusNodeNameTip"),
        Bundle.getMessage("CommentColTip"),
        Bundle.getMessage("TypeColTip"),
        Bundle.getMessage("SendToggleTip"),
        Bundle.getMessage("SendOntip"),
        Bundle.getMessage("SendOfftip"),
        Bundle.getMessage("IDColTip"),
        Bundle.getMessage("ColumnLastHeard"),
        Bundle.getMessage("ColumnRequestStatusTip"),
        Bundle.getMessage("ColumnTotalSession"),
        Bundle.getMessage("ColumnOnSession"),
        Bundle.getMessage("ColumnOffSession"),
        Bundle.getMessage("ColumnInSessionTip"),
        Bundle.getMessage("ColumnOutSessionTip"),
        Bundle.getMessage("ColumnEventDeleteTip"),
        Bundle.getMessage("FBLastTip"),        
        Bundle.getMessage("FBOutstandingTip"),
        Bundle.getMessage("FBNumTip"),
        Bundle.getMessage("FBTimeoutTip"),
        Bundle.getMessage("FBEventTip"),
        Bundle.getMessage("FBNodeTip"),
        Bundle.getMessage("StlrOnTip"),
        Bundle.getMessage("StlrOffTip")

    }; // Length = number of items in array should (at least) match number of columns
    
    /**
     * Return the number of rows to be displayed.
     */
    @Override
    public int getRowCount() {
        return eventarr.size();
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
            case CANID_COLUMN:
                return Bundle.getMessage("CanID");
            case NODE_COLUMN:
                return Bundle.getMessage("CbusNode");
            case NODENAME_COLUMN:
                return Bundle.getMessage("CbusNodeName");
            case NAME_COLUMN:
                return Bundle.getMessage("ColumnName");
            case EVENT_COLUMN:
                return Bundle.getMessage("CbusEvent");
            case TYPE_COLUMN:
                return Bundle.getMessage("CbusEventOnOrOff");
            case COMMENT_COLUMN:
                return Bundle.getMessage("ColumnComment");
            case DELETE_BUTTON_COLUMN:
                return Bundle.getMessage("ColumnEventDelete");
            case ON_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOn");
            case OFF_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOff");
            case TOGGLE_BUTTON_COLUMN:
                return Bundle.getMessage("ColumnToggle"); 
            case STATUS_REQUEST_BUTTON_COLUMN:
                return Bundle.getMessage("ColumnStatusRequest"); 
            case SESSION_ON_COLUMN:
                return Bundle.getMessage("ColumnOnSession"); 
            case SESSION_OFF_COLUMN:
                return Bundle.getMessage("ColumnOffSession");
            case SESSION_IN_COLUMN:
                return Bundle.getMessage("ColumnInSession"); 
            case SESSION_OUT_COLUMN:
                return Bundle.getMessage("ColumnOutSession");
            case SESSION_TOTAL_COLUMN:
                return Bundle.getMessage("ColumnTotalSession");
            case LATEST_TIMESTAMP_COLUMN:
                return Bundle.getMessage("ColumnLastHeard");
            case LASTFEEDBACK_COLUMN:
                return Bundle.getMessage("FBLast");                
            case FEEDBACKREQUIRED_COLUMN:
                return Bundle.getMessage("FBRequired");
            case FEEDBACKOUTSTANDING_COLUMN:
                return Bundle.getMessage("FBOutstanding");
            case FEEDBACKEVENT_COLUMN:
                return Bundle.getMessage("FBEvent");
            case FEEDBACKNODE_COLUMN:
                return Bundle.getMessage("FBNode");
            case FEEDBACKTIMEOUT_COLUMN:
                return Bundle.getMessage("FBTimeout");
            case STLR_ON_COLUMN:
                return Bundle.getMessage("JmriOnEv");
            case STLR_OFF_COLUMN:
                return Bundle.getMessage("JmriOffEv");
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
            case NODE_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case NODENAME_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case EVENT_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case TYPE_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case NAME_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case CANID_COLUMN:
                return new JTextField(4).getPreferredSize().width;
            case COMMENT_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case DELETE_BUTTON_COLUMN:
                return new JTextField(10).getPreferredSize().width;
            case ON_BUTTON_COLUMN:
            case OFF_BUTTON_COLUMN:               
            case TOGGLE_BUTTON_COLUMN:
                return new JTextField(8).getPreferredSize().width; 
            case STATUS_REQUEST_BUTTON_COLUMN:
                return new JTextField(9).getPreferredSize().width;
            case SESSION_ON_COLUMN:
            case SESSION_OFF_COLUMN:
            case SESSION_IN_COLUMN:
            case SESSION_OUT_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case SESSION_TOTAL_COLUMN:
                return new JTextField(7).getPreferredSize().width;
            case LATEST_TIMESTAMP_COLUMN:
                return new JTextField(7).getPreferredSize().width;
            case LASTFEEDBACK_COLUMN:
            case FEEDBACKREQUIRED_COLUMN:
            case FEEDBACKOUTSTANDING_COLUMN:
            case FEEDBACKTIMEOUT_COLUMN:
            case FEEDBACKNODE_COLUMN:
            case FEEDBACKEVENT_COLUMN:
                return new JTextField(5).getPreferredSize().width;
            case STLR_ON_COLUMN:
            case STLR_OFF_COLUMN:
                return new JTextField(20).getPreferredSize().width;
            default:
                return new JLabel(" <unknown> ").getPreferredSize().width; // NOI18N
        }
    }
    
    
    /**
     * Returns int of column width.
     * <p>
     * Just used for printing.
     * but also needed for buttons
     * in a test, there is 86 chars on a line
     * -1 is invalid
     * 0 is final column extend to end
     * </p>
     * @param col int col number
     */
    public int getColumnWidth(int col) {
        switch (col) {
            case CANID_COLUMN:
                return 5;
            case NODE_COLUMN:
                return 7;
            case NODENAME_COLUMN:
                return 9;
            case NAME_COLUMN:
                return 14;
            case EVENT_COLUMN:
                return 7;
            case TYPE_COLUMN: // on off
                return 8;
            case COMMENT_COLUMN:
                return 0; // 0 to get writer recognize it as the last column, will fill with spaces
            case DELETE_BUTTON_COLUMN:
                return 6;
            case ON_BUTTON_COLUMN:
                return 4;
            case OFF_BUTTON_COLUMN:
                return 4;
            case TOGGLE_BUTTON_COLUMN:
                return 4;
            case STATUS_REQUEST_BUTTON_COLUMN:
                return 4;
            case SESSION_ON_COLUMN:
                return 4;
            case SESSION_OFF_COLUMN:
                return 4;
            case SESSION_IN_COLUMN:
                return 4;
            case SESSION_OUT_COLUMN:
                return 4;
            case SESSION_TOTAL_COLUMN:
                return 4;
            case LATEST_TIMESTAMP_COLUMN:
                return 4;
            case FEEDBACKREQUIRED_COLUMN:
                return 4;
            case FEEDBACKOUTSTANDING_COLUMN:
                return 4;
            case STLR_ON_COLUMN:
                return 4;
            case STLR_OFF_COLUMN:
                return 4;
            default:
                return -1;
        }
    }
    
    /**
    * Returns column class type.
    */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case CANID_COLUMN:
                return Integer.class;
            case NODE_COLUMN:
                return Integer.class;
            case NODENAME_COLUMN:
                return String.class;
            case EVENT_COLUMN:
                return Integer.class;
            case TYPE_COLUMN:
                return Integer.class;
            case NAME_COLUMN:
                return String.class;
            case COMMENT_COLUMN:
                return String.class;
            case DELETE_BUTTON_COLUMN:
                return JButton.class;
            case ON_BUTTON_COLUMN:
                return JButton.class;
            case OFF_BUTTON_COLUMN:
                return JButton.class;
            case STATUS_REQUEST_BUTTON_COLUMN:
                return JButton.class;
            case TOGGLE_BUTTON_COLUMN:
                return JButton.class;
            case SESSION_ON_COLUMN:
                return Integer.class;
            case SESSION_OFF_COLUMN:
                return Integer.class;                
            case SESSION_IN_COLUMN:
                return Integer.class;
            case SESSION_OUT_COLUMN:
                return Integer.class; 
            case SESSION_TOTAL_COLUMN:
                return Integer.class;
            case LATEST_TIMESTAMP_COLUMN:
                return Date.class;
            case LASTFEEDBACK_COLUMN:
                return Integer.class;
            case FEEDBACKREQUIRED_COLUMN:
                return Integer.class;
            case FEEDBACKOUTSTANDING_COLUMN:
                return Integer.class;
            case FEEDBACKEVENT_COLUMN:
                return Integer.class;
            case FEEDBACKNODE_COLUMN:
                return Integer.class;
            case FEEDBACKTIMEOUT_COLUMN:
                return Integer.class;
            case STLR_ON_COLUMN:
                return String.class;
            case STLR_OFF_COLUMN:
                return String.class;
            default:
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
            case NAME_COLUMN:
            case COMMENT_COLUMN:
            case DELETE_BUTTON_COLUMN:
            case ON_BUTTON_COLUMN:
            case OFF_BUTTON_COLUMN:
            case TOGGLE_BUTTON_COLUMN:
            case STATUS_REQUEST_BUTTON_COLUMN:
            case FEEDBACKREQUIRED_COLUMN:
            case FEEDBACKEVENT_COLUMN:
            case FEEDBACKNODE_COLUMN:
            case FEEDBACKTIMEOUT_COLUMN:
            case FEEDBACKOUTSTANDING_COLUMN:
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
            case NODE_COLUMN:
                if ( nodearr.get(row) > 0 ) {
                    return nodearr.get(row);
                } else {
                    return null;
                }
            case EVENT_COLUMN:
                return eventarr.get(row);
            case NAME_COLUMN:
                return namearr.get(row);
            case NODENAME_COLUMN:
                return nodenamearr.get(row);
            case CANID_COLUMN:
                if (canidarr.get(row)>0) {
                    return canidarr.get(row);
                } else {
                    return null;
                }
            case TYPE_COLUMN:  // on or off event  1 is on, 0 is off, null unknown
                if (typearr.get(row)==1) { 
                    return Bundle.getMessage("CbusEventOff");
                } else if (typearr.get(row)==0) {
                    return Bundle.getMessage("CbusEventOn");
                } else {
                    return("");
                }
            case ON_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOn");
            case OFF_BUTTON_COLUMN:
                return Bundle.getMessage("CbusSendOff");
            case TOGGLE_BUTTON_COLUMN:  // on or off event  1 is on, 0 is off, null unknown
                if (typearr.get(row)==1) { 
                    return Bundle.getMessage("CbusSendOn");
                } else if (typearr.get(row)==0) {
                    return Bundle.getMessage("CbusSendOff");
                } else
                    return Bundle.getMessage("CbusSendOff");
            case STATUS_REQUEST_BUTTON_COLUMN:
                return Bundle.getMessage("StatusButton");
            case COMMENT_COLUMN:
                return commentarr.get(row);
            case DELETE_BUTTON_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            case SESSION_ON_COLUMN:
                if (sessiononarr.get(row)>0) {
                    return sessiononarr.get(row);
                } else {
                    return null;
                }
            case SESSION_OFF_COLUMN:
                if (sessionoffarr.get(row)>0) {
                    return sessionoffarr.get(row);
                } else {
                    return null;
                }
            case SESSION_IN_COLUMN:
                if (sessioninarr.get(row)>0) {
                    return sessioninarr.get(row);
                } else {
                    return null;
                }
            case SESSION_OUT_COLUMN:
                if (sessionoutarr.get(row)>0) {
                    return sessionoutarr.get(row);
                } else {
                    return null;
                }
            case SESSION_TOTAL_COLUMN:
                if ((sessiononarr.get(row) + sessionoffarr.get(row))>0) {
                    return (sessiononarr.get(row) + sessionoffarr.get(row));
                } else {
                    return null;
                }
            case LATEST_TIMESTAMP_COLUMN:
                return (latesttimestamparr.get(row)); // in Date format
            case LASTFEEDBACK_COLUMN:  // on or off event  1 is on, 0 is off, null unknown
                if (lfbarr.get(row)==1) {
                    return Bundle.getMessage("LfbFinding");
                } else if (lfbarr.get(row)==2) {
                    return Bundle.getMessage("LfbGood");
                } else if (lfbarr.get(row)==3) {
                    return Bundle.getMessage("LfbBad");
                } else {
                    return("");
                }
            case FEEDBACKREQUIRED_COLUMN:
                return feedbackreqdarr.get(row);
            case FEEDBACKOUTSTANDING_COLUMN:
                return feedbackoutstandingarr.get(row);
            case FEEDBACKEVENT_COLUMN:
                return feedbackeventarr.get(row);
            case FEEDBACKNODE_COLUMN:
                return feedbacknodearr.get(row);
            case FEEDBACKTIMEOUT_COLUMN:
                return feedbacktimeoutarr.get(row);
            case STLR_ON_COLUMN:
                return stlonarr.get(row);
            case STLR_OFF_COLUMN:
                return stloffarr.get(row);
            default:
                log.error("internal state inconsistent with table request for row {} col {}", row, col);
                return null;
        }
    }
    
    
    /**
     * Capture new comments or node names.
     * Button events
     * @param value object value
     * @param row int row number
     * @param col int col number
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        // log.debug("427 set valueat called row: {} col: {}", row, col);
        if (col == NAME_COLUMN) {
            namearr.set(row, (String) value);
            Runnable r = new Notify(-1, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }  else if (col == COMMENT_COLUMN) {        
            commentarr.set(row, (String) value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == NODENAME_COLUMN) {        
            nodenamearr.set(row, (String) value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == TYPE_COLUMN) {
            typearr.set(row, (Integer) value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == DELETE_BUTTON_COLUMN) {
            buttonDeleteClicked(row);
            Runnable r = new Notify(row, this);   // -1 in first arg means all
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == ON_BUTTON_COLUMN) {
            sendEventFromRow(row,1);
        }
        else if (col == OFF_BUTTON_COLUMN) {
            sendEventFromRow(row,0);
        }        
        else if (col == TOGGLE_BUTTON_COLUMN) {
            if ( typearr.get(row)==1) {
                sendEventFromRow(row,1);
            } else {
                sendEventFromRow(row,0);
            }
        }
        else if (col == STATUS_REQUEST_BUTTON_COLUMN) {
            sendEventFromRow(row,2);
        }
        else if (col == SESSION_ON_COLUMN) {
            sessiononarr.set(row, (sessiononarr.get(row)+1));
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
            }
        else if (col == SESSION_OFF_COLUMN) {
            sessionoffarr.set(row, (sessionoffarr.get(row)+1));
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == SESSION_IN_COLUMN) {
            sessioninarr.set(row, (sessioninarr.get(row)+1));
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == CANID_COLUMN) {
            canidarr.set(row, (Integer) value);
            Runnable r = new Notify(row, this); 
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == SESSION_OUT_COLUMN) {
            sessionoutarr.set(row, (sessionoutarr.get(row)+1));
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == LATEST_TIMESTAMP_COLUMN) {
            latesttimestamparr.set(row,new Date());
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == FEEDBACKREQUIRED_COLUMN) {
            int fbreqd = (int) value;
            if ((feedbackeventarr.get(row) > 0 )||(feedbacknodearr.get(row)>0)) { 
                fbreqd=0; 
            }
            feedbackreqdarr.set(row,fbreqd);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == FEEDBACKOUTSTANDING_COLUMN) {
            feedbackoutstandingarr.set(row, (Integer) value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == FEEDBACKEVENT_COLUMN) {
            int fbreqd = (int) value;
            if ((feedbacktimeoutarr.get(row)>0)||(feedbackreqdarr.get(row)>0)) {
                fbreqd=0; 
            }
            feedbackeventarr.set(row, fbreqd);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }        
        else if (col == FEEDBACKNODE_COLUMN) {
            int fbreqd = (int) value;
            if ((feedbacktimeoutarr.get(row)>0)||(feedbackreqdarr.get(row)>0)) {
                fbreqd=0; 
            }
            feedbacknodearr.set(row, fbreqd);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == FEEDBACKTIMEOUT_COLUMN) {
            int fbreqd = (int) value;
            if ((feedbackeventarr.get(row)>0)||(feedbacknodearr.get(row)>0)) { 
                fbreqd=0; 
            }
            feedbacktimeoutarr.set(row, fbreqd);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == LASTFEEDBACK_COLUMN) {
            lfbarr.set(row, (Integer) value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == STLR_ON_COLUMN) {
            stlonarr.set(row, (String) value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        else if (col == STLR_OFF_COLUMN) {
            stloffarr.set(row, (String) value);
            Runnable r = new Notify(row, this);
            javax.swing.SwingUtilities.invokeLater(r);
        }
        // table is dirty
        _saved = false;
    }

    public class Event {
        public String getEventName(int nn, int en) {
            StringBuilder buf = new StringBuilder(30);
            String eventname = namearr.get(eventRow(nn,en));
            // really this should be got from a node table
            String nodename = nodenamearr.get(eventRow(nn,en));
            
            if (eventname!=null) {
                buf.append( eventname + ", ");
            }
            // really this should be got from a node table            
            if (nodename!=null) {
                buf.append( nodename + ". ");
            }            
            
            buf.append(Bundle.getMessage("CbusEvent") + en);
            if (nn!=0) {
                buf.append(", " + Bundle.getMessage("CbusNode") + nn);
            }

            return buf.toString();
        }
        
        public int[] getMainEvent(int nn, int en) {
            int ar[] = new int[2];
            ar[0] = nn;
            ar[1] = en;
            if ( feedbackeventarr.get(eventRow(nn,en)) > 0 ) {
                // log.debug("match pair found ");
                ar[0] = feedbacknodearr.get(eventRow(nn,en));
                ar[1] = feedbackeventarr.get(eventRow(nn,en));
            }
            return ar; //returning two values at once
        }
        
        public synchronized int eventRow(int nn, int en) {
            for (int i = 0; i < getRowCount(); i++) {
                if ((en==eventarr.get(i)) && (nn == nodearr.get(i))) {
                    return i;
                }
            }
            return -1;
        }
        
        public boolean eventIsOnTable(int nn, int en) { 
            for (int i = 0; i < getRowCount(); i++) {
                if ((en==eventarr.get(i)) && (nn == nodearr.get(i))) {
                    return true;
                }
            }
            return false;
        }
        
        private void startTheTimer(int nn, int en) {
            // log.debug("startTheTimer starting timer for nn {} ev {}  ",nn,en);
            
            int delay = feedbacktimeoutarr.get(eventRow(nn,en));
            if (delay==0) {
                delay = _defaultfeedbackdelay;
            }
           
           eventFeedbackListener = new ActionListener(){
                @Override
                public void actionPerformed( ActionEvent e ){
                    setValueAt(0, eventRow(nn,en), FEEDBACKOUTSTANDING_COLUMN);
                    setValueAt(3, eventRow(nn,en), LASTFEEDBACK_COLUMN);
                    _context = Bundle.getMessage("FeedBackNotOK",getEventName(nn,en));
                    addToLog(3,_context);
                }
            };
            setValueAt(1, eventRow(nn,en), LASTFEEDBACK_COLUMN);
            mytimersarr.set(eventRow(nn,en), new Timer( delay, eventFeedbackListener));
            mytimersarr.get(eventRow(nn,en)).setRepeats( false );
            mytimersarr.get(eventRow(nn,en)).start();
           // log.debug("starting timer");
        }
        
        private synchronized void stopTheTimer(int nn, int en) {
            // log.debug("stopTheTimer stopping timer for nn {} ev {}",nn,en);
            try {
                mytimersarr.get(eventRow(nn,en)).stop();
                mytimersarr.set(eventRow(nn,en),null);
                eventFeedbackListener=null;
            } catch (NullPointerException e) {
                log.warn("Trouble stopping timer : Nullpointer {} ", e);
            }
            
            setValueAt(0, eventRow(nn,en), FEEDBACKOUTSTANDING_COLUMN);
            setValueAt(2, eventRow(nn,en), LASTFEEDBACK_COLUMN);
            addToLog(2,Bundle.getMessage("FeedBackOK",getEventName(nn,en)));
        }
    }

    
    /**
     * send events
     * @param row int row number
     * @param OfforOn 1 is on, 0 is off, 2 is either for request update
     * @since 4.13.3
     */
    public void sendEventFromRow(int row, int OfforOn){
        // log.debug( "313 send from row {} ", row);
        int nn, ev;
        nn = parseBinDecHexByte(String.valueOf(nodearr.get(row)), 65535, true, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SendEventNodeError"));
        if (nn == -1) {
            return;
        }
        ev = parseBinDecHexByte(String.valueOf(eventarr.get(row)), 65535, true, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SendEventInvalidError"));
        if (ev == -1) {
            return;
        }

        CanMessage m = new CanMessage(tc.getCanid());
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
    
        if (OfforOn==1) {
            if (nn > 0) {
                m.setElement(0, CbusConstants.CBUS_ACON);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASON);
            }
        } else if (OfforOn==0) {
            if (nn > 0) {
                m.setElement(0, CbusConstants.CBUS_ACOF);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASOF);
            }
        } else if (OfforOn==2) {
            if (nn > 0) {
                m.setElement(0, CbusConstants.CBUS_AREQ);
            } else {
                m.setElement(0, CbusConstants.CBUS_ASRQ);
            }
            // set listener to display response or not from response request
            // 10 seconds ?
        }
    
        m.setElement(1, nn >> 8);
        m.setElement(2, nn & 0xff);
        m.setElement(3, ev >> 8);
        m.setElement(4, ev & 0xff);
        m.setNumDataElements(5);
        message(m);
        tc.sendCanMessage(m, this);
    }

    /**
     * Delete Button Clicked
     * See whether to display confirm popup
     * @see removeRow
     * @param row int row number
     */
    public void buttonDeleteClicked(int row) {
        // log.debug("297 DELETE BUTTON CLICKED row {} confirm delete {} ", row, sessionConfirmDeleteRow);
        if (sessionConfirmDeleteRow) {
            // confirm deletion with the user
            JCheckBox checkbox = new JCheckBox(Bundle.getMessage("PopupSessionConfirmDel"));
            String message = Bundle.getMessage("DelConfirmOne") + "\n"   
            + Bundle.getMessage("DelConfirmTwo");
            Object[] params = {message, checkbox};
            
            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                    null, params, Bundle.getMessage("DelEvPopTitle"), 
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE)) {
                    boolean dontShow = checkbox.isSelected();
                    if (dontShow) {
                        sessionConfirmDeleteRow=false;
                    }
                    removeRow(row);
            }
        } else {
            // no need to show warning, just delete
            removeRow(row);
        }
    }
    
    /**
     * Remove Row from table
     * @see buttonDeleteClicked
     * @param row int row number
     */    
    synchronized void removeRow(int row) {
        // log.warn("322 delete row {} with max rows rowcount as {} ", row, _rowCount);
        Event deleteEv = new Event();
        _context = deleteEv.getEventName(nodearr.get(row), eventarr.get(row)) + " " + Bundle.getMessage("TableConfirmDelete");
        
        eventarr.remove(row);
        nodearr.remove(row);
        canidarr.remove(row);
        typearr.remove(row); 
        namearr.remove(row);
        nodenamearr.remove(row);
        sessiononarr.remove(row);
        sessionoffarr.remove(row);
        sessioninarr.remove(row);
        sessionoutarr.remove(row);
        commentarr.remove(row);
        feedbackreqdarr.remove(row);
        feedbackoutstandingarr.remove(row);
        feedbackeventarr.remove(row);
        feedbacknodearr.remove(row);
        feedbacktimeoutarr.remove(row);
        latesttimestamparr.remove(row);
        lfbarr.remove(row);
        mytimersarr.remove(row);
        stlonarr.remove(row);
        stloffarr.remove(row);
        
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
    public void configureTable(JTable eventTable) {
        // allow reordering of the columns
        eventTable.getTableHeader().setReorderingAllowed(true);

        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        eventTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // resize columns as requested
        for (int i = 0; i < eventTable.getColumnCount(); i++) {
            int width = getPreferredWidth(i);
            eventTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        eventTable.sizeColumnsToFit(-1);
        tablefeedback.setEditable ( false ); // set textArea non-editable
    }

    /**
     * Report whether table has changed.
     * @return boolean
     */
    public boolean isTableDirty() {
        return(_saved == false);
    }

    /**
     * Capture node and event, check if isevent and send to parse from message.
     * @param m canmessage
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
        // log.debug("296 Received new message: {} getevent: {} ", m, CbusMessage.getEvent(m) );
        
        int opc = CbusMessage.getOpcode(m);
        int nn = 0;
        if (!CbusOpCodes.isShortEvent(opc)) {
            nn = (m.getElement(1) * 256 + m.getElement(2));
        }
        int en = (m.getElement(3) * 256 + m.getElement(4));
        
        if (CbusOpCodes.isEventNotRequest(opc)) {
          //  log.warn("is event, not a request opc");
            int type = 1;
            if (CbusOpCodes.isOnEvent(opc)) {
                type=0;
            }
            parseMessage(
            CbusMessage.getId(m), 
            nn,
            en,
            type,
            0 );
            
            // check if feedback reqd
            processEvForFeedback(nn,en);
            
        }
        else if (CbusOpCodes.isEventRequest(opc)) {
            processEvRequest(nn,en);
        }
        else {
            return;
        }
    }
    
    
    /**
     * Capture node and event, check isevent and send to parse from reply.
     * @param m canmessage
     */
    @Override
    public void reply(CanReply m) { // incoming cbus message
        
        int opc = CbusMessage.getOpcode(m);
        int nn = 0;
        if (!CbusOpCodes.isShortEvent(opc)) {
            nn = (m.getElement(1) * 256 + m.getElement(2));
        }
        int en = (m.getElement(3) * 256 + m.getElement(4));
        
        if (CbusOpCodes.isEventNotRequest(opc)) {
           // log.warn("is event, not a request opc");
            int type = 1;
            if (CbusOpCodes.isOnEvent(opc)) {
                type=0;
            }
            parseMessage(
            CbusMessage.getId(m), 
            nn,
            en,
            type,
            1 );
            
            // check if feedback reqd
            processEvForFeedback(nn,en);
            
        } else if (CbusOpCodes.isEventRequest(opc)) {
            // log.debug("is an event request opc");
            processEvRequest(nn,en);
        }
    }


    public void processEvForFeedback( int nn, int en){
        int orignn = nn;
        int origen = en;
        boolean isfeedbackev=false;
        
        Event couldBeFeedBackEv = new Event();
        int mainEvent[] = couldBeFeedBackEv.getMainEvent(nn,en);
        nn = mainEvent[0];
        en = mainEvent[1];
        
        if (nn != orignn || en != origen) {
            isfeedbackev=true;
        }
        
        int existing=0;
        
        if (couldBeFeedBackEv.eventRow(nn,en)>=0) {
            existing = feedbackoutstandingarr.get(couldBeFeedBackEv.eventRow(nn,en));
        
            if ((existing == 0 ) && 
                (feedbackreqdarr.get(couldBeFeedBackEv.eventRow(nn,en)) > 0 ) && 
                (isfeedbackev==false) )  {
                // log.debug(" actual event, not the feedback response event! ");
                setValueAt(feedbackreqdarr.get(couldBeFeedBackEv.eventRow(nn,en)),
                couldBeFeedBackEv.eventRow(nn,en), 
                FEEDBACKOUTSTANDING_COLUMN);
                couldBeFeedBackEv.startTheTimer(nn,en);
            } 
            else if (existing==1) {
                setValueAt(0, couldBeFeedBackEv.eventRow(nn,en), FEEDBACKOUTSTANDING_COLUMN);
                couldBeFeedBackEv.stopTheTimer(nn,en);
            }
            else {
                setValueAt((existing-1), couldBeFeedBackEv.eventRow(nn,en), FEEDBACKOUTSTANDING_COLUMN);
            }
        }
    }
    

    public void processEvRequest( int nn, int en ) {
        Event couldBeFeedBackEv = new Event();
        
        int existingRow = seeIfEventOnTable( en, nn);
        // log.debug(" 339 existing event: {} ", existingRow);
        if (existingRow<0) {
            addEvent(en,nn,0,2,null,null,null,0,0,0,0); 
        }
        
        int existing = feedbackoutstandingarr.get(couldBeFeedBackEv.eventRow(nn,en));
        int fbr=feedbackreqdarr.get(couldBeFeedBackEv.eventRow(nn,en));
        // log.debug(" existing feedback outstanding is :{}: ",existing);
        
        if (fbr < 1) {
            fbr=1;
        }

        if (existing < 1 )  {
            setValueAt(
                fbr, 
                couldBeFeedBackEv.eventRow(nn,en), 
                FEEDBACKOUTSTANDING_COLUMN);
            couldBeFeedBackEv.startTheTimer(nn,en);
        }
    }
    
    
    /**
     * If new event add to table, else update table.
     * takes canid, node, event, onoroff
     * @since 4.13.3
     * @param canid of can message 
     * @param node of can message 
     * @param event of can message 
     * @param eventOnOrOff of can message 
     * @param inOrOut incoming or outgoing message
     */
    public void parseMessage( int canid, int node, int event, int eventOnOrOff, int inOrOut) {
        // log.debug("304 parseMessage  ");
        // log.debug(" 310 event: {}  node: {}  canid: {} ", event, node, canid);
        // log.debug(" 326 event onoroff 1 is off, 0 is on : {} ", eventOnOrOff);
        // inOrOut 1 Incoming message, 0 outgoing
        
        int existingRow = seeIfEventOnTable( event, node);
        // log.debug(" 339 existing event: {} ", existingRow);
        if (existingRow<0) {
            int on=0;
            int off=0;
            int in=0;
            int out=0;
            if (eventOnOrOff==0) {
                on=1;
            }
            if (eventOnOrOff==1) {
                off=1;
            }
            if (inOrOut==1) {
                in=1;
            }
            if (inOrOut==0) {
                out=1;
            }
            addEvent(event,node,canid,eventOnOrOff,null,null,null,on,off,in,out); // on off in out
        } else {
            updateEventfromNetwork(existingRow, eventOnOrOff, inOrOut, canid);
        } 
    }
    
    
    /**
     * Do Node + Event check, returns -1 if not on table, otherwise the row id
     * @since 4.13.3
     * @param event int
     * @param node int
     * @return int of row, otherwise -1
     */
    public int seeIfEventOnTable( int event, int node) {
        for (int i = 0; i < getRowCount(); i++) {
            if ((event==eventarr.get(i)) && (node == nodearr.get(i))) {
                return i;
            }
        }
        return -1;
    }
    
    
    /**
     * Attempts to add new event to table on button click
     * @since 4.13.3
     * @param ev event int
     * @param nd node int
     * @return int , -1 if existing row, 1 if created new ok
     */
    public synchronized int newEventFromButton(int ev, int nd){
        // int canid=tc.getCanid();
        // log.debug("new event button clicked event {} node {} canid {} ", ev, nd, canid);
        int existingRow = seeIfEventOnTable(ev,nd);
        if ( existingRow < 0 ) {
            addEvent(ev,nd,0,2,null,null,null,0,0,0,0);
            return 1;
        } else {    
            _context = Bundle.getMessage("CbusEvent") + ev + " " +
                Bundle.getMessage("CbusNode") + nd + Bundle.getMessage("AlreadyOnTable");
            addToLog(0,_context);
            return -1;  
        }
    }
    
    /**
     * Updates the table if event already on table
     * @param existingRow of event 
     * @param eventOnOrOff of can message
     * @param inOrOut incoming or outgoing cbus message
     * @param canId CAN ID of the message sender
     * @since 4.13.3
     */    
    public void updateEventfromNetwork( int existingRow, int eventOnOrOff, int inOrOut, int canId ) {
        // log.debug(" 393 updating row {} with status {} ", existingRow, eventOnOrOff);        
        setValueAt(eventOnOrOff, existingRow, TYPE_COLUMN);
        setValueAt(1, existingRow, LATEST_TIMESTAMP_COLUMN);
        setValueAt(canId, existingRow, CANID_COLUMN);
        if (eventOnOrOff==0) {  setValueAt(1, existingRow, SESSION_ON_COLUMN); }
        if (eventOnOrOff==1) { setValueAt(1, existingRow, SESSION_OFF_COLUMN); }
        if (inOrOut==1) {  setValueAt(1, existingRow, SESSION_IN_COLUMN); }
        if (inOrOut==0) { setValueAt(1, existingRow, SESSION_OUT_COLUMN); }

    }
    
    
    /**
     * Register new event to table
     */
    public synchronized void addEvent(int event, int node, int canid, int type, 
        String eventName, String nodeName, String evComment, int on, int off, int in, int out) {
        
        eventarr.add(event);
        nodearr.add(node);
        canidarr.add(canid); 

        typearr.add(type); 
        namearr.add(eventName);
        nodenamearr.add(nodeName);
        commentarr.add(evComment);
        
        sessiononarr.add(on);
        sessionoffarr.add(off);
        sessioninarr.add(in);
        sessionoutarr.add(out);
        
        feedbackreqdarr.add(_defaultFeedback);
        feedbackoutstandingarr.add(0);
        feedbackeventarr.add(0);
        feedbacknodearr.add(0);
        feedbacktimeoutarr.add(_defaultfeedbackdelay);
        stlonarr.add("");
        stloffarr.add("");
        
        lfbarr.add(0);
        mytimersarr.add(null);

        if ( ( in > 0 ) || ( out > 0 ) ) {
            latesttimestamparr.add( new Date());
        } else {
            latesttimestamparr.add(null);
        }
        
        StringBuilder addevbuf = new StringBuilder(50);
        
        addevbuf.append (Bundle.getMessage("CbusEvent"));
        addevbuf.append (" ");
        addevbuf.append (event);
        addevbuf.append (" ");
        
        if ( node > 0 ) {
            addevbuf.append (Bundle.getMessage("CbusNode"));
            addevbuf.append (" ");
            addevbuf.append (node);
            addevbuf.append (" ");
        }
        
        if (eventName!=null) {
            addevbuf.append (eventName);
            addevbuf.append (" ");
        }
        
        if (nodeName!=null) {
            addevbuf.append (nodeName);
            addevbuf.append (" ");
        }
        
        addevbuf.append (Bundle.getMessage("AddedToTable"));
        _context =addevbuf.toString();
        
        // notify the JTable object that a row has changed; do that in the Swing thread!
        Runnable r = new Notify(getRowCount(), this);   // -1 in first arg means all
        javax.swing.SwingUtilities.invokeLater(r);
        addToLog(1,_context);
    }
    
    
    public int getEventFromCan(CanMessage m){
        int en=0;
        int opc = CbusMessage.getOpcode(m);
        if (CbusOpCodes.isEvent(opc)) {
            en = (m.getElement(3) * 256 + m.getElement(4));
        }
        return en;
    }

    public int getNodeFromCan(CanMessage m){
        int nn=0;
        int opc = CbusMessage.getOpcode(m);
        if ((CbusOpCodes.isEvent(opc)) && (!CbusOpCodes.isShortEvent(opc))) {
            nn = (m.getElement(1) * 256 + m.getElement(2));
        }
        return nn;
    }
    
    private void updatejmricell(int row, Boolean ison, String name){
        String bb;
        if (ison) {
            bb = getValueAt(row, STLR_ON_COLUMN).toString();
            setValueAt(bb + " " + name, row, STLR_ON_COLUMN);
        }
        else {
            bb = getValueAt(row, STLR_OFF_COLUMN).toString();
            setValueAt(bb + " " + name, row, STLR_OFF_COLUMN);
        }
    }
    
    private void linkHwaddtoEvent(CanMessage m, String text ){
        int event = getEventFromCan(m);
        int node = getNodeFromCan(m);
        int opc = CbusMessage.getOpcode(m);
        int row = seeIfEventOnTable( event, node);
        if (row<0) {
            addEvent(event,node,0,2,null,null,null,0,0,0,0);
            row = seeIfEventOnTable( event, node);
        }
        updatejmricell(row, CbusOpCodes.isOnEvent(opc), text );
    }
    
    /**
     * Update all columns for JMRI Sensor, Turnout and light details
     */
    protected void updatejmricols(){
        log.debug("update jmri columns");
        // reset all columns
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt("", i, STLR_ON_COLUMN);
            setValueAt("", i, STLR_OFF_COLUMN);
        }
        jmri.SensorManager sm = InstanceManager.sensorManagerInstance();
        
        sm.getNamedBeanSet().forEach((nb) -> {
            CbusSensor cs;
            String userName = nb.getDisplayName();
            CanMessage m;
            String text;
            try {
                cs = (CbusSensor) sm.provideSensor(nb.toString());
                m = cs.getAddrActive();
                text = Bundle.getMessage("cbSensActive",userName);
                linkHwaddtoEvent( m, text );

                m = cs.getAddrInactive();
                text = Bundle.getMessage("cbSensInactive",userName);
                linkHwaddtoEvent( m, text );
            } catch(java.lang.ClassCastException cce){
                log.trace("not a cbus sensor");
            }
        });

        jmri.TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        tm.getNamedBeanSet().forEach((nb) -> {
            CbusTurnout ct;
            String userName = nb.getDisplayName();
            CanMessage m;
            String text;
            try {
                ct = (CbusTurnout) tm.provideTurnout(nb.toString());
                m = ct.getAddrThrown();
                text = Bundle.getMessage("cbTurnThrown",userName);
                linkHwaddtoEvent( m, text );

                m = ct.getAddrClosed();
                text = Bundle.getMessage("cbTurnClosed",userName);
                linkHwaddtoEvent( m, text );
            } catch(java.lang.ClassCastException cce){
                log.trace("not a cbus turnout");
            }
        });
        
        jmri.LightManager lm = InstanceManager.lightManagerInstance();
        lm.getNamedBeanSet().forEach((nb) -> {
            CbusLight cl;
            String userName = nb.getDisplayName();
            CanMessage m;
            String text;
            try {
                cl = (CbusLight) lm.provideLight(nb.toString());
                m = cl.getAddrOn();
                text = Bundle.getMessage("cbLightOn",userName);
                linkHwaddtoEvent( m, text );

                m = cl.getAddrOff();
                text = Bundle.getMessage("cbLightOff",userName);
                linkHwaddtoEvent( m, text );
            } catch(java.lang.ClassCastException cce){
                log.trace("not a cbus light");
            }
        });
    }

    /**
     * Add to Event Table Console Log
     * @param cbuserror int
     * @param cbustext String console message
     */
    public void addToLog(int cbuserror, String cbustext){
        if (cbuserror==3) {
          tablefeedback.append ("\n * * * * * * * * * * * * * * * * * * * * * * " + cbustext);
        } else {
          tablefeedback.append( "\n"+cbustext);
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
        
        if (tc != null) {
            tc.removeCanListener(this);
        }
        
    }

    /**
     * Import events from a MERG FCU XML File
     */
    public synchronized void readTheFCU14742File(final String filePath){

        _context = Bundle.getMessage("ImportStart") + filePath;
        addToLog(4,_context);
        
        if (filePath.length()<3) {
            _context = Bundle.getMessage("ImportNotXml");
            addToLog(0,_context);
            return;
        }        
        
        
        if (!filePath.toUpperCase().endsWith("XML")) {
            _context = Bundle.getMessage("ImportNotXml");
            addToLog(3,_context);
            return;
        }
        
        
        try {
            File inputFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("userEvents");
            int addedtotable=0;
            int alreadyontable=0;
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                Element eElement = (Element) nNode;
                
                String eventValue = eElement.getElementsByTagName("eventValue").item(0).getTextContent();
                String eventNode = eElement.getElementsByTagName("eventNode").item(0).getTextContent();
                String nodeName = eElement.getElementsByTagName("nodeName").item(0).getTextContent();
                String eventName = eElement.getElementsByTagName("eventName").item(0).getTextContent();
                
                int eventnum = Integer.parseInt(eventValue);
                int nodenum = Integer.parseInt(eventNode);

                //    log.warn(" 1173 eventnum is {} nodenum is {} ",eventnum, nodenum);
                int existingRow = seeIfEventOnTable(eventnum,nodenum);
                if ( existingRow < 0 ) {
                    addEvent(eventnum,nodenum,0,2,eventName,nodeName,null,0,0,0,0);
                    addedtotable++;
                } else {
                    
                    // update event name if null
                    // update event node name if null ( which should really be got from a future node table )
                    StringBuilder addbuf = new StringBuilder(50);
                    
                    addbuf.append (Bundle.getMessage("CbusEvent"));
                    addbuf.append (" ");                    
                    addbuf.append (eventValue);
                    addbuf.append (" ");                    
                    addbuf.append (Bundle.getMessage("CbusNode"));
                    addbuf.append (" ");  
                    addbuf.append (eventNode);
                    addbuf.append (" ");
                    addbuf.append (Bundle.getMessage("AlreadyOnTable"));
                    
                    if (namearr.get(existingRow)==null) {
                            setValueAt(eventName, existingRow, NAME_COLUMN);
                            addbuf.append (Bundle.getMessage("EventNameAdded", eventName));
                    } else {
                        addbuf.append (" ");
                        addbuf.append (namearr.get(existingRow));
                    }
                    
                    if (nodenamearr.get(existingRow)==null) {
                            setValueAt(nodeName, existingRow, NODENAME_COLUMN);
                            addbuf.append (Bundle.getMessage("NodeNameAdded", nodeName));
                    } else {
                        addbuf.append (" ");
                        addbuf.append (nodenamearr.get(existingRow));
                    }
                    
                    _context = addbuf.toString();
                    addToLog(0,_context);
                    alreadyontable++;
                }
            }
            
            _context = "---------------------------------------------------------- \n" + 
            Bundle.getMessage("ImportComplete") + " \n" + 
            String.valueOf(nList.getLength()) + " " + Bundle.getMessage("CbusEvents") + " " + Bundle.getMessage("ImportFound") + " \n" +
            String.valueOf(alreadyontable) + " " + Bundle.getMessage("AlreadyOnTable") + " \n" +
            String.valueOf(addedtotable) + " " + Bundle.getMessage("CbusEvents") + " " + Bundle.getMessage("AddedToTable");
            addToLog(2,_context); 
            
        } 
        
        catch (RuntimeException e) {
            log.warn(" 1423 Error importing xml file.  {} ", e);
            _context = Bundle.getMessage("ImportError");
            addToLog(3,_context); 
        } 
        
        catch (Exception e) {
            log.warn(" 1429 Error importing xml file. Valid xml? {} ", e);
            _context = Bundle.getMessage("ImportError");
            addToLog(3,_context); 
        }
    }
    
    /**
     * Self save as a .csv file.
     */
    public void saveTable() {
        // check for empty table
        if (this.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("EmptyTableDialogString"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (_saveFile == null) {
            saveAsTable();
        } else {
            saveToCSV();
        }
    }

    /**
     * Self save as a .csv file, first prompting for a filename.
     */
    public void saveAsTable() {
        // start at current file, show dialog
        
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
        "Comma Seperated Value", "csv");
        fileChooser.setFileFilter(filter);
        
        fileChooser.setSelectedFile(new File("myevents.csv"));
        int retVal = fileChooser.showSaveDialog(null);
        
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            _saveFileName = fileChooser.getSelectedFile().getPath();

            if (!_saveFileName .endsWith(".csv")) {
                _saveFileName += ".csv";
            }

            _saveFile = new File(_saveFileName);
            if (_saveFile.isFile()) {
                int response = JOptionPane.showConfirmDialog(null, //
                        Bundle.getMessage("ConfirmOverwriteFile"), //
                        Bundle.getMessage("ConfirmQuestion"), JOptionPane.YES_NO_OPTION, //
                        JOptionPane.QUESTION_MESSAGE);
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            // log.debug("File chosen: {}", _saveFileName);
        } else {
            return; // cancelled or pane closed, prevent NPE
        }
        saveToCSV();
    }


    protected void saveToCSV() {
        FileOutputStream out = null;
        PrintWriter p = null;
        if (_saveFileName == null) {
            log.error("saveToCSV: No file name available. Aborted");
            return;
        }
        try {
            // Create a print writer based on the file, so we can print to it.
            out = new FileOutputStream(_saveFileName);
            p = new PrintWriter(out, true);
        } catch (IOException e) {
                log.error("Problem creating output stream");
        }

        if (out == null) {
            log.error("Null File Output Stream");
        }
        if (p == null) { // certainly null if out == null
            log.error("Null Print Writer");
            return;
        }

        // Save table per row. We've checked for an empty table in SaveTable()
        // print header labels
        
        
        for (int i = 0; i < saveColumns.length; i++) {
            // log.debug("save column array column {}", Savecolumns[i]);
            p.print(this.getColumnName(saveColumns[i]));
            
            // last column, without comma
            if (i!=saveColumns.length-1) {
                p.print(",");
            }
        }
        
        p.println("");

        // print rows
        for (int i = 0; i < this.getRowCount(); i++) {
            p.print(canidarr.get(i));
            p.print(",");
            p.print(eventarr.get(i));
            p.print(",");
            p.print(nodearr.get(i));
            p.print(",");
            p.print(typearr.get(i));
            p.print(",");
            p.print(latesttimestamparr.get(i)); // Date format
            
            p.print(",");
            
            if (namearr.get(i) == null) {
                p.print("");
            } else {
                p.print('"' + namearr.get(i) + '"');
            }
            p.print(",");
            if (commentarr.get(i) == null) {
                p.print("");
            } else {
                p.print('"'+ commentarr.get(i) + '"');
            }
            p.println("");
        }

        try {
            p.flush();
            p.close();
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            log.error("879 IO Exception");
        }
        // mark that the table has been saved
        _saved = true; // TODO disable the Save menu item in CbusEventTablePane
    }

    
    // move this to global cbus for sending events from other swing

    /**
     * Parse a string for binary, decimal or hex byte value
     * <P>
     * 0b, 0d or 0x prefix will force parsing of binary, decimal or hex,
     * respectively. Entries with no prefix are parsed as decimal if decimal
     * flag is true, otherwise hex.
     *
     * copied from original version at CbusConsolePane.java
     *
     * @param s        string to be parsed
     * @param limit    upper bound of value to be parsed
     * @param decimal  flag for decimal or hex default
     * @param errTitle Title of error dialogue box if Number FormatException
     *                 encountered
     * @param errMsg   Message to be displayed if Number FormatException
     *                 encountered
     * @return the byte value, -1 indicates failure
     */
    public int parseBinDecHexByte(String s, int limit, boolean decimal, String errTitle, String errMsg) {
        int data = -1;
        boolean error = false;
        int radix = 16;

        if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0x")) {
            // hex, remove the prefix
            s = s.substring(2);
            radix = 16;
        } else if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0d")) {
            // decimal, remove the prefix
            s = s.substring(2);
            radix = 10;
        } else if ((s.length() > 3) && s.substring(0, 2).equalsIgnoreCase("0b")) {
            // binary, remove the prefix
            s = s.substring(2);
            radix = 2;
        } else if (decimal) {
            radix = 10;
        }

        try {
            data = Integer.parseInt(s, radix);
        } catch (NumberFormatException ex) {
            error = true;
        }
        if ((data < 0) || (data > limit)) {
            error = true;
        }
        if (error) {
            JOptionPane.showMessageDialog(null, errMsg,
                    errTitle, JOptionPane.ERROR_MESSAGE);
            data = -1;
        }
        return data;
    }

    protected TextAreaFIFO tablefeedback(){
        return tablefeedback;
    }    
    
    /**
     * Keeps the message log windows to a reasonable length
     * https://community.oracle.com/thread/1373400
     */
    protected static class TextAreaFIFO extends JTextArea implements DocumentListener {
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
            javax.swing.text.Element root = getDocument().getDefaultRootElement();
            while (root.getElementCount() > maxLines) {
                javax.swing.text.Element firstLine = root.getElement(0);
                try {
                    getDocument().remove(0, firstLine.getEndOffset());
                } catch(BadLocationException ble) {
                    System.out.println(ble);
                }
            }
        setCaretPosition( getDocument().getLength() );
        }
    }
    
    /**
     * Self print or print preview the table.
     * <p>
     * Copied from BeanTableDataModel modified to print variable column widths.
     * Final column with size zero runs to extent of page width.
     *
     * Printed with headings and vertical lines between each column. Data is
     * word wrapped within a column. Can handle data as strings, integers,
     * comboboxes or booleans
     * </p>
     */
    public void printTable(HardcopyWriter w) {
        // [AC] variable column sizes
        int columnTotal = 0;
        
        // log.debug("save column array column {}", saveColumns[i]);
        
        int[] columnWidth = new int[this.whichPrintColumns.length];
        // in a test, thats 86 chars on a line
        for (int i = 0; i < this.whichPrintColumns.length; i++) {
            
            int columnworkedon=whichPrintColumns[i];
            // log.debug(" 1016 print column i {} worked on is {} has width {} ",i, columnworkedon, this.getColumnWidth(columnworkedon));
            
            if (this.getColumnWidth(columnworkedon) == 0) {
                // Fill to end of line
                columnWidth[i] = w.getCharactersPerLine() - columnTotal;
            } else {
                columnWidth[i] = this.getColumnWidth(columnworkedon);
                columnTotal = columnTotal + columnWidth[i] + 1;
            }
            // log.debug(" 1027 print column i {} columnTotal {} has width {} ",i, columnTotal, columnWidth[i]);
        }

        // Draw horizontal dividing line
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                w.getCharactersPerLine());
        
        // print the column header labels
        String[] columnStrings = new String[this.whichPrintColumns.length];
        // Put each column header in the array
        for (int i = 0; i < this.whichPrintColumns.length; i++) {
            int columnworkedon=whichPrintColumns[i];
            columnStrings[i] = this.getColumnName(columnworkedon);
            // log.debug(" 1047 print column i {} has columnStrings {} getColumnName {} ", i, columnStrings[i], this.getColumnName(columnworkedon));
        }
        
        w.setFontStyle(Font.BOLD);
        printColumns(w, columnStrings, columnWidth);
        w.setFontStyle(0);
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                w.getCharactersPerLine());

        // now print each row of data
        // create a base string the width of the column
        for (int i = 0; i < this.getRowCount(); i++) {
            StringBuffer buf = new StringBuffer();
            // log.debug (" 1070 row i  {} ", i);
            
            for (int k = 0; k < whichPrintColumns.length; k++) {
                
                int j=whichPrintColumns[k];
                // log.debug("1076 i is {} j is {} ", i, j);
                
                //check for special, non string contents
                if (this.getValueAt(i, j) == null) {
                    columnStrings[k] = buf.toString();
                } else if (this.getValueAt(i, j) instanceof JComboBox) {
                    // columnStrings[j] = (String) ((JComboBox<String>) this.getValueAt(i, j)).getSelectedItem();
                    columnStrings[k]=null;
                } else if (this.getValueAt(i, j) instanceof Date) {
                    columnStrings[k] = (this.getValueAt(i, j)).toString();
                    } else if (this.getValueAt(i, j) instanceof Boolean) {
                    columnStrings[k] = (this.getValueAt(i, j)).toString();
                } else if (this.getValueAt(i, j) instanceof Integer) {
                    columnStrings[k] = (this.getValueAt(i, j)).toString();
                } else {
                    columnStrings[k] = (String) this.getValueAt(i, j);
                }
                // log.debug("588 columnStrings :{}:", columnStrings[j]);
            }
            
            printColumns(w, columnStrings, columnWidth);
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                    w.getCharactersPerLine());
        }
        w.close();
    }

    // [AC] modified to take an array of column widths
    protected void printColumns(HardcopyWriter w, String columnStrings[], int columnWidth[]) {
        String columnString = "";
        String lineString = "";
        String spaces;
        // loop through each column
        boolean complete = false;
        while (!complete) {
            complete = true;
            for (int i = 0; i < columnStrings.length; i++) {
                // create a base string the width of the column
                StringBuffer buf = new StringBuffer();
                for (int j = 0; j < columnWidth[i]; j++) {
                    buf.append(" ");
                }
                spaces = buf.toString();
                // if the column string is too wide, cut it at word boundary (valid delimiters are space, - and _)
                // Use the intial part of the text, pad it with spaces and place the remainder back in the array
                // for further processing on next line.
                // If column string isn't too wide, pad it to column width with spaces if needed
                if (columnStrings[i].length() > columnWidth[i]) {
                    boolean noWord = true;
                    for (int k = columnWidth[i]; k >= 1; k--) {
                        if (columnStrings[i].substring(k - 1, k).equals(" ")
                                || columnStrings[i].substring(k - 1, k).equals("-")
                                || columnStrings[i].substring(k - 1, k).equals("_")) {
                            columnString = columnStrings[i].substring(0, k)
                                    + spaces.substring(k);
                            columnStrings[i] = columnStrings[i].substring(k);
                            noWord = false;
                            complete = false;
                            break;
                        }
                        // log.debug("1050 columnString {}",columnString);
                    }
                    
                    // log.debug("1053 noword is {} ",noWord);
                    if (noWord) { // not breakable, hard break
                        columnString = columnStrings[i].substring(0, columnWidth[i]);
                        columnStrings[i] = columnStrings[i].substring(columnWidth[i]);
                        complete = false;
                    }
                } else {
                    columnString = columnStrings[i] + spaces.substring(columnStrings[i].length()); // pad with spaces
                    columnStrings[i] = "";
                }
                lineString = lineString + columnString + " ";
            }
            try {
                w.write(lineString);
                //write vertical dividing lines
                int column = 0;
           for (int i = 0; i < this.whichPrintColumns.length; i++) {
                    w.write(w.getCurrentLineNumber(), column, w.getCurrentLineNumber() + 1, column);
                    column = column + columnWidth[i] + 1;
                    // log.debug("1167 i is {} column is {} columnWidth[i] is {} ", i, column, columnWidth[i]);
                }
                w.write(w.getCurrentLineNumber(), w.getCharactersPerLine(), w.getCurrentLineNumber() + 1, w.getCharactersPerLine());
                lineString = "\n";
                w.write(lineString);
                lineString = "";
            } catch (IOException e) {
                log.warn("error during printing: " + e);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventTableDataModel.class);
}
