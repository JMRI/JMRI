package jmri.jmrix.marklin.swing;

import jmri.jmrix.marklin.MarklinSystemConnectionMemo;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Provide access to Swing components for the Marklin subsystem.
 *
 * @author Kevin Dickerson 2010
 */
@API(status = EXPERIMENTAL)
public class MarklinComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public MarklinComponentFactory(MarklinSystemConnectionMemo memo) {
        this.memo = memo;
    }
    MarklinSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    //JMenu currentMenu;
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new MarklinMenu(memo);
    }
}
