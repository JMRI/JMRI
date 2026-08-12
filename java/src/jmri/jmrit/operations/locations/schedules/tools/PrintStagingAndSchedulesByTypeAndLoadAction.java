package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action to print staging and schedules by car type and load
 * 
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class PrintStagingAndSchedulesByTypeAndLoadAction extends AbstractAction {

    public PrintStagingAndSchedulesByTypeAndLoadAction(boolean isPreview, SchedulesAndStagingFrame ssf) {
        super(isPreview ? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        _isPreview = isPreview;
        _ssf = ssf;
    }

    boolean _isPreview;
    SchedulesAndStagingFrame _ssf;


    @Override
    public void actionPerformed(ActionEvent e) {
        new PrintStagingAndSchedulesByTypeAndLoad(_isPreview, _ssf);
    }
}
