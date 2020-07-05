package jmri.server.json;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

@API(status = EXPERIMENTAL)
public class JsonServerAction extends AbstractAction {

    public JsonServerAction(String s) {
        super(s);
    }

    public JsonServerAction() {
        this(Bundle.getMessage("MenuItemStartServer"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(JsonServer.class).start();
    }
}
