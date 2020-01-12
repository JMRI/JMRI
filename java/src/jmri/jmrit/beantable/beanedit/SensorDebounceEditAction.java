package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import jmri.Sensor;

/**
 * Provides an edit panel for a sensor debounce object.
 * <p>
 * This is designed so that it can be re-used in other panels.
 * {@link jmri.jmrit.beantable.beanedit.BlockEditAction#sensor()}
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Egbert Broerse Copyright (C) 2017
 */
public class SensorDebounceEditAction extends BeanEditAction<Sensor> {

    @Override
    public String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorAddEdit";
    } // NOI18N

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameSensor");
    }

    @Override
    public Sensor getByUserName(String name) {
        return InstanceManager.sensorManagerInstance().getByUserName(name);
    }

    JCheckBox sensorDebounceGlobalCheck = new JCheckBox();
    JSpinner sensorDebounceInactiveSpinner = new JSpinner();
    JSpinner sensorDebounceActiveSpinner = new JSpinner();

    @Override
    protected void initPanels() {
    }

    @Override
    public void setBean(Sensor bean) {
        super.setBean(bean);
        if (bean == null) {
            enabled(false);
        } else {
            resetDebounceItems(null); // Get this to retrieve the current details.
            enabled(true);
        }
    }

    public BeanItemPanel sensorDebounce(BeanItemPanel basic) {
        if (basic == null) {
            basic = new BeanItemPanel();
            basic.setName(Bundle.getMessage("SensorDebounce"));
        }

        sensorDebounceGlobalCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sensorDebounceGlobalCheck.isSelected()) {
                    sensorDebounceInactiveSpinner.setEnabled(false);
                    sensorDebounceActiveSpinner.setEnabled(false);
                } else {
                    sensorDebounceInactiveSpinner.setEnabled(true);
                    sensorDebounceActiveSpinner.setEnabled(true);
                }
            }
        });

        basic.addItem(new BeanEditItem(null, null, Bundle.getMessage("SensorDebounceText")));
        basic.addItem(new BeanEditItem(sensorDebounceGlobalCheck, Bundle.getMessage("SensorDebounceUseGlobalText"), null));
        sensorDebounceInactiveSpinner.setModel(
                new SpinnerNumberModel((Long)0L, (Long)0L, Sensor.MAX_DEBOUNCE, (Long)1L));  // MAX_DEBOUNCE is a Long; casts are to force needed signature
//        sensorDebounceInactiveSpinner.setValue(0); // reset from possible previous use
        sensorDebounceInactiveSpinner.setPreferredSize(new JTextField(Long.toString(Sensor.MAX_DEBOUNCE).length()+1).getPreferredSize());
        basic.addItem(new BeanEditItem(sensorDebounceInactiveSpinner, Bundle.getMessage("SensorInActiveDebounce"), Bundle.getMessage("SensorInActiveDebounceText")));
        sensorDebounceActiveSpinner.setModel(
                new SpinnerNumberModel((Long)0L, (Long)0L, Sensor.MAX_DEBOUNCE, (Long)1L));  // MAX_DEBOUNCE is a Long; casts are to force needed signature
//        sensorDebounceActiveSpinner.setValue(0); // reset from possible previous use
        sensorDebounceActiveSpinner.setPreferredSize(new JTextField(Long.toString(Sensor.MAX_DEBOUNCE).length()+1).getPreferredSize());
        basic.addItem(new BeanEditItem(sensorDebounceActiveSpinner, Bundle.getMessage("SensorActiveDebounce"), Bundle.getMessage("SensorActiveDebounceText")));

        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDebounceItems(e);
            }
        });
        basic.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetDebounceItems(e);
            }
        });

        return basic;
    }

    protected void saveDebounceItems(ActionEvent e) {
        if (bean == null) {
            return;
        }

        long time = (long) sensorDebounceActiveSpinner.getValue();
        bean.setSensorDebounceGoingActiveTimer(time);

        time = (long) sensorDebounceInactiveSpinner.getValue();
        bean.setSensorDebounceGoingInActiveTimer(time);
        bean.setUseDefaultTimerSettings(sensorDebounceGlobalCheck.isSelected());
    }

    protected void resetDebounceItems(ActionEvent e) {
        if (bean == null) {
            enabled(false);
            return;
        }
        enabled(true);
        sensorDebounceGlobalCheck.setSelected(bean.getUseDefaultTimerSettings());
        if (bean.getUseDefaultTimerSettings()) {
            sensorDebounceActiveSpinner.setEnabled(false);
            sensorDebounceInactiveSpinner.setEnabled(false);
        } else {
            sensorDebounceActiveSpinner.setEnabled(true);
            sensorDebounceInactiveSpinner.setEnabled(true);
        }
        sensorDebounceActiveSpinner.setValue(Long.valueOf(bean.getSensorDebounceGoingActiveTimer())); // as long
        sensorDebounceInactiveSpinner.setValue(Long.valueOf(bean.getSensorDebounceGoingInActiveTimer()));
    }

    public void enabled(Boolean boo) {
        sensorDebounceGlobalCheck.setEnabled(boo);
        sensorDebounceInactiveSpinner.setEnabled(boo);
        sensorDebounceActiveSpinner.setEnabled(boo);
    }

}
