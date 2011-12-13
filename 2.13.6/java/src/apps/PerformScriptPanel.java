// PerformScriptPanel.java

package apps;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Provide a GUI for configuring PerformScriptModel objects.
 * <P>
 * A PerformScriptModel object runs a script when
 * the program is started.
 * <P>
 *
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision$
 * @see apps.PerformScriptModel
 */
public class PerformScriptPanel extends JPanel {

    JPanel self;  // used for synchronization
    protected ResourceBundle rb;

    public PerformScriptPanel() {
        self = this;

        rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

        // GUi is a series of horizontal entries
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add existing items

        JButton addButton = new JButton(rb.getString("ButtonScriptAdd"));
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
        int n = PerformScriptModel.rememberedObjects().size();
        for (int i = 0; i< n; i++) {
            PerformScriptModel m = PerformScriptModel.rememberedObjects().get(i);
            add(new Item(m));
        }
    }

    protected void addItem() {
        synchronized(self) {
            Item i = new Item();
            if (i.model.getFileName()==null) return;  // cancelled
            add(i);
            validate();
            if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
        }
    }

    JFileChooser fc = jmri.jmrit.XmlFile.userFileChooser("Python script files", "py");

    public class Item extends JPanel implements ActionListener {
        JButton removeButton = new JButton(rb.getString("ButtonScriptRemove"));
        Item() {
            setLayout(new FlowLayout());
            add(removeButton);
            removeButton.addActionListener(this);
            // get the filename
            fc.showOpenDialog(null);
            if (fc.getSelectedFile()==null) return; // cancelled
            selected = new JLabel(fc.getSelectedFile().getAbsolutePath());
            model.setFileName(fc.getSelectedFile().getAbsolutePath());
            add(selected);
        }

        Item(PerformScriptModel m) {
            setLayout(new FlowLayout());
            add(removeButton);
            removeButton.addActionListener(this);
            model = m;
            selected = new JLabel(m.getFileName());
            add(selected);
        }
        public PerformScriptModel getModel() { return model; }
        PerformScriptModel model = new PerformScriptModel();
        JLabel selected;

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


