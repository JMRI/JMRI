// PowerManagerMenu.java

package jmri.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JMenu;

import jmri.InstanceManager;
import jmri.PowerManager;

/**
 * Create a menu for selecting the Power Manager to use
 *
 * @author	Bob Jacobsen   Copyright 2010
 * @version     $Revision: 1.1 $
 * @since 2.9.5
 */
public class PowerManagerMenu extends JMenu {

    /**
     * Get the currently selected manager
     */
    public PowerManager get() {
        return null;
    }
    
    /**
     * Create a PowerManager menu.
     */
    public PowerManagerMenu() {
        super();

        //ResourceBundle rb = LocoNetBundle.bundle();

        //setText(memo.getUserName());
        setText("test");
        
        //jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        List<Object> managers = InstanceManager.getList(PowerManager.class);
        List<Action> actions = new ArrayList<Action>();
        
            add("foo");
            //add(new LnNamedPaneAction( rb.getString(item.name), wi, item.load, memo));

    }
}


