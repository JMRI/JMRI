package jmri.jmrit.display;

import java.awt.Point;
import java.awt.event.MouseListener;

/**
 * Defines display objects.
 * <P>
 * These are capable of:
 * <UL>
 * <LI>Being positioned by being dragged around on the screen.
 * (See {@link #setPositionable})
 * <LI>Having their properties edited. (See {@link #setEditable})
 * <LI>Controlling the layout. (See {@link #setControlling})
 * </OL>
 * These are manipulated externally, for example by a
 * {@link PanelEditor}.  They are generally not stored
 * directly as part of the state of the object, though they
 * could be, but as part of the state of the external control.
 *
 * <p>Copyright: Bob Jacobsen Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.7 $
 */
public interface Positionable  {
    public void setPositionable(boolean enabled);
    public boolean getPositionable();
    
    public void setEditable(boolean enabled);
    public boolean getEditable();
    
    public void setVisible(boolean enabled);
    public boolean getVisible();

    public void setViewCoordinates(boolean enabled);
    public boolean getViewCoordinates();

    public void setControlling(boolean enabled);
    public boolean getControlling();

    public Integer getDisplayLevel();
    public void setDisplayLevel(Integer l);

    // The following are common for all JComponents
    public int getX();
    public int getY();
    public Point getLocation();
    public void setLocation(int x, int y);
    public void setLocation(Point p);
    public void addMouseListener(MouseListener l);
    public void removeMouseListener(MouseListener l);
}