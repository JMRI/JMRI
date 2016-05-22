// JmriJFrameAction.java

 package jmri.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Default form of action to create an object that's
 * from a child class of JmriJFrame.  By using reflection,
 * this cuts the loader dependency on the loaded class.
 *
 * @author		Bob Jacobsen Copyright (C) 2007
 * @version		$Revision$
 */
 
public class JmriJFrameAction extends AbstractAction {

 	public JmriJFrameAction(String s) {
    	super(s);
     }
     
     /**
      * Method to be overridden to make this work.
      * Provide a completely qualified class name,
      * must be castable to JmriJFrame
      */
     public String getName() { return ""; }
     
     public void actionPerformed(ActionEvent e) {
        String name = getName();
        JmriJFrame j = null;
        
        if (!name.equals("")) {
            try {
                j = (JmriJFrame) Class.forName(name).newInstance();
                j.initComponents();
                j.setVisible(true);
            } catch (java.lang.ClassNotFoundException ex1) {
                log.error("Couldn't create window, because couldn't find class: "+ex1);
            } catch (Exception ex2) {
                log.error("Exception creating frame: "+ex2);
            }
        }
     }
     
    static Logger log = LoggerFactory.getLogger(JmriJFrameAction.class.getName());
}

/* @(#)JmriJFrameAction.java */
