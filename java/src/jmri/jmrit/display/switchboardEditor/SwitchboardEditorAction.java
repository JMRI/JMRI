package jmri.jmrit.display.switchboardEditor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;
import jmri.jmrit.display.PanelMenu;

/**
 * Start a SwitchboardEditor.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @see jmri.jmrit.display.switchboardEditor.SwitchboardEditor
 */
public class SwitchboardEditorAction extends AbstractAction {

    public SwitchboardEditorAction(String s) {
        super(s);
    }

    public SwitchboardEditorAction() {
        this("New Switchboard");
    } // NO I18N

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = Bundle.getMessage("SwitchboardDefaultName", "");
        for (int i = 2; i < 100; i++) {
            if (InstanceManager.getDefault(PanelMenu.class).isPanelNameUsed(name)) {
                name = Bundle.getMessage("SwitchboardDefaultName", " " + i);
            }
        }
        SwitchboardEditor frame = new SwitchboardEditor(name);
        InstanceManager.getDefault(PanelMenu.class).addEditorPanel(frame);
        frame.setLocation(570, 20); // position Editor

        frame.setTitle();
        frame.initView();

        frame.pack();
        frame.setVisible(true);
        frame.newPanelDefaults();
    }

}
