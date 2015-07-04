//TrainsScheduleEditAction.java
package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to edit timetable (Schedule)
 *
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class TrainsScheduleEditAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 1796097519377642811L;

    public TrainsScheduleEditAction() {
        super(Bundle.getMessage("MenuItemEditSchedule"));
    }

    public void actionPerformed(ActionEvent e) {
        new TrainsScheduleEditFrame();
    }

}
