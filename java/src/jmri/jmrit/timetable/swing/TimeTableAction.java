package jmri.jmrit.timetable.swing;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a TimeTableFrame
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class TimeTableAction extends JmriAbstractAction {

    public TimeTableAction(String s) {
        super(s);
    }

    public TimeTableAction() {
        this("TimeTable");  // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (jmri.InstanceManager.getNullableDefault(TimeTableFrame.class) != null) {
            // Prevent duplicate copies
            return;
        }
        TimeTableFrame f = new TimeTableFrame("");
        f.setVisible(true);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");  // NOI18N
    }
}
