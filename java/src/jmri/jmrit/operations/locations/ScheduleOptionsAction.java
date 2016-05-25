//ScheduleOptionsAction.java
package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Action to launch schedule options.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011
 * @version $Revision$
 */
public class ScheduleOptionsAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 6958291145537202643L;
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
