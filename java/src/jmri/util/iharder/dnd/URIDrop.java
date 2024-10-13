package jmri.util.iharder.dnd;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class makes it easy to drag and drop files from the operating system to
 * a Java program. Any {@code Component} can be dropped onto, but only
 * {@code JComponent}s will indicate the drop event with a changed border.
 * <p>
 * To use this class, construct a new {@code URIDrop} by passing it the target
 * component and a {@code Listener} to receive notification when file(s) have
 * been dropped. Here is an example:
 * <p>
 * <code>
 *      JPanel myPanel = new JPanel();
 *      new URIDrop( myPanel, new URIDrop.Listener()
 *      {   public void filesDropped( java.io.File[] files )
 *          {
 *              // handle file drop
 *              ...
 *          }
 *      });
 * </code>
 * <p>
 * You can specify the border that will appear when files are being dragged by
 * calling the constructor with a {@code border.Border}. Only
 * {@code JComponent}s will show any indication with a border.
 * <p>
 * You can turn on some debugging features by passing a {@code PrintStream}
 * object (such as {@code System.out}) into the full constructor. A {@code null}
 * value will result in no extra debugging information being output.
 *
 * @author Robert Harder rharder@users.sf.net
 * @author Nathan Blomquist
 * @version 1.0.1
 */
public class URIDrop {

    private transient Border normalBorder;
    private transient DropTargetListener dropListener;

    // Default border color
    private static Color defaultBorderColor = new Color(0f, 0f, 1f, 0.25f);

