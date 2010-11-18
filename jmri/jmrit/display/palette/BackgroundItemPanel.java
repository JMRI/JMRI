package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.event.ChangeListener; 
import javax.swing.event.ChangeEvent; 

import jmri.util.JmriJFrame;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PositionableLabel;

import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.CatalogTreeLeaf;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.jmrit.catalog.NamedIcon;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*/
public class BackgroundItemPanel extends IconItemPanel {

    /**
    * Constructor for plain icons and backgrounds
    */
    public BackgroundItemPanel(JmriJFrame parentFrame, String  itemType, Editor editor) {
        super(parentFrame,  itemType, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
    }

    public void init() {
        initIconPanel();
        initButtonPanel();
        initBottomPanel();
    }

    /**
    * Plain icons have only one family, usually named "set"
    * Override for plain icon & background and put all icons here
    */
    protected void initIconPanel() {
        Hashtable <String, Hashtable<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            if (families.size()!=1) {
                log.warn("ItemType \""+_itemType+"\" has "+families.size()+" families.");
            }
            // only one family
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
               icon.reduceTo(50, 80, 0.15);
               JPanel panel = new JPanel();
               String borderName = ItemPalette.convertText(entry.getKey());
               panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                                borderName));
               try {
                   JLabel label = new BkdDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
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
    */
    public void initButtonPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)

