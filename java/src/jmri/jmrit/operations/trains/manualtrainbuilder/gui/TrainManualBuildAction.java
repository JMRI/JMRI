package jmri.jmrit.operations.trains.manualtrainbuilder.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.trains.Train;

/**
 * Action to open the edit train manual build
 * 
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class TrainManualBuildAction extends AbstractAction {

    public TrainManualBuildAction(Train train) {
        super(Bundle.getMessage("TitleManualBuild"));
        _train = train;
    }

    Train _train;

    @Override
    public void actionPerformed(ActionEvent e) {
        new TrainManualBuildEditFrame(_train.getId());
    }
}
