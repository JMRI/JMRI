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
import jmri.JmriException;

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

    /**
     * Sets the Id of this Positionable
     * @param id the id or null if no id
     * @throws jmri.jmrit.display.Positionable.DuplicateIdException if another
     *         Positionable in the editor already has this id
     */
    void setId(String id) throws Positionable.DuplicateIdException;

    /**
     * Gets the Id of this Positionable
     * @return id the id or null if no id
     */
    String getId();

    void setPositionable(boolean enabled);

    boolean isPositionable();

    void setEditable(boolean enabled);

    boolean isEditable();

    void setShowToolTip(boolean set);

    boolean showToolTip();

    void setToolTip(ToolTip tip);

    ToolTip getToolTip();

    void setViewCoordinates(boolean enabled);

    boolean getViewCoordinates();

    void setControlling(boolean enabled);

    boolean isControlling();

    void setHidden(boolean enabled);

    boolean isHidden();

    void showHidden();

    int getDisplayLevel();

    void setDisplayLevel(int l);

    Editor getEditor();

    void setEditor(Editor ed);

    void updateSize();

    int maxWidth();

    int maxHeight();

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
    Positionable deepClone();

    /**
     * Get the name of the positional as a String. This is often the display
     * name of the NamedBean being positioned.
     *
     * @return the name to display
     */
    String getNameString();

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    boolean setRotateOrthogonalMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    boolean setRotateMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    boolean setScaleMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    boolean setEditIconMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    boolean setEditItemMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    boolean setDisableControlMenu(JPopupMenu popup);

    /**
     * Add additional menu items to the menu.
     *
     * @param popup the menu to add the menu items to
     * @return true if adding items; false otherwise
     */
    boolean setTextEditMenu(JPopupMenu popup);

    boolean showPopUp(JPopupMenu popup);

    void setScale(double s);

    double getScale();

    void rotate(int deg);

    int getDegrees();

    JComponent getTextComponent();

    void remove();

    /**
     * Check if a permanent copy of this Positionable should be stored.
     *
     * @return true if this Positionable should be stored; false otherwise
     */
    boolean storeItem();

    /**
     * Use the 'Standard' presentation of the popup menu items. The editor will
     * call this method to find out whether it should create any popup viewing
     * menu items.
     *
     * @return true if Editor may add the standardpopup menu items
     */
    boolean doViemMenu();

    /**
     * Utility to handle Margins, Borders and other common popup items
     *
     * @return null if these item do not apply
     */
    PositionablePopupUtil getPopupUtility();

    void setPopupUtility(PositionablePopupUtil tu);

    jmri.NamedBean getNamedBean();

    // Mouse-handling events.  See
    // Editor class for more information on how these are used.
    void doMousePressed(MouseEvent event);

    void doMouseReleased(MouseEvent event);

    void doMouseClicked(MouseEvent event);

    void doMouseDragged(MouseEvent event);

    void doMouseMoved(MouseEvent event);

    void doMouseEntered(MouseEvent event);

    void doMouseExited(MouseEvent event);

    // The following are common for all JComponents
    Rectangle getBounds(Rectangle r);

    boolean contains(int x, int y);

    int getX();

    int getY();

    Point getLocation();

    void setLocation(int x, int y);

    void setLocation(Point p);

    void setSize(int width, int height);

    void setVisible(boolean b);

    int getWidth();

    int getHeight();

    Container getParent();

    void setOpaque(boolean isOpaque);

    boolean isOpaque();

    void setBackground(Color bg);

    Color getBackground();

    void setForeground(Color bg);

    Color getForeground();

    Font getFont();

    void setBorder(Border border);

    Dimension getPreferredSize();

    void invalidate();

    void repaint();

    boolean requestFocusInWindow();



    public static class DuplicateIdException extends JmriException {
    }

}
