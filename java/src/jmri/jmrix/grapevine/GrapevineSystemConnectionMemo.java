package jmri.jmrix.grapevine;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Minimum required SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class GrapevineSystemConnectionMemo extends SystemConnectionMemo {

    public GrapevineSystemConnectionMemo() {
        super("G", "Grapevine");
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.grapevine.GrapevineActionListBundle");
    }

}
