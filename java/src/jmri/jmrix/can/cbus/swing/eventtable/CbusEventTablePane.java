package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.FlowLayout;
import java.awt.Frame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;


import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import java.text.DateFormat;

import jmri.util.swing.XTableColumnModel;

import jmri.jmrix.can.CanSystemConnectionMemo;

import jmri.util.davidflanagan.HardcopyWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing a Cbus event table. Menu code copied from BeanTableFrame.
 *
 * @author Andrew Crosland (C) 2009
 * @author Kevin Dickerson (C) 2012
 * @author Steve Young (C) 2018
 * @see CbusEventTableDataModel
 *
 * @since 2.99.2
 */
public class CbusEventTablePane extends jmri.jmrix.can.swing.CanPanel {

    protected CbusEventTableDataModel eventModel=null;
    protected JTable eventTable=null;
    protected JScrollPane eventScroll;
    protected JSplitPane split;
    protected JPanel pane1;
    protected JPanel neweventcontainer = new JPanel();        
    protected JTextArea tablefeedback = new JTextArea ( 1, 40 );         
    protected JScrollPane scrolltablefeedback = new JScrollPane (tablefeedback);    
    
    private DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS dd/MM/yy");
    
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        eventModel = new CbusEventTableDataModel(memo, 10,
                CbusEventTableDataModel.MAX_COLUMN); // controller, row, column
        init();
    }

    public CbusEventTablePane() {
        super();
    }

    public void init() {
        
        JTable eventTable = new JTable(eventModel) {
            // Override JTable Header to implement table header tool tips.
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    @Override
                    public String getToolTipText(MouseEvent e) {
                        try {
                            java.awt.Point p = e.getPoint();
                            int index = columnModel.getColumnIndexAtX(p.x);
                            int realIndex
                                = columnModel.getColumn(index).getModelIndex();
                            return eventModel.columnToolTips[realIndex];    
                        } catch (RuntimeException e1) {
                            //catch null pointer exception if mouse is over an empty line
                        }
                        return null;
                    }
                };
            }
        };

        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        eventTable.setColumnModel(tcm);
        
        eventTable.setAutoCreateRowSorter(true);
        
        eventTable.createDefaultColumnsFromModel();
        
        eventTable.setRowSelectionAllowed(true);
        eventTable.setColumnSelectionAllowed(false);
        eventTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
        TableColumnModel eventTableModel = eventTable.getColumnModel();
        
        TableColumn evIdColumn = eventTableModel.getColumn(CbusEventTableDataModel.EVENTID_COLUMN);
        TableColumn caIdColumn = eventTableModel.getColumn(CbusEventTableDataModel.CANID_COLUMN);

        TableColumn delBColumn = eventTableModel.getColumn(CbusEventTableDataModel.DELETE_BUTTON_COLUMN);
        delBColumn.setCellEditor(new ButtonEditor(new JButton()));
        delBColumn.setCellRenderer(new ButtonRenderer());
        
        TableColumn onBColumn = eventTableModel.getColumn(CbusEventTableDataModel.ON_BUTTON_COLUMN);
        onBColumn.setCellEditor(new ButtonEditor(new JButton()));
        onBColumn.setCellRenderer(new ButtonRenderer());
        
        TableColumn offBColumn = eventTableModel.getColumn(CbusEventTableDataModel.OFF_BUTTON_COLUMN);
        offBColumn.setCellEditor(new ButtonEditor(new JButton()));
        offBColumn.setCellRenderer(new ButtonRenderer());    
        
        TableColumn togBColumn = eventTableModel.getColumn(CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN);
        togBColumn.setCellEditor(new ButtonEditor(new JButton()));
        togBColumn.setCellRenderer(new ButtonRenderer());        
        
        TableColumn rqStatColumn = eventTableModel.getColumn(CbusEventTableDataModel.STATUS_REQUEST_BUTTON_COLUMN);
        rqStatColumn.setCellEditor(new ButtonEditor(new JButton()));
        rqStatColumn.setCellRenderer(new ButtonRenderer());   

        // format the last updated date time
        TableColumn timeColumn = eventTableModel.getColumn(CbusEventTableDataModel.LATEST_TIMESTAMP_COLUMN);
        timeColumn.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value != null && value instanceof Date) {
                    super.setValue(DATE_FORMAT.format((Date) value));
                } else {
                    super.setValue(value);
                }
            }
        });
        
        // configure items for GUI
        eventModel.configureTable(eventTable);

        // general GUI config
        
        tcm.setColumnVisible(evIdColumn, false);
        tcm.setColumnVisible(caIdColumn, false);
        tcm.setColumnVisible(delBColumn, false);
        tcm.setColumnVisible(onBColumn, false);
        tcm.setColumnVisible(offBColumn, false);
        tcm.setColumnVisible(rqStatColumn, false);
        
        addMouseListenerToHeader(eventTable);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

       
        // main pane
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BorderLayout());
        
        // scroller for main table
        eventScroll = new JScrollPane(eventTable);
        eventScroll.setPreferredSize(new Dimension(650, 250));
        
        
        tablefeedback.setEditable ( false ); // set textArea non-editable
       
        // add new event
        neweventcontainer.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), (Bundle.getMessage("NewEvent"))));
        // get event number

        // neweventcontainer.setLayout(new BoxLayout(neweventcontainer));
        
        JPanel newnode = new JPanel();
        newnode.add(new JLabel(Bundle.getMessage("CbusNode")));
        JSpinner newnodenumberSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        newnode.add(newnodenumberSpinner);
        newnode.setToolTipText(Bundle.getMessage("NewNodeTip"));
        newnodenumberSpinner.setToolTipText(Bundle.getMessage("NewNodeTip"));
        
        JPanel newev = new JPanel();
        newev.add(new JLabel("Event"));        
        JSpinner newevnumberSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 65535, 1));
        newev.add(newevnumberSpinner);
        
        JButton newevbutton = new JButton((Bundle.getMessage("NewEvent")));
        newevbutton.addActionListener((ActionEvent event) -> {
            int ev = (Integer) newevnumberSpinner.getValue();
            int nd = (Integer) newnodenumberSpinner.getValue();
            // log.warn("new event button clicked ev {} nd {} ");
            int response=eventModel.newEventFromButton(ev,nd);
            tablefeedback.append("\n");
            if (nd>0) {
                tablefeedback.append (Bundle.getMessage("CbusNode") + nd + " ");
            }
            tablefeedback.append (Bundle.getMessage("CbusEvent") + ev + " ");
            if (response==-1){
                tablefeedback.append("already on table.");
                JOptionPane.showMessageDialog(null, 
                (Bundle.getMessage("NoMakeEvent")), Bundle.getMessage("WarningTitle"),
                JOptionPane.ERROR_MESSAGE);
            }
            if (response==1) {
                tablefeedback.append("added to table.");
            }
        });

        neweventcontainer.add(newnode);
        neweventcontainer.add(newev);        
        neweventcontainer.add(newevbutton);        
        
        // adds top of table stuff
        pane1.add(neweventcontainer, BorderLayout.PAGE_START);

        // pane1.add(scrolltablefeedback, BorderLayout.PAGE_END);
        
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            eventScroll, scrolltablefeedback);
        split.setResizeWeight(0.95);
        split.setContinuousLayout(true);


        pane1.add(split, BorderLayout.CENTER);
        
        add(pane1);
        pane1.setVisible(true);    
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.eventtable.EventTablePane";
    }
    
    /**
     * Creates a Menu List
     * <p>
     * File - Print, Print Preview, Save, SaveAs csv
     * Display - show / hide Create new event pane, show/hide bottom feedback pane
     * </p>
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        Frame mFrame = new Frame();

        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        JMenu displayMenu = new JMenu(Bundle.getMessage("Display"));
        
        // Not currently implemented
        // JMenuItem openItem = new JMenuItem(rb.getString("MenuItemOpen"));
        // fileMenu.add(openItem);


        JMenuItem saveItem = new JMenuItem(rb.getString("MenuItemSave"));
        fileMenu.add(saveItem);
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventModel.saveTable();
            }
        });
        saveItem.setEnabled(eventModel.isTableDirty()); // disable menuItem if table was saved and has not changed since

        JMenuItem saveAsItem = new JMenuItem(rb.getString("MenuItemSaveAs"));
        fileMenu.add(saveAsItem);
        saveAsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventModel.saveAsTable();
            }
        });

        // add print menu items
        JMenuItem printItem = new JMenuItem(rb.getString("PrintTable"));
        fileMenu.add(printItem);
        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HardcopyWriter writer = null;
                try {
                    writer = new HardcopyWriter(mFrame, getTitle(), 10, .8, .5, .5, .5, false);
                } catch (HardcopyWriter.PrintCanceledException ex) {
                    log.debug("Print cancelled");
                    return;
                }
                writer.increaseLineSpacing(20);
                eventModel.printTable(writer); // close() is taken care of in printTable()
            }
        });
        
        
        JMenuItem previewItem = new JMenuItem(rb.getString("PreviewTable"));
        fileMenu.add(previewItem);
        previewItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HardcopyWriter writer = null;
                try {
                    writer = new HardcopyWriter(mFrame, getTitle(), 10, .8, .5, .5, .5, true);
                } catch (HardcopyWriter.PrintCanceledException ex) {
                    log.debug("Preview cancelled");
                    return;
                }
                writer.increaseLineSpacing(20);
                eventModel.printTable(writer); // close() is taken care of in printTable()
            }
        });
        
        
        
        JCheckBoxMenuItem shownewevent = new JCheckBoxMenuItem((Bundle.getMessage("NewEvent")));
        // shownewevent.setMnemonic(KeyEvent.VK_C);
        shownewevent.setSelected(true);
        displayMenu.add(shownewevent);
        shownewevent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newEvShow = shownewevent.isSelected();
                // log.debug(" show new event checkbox show {} ", newEvShow);
                neweventcontainer.setVisible(newEvShow);
            }
        });
        
        
        JCheckBoxMenuItem showinfopanel = new JCheckBoxMenuItem(Bundle.getMessage("ShowInfoPanel"));
        // shownewevent.setMnemonic(KeyEvent.VK_C);
        showinfopanel.setSelected(true);
        displayMenu.add(showinfopanel);
        showinfopanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean infoShow = showinfopanel.isSelected();
                // log.debug(" showinfopanel checkbox show {} ", newEvShow);
                scrolltablefeedback.setVisible(infoShow);
                validate();
                repaint();
            }
        });        
        
        menuList.add(fileMenu);
        menuList.add(displayMenu);
        return menuList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Bundle.getMessage("EventTableTitle");
    }
    
    /**
     * Process the column header click
     * @param e     the event data
     * @param table the JTable
     */
    protected void showTableHeaderPopup(MouseEvent e, JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        XTableColumnModel tcm = (XTableColumnModel) table.getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(false); i++) {
            TableColumn tc = tcm.getColumnByModelIndex(i);
            String columnName = table.getModel().getColumnName(i);
            if (columnName != null && !columnName.equals("")) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(table.getModel().getColumnName(i), tcm.isColumnVisible(tc));
                menuItem.addActionListener(new HeaderActionListener(tc, tcm));
                popupMenu.add(menuItem);
            }

        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Adds the column header pop listener to a JTable using XTableColumnModel
     * @param table The JTable effected.
     */
    protected void addMouseListenerToHeader(JTable table) {
        MouseListener mouseHeaderListener = new TableHeaderListener(table);
        table.getTableHeader().addMouseListener(mouseHeaderListener);
    }
    
    /**
     * Class to support listner for popup menu on XTableColum model.
     */
    static class HeaderActionListener implements ActionListener {

        TableColumn tc;
        XTableColumnModel tcm;

        HeaderActionListener(TableColumn tc, XTableColumnModel tcm) {
            this.tc = tc;
            this.tcm = tcm;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
            //Do not allow the last column to be hidden
            if (!check.isSelected() && tcm.getColumnCount(true) == 1) {
                return;
            }
            tcm.setColumnVisible(tc, check.isSelected());
        }
    }

    /**
     * Class to support Columnheader popup menu on XTableColum model.
     */
    class TableHeaderListener extends MouseAdapter {

        JTable table;

        TableHeaderListener(JTable tbl) {
            super();
            table = tbl;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }
    }
    
    public void update() {
        eventModel.fireTableDataChanged();
        // TODO disable menuItem if table was saved and has not changed since
        // replacing menuItem by a new getMenus(). Note saveItem.setEnabled(eventModel.isTableDirty());
    }

    private boolean mShown = false;

    // on startup
    @Override
    public void addNotify() {
        super.addNotify();        
        if (mShown) {
            return;
        }

        mShown = true;
    }

    @Override
    public void dispose() {
        String className = this.getClass().getSimpleName();
        log.debug("dispose called {} ",className);
        
        eventModel.dispose();
        eventModel = null;
        eventTable = null;
        eventScroll = null;
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemEventTable"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusEventTablePane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventTablePane.class);

}
