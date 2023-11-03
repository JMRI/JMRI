package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.trains.Train;

/**
 * Action to print a summary of a train
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2009, 2023
 */
public class PrintTrainAction extends AbstractAction {

    public PrintTrainAction(boolean isPreview, Train train) {
        super(isPreview ? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        _isPreview = isPreview;
        _train = train;
    }
    
    public PrintTrainAction(boolean isPreview) {
        this(isPreview, null);
    }

    boolean _isPreview;
    Train _train;

    @Override
    public void actionPerformed(ActionEvent e) {
        new PrintTrainsFrame(_isPreview, _train);
    }
}
