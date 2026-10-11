package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableModel;

/**
 * Swing action to show checkboxes in the engines window.
 *
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class ShowCheckboxesEnginesTableAction extends AbstractAction {

    EnginesTableModel _enginesTableModel;

    public ShowCheckboxesEnginesTableAction(EnginesTableModel enginesTableModel) {
        super(Bundle.getMessage("TitleShowCheckboxes"));
        _enginesTableModel = enginesTableModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _enginesTableModel.toggleSelectVisible();
    }
}
