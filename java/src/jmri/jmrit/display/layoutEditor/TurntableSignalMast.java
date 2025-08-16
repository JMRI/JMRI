package jmri.jmrit.display.layoutEditor;

import jmri.InstanceManager;
import jmri.implementation.VirtualSignalMast;
import java.util.Set;
import jmri.jmrit.display.EditorManager;

/**
 * A special SignalMast for use with LayoutTurntables.
 * <p>
 * This class allows other parts of the system to identify a signal mast as
 * belonging to a turntable and to get the turntable object from the mast.
 *
 * @author Gemini
 */
public class TurntableSignalMast extends VirtualSignalMast {

    public TurntableSignalMast(String systemName) {
        super(systemName);
    }

    /**
     * Get the LayoutTurntable associated with this signal mast.
     *
     * @return the LayoutTurntable, or null if not found.
     */
    public LayoutTurntable getTurntable() {
        // The system name is "IV-LT:" + turntable ID
        String turntableId = getSystemName().substring(6);

        // We need to find the turntable with this ID in the layout editors.
        Set<LayoutEditor> editors = InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class);
        for (LayoutEditor editor : editors) {
            for (LayoutTurntable tt : editor.getLayoutTurntables()) {
                if (tt.getName().equals(turntableId)) {
                    return tt;
                }
            }
        }
        return null;
    }
}