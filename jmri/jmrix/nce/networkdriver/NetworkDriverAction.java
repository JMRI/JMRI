// NetworkDriverAction.java

package jmri.jmrix.nce.networkdriver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			NetworkDriverFrame object.
 *
 * @author			Bob Jacobsen    Copyright (C) 2003
 * @version			$Revision: 1.5 $
 */
 @Deprecated 
public class NetworkDriverAction extends AbstractAction  {

    public NetworkDriverAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NetworkDriverAction.class.getName());

}


/* @(#)NetworkDriverAction.java */
