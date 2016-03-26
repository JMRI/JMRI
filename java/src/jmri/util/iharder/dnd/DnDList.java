package jmri.util.iharder.dnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of {@link javax.swing.JList} that supports drag and drop to
 * rearrange its contents and to move objects in and out of the list. The
 * objects in the list will be passed either as a String by calling the object's
 * <tt>toString()</tt> object, or if your drag and drop target accepts the
 * {@link TransferableObject#DATA_FLAVOR} data flavor then the actual object
 * will be passed.
 *
 * <p>
 * I'm releasing this code into the Public Domain. Enjoy.
 * </p>
 * <p>
 * <em>Original author: Robert Harder, rharder@usa.net</em></p>
 *
 * @author Robert Harder
 * @author rharder@usa.net
 * @version 1.1
 */
public class DnDList<E>
        extends javax.swing.JList<E>
        implements java.awt.dnd.DropTargetListener,
        java.awt.dnd.DragSourceListener,
        java.awt.dnd.DragGestureListener {

    private static final long serialVersionUID = -2989490853738310931L;

    private java.awt.dnd.DropTarget dropTarget = null;
    private java.awt.dnd.DragSource dragSource = null;

    private int sourceIndex = -1;

    /**
     * Constructs a default {@link DnDList} using a
     * {@link javax.swing.DefaultListModel}.
     *
     * @since 1.1
     */
    public DnDList() {
        super(new javax.swing.DefaultListModel<E>());
        initComponents();
    }   // end constructor

    /**
     * Constructs a {@link DnDList} using the passed list model that must be
     * extended from {@link javax.swing.DefaultListModel}.
     *
     * @param model The model to use
     * @since 1.1
     */
    public DnDList(javax.swing.DefaultListModel<E> model) {
        super(model);
        initComponents();
    }   // end constructor

    /**
     * Constructs a {@link DnDList} by filling in a
     * {@link javax.swing.DefaultListModel} with the passed array of objects.
     *
     * @param data The data from which to construct a list
     * @since 1.1
     */
    public DnDList(E[] data) {
        this();
        ((javax.swing.DefaultListModel) getModel()).copyInto(data);
    }   // end constructor

    /**
     * Constructs a {@link DnDList} by filling in a
     * {@link javax.swing.DefaultListModel} with the passed
     * {@link java.util.Vector} of objects.
     *
     * @param data The data from which to construct a list
     * @since 1.1
     */
    public DnDList(java.util.Vector<E> data) {
        this();
        ((javax.swing.DefaultListModel) getModel()).copyInto(data.toArray());
    }   // end constructor

    private void initComponents() {
        dropTarget = new java.awt.dnd.DropTarget(this, this);
        if (dropTarget == null) {
            log.error("Failed to create DropTarget");
        }
        dragSource = new java.awt.dnd.DragSource();
        dragSource.createDefaultDragGestureRecognizer(this, java.awt.dnd.DnDConstants.ACTION_MOVE, this);
    }   // end initComponents

    /* ********  D R A G   G E S T U R E   L I S T E N E R   M E T H O D S  ******** */
    public void dragGestureRecognized(java.awt.dnd.DragGestureEvent event) {   //System.out.println( "DragGestureListener.dragGestureRecognized" );
        final E selected = getSelectedValue();
        if (selected != null) {
            sourceIndex = getSelectedIndex();
            java.awt.datatransfer.Transferable transfer = new TransferableObject(new TransferableObject.Fetcher() {
                /**
                 * This will be called when the transfer data is requested at
                 * the very end. At this point we can remove the object from its
                 * original place in the list.
                 */
                public E getObject() {
                    ((javax.swing.DefaultListModel) getModel()).remove(sourceIndex);
                    return selected;
                }   // end getObject
            }); // end fetcher

            // as the name suggests, starts the dragging
            dragSource.startDrag(event, java.awt.dnd.DragSource.DefaultLinkDrop, transfer, this);
        } else {
            //System.out.println( "nothing was selected");   
        }
    }   // end dragGestureRecognized

    /* ********  D R A G   S O U R C E   L I S T E N E R   M E T H O D S  ******** */
    public void dragDropEnd(java.awt.dnd.DragSourceDropEvent evt) {   //System.out.println( "DragSourceListener.dragDropEnd" );
    }   // end dragDropEnd

    public void dragEnter(java.awt.dnd.DragSourceDragEvent evt) {   //System.out.println( "DragSourceListener.dragEnter" );
    }   // end dragEnter

    public void dragExit(java.awt.dnd.DragSourceEvent evt) {   //System.out.println( "DragSourceListener.dragExit" );
    }   // end dragExit

    public void dragOver(java.awt.dnd.DragSourceDragEvent evt) {   //System.out.println( "DragSourceListener.dragOver" );
    }   // end dragOver

    public void dropActionChanged(java.awt.dnd.DragSourceDragEvent evt) {   //System.out.println( "DragSourceListener.dropActionChanged" );
    }   // end dropActionChanged

    /* ********  D R O P   T A R G E T   L I S T E N E R   M E T H O D S  ******** */
    public void dragEnter(java.awt.dnd.DropTargetDragEvent evt) {   //System.out.println( "DropTargetListener.dragEnter" );
        evt.acceptDrag(java.awt.dnd.DnDConstants.ACTION_MOVE);
    }   // end dragEnter

    public void dragExit(java.awt.dnd.DropTargetEvent evt) {   //System.out.println( "DropTargetListener.dragExit" );
    }   // end dragExit

    public void dragOver(java.awt.dnd.DropTargetDragEvent evt) {   //System.out.println( "DropTargetListener.dragOver" );
    }   // end dragOver

    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent evt) {   //System.out.println( "DropTargetListener.dropActionChanged" );
        evt.acceptDrag(java.awt.dnd.DnDConstants.ACTION_MOVE);
    }   // end dropActionChanged

    @SuppressWarnings("unchecked") // DnD starts with a generic Object
    public void drop(java.awt.dnd.DropTargetDropEvent evt) {   //System.out.println( "DropTargetListener.drop" );
        java.awt.datatransfer.Transferable transferable = evt.getTransferable();

        // If it's our native TransferableObject, use that
        if (transferable.isDataFlavorSupported(TransferableObject.DATA_FLAVOR)) {
            evt.acceptDrop(java.awt.dnd.DnDConstants.ACTION_MOVE);
            Object obj = null;
            try {
                obj = transferable.getTransferData(TransferableObject.DATA_FLAVOR);
            } // end try
            catch (java.awt.datatransfer.UnsupportedFlavorException e) {
                e.printStackTrace();
            } // end catch
            catch (java.io.IOException e) {
                e.printStackTrace();
            }   // end catch

            if (obj != null) {
                // See where in the list we dropped the element.
                int dropIndex = locationToIndex(evt.getLocation());
                javax.swing.DefaultListModel<E> model = (javax.swing.DefaultListModel<E>) getModel();

                if (dropIndex < 0) {
                    model.addElement((E) obj);
                } // Else is it moving down the list?
                else if (sourceIndex >= 0 && dropIndex > sourceIndex) {
                    model.add(dropIndex - 1, (E) obj);
                } else {
                    model.add(dropIndex, (E) obj);
                }

            } // end if: we got the object
            // Else there was a problem getting the object
            else {
                evt.rejectDrop();
            }   // end else: can't get the object
        } // end if: it's a native TransferableObject
        // Else we can't handle this
        else {
            evt.rejectDrop();
        }
    }   // end drop

    private final static Logger log = LoggerFactory.getLogger(DnDList.class.getName());

}   // end class DnDList
