// IconDialog.java
package jmri.jmrit.display.palette;

import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;

/**
 *
 * @author Pete Cressman  Copyright (c) 2010, 2011
 */

public class IconDialog extends ItemDialog {

    protected Hashtable <String, NamedIcon>   _iconMap;
    protected JPanel        _iconPanel;
    protected CatalogPanel  _catalog;
    protected JTextField    _familyName;
    protected JButton       _addFamilyButton;
    protected JButton       _deleteButton;

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    */
    public IconDialog(String type, String family, ItemPanel parent) {
        super(type, family, 
              java.text.MessageFormat.format(ItemPalette.rbp.getString("ShowIconsTitle"), type), 
              parent, true);
        _familyName = new JTextField(family);
//        if (log.isDebugEnabled()) log.debug("ctor IconDialog type= "+type+", family= "+family);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        panel.add(ItemPalette.makeBannerPanel("IconSetName", _familyName));

        initMap(type, family);
        panel.add(_iconPanel);

        if (_family!=null) {
            panel.add(makeButtonPanel());
        } else {
            panel.add(makeCreateButtonPanel());
        }
        _catalog = CatalogPanel.makeDefaultCatalog();
        _catalog.setToolTipText(ItemPalette.rb.getString("ToolTipDragIcon"));
        panel.add(_catalog);

//        setContentPane(panel);
        setContentPane(new javax.swing.JScrollPane(panel));
    }

    protected void initMap(String type, String family) {
        _familyName.setEditable(true);
        if (family!=null) {
            _iconMap = ItemPalette.getIconMap(type, family);
            if (_iconMap==null) {
                JOptionPane.showMessageDialog(_parent._paletteFrame, 
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                       ItemPalette.rbp.getString(type), family), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            }
        }
        if (_iconMap==null) {
            _iconMap = ItemPanel.makeNewIconMap(type);
            _family = null;
            _familyName.setText("");
        }
        _iconPanel = makeIconPanel(_iconMap);
    }

    protected JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        makeAddSetButtonPanel(buttonPanel);
        makeDoneButtonPanel(buttonPanel);
        return buttonPanel;
    }

    /**
    * Add/Delete icon family for types that may have more than 1 fammily
    */
    protected void makeAddSetButtonPanel(JPanel buttonPanel) {
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        _addFamilyButton = new JButton(ItemPalette.rbp.getString("addNewFamily"));
        _addFamilyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addFamilySet();
                    dispose();
                }
        });
        _addFamilyButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddFamily"));
        panel1.add(_addFamilyButton);

        _deleteButton = new JButton(ItemPalette.rbp.getString("deleteFamily"));
        _deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    deleteFamilySet();
                    dispose();
                }
        });
        _deleteButton.setToolTipText(ItemPalette.rbp.getString("ToolTipDeleteFamily"));
        panel1.add(_deleteButton);
        buttonPanel.add(panel1);
    }
    
    public void dispose() {
        super.dispose();
    }

    /**
    * NOT add a new family.  Create a family when all previous families are deleted
    */
    protected void createNewFamily() {
        //check text
        String family = _familyName.getText();
        Iterator <String> iter = ItemPalette.getFamilyMaps(_type).keySet().iterator();
        if (!ItemPalette.familyNameOK(_parent._paletteFrame, _type, family, iter)) {
            return;
        }
        if (addFamily(family, _iconMap)) {
            checkIconSizes();
            _parent.updateFamiliesPanel();
            _parent.setFamily(family);
            dispose();
        }
    }

    /**
    * Action item for add new family
    */
    protected void addFamilySet() {
        setVisible(false);
        _parent.createNewFamily(_type);
    }

    /**
    * Action item for add delete family
    */
    protected void deleteFamilySet() {
        ItemPalette.removeIconMap(_type, _familyName.getText());
        _parent._family = null;
        _parent.updateFamiliesPanel();
    }

    protected void makeDoneButtonPanel(JPanel buttonPanel) {
        JPanel panel0 = new JPanel();
        panel0.setLayout(new FlowLayout());
        JButton doneButton = new JButton(ItemPalette.rbp.getString("doneButton"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (doDoneAction()) {
                        dispose();
                    }
                }
        });
        panel0.add(doneButton);

        JButton cancelButton = new JButton(ItemPalette.rbp.getString("cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _parent.updateFamiliesPanel();
                    dispose();
                }
        });
        panel0.add(cancelButton);
        buttonPanel.add(panel0);
    }

    /**
    * Action item for makeDoneButtonPanel
    */
    protected boolean doDoneAction() {
        //check text
        String family = _familyName.getText();
        if (_family!=null && _family.equals(family)) {
            ItemPalette.removeIconMap(_type, family);
        }
        if (addFamily(family, _iconMap)) {
            _parent.updateFamiliesPanel();
            return true;
        }
        return false;
    }

/*
    static public void printKeys(Hashtable <String, NamedIcon>  map) {
        Iterator <String> it = map.keySet().iterator();
        System.out.print("Keys= ");
        while (it.hasNext()) {
            System.out.print(it.next()+", ");
        }
        System.out.println();
    }
*/
    protected JPanel makeCreateButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton newFamilyButton = new JButton(ItemPalette.rbp.getString("createNewFamily"));
        newFamilyButton.addActionListener(new ActionListener() {
                //IconDialog dialog; never used?
                public void actionPerformed(ActionEvent a) {
                    createNewFamily();
                }
        });
        newFamilyButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddFamily"));
        panel.add(newFamilyButton);

        JButton cancelButton = new JButton(ItemPalette.rbp.getString("cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    dispose();
                }
        });
        panel.add(cancelButton);
        return panel;
    }

    protected JPanel makeIconPanel(Hashtable<String, NamedIcon> iconMap) {
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
    	   JLabel image = new DropJLabel(icon, _iconMap);
    	   image.setName(entry.getKey());
    	   if (icon.getIconWidth()<1 || icon.getIconHeight()<1) {
    		   image.setText(ItemPalette.rbp.getString("invisibleIcon"));
    		   image.setForeground(Color.lightGray);
    	   }
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

    	   if (log.isDebugEnabled()) log.debug("makeIconPanel: icon width= "+icon.getIconWidth()+" height= "+icon.getIconHeight());
    	   if (log.isDebugEnabled()) log.debug("makeIconPanel: gridx= "+c.gridx+" gridy= "+c.gridy);
    	   panel.add(iPanel);
    	   JLabel label = new JLabel(java.text.MessageFormat.format(ItemPalette.rbp.getString("scale"),
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

    void checkIconSizes() {
        Iterator <NamedIcon> iter = _iconMap.values().iterator();
        int lastWidth = 0;
        int lastHeight = 0;
        while (iter.hasNext()) {
           NamedIcon icon = iter.next();
           int nextWidth = icon.getIconWidth();
           int nextHeight = icon.getIconHeight();
           if ((lastWidth>0 && lastWidth != nextWidth) || (lastHeight>0 && lastHeight != nextHeight)) {
               JOptionPane.showMessageDialog(_parent._paletteFrame, 
                                             ItemPalette.rb.getString("IconSizeDiff"), ItemPalette.rb.getString("warnTitle"),
                                             JOptionPane.WARNING_MESSAGE);
               return;
           }
            lastWidth = nextWidth;
            lastHeight = nextHeight;
        }
        if (log.isDebugEnabled()) log.debug("Size: width= "+lastWidth+", height= "+lastHeight); 
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IconDialog.class.getName());
}
