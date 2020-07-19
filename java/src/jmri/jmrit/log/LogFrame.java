package jmri.jmrit.log;

import javax.swing.BoxLayout;

/**
 * Frame for adding to the log file.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 * @deprecated since 4.21.1; use {@link apps.jmrit.log.LogFrame} instead
 */
@Deprecated
public class LogFrame extends jmri.util.JmriJFrame {

    public LogFrame() {
        super();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void initComponents() {

        setTitle(Bundle.getMessage("LogInputTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(new LogPanel());

        pack();
    }
}
