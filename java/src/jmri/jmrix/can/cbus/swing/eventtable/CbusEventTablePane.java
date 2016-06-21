package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Frame providing a Cbus event table. Menu code copied from BeanTableFrame
 * <P>
 *
 * @author	Andrew Crosland (C) 2009
 * @author	Kevin Dickerson (C) 2012
 *
 * @since 2.99.2
 */
public class CbusEventTablePane extends jmri.jmrix.can.swing.CanPanel {

    /**
     *
     */
    private static final long serialVersionUID = 6372010365640931455L;
    CbusEventTableDataModel eventModel;
    JTable eventTable;
    JScrollPane eventScroll;

    protected String[] columnToolTips = {
        "CANbus ID of event producer",
        "CBUS Node Number of event producer", // "Last Name" assumed obvious
        "Event",
        "Type of Event",
        "Enter Comments in this column"
    };

    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Event table");

        }
        return "CBUS Event table";
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        eventModel = new CbusEventTableDataModel(memo, 20,
                CbusEventTableDataModel.NUMCOLUMN);
        init();
    }

    public CbusEventTablePane() {
        super();

    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    // There can only be one instance
    public void init() {

        eventTable = new JTable(eventModel) {
            /**
             *
             */
            private static final long serialVersionUID = 1681756586973465353L;

            // Override JTable Header to implement table header tool tips.
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    /**
                     *
                     */
                    private static final long serialVersionUID = -7938503603496660164L;

                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex
                                = columnModel.getColumn(index).getModelIndex();
                        return columnToolTips[realIndex];
                    }
                };
            }
        };

// breaks build        eventTable.setAutoCreateRowSorter(false);
        eventScroll = new JScrollPane(eventTable);

        // Allow selection of a single interval of columns
        eventTable.setRowSelectionAllowed(true);
        eventTable.setColumnSelectionAllowed(false);
        eventTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        // configure items for GUI
        eventModel.configureTable(eventTable);

        // general GUI config
        //setTitle("CBUS Event table");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add file menu items
        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        add(pane1);
        add(eventScroll);

        //pack();
        //pane1.setMaximumSize(pane1.getSize());
        //pack();
        self = this;
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.CbusEventTablePane";
    }

    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();

        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        JMenuItem openItem = new JMenuItem(rb.getString("MenuItemOpen"));
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem(rb.getString("MenuItemSave"));
        fileMenu.add(saveItem);
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eventModel.saveAsTable();
            }
        });

        JMenuItem saveAsItem = new JMenuItem(rb.getString("MenuItemSaveAs"));
        fileMenu.add(saveAsItem);
        saveAsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eventModel.saveTable();
            }
        });

        // add print menu items
        JMenuItem printItem = new JMenuItem(rb.getString("PrintTable"));
        fileMenu.add(printItem);

        printItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HardcopyWriter writer = null;
                try {
                    writer = new HardcopyWriter(getWindowInterface().getFrame(), getTitle(), 10, .8, .5, .5, .5, false);
                } catch (HardcopyWriter.PrintCanceledException ex) {
                    //log.debug("Print cancelled");
                    return;
                }
                writer.increaseLineSpacing(20);
                eventModel.printTable(writer);
            }
        });
        JMenuItem previewItem = new JMenuItem(rb.getString("PreviewTable"));
        fileMenu.add(previewItem);
        previewItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HardcopyWriter writer = null;
                try {
                    writer = new HardcopyWriter(getWindowInterface().getFrame(), getTitle(), 10, .8, .5, .5, .5, true);
                } catch (HardcopyWriter.PrintCanceledException ex) {
                    //log.debug("Print cancelled");
                    return;
                }
                writer.increaseLineSpacing(20);
                eventModel.printTable(writer);
            }
        });
        menuList.add(fileMenu);
        return menuList;
    }

    public void initComponents() {

    }

    /**
     * method to find the existing CBUS event table object
     */
    static public final CbusEventTablePane instance() {
        return self;
    }
    static private CbusEventTablePane self = null;

    public void update() {
        eventModel.fireTableDataChanged();
    }

    private boolean mShown = false;

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

    public void dispose() {
        eventModel.dispose();
        eventModel = null;
        eventTable = null;
        eventScroll = null;
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = 8642959048971044410L;

        public Default() {
            super("CBUS Event table",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusEventTablePane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
}
