package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

    protected JButton _catalogButton;
    protected JButton _deleteIconButton;
    protected CatalogPanel _catalog;
    protected IconDisplayPanel _selectedIcon;
    protected DataFlavor _positionableDataFlavor;
    protected DataFlavor _namedIconDataFlavor;
    protected int _level = Editor.ICONS; // sub classes can override (e.g. Background)
    private NamedIcon _updateIcon;

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
            super.init();
            initLinkPanel();
            _catalog = makeCatalog();
            add(_catalog);
        }
    }

    /**
     * Init for update of existing palette item type.
     *
     * @param doneAction doneAction
     */
    public void init(ActionListener doneAction) {
        _update = true;
        _doneAction = doneAction;
        _suppressDragging = true; // no dragging when updating
        initIconFamiliesPanel();
        add(_iconPanel);
        _catalog = makeCatalog();
        add(_catalog);
    }

    @Override
    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(new JLabel(Bundle.getMessage("DragIconPanel")));
        blurb.add(new JLabel(Bundle.getMessage("DragIconCatalog")));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    protected CatalogPanel makeCatalog() {
        CatalogPanel catalog = CatalogPanel.makeDefaultCatalog(false, false, true);
        ImagePanel panel = catalog.getPreviewPanel();
        if (!_update) {
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
    @Override
    protected void initIconFamiliesPanel() {
        super.initIconFamiliesPanel();
        if (!_update) {
            _iconPanel.addMouseListener(new IconListener());
        }
    }

    @Override
    protected void makeFamiliesPanel() {
        if (!_update) {
            HashMap<String, HashMap<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
            log.debug("makeFamiliesPanel Num families= {}", families.size());
            _currentIconMap = families.get("set");
            if (_currentIconMap == null) {
                _currentIconMap = new HashMap<>();
                if (families.size() != 0) {
                    log.error("{} Unknown families found for {}", families.size(), _itemType);
                }
            }
            makeDataFlavors();
        } else {
            _currentIconMap = new HashMap<>();
        }
        addIconsToPanel();
        makePreviewPanel(true, null);
    }
    private void addIconsToPanel() {
        boolean isEmpty = _currentIconMap.isEmpty();
        if (_bottomPanel == null) {
            makeBottomPanel(isEmpty);
        } else {
           if (isEmpty ^ _wasEmpty) {
               remove(_bottomPanel);
               makeBottomPanel(isEmpty);
           }
        }
        _wasEmpty = isEmpty;
        addIconsToPanel(_currentIconMap, _iconPanel, _update);
    }

    private void makeDataFlavors() {
        try {
            _positionableDataFlavor = new DataFlavor(Editor.POSITIONABLE_FLAVOR);
            _namedIconDataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
        } catch (ClassNotFoundException cnfe) {
            log.error("Unable to find class supporting {}", ImageIndexEditor.IconDataFlavorMime, cnfe);
        }
        if (!_update) {
            new DropTarget(_iconPanel, DnDConstants.ACTION_COPY_OR_MOVE, new ADropTargetListener());
        }
    }

    @Override
    protected JPanel makeIconDisplayPanel(String key, HashMap<String, NamedIcon> iconMap, boolean dropIcon) {
        NamedIcon icon = iconMap.get(key);
        return new IconDisplayPanel(key, icon, dropIcon);
    }

    @Override
    protected JPanel makeItemButtonPanel() {
        JPanel panel = new JPanel();
        panel.add(makeCatalogButton());
        if (_update) {
            return panel;
        }
        JButton renameButton = new JButton(Bundle.getMessage("RenameIcon"));
        renameButton.addActionListener(a -> renameIcon());
        panel.add(renameButton);

        _deleteIconButton = new JButton(Bundle.getMessage("deleteIcon"));
        _deleteIconButton.addActionListener(a -> deleteIcon());
        _deleteIconButton.setToolTipText(Bundle.getMessage("ToolTipDeleteIcon"));
        panel.add(_deleteIconButton);
        return panel;
    }

    private JButton makeCatalogButton() {
        if (_catalogButton == null) {
            _catalogButton = new JButton(Bundle.getMessage("ButtonShowCatalog"));
            _catalogButton.addActionListener(a -> {
                if (_catalog.isVisible()) {
                    hideCatalog();
                } else {
                    showCatalog();
                }
            });
            _catalogButton.setToolTipText(Bundle.getMessage("ToolTipCatalog"));
        }
        return _catalogButton;
    }

    /**
     * Replacement panel for _bottomPanel when no icon families exist for
     * _itemType.
     */
    @Override
    protected JPanel makeSpecialBottomPanel(boolean update) {
        JPanel _bottom2Panel = new JPanel();
        _bottom2Panel.add(makeCatalogButton());

        if(!update) {
            JButton button = new JButton(Bundle.getMessage("RestoreDefault"));
            button.addActionListener(a -> loadDefaultType());
            _bottom2Panel.add(button);
        }
        return _bottom2Panel;
    }

    protected void hideCatalog() {
        Dimension oldDim = getSize();
        boolean isPalette = (_frame instanceof ItemPalette); 
        Dimension totalDim = _frame.getSize();
        _catalog.setVisible(false);
        _catalog.invalidate();
        reSizeDisplay(isPalette, oldDim, totalDim);
        _catalogButton.setText(Bundle.getMessage("ButtonShowCatalog"));
    }
    
    protected void showCatalog() {
        Dimension oldDim = getSize();
        boolean isPalette = (_frame instanceof ItemPalette); 
        Dimension totalDim = _frame.getSize();
        _catalog.setVisible(true);
        _catalog.invalidate();
        reSizeDisplay(isPalette, oldDim, totalDim);
        _catalogButton.setText(Bundle.getMessage("HideCatalog"));
    }

    protected void putIcon(String name, NamedIcon icon) {
        _currentIconMap.put(name, icon);
        log.debug("putIcon {}", name);
        updateIcons();
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
        log.debug("deleteIcon {}", _selectedIcon._key);
        _currentIconMap.remove(_selectedIcon._key);
        updateIcons();
    }

    @Override
    protected void hideIcons() {
    }

    /*
     * Family 'set' has changed
     */
    private void updateIcons() {
        _previewPanel.setVisible(false);    // necessary to guarantee _iconPanel gets refreshed
        addIconsToPanel();
        Dimension oldDim = getSize();
        boolean isPalette = (_frame instanceof ItemPalette); 
        Dimension totalDim = _frame.getSize();
        _previewPanel.setVisible(true);
        if (!_update) {
            ItemPalette.removeIconMap(_itemType, "set");
            ItemPalette.addFamily(_itemType, "set", _currentIconMap);
        }
        _iconPanel.invalidate();
        reSizeDisplay(isPalette, oldDim, totalDim);
    }

    private void renameIcon() {
        if (_selectedIcon != null) {
            String name = JOptionPane.showInputDialog(_frame, Bundle.getMessage("NoIconName"),
                    Bundle.getMessage("QuestionTitle"), JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                _currentIconMap.remove(_selectedIcon._key);
                putIcon(name, _selectedIcon._icon);
                _selectedIcon._key = name;
                deselectIcon();
            }
        } else {
            JOptionPane.showMessageDialog(_frame, Bundle.getMessage("ToSelectIcon"),
                    Bundle.getMessage("ReminderTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void setSelection(@Nonnull IconDisplayPanel panel) {
        if (_selectedIcon != null && !panel.equals(_selectedIcon)) {
            deselectIcon();
        }
        String borderName = ItemPalette.convertText(panel._key);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.red, 2), borderName));
        _selectedIcon = panel;
        _catalog.deselectIcon();
    }

    public void deselectIcon() {
        if (_selectedIcon != null) {
            String borderName = ItemPalette.convertText(_selectedIcon._key);
            _selectedIcon.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.black, 1), borderName));
            _selectedIcon = null;
        }
    }

    protected String setIconName(String name) {
        name = JOptionPane.showInputDialog(this,
                Bundle.getMessage("NoIconName"), name);
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        while (_currentIconMap.get(name) != null) {
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

    protected void initLinkPanel() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("ToLinkToURL", "Text")));
        blurb.add(new JLabel(Bundle.getMessage("enterPanel")));
        blurb.add(new JLabel(Bundle.getMessage("enterURL")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("LinkName")));
        blurb.add(_linkName);
        _linkName.setToolTipText(Bundle.getMessage("ToolTipLink"));
        blurb.setToolTipText(Bundle.getMessage("ToolTipLink"));
        JPanel panel = new JPanel();
        panel.add(blurb);
        JPanel linkPanel = new JPanel();
        linkPanel.add(panel);
        add(linkPanel);
    }

    /*
     * edit a PositionableLabel's icon
     * @param icon the icon to be update/changed
     */
    public void setUpdateIcon(NamedIcon icon) {
        if (!_update || icon == null) {
            return;
        }
        _updateIcon = icon;
        String name = icon.getName();
        if (name == null) {
            name =Bundle.getMessage("unNamed");
        } else {
            java.io.File f = new java.io.File(name);
            name = f.getName();
            int index = name.indexOf('.');
            if (index > 0) {
                name = name.substring(0, index);
            }
        }
        _currentIconMap.put(name, icon);
        updateIcons();
    }

    /*
     * edit a PositionableLabel's icon
     * @return return update/changed icon
     */
    public NamedIcon getUpdateIcon() {
        return _updateIcon;
    }

    class ADropTargetListener extends DropTargetAdapter {
        ADropTargetListener() {
            super();
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            boolean accepted = false;
            try {
                Transferable tr = e.getTransferable();
                if (e.isDataFlavorSupported(_positionableDataFlavor)) {
                    PositionableLabel label = (PositionableLabel)tr.getTransferData(_positionableDataFlavor);
                    NamedIcon newIcon = new NamedIcon((NamedIcon)label.getIcon());
                    accepted = accept(label.getName(), newIcon);
                } else if (e.isDataFlavorSupported(_namedIconDataFlavor)) {
                    NamedIcon icon = (NamedIcon) tr.getTransferData(_namedIconDataFlavor);
                    NamedIcon newIcon = new NamedIcon(icon);
                    accepted = accept(icon.getName(), newIcon);
                } else if (e.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String text = (String) tr.getTransferData(DataFlavor.stringFlavor);
                    log.debug("drop for stringFlavor {}", text);
                    NamedIcon newIcon = new NamedIcon(text, text);
                    accepted = accept(Bundle.getMessage("unNamed"), newIcon);
                 } else {
                    log.debug("IconDragJLabel.drop REJECTED!");
                    e.rejectDrop();
                }
            } catch (IOException | UnsupportedFlavorException ioe) {
            }
            if (!accepted) {
                e.rejectDrop();
                log.debug("IconDragJLabel.drop REJECTED!");
            } else {
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            }
        }

        private boolean accept(String name, NamedIcon newIcon) {
            if (name == null || newIcon == null) {
                return false;
            }
            putIcon(name, newIcon);
            if (log.isDebugEnabled()) {
                log.debug("IconDragJLabel.drop COMPLETED for {}, {}", name, newIcon.getURL());
            }
            return true;
        }
        
    }
    
    public class IconDragJLabel extends DragJLabel /*implements DropTargetListener*/ {

        int level;

        public IconDragJLabel(DataFlavor flavor, NamedIcon icon, int zLevel) {
            super(flavor, icon);
            level = zLevel;
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
                return _itemType + " for \"" + url + "\"";
            }
            return null;
        }
    }

    class ADropJLabel extends DropJLabel {
        ADropJLabel(Icon icon) {
            super(icon);
        }
        ADropJLabel(Icon icon, HashMap<String, NamedIcon> iconMap) {
            super(icon, iconMap);
        }

        @Override
        protected void accept(DropTargetDropEvent e, NamedIcon newIcon) {
            super.accept(e, newIcon);
            _updateIcon = newIcon;
        }        
    }

    public class IconDisplayPanel extends JPanel implements MouseListener {
        String _key;
        NamedIcon _icon;

        public IconDisplayPanel(String key, NamedIcon icon, boolean dropIcon) {
            super();
            _key = key;
            _icon = icon;
            JLabel image;
            if (dropIcon) {
                image = new ADropJLabel(icon);
            } else {
                image = new IconDragJLabel(_positionableDataFlavor, icon, _level);
                image.addMouseListener(this);
                addMouseListener(new IconListener());
            }
            wrapIconImage(icon, image, this, key);
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
            } else {
                deselectIcon();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(IconItemPanel.class);

}
