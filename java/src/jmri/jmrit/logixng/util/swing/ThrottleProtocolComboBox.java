package jmri.jmrit.logixng.util.swing;

import javax.annotation.CheckForNull;
import javax.swing.JComboBox;

import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.SystemConnectionMemo;
import jmri.ThrottleManager;

/**
 * A combo box for the protocol the LogixNG throttle actions use, listing
 * "Default" and the protocols the selected connection's throttle manager
 * supports.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class ThrottleProtocolComboBox extends JComboBox<ThrottleProtocolComboBox.Item> {

    /**
     * Create the combo box with the protocols of the default throttle
     * connection, and "Default" selected.
     */
    public ThrottleProtocolComboBox() {
        setConnection(null, null);
    }

    /**
     * Fill the list with the protocols of a connection and select one.
     *
     * @param memo     the connection, or null for the default throttle connection
     * @param protocol the protocol to select, or null for "Default"; a
     *                 protocol the connection doesn't support selects "Default"
     */
    public final void setConnection(@CheckForNull SystemConnectionMemo memo,
            @CheckForNull LocoAddress.Protocol protocol) {
        removeAllItems();
        Item defaultItem = new Item(null);
        addItem(defaultItem);
        setSelectedItem(defaultItem);
        ThrottleManager manager = (memo != null)
                ? memo.get(ThrottleManager.class)
                : InstanceManager.getNullableDefault(ThrottleManager.class);
        if (manager == null) {
            return;
        }
        for (LocoAddress.Protocol p : manager.getAddressProtocolTypes()) {
            Item item = new Item(p);
            addItem(item);
            if (p == protocol) {
                setSelectedItem(item);
            }
        }
    }

    /**
     * Get the selected protocol.
     *
     * @return the protocol, or null if "Default" is selected
     */
    @CheckForNull
    public LocoAddress.Protocol getSelectedProtocol() {
        Item item = getItemAt(getSelectedIndex());
        return (item != null) ? item._protocol : null;
    }

    /**
     * An entry in the list: a protocol, or null for "Default".
     */
    public static final class Item {

        private final LocoAddress.Protocol _protocol;

        private Item(@CheckForNull LocoAddress.Protocol protocol) {
            _protocol = protocol;
        }

        @Override
        public String toString() {
            if (_protocol == null) {
                return Bundle.getMessage("ThrottleProtocolComboBox_Default");
            }
            return _protocol.getPeopleName();
        }
    }

}
