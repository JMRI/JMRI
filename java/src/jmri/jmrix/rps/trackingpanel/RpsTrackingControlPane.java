// RpsTrackingControlPane.java
package jmri.jmrix.rps.trackingpanel;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Panel to control the scaling of a RpsTrackingPane
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version $Revision$
 */
public class RpsTrackingControlPane extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1526969572941516857L;
    RpsTrackingPanel panel;

    public RpsTrackingControlPane(RpsTrackingPanel panel) {
        super();

        this.panel = panel;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p;

        trXf = new JTextField(6);
        trYf = new JTextField(6);
        blXf = new JTextField(6);
        blYf = new JTextField(6);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Upper-right corner X, Y:"));
        p.add(trXf);
        p.add(trYf);
        this.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Lower-left corner X, Y:"));
        p.add(blXf);
        p.add(blYf);
        this.add(p);

        // set button
        JButton set = new JButton("Set");
        set.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                update();
            }
        });
        this.add(set);
    }

    void set(double blX, double blY, double trX, double trY) {
        trXf.setText("" + trX);
        trYf.setText("" + trY);
        blXf.setText("" + blX);
        blYf.setText("" + blY);
    }

    public void update() {
        trX = Double.parseDouble(trXf.getText());
        trY = Double.parseDouble(trYf.getText());
        blX = Double.parseDouble(blXf.getText());
        blY = Double.parseDouble(blYf.getText());

        panel.setOrigin(blX, blY);
        panel.setCoordMax(trX, trY);
        panel.repaint();
    }

    public JTextField trXf;
    public JTextField trYf;
    public JTextField blXf;
    public JTextField blYf;
    double trX, trY, blX, blY;

    private final static Logger log = LoggerFactory.getLogger(RpsTrackingControlPane.class.getName());
}
