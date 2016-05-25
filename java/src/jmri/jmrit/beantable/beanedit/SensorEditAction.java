// StatusPanel.java
package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Provides an edit panel for a sensor object
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 * @version	$Revision: 19923 $
 */
public class SensorEditAction extends BeanEditAction {

    /**
     *
     */
    private static final long serialVersionUID = 3309738587501961767L;

    public String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    } //IN18N

    SensorDebounceEditAction debounce;

    @Override
    protected void initPanels() {
        super.initPanels();
        debounce = new SensorDebounceEditAction();
        debounce.setBean(bean);
        bei.add(debounce.sensorDebounce(null));
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameSensor");
    }

    public NamedBean getByUserName(String name) {
        return InstanceManager.sensorManagerInstance().getByUserName(name);
    }

    JCheckBox inverted = new JCheckBox();

    @Override
    BeanItemPanel basicDetails() {
        BeanItemPanel basic = super.basicDetails();

        basic.addItem(new BeanEditItem(inverted, Bundle.getMessage("Inverted"), Bundle.getMessage("SensorInvertedToolTip")));

        return basic;
    }

    @Override
    protected void saveBasicItems(ActionEvent e) {
        super.saveBasicItems(e);
        Sensor sen = (Sensor) bean;
        sen.setInverted(inverted.isSelected());
    }

    @Override
    protected void resetBasicItems(ActionEvent e) {
        super.resetBasicItems(e);
        Sensor sen = (Sensor) bean;
        inverted.setSelected(sen.getInverted());
    }

}
