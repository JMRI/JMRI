package apps;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.implementation.FileLocationsPreferences;
import jmri.profile.ProfileManager;
import jmri.swing.PreferencesPanel;
import jmri.util.FileUtil;

/**
 * Provide GUI to configure the Default File Locations
 * <P>
 * Provides GUI configuration for the default file locations by displaying
 * textfields for the user to directly enter in their own path or a Set button
 * is provided so that the user can select the path.
 *
 * @author Kevin Dickerson Copyright (C) 2010
 */
public class FileLocationPane extends JPanel implements PreferencesPanel {

    protected static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");
    private boolean restartRequired = false;
    private final JTextField scriptLocation = new JTextField();
    private final JTextField userLocation = new JTextField();

    public FileLocationPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(PrefLocation());
        add(ScriptsLocation());

        /*p = new JPanel();
         JLabel throttle = new JLabel("Default Throttle Location");
         p.add(throttle);
         p.add(throttleLocation);
         throttleLocation.setColumns(20);
         throttleLocation.setText(jmri.jmrit.throttle.ThrottleFrame.getDefaultThrottleFolder());
         add(p);*/
    }

    private JPanel ScriptsLocation() {
        JButton bScript = new JButton(rb.getString("ButtonSetDots"));
        final JFileChooser fcScript;
        fcScript = new JFileChooser(FileUtil.getScriptsPath());

        fcScript.setDialogTitle(rb.getString("MessageSelectDirectory"));
        fcScript.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fcScript.setAcceptAllFileFilterUsed(false);
        bScript.addActionListener(new OpenAction(fcScript, scriptLocation));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        JLabel scripts = new JLabel(rb.getString("ScriptDir"));
        p.add(scripts);
        p.add(scriptLocation);
        p.add(bScript);
        scriptLocation.setColumns(30);
        scriptLocation.setText(FileUtil.getScriptsPath());
        return p;
    }

    private JPanel PrefLocation() {
        JPanel p = new JPanel();
        JLabel users = new JLabel(rb.getString("PrefDir"));
        p.add(users);
        p.add(userLocation);
        userLocation.setColumns(30);
        userLocation.setText(FileUtil.getUserFilesPath());

        JButton bUser = new JButton(rb.getString("ButtonSetDots"));
        final JFileChooser fcUser;
        fcUser = new JFileChooser(FileUtil.getUserFilesPath());

        fcUser.setDialogTitle(rb.getString("MessageSelectDirectory"));
        fcUser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fcUser.setAcceptAllFileFilterUsed(false);
        bUser.addActionListener(new OpenAction(fcUser, userLocation));
        p.add(bUser);
        return p;
    }

    @Override
    public String getPreferencesItem() {
        return "FILELOCATIONS"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return rb.getString("MenuFileLocation"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return rb.getString("TabbedLayoutFileLocations"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return rb.getString("LabelTabbedFileLocations"); // NOI18N
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return true;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        if (!FileUtil.getUserFilesPath().equals(this.userLocation.getText())) {
            FileUtil.setUserFilesPath(this.userLocation.getText());
            this.restartRequired = true;
        }
        if (!FileUtil.getScriptsPath().equals(this.scriptLocation.getText())) {
            FileUtil.setScriptsPath(this.scriptLocation.getText());
            this.restartRequired = true;
        }
        InstanceManager.getDefault(FileLocationsPreferences.class).savePreferences(ProfileManager.getDefault().getActiveProfile());
    }

    @Override
    public boolean isDirty() {
        return (!FileUtil.getUserFilesPath().equals(this.userLocation.getText())
                || !FileUtil.getScriptsPath().equals(this.scriptLocation.getText()));
    }

    @Override
    public boolean isRestartRequired() {
        return this.restartRequired;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
    
    private class OpenAction extends AbstractAction {
        JFileChooser chooser;
        JTextField field;
        OpenAction(JFileChooser chooser, JTextField field) {
            this.chooser = chooser;
            this.field = field;
        }
        @Override
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification="protected by if instanceof")
        public void actionPerformed(ActionEvent e) {
            // get the file
            chooser.showOpenDialog(null);
            if (chooser.getSelectedFile() == null) {
                return; // cancelled
            }
            field.setText(chooser.getSelectedFile() + File.separator);
            validate();
            if (getTopLevelAncestor() != null && getTopLevelAncestor() instanceof JFrame) {
                ((JFrame)getTopLevelAncestor()).pack();
            }
        }
    }
}
