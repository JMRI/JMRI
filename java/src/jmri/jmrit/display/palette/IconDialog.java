// IconDialog.java
package jmri.jmrit.display.palette;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;

/**
 * This class is used when FamilyItemPanel classes add, modify or delete icon families.
 * 
 * Note this class cannot be used with super classes of FamilyItemPanel (ItemPanel etc) 
 * since there are several casts to FamilyItemPanel.
 * 
 * @author Pete Cressman  Copyright (c) 2010, 2011
 */

public class IconDialog extends ItemDialog {

    protected FamilyItemPanel _parent;
    protected String    _family;
    protected HashMap <String, NamedIcon>   _iconMap;
    protected JPanel        _iconPanel;
    protected CatalogPanel  _catalog;

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    */
    public IconDialog(String type, String family, FamilyItemPanel parent, HashMap <String, NamedIcon> iconMap ) {
        super(type, Bundle.getMessage("ShowIconsTitle", family));
        if (log.isDebugEnabled()) log.debug("IconDialog ctor: for "+type+" Family "+family);        
        _family = family;
        _parent = parent;
        _iconMap = clone(iconMap);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        
        JPanel p = new JPanel();
        p.add(new JLabel(Bundle.getMessage("FamilyName", family)));
        panel.add(p);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        if (_iconMap != null) {
            makeAddIconButtonPanel(buttonPanel, "ToolTipAddPosition", "ToolTipDeletePosition");
            makeDoneButtonPanel(buttonPanel, "doneButton");
        } else {
        	_iconMap = ItemPanel.makeNewIconMap(type);
        	makeDoneButtonPanel(buttonPanel, "addNewFamily");
        }

        _iconPanel = makeIconPanel(_iconMap);
        panel.add(_iconPanel);	// put icons above buttons
        panel.add(buttonPanel);
        panel.setMaximumSize(panel.getPreferredSize());

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(panel);
        _catalog = CatalogPanel.makeDefaultCatalog();
        _catalog.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        p.add(_catalog);

        setContentPane(p);
        pack();
    }


    // Only multiSensor adds and deletes icons 
    protected void makeAddIconButtonPanel(JPanel buttonPanel, String addTip, String deleteTip) {
    }

    /**
    * Action for both create new family and change existing family
    */
    protected boolean doDoneAction() {
        _parent.reset();
//        checkIconSizes();
    	_parent._currentIconMap = _iconMap;
        if (!_parent.isUpdate()) {  // don't touch palette's maps.  just modify individual device icons
            ItemPalette.removeIconMap(_type, _family);
        	if (!ItemPalette.addFamily(_parent._paletteFrame, _type, _family, _iconMap)) {
                return false;
            }
        }
        _parent.setFamily(_family);
    	_parent.updateFamiliesPanel();
        return true;
    }

    protected void makeDoneButtonPanel(JPanel buttonPanel, String text) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton doneButton = new JButton(Bundle.getMessage(text));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (doDoneAction()) {
                        dispose();
                    }
                }
        });
        panel.add(doneButton);

        JButton cancelButton = new JButton(Bundle.getMessage("cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    dispose();
                }
        });
        panel.add(cancelButton);
        buttonPanel.add(panel);
    }

    protected JPanel makeIconPanel(HashMap<String, NamedIcon> iconMap) {
        if (iconMap==null) {
            log.error("iconMap is null for type "+_type+" family "+_family);
            return null;
        }
       JPanel iconPanel = new JPanel();
       GridBagLayout gridbag = new GridBagLayout();
       iconPanel.setLayout(gridbag);

       int cnt = _iconMap.size();
       int numCol = 2;
       if (cnt>6) {
           numCol = 3;
       }
       GridBagConstraints c = new GridBagConstraints();
       c.fill = GridBagConstraints.NONE;
       c.anchor = GridBagConstraints.CENTER;
       c.weightx = 1.0;
       c.weighty = 1.0;
       int gridwidth = cnt%numCol == 0 ? 1 : 2 ;
       c.gridwidth = gridwidth;
       c.gridheight = 1;
       c.gridx = -gridwidth;
       c.gridy = 0;

       if (log.isDebugEnabled()) log.debug("makeIconPanel: for "+iconMap.size()+" icons. gridwidth= "+gridwidth);
       int panelWidth = 0;
       Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
       while (it.hasNext()) {
    	   Entry<String, NamedIcon> entry = it.next();
    	   NamedIcon icon = new NamedIcon(entry.getValue());    // make copy for possible reduction
    	   double scale = icon.reduceTo(100, 100, 0.2);
    	   JPanel panel = new JPanel();
    	   panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	   String borderName = ItemPalette.convertText(entry.getKey());
    	   panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
    			   borderName));
    	   panel.add(Box.createHorizontalStrut(100));
    	   JLabel image = new DropJLabel(icon, _iconMap, _parent.isUpdate());
    	   image.setName(entry.getKey());
    	   if (icon.getIconWidth()<1 || icon.getIconHeight()<1) {
    		   image.setText(Bundle.getMessage("invisibleIcon"));
    		   image.setForeground(Color.lightGray);
    	   }
    	   image.setToolTipText(icon.getName());
    	   JPanel iPanel = new JPanel();
    	   iPanel.add(image);

    	   c.gridx += gridwidth;
    	   if (c.gridx >= numCol*gridwidth) { //start next row
    		   c.gridy++;
    		   if (cnt < numCol) { // last row
    			   JPanel p =  new JPanel();
    			   p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    			   panelWidth = panel.getPreferredSize().width;
    			   p.add(Box.createHorizontalStrut(panelWidth));
    			   c.gridx = 0;
    			   c.gridwidth = 1;
    			   gridbag.setConstraints(p, c);
    			   //if (log.isDebugEnabled()) log.debug("makeIconPanel: gridx= "+c.gridx+" gridy= "+c.gridy);
    			   iconPanel.add(p);
    			   c.gridx = numCol-cnt;
    			   c.gridwidth = gridwidth;
    			   //c.fill = GridBagConstraints.NONE;
    		   } else {
    			   c.gridx = 0;
    		   }
    	   }
    	   cnt--;

