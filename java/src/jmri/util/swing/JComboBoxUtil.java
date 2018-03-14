package jmri.util.swing;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

/**
 * Common utility methods for working with JComboBoxes.
 * <P>
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @since 4.9.5
 */
public class JComboBoxUtil {

    /**
     * Set the maximum number of rows for a JComboBox so that it always can fit
     * on the screen
     *
     * @param <E>        type of JComboBox contents
     * @param <T>        subclass of JComboBox being setup
     * @param inComboBox the JComboBox to setup
     */
    public static <E extends Object, T extends JComboBox<E>> void setupComboBoxMaxRows(T inComboBox) {
        ListModel<E> lm = inComboBox.getModel();
        JList<E> list = new JList<>(lm);
        ListCellRenderer renderer = list.getCellRenderer();
        int maxItemHeight = 12; // pick some absolute minimum here
        for (int i = 0; i < lm.getSize(); ++i) {
            E value = lm.getElementAt(i);
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
