package jmri.script;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import javax.script.ScriptContext;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.util.PipeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class ScriptOutput {

    /**
     * JTextArea containing the output
     */
    private JTextArea output = null;
    private final static Logger log = LoggerFactory.getLogger(ScriptOutput.class);

    /**
     * Provide access to the JTextArea containing all ScriptEngine output.
     * <p>
     * The output JTextArea is not created until this is invoked, so that code
     * that doesn't use this feature can run on GUI-less machines.
     * <p>
     * This creates a "ScriptOutput PipeListener" thread which is not normally terminated.
     * @return component containing script output
     */
    public JTextArea getOutputArea() {
        if (output == null) {
            // convert to stored output

            try {
                // create the output area
                output = new JTextArea();

                // Add the I/O pipes
                PipedWriter pw = new PipedWriter();

                ScriptContext context = JmriScriptEngineManager.getDefault().getDefaultContext();
                context.setErrorWriter(pw);
                context.setWriter(pw);

                // ensure the output pipe is read and stored into a
                // Swing TextArea data model
                PipedReader pr = new PipedReader(pw);
                PipeListener pl = new PipeListener(pr, output);
                pl.setName("ScriptOutput PipeListener");
                pl.setDaemon(true);
                pl.start();
            } catch (IOException e) {
                log.error("Exception creating script output area", e);
                return null;
            }
        }
        return output;
    }

    static public ScriptOutput getDefault() {
        if (InstanceManager.getNullableDefault(ScriptOutput.class) == null) {
            InstanceManager.store(new ScriptOutput(), ScriptOutput.class);
        }
        return InstanceManager.getDefault(ScriptOutput.class);
    }

    /**
     * Write a script to the output area. The output is prepended with a leading
     * "&gt;&gt;&gt;" on the first line and a leading ellipsis on subsequent
     * lines.
     *
     * @param script The script to write.
     */
    static public void writeScript(final String script) {
        String output = ">>> " + script; // NOI18N
        // Strip ending newlines
        while (output.endsWith("\n")) { // NOI18N
            output = output.substring(0, output.length() - 1);
        }
        output = output.replaceAll("\n", "\n... "); // NOI18N
        output += "\n"; // NOI18N
        ScriptOutput.getDefault().getOutputArea().append(output);
    }
}
