// NetworkDriverAction.java

package jmri.jmrix.easydcc.networkdriver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			NetworkDriverFrame object.
 *
 * @author			Bob Jacobsen    Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class NetworkDriverAction extends AbstractAction  {

    public NetworkDriverAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        // create a SerialDriverFrame
        NetworkDriverFrame f = new NetworkDriverFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("starting NetworkDriverFrame caught exception: "+ex.toString());
        }
        f.show();
    };

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NetworkDriverAction.class.getName());

}


/* @(#)NetworkDriverAction.java */
