// AbstractActionPanel.java

package apps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.beans.PropertyChangeListener;

import java.util.List;

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
 * @version     $Revision$
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
        jmri.InstanceManager.getDefault(apps.CreateButtonModel.class).addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("length")){
                    Component[] l = getComponents();
                    for(Component m: l){
                        if ( (m!= null) && (m instanceof AbstractActionPanel.Item)) {
                            ((Item) m).updateCombo();
                        }
                    }
                }
            }
        });
    }

    abstract List<?> rememberedObjects();

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
        
        void updateCombo(){
            String current = (String)selections.getSelectedItem();
            selections.removeAllItems();
            String[] items = AbstractActionModel.nameList();
            for(int i = 0; i<items.length; i++){
                selections.addItem(items[i]);
            }
            if(Arrays.asList(items).contains(current)){
                selections.setSelectedItem(current);
            } else {
                log.info("Item " + current + " has been removed as it is no longer a valid option");
                actionPerformed(null);
            }
        }
        
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
    
    static Logger log = LoggerFactory.getLogger(AbstractActionPanel.class.getName());
}


