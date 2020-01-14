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
 * @deprecated since 4.19.3 without direct replacement
 */
@Deprecated
public class SwingSettings {

    /**
     * @return result of
     *         {@link GuiLafPreferencesManager#isNonStandardMouseEvent()}
     * @deprecated since 4.19.3; use
     *             {@link GuiLafPreferencesManager#isNonStandardMouseEvent()}
     *             instead
     */
    @Deprecated
    public static boolean getNonStandardMouseEvent() {
        return InstanceManager.getDefault(GuiLafPreferencesManager.class).isNonStandardMouseEvent();
    }

    /**
     * Does nothing; retained for API compatibility.
     * 
     * @param v ignored
     * @deprecated since 4.19.3 without direct replacement
     */
    @Deprecated
    public static void setNonStandardMouseEvent(boolean v) {
        // does nothing
    }

}
