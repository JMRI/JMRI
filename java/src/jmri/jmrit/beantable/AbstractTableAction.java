package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.ProxyManager;
import jmri.UserPreferencesManager;
import jmri.SystemConnectionMemo;
import jmri.jmrix.SystemConnectionMemoManager;
import jmri.swing.ManagerComboBox;
import jmri.util.swing.TriStateJCheckBox;
import jmri.util.swing.XTableColumnModel;

/**
 * Swing action to create and register a NamedBeanTable GUI.
 *
 * @param <E> type of NamedBean supported in this table
 * @author Bob Jacobsen Copyright (C) 2003
 */
public abstract class AbstractTableAction<E extends NamedBean> extends AbstractAction {

    public AbstractTableAction(String actionName) {
        super(actionName);
    }

    public AbstractTableAction(String actionName, Object option) {
        super(actionName);
    }

    protected BeanTableDataModel<E> m;

    /**
     * Create the JTable DataModel, along with the changes for the specific
     * NamedBean type.
     */
    protected abstract void createModel();

    /**
     * Include the correct title.
     */
    protected abstract void setTitle();

    protected BeanTableFrame<E> f;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();
        TableRowSorter<BeanTableDataModel<E>> sorter = new TableRowSorter<>(m);
        JTable dataTable = m.makeJTable(m.getMasterClassName(), m, sorter);

        // allow reordering of the columns
        dataTable.getTableHeader().setReorderingAllowed(true);

