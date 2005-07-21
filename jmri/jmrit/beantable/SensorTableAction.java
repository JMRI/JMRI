// SensorTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Sensor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Swing action to create and register a
 * SensorTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.13 $
 */

public class SensorTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public SensorTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary sensor manager available
        if (jmri.InstanceManager.sensorManagerInstance()==null ||
            (((jmri.managers.AbstractProxyManager)jmri.InstanceManager
                                                 .sensorManagerInstance())
                                                 .systemLetter()=='\0')) {
            setEnabled(false);
        }
    }
    public SensorTableAction() { this("Sensor Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Sensors
     */
    void createModel() {
        m = new BeanTableDataModel() {
            public String getValue(String name) {
                int val = InstanceManager.sensorManagerInstance().getBySystemName(name).getKnownState();
                switch (val) {
                case Sensor.ACTIVE: return rbean.getString("SensorStateActive");
                case Sensor.INACTIVE: return rbean.getString("SensorStateInactive");
                case Sensor.UNKNOWN: return rbean.getString("BeanStateUnknown");
                case Sensor.INCONSISTENT: return rbean.getString("BeanStateInconsistent");
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.sensorManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.sensorManagerInstance().getBySystemName(name);}
            public void clickOn(NamedBean t) {
                try {
                    int state = ((Sensor)t).getKnownState();
                    if (state==Sensor.INACTIVE) ((Sensor)t).setKnownState(Sensor.ACTIVE);
                    else ((Sensor)t).setKnownState(Sensor.INACTIVE);
                } catch (JmriException e) { this.log.warn("Error setting state: "+e); }
            }
            public JButton configureButton() {
                return new JButton(rbean.getString("SensorStateInactive"));
            }

        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleSensorTable"));
    }

    JFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JFrame(rb.getString("TitleAddSensor"));
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
            p.add(sysName);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(userNameLabel);
            p.add(userName);
            addFrame.getContentPane().add(p);

            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        addFrame.pack();
        addFrame.show();
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        InstanceManager.sensorManagerInstance().newSensor(sysName.getText().toUpperCase(), user);
    }

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorTableAction.class.getName());
}


/* @(#)SensorTableAction.java */
