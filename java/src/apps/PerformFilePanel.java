// PerformFilePanel.java
package apps;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.profile.ProfileManager;
import jmri.swing.PreferencesPanel;

/**
 * Provide a GUI for configuring PerformFileModel objects.
 * <P>
 * A PerformFileModel object loads a file when the program is started.
 * <P>
 *
 * <P>
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 * @see apps.PerformFileModel
 * @deprecated Replaced by {@link apps.startup.PerformFileModelFactory}
 */
@Deprecated
public class PerformFilePanel extends JPanel implements PreferencesPanel {

    /**
     *
     */
    private static final long serialVersionUID = 5019870518438422370L;
    JPanel self;  // used for synchronization
    protected ResourceBundle rb;
    private boolean dirty = false;

    public PerformFilePanel() {
        self = this;

        rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

        // GUi is a series of horizontal entries
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add existing items
        JButton addButton = new JButton(rb.getString("ButtonFileAdd"));
        JPanel panel = new JPanel();  // button is a horizontal item too; expands to fill BoxLayout
        panel.setLayout(new FlowLayout());
        panel.add(addButton);
        add(panel);
        addButton.addActionListener((ActionEvent e) -> {
            addItem();
        });

        // are there any existing objects from reading existing config?
        InstanceManager.getDefault(StartupActionsManager.class).getActions(PerformFileModel.class).stream().forEach((m) -> {
            add(new Item(m));
        });
    }

    protected void addItem() {
        synchronized (self) {
            Item i = new Item();
            if (i.model.getFileName() == null) {
                return;  // cancelled
            }
            InstanceManager.getDefault(StartupActionsManager.class).addAction(i.model);
            add(i);
            revalidate();
            repaint();
            this.dirty = true;
        }
    }

    JFileChooser fc = jmri.jmrit.XmlFile.userFileChooser("XML files", "xml");

    @Override
    public String getPreferencesItem() {
        return "STARTUP"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return rb.getString("MenuStartUp"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return rb.getString("TabbedLayoutStartupFiles"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return rb.getString("LabelTabbedLayoutStartupFiles"); // NOI18N
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
        InstanceManager.getDefault(StartupActionsManager.class).savePreferences(ProfileManager.getDefault().getActiveProfile());
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public boolean isRestartRequired() {
        return this.isDirty();
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }

    public class Item extends JPanel implements ActionListener {

        /**
         *
         */
        private static final long serialVersionUID = 9081562133732338951L;
        JButton removeButton = new JButton(rb.getString("ButtonFileRemove"));

        Item() {
            setLayout(new FlowLayout());
            add(removeButton);
            removeButton.addActionListener(this);
            // get the filename
            fc.showOpenDialog(null);
            if (fc.getSelectedFile() == null) {
                return; // cancelled
            }
            selected = new JLabel(fc.getSelectedFile().getAbsolutePath());
            model.setFileName(fc.getSelectedFile().getAbsolutePath());
            add(selected);
        }

        Item(PerformFileModel m) {
            setLayout(new FlowLayout());
            add(removeButton);
            removeButton.addActionListener(this);
            model = m;
            selected = new JLabel(m.getFileName());
            add(selected);
        }

        public PerformFileModel getModel() {
            return model;
        }
        PerformFileModel model = new PerformFileModel();
        JLabel selected;

        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (self) {
                // remove this item from display
                Container parent = this.getParent();  // have to do this before remove
                Component topParent = this.getTopLevelAncestor();
                parent.remove(this);
                parent.revalidate();
                if (topParent != null) {
                    ((JFrame) topParent).pack();
                }
                parent.repaint();
                // unlink to encourage garbage collection
                removeButton.removeActionListener(this);
                InstanceManager.getDefault(StartupActionsManager.class).removeAction(model);
                model = null;
                dirty = true;
            }
        }
    }
}
