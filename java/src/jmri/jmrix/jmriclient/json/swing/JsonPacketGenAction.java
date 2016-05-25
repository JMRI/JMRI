package jmri.jmrix.jmriclient.json.swing;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class JsonPacketGenAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -1938615445470223692L;
    private final JsonClientSystemConnectionMemo memo;

    JsonPacketGenAction(String message, JsonClientSystemConnectionMemo memo) {
        super(message);
        this.memo = memo;
    }

    public JsonPacketGenAction(JsonClientSystemConnectionMemo memo) {
        this("Generate JMRI Client message", memo);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JsonPacketGenFrame f = new JsonPacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }

        // connect to the traffic controller
        f.connect(memo.getTrafficController());
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(JsonPacketGenAction.class);
}
