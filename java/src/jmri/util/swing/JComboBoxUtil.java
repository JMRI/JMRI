package jmri.util.swing;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;

/**
 * Common utility methods for working with JComboBoxes.
 * <P>
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @since 4.9.5
 */
public class JComboBoxUtil {

    /**
     * Set the maximum number of rows for a JComboBox so that
     * it always can fit on the screen
     */
    public static <T extends javax.swing.JComboBox> void setupComboBoxMaxRows(T inComboBox) {
        // find the max height of all popup items
        BasicComboPopup popup = (BasicComboPopup) inComboBox.getAccessibleContext().getAccessibleChild(0);
        JList list = popup.getList();
        ListModel lm = list.getModel();
        ListCellRenderer renderer = list.getCellRenderer();
        int maxItemHeight = 12; // pick some absolute minimum here
        for (int i = 0; i < lm.getSize(); ++i) {
            Object value = lm.getElementAt(i);
            Component c = renderer.getListCellRendererComponent(list, value, i, false, false);
            maxItemHeight = Math.max(maxItemHeight, c.getPreferredSize().height);
        }

        int itemsPerScreen = inComboBox.getItemCount();
        // calculate the number of items that will fit on the screen
        if (!GraphicsEnvironment.isHeadless()) {
            // note: this line returns the maximum available size, accounting all
            // taskbars etc. no matter where they are aligned:
            Rectangle maxWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            itemsPerScreen = (int) maxWindowBounds.getHeight() / maxItemHeight;
        }

        int c = Math.min(itemsPerScreen, inComboBox.getItemCount());
        inComboBox.setMaximumRowCount(c);
    }


}
