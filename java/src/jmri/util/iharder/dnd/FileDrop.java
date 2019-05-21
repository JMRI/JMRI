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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
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
 * To use this class, construct a new {@code FileDrop} by passing it the target
 * component and a {@code Listener} to receive notification when file(s) have
 * been dropped. Here is an example:
 * <p>
 * <code>
 *      JPanel myPanel = new JPanel();
 *      new FileDrop( myPanel, new FileDrop.Listener()
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
public class FileDrop {

    private transient Border normalBorder;
    private transient DropTargetListener dropListener;

    // Default border color
    private static Color defaultBorderColor = new Color(0f, 0f, 1f, 0.25f);

    /**
     * Constructs a {@link FileDrop} with a default light-blue border and, if
     * <var>c</var> is a {@link Container}, recursively sets all elements
     * contained within as drop targets, though only the top level container
     * will change borders.
     *
     * @param c        Component on which files will be dropped.
     * @param listener Listens for {@code filesDropped}.
     * @since 1.0
     */
    public FileDrop(
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
    public FileDrop(
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
    public FileDrop(
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
    public FileDrop(
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
    public FileDrop(
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
    public FileDrop(
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
    public FileDrop(
            final java.io.PrintStream out,
            final Component c,
            final Border dragBorder,
            final boolean recursive,
            final Listener listener) {

        dropListener = new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent evt) {
                log.debug("FileDrop: dragEnter event.");

                // Is this an acceptable drag event?
                if (isDragOk(evt)) {
                    // If it's a Swing component, set its border
                    if (c instanceof JComponent) {
                        JComponent jc = (JComponent) c;
                        normalBorder = jc.getBorder();
                        log.debug("FileDrop: normal border saved.");
                        jc.setBorder(dragBorder);
                        log.debug("FileDrop: drag border set.");
                    }

                    // Acknowledge that it's okay to enter
                    //evt.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
                    evt.acceptDrag(DnDConstants.ACTION_COPY);
                    log.debug("FileDrop: event accepted.");
                } else {   // Reject the drag event
                    evt.rejectDrag();
                    log.debug("FileDrop: event rejected.");
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent evt) {   // This is called continually as long as the mouse is
                // over the drag target.
            }

            @SuppressWarnings("unchecked")
            @Override
            public void drop(DropTargetDropEvent evt) {
                log.debug("FileDrop: drop event.");
                try {   // Get whatever was dropped
                    Transferable tr = evt.getTransferable();

                    // Is it a file list?
                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        // Say we'll take it.
                        //evt.acceptDrop ( DnDConstants.ACTION_COPY_OR_MOVE );
                        evt.acceptDrop(DnDConstants.ACTION_COPY);
                        log.debug("FileDrop: file list accepted.");

                        // Get a useful list
                        List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);

                        // Convert list to array
                        java.io.File[] filesTemp = new java.io.File[fileList.size()];
                        fileList.toArray(filesTemp);
                        final java.io.File[] files = filesTemp;

                        // Alert listener to drop.
                        if (listener != null) {
                            listener.filesDropped(files);
                        }

                        // Mark that drop is completed.
                        evt.getDropTargetContext().dropComplete(true);
                        log.debug("FileDrop: drop complete.");
                    } else // this section will check for a reader flavor.
                    {
                        DataFlavor[] flavors = tr.getTransferDataFlavors();
                        boolean handled = false;
                        for (DataFlavor flavor : flavors) {
                            if (flavor.isRepresentationClassReader()) {
                                // Say we'll take it.
                                //evt.acceptDrop ( DnDConstants.ACTION_COPY_OR_MOVE );
                                evt.acceptDrop(DnDConstants.ACTION_COPY);
                                log.debug("FileDrop: reader accepted.");
                                Reader reader = flavor.getReaderForText(tr);
                                BufferedReader br = new BufferedReader(reader);
                                if (listener != null) {
                                    listener.filesDropped(createFileArray(br));
                                }
                                // Mark that drop is completed.
                                evt.getDropTargetContext().dropComplete(true);
                                log.debug("FileDrop: drop complete.");
                                handled = true;
                                break;
                            }
                        }
                        if (!handled) {
                            log.debug("FileDrop: not a file list or reader - abort.");
                            evt.rejectDrop();
                        }
                    }
                } catch (java.io.IOException io) {
                    log.error("FileDrop: IOException - abort:", io);
                    evt.rejectDrop();
                } catch (UnsupportedFlavorException ufe) {
                    log.error("FileDrop: UnsupportedFlavorException - abort:", ufe);
                    evt.rejectDrop();
                } finally {
                    // If it's a Swing component, reset its border
                    if (c instanceof JComponent) {
                        JComponent jc = (JComponent) c;
                        jc.setBorder(normalBorder);
                        log.debug("FileDrop: normal border restored.");
                    }
                }
            }

            @Override
            public void dragExit(DropTargetEvent evt) {
                log.debug("FileDrop: dragExit event.");
                // If it's a Swing component, reset its border
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    jc.setBorder(normalBorder);
                    log.debug("FileDrop: normal border restored.");
                }

            }

            @Override
            public void dropActionChanged(DropTargetDragEvent evt) {
                log.debug("FileDrop: dropActionChanged event.");
                // Is this an acceptable drag event?
                if (isDragOk(evt)) {   //evt.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
                    evt.acceptDrag(DnDConstants.ACTION_COPY);
                    log.debug("FileDrop: event accepted.");
                } else {
                    evt.rejectDrag();
                    log.debug("FileDrop: event rejected.");
                }
            }
        };

        // Make the component (and possibly children) drop targets
        makeDropTarget(c, recursive);
    }

    private static String ZERO_CHAR_STRING = "" + (char) 0;

    private static File[] createFileArray(BufferedReader bReader) {
        try {
            java.util.List<File> list = new java.util.ArrayList<>();
            java.lang.String line = null;
            while ((line = bReader.readLine()) != null) {
                try {
                    // kde seems to append a 0 char to the end of the reader
                    if (ZERO_CHAR_STRING.equals(line)) {
                        continue;
                    }

                    java.io.File file = new java.io.File(new java.net.URI(line));
                    list.add(file);
                } catch (java.net.URISyntaxException ex) {
                    log.debug("FileDrop: URISyntaxException");
                }
            }

            return list.toArray(new File[list.size()]);
        } catch (IOException ex) {
            log.debug("FileDrop: IOException");
        }
        return new File[0];
    }

    private void makeDropTarget(final Component c, boolean recursive) {
        // Make drop target
        final DropTarget dt = new DropTarget();
        try {
            dt.addDropTargetListener(dropListener);
        } catch (java.util.TooManyListenersException e) {
            log.error("FileDrop: Drop will not work due to previous error. Do you have another listener attached?", e);
        }

        // Listen for hierarchy changes and remove the drop target when the parent gets cleared out.
        c.addHierarchyListener((HierarchyEvent evt) -> {
            log.debug("FileDrop: Hierarchy changed.");
            Component parent = c.getParent();
            if (parent == null) {
                c.setDropTarget(null);
                log.debug("FileDrop: Drop target cleared from component.");
            } else {
                new DropTarget(c, dropListener);
                log.debug("FileDrop: Drop target added to component.");
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
                log.debug("FileDrop: no data flavors.");
            }
            for (i = 0; i < flavors.length; i++) {
                log.debug(flavors[i].toString());
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
        log.debug("FileDrop: Removing drag-and-drop hooks.");
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
     * Implement this inner interface to listen for when files are dropped. For
     * example your class declaration may begin like this:
     * <pre><code>
     *      public class MyClass implements FileDrop.Listener
     *      ...
     *      public void filesDropped( java.io.File[] files )
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
         * This method is called when files have been successfully dropped.
         *
         * @param files An array of {@code File}s that were dropped.
         * @since 1.0
         */
        public abstract void filesDropped(java.io.File[] files);
    }

    private final static Logger log = LoggerFactory.getLogger(FileDrop.class);
}
