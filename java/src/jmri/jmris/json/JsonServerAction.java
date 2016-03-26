package jmri.jmris.json;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class JsonServerAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -6621731521962689693L;

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
