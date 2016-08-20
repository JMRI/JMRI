package apps;

import apps.startup.AbstractStartupModel;

/**
 * A PerformScriptModel object runs a script when the program is started.
 * <P>
 * @author	Bob Jacobsen Copyright 2003
 * @author Randall Wood (c) 2016
 * @see apps.startup.PerformScriptModelFactory
 */
public class PerformScriptModel extends AbstractStartupModel {

    public String getFileName() {
        return this.getName();
    }

    public void setFileName(String n) {
        this.setName(n);
    }
}
