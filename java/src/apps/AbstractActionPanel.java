// AbstractActionPanel.java
package apps;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.profile.ProfileManager;
import jmri.swing.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an abstract GUI for configuring use of Actions.
 * <P>
 * A {@link PerformActionPanel}/{@link PerformActionModel} object pair invokes a
 * Swing Action when the program is started.
 * <P>
 * A {@link CreateButtonPanel}/{@link CreateButtonModel} object pair creates a
 * new GUI button when the program is started.
 * <P>
 *
 * <P>
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 * @deprecated use {@link apps.startup.StartupModelFactory} to implement preferences handlers for startup actions.
 */
@Deprecated
abstract public class AbstractActionPanel extends JPanel implements PreferencesPanel {

    JPanel self;  // used for synchronization
    protected ResourceBundle rb;
    protected boolean dirty = false;
    String removeButtonKey;

    public AbstractActionPanel(String addButtonKey, String removeButtonKey) {
        self = this;
        this.removeButtonKey = removeButtonKey;

        rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

        // GUi is a series of horizontal entries
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add existing items
        JButton addButton = new JButton(rb.getString(addButtonKey));
        JPanel panel = new JPanel();  // button is a horizontal item too; expands to fill BoxLayout
        panel.setLayout(new FlowLayout());
        panel.add(addButton);
        add(panel);
        addButton.addActionListener((ActionEvent e) -> {
            addItem();
        });

        // are there any existing objects from reading existing config?
        for (AbstractActionModel m : rememberedObjects()) {
            add(new Item(m));
            this.dirty = false; // reset to false - setting the model in the Item ctor sets this true
        }
        jmri.InstanceManager.getDefault(apps.CreateButtonModel.class).addPropertyChangeListener((java.beans.PropertyChangeEvent e) -> {
            if (e.getPropertyName().equals("length")) {
                Component[] l = getComponents();
                for (Component m : l) {
                    if ((m != null) && (m instanceof AbstractActionPanel.Item)) {
                        ((Item) m).updateCombo();
                    }
                }
            }
        });
    }

    abstract List<? extends AbstractActionModel> rememberedObjects();

    protected void addItem() {
        synchronized (self) {
            Item i = new Item();
            add(i);
            InstanceManager.getDefault(StartupActionsManager.class).addAction(i.model);
            revalidate();
            repaint();
            this.dirty = true;
        }
    }

    abstract AbstractActionModel getNewModel();

    @Override
    public String getPreferencesItem() {
        return "STARTUP"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return rb.getString("MenuStartUp"); // NOI18N
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

        private static final long serialVersionUID = -2499516926618516181L;
        JButton removeButton = new JButton(rb.getString(removeButtonKey));

        Item() {
            setLayout(new FlowLayout());
            add(removeButton);
            removeButton.addActionListener(this);
            // create the list of possibilities
            selections = new JComboBox<>(AbstractActionModel.nameList());
            add(selections);
            selections.addItemListener((ItemEvent e) -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    dirty = true;
                    model.setName((String) selections.getSelectedItem());
                }
            });
            // set the model name to the first item in selections
            model.setName(selections.getItemAt(0));
        }

        Item(AbstractActionModel m) {
            this();
            model = m;
            selections.setSelectedItem(m.getName());
        }

        AbstractActionModel model = getNewModel();
        JComboBox<String> selections;

        void updateCombo() {
            String current = (String) selections.getSelectedItem();
            selections.removeAllItems();
            String[] items = AbstractActionModel.nameList();
            for (String item : items) {
                selections.addItem(item);
            }
            if (Arrays.asList(items).contains(current)) {
                selections.setSelectedItem(current);
            } else {
                log.info("Item " + current + " has been removed as it is no longer a valid option");
                actionPerformed(null);
            }
        }

        public AbstractActionModel updatedModel() {
            if (model == null) {
                model = getNewModel();
            }
            model.setName((String) selections.getSelectedItem());
            return model;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (self) {
                // remove this item from display
                Container parent = this.getParent();  // have to do this before remove
                parent.remove(this);
                parent.revalidate();
                parent.repaint();
                // unlink to encourage garbage collection
                removeButton.removeActionListener(this);
                InstanceManager.getDefault(StartupActionsManager.class).removeAction(model);
                model = null;
                dirty = true;
            }
        }
    }

    static Logger log = LoggerFactory.getLogger(AbstractActionPanel.class.getName());
}
