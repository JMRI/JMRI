package jmri.util.swing;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 * Taken from http://www.stephenkelvin.de/XTableColumnModel/
 * {@code XTableColumnModel} extends the DefaultTableColumnModel . It provides a
 * comfortable way to hide/show columns. Columns keep their positions when
 * hidden and shown again.
 * <p>
 * In order to work with JTable it cannot add any events to
 * {@code TableColumnModelListener}. Therefore hiding a column will result in
 * {@code columnRemoved} event and showing it again will notify listeners of a
 * {@code columnAdded}, and possibly a {@code columnMoved} event. For the same
 * reason the following methods still deal with visible columns only:
 * getColumnCount(), getColumns(), getColumnIndex(), getColumn() There are
 * overloaded versions of these methods that take a parameter
 * {@code onlyVisible} which let's you specify whether you want invisible
 * columns taken into account.
 *
 * @version 0.9 04/03/01
 * @author Stephen Kelvin, mail@StephenKelvin.de
 * @see DefaultTableColumnModel
 */
public class XTableColumnModel extends DefaultTableColumnModel {

    /**
     * Array of TableColumn objects in this model. Holds all column objects,
     * regardless of their visibility
     */
    protected Vector<TableColumn> allTableColumns = new Vector<>();

    /**
     * Creates an extended table column model.
     */
    public XTableColumnModel() {
    }

    /**
     * Sets the visibility of the specified TableColumn. The call is ignored if
     * the TableColumn is not found in this column model or its visibility
     * status did not change.
     * <p>
     *
     * @param column  the column to show/hide
     * @param visible its new visibility status
     */
    // listeners will receive columnAdded()/columnRemoved() event
    public void setColumnVisible(TableColumn column, boolean visible) {
        if (!visible) {
            super.removeColumn(column);
        } else {
            // find the visible index of the column:
            // iterate through both collections of visible and all columns, counting
            // visible columns up to the one that's about to be shown again
            int noVisibleColumns = tableColumns.size();
            int noInvisibleColumns = allTableColumns.size();
            int visibleIndex = 0;

            for (int invisibleIndex = 0; invisibleIndex < noInvisibleColumns; ++invisibleIndex) {
                TableColumn visibleColumn = (visibleIndex < noVisibleColumns ? tableColumns.get(visibleIndex) : null);
                TableColumn testColumn = allTableColumns.get(invisibleIndex);

                if (testColumn == column) {
                    if (visibleColumn != column) {
                        super.addColumn(column);
                        super.moveColumn(tableColumns.size() - 1, visibleIndex);
                    }
                    return;
                }
                if (testColumn == visibleColumn) {
                    ++visibleIndex;
                }
            }
        }
    }

    /**
     * Makes all columns in this model visible
     */
    public void setAllColumnsVisible() {
        int noColumns = allTableColumns.size();

        for (int columnIndex = 0; columnIndex < noColumns; ++columnIndex) {
            TableColumn visibleColumn = (columnIndex < tableColumns.size() ? tableColumns.get(columnIndex) : null);
            TableColumn invisibleColumn = allTableColumns.get(columnIndex);

            if (visibleColumn != invisibleColumn) {
                super.addColumn(invisibleColumn);
                super.moveColumn(tableColumns.size() - 1, columnIndex);
            }
        }
    }

    /**
     * Maps the index of the column in the table model at
     * {@code modelColumnIndex} to the TableColumn object. There may be multiple
     * TableColumn objects showing the same model column, though this is
     * uncommon.
     *
     * @param modelColumnIndex index of column in table model
     * @return the first column, visible or invisible, with the specified index
     *         or null if no such column
     */
    public TableColumn getColumnByModelIndex(int modelColumnIndex) {
        for (int columnIndex = 0; columnIndex < allTableColumns.size(); ++columnIndex) {
            TableColumn column = allTableColumns.get(columnIndex);
            if (column.getModelIndex() == modelColumnIndex) {
                return column;
            }
        }
        return null;
    }

    /**
     * Checks whether the specified column is currently visible.
     *
     * @param aColumn column to check
     * @return visibility of specified column (false if there is no such column
     *         at all. [It's not visible, right?])
     */
    public boolean isColumnVisible(TableColumn aColumn) {
        return (tableColumns.indexOf(aColumn) >= 0);
    }

    /**
     * Append {@code column} to the right of existing columns. Posts
     * {@code columnAdded} event.
     *
     * @param column The column to be added
     * @see #removeColumn
     * @exception IllegalArgumentException if {@code column} is {@code null}
     */
    @Override
    public void addColumn(TableColumn column) {
        allTableColumns.add(column);
        super.addColumn(column);
    }

    /**
     * Removes {@code column} from this column model. Posts
     * {@code columnRemoved} event. Will do nothing if the column is not in this
     * model.
     *
     * @param column the column to be added
     * @see #addColumn
     */
    @Override
    public void removeColumn(TableColumn column) {
        int allColumnsIndex = allTableColumns.indexOf(column);
        if (allColumnsIndex != -1) {
            allTableColumns.remove(allColumnsIndex);
        }
        super.removeColumn(column);
    }

