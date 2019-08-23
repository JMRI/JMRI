package jmri.jmrix.cmri.serial.serialmon;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Override
    public void actionPerformed(ActionEvent e) {
		// create a SerialMonFrame
		SerialFilterFrame f = new SerialFilterFrame(_memo);
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.warn("SerialFilterAction starting SerialFilterFrame: Exception: "+ex.toString());
			}
		f.setVisible(true);
	}

	private final static Logger log = LoggerFactory.getLogger(SerialFilterAction.class);

}
