package jmri.util.swing;

/**
 * Wrap an object for easier null handling in a JComboBox. ("JCB" refers to
 * JComboBox)
 *
 * Define a {@code JComboBox<JCBHandle<Foo>>}, then fill it with a new
 * {@code JCBHandle("None string")} and your new {@code JCBHandle(foo)} entries.
 */
public class JCBHandle<T> {

    T item = null;
    String label;

    /**
     * Create a handle with a handled object.
     */
    public JCBHandle(T t) {
        item = t;
    }

    /**
     * Create a handle without a handled object, just a display label.
     */
    public JCBHandle(String l) {
        label = l;
    }

    /**
     * Display the handled item, or if there isn't one, the null-case label.
     */
    public String toString() {
        if (item != null) {
            return item.toString();
        } else {
            return label;
        }
    }

    /**
     * Retrieve the handled object for this handle
     */
    public T item() {
        return item;
    }

}
