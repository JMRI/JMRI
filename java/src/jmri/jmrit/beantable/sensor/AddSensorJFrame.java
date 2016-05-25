// AddSensorJFrame.java
package jmri.jmrit.beantable.sensor;

import javax.swing.BoxLayout;
import jmri.util.JmriJFrame;

/**
 * JFrame to create a new Sensor
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version $Revision$
 * @deprecated Replaced by
 * {@link jmri.jmrit.beantable.AddNewHardwareDevicePanel}
 */
@Deprecated
public class AddSensorJFrame extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 764691745775053638L;

    public AddSensorJFrame() {
        super(Bundle.getMessage("TitleAddSensor"));

        addHelpMenu("package.jmri.jmrit.beantable.SensorAddEdit", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(new AddSensorPanel());
        pack();
    }

}


/* @(#)AddSensorJFrame.java */
