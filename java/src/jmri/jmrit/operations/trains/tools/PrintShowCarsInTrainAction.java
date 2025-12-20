package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.trains.Train;

/**
 * Action to print the cars in the train.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class PrintShowCarsInTrainAction extends AbstractAction implements PropertyChangeListener {

    public PrintShowCarsInTrainAction(boolean isPreview, Train train) {
        super(isPreview ? Bundle.getMessage("MenuItemCarsInTrainPreview")
                : Bundle.getMessage("MenuItemCarsInTrainPrint"));
        _isPreview = isPreview;
        _train = train;
        train.addPropertyChangeListener(this);
    }

    boolean _isPreview;
    Train _train;

    @Override
    public void actionPerformed(ActionEvent e) {
        new PrintShowCarsInTrain().printCarsInTrain(_train, _isPreview);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        log.debug("Property change {} for: {} old: {} new: {}", e.getPropertyName(), e.getSource(), e.getOldValue(),
                e.getNewValue()); // NOI18N
        if (e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY)) {
            setEnabled(_train.isBuilt());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PrintShowCarsInTrainAction.class);
}
