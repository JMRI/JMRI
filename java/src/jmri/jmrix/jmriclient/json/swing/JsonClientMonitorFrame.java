package jmri.jmrix.jmriclient.json.swing;

import com.fasterxml.jackson.databind.JsonNode;
import jmri.jmrix.AbstractMonFrame;
import jmri.jmrix.jmriclient.json.JsonClientListener;
import jmri.jmrix.jmriclient.json.JsonClientMessage;
import jmri.jmrix.jmriclient.json.JsonClientReply;
import jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo;
import jmri.jmrix.jmriclient.json.JsonClientTrafficController;

/**
 *
 * @author rhwood
 */
class JsonClientMonitorFrame extends AbstractMonFrame implements JsonClientListener {

    protected JsonClientTrafficController trafficController = null;

    public JsonClientMonitorFrame(JsonClientSystemConnectionMemo memo) {
        super();
        this.trafficController = memo.getTrafficController();
    }

    @Override
    protected String title() {
        return Bundle.getMessage("JsonClientMonitor"); // NOI18N
    }

    @Override
    protected void init() {
        // connect to TrafficController
        this.trafficController.addJsonClientListener(this);
    }

    @Override
    public void dispose() {
        this.trafficController.removeJsonClientListener(this);
        super.dispose();
    }

    @Override
    public synchronized void message(JsonClientMessage l) {  // receive a message and log it
        nextLine("cmd: " + l.toString(), "");
    }

    @Override
    public synchronized void reply(JsonClientReply l) {  // receive a reply message and log it
        nextLine("rep: " + l.toString(), "");
    }

    @Override
    public void message(JsonNode message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reply(JsonNode reply) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
