package jmri.jmrix.rfid.networkdriver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * Definition of objects to handle configuring a layout connection via a
 * NetworkDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2015
 * @author B. Milhaupt  Copyright (C) 2017
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    public final static String NAME = "Network Interface"; // NOI18N

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter.
     */
    public ConnectionConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
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
                enableOpt4(options.get(adapter.getOption3Name()).getItem());
            }
        });

        enableOpt2(options.get(adapter.getOption1Name()).getItem());
        enableOpt3(options.get(adapter.getOption1Name()).getItem());
        enableOpt4(options.get(adapter.getOption3Name()).getItem());
    }

    private void enableOpt2(Object o) {
        boolean enable = o.equals("MERG Concentrator"); // NOI18N
        options.get(adapter.getOption2Name()).getLabel().setEnabled(enable);
        options.get(adapter.getOption2Name()).getComponent().setEnabled(enable);
        options.get(adapter.getOption2Name()).getComponent().setToolTipText(enable
                ? Bundle.getMessage("RfidPrefsOption2ToolTipA")
                : Bundle.getMessage("RfidPrefsOption2ToolTipB"));
    }

    @SuppressWarnings("unchecked")
    private void enableOpt3(Object o) {
        boolean enable = !o.equals("MERG Concentrator"); // NOI18N
        options.get(adapter.getOption3Name()).getLabel().setEnabled(enable);
        options.get(adapter.getOption3Name()).getComponent().setEnabled(enable);
        options.get(adapter.getOption3Name()).getComponent().setEnabled(enable);
        options.get(adapter.getOption3Name()).getComponent().setToolTipText(enable
                ? Bundle.getMessage("RfidPrefsOption3ToolTipA")
                : Bundle.getMessage("RfidPrefsOption3ToolTipB"));
        if (!enable) {
            ((JComboBox<Option>) options.get(adapter.getOption3Name()).getComponent()).setSelectedIndex(0);
        }
    }

    @SuppressWarnings("unchecked")
    private void enableOpt4(Object o) {
        boolean enable = o.equals("Olimex"); // NOI18N
        options.get(adapter.getOption4Name()).getLabel().setEnabled(enable);
        options.get(adapter.getOption4Name()).getComponent().setEnabled(enable);
        options.get(adapter.getOption4Name()).getComponent().setEnabled(enable);
        options.get(adapter.getOption4Name()).getComponent().setToolTipText(enable
                ? Bundle.getMessage("RfidPrefsOption4ToolTipA")
                : Bundle.getMessage("RfidPrefsOption4ToolTipB"));
        if (!enable) {
            ((JComboBox<Option>) options.get(adapter.getOption4Name()).getComponent()).setSelectedIndex(0);
        }
    }

     @Override
    public String name() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new NetworkDriverAdapter();
        }
    }

}
