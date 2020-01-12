package jmri.jmrix.rps.swing.soundset;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.MeasurementListener;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.ReadingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for control of the sound speed for the RPS system.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class SoundSetPane extends JPanel
        implements ReadingListener, MeasurementListener, PropertyChangeListener {

    public SoundSetPane() {
        super();
    }

    public void dispose() {
        // separate from data source
        Distributor.instance().removeReadingListener(this);
        Distributor.instance().removeMeasurementListener(this);
        Engine.instance().removePropertyChangeListener(this);
    }

    JTextField vsound;
    JTextField newval;
    JTextField dist;
    JTextField id;
    JTextField rcvr;
    JTextField speed;
    JCheckBox auto;
    JTextField gain;

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("vSound")) {
            // update sound display
            vsound.setText(nf.format(e.getNewValue()));
        }
    }

    public void initComponents() {
        // number format
        nf = java.text.NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(5);
        nf.setGroupingUsed(false);

        // GUI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // current value
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Current sound velocity: "));
        vsound = new JTextField(5);
        vsound.setEnabled(false);
        vsound.setText(nf.format(Engine.instance().getVSound()));
        p.add(vsound);
        this.add(p);

        this.add(new JSeparator());

        // set new value
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("New sound velocity: "));
        newval = new JTextField(8);
        p.add(newval);
        JButton b = new JButton("Set");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setPushed();
            }
        });
        p.add(b);
        this.add(p);

        this.add(new JSeparator());

        // calculate new speed
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Known Distance: "));
        dist = new JTextField(5);
        p.add(dist);
        p.add(new JLabel("Transmitter ID: "));
        id = new JTextField(5);
        p.add(id);
        p.add(new JLabel("Receiver Number: "));
        rcvr = new JTextField(3);
        p.add(rcvr);
        this.add(p);
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Measured Speed: "));
        speed = new JTextField(8);
        speed.setEnabled(false);
        p.add(speed);
        auto = new JCheckBox("Auto Set");
        p.add(auto);
        p.add(new JLabel("Damping: "));
        gain = new JTextField(3);
        gain.setText("10.");
        p.add(gain);
        this.add(p);

        // start working
        Distributor.instance().addReadingListener(this);
        Distributor.instance().addMeasurementListener(this);
        Engine.instance().addPropertyChangeListener(this);
    }

    void setPushed() {
        double val = Double.parseDouble(newval.getText());
        Engine.instance().setVSound(val);
    }

    java.text.NumberFormat nf;

    @Override
    public void notify(Reading r) {
        try {
            // right ID?
            if (!r.getId().equals(id.getText())) {
                return;
            }

            // get the right measurement
            int i = Integer.parseInt(rcvr.getText());
            if (i < 1 || i > r.getNValues()) {
                log.warn("resetting receiver number");
                rcvr.setText("");
            }
            log.debug("Rcvr " + i + " saw " + r.getValue(i));
            double val = r.getValue(i);

            // can't use speed too small
            if (val < 100) {
                log.warn("time too small to use: " + val);
                return;
            }

            // calculate speed
            double newspeed = Double.parseDouble(dist.getText()) / val;
            speed.setText(nf.format(newspeed));

            // if auto, do update
            if (auto.isSelected()) {
                double g = Double.parseDouble(gain.getText());
                if (g < 1) {
                    log.warn("resetting gain from " + gain.getText());
                    gain.setText("10.");
                    return;
                }
                double updatedspeed = (newspeed + g * Engine.instance().getVSound()) / (g + 1);
                Engine.instance().setVSound(updatedspeed);
            }
        } catch (Exception e) {
            log.debug("Error calculating speed: " + e);
            speed.setText("");
        }
    }

    @Override
    public void notify(Measurement m) {
        // don't have to do anything
    }

    private final static Logger log = LoggerFactory.getLogger(SoundSetPane.class);

}
