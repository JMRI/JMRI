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
 * @author Bob Jacobsen Copyright (C) 2008
 * @see jmri.jmrit.display.panelEditor.PanelEditorAction
 */
public class ModulesEditorAction extends AbstractAction {

    public ModulesEditorAction(String s) {
        super(s);
    }

    public ModulesEditorAction() {
        this("New Panel");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = "My Layout";
        for (int i = 2; i < 100; i++) {
            if (InstanceManager.getDefault(EditorManager.class).contains(name)) {
                name = "My Layout " + i;
            }
        }
        ModulesEditor panel = new ModulesEditor(name);
        panel.setLayoutName(name);
        panel.pack();
        panel.setVisible(true);
        panel.setAllEditable(true);
        panel.setCurrentPositionAndSize();
        InstanceManager.getDefault(EditorManager.class).add(panel);
        panel.newPanelDefaults();
    }
}
