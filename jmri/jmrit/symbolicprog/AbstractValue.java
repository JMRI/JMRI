// AbstractValue.java

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Define common base class methods for CvValue and VariableValue classes
 *
 * Presently, this is just static definitions for states and
 * presentation colors, but other stuff may move in here
 * due to refactoring.
 *
 * Description:		Represents a single CV value
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: AbstractValue.java,v 1.3 2002-10-17 11:38:28 jacobsen Exp $
 */
public abstract class AbstractValue {

	// method to handle color changes for states
	abstract void setColor(Color c);

	/** Defines state when nothing is known about the real value */
	public static final int UNKNOWN  =   0;

	/** Defines state where value has been edited, no longer same as in decoder or file */
	public static final int EDITED  =   4;

	/** Defines state where value has been read from (hence same as) decoder, but perhaps
		not same as in file */
	public static final int READ     =  16;

	/** Defines state where value has been written to (hence same as) decoder, but perhaps
		not same as in file */
	public static final int STORED   =  64;

	/** Defines state where value was read from a config file, but might not be
		the same as the decoder */
	public static final int FROMFILE = 256;

	/** Define color to denote UNKNOWN state.  null means to use default for the component */
	static final Color COLOR_UNKNOWN  = Color.red.brighter();

	/** Define color to denote EDITED state.  null means to use default for the component */
	static final Color COLOR_EDITED  = Color.orange;

	/** Define color to denote READ state.  null means to use default for the component */
	static final Color COLOR_READ     = null;

	/** Define color to denote STORED state.  null means to use default for the component */
	static final Color COLOR_STORED   = null;

	/** Define color to denote FROMFILE state.  null means to use default for the component */
	static final Color COLOR_FROMFILE = Color.yellow;

}
