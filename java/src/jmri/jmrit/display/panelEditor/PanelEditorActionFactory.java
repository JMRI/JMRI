package jmri.jmrit.display.panelEditor;

import javax.swing.AbstractAction;
import jmri.jmrit.display.EditorActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = EditorActionFactory.class)
public final class PanelEditorActionFactory implements EditorActionFactory {

    @Override
    public AbstractAction createAction() {
        return new PanelEditorAction(getTitle());
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("PanelEditor");
    }
    
}
