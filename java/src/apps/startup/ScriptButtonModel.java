package apps.startup;

import java.io.File;
import java.text.MessageFormat;

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
    
    @Override
    public String toString() {
        return MessageFormat.format("<html>{0}<br>{1}</html>", this.getName(), this.getScript());
    }

}
