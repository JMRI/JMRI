// MessageFrameAction.java

package jmri.jmrix.loconet.swing.throttlemsg;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Action to create and register a
 * MessageFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */public class MessageFrameAction extends AbstractAction {

     public MessageFrameAction(String s) {
         super(s);
     }

     public void actionPerformed(ActionEvent e) {

         MessageFrame f = new MessageFrame();
         f.setVisible(true);

     }

 }

/* @(#)MessageFrameAction.java */
