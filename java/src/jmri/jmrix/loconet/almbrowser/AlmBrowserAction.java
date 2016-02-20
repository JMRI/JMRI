// AlmBrowserAction.java
package jmri.jmrix.loconet.almbrowser;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.loconet.LnTrafficController;

/**
 * Swing action to create and register a AlmBrowser object
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Version:$
 * @deprecated 2.13.5, Does not work with the multi-connection correctly,
 * believe not to work correctly before hand and that the feature is not used.
 */
@Deprecated
public class AlmBrowserAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 5146701688497694865L;

    public AlmBrowserAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        // create a AlmBrowser
        AlmBrowserFrame f = new AlmBrowserFrame();
//		try {
        f.initComponents();
//			}
//		catch (Exception ex) {
//			log.warn("AlmBrowserAction starting AlmBrowser: Exception: "+ex.toString());
//			}
        // connect to the LnTrafficController
        f.connect(LnTrafficController.instance());
        // make visible
        f.setVisible(true);

    }

}


/* @(#)AlmBrowserAction.java */