    /**
     * Moves the column from {@code columnIndex} to {@code newIndex}. Posts
     * {@code columnMoved} event. Will not move any columns if
     * {@code columnIndex} equals {@code newIndex}. This method also posts a
     * {@code columnMoved} event to its listeners.
     *
     * @param columnIndex index of column to be moved
     * @param newIndex    new index of the column
     * @exception IllegalArgumentException if either {@code oldIndex} or
     *                                     {@code newIndex} are not in [0,
     *                                     getColumnCount() - 1]
     */
    @Override
    public void moveColumn(int columnIndex, int newIndex) {
        moveColumn(columnIndex, newIndex, true);
    }

    /**
     * Moves the column from {@code columnIndex} to {@code newIndex}. Posts
     * {@code columnMoved} event. Will not move any columns if
     * {@code columnIndex} equals {@code newIndex}. This method also posts a
     * {@code columnMoved} event to its listeners if a visible column moves.
     *
     * @param columnIndex index of column to be moved
     * @param newIndex    new index of the column
     * @param onlyVisible true if this should only move a visible column; false
     *                    to move any column
     * @exception IllegalArgumentException if either {@code oldIndex} or
     *                                     {@code newIndex} are not in [0,
     *                                     getColumnCount(onlyVisible) - 1]
     */
    public void moveColumn(int columnIndex, int newIndex, boolean onlyVisible) {
        if ((columnIndex < 0) || (columnIndex >= getColumnCount(onlyVisible))
                || (newIndex < 0) || (newIndex >= getColumnCount(onlyVisible))) {
            throw new IllegalArgumentException("moveColumn() - Index out of range");
        }

        if (onlyVisible) {
            if (columnIndex != newIndex) {
                // columnIndex and newIndex are indexes of visible columns, so need
                // to get index of column in list of all columns
                int allColumnsColumnIndex = allTableColumns.indexOf(tableColumns.get(columnIndex));
                int allColumnsNewIndex = allTableColumns.indexOf(tableColumns.get(newIndex));

                TableColumn column = allTableColumns.remove(allColumnsColumnIndex);
                allTableColumns.add(allColumnsNewIndex, column);
            }

            super.moveColumn(columnIndex, newIndex);
        } else {
            if (columnIndex != newIndex) {
                // columnIndex and newIndex are indexes of all columns, so need
                // to get index of column in list of visible columns
                int visibleColumnIndex = tableColumns.indexOf(allTableColumns.get(columnIndex));
                int visibleNewIndex = tableColumns.indexOf(allTableColumns.get(newIndex));

                TableColumn column = allTableColumns.remove(columnIndex);
                allTableColumns.add(newIndex, column);
                // call super moveColumn if both indexes are visible
                if (visibleColumnIndex != -1 && visibleNewIndex != -1) {
                    super.moveColumn(visibleColumnIndex, visibleNewIndex);
                }
            }

        }
    }

    /**
     * Returns the total number of columns in this model.
     *
     * @param onlyVisible if set only visible columns will be counted
     * @return the number of columns in the {@code tableColumns} array
     * @see #getColumns
     */
    public int getColumnCount(boolean onlyVisible) {
        return getColumnList(onlyVisible).size();
    }

    /**
     * Returns an {@code Enumeration} of all the columns in the model.
     *
     * @param onlyVisible if set all invisible columns will be missing from the
     *                    enumeration.
     * @return an {@code Enumeration} of the columns in the model
     */
    public Enumeration<TableColumn> getColumns(boolean onlyVisible) {
        return Collections.enumeration(getColumnList(onlyVisible));
    }

    /**
     * Returns the position of the first column whose identifier equals
     * {@code identifier}. Position is the index in all visible columns if
     * {@code onlyVisible} is true or else the index in all columns.
     *
     * @param identifier  the identifier object to search for
     * @param onlyVisible if set searches only visible columns
     *
     * @return the index of the first column whose identifier equals
     *         {@code identifier}
     *
     * @exception IllegalArgumentException if {@code identifier} is
     *                                     {@code null}, or if no
     *                                     {@code TableColumn} has this
     *                                     {@code identifier}
     * @see #getColumn
     */
    public int getColumnIndex(Object identifier, boolean onlyVisible) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier is null");
        }

        Vector<TableColumn> columns = getColumnList(onlyVisible);
        int noColumns = columns.size();
        TableColumn column;

        for (int columnIndex = 0; columnIndex < noColumns; ++columnIndex) {
            column = columns.get(columnIndex);

            if (identifier.equals(column.getIdentifier())) {
                return columnIndex;
            }
        }

        throw new IllegalArgumentException("Identifier not found");
    }

    /**
     * Returns the {@code TableColumn} object for the column at
     * {@code columnIndex}.
     *
     * @param columnIndex the index of the column desired
     * @param onlyVisible if set columnIndex is meant to be relative to all
     *                    visible columns only else it is the index in all
     *                    columns
     *
     * @return the {@code TableColumn} object for the column at
     *         {@code columnIndex}
     */
    public TableColumn getColumn(int columnIndex, boolean onlyVisible) {
        return getColumnList(onlyVisible).get(columnIndex);
    }

    /**
     * Get the list of columns. This list may be only the visible columns or may
     * be the list of all columns.
     *
     * @param onlyVisible true if the list should only contain visible columns;
     *                    false otherwise
     * @return the list of columns
     */
    private Vector<TableColumn> getColumnList(boolean onlyVisible) {
        return (onlyVisible ? tableColumns : allTableColumns);
    }
}
