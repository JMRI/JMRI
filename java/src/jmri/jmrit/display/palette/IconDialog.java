package jmri.jmrit.display.palette;

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
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used when FamilyItemPanel classes add, modify or delete icon
 * families.
 *
 * Note this class cannot be used with super classes of FamilyItemPanel
 * (ItemPanel etc) since there are several casts to FamilyItemPanel.
 *
 * @author Pete Cressman Copyright (c) 2010, 2011
 */
public class IconDialog extends ItemDialog {

    protected FamilyItemPanel _parent;
    protected String _family;
    protected HashMap<String, NamedIcon> _iconMap;
    protected ImagePanel _iconPanel;
    protected CatalogPanel _catalog;

    /**
     * Constructor for existing family to change icons, add/delete icons, or to
     * delete the family.
     */
    public IconDialog(String type, String family, FamilyItemPanel parent, HashMap<String, NamedIcon> iconMap) {
        super(type, Bundle.getMessage("ShowIconsTitle", family));
        if (log.isDebugEnabled()) {
            log.debug("IconDialog ctor: for {}, family = {}", type, family);
        }
        _family = family;
        _parent = parent;
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));

        JPanel p = new JPanel();
        p.add(new JLabel(Bundle.getMessage("FamilyName", family)));
        panel.add(p);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        if (iconMap != null) {
            _iconMap = clone(iconMap);
            makeDoneButtonPanel(buttonPanel, "ButtonDone");
        } else {
            _iconMap = ItemPanel.makeNewIconMap(type);
            makeDoneButtonPanel(buttonPanel, "addNewFamily");
        }
        // null method for all except multisensor.
        makeAddIconButtonPanel(buttonPanel, "ToolTipAddPosition", "ToolTipDeletePosition");

        if (!(type.equals("IndicatorTO") || type.equals("MultiSensor"))) {
            ItemPanel.checkIconMap(type, _iconMap);
        }
        _iconPanel = makeIconPanel(_iconMap);
        panel.add(_iconPanel); // put icons above buttons
        panel.add(buttonPanel);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(panel);
        _catalog = CatalogPanel.makeDefaultCatalog();
        _catalog.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        p.add(_catalog);

        setContentPane(p);
        pack();
    }

    // for _parent to update background
    protected ImagePanel getIconPanel() {
        return _iconPanel;
    }

    // Only multiSensor adds and deletes icons 
    protected void makeAddIconButtonPanel(JPanel buttonPanel, String addTip, String deleteTip) {
    }

    /**
     * Action for both create new family and change existing family.
     * @return true if success
     */
    protected boolean doDoneAction() {
        _parent.reset();
//        checkIconSizes();
        _parent._currentIconMap = _iconMap;
        if (!_parent.isUpdate()) {  // don't touch palette's maps. just modify individual device icons
            ItemPalette.removeIconMap(_type, _family);
            return _parent.addFamily(_type, _family, _iconMap);
        }
        return true;
    }

    protected void makeDoneButtonPanel(JPanel buttonPanel, String text) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton doneButton = new JButton(Bundle.getMessage(text));
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                if (doDoneAction()) {
                    dispose();
                }
            }
        });
        panel.add(doneButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                dispose();
            }
        });
        panel.add(cancelButton);
        buttonPanel.add(panel);
    }

    protected ImagePanel makeIconPanel(HashMap<String, NamedIcon> iconMap) {
        if (iconMap == null) {
            log.error("iconMap is null for type {}, family {}", _type, _family);
            return null;
        }
        ImagePanel iconPanel = new ImagePanel();
        GridBagLayout gridbag = new GridBagLayout();
        iconPanel.setLayout(gridbag);
        iconPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        if (!_parent.isUpdate()) {
            iconPanel.setImage(_parent._backgrounds[_parent.getParentFrame().getPreviewBg()]);
        } else {
            iconPanel.setImage(_parent._backgrounds[0]);   //update always should be the panel background
        }

        int cnt = _iconMap.size();
        int numCol = cnt;
        if (cnt > 6) {
            numCol = 6;
        }
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        int gridwidth = cnt % numCol == 0 ? 1 : 2;
        c.gridwidth = gridwidth;
        c.gridheight = 1;
        c.gridx = -gridwidth;
        c.gridy = 0;

        if (log.isDebugEnabled()) {
            log.debug("makeIconPanel: for {} icons. gridwidth = {}", iconMap.size(), gridwidth);
        }
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
            panel.setOpaque(false);
            JLabel image = new DropJLabel(icon, _iconMap, _parent.isUpdate());
            image.setName(entry.getKey());
            if (icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
                image.setText(Bundle.getMessage("invisibleIcon"));
                image.setForeground(Color.lightGray);
            }
            image.setToolTipText(icon.getName());
            JPanel iPanel = new JPanel();
            iPanel.setOpaque(false);
            iPanel.add(image);

            c.gridx += gridwidth;
            if (c.gridx >= numCol * gridwidth) { //start next row
                c.gridy++;
                if (cnt < numCol) { // last row
                    JPanel p = new JPanel();
                    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                    panelWidth = panel.getPreferredSize().width;
                    p.add(Box.createHorizontalStrut(panelWidth));
                    c.gridx = 0;
                    c.gridwidth = 1;
                    gridbag.setConstraints(p, c);
                    iconPanel.add(p);
                    c.gridx = numCol - cnt;
                    c.gridwidth = gridwidth;
                    //c.fill = GridBagConstraints.NONE;
                } else {
                    c.gridx = 0;
                }
            }
            cnt--;

            panel.add(iPanel);
            JLabel label = new JLabel(java.text.MessageFormat.format(Bundle.getMessage("scale"),
                    new Object[]{CatalogPanel.printDbl(scale, 2)}));
            JPanel sPanel = new JPanel();
            sPanel.setOpaque(false);
            sPanel.add(label);
            panel.add(sPanel);
            panel.add(Box.createHorizontalStrut(20));
            gridbag.setConstraints(panel, c);
            iconPanel.add(panel);
        }
        if (panelWidth > 0) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(Box.createHorizontalStrut(panelWidth));
            c.gridx = numCol * gridwidth - 1;
            c.gridwidth = 1;
            gridbag.setConstraints(p, c);
            iconPanel.add(p);
        }
        return iconPanel;
    }

    protected HashMap<String, NamedIcon> clone(HashMap<String, NamedIcon> map) {
        HashMap<String, NamedIcon> clone = null;
        if (map != null) {
            clone = new HashMap<>();
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

    private final static Logger log = LoggerFactory.getLogger(IconDialog.class);
}
