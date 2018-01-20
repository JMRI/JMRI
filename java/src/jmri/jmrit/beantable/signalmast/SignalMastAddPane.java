package jmri.jmrit.beantable.signalmast;

import java.awt.*;

import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import jmri.spi.JmriServiceProviderInterface;

/**
 * Definition of JPanel used to configure a specific SignalMast type
 *
 * Implementing classes <em>must</em> be registered as service providers of this
 * type to be recognized and usable.
 * <p>
 * General design documentation is available on the 
 * <a href="http://jmri.org/help/en/html/doc/Technical/SystemStructure.shtml">Structure of External System Connections page</a>.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2018
 * @see JmrixConfigPane
 * @see java.util.ServiceLoader
 * @since 4.11.2
 */
public abstract class SignalMastAddPane extends JPanel implements JmriServiceProviderInterface {

    /**
     * @return Human-prefered name for type of signal mast, in local language
     */
    @Nonnull abstract public String getPaneName();
    
    /**
     * Is this pane available, given the current configuration of the program?
     * In other words, are all necessary managers and other objects present?
     */
    public boolean isAvailable() { return true; }

}
