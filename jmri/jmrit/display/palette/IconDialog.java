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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.NamedBean;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;

/**
 *
 * @author Pete Cressman  Copyright (c) 2010
 */

public class IconDialog extends ItemDialog {

    Hashtable <String, NamedIcon>   _iconMap;
    JPanel          _iconPanel;
    JPanel          _buttonPanel;
    CatalogPanel    _catalog;
    JTextField      _familyName;

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    */
    public IconDialog(String type, String family, Hashtable <String, NamedIcon> iconMap, ItemPanel parent) {
        super(type, family, 
              java.text.MessageFormat.format(ItemPalette.rbp.getString("ShowIconsTitle"), type), 
              parent, true);
        _iconMap = iconMap;
        _familyName = new JTextField(family);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(makeBannerPanel(false, "IconSetName"));
        _iconPanel = makeIconPanel(_iconMap); 
        panel.add(_iconPanel);
        _buttonPanel = makeButtonPanel();
        panel.add(_buttonPanel);
        _catalog = CatalogPanel.makeDefaultCatalog();
        panel.add(_catalog);

        setContentPane(panel);
        // call super ItemDialog.init() to size and locate dialog
        init();
    }

    /**
    * Constructor for creating a new family
    */
    public IconDialog(String type, Hashtable <String, NamedIcon> newMap, ItemPanel parent) {
        super(type, null, 
              java.text.MessageFormat.format(ItemPalette.rbp.getString("ShowIconsTitle"), type), 
              parent, true);

        _iconMap = newMap;
        _familyName = new JTextField();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(makeBannerPanel(true, "IconSetName"));
        _iconPanel = makeIconPanel(_iconMap); 
        panel.add(_iconPanel);
        panel.add(makeCreateButtonPanel());
        _catalog = CatalogPanel.makeDefaultCatalog();
        panel.add(_catalog);

        getContentPane().add(panel);
        init();
    }

    protected JPanel makeButtonPanel() {
        _buttonPanel = new JPanel();
        _buttonPanel.setLayout(new BoxLayout(_buttonPanel, BoxLayout.Y_AXIS));
        makeAddSetButtonPanel(_buttonPanel);
        makeDoneButtonPanel(_buttonPanel);
        return _buttonPanel;
    }

    /**
    * Add/Delete icon family for types that may have more than 1 fammily
    */
    protected void makeAddSetButtonPanel(JPanel buttonPanel) {
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        JButton addFamilyButton = new JButton(ItemPalette.rbp.getString("addNewFamily"));
        addFamilyButton.addActionListener(new ActionListener() {
                IconDialog dialog;
                public void actionPerformed(ActionEvent a) {
                    setVisible(false);
                    createNewFamily(_parent.makeNewIconMap(_type));
                    dialog.dispose();
                }
                ActionListener init(IconDialog d) {
                    dialog = d;
                    return this;
                }
        }.init(this));
        addFamilyButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddFamily"));
        panel1.add(addFamilyButton);

        JButton deleteButton = new JButton(ItemPalette.rbp.getString("deleteFamily"));
        deleteButton.addActionListener(new ActionListener() {
                IconDialog dialog;
                public void actionPerformed(ActionEvent a) {
                    _parent.removeIconMap(_familyName.getText());
                    ImageIndexEditor.indexChanged(true);
                    updateFamiliesPanel();
                    dialog.dispose();
                }
                ActionListener init(IconDialog d) {
                    dialog = d;
                    return this;
                }
        }.init(this));
        deleteButton.setToolTipText(ItemPalette.rbp.getString("ToolTipDeleteFamily"));
        panel1.add(deleteButton);
        buttonPanel.add(panel1);
    }

