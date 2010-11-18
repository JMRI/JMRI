
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
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.AnalogClock2Display;

/**
*  ItemPanel for for plain icons and backgrounds 
*/
public class ClockItemPanel extends ItemPanel {

    Hashtable<String, NamedIcon> _iconMap;
    JPanel _iconPanel;

    /**
    * Constructor for plain icons and backgrounds
    */
    public ClockItemPanel(JmriJFrame parentFrame, String  itemType, Editor editor) {
        super(parentFrame,  itemType, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
    }

    public void init() {
        initIconPanel();
        initButtonPanel();
    }

    /**
    * Plain icons have only one family, usually named "set"
    * overide for plain icon & background and put all icons here
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
               icon.reduceTo(100, 100, 0.2);
               JPanel panel = new JPanel();
               String borderName = ItemPalette.convertText(entry.getKey());
               panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                                borderName));
               try {
                   JLabel label = new ClockDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
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

    public class ClockDragJLabel extends DragJLabel {

        public ClockDragJLabel(DataFlavor flavor) {
            super(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String url = ((NamedIcon)getIcon()).getURL();
            if (log.isDebugEnabled()) log.debug("DragJLabel.getTransferData url= "+url);
            AnalogClock2Display c = new AnalogClock2Display(_editor);
            c.setOpaque(false);
            c.update();
            c.setLevel(Editor.CLOCK);
            return c;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClockItemPanel.class.getName());
}
