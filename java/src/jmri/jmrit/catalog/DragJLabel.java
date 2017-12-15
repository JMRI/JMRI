package jmri.jmrit.catalog;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.io.IOException;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gives a JLabel the capability to Drag and Drop.
 *
 * @author Pete Cressman Copyright 2009, 2016
 */
public class DragJLabel extends JLabel implements DragGestureListener, DragSourceListener, Transferable {

    protected DataFlavor _dataFlavor;

    public DragJLabel(DataFlavor flavor) {
        super();
        init(flavor);
    }
    
    public DragJLabel(DataFlavor flavor, NamedIcon icon) {
        super(icon);
        init(flavor);
    }
    
    public DragJLabel(DataFlavor flavor, String text) {
        super(text);
        init(flavor);
    }
    
    private void init(DataFlavor flavor) {
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY, this);
        _dataFlavor = flavor;        
    }

    /**
     * Source can override to prohibit dragging if data is incomplete
     * when dragGestureRecognized() is called.
     *
     * @return Source's choice to allow drag
     */
    protected boolean okToDrag() {
        return true;
    }

    /**
     * ************** DragGestureListener **************
     */
    @Override
    public void dragGestureRecognized(DragGestureEvent e) {
        log.debug("DragJLabel.dragGestureRecognized ");
        if (okToDrag()) {
            e.startDrag(DragSource.DefaultCopyDrop, this, this);            
        }
    }

    /**
     * ************** DragSourceListener ***********
     */
    @Override
    public void dragDropEnd(DragSourceDropEvent e) {
        log.debug("DragJLabel.dragDropEnd ");
    }

    @Override
    public void dragEnter(DragSourceDragEvent e) {
        // log.debug("DragJLabel.DragSourceDragEvent ");
    }

    @Override
    public void dragExit(DragSourceEvent e) {
        // log.debug("DragJLabel.dragExit ");
    }

    @Override
    public void dragOver(DragSourceDragEvent e) {
        // log.debug("DragJLabel.dragOver ");
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent e) {
        // log.debug("DragJLabel.dropActionChanged ");
    }

    /**
     * ************* Transferable ********************
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        // log.debug("DragJLabel.getTransferDataFlavors ");
        return new DataFlavor[]{_dataFlavor, DataFlavor.stringFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        // log.debug("DragJLabel.isDataFlavorSupported ");
        if (DataFlavor.stringFlavor.equals(flavor)) {
            return true;
        }
        return _dataFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        log.debug("DragJLabel.getTransferData ");
        if (_dataFlavor.equals(flavor)) {
            return getIcon();
        }
        if (DataFlavor.stringFlavor.equals(flavor)) {
            NamedIcon icon = (NamedIcon) getIcon();
            return icon.getURL();
        }
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(DragJLabel.class);

}
