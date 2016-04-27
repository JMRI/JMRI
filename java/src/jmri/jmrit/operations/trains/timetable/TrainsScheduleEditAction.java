//TrainsScheduleEditAction.java
package jmri.jmrit.operations.trains.timetable;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to edit timetable (Schedule)
 *
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class TrainsScheduleEditAction extends AbstractAction {

    public TrainsScheduleEditAction() {
        super(Bundle.getMessage("MenuItemEditSchedule"));
    }
    
    TrainsScheduleEditFrame frame;

    public void actionPerformed(ActionEvent e) {
        if (frame != null) {
            frame.dispose();
        }
        frame = new TrainsScheduleEditFrame();
    }

}
