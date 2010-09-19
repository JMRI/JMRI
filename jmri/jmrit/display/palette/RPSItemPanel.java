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
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.RpsPositionIcon;

/**
*  ItemPanel for for plain icons and backgrounds 
*/
public class RPSItemPanel extends ItemPanel {

    JPanel      _iconFamilyPanel;
    JPanel      _iconPanel;
    JButton     _showIconsButton;
    /**
    * Constructor for plain icons and backgrounds
    */
    public RPSItemPanel(ItemPalette parentFrame, String  itemType, Editor editor) {
        super(parentFrame,  itemType, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragButton"));
    }

    public void init() {
        initIconFamiliesPanel();    // CENTER Panel
        initButtonPanel();          // SOUTH Panel
        if (log.isDebugEnabled()) log.debug("init done for family "+_family);
    }

    /**
    *  CENTER Panel
    */
    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));

        Hashtable <String, Hashtable> families = _paletteFrame.getFamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            String txt = java.text.MessageFormat.format(ItemPalette.rbp.getString("IconFamilies"), _itemType);
            _iconFamilyPanel.add(new JLabel(txt));
            ButtonGroup group = new ButtonGroup();
            @SuppressWarnings("unchecked")
            Iterator <String> it = families.keySet().iterator();
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
            String family = null;
            JRadioButton button = null;
            while (it.hasNext()) {
                family = it.next();
                button = new DragJRadioButton(family);
                button.addActionListener(new ActionListener() {
                        String family;
                        public void actionPerformed(ActionEvent e) {
                            setFamily(family);
                        }
                        ActionListener init(String f) {
                            family = f;
                            if (log.isDebugEnabled()) log.debug("ActionListener.init : for type \""+_itemType+"\", family \""+family+"\"");
                            return this;
                        }
                    }.init(family));
                if (family.equals(_family)) {
                    button.setSelected(true);
                }
                buttonPanel.add(button);
                group.add(button);
            }
            if (_family==null) {
                _family = family;       // let last familiy be the selected one
                if (button != null) button.setSelected(true);
            }
            makeIconPanel();        // need to have family identified  before calling
            _iconFamilyPanel.add(_iconPanel);
            _iconPanel.setVisible(false);
            _iconFamilyPanel.add(buttonPanel);
        } else {
            //log.error("Item type \""+_itemType+"\" has "+(families==null ? "null" : families.size())+ " families.");
            JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
        }
        add(_iconFamilyPanel, BorderLayout.CENTER);
    }

    protected void makeIconPanel() {
        _iconPanel = new JPanel();
        if (log.isDebugEnabled()) log.debug("makeIconPanel() _family= \""+_family+"\"");
        if (_family==null) {
            Hashtable <String, Hashtable> families = _paletteFrame.getFamilyMaps(_itemType);
            if (families!=null) {
                Iterator <String> it = families.keySet().iterator();
                while (it.hasNext()) {
                    _family = it.next();
                }
            }
        }
        Hashtable<String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
        if (iconMap==null) {
            if (log.isDebugEnabled()) log.debug("makeIconPanel() iconMap==null for type \""+_itemType+"\", family \""+_family+"\"");
            Thread.dumpStack();
            JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        Iterator <String> it = iconMap.keySet().iterator();
        while (it.hasNext()) {
           String name = it.next();
           NamedIcon icon = new NamedIcon(iconMap.get(name));    // make copy for possible reduction
           icon.reduceTo(100, 100, 0.2);
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
           panel.add(new JLabel(icon));
           _iconPanel.add(panel);
        }
    }

    /**
    *  SOUTH Panel
    */
    protected void initButtonPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
        _showIconsButton = new JButton(ItemPalette.rbp.getString("ShowIcons"));
        _showIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (_iconPanel.isVisible()) {
                        hideIcons();
                    } else {
                        _iconPanel.setVisible(true);
                        _showIconsButton.setText(ItemPalette.rbp.getString("HideIcons"));
                    }
                }
        });
        _showIconsButton.setToolTipText(ItemPalette.rbp.getString("ToolTipShowIcons"));
        bottomPanel.add(_showIconsButton);

        JButton editIconsButton = new JButton(ItemPalette.rbp.getString("EditIcons"));
        editIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    openEditDialog();
                }
        });
        editIconsButton.setToolTipText(ItemPalette.rbp.getString("ToolTipEditIcons"));
        bottomPanel.add(editIconsButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    protected void hideIcons() {
        _iconPanel.setVisible(false);
        _showIconsButton.setText(ItemPalette.rbp.getString("ShowIcons"));
    }

    protected void removeIconFamiliesPanel() {
        remove(_iconFamilyPanel);
    }

    protected void setFamily(String family) {
        _family = family;
        if (log.isDebugEnabled()) log.debug("setFamily: for type \""+_itemType+"\", family \""+family+"\"");
        boolean visible = _iconPanel.isVisible();
        _iconFamilyPanel.remove(_iconPanel);
        makeIconPanel();        // need to have family identified  before calling
        _iconPanel.setVisible(visible);
        _iconFamilyPanel.add(_iconPanel, 0);
        hideIcons();
        _paletteFrame.pack();
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
                JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            RpsPositionIcon r = new RpsPositionIcon(_editor);
            r.setActiveIcon(iconMap.get("active"));
            r.setErrorIcon(iconMap.get("error"));
            r.setSize(r.getPreferredSize().width, r.getPreferredSize().height);
            r.setDisplayLevel(Editor.SENSORS);
            return r;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RPSItemPanel.class.getName());
}
