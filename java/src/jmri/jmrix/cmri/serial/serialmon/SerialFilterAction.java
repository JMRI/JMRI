// SerialFilterAction.java

package jmri.jmrix.cmri.serial.serialmon;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * CMRInet Serial monitor packet filter
 * 
 * @author                      Chuck Catania  2016
 * @version
 */
public class SerialFilterAction extends AbstractAction {

	public SerialFilterAction(String s) { super(s);}

    public SerialFilterAction() {
        this("CMRInet Message Filter");
    }

    public void actionPerformed(ActionEvent e) {
		// create a SerialMonFrame
		SerialFilterFrame f = new SerialFilterFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("SerialFilterAction starting SerialFilterFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialFilterAction.class.getName());

}


/* @(#)SerialFilterAction.java */
