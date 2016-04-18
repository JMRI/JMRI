package apps.startup;

import java.io.File;

/**
 *
 * @author Randall Wood
 */
public class ScriptButtonModel extends AbstractStartupModel {

    private File script;

    public File getScript() {
        return this.script;
    }

    public void setScript(File script) {
        this.script = script;
    }

}
