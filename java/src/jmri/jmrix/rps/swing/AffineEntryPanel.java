package jmri.jmrix.rps.swing;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Panel for entry and modifiation of an Affine Transform.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class AffineEntryPanel extends javax.swing.JPanel {

    JTextField m00 = new JTextField(8);
    JTextField m01 = new JTextField(8);
    JTextField m02 = new JTextField(8);
    JTextField m10 = new JTextField(8);
    JTextField m11 = new JTextField(8);
    JTextField m12 = new JTextField(8);

    public AffineEntryPanel() {
        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(3, 3));

        p.add(new JLabel("X"));
        p.add(new JLabel("Y"));
        p.add(new JLabel("Offset"));

        p.add(m00);
        p.add(m01);
        p.add(m02);

        p.add(m10);
        p.add(m11);
        p.add(m12);

        add(p);

        JButton b = new JButton("Set");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                update();
            }
        });
        add(b);

        // init transform
        m00.setText("1.");
        m11.setText("1.");
        m01.setText("0.");
        m02.setText("0.");
        m10.setText("0.");
        m12.setText("0.");
        update();
    }

    /**
     * Load GIU to transform
     */
    void update() {
        AffineTransform oldt = t;
        double v00 = Double.parseDouble(m00.getText());
        double v01 = Double.parseDouble(m01.getText());
        double v02 = Double.parseDouble(m02.getText());
        double v10 = Double.parseDouble(m10.getText());
        double v11 = Double.parseDouble(m11.getText());
        double v12 = Double.parseDouble(m12.getText());
        t = new AffineTransform(v00, v10, v01, v11, v02, v12);
        firePropertyChange("value", oldt, t);
    }

    public AffineTransform getTransform() {
        return t;
    }

    public void setTransform(AffineTransform tnew) {
        m00.setText("" + tnew.getScaleX());
        m11.setText("" + tnew.getScaleY());

        m01.setText("" + tnew.getShearX());
        m10.setText("" + tnew.getShearY());

        m02.setText("" + tnew.getTranslateX());
        m12.setText("" + tnew.getTranslateY());
        update();
    }

    AffineTransform t = new AffineTransform();

}
