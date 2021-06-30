package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;

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
    public Sensor getByUserName(String name) {
        return InstanceManager.sensorManagerInstance().getByUserName(name);
    }

    private final JCheckBox sensorDebounceGlobalCheck = new JCheckBox();
    private final JSpinner sensorDebounceInactiveSpinner = new JSpinner();
    private final JSpinner sensorDebounceActiveSpinner = new JSpinner();

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

        sensorDebounceGlobalCheck.addActionListener((ActionEvent e) -> {
            sensorDebounceInactiveSpinner.setEnabled(!sensorDebounceGlobalCheck.isSelected());
            sensorDebounceActiveSpinner.setEnabled(!sensorDebounceGlobalCheck.isSelected());
        });

        sensorDebounceInactiveSpinner.setModel(
                new SpinnerNumberModel((Long)0L, (Long)0L, Sensor.MAX_DEBOUNCE, (Long)1L));  // MAX_DEBOUNCE is a Long; casts are to force needed signature
        sensorDebounceInactiveSpinner.setPreferredSize(new JTextField(Long.toString(Sensor.MAX_DEBOUNCE).length()+1).getPreferredSize());
        sensorDebounceActiveSpinner.setModel(
                new SpinnerNumberModel((Long)0L, (Long)0L, Sensor.MAX_DEBOUNCE, (Long)1L));  // MAX_DEBOUNCE is a Long; casts are to force needed signature
        sensorDebounceActiveSpinner.setPreferredSize(new JTextField(Long.toString(Sensor.MAX_DEBOUNCE).length()+1).getPreferredSize());

        basic.addItem(new BeanEditItem(null, null, Bundle.getMessage("SensorDebounceText")));
        basic.addItem(new BeanEditItem(sensorDebounceGlobalCheck, Bundle.getMessage("SensorDebounceUseGlobalText"), Bundle.getMessage("SensorGlobalActiveInactiveDelays",
            InstanceManager.getDefault(SensorManager.class).getDefaultSensorDebounceGoingActive(),
            InstanceManager.getDefault(SensorManager.class).getDefaultSensorDebounceGoingInActive())));
        basic.addItem(new BeanEditItem(sensorDebounceActiveSpinner, Bundle.getMessage("SensorActiveDebounce"), Bundle.getMessage("SensorActiveDebounceText")));
        basic.addItem(new BeanEditItem(sensorDebounceInactiveSpinner, Bundle.getMessage("SensorInActiveDebounce"), Bundle.getMessage("SensorInActiveDebounceText")));

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
        
        sensorDebounceActiveSpinner.setEnabled(!bean.getUseDefaultTimerSettings());
        sensorDebounceInactiveSpinner.setEnabled(!bean.getUseDefaultTimerSettings());
        
        sensorDebounceActiveSpinner.setValue(bean.getSensorDebounceGoingActiveTimer()); // as long
        sensorDebounceInactiveSpinner.setValue(bean.getSensorDebounceGoingInActiveTimer());
    }

    public void enabled(Boolean boo) {
        sensorDebounceGlobalCheck.setEnabled(boo);
        sensorDebounceInactiveSpinner.setEnabled(boo);
        sensorDebounceActiveSpinner.setEnabled(boo);
    }

}
