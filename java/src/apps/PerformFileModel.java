// PerformFileModel.java
package apps;

import apps.startup.AbstractStartupModel;

/**
 * A PerformFileModel object loads an xml file when the program is started.
 * <P>
 * @author	Bob Jacobsen Copyright 2003
 * @author Randall Wood (c) 2016
 * @version $Revision$
 * @see apps.startup.PerformFileModelFactory
 */
public class PerformFileModel extends AbstractStartupModel {

    public String getFileName() {
        return this.getName();
    }

    public void setFileName(String n) {
        this.setName(n);
    }
}
