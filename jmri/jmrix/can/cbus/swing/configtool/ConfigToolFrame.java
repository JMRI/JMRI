// ConfigToolFrame.java

package jmri.jmrix.can.cbus.swing.configtool;

import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Pane to ease creation of Sensor, Turnouts and Lights
 * that are linked to CBUS events.
 *
 * @author			Bob Jacobsen   Copyright (C) 2008
 * @version			$Revision: 1.4 $
 * @since 2.3.1
 */
public class ConfigToolFrame extends jmri.util.JmriJFrame {

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.configtool.ConfigToolBundle");
    
    ConfigToolPane pane;
    
    public ConfigToolFrame() {
        this(ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.configtool.ConfigToolBundle").getString("Title"));
    }
    
    public ConfigToolFrame(String Name) {
        super(Name);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add GUI items
        pane = new ConfigToolPane();
        getContentPane().add(pane);
        
        // add help
    	addHelpMenu("package.jmri.jmrix.can.cbus.swing.configtool.ConfigToolFrame", true);
        
        // prep for display
        pack();
    }



    public void dispose() {
        // disconnect the config pane from the CBUS
        pane.dispose();

        // take apart the JFrame
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConfigToolFrame.class.getName());
}
