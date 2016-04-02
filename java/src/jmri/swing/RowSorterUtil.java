package jmri.swing;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;

/**
 * Utilities for handling JTable row sorting.
 *
 * @author Randall Wood
 */
public final class RowSorterUtil {

    /**
     * Get the sort order for a column given a RowSorter for the TableModel
     * containing the column.
     *
     * @param rowSorter
     * @param column
     * @return the sort order or {@link javax.swing.SortOrder#UNSORTED}.
     */
    @Nonnull
    public static SortOrder getSortOrder(@Nonnull RowSorter<? extends TableModel> rowSorter, int column) {
        for (RowSorter.SortKey key : rowSorter.getSortKeys()) {
            if (key.getColumn() == column) {
                return key.getSortOrder();
            }
        }
        return SortOrder.UNSORTED;
    }

    /**
     * Set the sort order for a column given a RowSorter for the TableModel
     * containing the column.
     *
     * @param rowSorter
     * @param column
     * @param sortOrder
     */
    public static void setSortOrder(@Nonnull RowSorter<? extends TableModel> rowSorter, int column, @Nonnull SortOrder sortOrder) {
        List<RowSorter.SortKey> keys = new ArrayList<>();
        for (RowSorter.SortKey key : rowSorter.getSortKeys()) {
            if (key.getColumn() != column) {
                keys.add(key);
            }
        }
        keys.add(new RowSorter.SortKey(column, sortOrder));
        rowSorter.setSortKeys(keys);
    }
}
