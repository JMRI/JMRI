package jmri.util;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import jmri.util.com.sun.TableSorter;

/**
 * Common utility methods for working with JTables
 * <P>
 * We needed a place to refactor common JTable-processing idioms in JMRI code,
 * so this class was created. It's more of a library of procedures than a real
 * class, as (so far) all of the operations have needed no state information.
 * <P>
 * In particular, this is intended to provide Java 2 functionality on a Java
 * 1.1.8 system, or at least try to fake it.
 *
 * @author Bob Jacobsen Copyright 2003
 */
@Deprecated
public class JTableUtil {

    /**
     *
     * @param dataModel model for table
     * @return a table
     * @deprecated since 4.5.4; create a standard {@link javax.swing.JTable} and
     * add a {@link javax.swing.RowSorter} to that table instead. If you need
     * custom {@link javax.swing.table.TableCellEditor} selection behavior,
     * provide custom TableCellEditors for the columns that need the custom
     * behavior.
     */
    @Deprecated
    static public JTable sortableDataModel(TableModel dataModel) {

        TableSorter sorter;

        sorter = new TableSorter(dataModel);

        JTable dataTable = new JTable(sorter) {
            @Override
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                boolean res = super.editCellAt(row, column, e);
                java.awt.Component c = this.getEditorComponent();
                if (c instanceof javax.swing.JTextField) {
                    ((JTextField) c).selectAll();
                }
                return res;
            }
        };

        sorter.setTableHeader(dataTable.getTableHeader());
        return dataTable;
    }

    // private final static Logger log = LoggerFactory.getLogger(JTableUtil.class);
}
