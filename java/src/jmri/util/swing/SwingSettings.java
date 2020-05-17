package jmri.util.swing;

import jmri.InstanceManager;
import jmri.util.gui.GuiLafPreferencesManager;

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
 * @deprecated since 4.19.6; use {@link GuiLafPreferencesManager} instead
 */
@Deprecated
public class SwingSettings {

    /**
     * Get if non-standard mouse events are to be used.
     *
     * @return true if non-standard mouse events are to be used
     * @deprecated since 4.19.6; use
     * {@link GuiLafPreferencesManager#isNonStandardMouseEvent()} instead
     */
    @Deprecated
    static public boolean getNonStandardMouseEvent() {
        return InstanceManager.getDefault(GuiLafPreferencesManager.class).isNonStandardMouseEvent();
    }

    /**
     * Has no effect; retained until class is removed following deprecation
     * period.
     *
     * @param v ignored
     * @deprecated since 4.19.6; use
     * {@link GuiLafPreferencesManager#setNonStandardMouseEvent(boolean)}
     * instead
     */
    @Deprecated
    static public void setNonStandardMouseEvent(boolean v) {
        // do nothing
    }

}
