// AppConfigBase.java

package apps;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.JmrixConfigPane;
import jmri.util.swing.JmriPanel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;


import javax.swing.*;


/**
 * Basic configuration infrastructure, to be 
 * used by specific GUI implementations
 *
 * @author	Bob Jacobsen   Copyright (C) 2003, 2008, 2010
 * @author      Matthew Harris copyright (c) 2009
 * @version	$Revision: 1.4 $
 */
public class AppConfigBase extends JmriPanel {

    static protected ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

    /**
     * Number of layout connections to 
     * support.  By default, this is 4.
     */
    static int NCONNECTIONSDEFAULT = 4;
    /**
     * Remember items to persist
     */
    protected List<Component> items = new ArrayList<Component>();

    /**
     * Construct a configuration panel for inclusion in a preferences
     * or configuration dialog with default number of connections.
     */
    public AppConfigBase() {
        this.nConnections = NCONNECTIONSDEFAULT;
    }
    /**
     * Construct a configuration panel for inclusion in a preferences
     * or configuration dialog with custom number of connections.
     */
    public AppConfigBase(int nConnections) {
        this.nConnections = nConnections;
    }
    int nConnections;
    protected int getNConnections() { return nConnections; }

    public static String getConnection(int index) {
        return JmrixConfigPane.instance(index).getCurrentProtocolName();
    }

    public static String getPort(int index) {
        return JmrixConfigPane.instance(index).getCurrentProtocolInfo();
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppConfigBase.class.getName());

    /**
     * Detect duplicate connection types
     * It depends on all connections have the first word be the same
     * if they share the same type. So LocoNet ... is a fine example.
     * <P>
     * This implementation is specific to 4 connections, and that
     * has to change; there's an assert to catch the problem if it doesn't
     * <p>
     * This also was broken when the names for systems were updated before
     * JMRI 2.9.4, so it should be revisited.
     * 
     * @return true if OK, false if duplicates present.
     */
    private boolean checkDups() {
        if (getNConnections() != 4) {
            throw new IllegalArgumentException("this code can only handle exactly four connections");
        }
        String c1 = AppConfigPanel.getConnection(0);
        int x = c1.indexOf(" ");
        if (x > 0) {
            c1 = c1.substring(0, x);
        }
        String p1 = getPort(0);
        
        String c2 = AppConfigPanel.getConnection(1);
        x = c2.indexOf(" ");
        if (x > 0) {
            c2 = c2.substring(0, x);
        }
        String p2 = getPort(1);
        
        String c3 = AppConfigPanel.getConnection(2);
        x = c3.indexOf(" ");
        if (x > 0) {
            c3 = c3.substring(0, x);
        }
        String p3 = getPort(2);
        
        String c4 = AppConfigPanel.getConnection(3);
        x = c4.indexOf(" ");
        if (x > 0) {
            c4 = c4.substring(0, x);
        }
        String p4 = getPort(3);
        
        if (c1.compareToIgnoreCase(none) != 0) {
            if (c1.compareToIgnoreCase(c2) == 0) {
                return false;
            }
            if (c1.compareToIgnoreCase(c3) == 0) {
                return false;
            }
            if (c1.compareToIgnoreCase(c4) == 0) {
                return false;
            }
        }
        if (p1.compareToIgnoreCase(none) != 0) {
            if (p1.compareToIgnoreCase(p2) == 0) {
                return false;
            }
            if (p1.compareToIgnoreCase(p3) == 0) {
                return false;
            }
            if (p1.compareToIgnoreCase(p4) == 0) {
                return false;
            }
        }
        if (c2.compareToIgnoreCase(none) != 0) {
            if (c2.compareToIgnoreCase(c1) == 0) {
                return false;
            }
            if (c2.compareToIgnoreCase(c3) == 0) {
                return false;
            }
            if (c2.compareToIgnoreCase(c4) == 0) {
                return false;
            }
        }
        if (p2.compareToIgnoreCase(none) != 0) {
            if (p2.compareToIgnoreCase(p1) == 0) {
                return false;
            }
            if (p2.compareToIgnoreCase(p3) == 0) {
                return false;
            }
            if (p2.compareToIgnoreCase(p4) == 0) {
                return false;
            }
        }
        if (c3.compareToIgnoreCase(none) != 0) {
            if (c3.compareToIgnoreCase(c1) == 0) {
                return false;
            }
            if (c3.compareToIgnoreCase(c2) == 0) {
                return false;
            }
            if (c3.compareToIgnoreCase(c4) == 0) {
                return false;
            }
        }
        if (p3.compareToIgnoreCase(none) != 0) {
            if (p3.compareToIgnoreCase(p1) == 0) {
                return false;
            }
            if (p3.compareToIgnoreCase(p2) == 0) {
                return false;
            }
            if (p3.compareToIgnoreCase(p4) == 0) {
                return false;
            }
        }
        if (c4.compareToIgnoreCase(none) != 0) {
            if (c4.compareToIgnoreCase(c1) == 0) {
                return false;
            }
            if (c4.compareToIgnoreCase(c2) == 0) {
                return false;
            }
            if (c4.compareToIgnoreCase(c3) == 0) {
                return false;
            }
        }
        if (p4.compareToIgnoreCase(none) != 0) {
            if (p4.compareToIgnoreCase(p1) == 0) {
                return false;
            }
            if (p4.compareToIgnoreCase(p2) == 0) {
                return false;
            }
            if (p4.compareToIgnoreCase(p3) == 0) {
                return false;
            }
        }
        return true;
    }

