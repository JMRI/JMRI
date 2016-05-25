// ConsumerTablePane.java
package jmri.jmrix.openlcb.swing.tie;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Pane for showing the consumer table
 *
 * @author	Bob Jacobsen 2008
 * @version	$Revision$
 * @since 2.3.7
 */
public class ConsumerTablePane extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 583763500041401460L;

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.openlcb.swing.tie.TieBundle");

    protected JTable table = null;
    protected TableModel tableModel = null;

    public void initComponents() throws Exception {

        // set the frame's initial state
        setSize(500, 300);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        tableModel = new ConsumerTableModel();
        table = jmri.util.JTableUtil.sortableDataModel(tableModel);
        table.setRowSelectionAllowed(true);
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 350));

        TableColumnModel columnModel = table.getColumnModel();
        TableColumn column;
        column = columnModel.getColumn(ConsumerTableModel.USERNAME_COLUMN);
        column.setMinWidth(20);
        //column.setMaxWidth(40);
        column.setResizable(true);
        column = columnModel.getColumn(ConsumerTableModel.NODE_COLUMN);
        column.setMinWidth(40);
        //column.setMaxWidth(85);
        column.setResizable(true);
        column = columnModel.getColumn(ConsumerTableModel.NUMBER_COLUMN);
        column.setMinWidth(75);
        //column.setMaxWidth(100);
        column.setResizable(true);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);

    }

}

/* @(#)ConsumerTablePane.java */
