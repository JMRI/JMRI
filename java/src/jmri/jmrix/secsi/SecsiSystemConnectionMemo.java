package jmri.jmrix.secsi;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Minimum required implementation.
 *
 * @author Randall Wood {@literal <randall.h.wood@alexandriasoftware.com>}
 */
public class SecsiSystemConnectionMemo extends SystemConnectionMemo {

    public SecsiSystemConnectionMemo() {
        super("V", "SECSI");
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

}
