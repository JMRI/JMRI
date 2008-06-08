// RpsTrackingFrame.java

package jmri.jmrix.rps.trackingpanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Frame containing the entire display tool
 *
 * @author			Bob Jacobsen    Copyright (C) 2006, 2008
 * @version         $Revision: 1.2 $
 */
public class RpsTrackingFrame extends jmri.util.JmriJFrame {
    
    public RpsTrackingFrame(String s) { super(s);}
    
    public RpsTrackingFrame() {
        this("RPS Tracking Display");
    }
    
    public void initComponents() {
        
        addHelpMenu("package.jmri.jmrix.rps.trackingpanel.RpsTrackingFrame", true);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // add controls; first, clear button
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        JPanel line = new JPanel();
        line.setLayout(new FlowLayout());
        
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    clearButtonChanged();
                }
            });
        line.add(clearButton);
        
        // button for handling errors
        showButton = new JCheckBox("Show error points");
        showButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    showButtonChanged();
                }
            });
        line.add(showButton);
        controls.add(line);
        
        // then alignment control
        panel = new RpsTrackingPanel();
        RpsTrackingControlPane scale = new RpsTrackingControlPane(panel);
        controls.add(scale);
        controls.add(new JSeparator());
        
        panel.setSize(440,240);
        panel.setPreferredSize(new Dimension(440,240));
        
        scale.set(-20.,-20.,400.,400.);  // lower left X, Y, then upper right 
        scale.update();
        
        // combine
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, controls, panel);
        getContentPane().add(split);
        
        pack();        
    }
    
    JButton clearButton;
    JCheckBox showButton;
    RpsTrackingPanel panel;
    
    void showButtonChanged() {
        panel.setShowErrors(showButton.isSelected());
    }
    void clearButtonChanged() {
        panel.clear();
    }
}


/* @(#)RpsTrackingFrame.java */
