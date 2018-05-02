package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Provides an edit panel for the sensor pull up/pull down configuration.
 * This is designed so that it can be re-used in other panels.  This is based
 * on the SensorDebounceEditAction class.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Kevin Dickerson Copyright (C) 2017 
 */
public class SensorPullUpEditAction extends BeanEditAction {

    @Override
    public String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    } //IN18N

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameSensor");
    }

    @Override
    public NamedBean getByUserName(String name) {
        return InstanceManager.sensorManagerInstance().getByUserName(name);
    }

    JComboBox<Sensor.PullResistance> sensorPullUpComboBox = new JComboBox<Sensor.PullResistance>(Sensor.PullResistance.values());

    @Override
    protected void initPanels() {
    }

    @Override
    public void setBean(NamedBean bean) {
        super.setBean(bean);
        if (bean == null) {
            enabled(false);
        } else {
            resetPullUpItems(null); //Get this to retrieve the current details.
            enabled(true);
        }
    }

    public BeanItemPanel sensorPullUp(BeanItemPanel basic) {
        if (basic == null) {
            basic = new BeanItemPanel();
            basic.setName(Bundle.getMessage("SensorPullUp"));
        }

        basic.addItem(new BeanEditItem(null,null,Bundle.getMessage("SensorPullUpText")));
        basic.addItem(new BeanEditItem(sensorPullUpComboBox,Bundle.getMessage("SensorPullUp"),null));

        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePullUpItems(e);
            }
        });
        basic.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetPullUpItems(e);
            }
        });

        return basic;
    }

    protected void savePullUpItems(ActionEvent e) {
        if (bean == null) {
            return;
        }

        Sensor sen = (Sensor) bean;
        sen.setPullResistance((Sensor.PullResistance)sensorPullUpComboBox.getSelectedItem());
    }

    protected void resetPullUpItems(ActionEvent e) {
        if (bean == null) {
            enabled(false);
            return;
        }
        enabled(true);
        Sensor sen = (Sensor) bean;
        sensorPullUpComboBox.setSelectedItem(sen.getPullResistance());
    }

    public void enabled(Boolean boo) {
        sensorPullUpComboBox.setEnabled(boo);

    }

}
