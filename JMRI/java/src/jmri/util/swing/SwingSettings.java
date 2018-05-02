package jmri.util.swing;

/**
 * Settings for workarounds in Swing API.
 * <p>
 * By convention, "false" is the default, usual behavior for these.
 * <dl>
 * <dt>nonStandardMouseEvent
 * <dd>By default, the mouseClick event fires actions in the program. Certain HP
 * touch pads and other devices only provide mousePressed and mouseReleased
 * events, though, so if true this item will cause JMRI code that's looking for
 * mouse events to fire actions on mouseReleased. This will make those devices
 * work, but will really annoy Mac and Linux users.
 * </dl>
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.4
 */
public class SwingSettings {

    static private boolean nonStandardMouseEvent = false;

    static public boolean getNonStandardMouseEvent() {
        return nonStandardMouseEvent;
    }

    static public void setNonStandardMouseEvent(boolean v) {
        nonStandardMouseEvent = v;
    }

}
