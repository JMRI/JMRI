package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import jmri.*;
import jmri.swing.RowSorterUtil;
import jmri.util.AlphanumComparator;

public abstract class AbstractTableTabAction<E extends NamedBean> extends AbstractTableAction<E> {

    protected JTabbedPane dataTabs;
    protected boolean init = false;

    public AbstractTableTabAction(String s) {
        super(s);
    }

    @Override
    protected void createModel() {
        dataTabs = new JTabbedPane();
        Manager<E> mgr = getManager();
        if (mgr instanceof jmri.managers.AbstractProxyManager) {
            // build the list, with default at start and internal at end (if present)
            jmri.managers.AbstractProxyManager<E> proxy = (jmri.managers.AbstractProxyManager<E>) mgr;

            tabbedTableArray.add(new TabbedTableItem<>(
                Bundle.getMessage("All"), true, mgr, getNewTableAction("All"))); // NOI18N

            proxy.getDisplayOrderManagerList().stream().map(manager -> {
                String manuName = manager.getMemo().getUserName();
                return new TabbedTableItem<>(
                    manuName, true, manager, getNewTableAction(manuName)); // connection name to display in Tab
            }).forEachOrdered(itemModel -> tabbedTableArray.add(itemModel));

        } else {
            Manager<E> man = getManager();
            String manuName = ( man!=null ? man.getMemo().getUserName() : "Unknown Manager");
            tabbedTableArray.add(new TabbedTableItem<>(manuName, true, getManager(), getNewTableAction(manuName)));
        }
        for (int x = 0; x < tabbedTableArray.size(); x++) {
            AbstractTableAction<E> table = tabbedTableArray.get(x).getAAClass();
            table.addToPanel(this);
            dataTabs.addTab(tabbedTableArray.get(x).getItemString(),  tabbedTableArray.get(x).getPanel());
        }
        dataTabs.addChangeListener((ChangeEvent evt) -> setMenuBar(f));
        init = true;
    }

    @Override
    protected abstract Manager<E> getManager();

    protected abstract AbstractTableAction<E> getNewTableAction(String choice);

    @Override
    public JComponent getPanel() {
        if (!init) {
            createModel();
        }
        return dataTabs;
    }

    protected ArrayList<TabbedTableItem<E>> tabbedTableArray = new ArrayList<>();

    @Override
    protected void setTitle() {
        //atf.setTitle("multiple sensors");
    }

    @Override
    protected abstract String helpTarget();

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
        tabbedTableArray.forEach( table -> {
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

    protected static class TabbedTableItem<E extends NamedBean> extends AbstractTableAction.TableItem<E> {  // E comes from the parent

        final String itemText;

        final JPanel bottomBox;
        private boolean addToFrameRan = false;
        final Manager<E> manager;

        private boolean standardModel = true;

        private final JPanel tabPanel;

        @SuppressWarnings("unchecked")
        public TabbedTableItem(String choice, boolean stdModel, Manager<E> manager, @Nonnull AbstractTableAction<E> tableAction) {

            super(tableAction);

            itemText = choice;
            standardModel = stdModel;
            this.manager = manager;

            //If a panel model is used, it should really add to the bottom box
            //but it can be done this way if required.
            tabPanel = new JPanel();
            tabPanel.setLayout(new BorderLayout());

            bottomBox = new JPanel();
            bottomBox.setLayout(new jmri.util.swing.WrapLayout(jmri.util.swing.WrapLayout.LEFT,20,5));

            if (stdModel) {
                createDataModel();
            } else {
                addPanelModel();
            }
        }

        @SuppressWarnings("unchecked")
        final void createDataModel() {
            tableAction.setManager(manager);
            dataModel = tableAction.getTableDataModel();
            TableRowSorter<BeanTableDataModel<E>> sorter = new TableRowSorter<>(dataModel);
            dataTable = dataModel.makeJTable(dataModel.getMasterClassName() + ":" + getItemString(), dataModel, sorter);
            JScrollPane dataScroll = new JScrollPane(dataTable);

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
            dataScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            dataScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            tabPanel.add(dataScroll, BorderLayout.CENTER);
            tabPanel.add(bottomBox, BorderLayout.SOUTH);

            includeAddButton(tableAction.includeAddButton());

            includePropertyCheckBox();

        }

        final void addPanelModel() {
            tabPanel.add(tableAction.getPanel(), BorderLayout.CENTER);
            tabPanel.add(bottomBox, BorderLayout.SOUTH);
        }

        public boolean getStandardTableModel() {
            return standardModel;
        }

        public String getItemString() {
            return itemText;
        }

        public JComponent getPanel() {
            return tabPanel;
        }

        public boolean getAdditionsToFrameDone() {
            return addToFrameRan;
        }

        public void setAddToFrameRan() {
            addToFrameRan = true;
        }

        @Override
        protected void addToBottomBox(JComponent comp) {
            bottomBox.add(comp);
        }

    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractTableTabAction.class);

}
