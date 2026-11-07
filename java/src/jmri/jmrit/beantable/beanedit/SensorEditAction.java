package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.Sensor;

/**
 * Provides an edit panel for a Sensor object.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SensorEditAction extends BeanEditAction<Sensor> {

    @Override
    public String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorAddEdit";
    } // NOI18N

    SensorDebounceEditAction debounce;
    SensorPullUpEditAction pullup;

    @Override
    protected void initPanels() {
        super.initPanels();
        debounce = new SensorDebounceEditAction();
        debounce.setBean(bean);
        bei.add(debounce.sensorDebounce(null));
        pullup = new SensorPullUpEditAction();
        pullup.setBean(bean);
        bei.add(pullup.sensorPullUp(null));
    }

    @Override
    public Sensor getByUserName(String name) {
        return InstanceManager.sensorManagerInstance().getByUserName(name);
    }

    JCheckBox inverted = new JCheckBox();
    JComboBox<Sensor.Detects> detects = new JComboBox<>(Sensor.Detects.values());

    @Override
    BeanItemPanel basicDetails() {
        BeanItemPanel basic = super.basicDetails();

        basic.addItem(new BeanEditItem(inverted, Bundle.getMessage("Inverted"), Bundle.getMessage("SensorInvertedToolTip")));
        basic.addItem(new BeanEditItem(detects, Bundle.getMessage("SensorDetects"), Bundle.getMessage("SensorDetectsToolTip")));

        return basic;
    }

    @Override
    protected void saveBasicItems(ActionEvent e) {
        super.saveBasicItems(e);
        bean.setInverted(inverted.isSelected());
        Object d = detects.getSelectedItem();
        if (d instanceof Sensor.Detects) {
            bean.setDetects((Sensor.Detects) d);
        }
    }

    @Override
    protected void resetBasicItems(ActionEvent e) {
        super.resetBasicItems(e);
        inverted.setSelected(bean.getInverted());
        detects.setSelectedItem(bean.getDetects());
    }

}
