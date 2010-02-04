package jmri.jmrit.display;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Defines display objects.
 * <P>
 * These are capable of:
 * <UL>
 * <LI>Being positioned by being dragged around on the screen.
 * (See {@link #setPositionable})
 * <LI>Being hidden. (See {@link #setHidden})
 * <LI>Controlling the layout. (See {@link #setControlling})
 * </OL>
 * These are manipulated externally, for example by a
 * subclass of {@link Editor}.
 * They are generally not stored
 * directly as part of the state of the object, though they
 * could be, but as part of the state of the external control.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @author Pete Cressman Copyright (c) 2010
 * @version $Revision: 1.12 $
 */
public interface Positionable  {
    public void setPositionable(boolean enabled);
    public boolean isPositionable();
    
    public void setEditable(boolean enabled);
    public boolean isEditable();

    public void setShowTooltip(boolean set);
    public boolean showTooltip();
    public void setTooltip(String tip);
    public String getTooltip();

    public void setViewCoordinates(boolean enabled);
    public boolean getViewCoordinates();

    public void setControlling(boolean enabled);
    public boolean isControlling();

    public void setHidden(boolean enabled);
    public boolean isHidden();
    public void showHidden();

    public int getDisplayLevel();
    public void setDisplayLevel(int l);

    /** Methods to add popup menu items
    */
    public String getNameString();
    public void setRotateOrthogonalMenu(JPopupMenu popup);
    public void setRotateMenu(JPopupMenu popup);
    public void setScaleMenu(JPopupMenu popup);
    public void setEditIconMenu(JPopupMenu popup);
    public void setDisableControlMenu(JPopupMenu popup);
    /*
    public void setFixedTextMenu(JPopupMenu popup);
    public void setTextMarginMenu(JPopupMenu popup);
    public void setBackgroundFontColorMenu(JPopupMenu popup);
    public void setTextBorderMenu(JPopupMenu popup);
    public void setTextEditMenu(JPopupMenu popup, String menuTitle);
    */
    public void showPopUp(JPopupMenu popup);

    public void setScale(double s);
    public double getScale();

    public void remove();

    public void doMousePressed(MouseEvent event);
    public void doMouseReleased(MouseEvent event);

    // The following are common for all JComponents
    public Rectangle getBounds(Rectangle r);
    public boolean contains(int x, int y);
    public int getX();
    public int getY();
    public Point getLocation();
    public void setLocation(int x, int y);
    public void setLocation(Point p);
    public void setVisible(boolean b);
    public int getWidth();
    public int getHeight();
    public void invalidate();
    public void repaint();
}