package jmri.jmrix.jmriclient.json;

import jmri.jmris.json.JsonServerPreferences;
import jmri.jmrix.AbstractNetworkPortController;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonNetworkPortController extends AbstractNetworkPortController {

    public JsonNetworkPortController() {
        super(new JsonClientSystemConnectionMemo());
        this.setPort(JsonServerPreferences.DEFAULT_PORT);
    }

    @Override
    public void configure() {
        // connect to the traffic controller
        JsonClientTrafficController control = new JsonClientTrafficController();
        control.connectPort(this);
        this.getSystemConnectionMemo().setTrafficController(control);
    }

    @Override
    public JsonClientSystemConnectionMemo getSystemConnectionMemo() {
        return (JsonClientSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}
