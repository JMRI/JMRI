// LogFrame.java
package jmri.jmrit.log;

import javax.swing.BoxLayout;

/**
 * Frame for adding to the log file.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version	$Revision$
 */
public class LogFrame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 5024411537936197158L;

    public LogFrame() {
        super();
    }

    public void initComponents() throws Exception {

        setTitle("Make Log Entry");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(new LogPanel());

        pack();
    }
}
