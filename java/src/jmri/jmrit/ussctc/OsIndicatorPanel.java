// OsIndicatorPanel.java

package jmri.jmrit.ussctc;

import org.apache.log4j.Logger;
import java.awt.event.*;
import javax.swing.*;

/**
 * User interface frame for creating and editing "OS Indicator" logic
 * on USS CTC machines.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision$
 */
public class OsIndicatorPanel extends BasePanel {

    JTextField outputName;
    JTextField sensorName;
    JTextField lockName;

    JButton viewButton;
    JButton addButton;
    
    public OsIndicatorPanel() {

        JPanel p2xs = new JPanel();
        JLabel label;
        p2xs.setLayout(new BoxLayout(p2xs, BoxLayout.Y_AXIS));
        
        // add output name field
        JPanel p3 = new JPanel();
        
        label = new JLabel(rb.getString("LabelOutputName"));
        label.setToolTipText(rb.getString("ToolTipOsIndicatorOutput"));
        p3.add(label);
        outputName = new JTextField(12);
        outputName.setToolTipText(rb.getString("ToolTipOsIndicatorOutput"));
        p3.add(outputName);
        p2xs.add(p3);

        // add sensor name field
        p3 = new JPanel();
        label = new JLabel(rb.getString("LabelSensorName"));
        label.setToolTipText(rb.getString("ToolTipOsIndicatorSensor"));
        p3.add(label);
        sensorName = new JTextField(12);
        sensorName.setToolTipText(rb.getString("ToolTipOsIndicatorSensor"));
        p3.add(sensorName);
        p2xs.add(p3);
        
        // add lock name field
        p3 = new JPanel();
        label = new JLabel(rb.getString("LabelLockName"));
        label.setToolTipText(rb.getString("ToolTipOsIndicatorLock"));
        p3.add(label);
        lockName = new JTextField(12);
        lockName.setToolTipText(rb.getString("ToolTipOsIndicatorLock"));
        p3.add(lockName);
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
        if (!lockName.getText().equals(""))
            ok &= validateMemory(lockName.getText());
        
        // no errors?
        if (!ok) return;
        // create
        new OsIndicator(outputName.getText(), sensorName.getText(), lockName.getText())
                    .instantiate();
    }

    void viewPressed() {
        try {
            OsIndicator o =  new OsIndicator(outputName.getText());
            sensorName.setText(o.getOsSensorName());
            lockName.setText(o.getLockName());
        } catch (jmri.JmriException e) {
            log.error("Exception trying to find existing OS Indicator: "+e);
        }
    }
    
    static Logger log = Logger.getLogger(OsIndicatorPanel.class.getName());

}
