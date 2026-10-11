package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableModel;

/**
 * Swing action to reset checkboxes in the engines window.
 *
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class ResetCheckboxesEnginesTableAction extends AbstractAction {

    EnginesTableModel _enginesTableModel;

    public ResetCheckboxesEnginesTableAction(EnginesTableModel enginesTableModel) {
        super(Bundle.getMessage("TitleResetCheckboxes"));
        _enginesTableModel = enginesTableModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _enginesTableModel.resetCheckboxes();
    }
}
