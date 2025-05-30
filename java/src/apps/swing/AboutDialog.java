package apps.swing;

import java.awt.*;
import java.util.Locale;

import javax.swing.*;

import jmri.*;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.swing.ConnectionLabel;
import jmri.util.FileUtil;
import jmri.util.swing.JmriJOptionPane;


/**
 * About dialog.
 *
 * @author Randall Wood Copyright (C) 2012
 */
public final class AboutDialog {

    private final JFrame frame;
    private final boolean modal;

    // this should probably be changed to a JmriAbstractAction.
    public AboutDialog(JFrame jframe, boolean isModal) {
        frame = jframe;
        modal = isModal;
        
    }

    public void setVisible(boolean visible) {
        if ( !visible ) {
            return;
        }
        log.debug("Start UI");
        if (modal) {
            JmriJOptionPane.showMessageDialog(frame, getMainPanel(),
                Bundle.getMessage("TitleAbout", Application.getApplicationName()),
                JmriJOptionPane.PLAIN_MESSAGE);
        } else {
            JmriJOptionPane.showMessageDialogNonModal(frame, getMainPanel(),
                Bundle.getMessage("TitleAbout", Application.getApplicationName()),
                JmriJOptionPane.PLAIN_MESSAGE, null);
        }
    }

    private JPanel getMainPanel(){
        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(namePane());
        pane.add(infoPane());
        return pane;
    }

    protected JPanel namePane() {
        String logo = Application.getLogo();
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        if (log.isDebugEnabled()) {
            log.debug("Fetch main logo: {} ({})", logo, FileUtil.findURL(logo, FileUtil.Location.INSTALLED));
        }
        addCenteredComponent(new JLabel(new ImageIcon(
            pane.getToolkit().getImage(FileUtil.findURL(logo, FileUtil.Location.INSTALLED)),
            "JMRI logo"), SwingConstants.CENTER), pane);
        pane.add(Box.createRigidArea(new Dimension(0, 15)));
        String name = Application.getApplicationName();
        name = checkRegisteredTmInString(name);
        JLabel appName = new JLabel(name, SwingConstants.CENTER);
        appName.setFont(pane.getFont().deriveFont(Font.BOLD, pane.getFont().getSize() * 1.2f));
        addCenteredComponent(appName, pane);
        addCenteredComponent(new JLabel(Application.getURL(), SwingConstants.CENTER), pane);
        pane.add(Box.createRigidArea(new Dimension(0, 15)));
        pane.setAlignmentX(Component.CENTER_ALIGNMENT);
        return pane;
    }

    private static final String REGISTERED_TM_SYMBOL = "\u00ae";

    protected static String checkRegisteredTmInString(String name) {
        if ( !name.contains(REGISTERED_TM_SYMBOL) ) {
            return name + " " + REGISTERED_TM_SYMBOL;
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

    protected static void addCenteredComponent(JComponent c, JPanel p) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT); // doesn't work
        p.add(c);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AboutDialog.class);
}
