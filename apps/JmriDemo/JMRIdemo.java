/**
 * JMRIdemo.java
 */

package apps.JmriDemo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Main program, intended to demonstrate the contents of the JMRI libraries.
 * Since it contains everything, it's also used for testing.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author	Bob Jacobsen   Copyright 2002
 * @version     $Revision: 1.54 $
 */
public class JMRIdemo extends JPanel {

    // Main entry point
    public static void main(String args[]) {
    }

    static String configFile = null;

    // GUI members
    private JMenuBar menuBar;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JMRIdemo.class.getName());
}


