// SoundSetAction.java

package jmri.jmrix.rps.swing.soundset;

import org.apache.log4j.Logger;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 *       			SoundSetFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version         $Revision$
 */
public class SoundSetAction extends AbstractAction {

	public SoundSetAction(String s) { super(s);}

    public SoundSetAction() {
        this("RPS Sound Speed Monitor");
    }

    public void actionPerformed(ActionEvent e) {
        log.debug("starting frame creation");
		SoundSetFrame f = new SoundSetFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("starting frame: Exception: "+ex.toString());
			}
		f.setVisible(true);

	}

	static Logger log = Logger.getLogger(SoundSetAction.class.getName());

}


/* @(#)SoundSetAction.java */
