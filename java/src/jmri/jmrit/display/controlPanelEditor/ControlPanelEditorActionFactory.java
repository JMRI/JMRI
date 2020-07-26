package jmri.jmrit.display.controlPanelEditor;

import javax.swing.AbstractAction;
import jmri.jmrit.display.EditorActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = EditorActionFactory.class)
public final class ControlPanelEditorActionFactory implements EditorActionFactory {

    @Override
    public AbstractAction createAction() {
        return new ControlPanelEditorAction(getTitle());
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("ControlPanelEditor");
    }

}
