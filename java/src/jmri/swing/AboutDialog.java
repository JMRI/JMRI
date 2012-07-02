// AboutDialog.java
package jmri.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import jmri.Application;
import jmri.Version;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;

/**
 * Base class for Jmri applications. <P>
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008, 2010
 * @author Dennis Miller Copyright 2005
 * @author Giorgio Terdina Copyright 2008
 * @author Matthew Harris Copyright (C) 2011
 * @version $Revision$
 */
public class AboutDialog extends JDialog implements PropertyChangeListener {

    public AboutDialog(JFrame frame, boolean modal) {

        super(frame, modal);

        log.debug("Start UI");

        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(namePane());
        pane.add(infoPane());
        this.add(pane);
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null); // center on screen
        log.debug("End constructor");
    }

    // line 4
    JLabel cs4 = new JLabel();

    protected void buildLine4(JPanel pane) {
        if (connection[0] != null) {
            buildLine(connection[0], cs4, pane);
        }
    }
    // line 5 optional
    JLabel cs5 = new JLabel();

    protected void buildLine5(JPanel pane) {
        if (connection[1] != null) {
            buildLine(connection[1], cs5, pane);
        }
    }
    // line 6 optional
    JLabel cs6 = new JLabel();

    protected void buildLine6(JPanel pane) {
        if (connection[2] != null) {
            buildLine(connection[2], cs6, pane);
        }
    }
    // line 7 optional
    JLabel cs7 = new JLabel();

    protected void buildLine7(JPanel pane) {
        if (connection[3] != null) {
            buildLine(connection[3], cs7, pane);
        }
    }

    protected void buildLine(ConnectionConfig conn, JLabel cs, JPanel pane) {
        if (conn.name().equals(JmrixConfigPane.NONE)) {
            cs.setText(" ");
            return;
        }
        ConnectionStatus.instance().addConnection(conn.name(), conn.getInfo());
        cs.setFont(pane.getFont());
        updateLine(conn, cs);
        pane.add(cs);
    }

    protected void updateLine(ConnectionConfig conn, JLabel cs) {
        if (conn.getDisabled()) {
            return;
        }
        String name = conn.getConnectionName();
        if (name == null) {
            name = conn.getManufacturer();
        }
        if (ConnectionStatus.instance().isConnectionOk(conn.getInfo())) {
            cs.setForeground(Color.black);
            String cf = MessageFormat.format(rb.getString("ConnectionSucceeded"),
                    new Object[]{name, conn.name(), conn.getInfo()});
            cs.setText(cf);
        } else {
            cs.setForeground(Color.red);
            String cf = MessageFormat.format(rb.getString("ConnectionFailed"),
                    new Object[]{name, conn.name(), conn.getInfo()});
            cf = cf.toUpperCase();
            cs.setText(cf);
        }


        this.revalidate();
    }

    protected JPanel namePane() {
        String logo = "resources/logo.gif";
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        if (log.isDebugEnabled()) {
            log.debug("Fetch main logo: " + logo + " " + getToolkit().getImage(logo));
        }
        pane.add(new JLabel(new ImageIcon(getToolkit().getImage("resources/logo.gif"), "JMRI logo"), JLabel.CENTER));
        pane.add(Box.createRigidArea(new Dimension(0,15)));
        pane.add(new JLabel(Application.getApplicationName()));
        pane.add(new JLabel("http://jmri.org"));
        pane.add(Box.createRigidArea(new Dimension(0,15)));
        return pane;
    }

    protected JPanel infoPane() {
        JPanel pane1 = new JPanel();
        String spacer = " ";
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

        log.debug("start labels");

        // add listerner for Com port updates
        ConnectionStatus.instance().addPropertyChangeListener(this);
        ArrayList<Object> connList = jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class);
        int i = 0;
        if (connList != null) {
            for (int x = 0; x < connList.size(); x++) {
                jmri.jmrix.ConnectionConfig conn = (jmri.jmrix.ConnectionConfig) connList.get(x);
                if (!conn.getDisabled()) {
                    connection[i] = conn;
                    i++;
                }
                if (i > 3) {
                    break;
                }
            }
        }
        buildLine4(pane1);
        buildLine5(pane1);
        buildLine6(pane1);
        buildLine7(pane1);

        pane1.add(new JLabel(spacer));
        pane1.add(new JLabel(MessageFormat.format(rb.getString("DefaultVersionCredit"),
                new Object[]{Version.name()})));
        pane1.add(new JLabel(MessageFormat.format(rb.getString("JavaVersionCredit"),
                new Object[]{System.getProperty("java.version", "<unknown>"),
                    Locale.getDefault().toString()})));
        return pane1;
    }

    protected void addCenteredComponent(JComponent c, JPanel p) {
        // c.setAlignmentX(Component.CENTER_ALIGNMENT); // doesn't work
        p.add(c);
    }
    //int[] connection = {-1,-1,-1,-1};
    jmri.jmrix.ConnectionConfig[] connection = {null, null, null, null};
    protected static ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");

    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        if (log.isDebugEnabled()) {
            log.debug("property change: comm port status update");
        }
        if (connection[0] != null) {
            updateLine(connection[0], cs4);
        }

        if (connection[1] != null) {
            updateLine(connection[1], cs5);
        }

        if (connection[2] != null) {
            updateLine(connection[2], cs6);
        }

        if (connection[3] != null) {
            updateLine(connection[3], cs7);
        }

    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AboutDialog.class.getName());
}
