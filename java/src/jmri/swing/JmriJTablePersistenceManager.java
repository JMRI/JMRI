package jmri.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import jmri.UserPreferencesManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.jdom.JDOMUtil;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link JTablePersistenceManager}.
 *
 * @author Randall Wood Copyright (C) 2016
 */
public class JmriJTablePersistenceManager extends AbstractPreferencesManager implements JTablePersistenceManager, PropertyChangeListener, RowSorterListener, TableColumnModelListener {

    private final HashMap<String, JTable> tables = new HashMap<>();
    private final HashMap<String, HashMap<String, TableColumnPreferences>> columns = new HashMap<>();
    private boolean paused = false;
    private boolean dirty = false;
    UserPreferencesManager manager = null;
    public final String PAUSED = "paused";
    public final static String TABLES_NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/table-details-4-3-5.xsd"; // NOI18N
    public final static String TABLES_ELEMENT = "tableDetails"; // NOI18N
    private final static Logger log = LoggerFactory.getLogger(JmriJTablePersistenceManager.class);

    @Override
    public void persist(@Nonnull JTable table) throws IllegalArgumentException {
        if (table.getName() == null) {
            throw new IllegalArgumentException("Table name must be nonnull");
        }
        if (this.tables.containsKey(table.getName()) && !this.tables.get(table.getName()).equals(table)) {
            throw new IllegalArgumentException("Table name must be unique");
        }
        this.tables.put(table.getName(), table);
        if (!Arrays.asList(table.getPropertyChangeListeners()).contains(this)) {
            table.addPropertyChangeListener(this);
            table.getColumnModel().addColumnModelListener(this);
            RowSorter sorter = table.getRowSorter();
            if (sorter != null) {
                sorter.addRowSorterListener(this);
            }
        }
    }

    @Override
    public void stopPersisting(JTable table) {
        Objects.requireNonNull(table.getName(), "table name must be nonnull");
        this.tables.remove(table.getName());
        table.removePropertyChangeListener(this);
        table.getColumnModel().removeColumnModelListener(this);
        RowSorter sorter = table.getRowSorter();
        if (sorter != null) {
            sorter.removeRowSorterListener(this);
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
        this.dirty = true;
    }

    @Override
    public void setPaused(boolean paused) {
        boolean old = this.paused;
        this.paused = paused;
        if (paused != old) {
            this.firePropertyChange(PAUSED, old, paused);
        }
        if (!paused && this.dirty) {
            this.savePreferences(ProfileManager.getDefault().getActiveProfile());
        }
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void initialize(Profile profile) throws InitializationException {
        this.setInitialized(profile, true);
    }

    @Override
    public synchronized void savePreferences(Profile profile) {
        Element element = new Element(TABLES_ELEMENT, TABLES_NAMESPACE);
        if (!this.columns.isEmpty()) {
            this.columns.entrySet().stream().map((entry) -> {
                Element table = new Element("table").setAttribute("name", entry.getKey());
                RowSorter.SortKey sortKey = new RowSorter.SortKey(0, SortOrder.UNSORTED);
                Element columnsElement = new Element("columns");
                for (Map.Entry<String, TableColumnPreferences> column : entry.getValue().entrySet()) {
                    Element columnElement = new Element("column").setAttribute("name", column.getKey());
                    if (column.getValue().getOrder() != -1) {
                        columnElement.setAttribute("order", Integer.toString(column.getValue().getOrder()));
                    }
                    if (column.getValue().getWidth() != -1) {
                        columnElement.setAttribute("width", Integer.toString(column.getValue().getWidth()));
                    }
                    columnElement.setAttribute("hidden", Boolean.toString(column.getValue().getHidden()));
                    columnsElement.addContent(columnElement);
                    if (column.getValue().getSort() != SortOrder.UNSORTED) {
                        sortKey = new RowSorter.SortKey(column.getValue().getOrder(), column.getValue().getSort());
                    }
                }
                table.addContent(columnsElement);
                if (sortKey.getSortOrder() != SortOrder.UNSORTED) {
                    table.addContent(new Element("sortOrder").addContent(new Element("sortKey")
                            .setAttribute("column", Integer.toString(sortKey.getColumn()))
                            .setAttribute("sortOrder", sortKey.getSortOrder().name())
                    ));
                }
                return table;
            }).forEach((table) -> {
                element.addContent(table);
            });
        }
        this.saveElement(element);
        this.dirty = false;
    }

    protected void saveElement(Element element) {
        log.trace("Saving {} element.", element.getName());
        try {
            ProfileUtils.getUserInterfaceConfiguration(ProfileManager.getDefault().getActiveProfile()).putConfigurationFragment(JDOMUtil.toW3CElement(element), false);
        } catch (JDOMException ex) {
            log.error("Unable to save user preferences", ex);
        }
    }

    /**
     * Transition support for {@link jmri.UserPreferencesManager} instances so
     * they do not need to maintain separate knowledge of table column state.
     *
     * @param table  the requested table name
     * @param column the requested column name
     * @return the preferences for the column
     * @deprecated since 4.5.2
     */
    @Deprecated
    @CheckForNull
    public TableColumnPreferences getTableColumnPreferences(@Nonnull String table, @Nonnull String column) {
        HashMap<String, TableColumnPreferences> map = this.columns.get(table);
        if (map != null) {
            return map.get(column);
        }
        return null;
    }

    /**
     * Transition support for {@link jmri.UserPreferencesManager} instances so
     * they do not need to maintain separate knowledge of table column state.
     *
     * @param table  the table name
     * @param column the column name
     * @param order  order of the column
     * @param width  column width
     * @param sort   how the column is sorted
     * @param hidden true if column is hidden
     * @deprecated since 4.5.2
     */
    @Deprecated
    public void setTableColumnPreferences(String table, String column, int order, int width, SortOrder sort, boolean hidden) {
        if (!this.columns.containsKey(table)) {
            this.columns.put(table, new HashMap<>());
        }
        HashMap<String, TableColumnPreferences> columnPrefs = this.columns.get(table);
        columnPrefs.put(column, new TableColumnPreferences(order, width, sort, hidden));
        this.savePreferences(ProfileManager.getDefault().getActiveProfile());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) { // NOI18N
            if (evt.getOldValue() != null) {

            }
        }
    }

    @Override
    public void sorterChanged(RowSorterEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public final static class TableColumnPreferences {

        int order;
        int width;
        SortOrder sort;
        boolean hidden;

        public TableColumnPreferences(int order, int width, SortOrder sort, boolean hidden) {
            this.order = order;
            this.width = width;
            this.sort = sort;
            this.hidden = hidden;
        }

        public int getOrder() {
            return this.order;
        }

        public int getWidth() {
            return this.width;
        }

        public SortOrder getSort() {
            return this.sort;
        }

        public boolean getHidden() {
            return this.hidden;
        }
    }
}
