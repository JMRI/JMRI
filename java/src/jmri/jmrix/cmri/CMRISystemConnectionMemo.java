package jmri.jmrix.cmri;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Minimal SystemConnectionMemo for C/MRI systems.
 *
 * @author Randall Wood
 */
public class CMRISystemConnectionMemo extends SystemConnectionMemo {

    public CMRISystemConnectionMemo() {
        super("C", CMRIConnectionTypeList.CMRI);
        this.register();
        InstanceManager.store(this, CMRISystemConnectionMemo.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.cmri.CmriActionListBundle");
    }

}
