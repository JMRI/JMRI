package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action to print Schedules by Load
 * 
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class PrintSchedulesByTypeAndLoadAction extends AbstractAction {

    public PrintSchedulesByTypeAndLoadAction(boolean isPreview, SchedulesByLoadFrame sblf) {
        super(isPreview ? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        _isPreview = isPreview;
        _sblf = sblf;
    }

    boolean _isPreview;
    SchedulesByLoadFrame _sblf;


    @Override
    public void actionPerformed(ActionEvent e) {
        new PrintSchedulesByTypeAndLoad(_isPreview, _sblf.getCarType(), _sblf.getCarLoad());
    }
}
