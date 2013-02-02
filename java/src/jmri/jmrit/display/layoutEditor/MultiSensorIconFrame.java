package jmri.jmrit.display.layoutEditor;

import org.apache.log4j.Logger;
import jmri.util.JmriJFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import jmri.jmrit.display.MultiSensorIcon;

/**
 * Provides a simple editor for creating a MultiSensorIcon object
 * <p>
 * To work right, the MultiSensorIcon needs to have all
 * images the same size, but this is not enforced here. 
 * It should be.
 *
 * @author  Bob Jacobsen  Copyright (c) 2007
 * @version $Revision$
 */

public class MultiSensorIconFrame extends JmriJFrame {
    JPanel content = new JPanel();
    JmriJFrame defaultsFrame;
    MultiIconEditor defaultIcons;
	LayoutEditor layoutEditor = null;
    JRadioButton updown = new JRadioButton("Up - Down");
    JRadioButton rightleft = new JRadioButton("Right - Left");
    ButtonGroup group = new ButtonGroup();
    
    MultiSensorIconFrame(LayoutEditor p) {
        super("Enter MultiSensor");
        layoutEditor = p;
        
        addHelpMenu("package.jmri.jmrit.display.MultiSensorIconFrame", true);
    }
    
    public void initComponents() {
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel("Icon checks click: "));
        group.add(updown);
        group.add(rightleft);
        rightleft.setSelected(true);
        p.add(rightleft);
        p.add(updown);
        this.getContentPane().add(p);
        
        this.getContentPane().add(content);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        // start with three Entrys; there's no reason to have less
        content.add(new Entry(content, this, "resources/icons/USS/plate/levers/l-left.gif"));
        content.add(new Entry(content, this, "resources/icons/USS/plate/levers/l-vertical.gif"));
        content.add(new Entry(content, this, "resources/icons/USS/plate/levers/l-right.gif"));
                
        this.getContentPane().add(new JSeparator());
        JButton b = new JButton("Add Additional Sensor to Icon"); 
        ActionListener a = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                // remove this entry
                self.add(new Entry(self, frame, "resources/icons/USSpanels/Plates/lever-v.gif"));
                frame.pack();
            }
            JPanel self;
            JmriJFrame frame;
            ActionListener init(JPanel self, JmriJFrame frame) {
                this.frame = frame;
                this.self = self;
                return this;
            }
        }.init(content, this);
        b.addActionListener(a);
        this.getContentPane().add(b);

        this.getContentPane().add(new JSeparator());
        b = new JButton("Set icons for inactive, ...");
        defaultIcons = new MultiIconEditor(3);
            defaultIcons.setIcon(0, "Unknown:","resources/icons/USS/plate/levers/l-inactive.gif");
            defaultIcons.setIcon(1, "Inconsistent:","resources/icons/USS/plate/levers/l-unknown.gif");
            defaultIcons.setIcon(2, "Inactive:","resources/icons/USS/plate/levers/l-inconsistent.gif");
            defaultIcons.complete();
        defaultsFrame = new JmriJFrame("", false, true);
            defaultsFrame.getContentPane().add(new JLabel("  Select new file, then click on icon to change  "),BorderLayout.NORTH);
            defaultsFrame.getContentPane().add(defaultIcons);
            defaultsFrame.pack();
            defaultsFrame.addHelpMenu("package.jmri.jmrit.display.MultiSensorIconDefaultsFrame", true);
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent a){
                defaultsFrame.setVisible(true);
            }
        });
        this.getContentPane().add(b);
        
        this.getContentPane().add(new JSeparator());
        b = new JButton("Create and Add Icon To Panel");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent a){
                make();
				removeWindows();
            }
        });        
        this.getContentPane().add(b);
    }
    
    // Remove an Entry from the panel, 
    // and therefore from the eventual sensor
    void remove(Entry e) {
        content.remove(e);
        this.pack();
    }
    
    void make() {
        MultiSensorIcon m = new MultiSensorIcon(layoutEditor);
        m.setUnknownIcon(defaultIcons.getIcon(0));
        m.setInconsistentIcon(defaultIcons.getIcon(1));
        m.setInactiveIcon(defaultIcons.getIcon(2));
        
        for (int i = 0; i< content.getComponentCount(); i++) {
            Entry e = (Entry)content.getComponent(i);
            m.addEntry(e.sensor.getText(), e.ed.getIcon(0));
        }
        m.setUpDown(updown.isSelected());
        m.setDisplayLevel(jmri.jmrit.display.Editor.SENSORS);

        layoutEditor.addMultiSensor(m);
    
	}
	
	void removeWindows() {
		for (int i = 0; i<content.getComponentCount(); i++) {
			((Entry)content.getComponent(i)).dispose();
		}
		defaultsFrame.dispose();
		super.dispose();
	}
    
    class Entry extends JPanel {

        JTextField sensor = new JTextField(5);
        JPanel self;
        MultiIconEditor ed = new MultiIconEditor(1);
        JmriJFrame edf = new JmriJFrame("", false, true);
        
        public String toString() {
            return ed.getIcon(0).toString();
        }
        
        Entry(JPanel self, JmriJFrame frame, String name) {
            this.self = self;
            this.setLayout(new FlowLayout());
            this.add(new JLabel("Sensor:"));
            
            this.add(sensor);
            
            ed.setIcon(0, "Active:", name);
            ed.complete();
            edf.getContentPane().add(new JLabel("  Select new file, then click on icon to change  "),BorderLayout.NORTH);
            edf.getContentPane().add(ed);
            edf.pack();

            JButton b = new JButton("Set Icon...");
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    edf.setVisible(true);
                }
            });
            this.add(b);
            
            // button to remove this entry from it's parent 
            b = new JButton("Delete");
            ActionListener a = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    // remove this entry
                    self.remove(entry);
                    frame.pack();
                }
                Entry entry;
                JPanel self;
                JmriJFrame frame;
                ActionListener init(Entry entry, JPanel self, JmriJFrame frame) {
                    this.entry = entry;
                    this.self = self;
                    this.frame = frame;
                    return this;
                }
            }.init(this, self, frame);
            b.addActionListener(a);
            
            this.add(b);
        }
		public void dispose() {
			edf.dispose();
		}
    }
    
    // initialize logging
    static Logger log = Logger.getLogger(MultiSensorIconFrame.class.getName());
}
