package jmri.jmrit.display;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
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
 *<p>
 * Instead of the usual MouseEvent handling methods, e.g mouseClicked(...),
 * Positionables have similar methods called doMouseClicked 
 * invoked by the {@link Editor} subclass that contains
 * them, so the Editor can handle e.g. box selection, etc.
 *
 * @see PositionableJComponent
 * @see PositionableLabel
 * @author Bob Jacobsen Copyright (c) 2002
 * @author Pete Cressman Copyright (c) 2010
 * @version $Revision$
 */
public interface Positionable extends Cloneable  {
    public void setPositionable(boolean enabled);
    public boolean isPositionable();
    
    public void setEditable(boolean enabled);
    public boolean isEditable();

    public void setShowTooltip(boolean set);
    public boolean showTooltip();
    public void setTooltip(ToolTip tip);
    public ToolTip getTooltip();

    public void setViewCoordinates(boolean enabled);
    public boolean getViewCoordinates();

    public void setControlling(boolean enabled);
    public boolean isControlling();

    public void setHidden(boolean enabled);
    public boolean isHidden();
    public void showHidden();

    public int getDisplayLevel();
    public void setDisplayLevel(int l);

    public Editor getEditor();
    public void setEditor(Editor ed);
    public void updateSize();
    public int maxWidth();
    public int maxHeight();

    /**
    * Make a deep copy of Positional object. Implementation should
    * create a new object and immediately pass the object to
    * finishClone() returning the result of finishClone().
    * i.e. implementation must be:
    * public Positionable deepClone() {
    *    Subtype t = new Subtype();
    *    return finishClone(t);
    * }    
    */
    public Positionable deepClone();
    /**
    * Finsh the deep Copy of a Positional object. Implementation should
    * make deep copies of the additional members of this sub class and 
    * then pass Positionable p to super.finishClone().
    * i.e. implementation must terminate with statement
    * return super.finishClone(p);
    * See IndicatorTurnoutIcon extends TurnoutIcon extends PositionableLabel
    * for an example of how to continue deep cloning a chain of subclasses. 
    */
    public Positionable finishClone(Positionable p);

    /** Methods to add popup menu items
    * return true if a popup item is set
    */
    public String getNameString();
    public boolean setRotateOrthogonalMenu(JPopupMenu popup);
    public boolean setRotateMenu(JPopupMenu popup);
    public boolean setScaleMenu(JPopupMenu popup);
    public boolean setEditIconMenu(JPopupMenu popup);
    public boolean setEditItemMenu(JPopupMenu popup);
    public boolean setDisableControlMenu(JPopupMenu popup);
    public boolean setTextEditMenu(JPopupMenu popup);

    public boolean showPopUp(JPopupMenu popup);

    public void setScale(double s);
    public double getScale();
    public void rotate(int deg);
    public int getDegrees();
    public boolean getSaveOpaque();		// for rotated text

    public void remove();

    /**
     * Store a permanent copy of this Positionable
     * The editorXml will call this method to find out whether it
     * should store this Positionable item.
     * @return true if the Editor should store this in the configuration file
     * @return false if if the Editor should not store this object
     */
    public boolean storeItem();
    /**
     * Use the 'Standard' presentation of the popup menu items.
     * The editor will call this method to find out whether it
     * should creates any popup viewing menu items.
     * @return true if Editor may add the standardpopup menu items
    */
    public boolean doViemMenu();
    /*
    * Utility to handle Margins, Borders and other common popup items
    * @return null if these item do not apply
    */
    public PositionablePopupUtil getPopupUtility();
    public void setPopupUtility(PositionablePopupUtil tu);
    public jmri.NamedBean getNamedBean();

    // Mouse-handling events.  See
    // Editor class for more information on how these are used.
    public void doMousePressed(MouseEvent event);
    public void doMouseReleased(MouseEvent event);
    public void doMouseClicked(MouseEvent event);
    public void doMouseDragged(MouseEvent event);
    public void doMouseMoved(MouseEvent event);
    public void doMouseEntered(MouseEvent event);
    public void doMouseExited(MouseEvent event);

    // The following are common for all JComponents
    public Rectangle getBounds(Rectangle r);
    public boolean contains(int x, int y);
    public int getX();
    public int getY();
    public Point getLocation();
    public void setLocation(int x, int y);
    public void setLocation(Point p);
    public void setSize(int width, int height);
    public void setVisible(boolean b);
    public int getWidth();
    public int getHeight();
    public java.awt.Container getParent();
    public void setOpaque(boolean isOpaque);
    public boolean isOpaque();
    public void setBackground(java.awt.Color bg);
    public java.awt.Color getBackground();
    public void setForeground(java.awt.Color bg);
    public java.awt.Color getForeground();
    public java.awt.Font getFont();
    public void setBorder(javax.swing.border.Border border);
    public java.awt.Dimension getPreferredSize();
    public void invalidate();
    public void repaint();
}