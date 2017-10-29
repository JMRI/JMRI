package jmri.jmrix.openlcb.swing.tie;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

/**
 * Pane for showing the producer table
 *
 * @author Bob Jacobsen 2008
  * @since 2.3.7
 */
public class ProducerTablePane extends JPanel {

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.openlcb.swing.tie.TieBundle");

    protected JTable table = null;
    protected ProducerTableModel tableModel = null;

    public void initComponents() {

        // set the frame's initial state
        setSize(500, 300);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        tableModel = new ProducerTableModel();
        table = new JTable(tableModel);
        table.setRowSorter(new TableRowSorter<>(tableModel));
        table.setRowSelectionAllowed(true);
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 350));

        TableColumnModel columnModel = table.getColumnModel();
        TableColumn column;
        column = columnModel.getColumn(ProducerTableModel.USERNAME_COLUMN);
        column.setMinWidth(20);
        //column.setMaxWidth(40);
        column.setResizable(true);
        column = columnModel.getColumn(ProducerTableModel.NODE_COLUMN);
        column.setMinWidth(40);
        //column.setMaxWidth(85);
        column.setResizable(true);
        column = columnModel.getColumn(ProducerTableModel.NUMBER_COLUMN);
        column.setMinWidth(75);
        //column.setMaxWidth(100);
        column.setResizable(true);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

}
