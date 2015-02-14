package jmri.jmrix.jmriclient.json.swing;

import javax.swing.JMenu;
import jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo;
import jmri.jmrix.swing.ComponentFactory;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonClientComponentFactory extends ComponentFactory {

    private final JsonClientSystemConnectionMemo memo;

    public JsonClientComponentFactory(JsonClientSystemConnectionMemo memo) {
        this.memo = memo;
    }

    @Override
    public JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new JsonClientMenu(memo);
    }

}
