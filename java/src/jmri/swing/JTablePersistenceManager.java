package jmri.swing;

import javax.annotation.Nonnull;
import javax.swing.JTable;

/**
 * Manager for ensuring that {@link javax.swing.JTable} user interface state is
 * persisted.
 * <p>
 * JTable user interface state that can be persisted includes:</p>
 * <ul>
 * <li>row sort order (if the table has a non-null
 * {@link javax.swing.RowSorter})</li>
 * <li>column order</li>
 * <li>column visibility</li>
 * <li>column width</li>
 * </ul>
 * <p>
 * Row sort order is only persisted for JTables that implement the
 * {@link javax.swing.RowSorter} for sorting. Tables using the deprecated
 * {@link jmri.util.com.sun.TableSorter} will not have row sort order persisted.
 * If the RowSorter is null, the row sorting will not be persisted.</p>
 * <p>
 * Column attributes (order, visibility, and width) are persisted by listening
 * to changes in the {@link javax.swing.table.TableColumnModel} of the table.
 *
 * @author Randall Wood Copyright (C) 2016
 */
public interface JTablePersistenceManager {

    /**
     * Persist the user interface state for a table. The name returned by
     * {@link javax.swing.JComponent#getName()} is used to persist the table, so
     * ensure the name is set such that it can be retrieved by the same name in
     * a later JMRI execution.
     *
     * @param table the table to persist
     * @throws IllegalArgumentException if another table instance is already
     *                                  persisted by the same name
     */
    public void persist(@Nonnull JTable table) throws IllegalArgumentException;

    /**
     * Stop persisting the table. This does not clear the persistence state, but
     * merely causes the JTablePersistenceManager to stop listening to the
     * table.
     *
     * @param table the table to stop persisting
     */
    public void stopPersisting(@Nonnull JTable table);

    /**
     * Forget persistent state for a table.
     *
     * @param table the table to forget
     */
    public void clearState(@Nonnull JTable table);

    /**
     * Pause saving persistence data to storage. If setting paused to false,
     * pending persistence data is written immediately.
     *
     * @param paused true if saving persistence data should be paused; false
     *               otherwise.
     */
    public void setPaused(boolean paused);

    /**
     * Determine if saving persistence data is paused.
     *
     * @return true if saving persistence data is paused; false otherwise.
     */
    public boolean isPaused();
}
