package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

//import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PositionableLabel;

/**
*  ItemPanel for for plain icons and backgrounds 
*/
public class IconItemPanel extends ItemPanel implements MouseListener {

    Hashtable<String, NamedIcon> _iconMap;
    Hashtable<String, NamedIcon> _tmpIconMap;
    JPanel _iconPanel;
    JButton _catalogButton;
    CatalogPanel _catalog;
	JLabel _selectedIcon;
	JButton deleteIconButton;
    protected int _level = Editor.ICONS;      // sub classes can override (e.g. Background)

    /**
    * Constructor for plain icons and backgrounds
    */
    public IconItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame,  type, family, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
    }

    public void init() {
    	Thread.yield();
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
        blurb.add(new JLabel(ItemPalette.rbp.getString("ToAddDeleteModify")));
        blurb.add(new JLabel(ItemPalette.rbp.getString("ToChangeName")));
        blurb.add(new JLabel(java.text.MessageFormat.format(ItemPalette.rbp.getString("ToDeleteIcon"), 
                						ItemPalette.rbp.getString("deleteIcon"))));
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
            addIconsToPanel(_iconMap);
        } else {
            // make create message todo!!!
            log.error("Item type \""+_itemType+"\" has "+(families==null ? "null" : families.size())+" families.");
        }
    }

    /**
    *  Note caller must create _iconPanel before calling
    */
    protected void addIconsToPanel(Hashtable<String, NamedIcon> iconMap) {
        _iconPanel = new JPanel();
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
               label.setName(borderName);
               panel.add(label);
               if (icon.getIconWidth()<1 || icon.getIconHeight()<1) {
                   label.setText(ItemPalette.rbp.getString("invisibleIcon"));
                   label.setForeground(Color.lightGray);
               } else {
                   icon.reduceTo(50, 80, 0.2);
               }
               label.setIcon(icon);
               int width = Math.max(100, panel.getPreferredSize().width);
               panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));    	
           } catch (java.lang.ClassNotFoundException cnfe) {
               cnfe.printStackTrace();
           }
           _iconPanel.add(panel);
        }
        add(_iconPanel, 1);
        _iconPanel.addMouseListener(this);
    }
    
    /* 
    *  for plain icons and backgrounds, families panel is the icon panel of the one family
    */
    protected void removeIconFamiliesPanel() {
    	if (_iconPanel != null) {
            _iconPanel.removeMouseListener(this);
            remove(_iconPanel);    		
    	}
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

        JButton addIconButton = new JButton(ItemPalette.rbp.getString("addIcon"));
        addIconButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	addNewIcon();
                }
        });
        addIconButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddIcon"));
        bottomPanel.add(addIconButton);

        add(bottomPanel);

        deleteIconButton = new JButton(ItemPalette.rbp.getString("deleteIcon"));
        deleteIconButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	deleteIcon();
                }
        });
        deleteIconButton.setToolTipText(ItemPalette.rbp.getString("ToolTipDeleteIcon"));
        bottomPanel.add(deleteIconButton);
        deleteIconButton.setEnabled(false);

        add(bottomPanel);
    }

    void hideCatalog() {
        _catalog.setVisible(false);
        _catalogButton.setText(ItemPalette.rbp.getString("ButtonShowCatalog"));
    }

    /**
    * Action item for initButtonPanel
    */
    protected void addNewIcon() {
        if (log.isDebugEnabled()) log.debug("addNewIcon Action: iconMap.size()= "+_iconMap.size());
        String name = ItemPalette.rbp.getString("RedX");
        if (_iconMap.get(name)!=null) {
            JOptionPane.showMessageDialog(this,
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("DuplicateIconName"), name),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            name = setIconName(name);
            if ( name==null || _iconMap.get(name)!= null) {
            	return;
            }
        }
        String fileName = "resources/icons/misc/X-red.gif";
        NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(fileName, fileName);
        putIcon(name, icon);
    }
    
    private void putIcon(String name, NamedIcon icon) {
        _iconMap.put(name, icon);
        removeIconFamiliesPanel();
        addIconsToPanel(_iconMap);
        validate();    	
    }

    /**
    * Action item for initButtonPanel
    */
    protected void deleteIcon() {
    	if (_selectedIcon == null) {
    		return;
    	}
        if (_iconMap.remove(_selectedIcon.getName())!= null) {
            removeIconFamiliesPanel();
            addIconsToPanel(_iconMap);
            validate();
        }
    }
    
    protected String setIconName(String name) {   	
    	name = JOptionPane.showInputDialog(this,
    			ItemPalette.rbp.getString("NoIconName"), name);
        if ( name==null || name.trim().length()==0) {
        	return null;
        }
        while (_iconMap.get(name)!=null) {
            JOptionPane.showMessageDialog(this,
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("DuplicateIconName"), name),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
        	name = JOptionPane.showInputDialog(this,
        			ItemPalette.rbp.getString("NoIconName"), name);
            if ( name==null || name.trim().length()==0) {
            	return null;
            }
        }
        return name;
    }

    private void clickEvent(MouseEvent event) {
        java.awt.Component[] comp = _iconPanel.getComponents();
        for (int i=0; i<comp.length; i++) {
            if (comp[i] instanceof JPanel) {
                JPanel panel = (JPanel)comp[i];
                java.awt.Component[] com = panel.getComponents();
                for (int k=0; k<com.length; k++) {
                    if (com[k] instanceof IconDragJLabel) {
                        JLabel icon = (JLabel)com[k];
                        java.awt.Rectangle r = panel.getBounds();
                        if (r.contains(event.getX(), event.getY())) {
                    		if (event.getClickCount() >1){
                    			String name = setIconName(icon.getName());
                    			if (name!=null) {
                    				_iconMap.remove(icon.getName());
                    		        putIcon(name, (NamedIcon)icon.getIcon());
                    			}
                    			return;
                    		}
                        	if (icon.equals(_selectedIcon)) {
                                panel.setBorder(BorderFactory.createTitledBorder(
                                		BorderFactory.createLineBorder(Color.black), 
                                        icon.getName()));
                            	_selectedIcon = null;
                            	deleteIconButton.setEnabled(false);
                        	} else {
                                panel.setBorder(BorderFactory.createTitledBorder(
                                		BorderFactory.createLineBorder(Color.red), 
                                		icon.getName()));
                                deleteIconButton.setEnabled(true);
                            	_selectedIcon = icon;                                		
                        	}
                            return;
                        }
                    }
                }
            }
        }
    }
    public void mouseClicked(MouseEvent event) { clickEvent(event); } 
    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
    
    public class IconDragJLabel extends DragJLabel implements DropTargetListener {

        int level;

        public IconDragJLabel(DataFlavor flavor, int zLevel) {
            super(flavor);
            level = zLevel;
            
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            //if (log.isDebugEnabled()) log.debug("DropJLabel ctor");            
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
                if(e.isDataFlavorSupported(_dataFlavor)) {
                    NamedIcon newIcon = new NamedIcon((NamedIcon)tr.getTransferData(_dataFlavor));
                    accept(e, newIcon);
                } else if(e.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String text = (String)tr.getTransferData(DataFlavor.stringFlavor);
                    if (log.isDebugEnabled()) log.debug("drop for stringFlavor "+text);
                    NamedIcon newIcon = new NamedIcon(text, text);
                    accept(e, newIcon);
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
        private void accept(DropTargetDropEvent e, NamedIcon newIcon) {
            e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            DropTarget target = (DropTarget)e.getSource();
            IconDragJLabel label = (IconDragJLabel)target.getComponent();
            if (log.isDebugEnabled()) log.debug("accept drop for "+label.getName()+
                                                 ", "+newIcon.getURL());
            if (newIcon==null || newIcon.getIconWidth()<1 || newIcon.getIconHeight()<1) {
                label.setText(ItemPalette.rbp.getString("invisibleIcon"));
                label.setForeground(Color.lightGray);
            } else {
                newIcon.reduceTo(100, 100, 0.2);
                label.setText(null);
            }
            _iconMap.put(label.getName(), newIcon);
            removeIconFamiliesPanel();
            addIconsToPanel(_iconMap);
            e.dropComplete(true);
            if (log.isDebugEnabled()) log.debug("DropJLabel.drop COMPLETED for "+label.getName()+
                                                 ", "+(newIcon!=null ? newIcon.getURL().toString():" newIcon==null "));
        }    
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IconItemPanel.class.getName());
}