    private final static String none = "(none)";  // for later I8N?

    /**
     * Checks to see if user selected a valid serial port
     * @return true if okay
     */
    private boolean checkPortName() {
        if (getPort(0).equals(JmrixConfigPane.NONE_SELECTED) || getPort(0).equals(JmrixConfigPane.NO_PORTS_FOUND)) {
            return false;
        }
        return true;
    }

    public void dispose() {
        items.clear();
    }

    protected void saveContents() {
        // remove old prefs that are registered in ConfigManager
        InstanceManager.configureManagerInstance().removePrefItems();
        // put the new GUI items on the persistance list
        for (int i = 0; i < items.size(); i++) {
            InstanceManager.configureManagerInstance().registerPref(items.get(i));
        }
        InstanceManager.configureManagerInstance().storePrefs();
    }

    /**
     * Handle the Save button:  Backup the file, write a new one, prompt for
     * what to do next.  To do that, the last step is to present a dialog
     * box prompting the user to end the program.
     */
    public void savePressed() {

        if (!checkPortName()) {            
            if (JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("MessageSerialPortWarning"), new Object[]{getPort(0)}), rb.getString("MessageSerialPortNotValid"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
        }

        boolean dups = checkDups();
        // true if OK, which is a little confusing
        if (!dups) {
            dups = JOptionPane.showConfirmDialog(null, rb.getString("MessageLongDupsWarning"), rb.getString("MessageShortDupsWarning"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            if (!dups) {
                return;
            }
        }
        if (dups) {
            saveContents();
            final UserPreferencesManager p;
            p = InstanceManager.getDefault(UserPreferencesManager.class);
            p.resetChangeMade();
            if (p.getQuitAfterSave() == 0) {
                final JDialog dialog = new JDialog();
                dialog.setTitle(rb.getString("MessageShortQuitWarning"));
                dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JPanel container = new JPanel();
                container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                Icon icon = UIManager.getIcon("OptionPane.questionIcon");
                JLabel question = new JLabel(rb.getString("MessageLongQuitWarning"));
                question.setAlignmentX(Component.CENTER_ALIGNMENT);
                question.setIcon(icon);
                container.add(question);
                final JCheckBox remember = new JCheckBox("Remember this setting for next time?");
                remember.setFont(remember.getFont().deriveFont(10.0F));
                remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                JButton yesButton = new JButton("Yes");
                JButton noButton = new JButton("No");
                JPanel button = new JPanel();
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.add(yesButton);
                button.add(noButton);
                container.add(button);
                noButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (remember.isSelected()) {
                            p.setQuitAfterSave(1);
                        }
                        dialog.dispose();
                    }
                });
                yesButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (remember.isSelected()) {
                            p.setQuitAfterSave(2);
                            saveContents();
                        }
                        dialog.dispose();
                        dispose();
                        Apps.handleQuit();
                    }
                });
                container.add(remember);
                container.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.setAlignmentY(Component.CENTER_ALIGNMENT);
                dialog.getContentPane().add(container);
                dialog.pack();
                dialog.setModal(true);
                int w = dialog.getSize().width;
                int h = dialog.getSize().height;
                int x = (p.getScreen().width - w) / 2;
                int y = (p.getScreen().height - h) / 2;
                dialog.setLocation(x, y);
                dialog.setVisible(true);
            } else if (p.getQuitAfterSave() == 2) {
                // end the program
                dispose();
                // do orderly shutdown.  Note this
                // invokes Apps.handleQuit, even if this
                // panel hasn't been created by an Apps subclass.
                Apps.handleQuit();
            }
        }
        // don't end the program, just close the window
        if (getTopLevelAncestor() != null) {
            ((JFrame) getTopLevelAncestor()).setVisible(false);
        }
    }

}