        JButton backgroundButton = new JButton(ItemPalette.rbp.getString("BackgroundColor"));
        backgroundButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    hideCatalog();
                    new ColorDialog(_editor);
                }
        });
        backgroundButton.setToolTipText(ItemPalette.rbp.getString("ToolTipEditColor"));
        bottomPanel.add(backgroundButton);

        _catalogButton = new JButton(ItemPalette.rbp.getString("ShowCatalog"));
        _catalogButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (_catalog.isVisible()) {
                        hideCatalog();
                    } else {
                        _catalog.setVisible(true);
                        _catalogButton.setText(ItemPalette.rbp.getString("HideCatalog"));
                    }
                }
        });
        _catalogButton.setToolTipText(ItemPalette.rbp.getString("ToolTipCatalog"));
        bottomPanel.add(_catalogButton);

        add(bottomPanel);
    }

    void hideCatalog() {
        _catalog.setVisible(false);
        _catalogButton.setText(ItemPalette.rbp.getString("ShowCatalog"));
    }

    void initBottomPanel() {
        _catalog = BackgroudCatalogPanel.makeDefaultCatalog();
        _catalog.setSize(_paletteFrame.getSize().width, _catalog.getPreferredSize().height);
        _catalog.setVisible(false);
        add(_catalog);
    }

    class BkdDragJLabel extends DragJLabel {
        public BkdDragJLabel(DataFlavor flavor) {
            super(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String url = ((NamedIcon)getIcon()).getURL();
            if (log.isDebugEnabled()) log.debug("DragJLabel.getTransferData url= "+url);
            PositionableLabel b = new PositionableLabel(NamedIcon.getIconByName(url), _editor);
            b.setPopupUtility(null);        // no text
            b.setPositionable(false);
            b.setShowTooltip(false);
            b.setLevel(Editor.BKG);
            return b;
        }
    }

    static class BackgroudCatalogPanel extends CatalogPanel {

        /**
        *  Display the icons in the preview panel
        */
        protected String setIcons() throws OutOfMemoryError {
            resetPanel();
            CatalogTreeNode node = getSelectedNode();
            if (node == null) {
                return null;
            }
            List<CatalogTreeLeaf> leaves = node.getLeaves();
            if (leaves == null) {
                return null;
            }
            int numCol = 1;
            while (numCol*numCol <leaves.size()) {
                numCol++;
            }
            if (numCol > 1) {
                numCol--;
            }
            int numRow = leaves.size()/numCol;
            int cnt = 0;
            boolean newCol = false;
            _noMemory = false;
            // VM launches another thread to run ImageFetcher.
            // This handler will catch memory exceptions from that thread
            Thread.setDefaultUncaughtExceptionHandler(new MemoryExceptionHandler());
            GridBagLayout gridbag = new GridBagLayout();
            _preview.setLayout(gridbag);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.CENTER;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridy = 0;
            c.gridx = -1;
            for (int i=0; i<leaves.size(); i++) {
                if (_noMemory) {
                    cnt++;
                    continue;
                }
                CatalogTreeLeaf leaf = leaves.get(i);
                NamedIcon icon = new NamedIcon(leaf.getPath(), leaf.getName());
                double scale = icon.reduceTo(50, 80, .15);
                if (_noMemory) {
                    continue;
                }
                if (c.gridx < numCol) {
                    c.gridx++;
                } else if (c.gridy < numRow) { //start next row
                    c.gridy++;
                    if (!newCol) {
                        c.gridx=0;
                    }
                } else if (!newCol) { // start new column
                    c.gridx++;
                    numCol++;
                    c.gridy = 0;
                    newCol = true;
                } else {  // start new row
                    c.gridy++;
                    numRow++;
                    c.gridx = 0;
                    newCol = false;
                }
                JLabel image = null;
                c.insets = new Insets(5, 5, 0, 0);
                try {
                    image = new DragJLabel(new DataFlavor(ImageIndexEditor.IconDataFlavorMime));
                } catch (java.lang.ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                    image = new JLabel();
                }
                image.setOpaque(true);
                image.setName(leaf.getName());
                image.setBackground(_currentBackground);
                image.setIcon(icon);
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                JPanel iPanel = new JPanel();
                iPanel.add(image);
                p.add(iPanel);
                JLabel nameLabel = new JLabel(leaf.getName());
                p.add(nameLabel);
                JLabel label = new JLabel(java.text.MessageFormat.format(ItemPalette.rb.getString("scale"),
                                    new Object[] {printDbl(scale,2)}));
                p.add(label);
                p.addMouseListener(this);

                gridbag.setConstraints(p, c);
                if (_noMemory) {
                    continue;
                }
                _preview.add(p);
                if (log.isDebugEnabled()) {
                    log.debug(leaf.getName()+" inserted at ("+c.gridx+", "+c.gridy+
                                  ") w= "+icon.getIconWidth()+", h= "+icon.getIconHeight());
                }
                cnt++;
            }
            c.gridy++;
            c.gridx++;
            JLabel bottom = new JLabel();
            gridbag.setConstraints(bottom, c);
            _preview.add(bottom);

            Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
            packParentFrame(this);
            return java.text.MessageFormat.format(ItemPalette.rb.getString("numImagesInNode"),
                                  new Object[] {node.getUserObject(),Integer.valueOf(leaves.size())});
        }
    }

    class ColorDialog extends JDialog implements ChangeListener {

        JColorChooser _chooser;
        Editor        _editor;
        JPanel        _preview;

        ColorDialog(Editor editor) {
            super(_paletteFrame, ItemPalette.rbp.getString("ColorChooser"), true);
            _editor = editor;

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(5,5));

            _chooser = new JColorChooser(editor.getTargetPanel().getBackground());
            _chooser.getSelectionModel().addChangeListener(this);
            _preview = new JPanel();
            _preview.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 4), 
                                                             ItemPalette.rbp.getString("PanelColor")));
            _preview.add (new JLabel(new NamedIcon("resources/logo.gif", "resources/logo.gif")), BorderLayout.CENTER);
            _chooser.setPreviewPanel(_preview);
            panel.add(_chooser, BorderLayout.CENTER);
            panel.add(makeDoneButtonPanel(), BorderLayout.SOUTH);

            setContentPane(panel);
            _preview.setBackground(_editor.getBackground());
            _preview.getParent().setBackground(_editor.getBackground());
            setSize(_paletteFrame.getSize().width, this.getPreferredSize().height);
            setLocationRelativeTo(_paletteFrame);
            pack();
            setVisible(true);
        }

        protected JPanel makeDoneButtonPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            JButton doneButton = new JButton(ItemPalette.rbp.getString("doneButton"));
            doneButton.addActionListener(new ActionListener() {
                    ColorDialog dialog;
                    public void actionPerformed(ActionEvent a) {
                        _editor.setBackgroundColor(_chooser.getColor());
                        dialog.dispose();
                    }
                    ActionListener init(ColorDialog d) {
                        dialog = d;
                        return this;
                    }
            }.init(this));
            panel.add(doneButton);

            JButton cancelButton = new JButton(ItemPalette.rbp.getString("cancelButton"));
            cancelButton.addActionListener(new ActionListener() {
                    ColorDialog dialog;
                    public void actionPerformed(ActionEvent a) {
                        dialog.dispose();
                    }
                    ActionListener init(ColorDialog d) {
                        dialog = d;
                        return this;
                    }
            }.init(this));
            panel.add(cancelButton);

            return panel;
        }
        public void stateChanged(ChangeEvent e) {
            _preview.setBackground(_chooser.getColor());
            _preview.getParent().setBackground(_chooser.getColor());
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BackgroundItemPanel.class.getName());
}
