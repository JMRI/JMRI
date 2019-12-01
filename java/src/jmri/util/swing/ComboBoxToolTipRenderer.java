package jmri.util.swing;

import java.awt.Component;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

/**
 * Set ToolTips for ComboBox items
 *
 * Steve Young (c) 2019
 * 
 */
public class ComboBoxToolTipRenderer extends DefaultListCellRenderer {
    List<String> tooltips;

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {

        JComponent comp = (JComponent) super.getListCellRendererComponent(list,
            value, index, isSelected, cellHasFocus);

        if (-1 < index && null != value && null != tooltips) {
            list.setToolTipText(tooltips.get(index));
        }
        return comp;
    }
    
    /**
     * Set ToolTips for the ComboBox
     *
     * @param tooltips a List of Strings to be used for the Tooltips 
     */
    public void setTooltips(List<String> tooltips) {
        this.tooltips = tooltips;
    }
}
