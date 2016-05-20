// FollowerPanel.java

package jmri.jmrit.ussctc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;
import javax.swing.*;

/**
 * User interface frame for creating and editing "Follower" logic
 * on USS CTC machines.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision$
 */
public class FollowerPanel extends BasePanel {

    JTextField  outputName;
    JTextField  sensorName;
    JTextField  vetoName;
    
    JCheckBox   invert;
    
    JButton     viewButton;
    JButton     addButton;
    
    public FollowerPanel() {

        JPanel p2xs = new JPanel();
        JLabel label;
        p2xs.setLayout(new BoxLayout(p2xs, BoxLayout.Y_AXIS));
        
        // add output name field
        JPanel p3 = new JPanel();
        
        label = new JLabel(rb.getString("LabelOutputName"));
        label.setToolTipText(rb.getString("ToolTipFollowerOutput"));
        p3.add(label);
        outputName = new JTextField(12);
        outputName.setToolTipText(rb.getString("ToolTipFollowerOutput"));
        p3.add(outputName);
        p2xs.add(p3);

        // add sensor name field
        p3 = new JPanel();
        label = new JLabel(rb.getString("LabelSensorName"));
        label.setToolTipText(rb.getString("ToolTipFollowerSensor"));
        p3.add(label);
        sensorName = new JTextField(12);
        sensorName.setToolTipText(rb.getString("ToolTipFollowerSensor"));
        p3.add(sensorName);
        p2xs.add(p3);
        invert = new JCheckBox(rb.getString("ButtonInvert"));
        invert.setToolTipText(rb.getString("ToolTipFollowerInvert"));
        p2xs.add(invert);
        
        // add veto name field
        p3 = new JPanel();
        label = new JLabel(rb.getString("LabelVetoName"));
        label.setToolTipText(rb.getString("ToolTipFollowerVeto"));
        p3.add(label);
        vetoName = new JTextField(12);
        vetoName.setToolTipText(rb.getString("ToolTipFollowerVeto"));
        p3.add(vetoName);
        p2xs.add(p3);
        
        add(p2xs);
        
        // buttons
        p2xs = new JPanel();
        p2xs.setLayout(new BoxLayout(p2xs, BoxLayout.Y_AXIS));
        
        viewButton = new JButton(rb.getString("ButtonView"));
        viewButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    viewPressed();
                }
            });
        p2xs.add(viewButton);
        addButton = new JButton(rb.getString("ButtonAddUpdate"));
        addButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    addPressed();
                }
            });
        p2xs.add(addButton);
        add(p2xs);
    }

    void addPressed() {
        boolean ok = true;
        // validate
        ok &= validateTurnout(outputName.getText());
        ok &= validateSensor(sensorName.getText());
        if (!vetoName.getText().equals(""))
            ok &= validateSensor(vetoName.getText());
        
        // no errors?
        if (!ok) return;
        // create
        new Follower(outputName.getText(), sensorName.getText(), invert.isSelected(), vetoName.getText())
                    .instantiate();
    }

    void viewPressed() {
        try {
            Follower o =  new Follower(outputName.getText());
            sensorName.setText(o.getSensorName());
            invert.setSelected(o.getInvert());
            vetoName.setText(o.getVetoName());
        } catch (jmri.JmriException e) {
            log.error("Exception trying to find existing OS Indicator: "+e);
        }
    }
    
    static Logger log = LoggerFactory.getLogger(FollowerPanel.class.getName());

}
