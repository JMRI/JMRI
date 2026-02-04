package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;

/**
 * Swing action to create an EnginesSetFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2024
 */
public class EnginesSetFrameAction extends AbstractAction {

    JTable _enginesTable;

    public EnginesSetFrameAction(JTable enginesTable) {
        super(Bundle.getMessage("TitleSetEngines"));
        _enginesTable = enginesTable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EnginesSetFrame esf = new EnginesSetFrame();
        esf.initComponents(_enginesTable);
    }
}


