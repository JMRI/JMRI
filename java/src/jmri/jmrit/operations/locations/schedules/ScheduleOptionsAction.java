package jmri.jmrit.operations.locations.schedules;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Action to launch schedule options.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011
 */
@API(status = MAINTAINED)
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
