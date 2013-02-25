// PacketGenAction.java

package jmri.jmrix.jmriclient.swing.packetgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 *       			PacketGenFrame object
 * 
 * @author Bob Jacobsen    Copyright (C) 2008
 * @version $Revision$
 */
public class PacketGenAction 			extends AbstractAction {

        jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo _memo=null;

	public PacketGenAction(String s,jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) { 
           super(s);
           _memo=memo;
        }

    public PacketGenAction(jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) {
        this("Generate JMRI Client message",memo);
        
    }

    public void actionPerformed(ActionEvent e) {
		PacketGenFrame f = new PacketGenFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			}
 
                // connect to the traffic controller
                f.connect(_memo.getJMRIClientTrafficController());
		f.setVisible(true);
	}
   static Logger log = LoggerFactory.getLogger(PacketGenAction.class.getName());
}


/* @(#)PacketGenAction.java */
