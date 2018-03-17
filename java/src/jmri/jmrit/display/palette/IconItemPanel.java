package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LinkingLabel;
import jmri.jmrit.display.PositionableLabel;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemPanel for plain Icons and Backgrounds.
 * Does NOT use IconDialog class to add, replace or delete icons.
 * @see ItemPanel palette class diagram
 */
public class IconItemPanel extends ItemPanel {

    HashMap<String, NamedIcon> _iconMap;
    HashMap<String, NamedIcon> _tmpIconMap;
    ImagePanel _iconPanel;
    JButton _catalogButton;
    CatalogPanel _catalog;
    IconDisplayPanel _selectedIcon;
    IconDisplayPanel _displayPanel;
    JButton deleteIconButton;
    protected int _level = Editor.ICONS; // sub classes can override (e.g. Background)

    /**
     * Constructor for plain icons and backgrounds.
     *
     * @param type type
     * @param parentFrame parentFrame
     * @param editor editor
     */
    public IconItemPanel(DisplayFrame parentFrame, String type, Editor editor) {
        super(parentFrame, type, editor);
        setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
    }

    @Override
    public void init() {
        if (!_initialized) {
            init(false);
            super.init();
        }
    }

    public void init(boolean isBackGround) {
        add(instructions(isBackGround));
        initIconFamiliesPanel();
        if (!isBackGround) {
            initLinkPanel();
        }
        initButtonPanel();
        _catalog = CatalogPanel.makeDefaultCatalog();
        add(_catalog);
        _catalog.setVisible(false);
        _catalog.setToolTipText(Bundle.getMessage("ToolTipDragCatalog"));
    }

