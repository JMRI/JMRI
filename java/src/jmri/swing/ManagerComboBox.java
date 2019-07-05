package jmri.swing;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import jmri.Manager;
import jmri.NamedBean;
import jmri.util.ConnectionNameFromSystemName;

/**
 * A JComboBox for a set of Managers for the same type of NamedBean.
 * 
 * @author Randall Wood
 * @param <B> the type of NamedBean
 */
public class ManagerComboBox<B extends NamedBean> extends JComboBox<Manager<B>> {

    public ManagerComboBox() {
        this(new ArrayList<Manager<B>>());
    }

    public ManagerComboBox(@Nonnull List<Manager<B>> list) {
        this(list, null);
    }

    public ManagerComboBox(@Nonnull List<Manager<B>> list, Manager<B> selection) {
        super();
        ManagerRenderer managerRenderer = new ManagerRenderer(getRenderer());
        setRenderer(managerRenderer);
        setModel(new DefaultComboBoxModel<>(list.toArray(new Manager[list.size()])));
        if (!list.isEmpty()) {
            if (selection == null) {
                setSelectedIndex(0);
            } else {
                setSelectedItem(selection);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Manager<B> getSelectedItem() {
        return getItemAt(getSelectedIndex());
    }

    private class ManagerRenderer implements ListCellRenderer<Manager<B>> {

        private final ListCellRenderer<? super Manager<B>> renderer;

        public ManagerRenderer(ListCellRenderer<? super Manager<B>> renderer) {
            this.renderer = renderer;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Manager<B>> list, Manager<B> value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                label.setText(ConnectionNameFromSystemName.getConnectionName(value.getSystemPrefix()));
            }
            return label;
        }
    }


}
