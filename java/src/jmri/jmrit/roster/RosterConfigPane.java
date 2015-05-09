// RosterConfigPane.java
package jmri.jmrit.roster;

import java.awt.FlowLayout;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import jmri.swing.PreferencesPanel;
import jmri.util.FileUtil;

/**
 * Provide GUI to configure Roster defaults.
 *
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2007
 * @version	$Revision$
 */
public class RosterConfigPane extends JPanel implements PreferencesPanel {

    private static final long serialVersionUID = -8185051724790761792L;
    JLabel filename;
    JTextField owner = new JTextField(20);
    JFileChooser fc;
    private final ResourceBundle apb = ResourceBundle.getBundle("apps.AppsConfigBundle");

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

        p.add(filename = new JLabel(Roster.getFileLocation()));
        // don't show default location, so it's not deemed a user selection
        // and saved
        if (FileUtil.getUserFilesPath().equals(Roster.getFileLocation())) {
            filename.setText("");
        }
        JButton b = new JButton(Bundle.getMessage("ButtonSetDots"));

        b.addActionListener(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -1593137799319787064L;

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
            /**
             *
             */
            private static final long serialVersionUID = 898239723894109746L;

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
        owner.setText(RosterEntry.getDefaultOwner());
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
        return this.apb.getString("MenuRoster"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return this.apb.getString("TabbedLayoutRoster"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return this.apb.getString("LabelTabbedLayoutRoster"); // NOI18N
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
        // do nothing - the persistant manager will take care of this
    }

    @Override
    public boolean isDirty() {
        return (this.isFileLocationChanged()
                || !RosterEntry.getDefaultOwner().equals(this.getDefaultOwner()));
    }

    @Override
    public boolean isRestartRequired() {
        return this.isFileLocationChanged();
    }

    private boolean isFileLocationChanged() {
        return (this.getSelectedItem() == null || this.getSelectedItem().equals(""))
                ? !Roster.getFileLocation().equals(FileUtil.getUserFilesPath())
                : !Roster.getFileLocation().equals(this.getSelectedItem());
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
}
