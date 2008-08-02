// ConfigToolPane.java

package jmri.jmrix.can.cbus.swing.configtool;

import jmri.jmrix.can.*;
import jmri.util.StringUtil;

import java.util.ResourceBundle;
import java.awt.FlowLayout;
import java.awt.event.*;

import javax.swing.*;

/**
 * Pane to ease creation of Sensor, Turnouts and Lights
 * that are linked to CBUS events.
 *
 * @author			Bob Jacobsen   Copyright (C) 2008
 * @version			$Revision: 1.1 $
 * @since 2.3.1
 */
public class ConfigToolPane extends JPanel implements CanListener {

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.configtool.ConfigToolBundle");
    
    JPanel lb2Panel;
    JPanel rawPanel;
    JPanel pr2Panel;
    JPanel ms100Panel;

    public ConfigToolPane() {
        super();
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // add event displays
        add(new JTextField(12));
        add(new JTextField(12));
        add(new JTextField(12));
        add(new JTextField(12));
        
        // add sensor
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAddSensor")));
        p1.add(new JLabel("test"));
        add(p1);

        // add turnout
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAddTurnout")));
        p2.add(new JLabel("test"));
        add(p2);

    }

    public void reply(jmri.jmrix.can.CanReply m) {
    }

    public void message(jmri.jmrix.can.CanMessage m) {
    }

    public void dispose() {
        // disconnect from the CBUS
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConfigToolPane.class.getName());
}
