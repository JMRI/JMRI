package jmri.util.swing;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * Common utility methods for working with JComboBoxes.
 * <p>
 * To do vertical sizing of <u>empty</u> JComboBoxen,
 * this will create a dummy object and cast it to the
 * contents' type.  This can fail.
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @since 4.9.5
 */
public class JComboBoxUtil {

    /**
     * Set the maximum number of rows for a JComboBox so that it always can fit
     * on the screen
     * <p>
     * To do vertical sizing of <u>empty</u> JComboBoxen,
     * this will create a temporary String and use that to set a default height based
     * on the current font settings.
     *
     * @param <E>        type of JComboBox contents
     * @param <T>        subclass of JComboBox being setup
     * @param inComboBox the JComboBox to setup
     */
    public static <E extends Object, T extends JComboBox<E>> void setupComboBoxMaxRows(T inComboBox) {
        int maxItemHeight = 12; // pick some absolute minimum here

        if (inComboBox.getItemCount() == 0 || (inComboBox.getItemCount() == 1 && "".equals(inComboBox.getItemAt(0)))) {
            maxItemHeight = getDefaultRowHeight(maxItemHeight);
        }

        ListModel<E> lm = inComboBox.getModel();
        JList<E> list = new JList<>(lm);

        for (int i = 0; i < lm.getSize(); ++i) {
            E value = lm.getElementAt(i);
            Component c = list.getCellRenderer().getListCellRendererComponent(list, value, i, false, false);
            maxItemHeight = Math.max(maxItemHeight, c.getPreferredSize().height);
        }
        // Compensate for slightly undersized cell height for macOS
        // The last rows will be off the screen if the dock is hidden
        if (jmri.util.SystemType.isMacOSX()) maxItemHeight++;

        int itemsPerScreen = inComboBox.getItemCount();
        // calculate the number of items that will fit on the screen
        if (!GraphicsEnvironment.isHeadless()) {
            // note: this line returns the maximum available size, accounting all
            // taskbars etc. no matter where they are aligned:
            Rectangle maxWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            itemsPerScreen = (int) maxWindowBounds.getHeight() / maxItemHeight;
        }

        int c = Math.max(itemsPerScreen - 1, 8);

        // Adjust max rows if the Preferences => Display setting is greater than zero.
        int maxRows = jmri.InstanceManager.getDefault(jmri.util.gui.GuiLafPreferencesManager.class).getMaxComboRows();
        if (maxRows > 0) {
            c = Math.min(c, maxRows);
        }

        inComboBox.setMaximumRowCount(c);
    }

    /**
     * Set a default row height if the related combo box is empty.
     * @param minimumHeight The default minimum height.
     * @return a new height based on sample text.
     */
    private static int getDefaultRowHeight(int minimumHeight) {
        String[] data = {"XYZxyz"};
        JList<String> list = new JList<>(data);
        String value = list.getModel().getElementAt(0);
        Component c = list.getCellRenderer().getListCellRendererComponent(list, value, 0, false, false);
        return Math.max(minimumHeight, c.getPreferredSize().height);
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JComboBoxUtil.class);
}
