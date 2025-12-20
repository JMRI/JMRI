package jmri.util.table;

import jmri.swing.NamedBeanComboBox;

/**
 * Basic cell editor for JComboBox with optional callback.
 * @author Steve Young Copyright (c) 2024
 */
public class JComboBoxEditor extends javax.swing.DefaultCellEditor {

    private final transient Runnable onChangeCallback;

    public JComboBoxEditor(javax.swing.JComboBox<?> comboBox, @javax.annotation.CheckForNull final Runnable callback) {
        super(comboBox);
        onChangeCallback = callback;
        jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(comboBox);
        if ( comboBox instanceof NamedBeanComboBox<?> ) {
            ((NamedBeanComboBox<?>)comboBox).setAllowNull(true);
        }
    }

    @Override
    protected void fireEditingStopped() {
        if ( onChangeCallback != null ) {
            onChangeCallback.run();
        }
        super.fireEditingStopped();
    }

}
