// SignalHead.java

package jmri;

/**
 * Represent a single signal head. (Try saying that ten times fast!)
 * A signal may have more than one of these 
 * (e.g. a signal mast consisting of several heads)
 * when needed to represent more complex aspects, e.g. Diverging Appoach
 * etc.
 * <P>
 * Initially, this allows access to explicit appearance information. We
 * don't call this an Aspect, as that's a composite of the appearance
 * of several heads.
 * <P>
 * This class has two bound parameters:
 *<DL>
 *<DT>appearance<DD>The specific color being shown. Values are the
 * various color contants defined in the class. As yet,
 * we have no decision as to whether these are exclusive or
 * can be or'd together.
 *<DT>lit<DD>Whether the head's lamps are lit or left dark.
 *<P>
 *This differs from the DARK color defined for the appearance
 * parameter, in that it's independent of that.  Lit is 
 * intended to allow you to extinquish a signal head for 
 * approach lighting, while still allowing it's color to be
 * set to a definite value.
 *</dl>
 *
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.7 $
 */
public interface SignalHead extends NamedBean {

    public static final int DARK        = 0x00;
    public static final int RED         = 0x01;
    public static final int FLASHRED    = 0x02;
    public static final int YELLOW      = 0x04;
    public static final int FLASHYELLOW = 0x08;
    public static final int GREEN       = 0x10;
    public static final int FLASHGREEN  = 0x20;

    /**
     * Appearance is a bound parameter. 
     */
    public int getAppearance();
    public void setAppearance(int newAppearance);

    /**
     * Lit is a bound parameter. It controls
     * whether the signal head's lamps are lit or left dark.
     */
    public boolean getLit();
    public void setLit(boolean newLit);

}


/* @(#)SignalHead.java */
