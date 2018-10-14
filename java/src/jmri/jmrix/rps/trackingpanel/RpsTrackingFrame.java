package jmri.jmrix.rps.trackingpanel;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Frame containing the entire display tool
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 */
public class RpsTrackingFrame extends jmri.util.JmriJFrame {

    RpsSystemConnectionMemo memo = null;

    public RpsTrackingFrame(String s,RpsSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public RpsTrackingFrame(RpsSystemConnectionMemo _memo) {
        this("RPS Tracking Display",_memo);
    }

    @Override
    public void dispose() {
        panel.dispose();
        super.dispose();
    }

    @Override
    public void initComponents() {

        addHelpMenu("package.jmri.jmrix.rps.trackingpanel.RpsTrackingFrame", true);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        panel = new RpsTrackingPanel(memo);

        // add controls; first, clear button
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        JPanel line = new JPanel();
        line.setLayout(new FlowLayout());

        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearButtonChanged();
            }
        });
        line.add(clearButton);

        // button for handling errors
        showErrButton = new JCheckBox("Show error points");
        showErrButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showErrButtonChanged();
            }
        });
        line.add(showErrButton);

        // button for showing receivers
        showRecButton = new JCheckBox("Show receivers");
        showRecButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showRecButtonChanged();
            }
        });
        showRecButton.setSelected(true);
        showRecButtonChanged();
        line.add(showRecButton);

        // button for showing regions
        showRegButton = new JCheckBox("Show regions");
        showRegButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showRegButtonChanged();
            }
        });
        showRegButton.setSelected(true);
        showRegButtonChanged();
        line.add(showRegButton);

        controls.add(line);

        // configure alignment control
        scale = new RpsTrackingControlPane(panel);
        controls.add(scale);
        controls.add(new JSeparator());

        panel.setSize(440, 240);
        panel.setPreferredSize(new Dimension(440, 240));

        scale.set(-10., -10., 50., 50.);  // lower left X, Y, then upper right 
        scale.update();

        // combine
        //JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, controls, panel);
        //getContentPane().add(split);
        getContentPane().add(panel);
        getContentPane().add(new JSeparator());
        getContentPane().add(controls);

        panel.repaint();
        pack();

        // fix size of controls
        Dimension d = controls.getSize();
        controls.setMinimumSize(d);
        controls.setPreferredSize(d);
        controls.setMaximumSize(d);

    }

    JButton clearButton;
    JCheckBox showErrButton;
    JCheckBox showRecButton;
    JCheckBox showRegButton;
    RpsTrackingPanel panel;
    public RpsTrackingControlPane scale;

    void showErrButtonChanged() {
        panel.setShowErrors(showErrButton.isSelected());
    }

    void showRecButtonChanged() {
        panel.setShowReceivers(showRecButton.isSelected());
        panel.repaint();
    }

    void showRegButtonChanged() {
        panel.setShowRegions(showRegButton.isSelected());
        panel.repaint();
    }

    void clearButtonChanged() {
        panel.clear();
    }
}
