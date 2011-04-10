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
import javax.swing.Box;
import javax.swing.BoxLayout;
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
    protected int _level = Editor.ICONS;      // sub classes can override (e.g. Background)

    /**
    * Constructor for plain icons and backgrounds
    */
    public IconItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame,  type, family, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
    }

    public void init() {
        if (log.isDebugEnabled()) log.debug("init "+_itemType);
        add(instructions());
        initIconFamiliesPanel();
        initButtonPanel();
        _catalog = CatalogPanel.makeDefaultCatalog();
        add(_catalog);
       _catalog.setVisible(false);
        _catalog.setToolTipText(ItemPalette.rbp.getString("ToolTipDragCatalog"));
        setSize(getPreferredSize());
    }

    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(ItemPalette.rbp.getString("AddToPanel")));
        blurb.add(new JLabel(ItemPalette.rbp.getString("DragIconPanel")));
        blurb.add(new JLabel(java.text.MessageFormat.format(ItemPalette.rbp.getString("DragIconCatalog"), 
                                                       ItemPalette.rbp.getString("ButtonShowCatalog"))));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(java.text.MessageFormat.format(ItemPalette.rbp.getString("ToAddDeleteModify"), 
                                                       ItemPalette.rbp.getString("ButtonEditIcons"))));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    /**
    * Plain icons have only one family, usually named "set"
    * Override for plain icon & background and put all icons here
    */
    protected void initIconFamiliesPanel() {
        Hashtable <String, Hashtable<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            if (families.size()!=1) {
                log.warn("ItemType \""+_itemType+"\" has "+families.size()+" families.");
            }
            Iterator <String> iter = families.keySet().iterator();
            while (iter.hasNext()) {
                _family = iter.next();
            }
            _iconMap = families.get(_family);
            _iconPanel = new JPanel();
            addIconsToPanel(_iconMap);
            add(_iconPanel, 1);
        } else {
            // make create message todo!!!
            log.error("Item type \""+_itemType+"\" has "+(families==null ? "null" : families.size())+" families.");
        }
    }

    /**
    *  Note caller must create _iconPanel before calling
    */
    protected void addIconsToPanel(Hashtable<String, NamedIcon> iconMap) {
        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
           Entry<String, NamedIcon> entry = it.next();
           NamedIcon icon = new NamedIcon(entry.getValue());    // make copy for possible reduction
           JPanel panel = new JPanel();
           String borderName = ItemPalette.convertText(entry.getKey());
           panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                            borderName));
           try {
               JLabel label = new IconDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR), _level);
               if (icon.getIconWidth()<1 || icon.getIconHeight()<1) {
                   label.setText(ItemPalette.rbp.getString("invisibleIcon"));
                   label.setForeground(Color.lightGray);
               } else {
                   icon.reduceTo(50, 80, 0.2);
               }
               label.setIcon(icon);
               label.setName(borderName);
               panel.add(label);
               int width = Math.max(100, panel.getPreferredSize().width);
               panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
           } catch (java.lang.ClassNotFoundException cnfe) {
               cnfe.printStackTrace();
           }
           _iconPanel.add(panel);
        }
    }

    /* 
    *  for plain icons and backgrounds, families panel is the icon panel of the one family
    */
    protected void removeIconFamiliesPanel() {
        remove(_iconPanel);
    }

    protected void updateFamiliesPanel() {
        if (log.isDebugEnabled()) log.debug("updateFamiliesPanel for "+_itemType);
        removeIconFamiliesPanel();
        initIconFamiliesPanel();
        validate();
    }

    /**
    *  SOUTH Panel
    */
    public void initButtonPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)

        _catalogButton = new JButton(ItemPalette.rbp.getString("ButtonShowCatalog"));
        _catalogButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (_catalog.isVisible()) {
                        hideCatalog();
                    } else {
                        _catalog.setVisible(true);
                        _catalogButton.setText(ItemPalette.rbp.getString("HideCatalog"));
                    }
                    repaint();
                }
        });
        _catalogButton.setToolTipText(ItemPalette.rbp.getString("ToolTipCatalog"));
        bottomPanel.add(_catalogButton);

        JButton editIconsButton = new JButton(ItemPalette.rbp.getString("ButtonEditIcons"));
        editIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    openEditDialog();
                }
        });
        editIconsButton.setToolTipText(ItemPalette.rbp.getString("ToolTipEditIcons"));
        bottomPanel.add(editIconsButton);

        add(bottomPanel);
    }

    void hideCatalog() {
        _catalog.setVisible(false);
        _catalogButton.setText(ItemPalette.rbp.getString("ButtonShowCatalog"));
    }

    protected void openEditDialog() {
        IconDialog dialog = new SingleIconDialog(_itemType, _family, this);
        dialog.sizeLocate();
    }
    
    public class IconDragJLabel extends DragJLabel {

        int level;

        public IconDragJLabel(DataFlavor flavor, int zLevel) {
            super(flavor);
            level = zLevel;
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String url = ((NamedIcon)getIcon()).getURL();
            if (log.isDebugEnabled()) log.debug("DragJLabel.getTransferData url= "+url);
            PositionableLabel l = new PositionableLabel(NamedIcon.getIconByName(url), _editor);
            l.setPopupUtility(null);        // no text 
            l.setLevel(level);
            return l;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IconItemPanel.class.getName());
}
