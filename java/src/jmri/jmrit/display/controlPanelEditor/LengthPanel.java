package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import jmri.jmrit.logix.OBlock;

/**
 * A simple panel to collect lengths with units bring either inches or centimeters
 * 
 * @author Pete Cressman Copyright: Copyright (c) 2019
 *
 */

public class LengthPanel extends JPanel
{
    private OBlock _block;
    private float _length;
    private JTextField _lengthField;
    private boolean _lengthKeyedIn = false;
    private JToggleButton _units;

    LengthPanel(OBlock block, String label) {
        _block = block;

        JPanel pp = new JPanel();
        _lengthField = new JTextField();
        _lengthField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                _lengthKeyedIn = true;
            }
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
            }
          });

        _lengthField.setText("0.0");
        pp.add(CircuitBuilder.makeTextBoxPanel(
                false, _lengthField, label, true, "TooltipPathLength"));
        _lengthField.setPreferredSize(new Dimension(100, _lengthField.getPreferredSize().height));
        _units = new JToggleButton("", !_block.isMetric());
        _units.setToolTipText(Bundle.getMessage("TooltipPathUnitButton"));
        _units.addActionListener((ActionEvent event) -> {
            changeUnits();
        });
        pp.add(_units);
        add(pp);
    }

    protected void changeUnits() {
        String len = _lengthField.getText();
        if (len == null || len.length() == 0) {
            if (_block.isMetric()) {
                _units.setText("cm");
            } else {
                _units.setText("in");
            }
            return;
        }
        try {
            float f = Float.parseFloat(len);
            if (_units.isSelected()) {
                _lengthField.setText(Float.toString(f / 2.54f));
                _units.setText("in");
            } else {
                _lengthField.setText(Float.toString(f * 2.54f));
                _units.setText("cm");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustBeFloat", len),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Display 
     * @param len length in millimeters
     */
    protected void setLength(float len) {
        _length = len;
        if (_units.isSelected()) {
            _lengthField.setText(Float.toString(len / 25.4f));
        } else {
            _lengthField.setText(Float.toString(len / 10));
        }
    }

    protected float getLength() {
        String num = null;
        float f = -1;
        try {
            num = _lengthField.getText();
            if (num == null || num.length() == 0) {
                num = "0.0";
            }
            f = Float.parseFloat(num);
        } catch (NumberFormatException nfe) {
        }
        if (f < 0.0f) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustBeFloat", num),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            _lengthKeyedIn = false;
            if (_units.isSelected()) {
                _length = f * 25.4f;
            } else {
                _length = f * 10f;
            }
        }
        return _length;
    }
    protected boolean isChanged() {
        return _lengthKeyedIn;
    }

    protected void setChanged(boolean set) {
        _lengthKeyedIn = set;
    }
}
