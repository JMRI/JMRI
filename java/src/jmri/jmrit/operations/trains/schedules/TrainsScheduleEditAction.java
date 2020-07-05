package jmri.jmrit.operations.trains.schedules;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Action to edit train schedule
 *
 * @author Daniel Boudreau Copyright (C) 2010
 */
@API(status = MAINTAINED)
public class TrainsScheduleEditAction extends AbstractAction {

    public TrainsScheduleEditAction() {
        super(Bundle.getMessage("MenuItemEditSchedule"));
    }
    
    TrainsScheduleEditFrame frame;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (frame != null) {
            frame.dispose();
        }
        frame = new TrainsScheduleEditFrame();
    }

}
