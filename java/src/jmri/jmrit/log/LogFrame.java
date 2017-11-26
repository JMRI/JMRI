package jmri.jmrit.log;

import javax.swing.BoxLayout;

/**
 * Frame for adding to the log file.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
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
