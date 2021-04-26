package jmri.jmrit.display.switchboardEditor;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import jmri.jmrit.display.EditorActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = EditorActionFactory.class)
public final class SwitchboardEditorActionFactory implements EditorActionFactory {

    @Override
    public AbstractAction createAction() {
        return new SwitchboardEditorAction(getTitle());
    }

    @Override
    @Nonnull
    public String getTitle() {
        return Bundle.getMessage("SwitchboardEditor");
    }
    
}
