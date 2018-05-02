package jmri.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.Application;
import jmri.InstanceManager;
import jmri.Version;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * About dialog.
 *
 * @author Randall Wood Copyright (C) 2012
 */
public final class AboutDialog extends JDialog {

    // this should probably be changed to a JmriAbstractAction that opens a JOptionPane with the contents and an OK button instead.
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
        this.setTitle(Bundle.getMessage("TitleAbout", Application.getApplicationName()));
        log.debug("End constructor");
    }

    protected JPanel namePane() {
        String logo = Application.getLogo();
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        if (log.isDebugEnabled()) {
            log.debug("Fetch main logo: {} ({})", logo, FileUtil.findURL(logo, FileUtil.Location.INSTALLED));
        }
        addCenteredComponent(new JLabel(new ImageIcon(getToolkit().getImage(FileUtil.findURL(logo, FileUtil.Location.INSTALLED)), "JMRI logo"), JLabel.CENTER), pane);
        pane.add(Box.createRigidArea(new Dimension(0, 15)));
        String name = Application.getApplicationName();
        name = checkCopyright(name);
        JLabel appName = new JLabel(name, JLabel.CENTER);
        appName.setFont(pane.getFont().deriveFont(Font.BOLD, pane.getFont().getSize() * 1.2f));
        addCenteredComponent(appName, pane);
        addCenteredComponent(new JLabel(Application.getURL(), JLabel.CENTER), pane);
        pane.add(Box.createRigidArea(new Dimension(0, 15)));
        pane.setAlignmentX(Component.CENTER_ALIGNMENT);
        return pane;
    }

    protected String checkCopyright(String name) {
        if (name.toUpperCase().equals("DECODERPRO")) {
            name = name + "\u00ae";
        }
        return name;
    }

    protected JPanel infoPane() {
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

        log.debug("start labels");

        // add listener for Com port updates
        for (ConnectionConfig conn : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!conn.getDisabled()) {
                pane1.add(new ConnectionLabel(conn));
            }
        }
        pane1.add(Box.createRigidArea(new Dimension(0, 15)));

        pane1.add(new JLabel(Bundle.getMessage("DefaultVersionCredit", Version.name())));
        pane1.add(new JLabel(Version.getCopyright()));
        pane1.add(new JLabel(Bundle.getMessage("JavaVersionCredit",
                System.getProperty("java.version", "<unknown>"),
                Locale.getDefault().toString())));
        pane1.setAlignmentX(Component.CENTER_ALIGNMENT);
        return pane1;
    }

    protected void addCenteredComponent(JComponent c, JPanel p) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT); // doesn't work
        p.add(c);
    }

    private static final Logger log = LoggerFactory.getLogger(AboutDialog.class);
}
