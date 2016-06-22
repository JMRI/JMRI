// ConfigToolPane.java
package jmri.jmrix.can.cbus.swing.configtool;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import jmri.InstanceManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusMessage;

/**
 * Pane to ease creation of Sensor, Turnouts and Lights that are linked to CBUS
 * events.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 * @since 2.3.1
 */
public class ConfigToolPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    /**
     *
     */
    private static final long serialVersionUID = -6189931385951469812L;

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.configtool.ConfigToolBundle");

    static final int NRECORDERS = 6;
    CbusEventRecorder[] recorders = new CbusEventRecorder[NRECORDERS];

    public ConfigToolPane() {

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add event displays
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        for (int i = 0; i < recorders.length; i++) {
            recorders[i] = new CbusEventRecorder();
            p1.add(recorders[i]);
        }
        p1.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutEvents")));
        add(p1);

        // add sensor
        makeSensor = new MakeNamedBean("LabelEventActive", "LabelEventInactive") {
            /**
             *
             */
            private static final long serialVersionUID = -7423601645608436305L;

            void create(String name) {
                if (memo != null) {
                    ((jmri.SensorManager) memo.get(jmri.SensorManager.class)).provideSensor("MS" + name);
                } else {
                    InstanceManager.sensorManagerInstance().provideSensor("MS" + name);
                }
            }
        };
        makeSensor.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAddSensor")));
        add(makeSensor);

        // add turnout
        makeTurnout = new MakeNamedBean("LabelEventThrown", "LabelEventClosed") {
            /**
             *
             */
            private static final long serialVersionUID = 5143711808149483844L;

            void create(String name) {
                if (memo != null) {
                    ((jmri.TurnoutManager) memo.get(jmri.TurnoutManager.class)).provideTurnout("MS" + name);
                } else {
                    InstanceManager.turnoutManagerInstance().provideTurnout("MT" + name);
                }
            }
        };
        makeTurnout.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAddTurnout")));
        add(makeTurnout);

    }

    TrafficController tc;

    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
    }

    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Event Capture Tool");

        }
        return "CBUS Event Capture Tool";
    }

    MakeNamedBean makeSensor;
    MakeNamedBean makeTurnout;

    public void reply(jmri.jmrix.can.CanReply m) {
        // forward to anybody waiting to capture
        makeSensor.reply(m);
        makeTurnout.reply(m);
        for (int i = 0; i < recorders.length; i++) {
            if (recorders[i].waiting()) {
                recorders[i].reply(m);
                break;
            }
        }
    }

    public void message(jmri.jmrix.can.CanMessage m) {
        // forward to anybody waiting to capture
        makeSensor.message(m);
        makeTurnout.message(m);
        for (int i = 0; i < recorders.length; i++) {
            if (recorders[i].waiting()) {
                recorders[i].message(m);
                break;
            }
        }
    }

    public void dispose() {
        // disconnect from the CBUS
        tc.removeCanListener(this);
    }

    /**
     * Class to build one NamedBean
     */
    class MakeNamedBean extends JPanel implements CanListener {

        /**
         *
         */
        private static final long serialVersionUID = 7057190769757489242L;
        JTextField f1 = new JTextField(20);
        JTextField f2 = new JTextField(20);

        JButton bc;

        JToggleButton b1 = new JToggleButton(rb.getString("ButtonNext"));
        JToggleButton b2 = new JToggleButton(rb.getString("ButtonNext"));

        MakeNamedBean(String name1, String name2) {
            // actions
            bc = new JButton(rb.getString("ButtonCreate"));
            bc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (f2.getText().equals("")) {
                        create(f1.getText());
                    } else {
                        create(f1.getText() + ";" + f2.getText());
                    }
                }
            });

            // GUI
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = 1;
            c.gridheight = 1;

            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.EAST;
            add(new JLabel(rb.getString(name1)), c);

            c.gridx = 1;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            add(f1, c);

            c.gridx = 2;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            add(b1, c);

            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            add(new JLabel(rb.getString(name2)), c);

            c.gridx = 1;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            add(f2, c);

            c.gridx = 2;
            c.gridy = 1;
            c.anchor = GridBagConstraints.WEST;
            add(b2, c);

            c.gridx = 1;
            c.gridy = 2;
            c.anchor = GridBagConstraints.WEST;
            add(bc, c);
        }

        void create(String name) {
        }

        public void reply(jmri.jmrix.can.CanReply m) {
            if (b1.isSelected()) {
                f1.setText(CbusMessage.toAddress(m));
                b1.setSelected(false);
            }
            if (b2.isSelected()) {
                f2.setText(CbusMessage.toAddress(m));
                b2.setSelected(false);
            }
        }

        public void message(jmri.jmrix.can.CanMessage m) {
            if (b1.isSelected()) {
                f1.setText(CbusMessage.toAddress(m));
                b1.setSelected(false);
            }
            if (b2.isSelected()) {
                f2.setText(CbusMessage.toAddress(m));
                b2.setSelected(false);
            }
        }
    }

    /**
     * Class to handle recording and presenting one event.
     */
    static class CbusEventRecorder extends JPanel implements CanListener {

        /**
         *
         */
        private static final long serialVersionUID = 7826599461789753830L;

        CbusEventRecorder() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(event);
            add(capture);

            event.setEditable(false);
            event.setDragEnabled(true);
            capture.setSelected(true);
        }

        JCheckBox capture = new JCheckBox(rb.getString("MsgCaptureNext"));
        JTextField event = new JTextField(20);

        boolean waiting() {
            return capture.isSelected();
        }

        public void reply(jmri.jmrix.can.CanReply m) {
            if (capture.isSelected()) {
                event.setText(CbusMessage.toAddress(m));
                capture.setSelected(false);
            }
        }

        public void message(jmri.jmrix.can.CanMessage m) {
            if (capture.isSelected()) {
                event.setText(CbusMessage.toAddress(m));
                capture.setSelected(false);
            }
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = 8264581016480363352L;

        public Default() {
            super("CBUS Event Capture Tool",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    ConfigToolPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
}
