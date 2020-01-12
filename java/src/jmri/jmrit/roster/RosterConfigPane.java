package jmri.jmrit.roster;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import jmri.InstanceManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.swing.PreferencesPanel;
import jmri.util.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide GUI to configure Roster defaults.
 *
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2007
 */
@ServiceProvider(service = PreferencesPanel.class)
public class RosterConfigPane extends JPanel implements PreferencesPanel {

    JLabel filename;
    JTextField owner = new JTextField(20);
    JFileChooser fc;

    public RosterConfigPane() {
        fc = new JFileChooser(FileUtil.getUserFilesPath());
        // filter to only show the roster.xml file
        FileFilter filt = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.getName().equals("roster.xml")) {
                    return true;
                } else {
                    return f.isDirectory();
                }
            }

            @Override
            public String getDescription() {
                return "roster.xml only";
            }
        };

        fc.setDialogTitle(Bundle.getMessage("DialogTitleMove"));
        fc.setFileFilter(filt);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel(Bundle.getMessage("LabelMoveLocation")));

        p.add(filename = new JLabel(Roster.getDefault().getRosterLocation()));
        // don't show default location, so it's not deemed a user selection
        // and saved
        if (FileUtil.getUserFilesPath().equals(Roster.getDefault().getRosterLocation())) {
            filename.setText("");
        }
        JButton b = new JButton(Bundle.getMessage("ButtonSetDots"));

        b.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // prompt with instructions
                if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(RosterConfigPane.this.getTopLevelAncestor(),
                        Bundle.getMessage("DialogMsgMoveWarning"),
                        Bundle.getMessage("DialogMsgMoveQuestion"),
                        JOptionPane.OK_CANCEL_OPTION
                )) {
                    return;
                }

                // get the file
                fc.rescanCurrentDirectory();
                fc.showOpenDialog(null);
                if (fc.getSelectedFile() == null) {
                    return; // cancelled
                }
                if (!fc.getSelectedFile().getName().equals("roster.xml")) {
                    return; // wrong file
                }
                filename.setText(fc.getSelectedFile().getParent() + File.separator);
                validate();
                if (getTopLevelAncestor() != null) {
                    ((JFrame) getTopLevelAncestor()).pack();
                }
            }
        });
        p.add(b);
        b = new JButton(Bundle.getMessage("ButtonReset"));
        b.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filename.setText("");
                validate();
                if (getTopLevelAncestor() != null) {
                    ((JFrame) getTopLevelAncestor()).pack();
                }
            }
        });
        p.add(b);
        add(p);

        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(new JLabel(Bundle.getMessage("LabelDefaultOwner")));
        owner.setText(InstanceManager.getDefault(RosterConfigManager.class).getDefaultOwner());
        p2.add(owner);
        add(p2);
    }

    public String getDefaultOwner() {
        return owner.getText();
    }

    public void setDefaultOwner(String defaultOwner) {
        owner.setText(defaultOwner);
    }

    public String getSelectedItem() {
        return filename.getText();
    }

    @Override
    public String getPreferencesItem() {
        return "ROSTER"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuItemRoster"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("TabbedLayoutRoster"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return Bundle.getMessage("LabelTabbedLayoutRoster"); // NOI18N
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
        Profile project = ProfileManager.getDefault().getActiveProfile();
        RosterConfigManager manager = InstanceManager.getDefault(RosterConfigManager.class);
        manager.setDefaultOwner(project, this.getDefaultOwner());
        manager.setDirectory(project, this.getSelectedItem());
        manager.savePreferences(project);
    }

    @Override
    public boolean isDirty() {
        return (this.isFileLocationChanged()
                || !InstanceManager.getDefault(RosterConfigManager.class).getDefaultOwner().equals(this.getDefaultOwner()));
    }

    @Override
    public boolean isRestartRequired() {
        return this.isFileLocationChanged();
    }

    private boolean isFileLocationChanged() {
        return (this.getSelectedItem() == null || this.getSelectedItem().isEmpty())
                ? !Roster.getDefault().getRosterLocation().equals(FileUtil.getUserFilesPath())
                : !Roster.getDefault().getRosterLocation().equals(this.getSelectedItem());
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
}
