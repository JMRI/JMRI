/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.modulesEditor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;

/**
 * Start a ModulesEditor.
 *
 * @author George Warner Copyright (C) 2020
 * @see jmri.jmrit.display.panelEditor.PanelEditorAction
 */
public class ModulesEditorAction extends AbstractAction {

    public ModulesEditorAction(String s) {
        super(s);
    }

    public ModulesEditorAction() {
        this(Bundle.getMessage("DefaultModulesEditorPanelName"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String defaultName = Bundle.getMessage("DefaultModulesEditorPanelName");
        String name = defaultName;
        for (int i = 2; i < 100; i++) {
            if (InstanceManager.getDefault(EditorManager.class).contains(name)) {
                name = defaultName + i;
            } else {
                break;
            }
        }
        ModulesEditor panel = new ModulesEditor(name);
        panel.pack();
        panel.setVisible(true);
        panel.setAllEditable(true);
        InstanceManager.getDefault(EditorManager.class).add(panel);
        panel.newPanelDefaults();
    }
}
