package jmri.jmrit.picker;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;

/**
 * Demo for exploring operation of the PickSinglePanel class
 *
 * @author	Bob Jacobsen Copyright 2017
 */
public class PickSinglePanelDemo implements ListSelectionListener {

    // Main entry point
    static public void main(String[] args) {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();

        PickSinglePanelDemo demo = new PickSinglePanelDemo();
        demo.start();
    }

    PickListModel<Sensor> tableModel;
    PickSinglePanel<Sensor> panel;

    void start() {
        SensorManager m = InstanceManager.getDefault(SensorManager.class);
        m.provideSensor("1");
        m.provideSensor("2");
        m.provideSensor("3").setUserName("Three");
        m.provideSensor("4").setUserName("Four");
        m.provideSensor("5").setUserName("Five");
        m.provideSensor("6");
        m.provideSensor("7");
        m.provideSensor("8");
        m.provideSensor("9");

        tableModel = PickListModel.sensorPickModelInstance();
        panel = new PickSinglePanel<Sensor>(tableModel);

        // add a listener
        panel.getTable().getSelectionModel().addListSelectionListener(this);

        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        JFrame f = new JFrame();
        f.add(panel);
        f.pack();
        f.setVisible(true);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        System.out.println("  Selected: " + panel.getSelectedBeanHandle());
    }

}
