package jmri.util.iharder.dnd;

import java.awt.datatransfer.DataFlavor;

/**
 * At last an easy way to encapsulate your custom objects for dragging and
 * dropping in your Java programs! When you need to create a
 * {@link java.awt.datatransfer.Transferable} object, use this class to wrap
 * your object. For example:
 * <pre><code>
 *      ...
 *      MyCoolClass myObj = new MyCoolClass();
 *      Transferable xfer = new TransferableObject( myObj );
 *      ...
 * </code></pre> Or if you need to know when the data was actually dropped, like
 * when you're moving data out of a list, say, you can use the
 * {@link TransferableObject.Fetcher} inner class to return your object Just in
 * Time. For example:
 * <pre><code>
 *      ...
 *      final MyCoolClass myObj = new MyCoolClass();
 *
 *      TransferableObject.Fetcher fetcher = new TransferableObject.Fetcher()
 *      {   public Object getObject(){ return myObj; }
 *      }; // end fetcher
 *
 *      Transferable xfer = new TransferableObject( fetcher );
 *      ...
 * </code></pre>
 *
 * The {@link java.awt.datatransfer.DataFlavor} associated with
 * {@link TransferableObject} has the representation class
 * {@code net.iharder.dnd.TransferableObject.class} and MIME type
 * {@code application/x-net.iharder.dnd.TransferableObject}. This data flavor
 * is accessible via the static {@link #DATA_FLAVOR} property.
 *
 * <p>
 * <em>This code is licensed for public use under the Common Public License
 * version 0.5.</em><br>
 * The Common Public License, developed by IBM and modeled after their
 * industry-friendly IBM Public License, differs from other common open source
 * licenses in several important ways:
 * <ul>
 * <li>You may include this software with other software that uses a different
 * (even non-open source) license.</li>
 * <li>You may use this software to make for-profit software.</li>
 * <li>Your patent rights, should you generate patents, are protected.</li>
 * </ul>
 * <p>
 * <em>Copyright 2001 Robert Harder</em>
 *
 * @author Robert.Harder copyright 2001
 * @version 1.1
 */
public class TransferableObject implements java.awt.datatransfer.Transferable {

    /**
     * The MIME type for {@link #DATA_FLAVOR} is
     * {@code application/x-net.iharder.dnd.TransferableObject}.
     *
     * @since 1.1
     */
    public final static String MIME_TYPE = "application/x-net.iharder.dnd.TransferableObject";

    /**
     * The default {@link java.awt.datatransfer.DataFlavor} for
     * {@link TransferableObject} has the representation class
     * {@code net.iharder.dnd.TransferableObject.class}
     * and the MIME type
     * {@code application/x-net.iharder.dnd.TransferableObject}.
     *
     * @since 1.1
     */
    public final static java.awt.datatransfer.DataFlavor DATA_FLAVOR
            = new DataFlavor(jmri.util.iharder.dnd.TransferableObject.class, MIME_TYPE);

    private Fetcher fetcher;
    private Object data;

    private java.awt.datatransfer.DataFlavor customFlavor;

    /**
     * Creates a new {@link TransferableObject} that wraps <var>data</var>.
     * Along with the {@link #DATA_FLAVOR} associated with this class, this
     * creates a custom data flavor with a representation class determined from
     * <code>data.getClass()</code> and the MIME type
     * {@code application/x-net.iharder.dnd.TransferableObject}.
     *
     * @param data The data to transfer
     * @since 1.1
     */
    public TransferableObject(Object data) {
        this.data = data;
        this.customFlavor = new java.awt.datatransfer.DataFlavor(data.getClass(), MIME_TYPE);
    }   // end constructor

    /**
     * Creates a new {@link TransferableObject} that will return the object that
     * is returned by <var>fetcher</var>. No custom data flavor is set other
     * than the default {@link #DATA_FLAVOR}.
     *
     * @see Fetcher
     * @param fetcher The {@link Fetcher} that will return the data object
     * @since 1.1
     */
    public TransferableObject(Fetcher fetcher) {
        this.fetcher = fetcher;
    }   // end constructor

