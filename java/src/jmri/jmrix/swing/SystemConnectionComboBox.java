package jmri.jmrix.swing;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemo;

/**
 * JComboBox that provides a list of SystemConnectionMemos by user name.
 *
 * @author Randall Wood Copyright 2017
 */
public class SystemConnectionComboBox extends JComboBox<SystemConnectionMemo> {

    private final SystemConnectionComboBoxModel model = new SystemConnectionComboBoxModel();

    public SystemConnectionComboBox() {
        super();
        super.setModel(this.model);
    }

    public void dispose() {
        this.model.dispose();
    }

    private static class SystemConnectionComboBoxModel extends AbstractListModel<SystemConnectionMemo> 
                        implements ComboBoxModel<SystemConnectionMemo> {

        SystemConnectionMemo selectedItem = null;
        PropertyChangeListener memoListener = (PropertyChangeEvent evt) -> {
            switch (evt.getPropertyName()) {
                case SystemConnectionMemo.USER_NAME:
                    int index = this.getSource().indexOf(evt.getSource());
                    this.fireContentsChanged(this, index, index);
                    break;
                default:
                    // do nothing
                    break;
            }
        };
        PropertyChangeListener imListener = (PropertyChangeEvent evt) -> {
            if (evt.getSource().equals(InstanceManager.getDefault())) {
                if (evt instanceof IndexedPropertyChangeEvent) {
                    IndexedPropertyChangeEvent event = (IndexedPropertyChangeEvent) evt;
                    if (event.getNewValue() != null) {
                        ((SystemConnectionMemo) event.getNewValue()).addPropertyChangeListener(memoListener);
                        this.fireIntervalAdded(this, event.getIndex(), event.getIndex());
                    } else if (event.getOldValue() != null) {
                        ((SystemConnectionMemo) event.getOldValue()).removePropertyChangeListener(memoListener);
                        this.fireIntervalRemoved(this, event.getIndex(), event.getIndex());
                    }
                    this.fireContentsChanged(this, event.getIndex(), event.getIndex());
                } else {
                    Object item = evt.getNewValue();
                    if (item == null || item instanceof SystemConnectionMemo) {
                        SystemConnectionMemo memo = (SystemConnectionMemo) item;
                        if (memo == null || this.getSource().contains(memo)) {
                            this.setSelectedItem(memo);
                        }
                    }
                }
            }
        };

        public SystemConnectionComboBoxModel() {
            InstanceManager.addPropertyChangeListener(InstanceManager.getListPropertyName(SystemConnectionMemo.class), imListener);
            InstanceManager.addPropertyChangeListener(InstanceManager.getDefaultsPropertyName(SystemConnectionMemo.class), imListener);
            this.getSource().forEach((memo) -> {
                memo.addPropertyChangeListener(memoListener);
            });
        }

        /** {@inheritDoc} */
        @Override
        public void setSelectedItem(Object anItem) {  // Object parameter required by interface
            if ((anItem == null || anItem instanceof SystemConnectionMemo) // anItem is valid in this model
                    && ((selectedItem != null && !selectedItem.equals(anItem)) // anItem is not selectedItem
                    || selectedItem == null && anItem != null)) {
                this.selectedItem = (SystemConnectionMemo) anItem;
                this.fireContentsChanged(this, -1, -1);
            }
        }

        /** {@inheritDoc} */
        @Override
        public SystemConnectionMemo getSelectedItem() {
            return this.selectedItem;
        }

        /** {@inheritDoc} */
        @Override
        public int getSize() {
            return this.getSource().size();
        }

        /** {@inheritDoc} */
        @Override
        public SystemConnectionMemo getElementAt(int index) {
            return this.getSource().get(index);
        }

        public void dispose() {
            InstanceManager.removePropertyChangeListener(InstanceManager.getListPropertyName(SystemConnectionMemo.class), imListener);
            InstanceManager.removePropertyChangeListener(InstanceManager.getDefaultsPropertyName(SystemConnectionMemo.class), imListener);
        }

        private List<SystemConnectionMemo> getSource() {
            return InstanceManager.getList(SystemConnectionMemo.class);
        }
    }

}
