package jmri.jmrix.jmriclient.json.swing;

import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
class JsonClientMonitorAction extends PopupMenu {

    /**
     *
     */
    private static final long serialVersionUID = -5738608851589851778L;
    private final JsonClientSystemConnectionMemo memo;
    private final static Logger log = LoggerFactory.getLogger(JsonClientMonitorAction.class);

    public JsonClientMonitorAction(String message, JsonClientSystemConnectionMemo memo) {
        super(message);
        this.memo = memo;
    }

    public JsonClientMonitorAction(JsonClientSystemConnectionMemo memo) {
        this(Bundle.getMessage("JsonClientMonitor"), memo); // NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        // create a JMRIClientMonFrame
        JsonClientMonitorFrame f = new JsonClientMonitorFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("JsonClientMonitorAction starting JsonClientMonitorFrame: {}", ex);
        }
        f.setVisible(true);
    }

}
