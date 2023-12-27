package jmri.jmrit.swing;

import javax.swing.Action;

/**
 * Identifies an Action that can be added to the Tools menu
 * by being instantiated as @ServiceProvider(service = jmri.jmrit.swing.ToolsMenuItem.class)
 *
 * @author Bob Jacobsen Copyright 2023
 */
public interface ToolsMenuAction extends Action {

}
