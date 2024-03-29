package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.schedules.ScheduleEditFrame;

/**
 * Action to launch schedule options.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011
 */
public class ScheduleOptionsAction extends AbstractAction {

    private ScheduleEditFrame _sef;

    public ScheduleOptionsAction(ScheduleEditFrame sef) {
        super(Bundle.getMessage("MenuItemScheduleOptions"));
        _sef = sef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new ScheduleOptionsFrame(_sef);
    }

}
