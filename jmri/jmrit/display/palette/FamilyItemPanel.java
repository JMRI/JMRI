package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.*;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.util.JmriJFrame;

/**
*  ItemPanel for for plain icons and backgrounds 
*/
public abstract class FamilyItemPanel extends ItemPanel {

    protected JPanel    _iconFamilyPanel;
    protected JPanel    _iconPanel;         // panel contained in _iconFamilyPanel
    protected JPanel    _bottom1Panel;       // Typically _showIconsButton and editIconsButton 
    protected JPanel    _bottom2Panel;       // createIconFamilyButton - when all families deleted 
    JButton     _showIconsButton;
    protected JTextField _familyName;

    /**
    * Constructor types with multiple families and multiple icon families
    */
    public FamilyItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame,  type, family, editor);
    }

    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));

        Hashtable <String, Hashtable<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            if (_family!=null) {
                Hashtable<String, NamedIcon> iconMap = families.get(_family);
                if (iconMap==null) {
                    if (log.isDebugEnabled()) log.debug("makeIconPanel() iconMap==null for type \""+_itemType+"\", family \""+_family+"\"");
                    // Thread.dumpStack();
                    JOptionPane.showMessageDialog(_paletteFrame, 
                            java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"),
                                                           ItemPalette.rbp.getString(_itemType), _family), 
                            ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                    _family = null;
                }
            }
            ButtonGroup group = new ButtonGroup();
            Iterator <String> it = families.keySet().iterator();
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
            String family = null;
            JRadioButton button = null;
            while (it.hasNext()) {
                family = it.next();
                button = new JRadioButton(ItemPalette.convertText(family));
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
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            String txt = java.text.MessageFormat.format(ItemPalette.rbp.getString("IconFamiliesLabel"),
                                                        ItemPalette.rbp.getString(_itemType));
            JPanel p = new JPanel();
            p.add(new JLabel(txt));
            panel.add(p);
            panel.add(buttonPanel);
            _iconFamilyPanel.add(panel);
            _bottom1Panel.setVisible(true);
            _bottom2Panel.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                                                   ItemPalette.rbp.getString(_itemType)), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            _bottom1Panel.setVisible(false);
            _bottom2Panel.setVisible(true);
        }
        add(_iconFamilyPanel);
//        add(_iconFamilyPanel, 1);
    }

    protected void makeIconPanel() {
        if (log.isDebugEnabled()) log.debug("makeIconPanel() type= "+_itemType+" family= \""+_family+"\"");
        _iconPanel = new JPanel();
        if (_family==null) {
            Hashtable <String, Hashtable<String, NamedIcon>> families = ItemPalette.getFamilyMaps(_itemType);
            if (families!=null) {
                Iterator <String> it = families.keySet().iterator();
                while (it.hasNext()) {
                    _family = it.next();
                }
            }
        }
        if (_family!=null) {
            Hashtable<String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
            addIconsToPanel(iconMap);
        }
    }

    protected void addIconsToPanel(Hashtable<String, NamedIcon> iconMap) {
        if (iconMap==null) {
            log.error("iconMap is null for type "+_itemType+" family "+_family);
            return;
        }
        GridBagLayout gridbag = new GridBagLayout();
        _iconPanel.setLayout(gridbag);

        int numCol = 4;
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = -1;
        c.gridy = 0;

        int cnt = iconMap.size();
        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        while (it.hasNext()) {
           Entry<String, NamedIcon> entry = it.next();
           NamedIcon icon = new NamedIcon(entry.getValue());    // make copy for possible reduction
           icon.reduceTo(100, 100, 0.2);
           JPanel panel = new JPanel();
           String borderName = ItemPalette.convertText(entry.getKey());
           panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                            borderName));
           JLabel image = new JLabel(icon);
           if (icon.getIconWidth()<1 || icon.getIconHeight()<1) {
               image.setText(ItemPalette.rbp.getString("invisibleIcon"));
               image.setForeground(Color.lightGray);
           }
           panel.add(image);
           int width = Math.max(100, panel.getPreferredSize().width);
           panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
           c.gridx += 1;
           if (c.gridx >= numCol) { //start next row
               c.gridy++;
               c.gridx = 0;
               if (cnt < numCol-1) { // last row
                   JPanel p =  new JPanel();
                   p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                   p.add(Box.createHorizontalStrut(100));
                   gridbag.setConstraints(p, c);
                   //if (log.isDebugEnabled()) log.debug("makeIconPanel: gridx= "+c.gridx+" gridy= "+c.gridy);
                   _iconPanel.add(p);
                   c.gridx = 1;
               }
           }
           cnt--;
           gridbag.setConstraints(panel, c);
           _iconPanel.add(panel);
        }
    }

    protected JPanel makeBottom1Panel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        _showIconsButton = new JButton(ItemPalette.rbp.getString("ShowIcons"));
        _showIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (_iconPanel.isVisible()) {
                        hideIcons();
                    } else {
                        _iconPanel.setVisible(true);
                        _showIconsButton.setText(ItemPalette.rbp.getString("HideIcons"));
                    }
                    _paletteFrame.pack();
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
        return bottomPanel;
    }
    
    /**
    *  Replacement panel for _bottom1Panel when no icon families exist for _itemType 
    */
    protected JPanel makeBottom2Panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton newFamilyButton = new JButton(ItemPalette.rbp.getString("createNewFamily"));
        newFamilyButton.addActionListener(new ActionListener() {
                ItemPanel parent;
                public void actionPerformed(ActionEvent a) {
                    ItemPalette.createNewFamily(_itemType, parent);
                }
                ActionListener init(ItemPanel p) {
                    parent = p;
                    return this;
                }
        }.init(this));


        newFamilyButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddFamily"));
        panel.add(newFamilyButton);

        JButton cancelButton = new JButton(ItemPalette.rbp.getString("cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    updateFamiliesPanel();
                }
        });
        panel.add(cancelButton);
        return panel;
    }

    protected void hideIcons() {
        if (_iconPanel!=null) {
            _iconPanel.setVisible(false);
        }
        _showIconsButton.setText(ItemPalette.rbp.getString("ShowIcons"));
        _paletteFrame.pack();
    }

    protected void removeIconFamiliesPanel() {
        remove(_iconFamilyPanel);
    }
    protected void reset() {
        hideIcons();
    }
    

    protected void setFamily(String family) {
        _family = family;
        if (log.isDebugEnabled()) log.debug("setFamily: for type \""+_itemType+"\", family \""+family+"\"");
        _iconFamilyPanel.remove(_iconPanel);
        makeIconPanel();        // need to have family identified  before calling
        _iconFamilyPanel.add(_iconPanel, 0);
        hideIcons();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FamilyItemPanel.class.getName());
}

