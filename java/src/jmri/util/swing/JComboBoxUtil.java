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
     * this will create a temporary Object and cast it to the
     * contents' type.  This can fail at runtime with a cast-class
     * exception, which will be logged.  In that case, the choices are:
     * <ul>
     * <li>Make sure there's at least one item in the JComboBox before invoking this
     * <li>Rewrite this to take a sample (i.e. temporary) object
     * <li>Rewrite this to take a {@link java.util.function.Supplier} or similar to create the sample object if needed
     * <li>Do some zero-argument ctor magic...
     * <li>...
     * </ul>
     *
     * @param <E>        type of JComboBox contents
     * @param <T>        subclass of JComboBox being setup
     * @param inComboBox the JComboBox to setup
     */
    public static <E extends Object, T extends JComboBox<E>> void setupComboBoxMaxRows(T inComboBox) {
        boolean isDummy = false;

        if (inComboBox.getItemCount() == 0 || (inComboBox.getItemCount() == 1 && "".equals(inComboBox.getItemAt(0)))) {
            // Add a temporary row to insure the proper cell height
            //inComboBox.insertItemAt((E) makeObj("XYZxyz"), 0);
            insertDummy(inComboBox);
            isDummy = true;
        }

        ListModel<E> lm = inComboBox.getModel();
        JList<E> list = new JList<>(lm);
        int maxItemHeight = 12; // pick some absolute minimum here

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
        if (isDummy) {
            // remove the dummy element
            inComboBox.removeItemAt(0);
        }
        inComboBox.setMaximumRowCount(c);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Object, T extends JComboBox<E>> void insertDummy(T inComboBox) {
        try {
            inComboBox.insertItemAt((E) new Object() {
                @Override
                public String toString() { return "XYZxyz"; }
            }, 0);
        } catch (ClassCastException ex) {
            log.error("Could not handle cast of dummy element", ex);
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JComboBoxUtil.class);
}
