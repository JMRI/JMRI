package jmri.jmrit.roster.swing.rostergroup;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

/**
 * Provide a JFrame to display the Roster Data Based upon BeanTableFrame.
 * <P>
 * This frame includes the table itself at the top, plus a "bottom area" for
 * things like an Add... button and checkboxes that control display options.
 * <p>
 * The usual menus are also provided here.
 * <p>
 * Specific uses are customized via the RosterGroupTableDataModel implementation
 * they provide, and by providing a {@link #extras} implementation that can in
 * turn invoke {@link #addToBottomBox} as needed.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Kevin Dickerson Copyright (C) 2009
  */
public class RosterGroupTableFrame extends jmri.util.JmriJFrame {

    RosterGroupTableModel dataModel;
    JTable dataTable;
    JScrollPane dataScroll;
    Box bottomBox;  // panel at bottom for extra buttons etc
    int bottomBoxIndex; // index to insert extra stuff
    static final int bottomStrutWidth = 20;
    Box topBox;  // panel at bottom for extra buttons etc
    int topBoxIndex; // index to insert extra stuff
    static final int topStrutWidth = 20;

//    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    public RosterGroupTableFrame(RosterGroupTableModel model, String helpTarget) {

        super();
        dataModel = model;

        dataTable = new JTable(dataModel);
        TableRowSorter<RosterGroupTableModel> sorter = new TableRowSorter<>(dataModel);
        dataTable.setRowSorter(sorter);

        sorter.setComparator(RosterGroupTableModel.IDCOL, new jmri.util.AlphanumComparator());
        sorter.toggleSortOrder(RosterGroupTableModel.IDCOL);
        dataTable = new JTable(dataModel);
        
        dataScroll = new JScrollPane(dataTable);

        // configure items for GUI
        dataModel.configureTable(dataTable);

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add save menu item
        JMenuBar menuBar = new JMenuBar();

        setJMenuBar(menuBar);

        addHelpMenu(helpTarget, true);

        // install items in GUI
        topBox = Box.createHorizontalBox();
        topBox.add(Box.createHorizontalGlue()); // stays at beginning of box
        topBoxIndex = 0;
        getContentPane().add(topBox);
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

    }

    /**
     * Hook to allow sub-types to install more items in GUI.
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
     */
    protected void addToBottomBox(Component comp) {
        bottomBox.add(Box.createHorizontalStrut(bottomStrutWidth), bottomBoxIndex);
        ++bottomBoxIndex;
        bottomBox.add(comp, bottomBoxIndex);
        ++bottomBoxIndex;
    }

    protected Box getTopBox() {
        return topBox;
    }

    /**
     * Add a component to the bottom box. Takes care of organising glue, struts
     * etc.
     *
     */
    protected void addToTopBox(Component comp) {
        topBox.add(Box.createHorizontalStrut(topStrutWidth), topBoxIndex);
        ++topBoxIndex;
        topBox.add(comp, topBoxIndex);
        ++topBoxIndex;
    }

    @Override
    public void dispose() {
        if (dataModel != null) {
            dataModel.dispose();
        }
        dataModel = null;
        dataTable = null;
        dataScroll = null;
        super.dispose();
    }

}
