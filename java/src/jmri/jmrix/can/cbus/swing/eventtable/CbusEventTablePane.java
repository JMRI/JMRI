package jmri.jmrix.can.cbus.swing.eventtable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.FlowLayout;
import java.awt.Frame;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing a Cbus event table. Menu code copied from BeanTableFrame.
 * <P>
 *
 * @author Andrew Crosland (C) 2009
 * @author Kevin Dickerson (C) 2012
 *
 * @since 2.99.2
 */
public class CbusEventTablePane extends jmri.jmrix.can.swing.CanPanel {

    CbusEventTableDataModel eventModel;
    JTable eventTable;
    JScrollPane eventScroll;

    protected String[] columnToolTips = {
            Bundle.getMessage("IDColTip"),
            Bundle.getMessage("NodeColTip"), // "Last Name" assumed obvious
            Bundle.getMessage("NameColTip"),
            Bundle.getMessage("EventColTip"),
            Bundle.getMessage("TypeColTip"),
            Bundle.getMessage("CommentColTip")
    }; // Length = number of items in array should (at least) match number of columns

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("MenuItemEventTable"));

        }
        return Bundle.getMessage("MenuItemEventTable");
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

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    // There can only be one instance
    public void init() {

        eventTable = new JTable(eventModel) {

            // Override JTable Header to implement table header tool tips.
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {

                    @Override
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

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.CbusEventTablePane";
    }

    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        Frame mFrame = new Frame();

        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        JMenuItem openItem = new JMenuItem(rb.getString("MenuItemOpen"));
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem(rb.getString("MenuItemSave"));
        fileMenu.add(saveItem);
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventModel.saveAsTable();
            }
        });
        saveItem.setEnabled(eventModel.isTableDirty()); // disable menuItem if table was saved and has not changed since

        JMenuItem saveAsItem = new JMenuItem(rb.getString("MenuItemSaveAs"));
        fileMenu.add(saveAsItem);
        saveAsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventModel.saveTable();
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

    @Override
    public void initComponents() {

    }

    /**
     * Find the existing CBUS event table object.
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public final CbusEventTablePane instance() {
        return self;
    }
    static private CbusEventTablePane self = null;

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

        public Default() {
            super(Bundle.getMessage("MenuItemEventTable"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusEventTablePane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventTablePane.class);
}
