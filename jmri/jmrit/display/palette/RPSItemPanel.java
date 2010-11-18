package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
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
    public RPSItemPanel(JmriJFrame parentFrame, String  itemType, Editor editor) {
        super(parentFrame,  itemType, editor);
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

    /**
    *  CENTER Panel
    */
    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        _iconFamilyPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));

        Hashtable <String, Hashtable<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            ButtonGroup group = new ButtonGroup();
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
                _family = family;       // let last family be the selected one
                if (button != null) button.setSelected(true);
            }
            makeIconPanel();        // need to have family identified  before calling
            _iconFamilyPanel.add(_iconPanel);
            _iconPanel.setVisible(false);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            String txt = java.text.MessageFormat.format(ItemPalette.rbp.getString("IconFamilies"), _itemType);
            JLabel label = new JLabel(txt);
            panel.add(label);
            panel.add(buttonPanel);
            _iconFamilyPanel.add(panel);
            label.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
            panel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
            _bottom1Panel.setVisible(true);
            _bottom2Panel.setVisible(false);
        } else {
            //log.error("Item type \""+_itemType+"\" has "+(families==null ? "null" : families.size())+ " families.");
            JOptionPane.showMessageDialog(_paletteFrame, java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), _itemType), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            _bottom1Panel.setVisible(false);
            _bottom2Panel.setVisible(true);
        }
        add(_iconFamilyPanel, 0);
    }

    protected void makeIconPanel() {
        _iconPanel = new JPanel();
        if (log.isDebugEnabled()) log.debug("makeIconPanel() _family= \""+_family+"\"");
        if (_family==null) {
            Hashtable <String, Hashtable<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
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
            JOptionPane.showMessageDialog(_paletteFrame, java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), _itemType), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            NamedIcon icon = new NamedIcon(entry.getValue());    // make copy for possible reduction
           icon.reduceTo(100, 100, 0.2);
           JPanel panel = new JPanel();
           String borderName = ItemPalette.convertText(entry.getKey());
           panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                            borderName));
           panel.add(new JLabel(icon));
           int width = Math.max(100, panel.getPreferredSize().width);
           panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
           _iconPanel.add(panel);
        }
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
                JOptionPane.showMessageDialog(_paletteFrame, java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), _itemType), 
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
