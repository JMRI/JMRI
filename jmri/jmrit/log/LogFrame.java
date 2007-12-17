// LogFrame.java

package jmri.jmrit.log;

import jmri.*;
import jmri.util.*;
import java.awt.*;

import javax.swing.*;

/**
 * Frame for adding to the log file.
 * 
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision: 1.3 $
 */
public class LogFrame extends jmri.util.JmriJFrame {

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
