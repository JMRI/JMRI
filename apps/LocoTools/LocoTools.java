/**
 * LocoTools.java
 */

package apps.LocoTools;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Main program for a collection of LocoNet tools.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 * @author			Bob Jacobsen
 * @version         $Revision: 1.17 $
 */
public class LocoTools extends JPanel {

    // Main entry point
    public static void main(String args[]) {

    }

    static String configFile = null;

    // GUI members
    private JMenuBar menuBar;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoTools.class.getName());
}

