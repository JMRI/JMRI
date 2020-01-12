package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

/**
 * Defines display objects.
 * <p>
 * These are capable of:
 * <ul>
 * <li>Being positioned by being dragged around on the screen. (See
 * {@link #setPositionable})
 * <li>Being hidden. (See {@link #setHidden})
 * <li>Controlling the layout. (See {@link #setControlling})
 * </ul><p>
 * These are manipulated externally, for example by a subclass of
 * {@link Editor}. They are generally not stored directly as part of the state
 * of the object, though they could be, but as part of the state of the external
 * control.
 * <p>
 * Instead of the usual MouseEvent handling methods, e.g mouseClicked(...),
 * Positionables have similar methods called doMouseClicked invoked by the
 * {@link Editor} subclass that contains them, so the Editor can handle e.g. box
 * selection, etc.
 *
 * <a href="doc-files/Heirarchy.png"><img src="doc-files/Heirarchy.png" alt="UML class diagram for package" height="33%" width="33%"></a>
 * @see PositionableJComponent
 * @see PositionableLabel
 * @author Bob Jacobsen Copyright (c) 2002
 * @author Pete Cressman Copyright (c) 2010
 */
public interface Positionable extends Cloneable {

    public void setPositionable(boolean enabled);

    public boolean isPositionable();

    public void setEditable(boolean enabled);

    public boolean isEditable();

    public void setShowToolTip(boolean set);

    public boolean showToolTip();

    public void setToolTip(ToolTip tip);

    public ToolTip getToolTip();

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
     * Make a deep copy of Positional object. Implementation should create a new
     * object and immediately pass the object to finishClone() returning the
     * result of finishClone(). i.e. implementation must be:
     * <p>
     * {@code public Positionable deepClone() { Subtype t = new Subtype(); return finishClone(t);
     * } }
     * <p>
     * Then finishClone() finishes the deep Copy of a Positional object.
     * Implementation should make deep copies of the additional members of this
     * sub class and then pass Positionable p to super.finishClone(). i.e.
     * implementation must terminate with statement return super.finishClone(p);
     * See IndicatorTurnoutIcon extends TurnoutIcon extends PositionableLabel
     * for an example of how to continue deep cloning a chain of subclasses.
     *
     * @return the copy
     */
    public Positionable deepClone();

    /**
     * Get the name of the positional as a String. This is often the display
     * name of the NamedBean being positioned.
     *
     * @return the name to display
     */
    public String getNameString();

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    public boolean setRotateOrthogonalMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    public boolean setRotateMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    public boolean setScaleMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    public boolean setEditIconMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    public boolean setEditItemMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    public boolean setDisableControlMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    public boolean setTextEditMenu(JPopupMenu popup);

    public boolean showPopUp(JPopupMenu popup);

    public void setScale(double s);

    public double getScale();

    public void rotate(int deg);

    public int getDegrees();

    public JComponent getTextComponent();

    public void remove();

    /**
     * Check if a permanent copy of this Positionable should be stored.
     *
     * @return true if this Positionable should be stored; false otherwise
     */
    public boolean storeItem();

    /**
     * Use the 'Standard' presentation of the popup menu items. The editor will
     * call this method to find out whether it should create any popup viewing
     * menu items.
     *
     * @return true if Editor may add the standardpopup menu items
     */
    public boolean doViemMenu();

    /**
     * Utility to handle Margins, Borders and other common popup items
     *
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

    public Container getParent();

    public void setOpaque(boolean isOpaque);

    public boolean isOpaque();

    public void setBackground(Color bg);

    public Color getBackground();

    public void setForeground(Color bg);

    public Color getForeground();

    public Font getFont();

    public void setBorder(Border border);

    public Dimension getPreferredSize();

    public void invalidate();

    public void repaint();

    public boolean requestFocusInWindow();
}
