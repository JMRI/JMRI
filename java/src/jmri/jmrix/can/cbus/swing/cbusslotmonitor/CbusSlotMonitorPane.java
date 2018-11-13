package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.util.swing.XTableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for monitoring and configuring a MERG CBUS Command Station
 *
 * @author Steve Young Copyright (C) 2018
 * @since 4.13.4
 */
public class CbusSlotMonitorPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    TrafficController tc;

    static private int MAX_LINES = 5000;
    private static TextAreaFIFO tablefeedback = new TextAreaFIFO(MAX_LINES);
    private static JScrollPane scrolltablefeedback = new JScrollPane (tablefeedback);
    private JSplitPane split;
    private double _splitratio = 0.95;
    protected JScrollPane slotScroll;
    
    protected CbusSlotMonitorDataModel slotModel=null;
    protected JTable slotTable=null;
    
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
        slotModel = new CbusSlotMonitorDataModel(memo, 5,
            CbusSlotMonitorDataModel.MAX_COLUMN); // controller, row, column
        init();
    }


    public void init() {
        
        
        JTable slotTable = new JTable(slotModel) {
            // Override JTable Header to implement table header tool tips.
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    @Override
                    public String getToolTipText(MouseEvent e) {
                        try {
                           // log.debug("131 gettttext");
                            java.awt.Point p = e.getPoint();
                            int index = columnModel.getColumnIndexAtX(p.x);
                            int realIndex = columnModel.getColumn(index).getModelIndex();
                            return CbusSlotMonitorDataModel.columnToolTips[realIndex];    
                        } catch (RuntimeException e1) {
                            //catch null pointer exception if mouse is over an empty line
                        }
                        return null;
                    }
                };
            }
        };        
        
        
        // Use XTableColumnModel so we can control which columns are visible
        final  XTableColumnModel tcm = new XTableColumnModel();
        slotTable.setColumnModel(tcm);
        slotTable.createDefaultColumnsFromModel();        
        
        
        slotTable.setAutoCreateRowSorter(true);
        
        final TableRowSorter<CbusSlotMonitorDataModel> sorter = new TableRowSorter<CbusSlotMonitorDataModel>(slotModel);
        slotTable.setRowSorter(sorter);        
        
        
        
        // configure items for GUI
        slotModel.configureTable(slotTable);
        
        
        
        
        slotScroll = new JScrollPane(slotTable);
        slotScroll.setPreferredSize(new Dimension(450, 200));
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add event displays
        JPanel p1 = new JPanel();
        // p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.setLayout(new BorderLayout());
        // p1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutEvents")));
        
        JPanel toppanelcontainer = new JPanel();
        toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
        tablefeedback.setEditable ( false );
        
        
        
        Dimension scrolltablefeedbackminimumSize = new Dimension(150, 20);
        scrolltablefeedback.setMinimumSize(scrolltablefeedbackminimumSize);
        
      //  JLabel test = new JLabel("Main table menu stuff, e-stop button, track power button, comment / function column for dkeep?");
      //  toppanelcontainer.add(test);

        
        // JPanel slottablepane = new JPanel();
        
        
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            slotScroll, scrolltablefeedback);
        split.setResizeWeight(_splitratio);
        split.setContinuousLayout(true);

        p1.add(toppanelcontainer, BorderLayout.PAGE_START);
        p1.add(split, BorderLayout.CENTER);        
        add(p1);        
        
        p1.setVisible(true);
        log.debug("init complete, mini delay then send RSTAT to get command station stuff, button refresh row via QLOC? ");
        
        log.debug("class name {} ",CbusSlotMonitorPane.class.getName());
        
    }
    
    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("MenuItemCbusSlotMonitor"));
        }
        return Bundle.getMessage("MenuItemCbusSlotMonitor");
    }

    public CbusSlotMonitorPane() {
        super();
    }

    @Override
    public void reply(jmri.jmrix.can.CanReply m) {
    }

    @Override
    public void message(jmri.jmrix.can.CanMessage m) {
    }
    
    protected static void updateLogFromModel(int cbuserror, String cbustext){
        
       // log.warn("in event table error = {} text = {} length = {} ",cbuserror,cbustext,cbustext.length());
        // tablefeedback.append("\n");
        
        if (cbuserror==3) {
            tablefeedback.append ("\n * * * * * * * * * * * * * * * * * * * * * * " + cbustext);
        } else {
            tablefeedback.append( "\n"+cbustext);
        }
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.cbusslotmonitor.CbusSlotMonitorPane";
    }    
    
    @Override
    public void dispose() {
        // todo - send messages to null signal data on any cabs

        slotTable = null;
        slotModel.dispose();
        // disconnect from CBUS
        tc.removeCanListener(this);
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemCbusSlotMonitor"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusSlotMonitorPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    private static final Logger log = LoggerFactory.getLogger(CbusSlotMonitorPane.class);

}
