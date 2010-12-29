package jmri.jmrit.display.palette;

import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

import java.util.Hashtable;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import jmri.util.JmriJFrame;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.RpsPositionIcon;

/**
*  ItemPanel for for plain icons and backgrounds 
*/
public class RPSItemPanel extends FamilyItemPanel {

    /**
    * Constructor for plain icons and backgrounds
    */
    public RPSItemPanel(JmriJFrame parentFrame, String  type, String family, Editor editor) {
        super(parentFrame,  type, family, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
    }

    /**
    * _bottom1Panel and _bottom2Panel alternate visibility in bottomPanel depending on
    * whether icon families exist.  They are made first because they are referenced in
    * initIconFamiliesPanel()
    */
    public void init() {
        _bottom1Panel = makeBottom1Panel();
        _bottom2Panel = makeBottom2Panel();
        initIconFamiliesPanel();
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        add(bottomPanel);
        if (log.isDebugEnabled()) log.debug("init done for family "+_family);
    }

    public class DragJRadioButton extends JRadioButton implements DragGestureListener, DragSourceListener, Transferable {    

        DataFlavor dataFlavor;

        public DragJRadioButton(String caption) {
            super(caption);

            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this,
                        DnDConstants.ACTION_COPY_OR_MOVE, this);
            try {
                dataFlavor = new DataFlavor(Editor.POSITIONABLE_FLAVOR);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            //if (log.isDebugEnabled()) log.debug("DragJRadioButton ctor");
        }
        /**************** DragGestureListener ***************/
        public void dragGestureRecognized(DragGestureEvent e) {
            if (log.isDebugEnabled()) log.debug("DragJRadioButton.dragGestureRecognized ");
            if (isSelected()) {
                e.startDrag(DragSource.DefaultCopyDrop, this, this); 
            }
        }
        /**************** DragSourceListener ************/
        public void dragDropEnd(DragSourceDropEvent e) {
            }
        public void dragEnter(DragSourceDragEvent e) {
            }
        public void dragExit(DragSourceEvent e) {
            }
        public void dragOver(DragSourceDragEvent e) {
            }
        public void dropActionChanged(DragSourceDragEvent e) {
            }
        /*************** Transferable *********************/
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { dataFlavor };
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            //if (log.isDebugEnabled()) log.debug("DragJRadioButton.isDataFlavorSupported ");
            if (isSelected()) {
                return dataFlavor.equals(flavor);
            }
            return false;
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            Hashtable <String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
            if (iconMap==null) {
                JOptionPane.showMessageDialog(_paletteFrame, 
                                              java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                                             ItemPalette.rbp.getString(_itemType), _family),
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            RpsPositionIcon r = new RpsPositionIcon(_editor);
            r.setActiveIcon(iconMap.get("active"));
            r.setErrorIcon(iconMap.get("error"));
            r.setSize(r.getPreferredSize().width, r.getPreferredSize().height);
            r.setLevel(Editor.SENSORS);
            return r;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RPSItemPanel.class.getName());
}
