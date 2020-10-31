/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.modulesEditor;

import javax.swing.AbstractAction;
import jmri.jmrit.display.EditorActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author George Warner Copyright (C) 2020
 */
@ServiceProvider(service = EditorActionFactory.class)
public final class ModulesEditorActionFactory implements EditorActionFactory {

    @Override
    public AbstractAction createAction() {
        return new ModulesEditorAction(getTitle());
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("ModulesEditor");
    }

}
