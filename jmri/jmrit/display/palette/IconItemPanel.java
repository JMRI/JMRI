package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;
import javax.swing.TransferHandler;

import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;

/**
*  ItemPanel for for plain icons and backgrounds 
*/
public class IconItemPanel extends ItemPanel {

    Hashtable<String, NamedIcon> _iconMap;
    JPanel _iconPanel;
    JButton _catalogButton;
    CatalogPanel _catalog;

    /**
    * Constructor for plain icons and backgrounds
    */
    public IconItemPanel(ItemPalette parentFrame, String  itemType, Editor editor) {
        super(parentFrame,  itemType, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
    }

    public void init() {
        initIconPanel();
        initButtonPanel();
        _catalog = CatalogPanel.makeDefaultCatalog();
        _catalog.setVisible(false);
        add(_catalog, BorderLayout.SOUTH);
    }

    /**
    * Plain icons have only one family, usually named "set"
    * overide for plain icon & background and put all icons here
    */
    @SuppressWarnings("unchecked")
    protected void initIconPanel() {
        Hashtable <String, Hashtable> families = _paletteFrame.getFamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            if (families.size()!=1) {
                log.warn("ItemType \""+_itemType+"\" has "+families.size()+" families.");
            }
            Iterator <String> it = families.keySet().iterator();
            while (it.hasNext()) {
                _family = it.next();
            }
            _iconPanel = new JPanel();
            _iconMap = families.get(_family);
            it = _iconMap.keySet().iterator();
            while (it.hasNext()) {
               String name = it.next();
               NamedIcon icon = new NamedIcon(_iconMap.get(name));    // make copy for possible reduction
               icon.reduceTo(50, 80, 0.2);
               JPanel panel = new JPanel();
               String borderName = null;
               try {
                   borderName = ItemPalette.rbean.getString(name);
               } catch (java.util.MissingResourceException mre) {
                   try {
                       borderName = ItemPalette.rbp.getString(name);
                   } catch (java.util.MissingResourceException mre2) {
                       borderName = name;
                   }
               }
               panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                                borderName));
               JLabel label = new DragJLabel(icon);
               label.setName(borderName);
            //   label.setTransferHandler(new DnDIconItemHandler(_editor));
               panel.add(label);

               _iconPanel.add(panel);
            }

        } else {
            log.error("Item type \""+_itemType+"\" has "+(families==null ? "null" : families.size())+" families.");
        }
        add(_iconPanel, BorderLayout.NORTH);
    }

    /**
    *  SOUTH Panel
    */
    public void initButtonPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)

        _catalogButton = new JButton(ItemPalette.rbp.getString("ShowCatalog"));
        _catalogButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (_catalog.isVisible()) {
                        _catalog.setVisible(false);
                        _catalogButton.setText(ItemPalette.rbp.getString("ShowCatalog"));
                    } else {
                        _catalog.setVisible(true);
                        _catalogButton.setText(ItemPalette.rbp.getString("HideCatalog"));
                    }
                    _paletteFrame.pack();
                }
        });
        _catalogButton.setToolTipText(ItemPalette.rbp.getString("ToolTipCatalog"));
        bottomPanel.add(_catalogButton);

        JButton editIconsButton = new JButton(ItemPalette.rbp.getString("EditIcons"));
        editIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    openEditDialog();
                }
        });
        editIconsButton.setToolTipText(ItemPalette.rbp.getString("ToolTipEditIcons"));
        bottomPanel.add(editIconsButton);

        add(bottomPanel, BorderLayout.CENTER);
    }

    protected void setFamily(String family) {
        super.setFamily(family);
        remove(_iconPanel);
        initIconPanel();
    }

    public class DragJLabel extends JLabel implements DragGestureListener, DragSourceListener, Transferable {    

        DataFlavor dataFlavor;

        public DragJLabel(NamedIcon icon) {
            super(icon);

            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this,
                        DnDConstants.ACTION_COPY_OR_MOVE, this);
            try {
                dataFlavor = new DataFlavor(Editor.POSITIONABLE_FLAVOR);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            //if (log.isDebugEnabled()) log.debug("DragJLabel ctor");
        }
        /**************** DragGestureListener ***************/
        public void dragGestureRecognized(DragGestureEvent e) {
            if (log.isDebugEnabled()) log.debug("DragJLabel.dragGestureRecognized ");
            //Transferable t = getTransferable(this);
            e.startDrag(DragSource.DefaultCopyDrop, this, this); 
        }
        /**************** DragSourceListener ************/
        public void dragDropEnd(DragSourceDropEvent e) {
            if (log.isDebugEnabled()) log.debug("DragJLabel.dragDropEnd ");
            }
        public void dragEnter(DragSourceDragEvent e) {
            //if (log.isDebugEnabled()) log.debug("DragJLabel.DragSourceDragEvent ");
            }
        public void dragExit(DragSourceEvent e) {
            //if (log.isDebugEnabled()) log.debug("DragJLabel.dragExit ");
            }
        public void dragOver(DragSourceDragEvent e) {
            //if (log.isDebugEnabled()) log.debug("DragJLabel.dragOver ");
            }
        public void dropActionChanged(DragSourceDragEvent e) {
            //if (log.isDebugEnabled()) log.debug("DragJLabel.dropActionChanged ");
            }
        /*************** Transferable *********************/
        public DataFlavor[] getTransferDataFlavors() {
            //if (log.isDebugEnabled()) log.debug("DragJLabel.getTransferDataFlavors ");
            return new DataFlavor[] { dataFlavor };
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            //if (log.isDebugEnabled()) log.debug("DragJLabel.isDataFlavorSupported ");
            return dataFlavor.equals(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String url = ((NamedIcon)getIcon()).getURL();
            if (log.isDebugEnabled()) log.debug("DragJLabel.getTransferData url= "+url);
            PositionableLabel l = new PositionableLabel(NamedIcon.getIconByName(url), _editor);
            l.setPopupUtility(null);        // no text 
            l.setDisplayLevel(Editor.ICONS);
            return l;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IconItemPanel.class.getName());
}
