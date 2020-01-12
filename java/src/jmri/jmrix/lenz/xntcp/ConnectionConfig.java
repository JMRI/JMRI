package jmri.jmrix.lenz.xntcp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrix.JmrixConfigPane;

/**
 * Handle configuring an XpressNet layout connection via a XnTcp adapter.
 * <p>
 * This uses the {@link XnTcpAdapter} class to do the actual connection.
 *
 * @author Giorgio Terdina Copyright (C) 2008-2011, based on LI100 Action by Bob
 * Jacobsen, Copyright (C) 2003
 * GT - May 2008 - Added possibility of manually defining the IP address and the TCP port number
 * GT - May 2011 - Fixed problems arising from recent refactoring
 * GT - Dec 2011 - Fixed problems in 2.14 arising from changes introduced since May
 *
 * @see XnTcpAdapter
 */
public class ConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    private boolean manualInput = false;

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);

        String h = adapter.getHostName();
        if (h != null && !h.equals(JmrixConfigPane.NONE)) {
            hostNameField = new JTextField(h);
        }
        String t = "" + adapter.getPort();
        if (!t.equals("0")) {
            portField = new JTextField(t);
        }
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return Bundle.getMessage("XnTcpName");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new XnTcpAdapter();
        }
    }

    @Override
    public String getInfo() {
        // GT 2.14 retrieving adapter name from CurrentOption1Setting, since Opt1Box now returns null
        String x = adapter.getOptionState("XnTcpInterface");
        if (x == null) {
            return JmrixConfigPane.NONE;
        }
        if (x.equals(Bundle.getMessage("Manual"))) {
            x = "";
        } else {
            x += ":";
        }
        String t = adapter.getHostName();
        int p = adapter.getPort();
        if (t != null && !t.equals("")) {
            if (p != 0) {
                return x + t + ":" + p;
            }
            return x + t;
        } else {
            return JmrixConfigPane.NONE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void loadDetails(final JPanel d) {
        super.loadDetails(d);

        if (options.get("XnTcpInterface").getComponent() instanceof JComboBox) {
            ((JComboBox<Option>) options.get("XnTcpInterface").getComponent()).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enableInput();
                }
            });
        }
    }

    @Override
    protected void showAdvancedItems() {
        super.showAdvancedItems();
        enableInput();
        _details.repaint();
    }

    private void enableInput() {
        String choice = options.get("XnTcpInterface").getItem();
        //GT 2.14 - Added test for null, now returned by opt1Box at startup (somewhere the initialization is missing)
        if (choice != null) {
            manualInput = (choice.equals(Bundle.getMessage("Manual")) || choice.equals("Manual")); // support pre-i18n configurations
        } else {
            manualInput = false;
        }
        hostNameField.setEnabled(manualInput);
        portField.setEnabled(manualInput);
        adapter.configureOption1(choice);
        adapter.setHostName(hostNameField.getText());
        adapter.setPort(portField.getText());
    }

    String manufacturerName = jmri.jmrix.lenz.LenzConnectionTypeList.LENZ;

    @Override
    public String getManufacturer() {
        return manufacturerName;
    }

    @Override
    public void setManufacturer(String manu) {
        manufacturerName = manu;
    }

    @Override
    public boolean isHostNameAdvanced() {
        return true;
    }

}
