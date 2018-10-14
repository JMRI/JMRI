package jmri.jmrix.loconet.sdfeditor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import jmri.jmrix.loconet.sdf.SdfBuffer;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Pane for editing Digitrax SDF files.
 * <p>
 * The GUI consists of a tree of instructions on the left, and on the right an
 * edit panel. The edit panel has a small detailed view of the instruction over
 * a larger detailed view.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2008
 */
public class EditorPane extends javax.swing.JPanel implements TreeSelectionListener {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.sdfeditor.Editor");
    static ResourceBundle exp = ResourceBundle.getBundle("jmri.jmrix.loconet.sdfeditor.Explanations");

    public EditorPane() {
        // start to configure GUI
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // install left and right parts in split pane
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, newTree(), newEditPane());
        add(split);
    }

    JSplitPane split;
    JTree tree;
    DefaultMutableTreeNode topNode;

    JComponent newTree() {
        topNode = new DefaultMutableTreeNode("file"); // NOI18N
        tree = new JTree(topNode);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        // install in scroll area
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setMinimumSize(new Dimension(250, 600));
        treeView.setPreferredSize(new Dimension(250, 600));
        return treeView;
    }

    SdfMacroEditor lastEditor = null;

    /**
     * Handle tree selection
     */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node == null) {
            return;
        }

        // get an editor
        SdfMacroEditor nodeInfo = (SdfMacroEditor) node.getUserObject();

        // use that editor to show the instruction
        instruction.setText(nodeInfo.oneInstructionString());

        // show the explanation text
        explanation.setText(exp.getString(nodeInfo.getMacro().name()));

        // make the correct editor visible
        if (lastEditor != null) {
            lastEditor.setVisible(false);
        }
        lastEditor = nodeInfo;
        nodeInfo.update();
        nodeInfo.setVisible(true);
    }

    public void updateSummary() {
        if (lastEditor != null) {
            instruction.setText(lastEditor.oneInstructionString());
        }
    }

    JPanel newEditPane() {
        JPanel p = new JPanel();
        p.setMinimumSize(new Dimension(600, 400));
        p.setPreferredSize(new Dimension(600, 400));
        p.setMaximumSize(new Dimension(600, 400));

        // layout is two vertical parts
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // upper part of right window
        p.add(newInstructionPane());

        p.add(new JSeparator());

        p.add(explanation);
        explanation.setEditable(false);
        explanation.setContentType("text/html"); // NOI18N
        explanation.setMinimumSize(new Dimension(600, 200));
        explanation.setPreferredSize(new Dimension(600, 200));
        explanation.setMaximumSize(new Dimension(600, 200));
        explanation.setBackground(new JLabel().getBackground());

        p.add(new JSeparator());

        // lower part of right window
        p.add(newDetailPane());

        return p;
    }

    MonitoringLabel instruction = new MonitoringLabel();
    JEditorPane explanation = new JEditorPane();

    JComponent newInstructionPane() {
        instruction.setLineWrap(true);
        instruction.setWrapStyleWord(true);
        instruction.setText("Select an instruction in the tree to the left"); // NOI18N
        instruction.setEditable(false);
        instruction.setMinimumSize(new Dimension(600, 80));
        instruction.setPreferredSize(new Dimension(600, 80));
        instruction.setMaximumSize(new Dimension(600, 80));
        instruction.setBackground(new JLabel().getBackground());
        return instruction;
    }

    JPanel detailed = new JPanel(); // panel that contains the specific editors

    JPanel newDetailPane() {

        detailed.setLayout(new FlowLayout());

        return detailed;
    }

    /**
     * Add the instructions to the tree
     */
    void addSdf(SdfBuffer buff) {
        //DefaultMutableTreeNode newNode = null;

        // make the top elements at the top
        List<SdfMacro> ops = buff.getMacroList();
        for (int i = 0; i < ops.size(); i++) {
            nestNodes(topNode, ops.get(i));
        }

        // don't show the top (single) node, 
        // do show all the ones right under that.
        tree.expandPath(new TreePath(topNode));
        tree.setRootVisible(false);

    }

    void nestNodes(DefaultMutableTreeNode parent, SdfMacro macro) {
        // put in the new topmost node
        SdfMacroEditor e = SdfMacroEditor.attachEditor(macro);
        detailed.add(e);
        e.setVisible(false);
        DefaultMutableTreeNode newNode
                = new DefaultMutableTreeNode(e);
        e.setNotify(newNode, this);
        parent.add(newNode);

        // recurse for kids
        List<SdfMacro> children = macro.getChildren();
        if (children == null) {
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            nestNodes(newNode, children.get(i));
        }
    }

    /**
     * Get rid of held resources
     */
    void dispose() {
    }

}
