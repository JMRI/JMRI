// JmriPlugin.java

package jmri;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 * Method for invoking user code at startup time.
 * <P>
 * This class provides a null static member.  By replacing
 * it with another implemention, the user can update configuration,
 * etc at startup time.
 *
 * @author			Bob Jacobsen Copyright (C) 2003
 * @version			$Revision$
 */
public class JmriPlugin {
     public static void start(JFrame mainFrame, JMenuBar menuBar) {}
}

/* @(#)JmriPlugin.java */
