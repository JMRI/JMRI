// ProgModeSelector.java

package jmri;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.Programmer;
import jmri.ProgListener;

/**
 * Provide a JPanel to configure the programming mode.
 * <P>
 * The using code should get a configured programmer with getProgrammer.
 * <P>
 * This pane will only display ops mode options if ops mode is available,
 * as evidenced by an attempt to get an ops mode programmer at startup time.
 * <P>
 * For service mode, you can get the programmer either from here
 * or direct from the instance manager.  For ops mode, you have to
 * get it from here.
 * <P>
 * Note that you should call the dispose() method when you're really done, so that
 * a ProgModeSelector object can disconnect its listeners.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public abstract class ProgModeSelector extends javax.swing.JPanel {

    /**
     * Get the configured programmer
     */
    abstract public Programmer getProgrammer();

    /**
     * Does this object have sufficient selection information to
     * provide a programmer?
     * @return true if a programmer is available
     */
    abstract public boolean isSelected();

    /**
     * Clean up when done. Required.
     */
    abstract public void dispose();

}
