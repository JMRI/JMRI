// SerialFilterAction.java

package jmri.jmrix.cmri.serial.serialmon;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * CMRInet Serial monitor packet filter
 * 
 * @author                      Chuck Catania  2016
 */
public class SerialFilterAction extends AbstractAction {

    private CMRISystemConnectionMemo _memo = null;
	
    public SerialFilterAction(String s, CMRISystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    public SerialFilterAction(CMRISystemConnectionMemo memo) {
        this(Bundle.getMessage("WindowTitle"), memo);
//        this("CMRInet Message Filter");
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
