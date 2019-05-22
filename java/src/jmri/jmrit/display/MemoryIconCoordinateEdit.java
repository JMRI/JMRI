package jmri.jmrit.display;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Displays and allows user to modify {@literal x & y} coordinates of
 * positionable labels This class has been generalized to provide popup edit
 * dialogs for positionable item properties when TextFields are needed to input
 * data.
 * <p>
 * The class name no longer identifies the full purpose of the class, However
 * the name is retained because coordinate editing was the genesis. The current
 * list of properties served for editing is:
 * <ul>
 * <li>modify {@literal x & y} coordinates modify level modify tooltip modify
 * border size</li>
 * <li>modify margin size modify fixed size modify rotation degress modify
 * scaling</li>
 * <li>modify text labels modify zoom scaling modify panel name</li>
 * </ul>
 * To use, write a static method that provides the dialog frame. Then write an
 * initX method that customizes the dialog for the property.
 *
 * @author Dan Boudreau Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2010
 */
public class MemoryIconCoordinateEdit extends CoordinateEdit {

    MemoryIcon pl;    // positional label tracked by this frame
    int oldX;
    int oldY;
    double oldD;
    String oldStr;

    public void init(String title, MemoryIcon pos, boolean showName) {
        super.init(title, pos, showName);
        pl = pos;
    }

    public static AbstractAction getCoordinateEditAction(final MemoryIcon pos) {
        return new AbstractAction(Bundle.getMessage("SetXY", "")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                MemoryIconCoordinateEdit f = new MemoryIconCoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("SetXY", ""), pos, true);
                f.initSetXY();
                f.setVisible(true);
                f.setLocationRelativeTo(pos);
            }
        };
    }

    @Override
    public void initSetXY() {
        oldX = pl.getOriginalX();
        oldY = pl.getOriginalY();

        textX = new javax.swing.JLabel();
        textX.setText("X: " + pl.getOriginalX());
        textX.setVisible(true);
        textY = new javax.swing.JLabel();
        textY.setText("Y: " + pl.getOriginalY());
        textY.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 10000, 1);
        ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int x = ((Number) spinX.getValue()).intValue();
                int y = ((Number) spinY.getValue()).intValue();
                pl.setLocation(x, y);
                textX.setText(" X: " + pl.getOriginalX());
                textY.setText(" Y: " + pl.getOriginalY());
            }
        };
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(Integer.valueOf(pl.getOriginalX()));
        spinX.setToolTipText(Bundle.getMessage("EnterXcoord"));
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));
        spinX.addChangeListener(listener);
        model = new javax.swing.SpinnerNumberModel(0, 0, 10000, 1);
        spinY = new javax.swing.JSpinner(model);
        spinY.setValue(Integer.valueOf(pl.getOriginalY()));
        spinY.setToolTipText(Bundle.getMessage("EnterYcoord"));
        spinY.setMaximumSize(new Dimension(
                spinY.getMaximumSize().width, spinY.getPreferredSize().height));
        spinY.addChangeListener(listener);

        getContentPane().setLayout(new GridBagLayout());

        addSpinItems(true);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int x = ((Number) spinX.getValue()).intValue();
                int y = ((Number) spinY.getValue()).intValue();
                pl.setLocation(x, y);
                textX.setText(" X: " + pl.getOriginalX());
                textY.setText(" Y: " + pl.getOriginalY());
                dispose();
            }
        });
        okButton.getRootPane().setDefaultButton(okButton);

        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.setLocation(oldX, oldY);
                dispose();
            }
        });
        // make large enough to easily move
        setMinimumSize(new Dimension(250, 175));
        pack();
    }
}