//    	   if (log.isDebugEnabled()) log.debug("makeIconPanel: icon width= "+icon.getIconWidth()+" height= "+icon.getIconHeight());
//    	   if (log.isDebugEnabled()) log.debug("makeIconPanel: gridx= "+c.gridx+" gridy= "+c.gridy);
    	   panel.add(iPanel);
    	   JLabel label = new JLabel(java.text.MessageFormat.format(Bundle.getMessage("scale"),
    			   new Object[] {CatalogPanel.printDbl(scale,2)}));
    	   JPanel sPanel = new JPanel();
    	   sPanel.add(label);
    	   panel.add(sPanel);
    	   panel.add(Box.createHorizontalStrut(20));
    	   gridbag.setConstraints(panel, c);
    	   iconPanel.add(panel);
       }
       if (panelWidth > 0) {
    	   JPanel p =  new JPanel();
    	   p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    	   p.add(Box.createHorizontalStrut(panelWidth));
    	   c.gridx = numCol*gridwidth-1;
    	   c.gridwidth = 1;
    	   gridbag.setConstraints(p, c);
    	   //if (log.isDebugEnabled()) log.debug("makeIconPanel: gridx= "+c.gridx+" gridy= "+c.gridy);
    	   iconPanel.add(p);
       }
       return iconPanel;
    }
/*
    private void checkIconSizes() {
        Iterator <NamedIcon> iter = _iconMap.values().iterator();
        int lastWidth = 0;
        int lastHeight = 0;
        boolean first = true;
        while (iter.hasNext()) {
            NamedIcon icon = iter.next();
        	if (first) {
                lastWidth = icon.getIconWidth();
                lastHeight = icon.getIconHeight();
                first = false;
                continue;
        	}
           int nextWidth = icon.getIconWidth();
           int nextHeight = icon.getIconHeight();
           if ((Math.abs(lastWidth - nextWidth) > 5 || Math.abs(lastHeight - nextHeight) > 5)) {
               JOptionPane.showMessageDialog(_parent._paletteFrame, 
                                             Bundle.getMessage("IconSizeDiff"), Bundle.getMessage("warnTitle"),
                                             JOptionPane.WARNING_MESSAGE);
               return;
           }
            lastWidth = nextWidth;
            lastHeight = nextHeight;
        }
        if (log.isDebugEnabled()) log.debug("Size: width= "+lastWidth+", height= "+lastHeight); 
    }*/
    
    protected HashMap<String, NamedIcon> clone(HashMap<String, NamedIcon> map) {
        HashMap<String, NamedIcon> clone = null;
        if (map!=null) {
        	clone = new HashMap<String, NamedIcon>();
            Iterator<Entry<String, NamedIcon>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                clone.put(entry.getKey(), new NamedIcon(entry.getValue()));
            }
        }
        return clone;
    }
    
    protected void sizeLocate() {
        setSize(_parent.getSize().width, this.getPreferredSize().height);
        setLocationRelativeTo(_parent);
        setVisible(true);
        pack();
    }

    static Logger log = LoggerFactory.getLogger(IconDialog.class.getName());
}
