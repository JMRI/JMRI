package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import jmri.*;
import jmri.swing.RowSorterUtil;
import jmri.util.AlphanumComparator;
import jmri.util.swing.TriStateJCheckBox;
import jmri.util.swing.XTableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractTableTabAction<E extends NamedBean> extends AbstractTableAction<E> {

    protected JPanel dataPanel;
    protected JTabbedPane dataTabs;

    protected boolean init = false;

    public AbstractTableTabAction(String s) {
        super(s);
    }

    @Override
    protected void createModel() {
        dataPanel = new JPanel();
        dataTabs = new JTabbedPane();
        dataPanel.setLayout(new BorderLayout());
        Manager<E> mgr = getManager();
        if (mgr instanceof jmri.managers.AbstractProxyManager) {
            // build the list, with default at start and internal at end (if present)
            jmri.managers.AbstractProxyManager<E> proxy = (jmri.managers.AbstractProxyManager<E>) mgr;

            tabbedTableArray.add(new TabbedTableItem<>(Bundle.getMessage("All"), true, mgr, getNewTableAction("All"))); // NOI18N

            proxy.getDisplayOrderManagerList().stream().map(manager -> {
                String manuName = manager.getMemo().getUserName();
                TabbedTableItem<E> itemModel = new TabbedTableItem<>(manuName, true, manager, getNewTableAction(manuName)); // connection name to display in Tab
                return itemModel;
            }).forEachOrdered(itemModel -> {
                tabbedTableArray.add(itemModel);
            });

        } else {
            Manager<E> man = getManager();
            String manuName = ( man!=null ? man.getMemo().getUserName() : "Unknown Manager");
            tabbedTableArray.add(new TabbedTableItem<>(manuName, true, getManager(), getNewTableAction(manuName)));
        }
        for (int x = 0; x < tabbedTableArray.size(); x++) {
            AbstractTableAction<E> table = tabbedTableArray.get(x).getAAClass();
            table.addToPanel(this);
            dataTabs.addTab(tabbedTableArray.get(x).getItemString(), null, tabbedTableArray.get(x).getPanel(), null);
        }
        dataTabs.addChangeListener((ChangeEvent evt) -> {
            setMenuBar(f);
        });
        dataPanel.add(dataTabs, BorderLayout.CENTER);
        init = true;
    }

    @Override
    abstract protected Manager<E> getManager();

    abstract protected AbstractTableAction<E> getNewTableAction(String choice);

    @Override
    public JPanel getPanel() {
        if (!init) {
            createModel();
        }
        return dataPanel;
    }

    protected ArrayList<TabbedTableItem<E>> tabbedTableArray = new ArrayList<>();

    @Override
    protected void setTitle() {
        //atf.setTitle("multiple sensors");
    }

    @Override
    abstract protected String helpTarget();

    @Override
    protected void addPressed(ActionEvent e) {
        log.warn("This should not have happened");
    }

    @Override
    public void addToFrame(BeanTableFrame<E> f) {
        try {
            TabbedTableItem<E> table = tabbedTableArray.get(dataTabs.getSelectedIndex());
            if (table != null) {
                table.getAAClass().addToFrame(f);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            log.error("{} in add to Frame {} {}", ex.toString(), dataTabs.getSelectedIndex(), dataTabs.getSelectedComponent());
        }
    }

    @Override
    public void setMenuBar(BeanTableFrame<E> f) {
        try {
            tabbedTableArray.get(dataTabs.getSelectedIndex()).getAAClass().setMenuBar(f);
        } catch (ArrayIndexOutOfBoundsException ex) {
            log.error("{} in add to Menu {} {}", ex.toString(), dataTabs.getSelectedIndex(), dataTabs.getSelectedComponent());
        }
    }

    public void addToBottomBox(JComponent c, String str) {
        tabbedTableArray.forEach((table) -> {
            String item = table.getItemString();
            if (item != null && item.equals(str)) {
                table.addToBottomBox(c);
            }
        });
    }

    @Override
    public void print(javax.swing.JTable.PrintMode mode, java.text.MessageFormat headerFormat, java.text.MessageFormat footerFormat) {
        try {
            tabbedTableArray.get(dataTabs.getSelectedIndex()).getDataTable().print(mode, headerFormat, footerFormat);
        } catch (java.awt.print.PrinterException e1) {
            log.warn("error printing", e1);
        } catch (NullPointerException ex) {
            log.error("Trying to print returned a NPE error");
        }
    }

    @Override
    public void dispose() {
        for (int x = 0; x < tabbedTableArray.size(); x++) {
            tabbedTableArray.get(x).dispose();
        }
        super.dispose();
    }

    static protected class TabbedTableItem<E extends NamedBean> implements TableColumnModelListener {  // E comes from the parent

        final AbstractTableAction<E> tableAction;
        final String itemText;
        private BeanTableDataModel<E> dataModel;
        private JTable dataTable;
        private JScrollPane dataScroll;
        final Box bottomBox;
        private boolean addToFrameRan = false;
        final Manager<E> manager;

        private int bottomBoxIndex; // index to insert extra stuff
        static final int BOTTOM_STRUT_WIDTH = 20;

        private boolean standardModel = true;

        final JPanel dataPanel = new JPanel();

        public TabbedTableItem(String choice, boolean stdModel, Manager<E> manager, AbstractTableAction<E> tableAction) {

            this.tableAction = tableAction;
            itemText = choice;
            standardModel = stdModel;
            this.manager = manager;

            //If a panel model is used, it should really add to the bottom box
            //but it can be done this way if required.
            bottomBox = Box.createHorizontalBox();
            bottomBox.add(Box.createHorizontalGlue());
            bottomBoxIndex = 0;
            dataPanel.setLayout(new BorderLayout());
            if (stdModel) {
                createDataModel();
            } else {
                addPanelModel();
            }
        }

        final void createDataModel() {
            if (manager != null) {
                tableAction.setManager(manager);
            }
            dataModel = tableAction.getTableDataModel();
            TableRowSorter<BeanTableDataModel<E>> sorter = new TableRowSorter<>(dataModel);
            dataTable = dataModel.makeJTable(dataModel.getMasterClassName() + ":" + getItemString(), dataModel, sorter);
            dataScroll = new JScrollPane(dataTable);

            RowSorterUtil.setSortOrder(sorter, BeanTableDataModel.SYSNAMECOL, SortOrder.ASCENDING);

            sorter.setComparator(BeanTableDataModel.USERNAMECOL, new AlphanumComparator());
            RowSorterUtil.setSortOrder(sorter, BeanTableDataModel.USERNAMECOL, SortOrder.ASCENDING);

            dataModel.configureTable(dataTable);
            tableAction.configureTable(dataTable);

            java.awt.Dimension dataTableSize = dataTable.getPreferredSize();
            // width is right, but if table is empty, it's not high
            // enough to reserve much space.
            dataTableSize.height = Math.max(dataTableSize.height, 400);
            dataScroll.getViewport().setPreferredSize(dataTableSize);

            // set preferred scrolling options
            dataScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            dataScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            //dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
            dataPanel.add(dataScroll, BorderLayout.CENTER);

            dataPanel.add(bottomBox, BorderLayout.SOUTH);
            if (tableAction.includeAddButton()) {
                JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));
                addToBottomBox(addButton);
                addButton.addActionListener((ActionEvent e) -> {
                    tableAction.addPressed(e);
                });
            }
            if (dataModel.getPropertyColumnCount() > 0) {
                propertyVisible.setToolTipText(Bundle.getMessage
                        ("ShowSystemSpecificPropertiesToolTip"));
                addToBottomBox(propertyVisible);
                propertyVisible.addActionListener((ActionEvent e) -> {
                    dataModel.setPropertyColumnsVisible(dataTable, propertyVisible.isSelected());
                });
            }

            fireColumnsUpdated(); // init bottom buttons
            dataTable.getColumnModel().addColumnModelListener(this);

        }

        private final TriStateJCheckBox propertyVisible = new TriStateJCheckBox(Bundle.getMessage("ShowSystemSpecificProperties"));

        /**
         * Notify the subclasses that column visibility has been updated,
         * or the table has finished loading.
         *
         * Sends notification to the tableAction with boolean array of column visibility.
         *
         */
        private void fireColumnsUpdated(){
            TableColumnModel model = dataTable.getColumnModel();
            if (model instanceof XTableColumnModel) {
                Enumeration<TableColumn> e = ((XTableColumnModel) model).getColumns(false);
                int numCols = ((XTableColumnModel) model).getColumnCount(false);
                // XTableColumnModel has been spotted to return a fleeting different
                // column count to actual model, generally if manager is changed at startup
                // so we do a sanity check to make sure the models are in synch.
                if (numCols != dataModel.getColumnCount()){
                    log.debug("Difference with Xtable cols: {} Model cols: {}",numCols,dataModel.getColumnCount());
                    return;
                }
                boolean[] colsVisible = new boolean[numCols];
                while (e.hasMoreElements()) {
                    TableColumn column = e.nextElement();
                    boolean visible = ((XTableColumnModel) model).isColumnVisible(column);
                    colsVisible[column.getModelIndex()] = visible;
                }
                tableAction.columnsVisibleUpdated(colsVisible);
                setPropertyVisibleCheckbox(colsVisible);
            }
        }

        /**
         * Updates the custom bean property columns checkbox.
         * @param colsVisible array of column visibility
         */
        private void setPropertyVisibleCheckbox(boolean[] colsVisible){
            int numberofCustomCols = dataModel.getPropertyColumnCount();
            if (numberofCustomCols>0){
                boolean[] customColVisibility = new boolean[numberofCustomCols];
                for ( int i=0; i<numberofCustomCols; i++){
                    customColVisibility[i]=colsVisible[colsVisible.length-i-1];
                }
                propertyVisible.setState(customColVisibility);
            }
        }

        /**
         * {@inheritDoc}
         * A column is now visible.  fireColumnsUpdated()
         */
        @Override
        public void columnAdded(TableColumnModelEvent e) {
            fireColumnsUpdated();
        }

        /**
         * {@inheritDoc}
         * A column is now hidden.  fireColumnsUpdated()
         */
        @Override
        public void columnRemoved(TableColumnModelEvent e) {
            fireColumnsUpdated();
        }

        /**
         * {@inheritDoc}
         * Unused.
         */
        @Override
        public void columnMoved(TableColumnModelEvent e) {}

        /**
         * {@inheritDoc}
         * Unused.
         */
        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {}

        /**
         * {@inheritDoc}
         * Unused.
         */
        @Override
        public void columnMarginChanged(ChangeEvent e) {}

        final void addPanelModel() {
            dataPanel.add(tableAction.getPanel(), BorderLayout.CENTER);
            dataPanel.add(bottomBox, BorderLayout.SOUTH);
        }

        public boolean getStandardTableModel() {
            return standardModel;
        }

        public String getItemString() {
            return itemText;
        }

        public AbstractTableAction<E> getAAClass() {
            return tableAction;
        }

        public JPanel getPanel() {
            return dataPanel;
        }

        public boolean getAdditionsToFrameDone() {
            return addToFrameRan;
        }

        public void setAddToFrameRan() {
            addToFrameRan = true;
        }

        public JTable getDataTable() {
            return dataTable;
        }

        protected void addToBottomBox(JComponent comp) {
            try {
                bottomBox.add(Box.createHorizontalStrut(BOTTOM_STRUT_WIDTH), bottomBoxIndex);
                ++bottomBoxIndex;
                bottomBox.add(comp, bottomBoxIndex);
                ++bottomBoxIndex;
            } catch (java.lang.IllegalArgumentException ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
        }

        protected void dispose() {
            if (dataTable !=null ) {
                dataTable.getColumnModel().removeColumnModelListener(this);
            }
            if (dataModel != null) {
                dataModel.stopPersistingTable(dataTable);
                dataModel.dispose();
            }
            dataModel = null;
            dataTable = null;
            dataScroll = null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractTableTabAction.class);

}