    protected void makeDoneButtonPanel(JPanel buttonPanel) {
        JPanel panel0 = new JPanel();
        panel0.setLayout(new FlowLayout());
        JButton doneButton = new JButton(ItemPalette.rbp.getString("doneButton"));
        doneButton.addActionListener(new ActionListener() {
                IconDialog dialog;
                public void actionPerformed(ActionEvent a) {
                    _parent.removeIconMap(_family);
                    addFamily(_family, _iconMap);
                    updateFamiliesPanel();
                    ImageIndexEditor.indexChanged(true);
                    dialog.dispose();
                }
                ActionListener init(IconDialog d) {
                    dialog = d;
                    return this;
                }
        }.init(this));
        panel0.add(doneButton);

        JButton cancelButton = new JButton(ItemPalette.rbp.getString("cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
                IconDialog dialog;
                public void actionPerformed(ActionEvent a) {
                    updateFamiliesPanel();
                    dialog.dispose();
                }
                ActionListener init(IconDialog d) {
                    dialog = d;
                    return this;
                }
        }.init(this));
        panel0.add(cancelButton);
        buttonPanel.add(panel0);
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
                    //check text
                    String family = _familyName.getText();
                    if (family==null || family.length()==0) {
                        JOptionPane.showMessageDialog(_parent.getPaletteFrame(), 
                                ItemPalette.rbp.getString("EnterFamilyName"), 
                                ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Iterator <String> it = getFamilyMaps().keySet().iterator();
                    while (it.hasNext()) {
                       if (family.equals(it.next())) {
                           JOptionPane.showMessageDialog(_parent.getPaletteFrame(),
                                java.text.MessageFormat.format(ItemPalette.rbp.getString("DuplicateFamilyName"), 
							    new Object[] { family, getType() }), 
                                ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                           return;
                       }
                    }
                    addFamily(family, _iconMap);
                    checkIconSizes();
                    ImageIndexEditor.indexChanged(true);
                    updateFamiliesPanel();
                    _parent.setFamily(family);
                    dispose();
                }
                ActionListener init(IconDialog d) {
                    //dialog = d;
                    return this;
                }
        }.init(this));
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

    /**
    * Top panel of both the edit dialog and the create dialog.  Has a text field for a name.
    * @param editable - can text field be edited.
    */
    protected JPanel makeBannerPanel(boolean editable, String labelText) {
        JPanel panel = new JPanel(); 
//        panel.setLayout(new FlowLayout());
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        panel.add(new JLabel(ItemPalette.rbp.getString(labelText)), c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        panel.add(_familyName, c);
        _familyName.setEditable(editable);
        return panel;
    }

    protected JPanel makeIconPanel(Hashtable<String, NamedIcon> iconMap) {
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
       Iterator <String> it = iconMap.keySet().iterator();
       while (it.hasNext()) {
          String name = it.next();
          NamedIcon icon = new NamedIcon(iconMap.get(name));    // make copy for possible reduction
          double scale = icon.reduceTo(100, 100, 0.2);
          JPanel panel = new JPanel();
          panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
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
          panel.add(Box.createHorizontalStrut(100));
          JLabel image = new DropJLabel(icon);
          image.setName(name);
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

          //if (log.isDebugEnabled()) log.debug("makeIconPanel: icon width= "+icon.getIconWidth()+" height= "+icon.getIconHeight());
          //if (log.isDebugEnabled()) log.debug("makeIconPanel: gridx= "+c.gridx+" gridy= "+c.gridy);
          panel.add(iPanel);
          JLabel label = new JLabel(java.text.MessageFormat.format(ItemPalette.rbp.getString("scale"),
                              new Object[] {CatalogPanel.printDbl(scale,2)}));
          JPanel sPanel = new JPanel();
          sPanel.add(label);
          panel.add(sPanel);
          panel.add(Box.createHorizontalStrut(50));
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
               JOptionPane.showMessageDialog(_parent.getPaletteFrame(), 
                                             ItemPalette.rb.getString("IconSizeDiff"), ItemPalette.rb.getString("warnTitle"),
                                             JOptionPane.WARNING_MESSAGE);
               return;
           }
            lastWidth = nextWidth;
            lastHeight = nextHeight;
        }
        if (log.isDebugEnabled()) log.debug("Size: width= "+lastWidth+", height= "+lastHeight); 
    }

    protected class DropJLabel extends JLabel implements DropTargetListener {
        DataFlavor dataFlavor;
        DropJLabel (Icon icon) {
            super(icon);
            try {
                dataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            //if (log.isDebugEnabled()) log.debug("DropJLabel ctor");
        }
        public void dragExit(DropTargetEvent dte) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dragExit ");
        }
        public void dragEnter(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dragEnter ");
        }
        public void dragOver(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dragOver ");
        }
        public void dropActionChanged(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dropActionChanged ");
        }
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if(e.isDataFlavorSupported(dataFlavor)) {
                    NamedIcon newIcon = new NamedIcon((NamedIcon)tr.getTransferData(dataFlavor));
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    DropTarget target = (DropTarget)e.getSource();
                    DropJLabel label = (DropJLabel)target.getComponent();
                    newIcon.reduceTo(100, 100, 0.2);
                    label.setIcon(newIcon);
                    _catalog.setBackground(label);
                    _iconMap.put(label.getName(), newIcon);
                    e.dropComplete(true);
                    ImageIndexEditor.indexChanged(true);
                    if (log.isDebugEnabled()) log.debug("DropJLabel.drop COMPLETED for "+label.getName()+
                                                         ", "+newIcon.getURL());
                } else {
                    if (log.isDebugEnabled()) log.debug("DropJLabel.drop REJECTED!");
                    e.rejectDrop();
                }
            } catch(IOException ioe) {
                if (log.isDebugEnabled()) log.debug("DropPanel.drop REJECTED!");
                e.rejectDrop();
            } catch(UnsupportedFlavorException ufe) {
                if (log.isDebugEnabled()) log.debug("DropJLabel.drop REJECTED!");
                e.rejectDrop();
            }
        }
    }    
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IconDialog.class.getName());
}
