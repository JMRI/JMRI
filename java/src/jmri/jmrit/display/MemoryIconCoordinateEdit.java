// CoordinateEdit.java
package jmri.jmrit.display;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Displays and allows user to modify x & y coordinates of positionable labels
 * This class has been generalized to provide popup edit dialogs for
 * positionable item properties when TextFields are needed to input data.
 * <P>
 * The class name no longer identifies the full purpose of the class, However
 * the name is retained because coordinate editing was the genesis. The current
 * list of properties served for editing is:
 * <LI>
 * modify x & y coordinates modify level modify tooltip modify border size
 * modify margin size modify fixed size modify rotation degress modify scaling
 * modify text labels modify zoom scaling modify panel name
 * </LI>
 * To use, write a static method that provides the dialog frame. Then write an
 * initX method that customizes the dialog for the property.
 *
 * @author Dan Boudreau Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2010
 * @version $Revision$
 */
public class MemoryIconCoordinateEdit extends CoordinateEdit {

    /**
     *
     */
    private static final long serialVersionUID = 2891580354099086691L;
    MemoryIcon pl; 			// positional label tracked by this frame
    int oldX;
    int oldY;
    double oldD;
    String oldStr;

    public void init(String title, MemoryIcon pos, boolean showName) {
        super.init(title, pos, showName);
        pl = pos;
    }

    public static AbstractAction getCoordinateEditAction(final MemoryIcon pos) {
        return new AbstractAction(Bundle.getMessage("SetXY")) {
            /**
             *
             */
            private static final long serialVersionUID = -7073550829531445326L;

            public void actionPerformed(ActionEvent e) {
                MemoryIconCoordinateEdit f = new MemoryIconCoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("SetXY"), pos, true);
                f.initSetXY();
                f.setVisible(true);
                f.setLocationRelativeTo(pos);
            }
        };
    }

    public void initSetXY() {
        oldX = pl.getOriginalX();
        oldY = pl.getOriginalY();

        textX = new javax.swing.JLabel();
        textX.setText("x= " + pl.getOriginalX());
        textX.setVisible(true);
        textY = new javax.swing.JLabel();
        textY.setText("y= " + pl.getOriginalY());
        textY.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 10000, 1);
        ChangeListener listener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int x = ((Number) spinX.getValue()).intValue();
                int y = ((Number) spinY.getValue()).intValue();
                pl.setLocation(x, y);
                textX.setText("x= " + pl.getOriginalX());
                textY.setText("y= " + pl.getOriginalY());
            }
        };
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(Integer.valueOf(pl.getOriginalX()));
        spinX.setToolTipText("Enter x coordinate");
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));
        spinX.addChangeListener(listener);
        model = new javax.swing.SpinnerNumberModel(0, 0, 10000, 1);
        spinY = new javax.swing.JSpinner(model);
        spinY.setValue(Integer.valueOf(pl.getOriginalY()));
        spinY.setToolTipText("Enter y coordinate");
        spinY.setMaximumSize(new Dimension(
                spinY.getMaximumSize().width, spinY.getPreferredSize().height));
        spinY.addChangeListener(listener);

        getContentPane().setLayout(new GridBagLayout());

        addSpinItems(true);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int x = ((Number) spinX.getValue()).intValue();
                int y = ((Number) spinY.getValue()).intValue();
                pl.setLocation(x, y);
                textX.setText("x= " + pl.getOriginalX());
                textY.setText("y= " + pl.getOriginalY());
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.setLocation(oldX, oldY);
                dispose();
            }
        });
        pack();
    }
}
