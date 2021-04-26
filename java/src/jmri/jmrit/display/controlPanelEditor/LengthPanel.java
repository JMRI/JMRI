package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
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
    private final OBlock _block;
    private float _length;
    private String _label ;
    private final JTextField _lengthField;
    private final JToggleButton _units;
    private static final java.text.DecimalFormat _lenFormat = new java.text.DecimalFormat("###,##0.0");
    
    public static final String PATH_LENGTH    = "pathLength" ;
    public static final String BLOCK_LENGTH   = "blockLength" ;
    public static final String ENTRANCE_SPACE = "entranceSpace" ;
    

    LengthPanel(OBlock block, String label, String tip) {
        _block = block;
        _label = label ;
        
        JPanel pp = new JPanel();
        _lengthField = new JTextField();

        _lengthField.setText(_lenFormat.format(0f));
        pp.add(CircuitBuilder.makeTextBoxPanel(
                false, _lengthField, _label, true, tip));
        _lengthField.setPreferredSize(new Dimension(100, _lengthField.getPreferredSize().height));
        _units = new JToggleButton("", !_block.isMetric());
        _units.setToolTipText(Bundle.getMessage("TooltipPathUnitButton"));
        _units.addActionListener((ActionEvent event) -> changeUnits());
        pp.add(_units);
        add(pp);
    }

    protected void changeUnits() {
        String len = _lengthField.getText();
        if (len == null || len.length() == 0) {
            if (_block != null && _block.isMetric()) {
                _units.setText("cm");
            } else {
                _units.setText("in");
            }
            return;
        }
        try {
            float f = Float.parseFloat(len.replace(',', '.'));
            if (_units.isSelected()) {
                _lengthField.setText(_lenFormat.format(f / 2.54f));
                _units.setText("in");
            } else {
                _lengthField.setText(_lenFormat.format(f * 2.54f));
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
            _lengthField.setText(_lenFormat.format(len / 25.4f));
        } else {
            _lengthField.setText(_lenFormat.format(len / 10));
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
            f = Float.parseFloat(num.replace(',', '.'));
        } catch (NumberFormatException nfe) {
        }
        
        if (_label.equals(PATH_LENGTH) || _label.equals(BLOCK_LENGTH)) {
            if (f < 0.0f) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("MustBeFloat", num),
                        Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (_units.isSelected()) {
                    _length = f * 25.4f;
                } else {
                    _length = f * 10f;
                }
            }            
        } else {
            // ENTRANCE_SPACE
                if (_units.isSelected()) {
                    _length = f * 25.4f;
                } else {
                    _length = f * 10f;
                }            
        }
        return _length;
    }

    protected boolean isChanged(float len) {
        return Math.abs(getLength() - len) > .5;
    }

}
