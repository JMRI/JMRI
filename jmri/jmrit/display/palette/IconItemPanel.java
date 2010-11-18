package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

//import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
//import java.awt.dnd.*;
import java.io.IOException;

import jmri.util.JmriJFrame;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
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
    public IconItemPanel(JmriJFrame parentFrame, String  itemType, Editor editor) {
        super(parentFrame,  itemType, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
    }

    public void init() {
        initIconPanel();
        initButtonPanel();
        _catalog = CatalogPanel.makeDefaultCatalog();
        add(_catalog);
        _catalog.setVisible(false);
    }

    /**
    * Plain icons have only one family, usually named "set"
    * Override for plain icon & background and put all icons here
    */
    protected void initIconPanel() {
        Hashtable <String, Hashtable<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            if (families.size()!=1) {
                log.warn("ItemType \""+_itemType+"\" has "+families.size()+" families.");
            }
            Iterator <String> iter = families.keySet().iterator();
            while (iter.hasNext()) {
                _family = iter.next();
            }
            _iconPanel = new JPanel();
            _iconMap = families.get(_family);
            Iterator<Entry<String, NamedIcon>> it = _iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                NamedIcon icon = new NamedIcon(entry.getValue());    // make copy for possible reduction
               icon.reduceTo(50, 80, 0.2);
               JPanel panel = new JPanel();
               String borderName = ItemPalette.convertText(entry.getKey());
               panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                                borderName));
               try {
                   JLabel label = new IconDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
                   label.setIcon(icon);
                   label.setName(borderName);
                   panel.add(label);
               } catch (java.lang.ClassNotFoundException cnfe) {
                   cnfe.printStackTrace();
               }
               _iconPanel.add(panel);
            }

        } else {
            log.error("Item type \""+_itemType+"\" has "+(families==null ? "null" : families.size())+" families.");
        }
        add(_iconPanel);
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
                    repaint();
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

        add(bottomPanel);
    }

    protected void setFamily(String family) {
        super.setFamily(family);
        remove(_iconPanel);
        initIconPanel();
    }

    public class IconDragJLabel extends DragJLabel {

        public IconDragJLabel(DataFlavor flavor) {
            super(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String url = ((NamedIcon)getIcon()).getURL();
            if (log.isDebugEnabled()) log.debug("DragJLabel.getTransferData url= "+url);
            PositionableLabel l = new PositionableLabel(NamedIcon.getIconByName(url), _editor);
            l.setPopupUtility(null);        // no text 
            l.setLevel(Editor.ICONS);
            return l;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IconItemPanel.class.getName());
}
