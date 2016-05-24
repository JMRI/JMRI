package jmri.jmrix.oaktree;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Minimum required SystemConnectionMemo.
 *
 * @author Randall Wood {@literal <randall.h.wood@alexandriasoftware.com>}
 */
public class OakTreeSystemConnectionMemo extends SystemConnectionMemo {

    public OakTreeSystemConnectionMemo() {
        super("O", "Oak Tree");
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

}
