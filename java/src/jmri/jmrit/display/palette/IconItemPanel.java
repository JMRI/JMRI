package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LinkingLabel;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PreviewPanel;
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
    JButton _deleteIconButton;
    CatalogPanel _catalog;
    IconDisplayPanel _selectedIcon;
    protected int _level = Editor.ICONS; // sub classes can override (e.g. Background)

    /**
     * Constructor for plain icons and backgrounds.
     *
     * @param type type
     * @param parentFrame parentFrame
     */
    public IconItemPanel(DisplayFrame parentFrame, String type) {
        super(parentFrame, type);
        setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
    }

    @Override
    public void init() {
        if (!_initialized) {
            add(instructions());
            initIconFamiliesPanel(true);
            initLinkPanel();
            add(makeBottomPanel(null));
            _catalog = makeCatalog();
            add(_catalog);
            super.init();
        }
        _catalog.invalidate();
        _previewPanel.invalidate();
    }

    /**
     * Init for update of existing palette item type.
     * _bottom3Panel has an [Update Panel] button put onto _bottom1Panel.
     *
     * @param doneAction doneAction
     */
    @Override
    public void init(ActionListener doneAction) {
        _update = true;
        _suppressDragging = true; // no dragging when updating
        add(new JLabel(Bundle.getMessage("ToUpdateIcon", Bundle.getMessage("updateButton"))));
        initIconFamiliesPanel(true);
        makeBottomPanel(doneAction);
        _catalog = makeCatalog();
        add(_catalog);
    }

    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("AddToPanel")));
        blurb.add(new JLabel(Bundle.getMessage("DragIconPanel")));
        blurb.add(new JLabel(Bundle.getMessage("DragIconCatalog", Bundle.getMessage("ButtonShowCatalog"))));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("ToSelectIcon")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    protected CatalogPanel makeCatalog() {
        CatalogPanel catalog = CatalogPanel.makeDefaultCatalog(false, false, !_update);
        ImagePanel panel = catalog.getPreviewPanel();
        if (!isUpdate()) {
            panel.setImage(_frame.getPreviewBackground());
        } else {
            panel.setImage(_frame.getBackground(0));   //update always should be the panel background
            catalog.setParent(this);
        }
        catalog.setToolTipText(Bundle.getMessage("ToolTipDragCatalog"));
        catalog.setVisible(false);
        return catalog;
    }

    @Override
    protected void previewColorChange() {
        if (_initialized) {
            ImagePanel iconPanel = _catalog.getPreviewPanel();
            if (iconPanel != null) {
                iconPanel.setImage(_frame.getPreviewBackground());
            }
        }
        super.previewColorChange();
    }
    
    /*
     * Plain icons have only one family, usually named "set".
     * Override for plain icon and background and put all icons here.
     */
    protected void initIconFamiliesPanel(boolean addComboBox) {
        if (_iconPanel == null) { // create a new one
            _iconPanel = new ImagePanel();
            _iconPanel.add(Box.createVerticalStrut(20));
            _iconPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            _previewPanel = new PreviewPanel(_frame, _iconPanel, null, addComboBox);
            _iconPanel.setImage(_frame.getPreviewBackground());
            add(_previewPanel, 1);            
            _iconPanel.addMouseListener(new IconListener());
        }

        HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families != null && families.size() > 0) {
            if (families.size() != 1) {
                log.warn("ItemType \"{}\" has {} entries, more than the single one expected", _itemType, families.size());
            }
            
            for (HashMap<String, NamedIcon> map : families.values() ) {
                _iconMap = map; // setting object member variable
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

        if (_iconPanel == null) {
            _iconPanel = new ImagePanel();
            add(new PreviewPanel(_frame, _iconPanel, null, true), 1);            
            log.error("setFamily called with _iconPanel == null typs= {}", _itemType);
       } else {
            _iconPanel.removeAll();
        }
        for (Entry<String, NamedIcon> entry : iconMap.entrySet()) {
            NamedIcon icon = new NamedIcon(entry.getValue()); // make copy for possible reduction
            String borderName = ItemPalette.convertText(entry.getKey());
            IconDisplayPanel panel = new IconDisplayPanel(borderName, icon);
            _iconPanel.add(panel);
        }
    }

    protected JPanel makeBottomPanel(ActionListener doneAction) {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());

        _catalogButton = new JButton(Bundle.getMessage("ButtonShowCatalog"));
        _catalogButton.addActionListener(a -> {
            if (_catalog.isVisible()) {
                hideCatalog();
            } else {
                showCatalog();
            }
        });
        _catalogButton.setToolTipText(Bundle.getMessage("ToolTipCatalog"));
        bottomPanel.add(_catalogButton);

        if (doneAction == null) {
            JButton renameButton = new JButton(Bundle.getMessage("RenameIcon"));
            renameButton.addActionListener(a -> renameIcon());
            bottomPanel.add(renameButton);

            JButton addIconButton = new JButton(Bundle.getMessage("addIcon"));
            addIconButton.addActionListener(a -> addNewIcon());
            addIconButton.setToolTipText(Bundle.getMessage("ToolTipAddIcon"));
            bottomPanel.add(addIconButton);

            _deleteIconButton = new JButton(Bundle.getMessage("deleteIcon"));
            _deleteIconButton.addActionListener(a -> deleteIcon());
            _deleteIconButton.setToolTipText(Bundle.getMessage("ToolTipDeleteIcon"));
            bottomPanel.add(_deleteIconButton);
            _deleteIconButton.setEnabled(false);
        } else {
            JButton updateButton = new JButton(Bundle.getMessage("updateButton")); // custom update label
            updateButton.addActionListener(doneAction);
            bottomPanel.add(updateButton);
        }
        return bottomPanel;
    }

    protected void hideColorPanel() {
    }

    protected void hideCatalog() {
        Dimension oldDim = getSize();
        boolean isPalette = (_frame instanceof ItemPalette); 
        Dimension totalDim;
        if (isPalette) {
            totalDim = ItemPalette._tabPane.getSize();
        } else {
            totalDim = _frame.getSize();            
        }
        _catalog.setVisible(false);
        _catalog.invalidate();
        reSizeDisplay(isPalette, oldDim, totalDim);
        _catalogButton.setText(Bundle.getMessage("ButtonShowCatalog"));
    }
    
    protected void showCatalog() {
        hideColorPanel();
        Dimension oldDim = getSize();
        boolean isPalette = (_frame instanceof ItemPalette); 
        Dimension totalDim;
        if (isPalette) {
            totalDim = ItemPalette._tabPane.getSize();
        } else {
            totalDim = _frame.getSize();            
        }
        _catalog.setVisible(true);
        _catalog.invalidate();
        reSizeDisplay(isPalette, oldDim, totalDim);
        _catalogButton.setText(Bundle.getMessage("HideCatalog"));
    }

    /**
     * Action item for makeBottomPanel.
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

    protected void putIcon(String name, NamedIcon icon) {
        _iconMap.put(name, icon);
        addIconsToPanel(_iconMap);
        validate();
    }

    /**
     * Action item for makeBottomPanel.
     */
    protected void deleteIcon() {
        if (_selectedIcon == null) {
            JOptionPane.showMessageDialog(_frame, Bundle.getMessage("ToSelectIcon"),
                    Bundle.getMessage("ReminderTitle"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        _iconMap.remove(_selectedIcon.getIconName());
        addIconsToPanel(_iconMap);
        _deleteIconButton.setEnabled(false);
        _selectedIcon = null;
        validate();
    }
 
    private void renameIcon() {
        if (_selectedIcon != null) {
            String name = JOptionPane.showInputDialog(_frame, Bundle.getMessage("NoIconName"),
                    Bundle.getMessage("QuestionTitle"), JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                _iconMap.remove(_selectedIcon._borderName);
                putIcon(name, _selectedIcon.getIcon());
                _deleteIconButton.setEnabled(false);
                deselectIcon();
            }
        } else {
            JOptionPane.showMessageDialog(_frame, Bundle.getMessage("ToSelectIcon"),
                    Bundle.getMessage("ReminderTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void setSelection(IconDisplayPanel panel) {
        if (_selectedIcon != null && !panel.equals(_selectedIcon)) {
            deselectIcon();
            setDeleteIconButton(false);
        }
        if (panel._borderName != null) {
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.red, 2), panel._borderName));
            _selectedIcon = panel;
            _catalog.deselectIcon();
            setDeleteIconButton(true);
        } else {    // click not on an "icon"
            _selectedIcon = null;
            setDeleteIconButton(false);
        }
    }

    public void deselectIcon() {
        if (_selectedIcon != null) {
            _selectedIcon.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.black, 1), _selectedIcon.getIconName()));
            _selectedIcon = null;
        }
    }

    private void setDeleteIconButton(boolean set) {
        if (!_update) {
            _deleteIconButton.setEnabled(set);
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

    public NamedIcon getIcon() {
        NamedIcon icon = null;
        if (_selectedIcon != null) {
            icon = _selectedIcon.getIcon();
        }
        if (icon == null) {
            icon = _catalog.getIcon();
            if (icon == null) {
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("ToSelectIcon"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            }
        }
        return icon;
    }

    public class IconDragJLabel extends DragJLabel implements DropTargetListener {

        int level;

        public IconDragJLabel(DataFlavor flavor, int zLevel) {
            super(flavor);
            level = zLevel;
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
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
                    l = new PositionableLabel(NamedIcon.getIconByName(url), _frame.getEditor());
                } else {
                    l = new LinkingLabel(NamedIcon.getIconByName(url), _frame.getEditor(), link);
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
            //if (log.isDebugEnabled()) log.debug("IconDragJLabel.dragExit ");
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
                    log.debug("IconDragJLabel.drop REJECTED!");
                    e.rejectDrop();
                }
            } catch (IOException | UnsupportedFlavorException ioe) {
                log.debug("IconDragJLabel.drop REJECTED!");
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
//                newIcon.reduceTo(100, 100, 0.2);
                label.setText(null);
            }
            _iconMap.put(label.getName(), newIcon);
            if (!_update) {  // only prompt for save from palette
                InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
            }
            addIconsToPanel(_iconMap);
            e.dropComplete(true);
            if (log.isDebugEnabled()) {
                log.debug("IconDragJLabel.drop COMPLETED for {}, {}", label.getName(),
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
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            if (borderName != null) {
                setBorderAndIcon(icon);
            }
            addMouseListener(new IconListener());
        }
        
        String getBorderName() {
            return _borderName;
        }
        
        NamedIcon getIcon() {
            return _icon;
        }

        void setBorderAndIcon(NamedIcon icon) {
            if (icon == null) {
                log.error("IconDisplayPanel: No icon for \"{}\"", _borderName);
                return;
            }
            try {
                JLabel image;
                if (_update) {
                    image = new JLabel();
                } else {
                    image = new IconDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR), _level);
                }
                image.setOpaque(false);
                image.setName(_borderName);
                image.setToolTipText(icon.getName());
                double scale; 
                if (icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
                    image.setText(Bundle.getMessage("invisibleIcon"));
                    image.setForeground(Color.lightGray);
                    scale = 0;
                } else {
                    scale = icon.reduceTo(CatalogPanel.ICON_WIDTH, CatalogPanel.ICON_HEIGHT, CatalogPanel.ICON_SCALE);
                }
                image.setIcon(icon);
                image.addMouseListener(this);
                JPanel iPanel = new JPanel();
                iPanel.setOpaque(false);
                iPanel.add(image);
                add(iPanel);
                
                String scaleMessage = Bundle.getMessage("scale", CatalogPanel.printDbl(scale, 2));
                JLabel label = new JLabel(scaleMessage);
                JPanel sPanel = new JPanel();
                sPanel.setOpaque(false);
                sPanel.add(label);
                add(sPanel);
                FontMetrics fm = getFontMetrics(getFont());
                setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), _borderName));
                int width = fm.stringWidth(_borderName) + 10;
                width = Math.max(fm.stringWidth(scaleMessage), Math.max(width, CatalogPanel.ICON_WIDTH+10));
                int height = getPreferredSize().height;
                setPreferredSize(new Dimension(width, height));
            } catch (java.lang.ClassNotFoundException cnfe) {
                log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
            }
        }

        public String getIconName() {
            return _borderName;
        }
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getSource() instanceof JLabel) {
                setSelection(this);
            } else if (event.getSource() instanceof IconDisplayPanel) {
                IconDisplayPanel panel = (IconDisplayPanel)event.getSource();
                setSelection(panel);
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
    
    class IconListener extends MouseAdapter  {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getSource() instanceof IconDisplayPanel) {
                IconDisplayPanel panel = (IconDisplayPanel)event.getSource();
                setSelection(panel);
            } else if(event.getSource() instanceof ImagePanel) {
                deselectIcon();
           }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(IconItemPanel.class);

}
