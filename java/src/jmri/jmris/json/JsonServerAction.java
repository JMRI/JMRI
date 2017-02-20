package jmri.jmris.json;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class JsonServerAction extends AbstractAction {

    public JsonServerAction(String s) {
        super(s);
    }

    public JsonServerAction() {
        this(Bundle.getMessage("MenuItemStartServer"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JsonServer.getDefault().start();
    }
}
