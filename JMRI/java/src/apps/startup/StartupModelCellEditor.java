package apps.startup;

import apps.StartupActionsManager;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import jmri.InstanceManager;

/**
 *
 * @author rhwood
 */
class StartupModelCellEditor extends DefaultCellEditor {

    private StartupModel model;
    private final JButton button;

    public StartupModelCellEditor() {
        super(new JTextField());
        this.model = null;
        this.button = new JButton();
        this.button.setHorizontalAlignment(SwingConstants.LEADING);
        this.button.setBorderPainted(false);
        this.button.setFocusable(false);
        this.button.setContentAreaFilled(false);
    }

    @Override
    public Object getCellEditorValue() {
        return this.model;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.model = InstanceManager.getDefault(StartupActionsManager.class).getActions(row);
        if (this.model != null) {
            SwingUtilities.invokeLater(() -> {
                StartupModelFactory factory = InstanceManager.getDefault(StartupActionsManager.class).getFactories(this.model.getClass());
                factory.editModel(this.model, table.getRootPane());
                //Make the renderer reappear.
                fireEditingStopped();
            });
        }
        return table.getCellRenderer(row, column).getTableCellRendererComponent(table, value, isSelected, true, row, column);
    }
}
