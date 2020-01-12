package jmri.jmrix.jinput.treecontrol;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import jmri.jmrix.jinput.TreeModel;
import jmri.jmrix.jinput.UsbNode;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a JPanel containing a tree of JInput sources.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class TreePanel extends JPanel {

    public TreePanel() {

        super(true);

        // create basic GUI
        dTree = new JTree(TreeModel.instance());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // build the main GUI
        JScrollPane treePanel = new JScrollPane(dTree);
        JPanel nodePanel = new JPanel();
        add(new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, treePanel, nodePanel));

        // configure the tree
        dTree.setRootVisible(false);
        dTree.setShowsRootHandles(true);
        dTree.setScrollsOnExpand(true);
        dTree.setExpandsSelectedPaths(true);

        dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

        dTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (!dTree.isSelectionEmpty() && dTree.getSelectionPath() != null) {
                    // node has been selected
                    currentNode = getSelectedElement();
                    update();
                } else {
                    currentNode = null;
                    // no node selected, clear
                    sensorBox.setSelected(false);
                    memoryBox.setSelected(false);
                    sensorName.setText("");
                    memoryName.setText("");
                }
            }
        });

        // configure the view pane
        JPanel p2 = new JPanel();
        nodePanel.setLayout(new BorderLayout());
        nodePanel.add(p2, BorderLayout.NORTH);

        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("USBController") + ": "));
        p.add(controllerName);
        p.add(Box.createHorizontalGlue());
        p2.add(p);
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("USBType") + ": "));
        p.add(controllerType);
        p.add(Box.createHorizontalGlue());
        p2.add(p);

        p2.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.NORTH);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("USBComponent") + ": "));
        p.add(componentName);
        p.add(Box.createHorizontalGlue());
        p2.add(p);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("USBIdentifier") + ": "));
        p.add(componentId);
        p.add(Box.createHorizontalGlue());
        p2.add(p);

        p2.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.NORTH);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("USBAnalog") + ": "));
        p.add(componentAnalog);
        p.add(new JLabel("  " + Bundle.getMessage("USBRelative") + ": "));
        p.add(componentRelative);
        p.add(Box.createHorizontalGlue());
        p2.add(p);

        p2.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.NORTH);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("USBValue") + ": "));
        p.add(componentValue);
        p.add(Box.createHorizontalGlue());
        p2.add(p);

        p2.add(new JSeparator(JSeparator.HORIZONTAL));

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(sensorBox);
        p.add(new JLabel(Bundle.getMessage("USBName") + ": "));
        p.add(sensorName);
        p.add(Box.createHorizontalGlue());
        p2.add(p);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(memoryBox);
        p.add(new JLabel(Bundle.getMessage("USBName") + ": "));
        p.add(memoryName);
        p.add(Box.createHorizontalGlue());
        p2.add(p);

        // attach controls
        sensorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkSensorBox();
            }
        });
        memoryBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkMemoryBox();
            }
        });

        // initial states
        sensorBox.setSelected(false);
        memoryBox.setSelected(false);
        sensorName.setEditable(true);
        memoryName.setEditable(true);

        // starting listening for changes
        TreeModel.instance().addPropertyChangeListener(
                new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if ((currentNode != null) && (e.getOldValue() == currentNode)) {
                    // right place, update
                    float value = ((Float) e.getNewValue()).floatValue();
                    if (currentNode.getComponent().isAnalog()) {
                        componentValue.setText("" + value);
                    } else {
                        componentValue.setText((value > 0.0) ? Bundle.getMessage("ButtonYes") : Bundle.getMessage("ButtonNo"));
                    }
                }
            }
        });
    }

    void checkSensorBox() {
        if (currentNode == null) {
            return;
        }
        if (sensorBox.isSelected()) {
            // checked box, if anything there, set the node
            currentNode.setAttachedSensor(sensorName.getText());
            sensorName.setEditable(false);
        } else {
            // unchecked box, reset the node
            currentNode.setAttachedSensor("");
            sensorName.setEditable(true);
        }

    }

    void checkMemoryBox() {
        if (currentNode == null) {
            return;
        }
        if (memoryBox.isSelected()) {
            // checked box, if anything there, set the node
            currentNode.setAttachedMemory(memoryName.getText());
            memoryName.setEditable(false);
        } else {
            // unchecked box, reset the node
            currentNode.setAttachedMemory("");
            memoryName.setEditable(true);
        }
    }

    UsbNode currentNode = null;

    void update() {
        Controller controller = currentNode.getController();
        if (controller != null) {
            controllerName.setText(controller.getName());
            controllerType.setText(controller.getType().toString());
        } else {
            controllerName.setText("");
            controllerType.setText("");
        }
        Component component = currentNode.getComponent();
        if (component != null) {
            componentName.setText(component.getName());
            componentId.setText(component.getIdentifier().toString());
            if (component.isAnalog()) {
                componentAnalog.setText(Bundle.getMessage("ButtonYes"));
                componentValue.setText("" + currentNode.getValue());
                componentRelative.setText(component.isRelative() ? Bundle.getMessage("ButtonYes") : Bundle.getMessage("ButtonNo"));
            } else {
                componentAnalog.setText(Bundle.getMessage("ButtonNo"));
                componentRelative.setText("");
                componentValue.setText((currentNode.getValue() > 0.0) ? Bundle.getMessage("ButtonYes") : Bundle.getMessage("ButtonNo"));
            }

            String attachedSensor = currentNode.getAttachedSensor();
            if ((attachedSensor != null) && !attachedSensor.isEmpty()) {
                sensorName.setText(attachedSensor);
                sensorName.setEditable(false);
                sensorBox.setSelected(true);
            } else {
                sensorName.setText("");
                sensorName.setEditable(true);
                sensorBox.setSelected(false);
            }

            String attachedMemory = currentNode.getAttachedMemory();
            if ((attachedMemory != null) && (!attachedMemory.equals(""))) {
                memoryName.setText(attachedMemory);
                memoryName.setEditable(false);
                memoryBox.setSelected(true);
            } else {
                memoryName.setText("");
                memoryName.setEditable(true);
                memoryBox.setSelected(false);
            }
        } else {
            componentName.setText("");
            componentId.setText("");
            componentAnalog.setText(Bundle.getMessage("ButtonNo"));
            componentRelative.setText(Bundle.getMessage("ButtonNo"));
            componentValue.setText("");
            sensorName.setText("");
            sensorName.setEditable(true);
            sensorBox.setSelected(false);
            memoryName.setText("");
            memoryName.setEditable(true);
            memoryBox.setSelected(false);
        }
    }

    JLabel controllerName = new JLabel();
    JLabel controllerType = new JLabel();
    JLabel componentName = new JLabel();
    JLabel componentId = new JLabel();
    JLabel componentAnalog = new JLabel();
    JLabel componentRelative = new JLabel();
    JLabel componentValue = new JLabel();
    JCheckBox sensorBox = new JCheckBox(Bundle.getMessage("USBCopyJMRISensor") + "  ");
    JTextField sensorName = new JTextField(25);
    JCheckBox memoryBox = new JCheckBox(Bundle.getMessage("USBCopyJMRIMemory") + "  ");
    JTextField memoryName = new JTextField(25);

    public UsbNode getSelectedElement() {
        if (!dTree.isSelectionEmpty() && dTree.getSelectionPath() != null) {
            // somebody has been selected
            log.debug("getSelectedIcon with " + dTree.getSelectionPath().toString());
            TreePath path = dTree.getSelectionPath();

            int level = path.getPathCount();
            // specific items are at level 3, no action above that

            return (UsbNode) path.getPathComponent(level - 1);
        } else {
            return null;
        }
    }

    JTree dTree;

    private final static Logger log = LoggerFactory.getLogger(TreePanel.class);
}
