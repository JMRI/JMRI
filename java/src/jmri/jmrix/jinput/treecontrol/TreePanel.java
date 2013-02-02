// TreePane.java

package jmri.jmrix.jinput.treecontrol;

import org.apache.log4j.Logger;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import jmri.jmrix.jinput.TreeModel;
import jmri.jmrix.jinput.UsbNode;


/**
 * Create a JPanel containing a tree of JInput sources.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Bob Jacobsen  Copyright 2008
 * @version			$Revision$
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
            public void valueChanged(TreeSelectionEvent e) {
                if (!dTree.isSelectionEmpty() && dTree.getSelectionPath()!=null ) {
                       // node has been selected
                       currentNode = getSelectedElement();
                       update();
                    }
                else {
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
        
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));     
        p.add(new JLabel("Controller: "));
        p.add(controllerName);
        p.add(Box.createHorizontalGlue());
        p2.add(p);
        p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); 
        p.add(new JLabel("Type: "));
        p.add(controllerType);
        p.add(Box.createHorizontalGlue());
        p2.add(p);
        
        p2.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.NORTH);
        
        p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); 
        p.add(new JLabel("Component: "));
        p.add(componentName);
        p.add(Box.createHorizontalGlue());
        p2.add(p);
        
        p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); 
        p.add(new JLabel("Identifier: "));
        p.add(componentId);
        p.add(Box.createHorizontalGlue());
        p2.add(p);
        
        p2.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.NORTH);
        
        p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); 
        p.add(new JLabel("Analog: "));
        p.add(componentAnalog);
        p.add(new JLabel("  Relative: "));
        p.add(componentRelative);
        p.add(Box.createHorizontalGlue());
        p2.add(p);
        
        p2.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.NORTH);
        
        p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); 
        p.add(new JLabel("Value: "));
        p.add(componentValue);
        p.add(Box.createHorizontalGlue());
        p2.add(p);
        
        p2.add(new JSeparator(JSeparator.HORIZONTAL));

        p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); 
        p.add(sensorBox);
        p.add(new JLabel("Name: "));
        p.add(sensorName);
        p.add(Box.createHorizontalGlue());
        p2.add(p);

        p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); 
        p.add(memoryBox);
        p.add(new JLabel("Name: "));
        p.add(memoryName);
        p.add(Box.createHorizontalGlue());
        p2.add(p);
        
        // attach controls
        sensorBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                checkSensorBox();
            }
        });
        memoryBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
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
            new java.beans.PropertyChangeListener(){
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (currentNode == null) return;
                    if (e.getOldValue()!=currentNode) return;
                    // right place, update
                    float value = ((Float)e.getNewValue()).floatValue();
                    if (currentNode.getComponent().isAnalog())
                        componentValue.setText(""+value);
                    else
                        componentValue.setText((value > 0.0) ? "Yes" : "No");
                }
            }
        );
    }
    
    void checkSensorBox() {
        if (currentNode == null) return;
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
        if (currentNode == null) return;
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
        if (currentNode.getController() != null) {
            controllerName.setText(currentNode.getController().getName());
            controllerType.setText(currentNode.getController().getType().toString());
        } else {
            controllerName.setText("");
            controllerType.setText("");
        }
        if (currentNode.getComponent() != null) {
            componentName.setText(currentNode.getComponent().getName());
            componentId.setText(currentNode.getComponent().getIdentifier().toString());
            if (currentNode.getComponent().isAnalog()) {
                componentAnalog.setText("Yes");
                componentValue.setText(""+currentNode.getValue());
                componentRelative.setText(currentNode.getComponent().isRelative()?"Yes":"No");
            } else {
                componentAnalog.setText("No");
                componentRelative.setText("");
                componentValue.setText((currentNode.getValue() > 0.0) ? "Yes" : "No");
            }
            if ( (currentNode.getAttachedSensor()!=null) && (!currentNode.getAttachedSensor().equals(""))) {
                sensorName.setText(currentNode.getAttachedSensor());
                sensorName.setEditable(false);
                sensorBox.setSelected(true);
            } else {
                sensorName.setText("");
                sensorName.setEditable(true);
                sensorBox.setSelected(false);
            }
            if ( (currentNode.getAttachedMemory()!=null) && (!currentNode.getAttachedMemory().equals(""))) {
                memoryName.setText(currentNode.getAttachedMemory());
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
            componentAnalog.setText("No");
            componentRelative.setText("No");
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
    JCheckBox sensorBox = new JCheckBox("Copy to JMRI Sensor  ");
    JTextField sensorName = new JTextField(25);
    JCheckBox memoryBox = new JCheckBox("Copy to JMRI Memory  ");
    JTextField memoryName = new JTextField(25);
    
    public UsbNode getSelectedElement() {
        if (!dTree.isSelectionEmpty() && dTree.getSelectionPath()!=null ) {
            // somebody has been selected
            log.debug("getSelectedIcon with "+dTree.getSelectionPath().toString());
            TreePath path = dTree.getSelectionPath();
            
            int level = path.getPathCount();
            // specific items are at level 3, no action above that
            
            return (UsbNode)path.getPathComponent(level-1);
        } else return null;
    }

    JTree dTree;

    static Logger log = Logger.getLogger(TreePanel.class.getName());
}

