package jmri.jmrit.display;

/**
 * <p>Title: Positionable is a an interface for objects
 * that can be dragged around on the screen.</p>
 * This is here to allow us to (eventually) turn on and off
 * this capability in Components in a Container.
 * <p>Copyright: Bob Jacobsen Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.1 $
 */
public interface Positionable {
    public void setPositionable(boolean enabled);
    public boolean getPositionable();
}