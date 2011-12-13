// ConfigToolAction.java

package jmri.jmrix.can.cbus.swing.configtool;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.util.ResourceBundle;

/**
 * Create and register a LocoStatsFrame object.
 * 
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version			$Revision$
 * @since 2.3.1
 */
public class ConfigToolAction extends AbstractAction {

    public ConfigToolAction(String s) {
        super(s);
    }
    
    public ConfigToolAction() {
        this(ResourceBundle
                .getBundle("jmri.jmrix.can.cbus.swing.configtool.ConfigToolBundle")
                        .getString("MenuItemConfigTool"));
    }

    public void actionPerformed(ActionEvent e) {
        ConfigToolFrame f = new ConfigToolFrame();
        f.setVisible(true);
    }
}

/* @(#)ConfigToolAction.java */
