package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import jmri.InstanceManager;

/**
 * Display a combo list of panels. A selected panel will be deleted if selected
 * and OK is pressed.  The panel is immediately removed from memory using dispose().
 * <p>
 * The delete is final when the Store action is performed.
 *
 * @author Dave Sand Copyright: Copyright (c) 2021
 */
public class PanelDeleteAction extends AbstractAction {

    /**
     * Constructor just initializes parent class.
     *
     * @param name Action name
     */
    public PanelDeleteAction(String name) {
        super(name);
    }

    public PanelDeleteAction() {
        super("Delete Panel ...");  // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Set<Editor> editors = InstanceManager.getDefault(EditorManager.class).getAll();
        if (editors.isEmpty()) {
            log.warn("PanelDeleteAction::actionPerformed nothing to delete");  // NOI18N
        } else {
            List<String> panelNames = new ArrayList<>();
            editors.forEach(editor -> {
                panelNames.add(editor.getTitle());
            });

            String panelName = (String) JOptionPane.showInputDialog(null,
                    Bundle.getMessage("PanelDeleteMessage"),  // NOI18N
                    Bundle.getMessage("PanelDeleteTitle"),    // NOI18N
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    panelNames.toArray(),
                    null);

            if (panelName != null && !panelName.isEmpty()) {
                Editor selected = InstanceManager.getDefault(EditorManager.class).get(panelName);
                if (selected != null) {
                    if (selected instanceof jmri.jmrit.display.panelEditor.PanelEditor ||
                             selected instanceof jmri.jmrit.display.switchboardEditor.SwitchboardEditor) {
                        selected.getTargetFrame().dispose();
                    }
                    selected.dispose();
                }
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PanelDeleteAction.class);
}

