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
 * These are manipulated externally, for example by a
 * {@link PanelEditor}.  They are generally not stored
 * directly as part of the state of the object, though they
 * could be, but as part of the state of the external control.
 *
 * <p>Copyright: Bob Jacobsen Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.3 $
 */
public interface Positionable {
    public void setPositionable(boolean enabled);
    public boolean getPositionable();

    public void setEditable(boolean enabled);
    public boolean getEditable();

    public void setControlling(boolean enabled);
    public boolean getControlling();
}