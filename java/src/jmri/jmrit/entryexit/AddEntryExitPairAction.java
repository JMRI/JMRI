package jmri.jmrit.entryexit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register the Add Entry Exit Pair.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class AddEntryExitPairAction extends AbstractAction {

    public AddEntryExitPairAction(String s, LayoutEditor panel) {
        super(s);
        this.panel = panel;
    }
    LayoutEditor panel;

    @Override
    public void actionPerformed(ActionEvent e) {
        AddEntryExitPairFrame f = new AddEntryExitPairFrame();
        try {
            f.initComponents(panel);
        } catch (Exception ex) {
            log.error("Exception: ", ex);
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(AddEntryExitPairAction.class);
}
