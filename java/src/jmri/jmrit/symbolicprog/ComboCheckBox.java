package jmri.jmrit.symbolicprog;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Represents a JComboBox as a JCheckBox
 *
 * @author   Bob Jacobsen   Copyright (C) 2001
 */
public class ComboCheckBox extends JCheckBox {

    ComboCheckBox(JComboBox<String> box, EnumVariableValue var) {
        super();
        _var = var;
        _box = box;
        setBackground(_var._value.getBackground());
        setOpaque(true);
        // listen for changes to ourself
        addActionListener(l1 = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                thisActionPerformed(e);
            }
        });
        // listen for changes to original
        _box.addActionListener(l2 = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                originalActionPerformed(e);
            }
        });
        // listen for changes to original state
        _var.addPropertyChangeListener(p1 = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                originalPropertyChanged(e);
            }
        });

        // set initial value
        if (_box.getSelectedIndex() == 1) {
            setSelected(true);
        }
    }

    void thisActionPerformed(java.awt.event.ActionEvent e) {
        // update original state to this state
        if (isSelected()) {
            _box.setSelectedIndex(1);
        } else {
            _box.setSelectedIndex(0);
        }
    }

    void originalActionPerformed(java.awt.event.ActionEvent e) {
        // update this state to original state
        if (_box.getSelectedIndex() == 1) {
            setSelected(true);
        } else {
            setSelected(false);
        }
    }

    void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
        // update this color from original state
        if (e.getPropertyName().equals("State")) {
            if (log.isDebugEnabled()) {
                log.debug("State change seen");
            }
            setBackground(_var._value.getBackground());
            setOpaque(true);
        }
    }

    transient ActionListener l1;
    transient ActionListener l2;
    transient PropertyChangeListener p1;

    EnumVariableValue _var = null;
    JComboBox<String> _box = null;

    public void dispose() {
        removeActionListener(l1);
        _box.removeActionListener(l2);
        _var.removePropertyChangeListener(p1);
        _var = null;
        _box = null;
    }

    // initialize logging 
    private final static Logger log = LoggerFactory.getLogger(ComboCheckBox.class);

}
