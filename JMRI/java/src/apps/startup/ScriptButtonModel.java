package apps.startup;

import apps.Apps;
import apps.gui3.Apps3;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import jmri.JmriException;
import jmri.script.JmriScriptEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class ScriptButtonModel extends AbstractStartupModel {

    private File script;
    private final static Logger log = LoggerFactory.getLogger(ScriptButtonModel.class);

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

    @Override
    public void performAction() throws JmriException {
        if (Apps.buttonSpace() != null) {
            JButton b = new JButton(new ScriptButtonAction(this));
            Apps.buttonSpace().add(b);
        } else if (Apps3.buttonSpace() != null) {
            JButton b = new JButton(new ScriptButtonAction(this));
            Apps3.buttonSpace().add(b);
        }
    }

    private static class ScriptButtonAction extends AbstractAction {

        ScriptButtonModel model;

        public ScriptButtonAction(ScriptButtonModel model) {
            this.model = model;
            super.putValue(Action.NAME, model.getName());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                JmriScriptEngineManager.getDefault().eval(model.getScript());
            } catch (ScriptException | IOException ex) {
                log.error("Unable to run script {}.", model.getScript(), ex);
            }
        }
    }

}
