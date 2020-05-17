package jmri.jmrit.display.switchboardEditor;

import javax.swing.AbstractAction;
import jmri.jmrit.display.EditorActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = EditorActionFactory.class)
public class SwitchboardEditorActionFactory implements EditorActionFactory {

    @Override
    public AbstractAction createAction() {
        return new SwitchboardEditorAction(getTitle());
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("SwitchboardEditor");
    }
    
}