        // create the frame
        f = new BeanTableFrame<E>(m, helpTarget(), dataTable) {

            /**
             * Include an "Add..." button
             */
            @Override
            void extras() {
                
                addBottomButtons(this, dataTable);
            }
        };
        setMenuBar(f); // comes after the Help menu is added by f = new
                       // BeanTableFrame(etc.) in stand alone application
        configureTable(dataTable);
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }

    @SuppressWarnings("unchecked") // revisit Java16+  if dm instanceof BeanTableDataModel<E>
    protected void addBottomButtons(BeanTableFrame<E> ata, JTable dataTable ){

        TableItem<E> ti = new TableItem<>(this);
        ti.setTableFrame(ata);
        ti.includeAddButton(includeAddButton);
        ti.dataTable = dataTable;
        TableModel dm = dataTable.getModel();

        if ( dm instanceof BeanTableDataModel) {
            ti.dataModel = (BeanTableDataModel<E>)dm;
        }
        ti.includePropertyCheckBox();

    }

    /**
     * Notification that column visibility for the JTable has updated.
     * <p>
     * This is overridden by classes which have column visibility Checkboxes on bottom bar.
     * <p>
     *
     * Called on table startup and whenever a column goes hidden / visible.
     *
     * @param colsVisible   array of ALL table columns and their visibility
     *                      status in order of main Table Model, NOT XTableColumnModel.
     */
    protected void columnsVisibleUpdated(boolean[] colsVisible){
        log.debug("columns updated {}",colsVisible);
    }

    public BeanTableDataModel<E> getTableDataModel() {
        createModel();
        return m;
    }

    public void setFrame(@Nonnull BeanTableFrame<E> frame) {
        f = frame;
    }

    public BeanTableFrame<E> getFrame() {
        return f;
    }

    /**
     * Allow subclasses to add to the frame without having to actually subclass
     * the BeanTableDataFrame.
     *
     * @param f the Frame to add to
     */
    public void addToFrame(@Nonnull BeanTableFrame<E> f) {
    }

    /**
     * Allow subclasses to add to the frame without having to actually subclass
     * the BeanTableDataFrame.
     *
     * @param tti the TabbedTableItem to add to
     */
    public void addToFrame(@Nonnull ListedTableFrame.TabbedTableItem<E> tti) {
    }

    /**
     * If the subClass is being included in a greater tabbed frame, then this
     * method is used to add the details to the tabbed frame.
     *
     * @param f AbstractTableTabAction for the containing frame containing these
     *          and other tabs
     */
    public void addToPanel(AbstractTableTabAction<E> f) {
    }

    /**
     * If the subClass is being included in a greater tabbed frame, then this is
     * used to specify which manager the subclass should be using.
     *
     * @param man Manager for this table tab
     */
    protected void setManager(@Nonnull Manager<E> man) {
    }

    /**
     * Get the Bean Manager in use by the TableAction.
     * @return Bean Manager, could be Proxy or normal Manager, may be null.
     */
    @CheckForNull
    protected Manager<E> getManager(){
        return null;
    }

    /**
     * Allow subclasses to alter the frame's Menubar without having to actually
     * subclass the BeanTableDataFrame.
     *
     * @param f the Frame to attach the menubar to
     */
    public void setMenuBar(BeanTableFrame<E> f) {
    }

    public JComponent getPanel() {
        return null;
    }

    /**
     * Perform configuration of the JTable as required by a specific TableAction.
     * @param table The table to configure.
     */
    protected void configureTable(JTable table){
    }

    /**
     * Dispose of the BeanTableDataModel ( if present ),
     * which removes the DataModel property change listeners from Beans.
     */
    public void dispose() {
        if (m != null) {
            m.dispose();
        }
        // should this also dispose of the frame f?
    }

    /**
     * Increments trailing digits of a system/user name (string) I.E. "Geo7"
     * returns "Geo8" Note: preserves leading zeros: "Geo007" returns "Geo008"
     * Also, if no trailing digits, appends "1": "Geo" returns "Geo1"
     *
     * @param name the system or user name string
     * @return the same name with trailing digits incremented by one
     */
    protected @Nonnull String nextName(@Nonnull String name) {
        final String[] parts = name.split("(?=\\d+$)", 2);
        String numString = "0";
        if (parts.length == 2) {
            numString = parts[1];
        }
        final int numStringLength = numString.length();
        final int num = Integer.parseInt(numString) + 1;
        return parts[0] + String.format("%0" + numStringLength + "d", num);
    }

    /**
     * Specify the JavaHelp target for this specific panel.
     *
     * @return a fixed default string "index" pointing to to highest level in
     *         JMRI Help
     */
    protected String helpTarget() {
        return "index"; // by default, go to the top
    }

    public String getClassDescription() {
        return "Abstract Table Action";
    }

    public void setMessagePreferencesDetails() {
        HashMap<Integer, String> options = new HashMap<>(3);
        options.put(0x00, Bundle.getMessage("DeleteAsk"));
        options.put(0x01, Bundle.getMessage("DeleteNever"));
        options.put(0x02, Bundle.getMessage("DeleteAlways"));
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMessageItemDetails(getClassName(),
                "deleteInUse", Bundle.getMessage("DeleteItemInUse"), options, 0x00);
    }

    protected abstract String getClassName();

    /**
     * Test if to include an Add New Button.
     * @return true to include, else false.
     */
    public boolean includeAddButton() {
        return includeAddButton;
    }

    protected boolean includeAddButton = true;

    /**
     * Used with the Tabbed instances of table action, so that the print option
     * is handled via that on the appropriate tab.
     *
     * @param mode         table print mode
     * @param headerFormat messageFormat for header
     * @param footerFormat messageFormat for footer
     */
    public void print(JTable.PrintMode mode, MessageFormat headerFormat, MessageFormat footerFormat) {
        log.error("Printing not handled for {} tables.", m.getBeanType());
    }

    protected abstract void addPressed(ActionEvent e);

    /**
     * Configure the combo box listing managers.
     * Can be placed on Add New pane to select a connection for the new item.
     *
     * @param comboBox     the combo box to configure
     * @param manager      the current manager
     * @param managerClass the implemented manager class for the current
     *                     manager; this is the class used by
     *                     {@link InstanceManager#getDefault(Class)} to get the
     *                     default manager, which may or may not be the current
     *                     manager
     */
    protected void configureManagerComboBox(ManagerComboBox<E> comboBox, Manager<E> manager,
            Class<? extends Manager<E>> managerClass) {
        Manager<E> defaultManager = InstanceManager.getDefault(managerClass);
        // populate comboBox
        if (defaultManager instanceof ProxyManager) {
            comboBox.setManagers(defaultManager);
        } else {
            comboBox.setManagers(manager);
        }
        // set current selection
        if (manager instanceof ProxyManager) {
            UserPreferencesManager upm = InstanceManager.getDefault(UserPreferencesManager.class);
            String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
            String userPref = upm.getComboBoxLastSelection(systemSelectionCombo);
            if ( userPref != null) {
                SystemConnectionMemo memo = SystemConnectionMemoManager.getDefault()
                        .getSystemConnectionMemoForUserName(userPref);
                if (memo!=null) {
                    comboBox.setSelectedItem(memo.get(managerClass));
                } else {
                    ProxyManager<E> proxy = (ProxyManager<E>) manager;
                    comboBox.setSelectedItem(proxy.getDefaultManager());
                }
            } else {
                ProxyManager<E> proxy = (ProxyManager<E>) manager;
                comboBox.setSelectedItem(proxy.getDefaultManager());
            }
        } else {
            comboBox.setSelectedItem(manager);
        }
    }

    /**
     * Remove the Add panel prefixBox listener before disposal.
     * The listener is created when the Add panel is defined.  It persists after the
     * the Add panel has been disposed.  When the next Add is created, AbstractTableAction
     * sets the default connection as the current selection.  This triggers validation before
     * the new Add panel is created.
     * <p>
     * The listener is removed by the controlling table action before disposing of the Add
     * panel after Close or Create.
     * @param prefixBox The prefix combobox that might contain the listener.
     */
    protected void removePrefixBoxListener(ManagerComboBox<E> prefixBox) {
        Arrays.asList(prefixBox.getActionListeners()).forEach((l) -> {
            prefixBox.removeActionListener(l);
        });
    }

    /**
     * Display a warning to user about invalid entry. Needed as entry validation
     * does not disable the Create button when full system name eg "LT1" is entered.
     *
     * @param curAddress address as entered in Add new... pane address field
     * @param ex the exception that occurred
     */
    protected void displayHwError(String curAddress, Exception ex) {
        log.warn("Invalid Entry: {}",ex.getMessage());
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager .class).
                showErrorMessage(Bundle.getMessage("ErrorTitle"),
                        Bundle.getMessage("ErrorConvertHW", curAddress),"" + ex,"",
                        true,false);
    }

    protected static class TableItem<E extends NamedBean> implements TableColumnModelListener {  // E comes from the parent

        BeanTableDataModel<E> dataModel;
        JTable dataTable;
        final AbstractTableAction<E> tableAction;
        BeanTableFrame<E> beanTableFrame;

        void setTableFrame(BeanTableFrame<E> frame){
            beanTableFrame = frame;
        }

        final TriStateJCheckBox propertyVisible =
            new TriStateJCheckBox(Bundle.getMessage("ShowSystemSpecificProperties"));

        public TableItem(@Nonnull AbstractTableAction<E> tableAction) {
            this.tableAction = tableAction;
        }

        @SuppressWarnings("unchecked")
        public AbstractTableAction<E> getAAClass() {
            return tableAction;
        }

        public JTable getDataTable() {
            return dataTable;
        }

        void includePropertyCheckBox() {

            if (dataModel==null) {
                log.error("datamodel for dataTable {} should not be null", dataTable);
                return;
            }

            if (dataModel.getPropertyColumnCount() > 0) {
                propertyVisible.setToolTipText(Bundle.getMessage
                        ("ShowSystemSpecificPropertiesToolTip"));
                addToBottomBox(propertyVisible);
                propertyVisible.addActionListener((ActionEvent e) ->
                    dataModel.setPropertyColumnsVisible(dataTable, propertyVisible.isSelected()));
            }
            fireColumnsUpdated(); // init bottom buttons
            dataTable.getColumnModel().addColumnModelListener(this);

        }

        void includeAddButton(boolean includeAddButton){

            if (includeAddButton) {
                JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));
                addToBottomBox(addButton );
                addButton.addActionListener(tableAction::addPressed);
            }
        }

        protected void addToBottomBox(JComponent comp) {
            if (beanTableFrame != null ) {
                beanTableFrame.addToBottomBox(comp, this.getClass().getName());
            }
        }

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
        }

    }
    
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractTableAction.class);

}