    /**
     * Creates a new {@link TransferableObject} that will return the object that
     * is returned by <var>fetcher</var>. Along with the {@link #DATA_FLAVOR}
     * associated with this class, this creates a custom data flavor with a
     * representation class <var>dataClass</var>
     * and the MIME type
     * {@code application/x-net.iharder.dnd.TransferableObject}.
     *
     * @see Fetcher
     * @param dataClass The {@link java.lang.Class} to use in the custom data
     *                  flavor
     * @param fetcher   The {@link Fetcher} that will return the data object
     * @since 1.1
     */
    public TransferableObject(Class<?> dataClass, Fetcher fetcher) {
        this.fetcher = fetcher;
        this.customFlavor = new java.awt.datatransfer.DataFlavor(dataClass, MIME_TYPE);
    }   // end constructor

    /**
     * Returns the custom {@link java.awt.datatransfer.DataFlavor} associated
     * with the encapsulated object or {@code null} if the {@link Fetcher}
     * constructor was used without passing a {@link java.lang.Class}.
     *
     * @return The custom data flavor for the encapsulated object
     * @since 1.1
     */
    public java.awt.datatransfer.DataFlavor getCustomDataFlavor() {
        return customFlavor;
    }   // end getCustomDataFlavor

    /* ********  T R A N S F E R A B L E   M E T H O D S  ******** */
    /**
     * Returns a two- or three-element array containing first the custom data
     * flavor, if one was created in the constructors, second the default
     * DATA_FLAVOR associated with the TransferableObject, and third the
     * java.awt.datatransfer.DataFlavor.stringFlavor.
     *
     * @return An array of supported data flavors
     * @since 1.1
     */
    @Override
    public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
        if (customFlavor != null) {
            return new java.awt.datatransfer.DataFlavor[]{customFlavor,
                DATA_FLAVOR,
                java.awt.datatransfer.DataFlavor.stringFlavor
            };  // end flavors array
        } else {
            return new java.awt.datatransfer.DataFlavor[]{DATA_FLAVOR,
                java.awt.datatransfer.DataFlavor.stringFlavor
            };  // end flavors array
        }
    }   // end getTransferDataFlavors

    /**
     * Returns the data encapsulated in this {@link TransferableObject}. If the
     * {@link Fetcher} constructor was used, then this is when the
     * {@link Fetcher#getObject getObject()} method will be called. If the
     * requested data flavor is not supported, then the
     * {@link Fetcher#getObject getObject()} method will not be called.
     *
     * @param flavor The data flavor for the data to return
     * @return The dropped data
     * @since 1.1
     */
    @Override
    public Object getTransferData(java.awt.datatransfer.DataFlavor flavor)
            throws java.awt.datatransfer.UnsupportedFlavorException, java.io.IOException {
        // Native object
        if (flavor.equals(DATA_FLAVOR)) {
            return fetcher == null ? data : fetcher.getObject();
        }

        // String
        if (flavor.equals(java.awt.datatransfer.DataFlavor.stringFlavor)) {
            return fetcher == null ? data.toString() : fetcher.getObject().toString();
        }

        // We can't do anything else
        throw new java.awt.datatransfer.UnsupportedFlavorException(flavor);
    }   // end getTransferData

    /**
     * Returns {@code true} if <var>flavor</var> is one of the supported
     * flavors. Flavors are supported using the <code>equals(...)</code> method.
     *
     * @param flavor The data flavor to check
     * @return Whether or not the flavor is supported
     * @since 1.1
     */
    @Override
    public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor flavor) {
        // Native object
        if (flavor.equals(DATA_FLAVOR)) {
            return true;
        }

        // String
        if (flavor.equals(java.awt.datatransfer.DataFlavor.stringFlavor)) {
            return true;
        }

        // We can't do anything else
        return false;
    }   // end isDataFlavorSupported

    /* ********  I N N E R   I N T E R F A C E   F E T C H E R  ******** */
    /**
     * Instead of passing your data directly to the {@link TransferableObject}
     * constructor, you may want to know exactly when your data was received in
     * case you need to remove it from its source (or do anyting else to it).
     * When the {@link #getTransferData getTransferData(...)} method is called
     * on the {@link TransferableObject}, the {@link Fetcher}'s
     * {@link #getObject getObject()} method will be called.
     *
     * @author Robert Harder copyright 2001
     * @version 1.1
     * @since 1.1
     */
    public static interface Fetcher {

        /**
         * Return the object being encapsulated in the
         * {@link TransferableObject}.
         *
         * @return The dropped object
         * @since 1.1
         */
        public abstract Object getObject();
    }   // end inner interface Fetcher

}   // end class TransferableObject
