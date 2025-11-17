package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableFrame;

/**
 * Starts the ImportEngines thread
 *
 * @author Dan Boudreau Copyright (C) 2008, 2025
 */
public class ExportEngineRosterAction extends AbstractAction {

    EnginesTableFrame _enginesTableFrame;

    public ExportEngineRosterAction(EnginesTableFrame enginesTableFrame) {
        super(Bundle.getMessage("MenuItemExport"));
        _enginesTableFrame = enginesTableFrame;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        ExportEngines ex = new ExportEngines(_enginesTableFrame.enginesTableModel.getSelectedEngineList());
        ex.writeOperationsEngineFile();
    }

//    private final static Logger log = LoggerFactory.getLogger(ExportEngineRosterAction.class);
}
