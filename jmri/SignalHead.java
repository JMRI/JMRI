// SignalHead.java

package jmri;

/**
 * Represent a single signal head. (Try saying that ten times fast!)
 * A signal may have more than one of these to represent Diverging Appoach
 * etc.
 * <P>
 * Initially, this allows access to explicit appearance information. We
 * don't call this an Aspect, as that's a composite of the appearance
 * of several heads.
 *
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.1 $
 */
public interface SignalHead {

	// states are parameters; both closed and thrown is possible!
	public static final int NONE        = 0x00;
	public static final int RED         = 0x02;
	public static final int YELLOW      = 0x08;
	public static final int FLASHYELLOW = 0x10;
	public static final int GREEN       = 0x40;

	/**
     * Appearance is a bound parameter. Value values are the
     * various color contants defined in the class. As yet,
     * we have no decision as to whether these are exclusive or
     * can be or'd together.
     */
	public int getAppearance();

	/**
	 * Request a call-back when the bound KnownState property changes.
	 */
	public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

	/**
	 * Remove a request for a call-back when a bound property changes.
	 */
	public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

	/**
	 * Remove references to and from this object, so that it can
	 * eventually be garbage-collected.
	 */
	public void dispose();  // remove _all_ connections!

}


/* @(#)SignalHead.java */
