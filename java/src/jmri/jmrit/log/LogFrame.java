package jmri.jmrit.log;

import javax.swing.BoxLayout;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Frame for adding to the log file.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
@API(status = MAINTAINED)
public class LogFrame extends jmri.util.JmriJFrame {

    public LogFrame() {
        super();
    }

    @Override
    public void initComponents() {

        setTitle(Bundle.getMessage("LogInputTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(new LogPanel());

        pack();
    }
}
