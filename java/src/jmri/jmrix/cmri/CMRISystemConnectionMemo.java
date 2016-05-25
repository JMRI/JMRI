package jmri.jmrix.cmri;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Minimal SystemConnectionMemo for C/MRI systems.
 *
 * @author Randall Wood
 */
public class CMRISystemConnectionMemo extends SystemConnectionMemo {

    public CMRISystemConnectionMemo() {
        super("C", CMRIConnectionTypeList.CMRI);
        register(); // registers general type
        jmri.InstanceManager.store(this, CMRISystemConnectionMemo.class); // also register as specific type
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.cmri.CmriActionListBundle");
    }

}
