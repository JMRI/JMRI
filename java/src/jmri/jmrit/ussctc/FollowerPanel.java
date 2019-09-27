package jmri.jmrit.ussctc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface frame for creating and editing "Follower" logic on USS CTC
 * machines.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class FollowerPanel extends BasePanel {

    JTextField outputName;
    JTextField sensorName;
    JTextField vetoName;

    JCheckBox invert;

    JButton viewButton;
    JButton addButton;

    public FollowerPanel() {

        JPanel p2xs = new JPanel();
        JLabel label;
        p2xs.setLayout(new BoxLayout(p2xs, BoxLayout.Y_AXIS));

        // add output name field
        JPanel p3 = new JPanel();

        label = new JLabel(Bundle.getMessage("LabelOutputName")); //NOI18N
        label.setToolTipText(Bundle.getMessage("ToolTipFollowerOutput")); //NOI18N
        p3.add(label);
        outputName = new JTextField(12);
        outputName.setToolTipText(Bundle.getMessage("ToolTipFollowerOutput")); //NOI18N
        p3.add(outputName);
        p2xs.add(p3);

        // add sensor name field
        p3 = new JPanel();
        label = new JLabel(Bundle.getMessage("LabelSensorName")); //NOI18N
        label.setToolTipText(Bundle.getMessage("ToolTipFollowerSensor")); //NOI18N
        p3.add(label);
        sensorName = new JTextField(12);
        sensorName.setToolTipText(Bundle.getMessage("ToolTipFollowerSensor")); //NOI18N
        p3.add(sensorName);
        p2xs.add(p3);
        invert = new JCheckBox(Bundle.getMessage("ButtonInvert")); //NOI18N
        invert.setToolTipText(Bundle.getMessage("ToolTipFollowerInvert")); //NOI18N
        p2xs.add(invert);

        // add veto name field
        p3 = new JPanel();
        label = new JLabel(Bundle.getMessage("LabelVetoName")); //NOI18N
        label.setToolTipText(Bundle.getMessage("ToolTipFollowerVeto")); //NOI18N
        p3.add(label);
        vetoName = new JTextField(12);
        vetoName.setToolTipText(Bundle.getMessage("ToolTipFollowerVeto")); //NOI18N
        p3.add(vetoName);
        p2xs.add(p3);

        add(p2xs);

        // buttons
        p2xs = new JPanel();
        p2xs.setLayout(new BoxLayout(p2xs, BoxLayout.Y_AXIS));

        viewButton = new JButton(Bundle.getMessage("ButtonView")); //NOI18N
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewPressed();
            }
        });
        p2xs.add(viewButton);
        addButton = new JButton(Bundle.getMessage("ButtonAddUpdate")); //NOI18N
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPressed();
            }
        });
        p2xs.add(addButton);
        add(p2xs);
    }

    void addPressed() {
        boolean ok = true;
        // validate
        ok &= validateTurnout(outputName.getText());
        ok &= validateSensor(sensorName.getText());
        if (!vetoName.getText().isEmpty()) {
            ok &= validateSensor(vetoName.getText());
        }

        // no errors?
        if (!ok) {
            return;
        }
        // create
        new Follower(outputName.getText(), sensorName.getText(), invert.isSelected(), vetoName.getText())
                .instantiate();
    }

    void viewPressed() {
        try {
            Follower o = new Follower(outputName.getText());
            sensorName.setText(o.getSensorName());
            invert.setSelected(o.getInvert());
            vetoName.setText(o.getVetoName());
        } catch (jmri.JmriException e) {
            log.error("Exception trying to find existing OS Indicator: " + e); //NOI18N
        }
    }

    private final static Logger log = LoggerFactory.getLogger(FollowerPanel.class);

}