    /**
     * Constructs a {@link URIDrop} with a default light-blue border and, if
     * <var>c</var> is a {@link Container}, recursively sets all elements
     * contained within as drop targets, though only the top level container
     * will change borders.
     *
     * @param c        Component on which files will be dropped.
     * @param listener Listens for {@code filesDropped}.
     * @since 1.0
     */
    public URIDrop(
            final Component c,
            final Listener listener) {
        this(null, // Logging stream
                c, // Drop target
                BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor), // Drag border
                true, // Recursive
                listener);
    }

    /**
     * Constructor with a default border and the option to recursively set drop
     * targets. If your component is a {@code Container}, then each of its
     * children components will also listen for drops, though only the parent
     * will change borders.
     *
     * @param c         Component on which files will be dropped.
     * @param recursive Recursively set children as drop targets.
     * @param listener  Listens for {@code filesDropped}.
     * @since 1.0
     */
    public URIDrop(
            final Component c,
            final boolean recursive,
            final Listener listener) {
        this(null, // Logging stream
                c, // Drop target
                BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor), // Drag border
                recursive, // Recursive
                listener);
    }

    /**
     * Constructor with a default border, debugging optionally turned on and the
     * option to recursively set drop targets. If your component is a
     * {@code Container}, then each of its children components will also listen
     * for drops, though only the parent will change borders. With Debugging
     * turned on, more status messages will be displayed to {@code out}. A
     * common way to use this constructor is with {@code System.out} or
     * {@code System.err}. A {@code null} value for the parameter {@code out}
     * will result in no debugging output.
     *
     * @param out       PrintStream to record debugging info or null for no
     *                  debugging.
     * @param c         Component on which files will be dropped.
     * @param recursive Recursively set children as drop targets.
     * @param listener  Listens for {@code filesDropped}.
     * @since 1.0
     */
    public URIDrop(
            final java.io.PrintStream out,
            final Component c,
            final boolean recursive,
            final Listener listener) {
        this(out, // Logging stream
                c, // Drop target
                BorderFactory.createMatteBorder(2, 2, 2, 2, defaultBorderColor), // Drag border
                recursive, // Recursive
                listener);
    }

    /**
     * Constructor with a specified border
     *
     * @param c          Component on which files will be dropped.
     * @param dragBorder Border to use on {@code JComponent} when dragging
     *                   occurs.
     * @param listener   Listens for {@code filesDropped}.
     * @since 1.0
     */
    public URIDrop(
            final Component c,
            final Border dragBorder,
            final Listener listener) {
        this(
                null, // Logging stream
                c, // Drop target
                dragBorder, // Drag border
                false, // Recursive
                listener);
    }

    /**
     * Constructor with a specified border and the option to recursively set
     * drop targets. If your component is a {@code Container}, then each of its
     * children components will also listen for drops, though only the parent
     * will change borders.
     *
     * @param c          Component on which files will be dropped.
     * @param dragBorder Border to use on {@code JComponent} when dragging
     *                   occurs.
     * @param recursive  Recursively set children as drop targets.
     * @param listener   Listens for {@code filesDropped}.
     * @since 1.0
     */
    public URIDrop(
            final Component c,
            final Border dragBorder,
            final boolean recursive,
            final Listener listener) {
        this(
                null,
                c,
                dragBorder,
                recursive,
                listener);
    }

    /**
     * Constructor with a specified border and debugging optionally turned on.
     * With Debugging turned on, more status messages will be displayed to
     * {@code out}. A common way to use this constructor is with
     * {@code System.out} or {@code System.err}. A {@code null} value for the
     * parameter {@code out} will result in no debugging output.
     *
     * @param out        PrintStream to record debugging info or null for no
     *                   debugging.
     * @param c          Component on which files will be dropped.
     * @param dragBorder Border to use on {@code JComponent} when dragging
     *                   occurs.
     * @param listener   Listens for {@code filesDropped}.
     * @since 1.0
     */
    public URIDrop(
            final java.io.PrintStream out,
            final Component c,
            final Border dragBorder,
            final Listener listener) {
        this(
                out, // Logging stream
                c, // Drop target
                dragBorder, // Drag border
                false, // Recursive
                listener);
    }

    /**
     * Full constructor with a specified border and debugging optionally turned
     * on. With Debugging turned on, more status messages will be displayed to
     * {@code out}. A common way to use this constructor is with
     * {@code System.out} or {@code System.err}. A {@code null} value for the
     * parameter {@code out} will result in no debugging output.
     *
     * @param out        PrintStream to record debugging info or null for no
     *                   debugging.
     * @param c          Component on which files will be dropped.
     * @param dragBorder Border to use on {@code JComponent} when dragging
     *                   occurs.
     * @param recursive  Recursively set children as drop targets.
     * @param listener   Listens for {@code filesDropped}.
     * @since 1.0
     */
    public URIDrop(
            final java.io.PrintStream out,
            final Component c,
            final Border dragBorder,
            final boolean recursive,
            final Listener listener) {

        dropListener = new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent evt) {
                log.debug("URIDrop: dragEnter event.");

                // Is this an acceptable drag event?
                if (isDragOk(evt)) {
                    // If it's a Swing component, set its border
                    if (c instanceof JComponent) {
                        JComponent jc = (JComponent) c;
                        normalBorder = jc.getBorder();
                        log.debug("URIDrop: normal border saved.");
                        jc.setBorder(dragBorder);
                        log.debug("URIDrop: drag border set.");
                    }

                    // Acknowledge that it's okay to enter
                    //evt.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
                    evt.acceptDrag(DnDConstants.ACTION_COPY);
                    log.debug("URIDrop: event accepted.");
                } else {   // Reject the drag event
                    evt.rejectDrag();
                    log.debug("URIDrop: event rejected.");
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent evt) {   // This is called continually as long as the mouse is
                // over the drag target.
            }

            @SuppressWarnings("unchecked")
            @Override
            public void drop(DropTargetDropEvent evt) {
                log.debug("URIDrop: drop event.");
                try {   // Get whatever was dropped
                    Transferable tr = evt.getTransferable();
                    boolean handled = false;
                    // Is it a raw image?
                    if (!handled && tr.isDataFlavorSupported(DataFlavor.imageFlavor) && listener != null && listener instanceof ListenerExt) {
                        // Say we'll take it.
                        evt.acceptDrop(DnDConstants.ACTION_COPY);
                        log.debug("HTMLDrop: raw image accepted.");
                        BufferedImage img = (BufferedImage) tr.getTransferData(DataFlavor.imageFlavor);
                        ((ListenerExt)listener).imageDropped(img);
                        // Mark that drop is completed.
                        evt.getDropTargetContext().dropComplete(true);
                        handled = true;
                        log.debug("ImageDrop: drop complete as image.");                           
                    }
                    // Is it a file path list ?
                    if (!handled && tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        // Say we'll take it.
                        evt.acceptDrop(DnDConstants.ACTION_COPY);
                        log.debug("FileDrop: file list accepted.");
                        // Get a useful list
                        List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                        // Alert listener to drop.
                        if (listener != null) {
                            listener.URIsDropped(createURIArray(fileList));
                        }
                        // Mark that drop is completed.
                        evt.getDropTargetContext().dropComplete(true);
                        handled = true;
                        log.debug("FileDrop: drop complete as files.");
                    }
                    // Is it a string?
                    if (!handled && tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        // Say we'll take it.
                        evt.acceptDrop(DnDConstants.ACTION_COPY);
                        log.debug("URIDrop: string accepted.");
                        // Get a useful list
                        String uristr = (String) tr.getTransferData(DataFlavor.stringFlavor);
                        // Alert listener to drop.
                        if (listener != null) {
                            listener.URIsDropped(createURIArray(uristr));
                        }
                        // Mark that drop is completed.
                        evt.getDropTargetContext().dropComplete(true);
                        handled = true;
                        log.debug("URIDrop: drop complete as URIs.");
                    }
                    // this section will check for a reader flavor.
                    if (!handled) {
                        DataFlavor[] flavors = tr.getTransferDataFlavors();
                        for (DataFlavor flavor : flavors) {
                            if (flavor.isRepresentationClassReader()) {
                                // Say we'll take it.
                                evt.acceptDrop(DnDConstants.ACTION_COPY);
                                log.debug("URIDrop: reader accepted.");
                                Reader reader = flavor.getReaderForText(tr);
                                BufferedReader br = new BufferedReader(reader);
                                if (listener != null) {
                                    listener.URIsDropped(createURIArray(br));
                                }
                                // Mark that drop is completed.
                                evt.getDropTargetContext().dropComplete(true);
                                log.debug("URIDrop: drop complete as {}",flavor.getHumanPresentableName());
                                handled = true;
                                break;
                            }
                        }
                    }
                    if (!handled) {
                        log.debug("URIDrop: not droppable.");
                        evt.rejectDrop();
                    }
                } catch (java.io.IOException io) {
                    log.error("URIDrop: IOException - abort:", io);
                    evt.rejectDrop();
                } catch (UnsupportedFlavorException ufe) {
                    log.error("URIDrop: UnsupportedFlavorException - abort:", ufe);
                    evt.rejectDrop();
                } finally {
                    // If it's a Swing component, reset its border
                    if (c instanceof JComponent) {
                        JComponent jc = (JComponent) c;
                        jc.setBorder(normalBorder);
                        log.debug("URIDrop: normal border restored.");
                    }
                }
            }

            @Override
            public void dragExit(DropTargetEvent evt) {
                log.debug("URIDrop: dragExit event.");
                // If it's a Swing component, reset its border
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    jc.setBorder(normalBorder);
                    log.debug("URIDrop: normal border restored.");
                }

            }

            @Override
            public void dropActionChanged(DropTargetDragEvent evt) {
                log.debug("URIDrop: dropActionChanged event.");
                // Is this an acceptable drag event?
                if (isDragOk(evt)) {   //evt.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
                    evt.acceptDrag(DnDConstants.ACTION_COPY);
                    log.debug("URIDrop: event accepted.");
                } else {
                    evt.rejectDrag();
                    log.debug("URIDrop: event rejected.");
                }
            }
        };

        // Make the component (and possibly children) drop targets
        makeDropTarget(c, recursive);
    }

    private static String ZERO_CHAR_STRING = "" + (char) 0;

    private static java.net.URI[] createURIArray(BufferedReader bReader) {
        try {
            java.util.List<java.net.URI> list = new java.util.ArrayList<>();
            java.lang.String line;
            while ((line = bReader.readLine()) != null) {
                try {
                    // kde seems to append a 0 char to the end of the reader
                    if (ZERO_CHAR_STRING.equals(line)) {
                        continue;
                    }

                    java.net.URI uri = new java.net.URI(line);
                    list.add(uri);
                } catch (java.net.URISyntaxException ex) {
                    log.error("URIDrop: URISyntaxException");
                    log.debug("URIDrop: line for URI : {}",line);
                }
            }

            return list.toArray(new java.net.URI[list.size()]);
        } catch (IOException ex) {
            log.debug("URIDrop: IOException");
        }
        return new java.net.URI[0];
    }

    private static java.net.URI[] createURIArray(String str) {
        java.util.List<java.net.URI> list = new java.util.ArrayList<>();
        String lines[] = str.split("(\\r|\\n)");
        for (String line : lines) {
            // kde seems to append a 0 char to the end of the reader
            if (ZERO_CHAR_STRING.equals(line)) {
                continue;
            }
            try {
                java.net.URI uri = new java.net.URI(line);
                list.add(uri);
            }catch (java.net.URISyntaxException ex) {
                log.error("URIDrop: URISyntaxException");
            }
        }
        return list.toArray(new java.net.URI[list.size()]);
    }

    private static java.net.URI[] createURIArray(List<File> fileList) {
        java.util.List<java.net.URI> list = new java.util.ArrayList<>();
        fileList.forEach((f) -> {
            list.add(f.toURI());
        });
        return list.toArray(new java.net.URI[list.size()]);
    }

    private void makeDropTarget(final Component c, boolean recursive) {
        // Make drop target
        final DropTarget dt = new DropTarget();
        try {
            dt.addDropTargetListener(dropListener);
        } catch (java.util.TooManyListenersException e) {
            log.error("URIDrop: Drop will not work due to previous error. Do you have another listener attached?", e);
        }

        // Listen for hierarchy changes and remove the drop target when the parent gets cleared out.
        c.addHierarchyListener((HierarchyEvent evt) -> {
            log.debug("URIDrop: Hierarchy changed.");
            Component parent = c.getParent();
            if (parent == null) {
                c.setDropTarget(null);
                log.debug("URIDrop: Drop target cleared from component.");
            } else {
                new DropTarget(c, dropListener);
                log.debug("URIDrop: Drop target added to component.");
            }
        });
        if (c.getParent() != null) {
            new DropTarget(c, dropListener);
        }

        if (recursive && (c instanceof Container)) {
            // Get the container
            Container cont = (Container) c;

            // Get its components
            Component[] comps = cont.getComponents();

            // Set its components as listeners also
            for (Component comp : comps) {
                makeDropTarget(comp, recursive);
            }
        }
    }

    /**
     * Determine if the dragged data is a file list.
     */
    private boolean isDragOk(final DropTargetDragEvent evt) {
        boolean ok = false;

        // Get data flavors being dragged
        DataFlavor[] flavors = evt.getCurrentDataFlavors();

        // See if any of the flavors are a file list
        int i = 0;
        while (!ok && i < flavors.length) {
            // Is the flavor a file list?
            final DataFlavor curFlavor = flavors[i];
            if (curFlavor.equals(DataFlavor.javaFileListFlavor)
                    || curFlavor.isRepresentationClassReader()) {
                ok = true;
            }
            i++;
        }

        // If logging is enabled, show data flavors
        if (log.isDebugEnabled()) {
            if (flavors.length == 0) {
                log.debug("URIDrop: no data flavors.");
            }
            for (i = 0; i < flavors.length; i++) {
                log.debug("flavor {} {}", i, flavors[i].toString());
            }
        }

        return ok;
    }

    /**
     * Removes the drag-and-drop hooks from the component and optionally from
     * the all children. You should call this if you add and remove components
     * after you've set up the drag-and-drop. This will recursively unregister
     * all components contained within
     * <var>c</var> if <var>c</var> is a {@link Container}.
     *
     * @param c The component to unregister as a drop target
     * @return true if any components were unregistered
     * @since 1.0
     */
    public static boolean remove(Component c) {
        return remove(c, true);
    }

    /**
     * Removes the drag-and-drop hooks from the component and optionally from
     * the all children. You should call this if you add and remove components
     * after you've set up the drag-and-drop.
     *
     * @param c         The component to unregister
     * @param recursive Recursively unregister components within a container
     * @return true if any components were unregistered
     * @since 1.0
     */
    public static boolean remove(Component c, boolean recursive) {
        log.debug("URIDrop: Removing drag-and-drop hooks.");
        c.setDropTarget(null);
        if (recursive && (c instanceof Container)) {
            Component[] comps = ((Container) c).getComponents();
            for (Component comp : comps) {
                remove(comp, recursive);
            }
            return true;
        } else {
            return false;
        }
    }

    /* ********  I N N E R   I N T E R F A C E   L I S T E N E R  ******** */
    /**
     * Implement this inner interface to listen for when uris are dropped. For
     * example your class declaration may begin like this:
     * <pre><code>
     *      public class MyClass implements URIsDrop.Listener
     *      ...
     *      public void URIsDropped( java.io.URI[] files )
     *      {
     *          ...
     *      }
     *      ...
     * </code></pre>
     *
     * @since 1.0
     */
    public interface Listener {

        /**
         * This method is called when uris have been successfully dropped.
         *
         * @param uris An array of {@code URI}s that were dropped.
         * @since 1.0
         */
        public abstract void URIsDropped(java.net.URI[] uris);
    }

    public interface ListenerExt extends Listener{        
        /**
         * This method is called when an image has been successfully dropped.
         *
         * @param image The BufferedImage that has been dropped
         * @since 1.0
         */
        public void imageDropped(BufferedImage image);
    }            
        
    
    private final static Logger log = LoggerFactory.getLogger(URIDrop.class);
}
