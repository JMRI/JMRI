// EditorPane.java

package jmri.jmrix.loconet.sdfeditor;

import java.awt.FlowLayout;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.ResourceBundle;

import jmri.jmrix.loconet.sdf.*;

import java.util.List;

/**
 * Pane for editing Digitrax SDF files.
 *
 * The GUI consists of a tree of instructions on the left, 
 * and on the right an edit panel. The edit panel 
 * has a small detailed view of the instruction over
 * a larger detailed view.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2007
 * @version	    $Revision: 1.1 $
 */
public class EditorPane extends javax.swing.JPanel {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.sdfeditor.Editor");
        
    public EditorPane() {
        // start to configure GUI
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // set up basic layout; order matters here
        add(newTree());
        add(new JSeparator());
        add(newEditPane());
    }
    
    JTree tree;
    DefaultMutableTreeNode topNode;
    
    JComponent newTree() {
        topNode = new DefaultMutableTreeNode("file");
        tree = new JTree(topNode);
        tree.setMinimumSize(new Dimension(250,600));
        tree.setPreferredSize(new Dimension(250,600));

        JScrollPane treeView = new JScrollPane(tree);
        return treeView;
    }
            
    JPanel newEditPane() {
        JPanel p = new JPanel();
        p.setMinimumSize(new Dimension(600,400));
        p.setPreferredSize(new Dimension(600,400));
        p.setMaximumSize(new Dimension(600,400));
        
        // layout is two vertical parts
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        // upper part of right window
        p.add(newInstructionPane());
        
        p.add(new JSeparator());
        
        // lower part of right window
        p.add(newDetailPane());
        
        return p;
    }
    
    JPanel newInstructionPane() {
        JPanel p = new JPanel();
        
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Instruction: "));
        
        return p;
    }
    
    JPanel newDetailPane() {
        JPanel p = new JPanel();
        
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Details: "));
        
        return p;
    }
    
    /**
     * Add the instructions to the tree
     */
    void addSdf(SdfByteBuffer buff) {
        DefaultMutableTreeNode newNode = null;
    
        // make the top elements at the top
        List ops = buff.getArray();
        System.out.println("size "+ops.size());
        for (int i=0; i<ops.size(); i++) {
            newNode = new DefaultMutableTreeNode(ops.get(i));
            topNode.add(newNode);
            System.out.println("item 1"+((SdfMacro)ops.get(i)).name());
        }
    }
    
    /**
     * Get rid of held resources
     */
    void dispose() {
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EditorPane.class.getName());

}
