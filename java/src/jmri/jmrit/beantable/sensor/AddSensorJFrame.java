package jmri.jmrit.beantable.sensor;

import javax.swing.BoxLayout;
import jmri.util.JmriJFrame;

/**
 * JFrame to create a new Sensor
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @deprecated As of JMRI 2.14.1 (2008) Replaced by
 * {@link jmri.jmrit.beantable.AddNewHardwareDevicePanel}
 */
@Deprecated
public class AddSensorJFrame extends JmriJFrame {

    public AddSensorJFrame() {
        super(Bundle.getMessage("TitleAddSensor"));

        addHelpMenu("package.jmri.jmrit.beantable.SensorAddEdit", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(new AddSensorPanel());
        pack();
    }

}
