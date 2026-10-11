package jmri.util.table;

import java.awt.Component;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import jmri.NamedBean;
import jmri.swing.NamedBeanComboBox;
import jmri.util.SystemType;

/**
 * NamedBeanBoxRenderer renders a NamedBeanComboBox within a JTable Cell.
 * @author Steve
 * @param <T> a Bean which extends from NamedBean, e.g. Sensor or Turnout.
 */
public class NamedBeanBoxRenderer<T extends NamedBean> extends NamedBeanComboBox<T> implements TableCellRenderer {

    private final Border normalBorder;
    private final Border errorBorder = BorderFactory.createLineBorder(java.awt.Color.RED);

    public NamedBeanBoxRenderer(jmri.Manager<T> mgr) {
        super(mgr);
        normalBorder = getBorder();
        setAllowNull(true);
    }

    public void setNormalBorder() {
        setBorder(normalBorder);
    }

    public void setErrorBorder() {
        setBorder(errorBorder);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        if (!(SystemType.isMacOSX() && UIManager.getLookAndFeel().isNativeLookAndFeel())) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
        }
        setSelectedItem(value instanceof NamedBean ? ((NamedBean) value) : null);
        return this;
    }

}
