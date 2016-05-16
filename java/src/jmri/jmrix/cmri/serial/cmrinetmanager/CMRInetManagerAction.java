// CMRInetManagerAction.java

package jmri.jmrix.cmri.serial.cmrinetmanager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * CMRInetManagerAction object
 *
 * @author	Chuck Catania    Copyright (C) 2014, 2015, 2016
 * @version	$Revision: 17977 $
 */
public class CMRInetManagerAction extends AbstractAction {

	public CMRInetManagerAction(String s) { super(s);}

    public CMRInetManagerAction() {
        this("CMRInet Network Manager");
    }

    public void actionPerformed(ActionEvent e) {
        CMRInetManagerFrame f = new CMRInetManagerFrame();
        try {
            f.initComponents();
            }
        catch (Exception ex) {
            log.error("Exception-C2: "+ex.toString());
            }
        f.setLocation(20,40);
        f.setVisible(true);
    }
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CMRInetManagerFrame.class.getName());
}


/* @(#)CMRInetManagerAction.java */
