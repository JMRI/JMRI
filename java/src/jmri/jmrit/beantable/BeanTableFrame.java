package jmri.jmrit.beantable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import jmri.swing.RowSorterUtil;
import jmri.util.AlphanumComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a JFrame to display a table of NamedBeans.
 * <P>
 * This frame includes the table itself at the top, plus a "bottom area" for
 * things like an Add... button and checkboxes that control display options.
 * <p>
 * The usual menus are also provided here.
 * <p>
 * Specific uses are customized via the BeanTableDataModel implementation they
 * provide, and by providing a {@link #extras} implementation that can in turn
 * invoke {@link #addToBottomBox} as needed.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class BeanTableFrame extends jmri.util.JmriJFrame {

    BeanTableDataModel dataModel;
    JTable dataTable;
    JScrollPane dataScroll;
    Box bottomBox;  // panel at bottom for extra buttons etc
    int bottomBoxIndex; // index to insert extra stuff
    static final int bottomStrutWidth = 20;

    ResourceBundle rbapps = ResourceBundle.getBundle("apps.AppsBundle");

    public BeanTableFrame() {
        super();
    }

    public BeanTableFrame(String s) {
        super(s);
    }

    public BeanTableFrame(BeanTableDataModel model, String helpTarget, JTable dataTab) {

        super();
        dataModel = model;
        this.dataTable = dataTab;

        dataScroll = new JScrollPane(dataTable);

        // give system name column as smarter sorter and use it initially
        TableRowSorter<BeanTableDataModel> sorter = new TableRowSorter<>(dataModel);

        // use NamedBean's built-in Comparator interface for sorting the system name column
        RowSorterUtil.setSortOrder(sorter, BeanTableDataModel.SYSNAMECOL, SortOrder.ASCENDING);

        sorter.setComparator(BeanTableDataModel.USERNAMECOL, new AlphanumComparator());
        RowSorterUtil.setSortOrder(sorter, BeanTableDataModel.USERNAMECOL, SortOrder.ASCENDING);

        this.dataTable.setRowSorter(sorter);

        // configure items for GUI
        dataModel.configureTable(dataTable);

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.SaveMenu());

        JMenuItem printItem = new JMenuItem(rbapps.getString("PrintTable"));
        fileMenu.add(printItem);
        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // MessageFormat headerFormat = new MessageFormat(getTitle());  // not used below
                    MessageFormat footerFormat = new MessageFormat(getTitle() + " page {0,number}");
                    dataTable.print(JTable.PrintMode.FIT_WIDTH, null, footerFormat);
                } catch (java.awt.print.PrinterException e1) {
                    log.warn("error printing: " + e1, e1);
                }
            }
        });

        setJMenuBar(menuBar);

        addHelpMenu(helpTarget, true);

        // install items in GUI
        getContentPane().add(dataScroll);
        bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue()); // stays at end of box
        bottomBoxIndex = 0;

        getContentPane().add(bottomBox);

        // add extras, if desired by subclass
        extras();

        // set Viewport preferred size from size of table
        java.awt.Dimension dataTableSize = dataTable.getPreferredSize();
        // width is right, but if table is empty, it's not high
        // enough to reserve much space.
        dataTableSize.height = Math.max(dataTableSize.height, 400);
        dataScroll.getViewport().setPreferredSize(dataTableSize);

        // set preferred scrolling options
        dataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        dataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        dataModel.persistTable(dataTable);
    }

    /**
     * Hook to allow sub-types to install more items in GUI
     */
    void extras() {
    }

    protected Box getBottomBox() {
        return bottomBox;
    }

    /**
     * Add a component to the bottom box. Takes care of organising glue, struts
     * etc
     *
     * @param comp {@link Component} to add
     * @param c    Class name
     */
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD",
            justification = "param c is required in the listedtableframe")
    protected void addToBottomBox(Component comp, String c) {
        bottomBox.add(Box.createHorizontalStrut(bottomStrutWidth), bottomBoxIndex);
        ++bottomBoxIndex;
        bottomBox.add(comp, bottomBoxIndex);
        ++bottomBoxIndex;
    }

    @Override
    public void dispose() {
        if (dataModel != null) {
            dataModel.stopPersistingTable(dataTable);
            dataModel.dispose();
        }
        dataModel = null;
        dataTable = null;
        dataScroll = null;
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(BeanTableFrame.class);
}
