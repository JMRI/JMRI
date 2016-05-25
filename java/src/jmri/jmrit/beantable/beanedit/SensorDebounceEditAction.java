// StatusPanel.java
package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Provides an edit panel for a sensor debounce object This is designed so that
 * it can be re-used in other panels.
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 * @version	$Revision: 19923 $
 */
public class SensorDebounceEditAction extends BeanEditAction {

    /**
     *
     */
    private static final long serialVersionUID = -3622370435108538307L;

    public String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    } //IN18N

    public String getBeanType() {
        return Bundle.getMessage("BeanNameSensor");
    }

    public NamedBean getByUserName(String name) {
        return InstanceManager.sensorManagerInstance().getByUserName(name);
    }

    JTextField sensorDebounceInactiveField = new JTextField(5);
    JTextField sensorDebounceActiveField = new JTextField(5);
    JCheckBox sensorDebounceGlobalCheck = new JCheckBox();

    @Override
    protected void initPanels() {
    }

    @Override
    public void setBean(NamedBean bean) {
        super.setBean(bean);
        if (bean == null) {
            enabled(false);
        } else {
            resetDebounceItems(null); //Get this to retrieve the current details.
            enabled(true);
        }
    }

    public BeanItemPanel sensorDebounce(BeanItemPanel basic) {
        if (basic == null) {
            basic = new BeanItemPanel();
            basic.setName(Bundle.getMessage("SensorDebounce"));
        }

        sensorDebounceGlobalCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (sensorDebounceGlobalCheck.isSelected()) {
                    sensorDebounceInactiveField.setEnabled(false);
                    sensorDebounceActiveField.setEnabled(false);
                } else {
                    sensorDebounceInactiveField.setEnabled(true);
                    sensorDebounceActiveField.setEnabled(true);
                }
            }
        });

        sensorDebounceInactiveField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }

            public void keyReleased(KeyEvent keyEvent) {
                String text = sensorDebounceInactiveField.getText();
                if (!validateNumericalInput(text)) {
                    String msg = java.text.MessageFormat.format(Bundle.getMessage("ShouldBeNumber"), new Object[]{Bundle.getMessage("SensorInActiveDebounce")});
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).showInfoMessage(Bundle.getMessage("ErrorTitle"), msg, "Block Details", "Inactive", false, false);
                }
            }

            public void keyTyped(KeyEvent keyEvent) {
            }
        });

        sensorDebounceActiveField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }

            public void keyReleased(KeyEvent keyEvent) {
                String text = sensorDebounceActiveField.getText();
                if (!validateNumericalInput(text)) {
                    String msg = java.text.MessageFormat.format(Bundle.getMessage("ShouldBeNumber"), new Object[]{Bundle.getMessage("SensorActiveDebounce")});
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).showInfoMessage(Bundle.getMessage("ErrorTitle"), msg, "Block Details", "Active", false, false);
                }
            }

            public void keyTyped(KeyEvent keyEvent) {
            }
        });

        basic.addItem(new BeanEditItem(null, null, Bundle.getMessage("SensorDebounceText")));
        basic.addItem(new BeanEditItem(sensorDebounceGlobalCheck, Bundle.getMessage("SensorDebounceUseGlobalText"), null));
        basic.addItem(new BeanEditItem(sensorDebounceInactiveField, Bundle.getMessage("SensorInActiveDebounce"), Bundle.getMessage("SensorInActiveDebounceText")));
        basic.addItem(new BeanEditItem(sensorDebounceActiveField, Bundle.getMessage("SensorActiveDebounce"), Bundle.getMessage("SensorActiveDebounceText")));

        basic.setSaveItem(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -4211002470804824662L;

            public void actionPerformed(ActionEvent e) {
                saveDebounceItems(e);
            }
        });
        basic.setResetItem(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -9107982698145419220L;

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

        Sensor sen = (Sensor) bean;
        String timeVal = sensorDebounceActiveField.getText();
        int time = Integer.valueOf(timeVal).intValue();
        sen.setSensorDebounceGoingActiveTimer(time);

        timeVal = sensorDebounceInactiveField.getText();
        time = Integer.valueOf(timeVal).intValue();
        sen.setSensorDebounceGoingInActiveTimer(time);
        sen.useDefaultTimerSettings(sensorDebounceGlobalCheck.isSelected());
    }

    protected void resetDebounceItems(ActionEvent e) {
        if (bean == null) {
            enabled(false);
            return;
        }
        enabled(true);
        Sensor sen = (Sensor) bean;
        sensorDebounceGlobalCheck.setSelected(sen.useDefaultTimerSettings());
        if (sen.useDefaultTimerSettings()) {
            sensorDebounceActiveField.setEnabled(false);
            sensorDebounceInactiveField.setEnabled(false);
        } else {
            sensorDebounceActiveField.setEnabled(true);
            sensorDebounceInactiveField.setEnabled(true);
        }
        sensorDebounceActiveField.setText("" + sen.getSensorDebounceGoingActiveTimer());
        sensorDebounceInactiveField.setText("" + sen.getSensorDebounceGoingInActiveTimer());
    }

    public void enabled(Boolean boo) {
        sensorDebounceGlobalCheck.setEnabled(boo);
        sensorDebounceInactiveField.setEnabled(boo);
        sensorDebounceActiveField.setEnabled(boo);

    }

}