    protected JPanel instructions(boolean isBackGround) {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("AddToPanel")));
        blurb.add(new JLabel(Bundle.getMessage("DragIconPanel")));
        blurb.add(new JLabel(Bundle.getMessage("DragIconCatalog", Bundle.getMessage("ButtonShowCatalog"))));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("ToAddDeleteModify")));
        blurb.add(new JLabel(Bundle.getMessage("ToChangeName")));
        blurb.add(new JLabel(Bundle.getMessage("ToDeleteIcon", Bundle.getMessage("deleteIcon"))));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    /**
     * Plain icons have only one family, usually named "set".
     * Override for plain icon {@literal &} background and put all icons here.
     */
    protected void initIconFamiliesPanel() {
        if (_iconPanel == null) { // create a new one
            _iconPanel = new ImagePanel();
            _iconPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            add(makePreviewPanel(_iconPanel, null), 1);            
        }

        HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families != null && families.size() > 0) {
            if (families.size() != 1) {
                log.warn("ItemType \"{}\" has {}", _itemType, families.size());
            }
            Iterator<String> iter = families.keySet().iterator();
            while (iter.hasNext()) {
                String family = iter.next();
                _iconMap = families.get(family);
                addIconsToPanel(_iconMap);
            }
        } else {
            // make create message
            log.error("Item type \"{}\" has {} families.", _itemType, (families == null ? "null" : families.size()));
        }
    }

    /**
     * Add icons to panel.
     *
     * @param iconMap set of icons to add to panel
     */
    protected void addIconsToPanel(HashMap<String, NamedIcon> iconMap) {

        if (_displayPanel == null) {
            _displayPanel = new IconDisplayPanel(null, null);
            _displayPanel.setOpaque(false);
            _iconPanel.add(_displayPanel);
            _displayPanel.setVisible(true);
        } else {
            _displayPanel.removeAll();
        }
        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            NamedIcon icon = new NamedIcon(entry.getValue()); // make copy for possible reduction
            String borderName = ItemPalette.convertText(entry.getKey());
            IconDisplayPanel panel = new IconDisplayPanel(borderName, icon);
            _displayPanel.add(panel);
        }
    }

    @Override
    protected void setEditor(Editor ed) {
        super.setEditor(ed);
        if (_initialized) {
            addIconsToPanel(_iconMap);
        }
    }

    protected void updateFamiliesPanel() {
        log.debug("updateFamiliesPanel for {}", _itemType);
        initIconFamiliesPanel();
        validate();
    }

    /**
     * SOUTH Panel
     */
    public void initButtonPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());

        _catalogButton = new JButton(Bundle.getMessage("ButtonShowCatalog"));
        _catalogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (_catalog.isVisible()) {
                    hideCatalog();
                } else {
                    _catalog.setVisible(true);
                    _catalogButton.setText(Bundle.getMessage("HideCatalog"));
                }
                repaint();
            }
        });
        _catalogButton.setToolTipText(Bundle.getMessage("ToolTipCatalog"));
        bottomPanel.add(_catalogButton);

        JButton addIconButton = new JButton(Bundle.getMessage("addIcon"));
        addIconButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                addNewIcon();
            }
        });
        addIconButton.setToolTipText(Bundle.getMessage("ToolTipAddIcon"));
        bottomPanel.add(addIconButton);

        add(bottomPanel);

        deleteIconButton = new JButton(Bundle.getMessage("deleteIcon"));
        deleteIconButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                deleteIcon();
            }
        });
        deleteIconButton.setToolTipText(Bundle.getMessage("ToolTipDeleteIcon"));
        bottomPanel.add(deleteIconButton);
        deleteIconButton.setEnabled(false);
        add(bottomPanel);
    }

    void hideCatalog() {
        _catalog.setVisible(false);
        _catalogButton.setText(Bundle.getMessage("ButtonShowCatalog"));
    }

    /**
     * Action item for initButtonPanel.
     */
    protected void addNewIcon() {
        if (log.isDebugEnabled()) {
            log.debug("addNewIcon Action: iconMap.size()= {}", _iconMap.size());
        }
        String name = JOptionPane.showInputDialog(this,
                Bundle.getMessage("NoIconName"), null);
        if (name == null || name.trim().length() == 0) {
            return;
        }
        if (_iconMap.get(name) != null) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("DuplicateIconName", name),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            name = setIconName(name);
            if (name == null || _iconMap.get(name) != null) {
                return;
            }
        }
        String fileName = "resources/icons/misc/X-red.gif";
        NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(fileName, fileName);
        putIcon(name, icon);
    }

    private void putIcon(String name, NamedIcon icon) {
        _iconMap.put(name, icon);
        addIconsToPanel(_iconMap);
        validate();
    }

    /**
     * Action item for initButtonPanel.
     */
    protected void deleteIcon() {
        if (_selectedIcon == null) {
            return;
        }
        if (_iconMap.remove(_selectedIcon.getIconName()) != null) {
            addIconsToPanel(_iconMap);
            deleteIconButton.setEnabled(false);
            validate();
        }
    }

    protected String setIconName(String name) {
        name = JOptionPane.showInputDialog(this,
                Bundle.getMessage("NoIconName"), name);
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        while (_iconMap.get(name) != null) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("DuplicateIconName", name),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            name = JOptionPane.showInputDialog(this,
                    Bundle.getMessage("NoIconName"), name);
            if (name == null || name.trim().length() == 0) {
                return null;
            }
        }
        return name;
    }
    public class IconDragJLabel extends DragJLabel implements DropTargetListener {

        int level;

        public IconDragJLabel(DataFlavor flavor, int zLevel) {
            super(flavor);
            level = zLevel;

            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            log.debug("DropJLabel ctor");
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return _dataFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String url = ((NamedIcon) getIcon()).getURL();
            log.debug("DragJLabel.getTransferData url= {}", url);
            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                String link = _linkName.getText().trim();
                PositionableLabel l;
                if (link.length() == 0) {
                    l = new PositionableLabel(NamedIcon.getIconByName(url), _editor);
                } else {
                    l = new LinkingLabel(NamedIcon.getIconByName(url), _editor, link);
                }
                l.setLevel(level);
                return l;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder sb = new StringBuilder(_itemType);
                sb.append(" for \"");
                sb.append(url);
                sb.append("\"");
                return sb.toString();
            }
            return null;
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dragExit ");
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dragEnter ");
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dragOver ");
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropJLabel.dropActionChanged ");
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if (e.isDataFlavorSupported(_dataFlavor)) {
                    PositionableLabel label = (PositionableLabel)tr.getTransferData(_dataFlavor);
                    accept(e, (NamedIcon)label.getIcon());
                } else if (e.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String text = (String) tr.getTransferData(DataFlavor.stringFlavor);
                    log.debug("drop for stringFlavor {}", text);
                    NamedIcon newIcon = new NamedIcon(text, text);
                    accept(e, newIcon);
                } else {
                    log.debug("DropJLabel.drop REJECTED!");
                    e.rejectDrop();
                }
/*                if (e.isDataFlavorSupported(_dataFlavor)) {
                    NamedIcon newIcon = new NamedIcon((NamedIcon) tr.getTransferData(_dataFlavor));
                    accept(e, newIcon);
                } else if (e.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String text = (String) tr.getTransferData(DataFlavor.stringFlavor);
                    log.debug("drop for stringFlavor {}", text);
                    NamedIcon newIcon = new NamedIcon(text, text);
                    accept(e, newIcon);
                } else {
                    log.debug("DropJLabel.drop REJECTED!");
                    e.rejectDrop();
                }*/
            } catch (IOException ioe) {
                log.debug("DropPanel.drop REJECTED!");
                e.rejectDrop();
            } catch (UnsupportedFlavorException ufe) {
                log.debug("DropJLabel.drop REJECTED!");
                e.rejectDrop();
            }
        }

        private void accept(DropTargetDropEvent e, NamedIcon newIcon) {
            e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            DropTarget target = (DropTarget) e.getSource();
            IconDragJLabel label = (IconDragJLabel) target.getComponent();
            if (log.isDebugEnabled()) {
                log.debug("accept drop for {}, {}", label.getName(), newIcon.getURL());
            }
            if (newIcon == null || newIcon.getIconWidth() < 1 || newIcon.getIconHeight() < 1) {
                label.setText(Bundle.getMessage("invisibleIcon"));
                label.setForeground(Color.lightGray);
            } else {
                newIcon.reduceTo(100, 100, 0.2);
                label.setText(null);
            }
            _iconMap.put(label.getName(), newIcon);
            if (!_update) {  // only prompt for save from palette
                InstanceManager.getDefault(ImageIndexEditor.class).indexChanged(true);
            }
            addIconsToPanel(_iconMap);
            e.dropComplete(true);
            if (log.isDebugEnabled()) {
                log.debug("DropJLabel.drop COMPLETED for {}, {}", label.getName(),
                        (newIcon != null ? newIcon.getURL() : " newIcon==null "));
            }
        }
    }
    
    public class IconDisplayPanel extends JPanel implements MouseListener {
        String _borderName;
        NamedIcon _icon;

        public IconDisplayPanel(String borderName, NamedIcon icon) {
            super();
            _borderName = borderName;
            _icon = icon;
            setOpaque(false);
            if (borderName != null) {
                setBorderAndIcon(icon);
            }
            addMouseListener(this);
        }
        
        private void setBorderAndIcon(NamedIcon icon) {
            if (icon == null) {
                log.error("IconDisplayPanel: No icon for \"{}\"", _borderName);
                return;
            }
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), _borderName));
            try {
                JLabel label = new IconDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR), _level);
                label.setOpaque(false);
                label.setName(_borderName);
                label.setToolTipText(icon.getName());
                add(label);
                if (icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
                    label.setText(Bundle.getMessage("invisibleIcon"));
                    label.setForeground(Color.lightGray);
                } else {
                    icon.reduceTo(50, 80, 0.2);
                }
                label.setIcon(icon);
                int width = Math.max(100, getPreferredSize().width);
                setPreferredSize(new java.awt.Dimension(width, getPreferredSize().height));
            } catch (java.lang.ClassNotFoundException cnfe) {
                log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
            }
        }

        public String getIconName() {
            return _borderName;
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            if (_selectedIcon!= null && !this.equals(_selectedIcon)) {
                _selectedIcon.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.black), _selectedIcon.getIconName()));
            }
            if (_borderName != null) {
                setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.red), _borderName));
                _selectedIcon = this;
                deleteIconButton.setEnabled(true);
            } else {    // click not on an "icon"
                _selectedIcon = null;
                deleteIconButton.setEnabled(false);
            }
        }
        @Override
        public void mousePressed(MouseEvent event) {
        }
        @Override
        public void mouseReleased(MouseEvent event) {
        }
        @Override
        public void mouseEntered(MouseEvent event) {
        }
        @Override
        public void mouseExited(MouseEvent event) {
        }
    }

    private final static Logger log = LoggerFactory.getLogger(IconItemPanel.class);

}
