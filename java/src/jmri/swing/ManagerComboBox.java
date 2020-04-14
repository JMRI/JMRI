package jmri.swing;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import jmri.Manager;
import jmri.NamedBean;
import jmri.ProxyManager;

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
        setRenderer(new ManagerRenderer(getRenderer()));
        // prevent overriding method from being used
        ManagerComboBox.this.setManagers(list, selection);
    }

    /**
     * Set the list of managers, selecting the first manager in the list.
     *
     * @param list the list of managers
     */
    public void setManagers(@Nonnull List<Manager<B>> list) {
        setManagers(list, null);
    }

    /**
     * Set the list of managers, selecting the passed in manager.
     *
     * @param list      the list of managers
     * @param selection the manager to select; if null, the first manager in the
     *                  list is selected
     */
    public void setManagers(@Nonnull List<Manager<B>> list, Manager<B> selection) {
        setModel(new DefaultComboBoxModel<>(new Vector<>(list)));
        if (!list.isEmpty()) {
            if (selection == null) {
                setSelectedIndex(0);
            } else {
                setSelectedItem(selection);
            }
        }
    }

    /**
     * Set the list of managers to the single passed in manager, and select it.
     *
     * @param manager the manager; if manager is a {@link ProxyManager}, this is
     *                equivalent to calling {@link #setManagers(List, Manager)}
     *                with the results of
     *                {@link ProxyManager#getDisplayOrderManagerList()},
     *                {@link ProxyManager#getDefaultManager()}
     */
    public void setManagers(@Nonnull Manager<B> manager) {
        if (manager instanceof ProxyManager) {
            ProxyManager<B> proxy = (ProxyManager<B>) manager;
            setManagers(proxy.getDisplayOrderManagerList(), proxy.getDefaultManager());
        } else {
            List<Manager<B>> list = new ArrayList<>();
            list.add(manager);
            setManagers(list, manager);
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
        public Component getListCellRendererComponent(JList<? extends Manager<B>> list, Manager<B> value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                label.setText(value.getMemo().getUserName());
            }
            return label;
        }
    }

}
