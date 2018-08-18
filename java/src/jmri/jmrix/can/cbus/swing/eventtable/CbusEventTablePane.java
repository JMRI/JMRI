package jmri.jmrix.can.cbus.swing.eventtable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.FlowLayout;
import java.awt.Frame;

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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
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

    CbusEventTableDataModel eventModel;
    JTable eventTable;
    JScrollPane eventScroll;

    
    
    

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        eventModel = new CbusEventTableDataModel(memo, 20,
                CbusEventTableDataModel.MAX_COLUMN); // controller, row, column
        init();
    }

    public CbusEventTablePane() {
        super();
    }


    public void init() {

        eventTable = new JTable(eventModel) {
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
        
        
        eventScroll = new JScrollPane(eventTable);
        
        
        eventTable.createDefaultColumnsFromModel();
        
        eventTable.setRowSelectionAllowed(true);
        eventTable.setColumnSelectionAllowed(false);
        eventTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
        
        eventTable.getColumnModel().getColumn(eventModel.DELETE_BUTTON_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        eventTable.getColumnModel().getColumn(eventModel.DELETE_BUTTON_COLUMN).setCellRenderer(new ButtonRenderer());
        
        eventTable.getColumnModel().getColumn(eventModel.ON_BUTTON_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        eventTable.getColumnModel().getColumn(eventModel.ON_BUTTON_COLUMN).setCellRenderer(new ButtonRenderer());
                
        eventTable.getColumnModel().getColumn(eventModel.OFF_BUTTON_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        eventTable.getColumnModel().getColumn(eventModel.OFF_BUTTON_COLUMN).setCellRenderer(new ButtonRenderer());


        eventTable.getColumnModel().getColumn(eventModel.TOGGLE_BUTTON_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        eventTable.getColumnModel().getColumn(eventModel.TOGGLE_BUTTON_COLUMN).setCellRenderer(new ButtonRenderer());    

        eventTable.getColumnModel().getColumn(eventModel.STATUS_REQUEST_BUTTON_COLUMN).setCellEditor(new ButtonEditor(new JButton()));
        eventTable.getColumnModel().getColumn(eventModel.STATUS_REQUEST_BUTTON_COLUMN).setCellRenderer(new ButtonRenderer());    
        
        
        // format the last updated date time
        eventTable.getColumnModel().getColumn(eventModel.LATEST_TIMESTAMP_COLUMN).setCellRenderer(new DefaultTableCellRenderer() {
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
        
        tcm.setColumnVisible(tcm.getColumnByModelIndex(eventModel.EVENTID_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(eventModel.DELETE_BUTTON_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(eventModel.ON_BUTTON_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(eventModel.OFF_BUTTON_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(eventModel.STATUS_REQUEST_BUTTON_COLUMN), false);
        
        addMouseListenerToHeader(eventTable);
        
        
        
        // this.setTitle(Bundle.getMessage("MenuItemEventTable")); // TODO I18N
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        
        
        /* TODO _ Add top menu bar
        
        
        
        JPanel paneTopAcross = new JPanel();
        paneTopAcross.setLayout(new BoxLayout(paneTopAcross, BoxLayout.Y_AXIS));

        JPanel topPane = new JPanel();
        // Add a nice border
        topPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), (" dfgh dfgh fgh fgh gfh ")));
        add(topPane);
        
        JTextArea myTextarea = new JTextArea();
        myTextarea.setText("Total events + options + space to drag to create new turnout sensor light");
        myTextarea.setVisible(true);

        topPane.add(myTextarea);
        
        */
        
        
        // add file menu items
        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        add(pane1);
        add(eventScroll);

        //pack();
        //pane1.setMaximumSize(pane1.getSize());
        //pack();
    }

    
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS dd/MM/yy");
    
    
    
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.eventtable.EventTablePane";
    }
    
    

    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        Frame mFrame = new Frame();

        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));

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
        menuList.add(fileMenu);
        return menuList;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Bundle.getMessage("EventTableTitle");
    }
    
    
    

    /*
     * Mouse popup stuff
     */

    /**
     * Process the column header click
     * @param e     the evnt data
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
    
    
    protected class HeaderActionListener implements ActionListener {

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

    @Override
    public void addNotify() {
        super.addNotify();

        if (mShown) {
            return;
        }

        // resize frame to account for menubar
        /*JMenuBar jMenuBar = getJMenuBar();
         if (jMenuBar != null) {
         int jMenuBarHeight = jMenuBar.getPreferredSize().height;
         Dimension dimension = getSize();
         dimension.height += jMenuBarHeight;
         setSize(dimension);
         }*/
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
