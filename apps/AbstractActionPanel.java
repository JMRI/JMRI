// AbstractActionPanel.java

package apps;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sun.java.util.collections.List;

/**
 * Provide an abstract GUI for configuring use of Actions.
 * <P>
 * A {@link PerformActionPanel}/{@link PerformActionModel} object pair
 * invokes a Swing Action when
 * the program is started.
 * <P>
 * A {@link CreateButtonPanel}/{@link CreateButtonModel} object pair
 * creates a new GUI button when
 * the program is started.
 * <P>
 *
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.1 $
 */
abstract public class AbstractActionPanel extends JPanel {

    JPanel self;  // used for synchronization
    protected ResourceBundle rb;

    String removeButtonKey;

    public AbstractActionPanel(String addButtonKey, String removeButtonKey) {
        self = this;
        this.removeButtonKey = removeButtonKey;

        // GUi is a series of horizontal entries
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

        // add existing items

        JButton addButton = new JButton(rb.getString(addButtonKey));
        JPanel panel = new JPanel();  // button is a horizontal item too; expands to fill BoxLayout
        panel.setLayout(new FlowLayout());
        panel.add(addButton);
        add(panel);
        addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addItem();
                }
            }
        );

        // are there any existing objects from reading existing config?
        int n = rememberedObjects().size();
        for (int i = 0; i< n; i++) {
            AbstractActionModel m = (AbstractActionModel) rememberedObjects().get(i);
            add(new Item(m));
        }
    }

    abstract List rememberedObjects();

    protected void addItem() {
        synchronized(self) {
            add(new Item());
            validate();
            if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
        }
    }

    abstract AbstractActionModel getNewModel();

    public class Item extends JPanel implements ActionListener {
        JButton removeButton = new JButton(rb.getString(removeButtonKey));
        Item() {
            setLayout(new FlowLayout());
            add(removeButton);
            removeButton.addActionListener(this);
            // create the list of possibilities
            selections = new JComboBox(AbstractActionModel.nameList());
            add(selections);
        }
        Item(AbstractActionModel m) {
            this();
            model = m;
            selections.setSelectedItem(m.getName());
        }

        AbstractActionModel model = null;
        JComboBox selections;

        public AbstractActionModel updatedModel() {
            if (model==null) model = getNewModel();
            model.setName((String)selections.getSelectedItem());
            return model;
        }

        public void actionPerformed(ActionEvent e) {
            synchronized (self) {
                // remove this item from display
                Container parent = this.getParent();  // have to do this before remove
                Component topParent = this.getTopLevelAncestor();
                parent.remove(this);
                parent.validate();
                if (topParent!=null) ((JFrame)topParent).pack();
                parent.repaint();
                // unlink to encourage garbage collection
                removeButton.removeActionListener(this);
                model = null;
            }
        }
    }
}


