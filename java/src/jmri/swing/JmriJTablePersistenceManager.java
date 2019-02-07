package jmri.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimerTask;
import javax.annotation.Nonnull;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.spi.PreferencesManager;
import jmri.util.jdom.JDOMUtil;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import jmri.util.swing.XTableColumnModel;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link JTablePersistenceManager}. The column
 * preferredWidth retained for a column is the
 * {@link TableColumn#getPreferredWidth()}, since this preferredWidth is
 * available before the table column is rendered by Swing.
 *
 * @author Randall Wood Copyright (C) 2016, 2018
 */
@ServiceProvider(service = PreferencesManager.class)
public class JmriJTablePersistenceManager extends AbstractPreferencesManager implements JTablePersistenceManager, PropertyChangeListener {

    protected final HashMap<String, JTableListener> listeners = new HashMap<>();
    protected final HashMap<String, HashMap<String, TableColumnPreferences>> columns = new HashMap<>();
    protected final HashMap<String, List<SortKey>> sortKeys = new HashMap<>();
    private boolean paused = false;
    private boolean dirty = false;
    public final String PAUSED = "paused";
    public final static String TABLES_NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/table-details-4-3-5.xsd"; // NOI18N
    public final static String TABLES_ELEMENT = "tableDetails"; // NOI18N
    public final static String SORT_ORDER = "sortOrder"; // NOI18N
    private final static Logger log = LoggerFactory.getLogger(JmriJTablePersistenceManager.class);

    /**
     * {@inheritDoc}
     * <p>
     * Persisting a table that is already persisted may cause the persistence
     * state to be updated, but will not cause additional listeners to be added
     * to the table.
     */
    @Override
    public void persist(@Nonnull JTable table, boolean resetState) throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(table.getName(), "Table name must be nonnull");
        if (this.listeners.containsKey(table.getName()) && !this.listeners.get(table.getName()).getTable().equals(table)) {
            throw new IllegalArgumentException("Table name must be unique");
        }
        if (resetState) {
            this.resetState(table);
        }
        if (!this.listeners.containsKey(table.getName())) {
            JTableListener listener = new JTableListener(table, this);
            this.listeners.put(table.getName(), listener);
            if (!Arrays.asList(table.getPropertyChangeListeners()).contains(this)) {
                table.addPropertyChangeListener(this);
                table.addPropertyChangeListener(listener);
                TableColumnModel model = table.getColumnModel();
                model.addColumnModelListener(listener);
                RowSorter<? extends TableModel> sorter = table.getRowSorter();
                if (sorter != null) {
                    sorter.addRowSorterListener(listener);
                }
                Enumeration<TableColumn> e = this.getColumns(model);
                List<Object> columnIds = new ArrayList<>();
                while (e.hasMoreElements()) {
                    TableColumn column = e.nextElement();
                    column.addPropertyChangeListener(listener);
                    Object columnId = column.getIdentifier();
                    if (columnId == null || columnId.toString().isEmpty()) {
                        log.error("Columns in table {} have empty or null identities; saving table state will not be reliable.", table.getName());
                    } else if (columnIds.contains(columnId)) {
                        log.error("Columns in table {} share the identity \"{}\"; saving table state will not be reliable.", table.getName(), columnId);
                    } else {
                        columnIds.add(columnId);
                    }
                }
                if (log.isDebugEnabled() && this.getColumnCount(model) != columnIds.size()) {
                    log.debug("Saving table state for table {} will not be reliable.", table.getName(), new Exception());
                }
            }
        }
        if (this.columns.get(table.getName()) == null) {
            this.cacheState(table);
        }
    }

    @Override
    public void stopPersisting(JTable table) {
        Objects.requireNonNull(table.getName(), "table name must be nonnull");
        JTableListener listener = this.listeners.remove(table.getName());
        table.removePropertyChangeListener(this);
        table.removePropertyChangeListener(listener);
        table.getColumnModel().removeColumnModelListener(listener);
        RowSorter<? extends TableModel> sorter = table.getRowSorter();
        if (sorter != null) {
            sorter.removeRowSorterListener(listener);
        }
        Enumeration<TableColumn> e = this.getColumns(table.getColumnModel());
        while (e.hasMoreElements()) {
            TableColumn column = e.nextElement();
            column.removePropertyChangeListener(listener);
        }
    }

    @Override
    public void clearState(JTable table) {
        Objects.requireNonNull(table.getName(), "table name must be nonnull");
        this.columns.remove(table.getName());
        this.dirty = true;
    }

    @Override
    public void cacheState(JTable table) {
        Objects.requireNonNull(table.getName(), "table name must be nonnull");
        TableColumnModel model = table.getColumnModel();
        Objects.requireNonNull(model, "table " + table.getName() + " has a null columnModel");
        RowSorter<? extends TableModel> sorter = table.getRowSorter();
        boolean isXModel = model instanceof XTableColumnModel;
        Enumeration<TableColumn> e = this.getColumns(table.getColumnModel());
        while (e.hasMoreElements()) {
            TableColumn column = e.nextElement();
            String name = column.getIdentifier().toString();
            int index = column.getModelIndex();
            if (isXModel) {
                index = ((XTableColumnModel) model).getColumnIndex(column.getIdentifier(), false);
            }
            int width = column.getPreferredWidth();
            boolean hidden = false;
            if (isXModel) {
                hidden = !((XTableColumnModel) model).isColumnVisible(column);
            }
            SortOrder sorted = SortOrder.UNSORTED;
            if (sorter != null) {
                sorted = RowSorterUtil.getSortOrder(sorter, index);
                log.trace("Column {} (model index {}) is {}", name, index, sorted);
            }
            this.setPersistedState(table.getName(), name, index, width, sorted, hidden);
        }
        if (sorter != null) {
            this.sortKeys.put(table.getName(), new ArrayList<>(sorter.getSortKeys()));
        }
        this.dirty = true;
    }

    @Override
    public void resetState(JTable table) {
        Objects.requireNonNull(table.getName(), "table name must be nonnull");
        boolean persisting = this.listeners.containsKey(table.getName());
        // while setting table state, don't listen to changes in table state
        this.stopPersisting(table);
        TableColumnModel model = table.getColumnModel();
        Objects.requireNonNull(model, "table " + table.getName() + " has a null columnModel");
        RowSorter<? extends TableModel> sorter = table.getRowSorter();
        boolean isXModel = model instanceof XTableColumnModel;
        Map<Integer, String> indexes = new HashMap<>();
        if (this.columns.get(table.getName()) == null) {
            this.columns.put(table.getName(), new HashMap<>());
        }
        this.columns.get(table.getName()).entrySet().stream().forEach((entry) -> {
            int index = entry.getValue().getOrder();
            indexes.put(index, entry.getKey());
        });
        // order columns
        int count = this.getColumnCount(model);
        for (int i = 0; i < count; i++) {
            String name = indexes.get(i);
            if (name != null) {
                int dataModelIndex = -1;
                for (int j = 0; j < count; j++) {
                    Object identifier = ((isXModel) ? ((XTableColumnModel) model).getColumn(j, false) : model.getColumn(j)).getIdentifier();
                    if (identifier != null && identifier.equals(name)) {
                        dataModelIndex = j;
                        break;
                    }
                }
                if (dataModelIndex != -1 && (dataModelIndex != i)) {
                    if (isXModel) {
                        ((XTableColumnModel) model).moveColumn(dataModelIndex, i, false);
                    } else {
                        model.moveColumn(dataModelIndex, i);
                    }
                }
            }
        }
        // configure columns
        Enumeration<TableColumn> e = this.getColumns(table.getColumnModel());
        while (e.hasMoreElements()) {
            TableColumn column = e.nextElement();
            String name = column.getIdentifier().toString();
            TableColumnPreferences preferences = this.columns.get(table.getName()).get(name);
            if (preferences != null) {
                column.setPreferredWidth(preferences.getPreferredWidth());
                if (isXModel) {
                    ((XTableColumnModel) model).setColumnVisible(column, !preferences.getHidden());
                }
            }
        }
        if (sorter != null && this.sortKeys.get(table.getName()) != null) {
            try {
                sorter.setSortKeys(this.sortKeys.get(table.getName()));
            } catch (IllegalArgumentException ex) {
                log.debug("Ignoring IllegalArgumentException \"{}\" as column does not exist.", ex.getMessage());
            }
        }
        if (persisting) {
            this.persist(table);
        }
    }

    /**
     * Set dirty (needs to be saved) state. Protected so that subclasses can
     * manipulate this state.
     *
     * @param dirty true if needs to be saved
     */
    protected void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Get dirty (needs to be saved) state. Protected so that subclasses can
     * manipulate this state.
     *
     * @return true if needs to be saved
     */
    protected boolean isDirty() {
        return this.dirty;
    }

    /**
     * Get dirty (needs to be saved) state. Protected so that subclasses can
     * manipulate this state.
     *
     * @return true if needs to be saved
     * @deprecated since 4.9.7; use {@link #isDirty()} instead
     */
    @Deprecated
    protected boolean getDirty() {
        return this.isDirty();
    }

    @Override
    public void setPaused(boolean paused) {
        boolean old = this.paused;
        this.paused = paused;
        if (paused != old) {
            this.firePropertyChange(PAUSED, old, paused);
        }
        if (!paused && this.dirty) {
            Profile profile = ProfileManager.getDefault().getActiveProfile();
            if (profile != null) {
                this.savePreferences(profile);
            }
        }
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void initialize(Profile profile) throws InitializationException {
        try {
            Element element = JDOMUtil.toJDOMElement(ProfileUtils.getUserInterfaceConfiguration(ProfileManager.getDefault().getActiveProfile())
                    .getConfigurationFragment(TABLES_ELEMENT, TABLES_NAMESPACE, false));
            element.getChildren("table").stream().forEach((table) -> {
                String tableName = table.getAttributeValue("name");
                int sortColumn = -1;
                SortOrder sortOrder = SortOrder.UNSORTED;
                Element sortElement = table.getChild(SORT_ORDER);
                if (sortElement != null) {
                    List<SortKey> keys = new ArrayList<>();
                    for (Element sortKey : sortElement.getChildren()) {
                        sortOrder = SortOrder.valueOf(sortKey.getAttributeValue(SORT_ORDER));
                        try {
                            sortColumn = sortKey.getAttribute("column").getIntValue();
                            SortKey key = new SortKey(sortColumn, sortOrder);
                            keys.add(key);
                        } catch (DataConversionException ex) {
                            log.error("Unable to get sort column as integer");
                        }
                    }
                    this.sortKeys.put(tableName, keys);
                }
                log.debug("Table {} column {} is sorted {}", tableName, sortColumn, sortOrder);
                for (Element column : table.getChild("columns").getChildren()) {
                    String columnName = column.getAttribute("name").getValue();
                    int order = -1;
                    int width = -1;
                    boolean hidden = false;
                    try {
                        if (column.getAttributeValue("order") != null) {
                            order = column.getAttribute("order").getIntValue();
                        }
                        if (column.getAttributeValue("width") != null) {
                            width = column.getAttribute("width").getIntValue();
                        }
                        if (column.getAttribute("hidden") != null) {
                            hidden = column.getAttribute("hidden").getBooleanValue();
                        }
                    } catch (DataConversionException ex) {
                        log.error("Unable to parse column \"{}\"", columnName);
                        continue;
                    }
                    if (sortColumn == order) {
                        this.setPersistedState(tableName, columnName, order, width, sortOrder, hidden);
                    } else {
                        this.setPersistedState(tableName, columnName, order, width, SortOrder.UNSORTED, hidden);
                    }
                }
            });
        } catch (NullPointerException ex) {
            log.info("Table preferences not found.\nThis is expected on the first time the \"{}\" profile is used on this computer.",
                    ProfileManager.getDefault().getActiveProfileName());
        }
        this.setInitialized(profile, true);
    }

    @Override
    public synchronized void savePreferences(Profile profile) {
        log.debug("Saving preferences (dirty={})...", this.dirty);
        Element element = new Element(TABLES_ELEMENT, TABLES_NAMESPACE);
        if (!this.columns.isEmpty()) {
            this.columns.entrySet().stream().map((entry) -> {
                Element table = new Element("table").setAttribute("name", entry.getKey());
                Element columnsElement = new Element("columns");
                entry.getValue().entrySet().stream().map((column) -> {
                    Element columnElement = new Element("column").setAttribute("name", column.getKey());
                    if (column.getValue().getOrder() != -1) {
                        columnElement.setAttribute("order", Integer.toString(column.getValue().getOrder()));
                    }
                    if (column.getValue().getPreferredWidth() != -1) {
                        columnElement.setAttribute("width", Integer.toString(column.getValue().getPreferredWidth()));
                    }
                    columnElement.setAttribute("hidden", Boolean.toString(column.getValue().getHidden()));
                    return columnElement;
                }).forEach((columnElement) -> {
                    columnsElement.addContent(columnElement);
                });
                table.addContent(columnsElement);
                List<SortKey> keys = this.sortKeys.get(entry.getKey());
                if (keys != null) {
                    Element sorter = new Element(SORT_ORDER);
                    keys.stream().forEach((key) -> {
                        sorter.addContent(new Element("sortKey")
                                .setAttribute("column", Integer.toString(key.getColumn()))
                                .setAttribute(SORT_ORDER, key.getSortOrder().name())
                        );
                    });
                    table.addContent(sorter);
                }
                return table;
            }).forEach((table) -> {
                element.addContent(table);
            });
        }
        try {
            ProfileUtils.getUserInterfaceConfiguration(ProfileManager.getDefault().getActiveProfile())
                    .putConfigurationFragment(JDOMUtil.toW3CElement(element), false);
        } catch (JDOMException ex) {
            log.error("Unable to save user preferences", ex);
        }
        this.dirty = false;
    }

    @Override
    @Nonnull
    public Set<Class<?>> getProvides() {
        Set<Class<?>> provides = super.getProvides();
        provides.add(JTablePersistenceManager.class);
        return provides;
    }

    /**
     * Transition support for the standard {@link jmri.UserPreferencesManager}
     * instance (a {@link jmri.managers.JmriUserPreferencesManager}) so it does
     * not need to maintain separate knowledge of table column state.
     *
     * @param table  the table name
     * @param column the column name
     * @param order  order of the column
     * @param width  column preferredWidth
     * @param sort   how the column is sorted
     * @param hidden true if column is hidden
     * @throws NullPointerException if either name is null
     * @deprecated since 4.5.2; not to be removed; used by
     * {@link jmri.managers.configurexml.DefaultUserMessagePreferencesXml} to
     * allow tabled preferences from JMRI 4.4 and earlier to be read when a user
     * is upgrading to a newer version; not be used elsewhere
     */
    @Deprecated
    public void setTableColumnPreferences(String table, String column, int order, int width, SortOrder sort, boolean hidden) {
        Objects.requireNonNull(table, "table name must be nonnull");
        if (sort != SortOrder.UNSORTED) {
            List<SortKey> keys = new ArrayList<>();
            keys.add(new SortKey(order, sort));
            this.sortKeys.put(table, keys);
        }
        this.setPersistedState(table, column, order, width, sort, hidden);
    }

    /**
     * Set the persisted state for the given column in the given table. The
     * persisted state is not saved until
     * {@link #savePreferences(jmri.profile.Profile)} is called.
     *
     * @param table  the table name
     * @param column the column name
     * @param order  order of the column
     * @param width  column preferredWidth
     * @param sort   how the column is sorted
     * @param hidden true if column is hidden
     * @throws NullPointerException if either name is null
     */
    protected void setPersistedState(@Nonnull String table, @Nonnull String column, int order, int width, SortOrder sort, boolean hidden) {
        Objects.requireNonNull(table, "table name must be nonnull");
        Objects.requireNonNull(column, "column name must be nonnull");
        if (!this.columns.containsKey(table)) {
            this.columns.put(table, new HashMap<>());
        }
        HashMap<String, TableColumnPreferences> columnPrefs = this.columns.get(table);
        columnPrefs.put(column, new TableColumnPreferences(order, width, sort, hidden));
        this.dirty = true;
    }

    @Override
    public boolean isPersistenceDataRetained(JTable table) {
        Objects.requireNonNull(table, "Table must be non-null");
        return this.isPersistenceDataRetained(table.getName());
    }

    @Override
    public boolean isPersistenceDataRetained(String name) {
        Objects.requireNonNull(name, "Table name must be non-null");
        return this.columns.containsKey(name);
    }

    @Override
    public boolean isPersisting(JTable table) {
        Objects.requireNonNull(table, "Table must be non-null");
        return this.isPersisting(table.getName());
    }

    @Override
    public boolean isPersisting(String name) {
        Objects.requireNonNull(name, "Table name must be non-null");
        return this.listeners.containsKey(name);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) { // NOI18N
            String oldName = (String) evt.getOldValue();
            String newName = (String) evt.getNewValue();
            if (oldName != null && !this.listeners.containsKey(newName)) {
                if (newName != null) {
                    this.listeners.put(newName, this.listeners.get(oldName));
                    this.columns.put(newName, this.columns.get(oldName));
                } else {
                    this.stopPersisting((JTable) evt.getSource());
                }
                this.listeners.remove(oldName);
                this.columns.remove(oldName);
                this.dirty = true;
            }
        }
    }

    /**
     * Get all columns in the column model for the table. Includes hidden
     * columns if the model is an instance of
     * {@link jmri.util.swing.XTableColumnModel}.
     *
     * @param model the column model to get columns from
     * @return an enumeration of the columns
     */
    private Enumeration<TableColumn> getColumns(TableColumnModel model) {
        if (model instanceof XTableColumnModel) {
            return ((XTableColumnModel) model).getColumns(false);
        }
        return model.getColumns();
    }

    /**
     * Get a count of all columns in the column model for the table. Includes
     * hidden columns if the model is an instance of
     * {@link jmri.util.swing.XTableColumnModel}.
     *
     * @param model the column model to get the count from
     * @return the number of columns in the model
     */
    private int getColumnCount(TableColumnModel model) {
        if (model instanceof XTableColumnModel) {
            return ((XTableColumnModel) model).getColumnCount(false);
        }
        return model.getColumnCount();
    }

    /**
     * Handler for individual column preferences.
     */
    public final static class TableColumnPreferences {

        int order;
        int preferredWidth;
        SortOrder sort;
        boolean hidden;

        public TableColumnPreferences(int order, int preferredWidth, SortOrder sort, boolean hidden) {
            this.order = order;
            this.preferredWidth = preferredWidth;
            this.sort = sort;
            this.hidden = hidden;
        }

        public int getOrder() {
            return this.order;
        }

        public int getPreferredWidth() {
            return this.preferredWidth;
        }

        public SortOrder getSort() {
            return this.sort;
        }

        public boolean getHidden() {
            return this.hidden;
        }
    }

    protected final static class JTableListener implements PropertyChangeListener, RowSorterListener, TableColumnModelListener {

        private final JTable table;
        private final JmriJTablePersistenceManager manager;

        public JTableListener(JTable table, JmriJTablePersistenceManager manager) {
            this.table = table;
            this.manager = manager;
        }

        private JTable getTable() {
            return this.table;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof JTable) {
                switch (evt.getPropertyName()) {
                    case "name": // NOI18N
                        break;
                    case "Frame.active": // NOI18N
                        break;
                    case "ancestor": // NOI18N
                        break;
                    case "selectionForeground": // NOI18N
                        break;
                    case "selectionBackground": // NOI18N
                        break;
                    case "JComponent_TRANSFER_HANDLER": // NOI18N
                        break;
                    case "transferHandler": // NOI18N
                        break;
                    default:
                        // log unrecognized events
                        log.trace("Got propertyChange {} for table {} (\"{}\" -> \"{}\")", evt.getPropertyName(), this.table.getName(), evt.getOldValue(), evt.getNewValue());
                }
            } else if (evt.getSource() instanceof TableColumn) {
                TableColumn column = ((TableColumn) evt.getSource());
                String name = column.getIdentifier().toString();
                switch (evt.getPropertyName()) {
                    case "preferredWidth": // NOI18N
                        this.saveState();
                        break;
                    case "width": // NOI18N
                        break;
                    default:
                        // log unrecognized events
                        log.trace("Got propertyChange {} for column {} (\"{}\" -> \"{}\")", evt.getPropertyName(), name, evt.getOldValue(), evt.getNewValue());
                }
            }
        }

        @Override
        public void sorterChanged(RowSorterEvent e) {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                this.saveState();
                log.debug("Sort order changed for {}", this.table.getName());
            }
        }

        @Override
        public void columnAdded(TableColumnModelEvent e) {
            this.saveState();
            log.debug("Got columnAdded for {} ({} -> {})", this.table.getName(), e.getFromIndex(), e.getToIndex());
        }

        @Override
        public void columnRemoved(TableColumnModelEvent e) {
            this.manager.clearState(this.table); // deletes column data from xml file
            this.saveState();
            log.debug("Got columnRemoved for {} ({} -> {})", this.table.getName(), e.getFromIndex(), e.getToIndex());
        }

        @Override
        public void columnMoved(TableColumnModelEvent e) {
            if (e.getFromIndex() != e.getToIndex()) {
                this.saveState();
                log.debug("Got columnMoved for {} ({} -> {})", this.table.getName(), e.getFromIndex(), e.getToIndex());
            }
        }

        @Override
        public void columnMarginChanged(ChangeEvent e) {
            // do nothing - we don't retain margins
            log.trace("Got columnMarginChanged for {}", this.table.getName());
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {
            // do nothing - we don't retain selections
            log.trace("Got columnSelectionChanged for {} ({} -> {})", this.table.getName(), e.getFirstIndex(), e.getLastIndex());
        }

        TimerTask delay;
        
        protected void cancelDelay() {
            if (this.delay != null) {
                this.delay.cancel(); // cancel complete before dropping reference
                this.delay = null;
            }
        }

        /**
         * Saves the state after a 1/2 second delay. Every time the listener
         * triggers this method any pending save is canceled and a new delay is
         * created. This is intended to prevent excessive writes to disk while
         * (for example) a column is being resized or moved. Calling
         * {@link JmriJTablePersistenceManager#savePreferences(jmri.profile.Profile)}
         * is not subject to this timer.
         */
        private void saveState() {
            cancelDelay();
            jmri.util.TimerUtil.schedule(delay = new TimerTask() {
                @Override
                public void run() {
                    JTableListener.this.manager.cacheState(JTableListener.this.table);
                    if (!JTableListener.this.manager.isPaused() && JTableListener.this.manager.isDirty()) {
                        JTableListener.this.manager.savePreferences(ProfileManager.getDefault().getActiveProfile());
                    }
                    JTableListener.this.cancelDelay();
                }
            }, 500); // milliseconds
        }
    }
}
