package jmri.util.swing;

/**
 * Wrap an object for easier null handling in a JComboBox. ("JCB" refers to
 * JComboBox)
 *
 * Define a {@code JComboBox<JCBHandle<Foo>>}, then fill it with a new
 * {@code JCBHandle("None string")} and your new {@code JCBHandle(foo)} entries.
 *
 * @param <T> the class accepted by the JComboBox
 */
public class JCBHandle<T> {

    T item = null;
    String label;

    /**
     * Create a handle with a handled object.
     *
     * @param t the class accepted by the JComboBox
     */
    public JCBHandle(T t) {
        item = t;
    }

    /**
     * Create a handle without a handled object, just a display label.
     *
     * @param l label for handle
     */
    public JCBHandle(String l) {
        label = l;
    }

    /**
     * Display the handled item, or if there isn't one, the null-case label.
     *
     * @return the item's String representation or the default label
     */
    @Override
    public String toString() {
        if (item != null) {
            return item.toString();
        } else {
            return label;
        }
    }

    /**
     * Retrieve the handled object for this handle
     * @return the object
     */
    public T item() {
        return item;
    }

}
