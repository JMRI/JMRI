package jmri.jmrix.rfid;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * Handle configuring a standalone RFID layout connection via an RfidStreamPortController
 * adapter.
 * <p>
 * This uses the {@link RfidStreamPortController} class to do the actual connection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Paul Bender Copyright (C) 2009
 *
 * @see RfidStreamPortController
 */
public class RfidStreamConnectionConfig extends jmri.jmrix.AbstractStreamConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public RfidStreamConnectionConfig(jmri.jmrix.AbstractStreamPortController p) {
        super(p);
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public RfidStreamConnectionConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);

        // Add a listener to the combo box
        ((JComboBox<Option>) options.get(adapter.getOption1Name()).getComponent()).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableOpt2(options.get(adapter.getOption1Name()).getItem());
                enableOpt3(options.get(adapter.getOption1Name()).getItem());
                enableOpt4(options.get(adapter.getOption3Name()).getItem());
            }
        });

        // Add a listener to the combo box
        ((JComboBox<Option>) options.get(adapter.getOption3Name()).getComponent()).addActionListener(new ActionListener() {
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
        return Bundle.getMessage("RfidStreamName");
    }

    String manufacturerName = "RFID"; // NOI18N

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new RfidStreamPortController();
        }
    }

}
