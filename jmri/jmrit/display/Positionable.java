package jmri.jmrit.display;

/**
 * Defines display objects.
 * <P>
 * These are capable of:
 * <UL>
 * <LI>Being positioned by being dragged around on the screen.
 * (See {@link setPositionable})
 * <LI>Having their properties edited. (See {@link setEditable})
 * <LI>Controlling the layout. (See {@link setControlling})
 * </OL>
 * This is here to allow us to (eventually) turn on and off
 * this capability in Components in a Container.
 * <p>Copyright: Bob Jacobsen Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.2 $
 */
public interface Positionable {
    public void setPositionable(boolean enabled);
    public boolean getPositionable();

    public void setEditable(boolean enabled);
    public boolean getEditable();

    public void setControlling(boolean enabled);
    public boolean getControlling();
}