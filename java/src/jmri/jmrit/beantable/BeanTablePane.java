package jmri.jmrit.beantable;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import jmri.swing.RowSorterUtil;
import jmri.util.SystemNameComparator;

/**
 * Provide a JPanel to display a table of NamedBeans.
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
public class BeanTablePane extends jmri.util.swing.JmriPanel {

    BeanTableDataModel dataModel;
    JTable dataTable;
    JScrollPane dataScroll;
    Box bottomBox;  // panel at bottom for extra buttons etc
    int bottomBoxIndex; // index to insert extra stuff
    static final int bottomStrutWidth = 20;

    public void init(BeanTableDataModel model) {

        dataModel = model;

        TableRowSorter<BeanTableDataModel> sorter = new TableRowSorter<>(dataModel);
        dataTable = dataModel.makeJTable(dataModel.getMasterClassName(), dataModel, sorter);
        dataScroll = new JScrollPane(dataTable);

        // give system name column as smarter sorter and use it initially
        sorter.setComparator(BeanTableDataModel.SYSNAMECOL, new SystemNameComparator());
        RowSorterUtil.setSortOrder(sorter, BeanTableDataModel.SYSNAMECOL, SortOrder.ASCENDING);
        this.dataTable.setRowSorter(sorter);

        // configure items for GUI
        dataModel.configureTable(dataTable);

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // install items in GUI
        add(dataScroll);
        bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue()); // stays at end of box
        bottomBoxIndex = 0;

        add(bottomBox);

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
     * Add a component to the bottom box.
     * <p>
     * Takes care of organising glue, struts etc.
     *
     * @param comp {@link Component} to add
     */
    protected void addToBottomBox(Component comp) {
        bottomBox.add(Box.createHorizontalStrut(bottomStrutWidth), bottomBoxIndex);
        ++bottomBoxIndex;
        bottomBox.add(comp, bottomBoxIndex);
        ++bottomBoxIndex;
    }

    @Override
    public void dispose() {
        if (dataModel != null) {
            dataModel.dispose();
        }
        dataModel = null;
        dataTable = null;
        dataScroll = null;
    }
}
