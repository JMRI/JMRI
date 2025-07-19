package jmri.util.swing;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import jmri.util.SystemType;

/**
 * Adaptor for MouseEvent.
 * This class is used to fix some issues with MouseEvent on Windows.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class JmriMouseEvent {

    /**
     * The "mouse clicked" event. This {@code MouseEvent}
     * occurs when a mouse button is pressed and released.
     */
    public static final int MOUSE_CLICKED = MouseEvent.MOUSE_CLICKED;

    /**
     * The "mouse pressed" event. This {@code MouseEvent}
     * occurs when a mouse button is pushed down.
     */
    public static final int MOUSE_PRESSED = MouseEvent.MOUSE_PRESSED; //Event.MOUSE_DOWN

    /**
     * The "mouse released" event. This {@code MouseEvent}
     * occurs when a mouse button is let up.
     */
    public static final int MOUSE_RELEASED = MouseEvent.MOUSE_RELEASED; //Event.MOUSE_UP

    /**
     * The "mouse moved" event. This {@code MouseEvent}
     * occurs when the mouse position changes.
     */
    public static final int MOUSE_MOVED = MouseEvent.MOUSE_MOVED; //Event.MOUSE_MOVE

    /**
     * The "mouse entered" event. This {@code MouseEvent}
     * occurs when the mouse cursor enters the unobscured part of component's
     * geometry.
     */
    public static final int MOUSE_ENTERED = MouseEvent.MOUSE_ENTERED; //Event.MOUSE_ENTER

    /**
     * The "mouse exited" event. This {@code MouseEvent}
     * occurs when the mouse cursor exits the unobscured part of component's
     * geometry.
     */
    public static final int MOUSE_EXITED = MouseEvent.MOUSE_EXITED; //Event.MOUSE_EXIT

    /**
     * The "mouse dragged" event. This {@code MouseEvent}
     * occurs when the mouse position changes while a mouse button is pressed.
     */
    public static final int MOUSE_DRAGGED = MouseEvent.MOUSE_DRAGGED; //Event.MOUSE_DRAG

    /**
     * The "mouse wheel" event.  This is the only {@code MouseWheelEvent}.
     * It occurs when a mouse equipped with a wheel has its wheel rotated.
     * @since 1.4
     */
    public static final int MOUSE_WHEEL = MouseEvent.MOUSE_WHEEL;

    /**
     * Indicates no mouse buttons; used by {@link #getButton}.
     * @since 1.4
     */
    public static final int NOBUTTON = MouseEvent.NOBUTTON;

    /**
     * Indicates mouse button #1; used by {@link #getButton}.
     * @since 1.4
     */
    public static final int BUTTON1 = MouseEvent.BUTTON1;

    /**
     * Indicates mouse button #2; used by {@link #getButton}.
     * @since 1.4
     */
    public static final int BUTTON2 = MouseEvent.BUTTON2;

    /**
     * Indicates mouse button #3; used by {@link #getButton}.
     * @since 1.4
     */
    public static final int BUTTON3 = MouseEvent.BUTTON3;


    private final MouseEvent event;

    public JmriMouseEvent(MouseEvent event) {
        this.event = event;
    }

    public JmriMouseEvent(Component source, int id, long when, int modifiers,
                      int x, int y, int clickCount, boolean popupTrigger) {
        this.event = new MouseEvent(source, id, when, modifiers, x, y, clickCount, popupTrigger, NOBUTTON);
     }

    public JmriMouseEvent(Component source, int id, long when, int modifiers,
                      int x, int y, int clickCount, boolean popupTrigger,
                      int button) {
        this.event = new MouseEvent(source, id, when, modifiers, x, y, clickCount, popupTrigger, button);
     }

    /**
     * Returns the event type.
     *
     * @return the event's type id
     */
    public int getID() {
        return event.getID();
    }

    /**
     * Returns the absolute x, y position of the event.
     * In a virtual device multi-screen environment in which the
     * desktop area could span multiple physical screen devices,
     * these coordinates are relative to the virtual coordinate system.
     * Otherwise, these coordinates are relative to the coordinate system
     * associated with the Component's GraphicsConfiguration.
     *
     * @return a {@code Point} object containing the absolute  x
     *  and y coordinates.
     *
     * @see java.awt.GraphicsConfiguration
     * @since 1.6
     */
    public Point getLocationOnScreen(){
        return event.getLocationOnScreen();
    }

    /**
     * Returns the absolute horizontal x position of the event.
     * In a virtual device multi-screen environment in which the
     * desktop area could span multiple physical screen devices,
     * this coordinate is relative to the virtual coordinate system.
     * Otherwise, this coordinate is relative to the coordinate system
     * associated with the Component's GraphicsConfiguration.
     *
     * @return x  an integer indicating absolute horizontal position.
     *
     * @see java.awt.GraphicsConfiguration
     * @since 1.6
     */
    public int getXOnScreen() {
        return event.getXOnScreen();
    }

    /**
     * Returns the absolute vertical y position of the event.
     * In a virtual device multi-screen environment in which the
     * desktop area could span multiple physical screen devices,
     * this coordinate is relative to the virtual coordinate system.
     * Otherwise, this coordinate is relative to the coordinate system
     * associated with the Component's GraphicsConfiguration.
     *
     * @return y  an integer indicating absolute vertical position.
     *
     * @see java.awt.GraphicsConfiguration
     * @since 1.6
     */
    public int getYOnScreen() {
        return event.getYOnScreen();
    }

    /**
     * Returns the horizontal x position of the event relative to the
     * source component.
     *
     * @return x  an integer indicating horizontal position relative to
     *            the component
     */
    public int getX() {
        return event.getX();
    }

    /**
     * Returns the vertical y position of the event relative to the
     * source component.
     *
     * @return y  an integer indicating vertical position relative to
     *            the component
     */
    public int getY() {
        return event.getY();
    }

    /**
     * Returns the x,y position of the event relative to the source component.
     *
     * @return a {@code Point} object containing the x and y coordinates
     *         relative to the source component
     *
     */
    public Point getPoint() {
        return event.getPoint();
    }

    /**
     * Translates the event's coordinates to a new position
     * by adding specified {@code x} (horizontal) and {@code y}
     * (vertical) offsets.
     *
     * @param x the horizontal x value to add to the current x
     *          coordinate position
     * @param y the vertical y value to add to the current y
                coordinate position
     */
    public synchronized void translatePoint(int x, int y) {
        event.translatePoint(x, y);
    }

    /**
     * Returns the number of mouse clicks associated with this event.
     *
     * @return integer value for the number of clicks
     */
    public int getClickCount() {
        return event.getClickCount();
    }

    /**
     * Returns which, if any, of the mouse buttons has changed state.
     * The returned value is ranged
     * from 0 to the {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()}
     * value.
     * The returned value includes at least the following constants:
     * <ul>
     * <li> {@code NOBUTTON}
     * <li> {@code BUTTON1}
     * <li> {@code BUTTON2}
     * <li> {@code BUTTON3}
     * </ul>
     * It is allowed to use those constants to compare with the returned button number in the application.
     * For example,
     * <pre>
     * if (anEvent.getButton() == JmriMouseEvent.BUTTON1) {
     * </pre>
     * In particular, for a mouse with one, two, or three buttons this method may return the following values:
     * <ul>
     * <li> 0 ({@code NOBUTTON})
     * <li> 1 ({@code BUTTON1})
     * <li> 2 ({@code BUTTON2})
     * <li> 3 ({@code BUTTON3})
     * </ul>
     * Button numbers greater than {@code BUTTON3} have no constant identifier.
     * So if a mouse with five buttons is
     * installed, this method may return the following values:
     * <ul>
     * <li> 0 ({@code NOBUTTON})
     * <li> 1 ({@code BUTTON1})
     * <li> 2 ({@code BUTTON2})
     * <li> 3 ({@code BUTTON3})
     * <li> 4
     * <li> 5
     * </ul>
     * <p>
     * Note: If support for extended mouse buttons is {@link Toolkit#areExtraMouseButtonsEnabled() disabled} by Java
     * then the AWT event subsystem does not produce mouse events for the extended mouse
     * buttons. So it is not expected that this method returns anything except {@code NOBUTTON}, {@code BUTTON1},
     * {@code BUTTON2}, {@code BUTTON3}.
     *
     * @return one of the values from 0 to {@link java.awt.MouseInfo#getNumberOfButtons() MouseInfo.getNumberOfButtons()}
     *         if support for the extended mouse buttons is {@link Toolkit#areExtraMouseButtonsEnabled() enabled} by Java.
     *         That range includes {@code NOBUTTON}, {@code BUTTON1}, {@code BUTTON2}, {@code BUTTON3};
     *         <br>
     *         {@code NOBUTTON}, {@code BUTTON1}, {@code BUTTON2} or {@code BUTTON3}
     *         if support for the extended mouse buttons is {@link Toolkit#areExtraMouseButtonsEnabled() disabled} by Java
     * @since 1.4
     * @see Toolkit#areExtraMouseButtonsEnabled()
     * @see java.awt.MouseInfo#getNumberOfButtons()
     * @see MouseEvent#MouseEvent(Component, int, long, int, int, int, int, int, int, boolean, int)
     * @see InputEvent#getMaskForButton(int)
     */
    public int getButton() {
        return event.getButton();
    }

    /**
     * Returns whether or not this mouse event is the popup menu
     * trigger event for the platform.
     * <p><b>Note</b>: Popup menus are triggered differently
     * on different systems. Therefore, {@code isPopupTrigger}
     * should be checked in both {@code mousePressed}
     * and {@code mouseReleased}
     * for proper cross-platform functionality.
     *
     * @return boolean, true if this event is the popup menu trigger
     *         for this platform
     */
    public boolean isPopupTrigger() {
        if (SystemType.isWindows()) {
            switch (event.getID()) {
                case MouseEvent.MOUSE_PRESSED:
                case MouseEvent.MOUSE_RELEASED:
                case MouseEvent.MOUSE_CLICKED:
                    // event.isPopupTrigger() returns false on mousePressed() on Windows.
                    // The bad news is that SwingUtilities.isRightMouseButton(event) doesn't work either.
                    return (event.getModifiersEx() & InputEvent.META_DOWN_MASK) != 0
                            || (event.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0;

                default:
                    return event.isPopupTrigger();
            }
        } else {
            return event.isPopupTrigger();
        }
    }

    /**
     * Returns a {@code String} instance describing the modifier keys and
     * mouse buttons that were down during the event, such as "Shift",
     * or "Ctrl+Shift". These strings can be localized by changing
     * the {@code awt.properties} file.
     * <p>
     * Note that the {@code InputEvent.ALT_MASK} and
     * {@code InputEvent.BUTTON2_MASK} have equal values,
     * so the "Alt" string is returned for both modifiers.  Likewise,
     * the {@code InputEvent.META_MASK} and
     * {@code InputEvent.BUTTON3_MASK} have equal values,
     * so the "Meta" string is returned for both modifiers.
     * <p>
     * Note that passing negative parameter is incorrect,
     * and will cause the returning an unspecified string.
     * Zero parameter means that no modifiers were passed and will
     * cause the returning an empty string.
     *
     * @param modifiers A modifier mask describing the modifier keys and
     *                  mouse buttons that were down during the event
     * @return string   string text description of the combination of modifier
     *                  keys and mouse buttons that were down during the event
     * @see InputEvent#getModifiersExText(int)
     * @since 1.4
     */
    public static String getMouseModifiersText(int modifiers) {
        return MouseEvent.getMouseModifiersText(modifiers);
    }

    /**
     * Returns a parameter string identifying this event.
     * This method is useful for event-logging and for debugging.
     *
     * @return a string identifying the event and its attributes
     */
    public String paramString() {
        return event.paramString();
    }

    /**
     * Returns whether or not the Shift modifier is down on this event.
     * @return whether or not the Shift modifier is down on this event
     */
    public boolean isShiftDown() {
        return event.isShiftDown();
    }

    /**
     * Returns whether or not the Control modifier is down on this event.
     * @return whether or not the Control modifier is down on this event
     */
    public boolean isControlDown() {
        return event.isControlDown();
    }

    /**
     * Returns whether or not the Meta modifier is down on this event.
     *
     * The meta key was until Java 8 the right mouse button on Windows.
     * On Java 9 on Windows 10, there is no more meta key. Note that this
     * method is called both on mouse button events and mouse move events,
     * and therefore "event.getButton() == JmriMouseEvent.BUTTON3" doesn't work.
     *
     * As of Java 11, the meta key process has changed.  The getModifiersEx() value will vary
     * when button 3 is used, depending on the mouse event.
     *     mousePressed  :: 4096 (button 3)
     *     mouseDragged  :: 4096
     *     mouseReleased :: 256  (meta)
     *     mouseClicked  :: 256
     * The meta value is simulated by Java for Linux and Windows based on button 3 being active.
     *
     * @return whether or not the Meta modifier is down on this event
     */
    public boolean isMetaDown() {
        if (SystemType.isWindows() || SystemType.isLinux()) {
            return ((event.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0 ||
                    (event.getModifiersEx() & InputEvent.META_DOWN_MASK) != 0);
        } else {
            return event.isMetaDown();
        }
    }

    /**
     * Returns whether or not the Alt modifier is down on this event.
     * @return whether or not the Alt modifier is down on this event
     */
    public boolean isAltDown() {
        return event.isAltDown();
    }

    /**
     * Returns whether or not the AltGraph modifier is down on this event.
     * @return whether or not the AltGraph modifier is down on this event
     */
    public boolean isAltGraphDown() {
        return event.isAltGraphDown();
    }

    /**
     * Returns the difference in milliseconds between the timestamp of when this event occurred and
     * midnight, January 1, 1970 UTC.
     * @return the difference in milliseconds between the timestamp and midnight, January 1, 1970 UTC
     */
    public long getWhen() {
        return event.getWhen();
    }

    /**
     * Returns the modifier mask for this event.
     *
     * @return the modifier mask for this event
     * @deprecated It is recommended that extended modifier keys and
     *             {@link #getModifiersEx()} be used instead
     */
    @Deprecated(since = "9")
    @SuppressWarnings("deprecation")
    public int getModifiers() {
        return event.getModifiers();
    }

    /**
     * Returns the extended modifier mask for this event.
     * <P>
     * Extended modifiers are the modifiers that ends with the _DOWN_MASK suffix,
     * such as ALT_DOWN_MASK, BUTTON1_DOWN_MASK, and others.
     * <P>
     * Extended modifiers represent the state of all modal keys,
     * such as ALT, CTRL, META, and the mouse buttons just after
     * the event occurred.
     * <P>
     * For example, if the user presses <b>button 1</b> followed by
     * <b>button 2</b>, and then releases them in the same order,
     * the following sequence of events is generated:
     * <PRE>
     *    {@code MOUSE_PRESSED}:  {@code BUTTON1_DOWN_MASK}
     *    {@code MOUSE_PRESSED}:  {@code BUTTON1_DOWN_MASK | BUTTON2_DOWN_MASK}
     *    {@code MOUSE_RELEASED}: {@code BUTTON2_DOWN_MASK}
     *    {@code MOUSE_CLICKED}:  {@code BUTTON2_DOWN_MASK}
     *    {@code MOUSE_RELEASED}:
     *    {@code MOUSE_CLICKED}:
     * </PRE>
     * <P>
     * It is not recommended to compare the return value of this method
     * using {@code ==} because new modifiers can be added in the future.
     * For example, the appropriate way to check that SHIFT and BUTTON1 are
     * down, but CTRL is up is demonstrated by the following code:
     * <PRE>
     *    int onmask = SHIFT_DOWN_MASK | BUTTON1_DOWN_MASK;
     *    int offmask = CTRL_DOWN_MASK;
     *    if ((event.getModifiersEx() &amp; (onmask | offmask)) == onmask) {
     *        ...
     *    }
     * </PRE>
     * The above code will work even if new modifiers are added.
     *
     * @return the extended modifier mask for this event
     * @since 1.4
     */
    public int getModifiersEx() {
        return event.getModifiersEx();
    }

    /**
     * Returns the originator of the event.
     *
     * @return the {@code Component} object that originated
     * the event, or {@code null} if the object is not a
     * {@code Component}.
     */
    public Component getComponent() {
        return event.getComponent();
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return the object on which the Event initially occurred
     */
    public Object getSource() {
        return event.getSource();
    }

    /**
     * Consumes this event so that it will not be processed
     * in the default manner by the source which originated it.
     */
    public void consume() {
        event.consume();
    }

}
