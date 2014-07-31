package jmri.jmrix.jmriclient.json.swing;

import javax.swing.JMenu;
import jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo;

/**
 *
 * @author Bob Jacobsen Copyright 2008
 * @author Randall Wood 2014
 */
public class JsonClientMenu extends JMenu {

    JsonClientSystemConnectionMemo memo = null;

    public JsonClientMenu(String name, JsonClientSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public JsonClientMenu(JsonClientSystemConnectionMemo memo) {
        super();
        this.memo = memo;
        setText(Bundle.getMessage("MenuItemJsonClient"));
        add(new JsonClientMonitorAction(Bundle.getMessage("JsonClientMonitor"), this.memo));
        add(new JsonPacketGenAction(Bundle.getMessage("JsonClientSendCommand"), this.memo));
    }

}
