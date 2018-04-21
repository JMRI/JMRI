package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.swing.RowSorterUtil;

/**
 * Provide a table of block route entries as a JmriJPanel
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class LayoutBlockRouteTable extends jmri.util.swing.JmriPanel {

    private LayoutBlockRouteTableModel dataModel;
    private LayoutBlockNeighbourTableModel neighbourDataModel;
    private TableRowSorter<LayoutBlockNeighbourTableModel> neighbourSorter;
    private JTable neighbourDataTable;
    private JScrollPane neighbourDataScroll;
    private TableRowSorter<LayoutBlockRouteTableModel> sorter;
    private JTable dataTable;
    private JScrollPane dataScroll;

    private LayoutBlockThroughPathsTableModel throughPathsDataModel;
    private TableRowSorter<LayoutBlockThroughPathsTableModel> throughPathsSorter;
    private JTable throughPathsDataTable;
    private JScrollPane throughPathsDataScroll;

    public LayoutBlockRouteTable(boolean editable, LayoutBlock block) {
        super();

        //This could do with being presented in a JSplit Panel
        dataModel = new LayoutBlockRouteTableModel(editable, block);
        sorter = new TableRowSorter<>(dataModel);
        dataTable = new JTable(dataModel);
        dataTable.setRowSorter(sorter);
        dataScroll = new JScrollPane(dataTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        neighbourDataModel = new LayoutBlockNeighbourTableModel(editable, block);
        neighbourSorter = new TableRowSorter<>(neighbourDataModel);
        neighbourDataTable = new JTable(neighbourDataModel);
        neighbourDataTable.setRowSorter(neighbourSorter);
        neighbourDataScroll = new JScrollPane(neighbourDataTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        throughPathsDataModel = new LayoutBlockThroughPathsTableModel(editable, block);
        throughPathsSorter = new TableRowSorter<>(throughPathsDataModel);
        throughPathsDataTable = new JTable(throughPathsDataModel);
        throughPathsDataTable.setRowSorter(throughPathsSorter);
        throughPathsDataScroll = new JScrollPane(throughPathsDataTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // set initial sort
        RowSorterUtil.setSortOrder(sorter, LayoutBlockRouteTableModel.HOPCOUNTCOL, SortOrder.ASCENDING);
        RowSorterUtil.setSortOrder(this.neighbourSorter, LayoutBlockNeighbourTableModel.NEIGHBOURCOL, SortOrder.ASCENDING);
        RowSorterUtil.setSortOrder(this.throughPathsSorter, LayoutBlockThroughPathsTableModel.SOURCECOL, SortOrder.ASCENDING);

        // allow reordering of the columns
        dataTable.getTableHeader().setReorderingAllowed(true);
        neighbourDataTable.getTableHeader().setReorderingAllowed(true);
        throughPathsDataTable.getTableHeader().setReorderingAllowed(true);

        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        neighbourDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        throughPathsDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // - - - Configure data table - - -
        // resize columns as requested
        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            int width = dataModel.getPreferredWidth(i);
            dataTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        dataTable.sizeColumnsToFit(-1);

        // set Viewport preferred size from size of table
        java.awt.Dimension dataTableSize = dataTable.getPreferredSize();
        // set minimum Viewport size
        dataTableSize.height = Math.max(dataTableSize.height, 400);
        dataTableSize.width = Math.max(dataTableSize.width, 400);
        dataScroll.getViewport().setPreferredSize(dataTableSize);

        // set preferred scrolling options
//        dataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        dataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // set to single selection
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // - - - Configure neighbor table - - -
        // resize columns as requested
        for (int i = 0; i < neighbourDataTable.getColumnCount(); i++) {
            int width = neighbourDataModel.getPreferredWidth(i);
            neighbourDataTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        neighbourDataTable.sizeColumnsToFit(-1);

        // set Viewport preferred size from size of table
        java.awt.Dimension neighbourDataTableSize = neighbourDataTable.getPreferredSize();
        // set minimum Viewport size
        neighbourDataTableSize.height = Math.max(neighbourDataTableSize.height, 400);
        neighbourDataTableSize.width = Math.max(neighbourDataTableSize.width, 400);
        neighbourDataScroll.getViewport().setPreferredSize(neighbourDataTableSize);

        // set preferred scrolling options
        neighbourDataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        neighbourDataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // set to single selection
        neighbourDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // - - - Configure through paths table - - -
        // resize columns as requested
        for (int i = 0; i < throughPathsDataTable.getColumnCount(); i++) {
            int width = throughPathsDataModel.getPreferredWidth(i);
            throughPathsDataTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        throughPathsDataTable.sizeColumnsToFit(-1);

        // set Viewport preferred size from size of table
        java.awt.Dimension throughPathsDataTableSize = throughPathsDataTable.getPreferredSize();
        // set minimum Viewport size
        throughPathsDataTableSize.height = Math.max(throughPathsDataTableSize.height, 400);
        throughPathsDataTableSize.width = Math.max(throughPathsDataTableSize.width, 400);
        throughPathsDataScroll.getViewport().setPreferredSize(throughPathsDataTableSize);

        // set preferred scrolling options
        throughPathsDataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        throughPathsDataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // set to single selection
        throughPathsDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel neigh = new JPanel();
        neigh.setLayout(new BoxLayout(neigh, BoxLayout.Y_AXIS));
        neigh.add(new JLabel(Bundle.getMessage("Neighbouring")));
        neigh.add(neighbourDataScroll);

        JPanel through = new JPanel();
        through.setLayout(new BoxLayout(through, BoxLayout.Y_AXIS));
        through.add(new JLabel(Bundle.getMessage("ValidPaths")));
        through.add(throughPathsDataScroll);

        JPanel routePane = new JPanel();
        routePane.setLayout(new BoxLayout(routePane, BoxLayout.Y_AXIS));
        routePane.add(new JLabel(Bundle.getMessage("Accessible")));
        routePane.add(dataScroll);

        JSplitPane splitTopPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                neigh, through);
        splitTopPane.setOneTouchExpandable(true);
        splitTopPane.setDividerLocation(150);
        JSplitPane splitBotPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                splitTopPane, routePane);
        splitBotPane.setOneTouchExpandable(true);
        splitBotPane.setDividerLocation(300);
        add(splitBotPane);

    }

    public JTable getTable() {
        return dataTable;
    }

    public TableModel getModel() {
        return this.dataModel;
    }

    public JTable getNeighbourTable() {
        return neighbourDataTable;
    }

    public TableModel getNeighbourModel() {
        return this.neighbourDataModel;
    }

    @Override
    public void dispose() {
        if (dataModel != null) {
            dataModel.dispose();
        }
        dataModel = null;
        dataTable = null;
        dataScroll = null;
        neighbourDataModel = null;
        neighbourSorter = null;
        neighbourDataTable = null;
        neighbourDataScroll = null;

        super.dispose();
    }
}
