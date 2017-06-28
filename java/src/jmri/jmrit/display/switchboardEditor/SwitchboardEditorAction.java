package jmri.jmrit.display.switchboardEditor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Start a SwitchboardEditor.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @see jmri.jmrit.display.switchboardEditor.SwitchboardEditorAction
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
            if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)) {
                name = Bundle.getMessage("SwitchboardDefaultName", " " + i);
            }
        }
        SwitchboardEditor frame = new SwitchboardEditor(name);
        jmri.jmrit.display.PanelMenu.instance().addEditorPanel(frame);
        frame.setLocation(570, 20); // position Editor

        frame.setTitle();
        frame.initView();

        frame.pack();
        frame.setVisible(true);
    }
}
