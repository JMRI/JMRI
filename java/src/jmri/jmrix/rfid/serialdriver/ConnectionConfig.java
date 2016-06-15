package jmri.jmrix.rfid.serialdriver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p {@link jmri.jmrix.SerialPortAdapter} to configure
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no preexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);

        //Add a listener to the combo box
        ((JComboBox<Option>) options.get(adapter.getOption1Name()).getComponent()).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableOpt2(options.get(adapter.getOption1Name()).getItem());
                enableOpt3(options.get(adapter.getOption1Name()).getItem());
            }
        });

        enableOpt2(options.get(adapter.getOption1Name()).getItem());
        enableOpt3(options.get(adapter.getOption1Name()).getItem());

    }

    private void enableOpt2(Object o) {
        boolean enable = o.equals("MERG Concentrator");
        options.get(adapter.getOption2Name()).getLabel().setEnabled(enable);
        options.get(adapter.getOption2Name()).getComponent().setEnabled(enable);
        options.get(adapter.getOption2Name()).getComponent().setToolTipText(enable
                ? "Choose RFID concentrator range setting"
                : "Range setting not applicable for selected RFID reader type");
    }

    @SuppressWarnings("unchecked")
    private void enableOpt3(Object o) {
        boolean enable = !o.equals("MERG Concentrator");
        options.get(adapter.getOption3Name()).getLabel().setEnabled(enable);
        options.get(adapter.getOption3Name()).getComponent().setEnabled(enable);
        options.get(adapter.getOption3Name()).getComponent().setEnabled(enable);
        options.get(adapter.getOption3Name()).getComponent().setToolTipText(enable
                ? "Choose RFID protocol"
                : "Protocol setting not applicable for selected RFID reader type");
        if (!enable) {
            ((JComboBox<Option>) options.get(adapter.getOption3Name()).getComponent()).setSelectedIndex(0);
        }
    }

    @Override
    public String name() {
        return "Direct Serial Connection";
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SerialDriverAdapter();
        }
    }
}
