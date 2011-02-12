package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.datatransfer.Transferable;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.*;

//import jmri.Sensor;
//import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.util.JmriJFrame;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.*;
import jmri.jmrit.picker.PickListModel;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*/
public class IndicatorTOItemPanel extends TableItemPanel {

    final static String[] STATUS_KEYS = {"ClearTrack", "OccupiedTrack", "PositionTrack", 
                            "AllocatedTrack", "DontUseTrack", "ErrorTrack"};

    private DetectionPanel  _detectPanel;
    private JPanel          _trainIdPanel;
    private JPanel          _tablePanel;
    private JCheckBox   _showTrainName;
    protected Hashtable<String, Hashtable<String, NamedIcon>> _iconGroupsMap;
    protected Hashtable<String, Hashtable<String, NamedIcon>> _updateGroupsMap;
    
    /**
    * Constructor for all table types.  When item is a bean, the itemType is the name key 
    * for the item in jmri.NamedBeanBundle.properties
    */
    public IndicatorTOItemPanel(JmriJFrame parentFrame, String type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    /**
    * Init for creation
    * _bottom1Panel and _bottom2Panel alternate visibility in bottomPanel depending on
    * whether icon families exist.  They are made first because they are referenced in
    * initIconFamiliesPanel()
    */
    public void init() {
        _detectPanel = new DetectionPanel(this);   // Create panel before calling initIconFamiliesPanel()
        add(_detectPanel);
        super.init();
    }

    /**
    * Init for update of existing indicator turnout
    * _bottom3Panel has "Update Panel" button put into _bottom1Panel
    */
    public void initUpdate(ActionListener doneAction, Hashtable<String, Hashtable<String, NamedIcon>> iconMaps) {
        _detectPanel= new DetectionPanel(this);
        add(_detectPanel);
        checkCurrentMaps(iconMaps);   // is map in families?, does user want to add it? etc
        super.init(doneAction, null);
    }

    /**
    * iconMap is existing map of the icon.  Check whether map is one of the
    * families. if so, return.  if not, does user want to add it to families?
    * if so, add.  If not, save for return when updated.
    */
    protected void checkCurrentMaps(Hashtable<String, Hashtable<String, NamedIcon>> iconMaps) {
        _updateGroupsMap = iconMaps;
        if (_family!=null && _family.trim().length()>0) {
            Hashtable<String, Hashtable<String, NamedIcon>> map = ItemPalette.getLevel4FamilyMaps(_itemType).get(_family);
            if (map!=null) {
                return;     // Must assume no family names were changed
            }
        }
        int result = JOptionPane.showConfirmDialog(_paletteFrame,ItemPalette.rbp.getString("NoFamilyName"),
                ItemPalette.rb.getString("questionTitle"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result==JOptionPane.NO_OPTION) {
            _updateWithSameMap = true;
            return;
        }
        if (_family!=null && _family.trim().length()>0) {
            if (ItemPalette.addLevel4Family(_paletteFrame, _itemType, _family, iconMaps)) {
                return;
            }
        }
        do {
            _family = JOptionPane.showInputDialog(_paletteFrame, ItemPalette.rbp.getString("EnterFamilyName"),
                    ItemPalette.rb.getString("questionTitle"), JOptionPane.QUESTION_MESSAGE);
            if (_family==null || _family.trim().length()==0) {
                // bail out
                _updateWithSameMap = true;
                return;
            }
        } while (!ItemPalette.addLevel4Family(_paletteFrame, _itemType, _family, iconMaps));
        
    }

    protected JPanel initTablePanel(PickListModel model, Editor editor) {
        _tablePanel = super.initTablePanel(model, editor);
        _table.setTransferHandler(new DnDIndicatorTOHandler(editor));
        return _tablePanel;
    }
    
    public void dispose() {
        if (_detectPanel!=null) {
            _detectPanel.dispose();
        }
    }

    private JPanel makeTrainIdPanel() {
        JPanel panel = new JPanel();
        _showTrainName = new JCheckBox(ItemPalette.rbp.getString("ShowTrainName"));
        _showTrainName.setToolTipText(ItemPalette.rbp.getString("ToolTipShowTrainName"));
        JPanel p = new JPanel();
        p.add(_showTrainName);
        p.setToolTipText(ItemPalette.rbp.getString("ToolTipShowTrainName"));
        panel.add(p);
        return panel;
    }

    /**
    *  CENTER Panel
    */
    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));

        Hashtable <String, Hashtable<String, Hashtable<String, NamedIcon>>> families = 
                            ItemPalette.getLevel4FamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            ButtonGroup group = new ButtonGroup();
            Iterator <String> it = families.keySet().iterator();
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
            String family = null;
            JRadioButton button = null;
            while (it.hasNext()) {
                family = it.next();
                button = new JRadioButton(family);
                button.addActionListener(new ActionListener() {
                        String family;
                        public void actionPerformed(ActionEvent e) {
                            setFamily(family);
                        }
                        ActionListener init(String f) {
                            family = f;
                            return this;
                        }
                    }.init(family));
                if (family.equals(_family)) {
                    button.setSelected(true);
                }
                buttonPanel.add(button);
                group.add(button);
            }
            if (_family==null && _updateGroupsMap==null) {
                _family = family;              // let last familiy be the selected one
                button.setSelected(true);
            }
            _iconFamilyPanel.add(_detectPanel);

            if (_updateGroupsMap==null) {
                _iconGroupsMap = families.get(_family);
            } else {
                _iconGroupsMap = _updateGroupsMap;
            }
            _iconPanel = new JPanel();
            if (_iconGroupsMap==null) {
                JOptionPane.showMessageDialog(_paletteFrame, 
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                       ItemPalette.rbp.getString(_itemType), _family), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                _family = null;
            } else {
                addIcons2Panel(_iconGroupsMap);  // need to have family iconMap identified before calling
            }
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

            _trainIdPanel = makeTrainIdPanel();
            _iconFamilyPanel.add(_trainIdPanel);

            _iconFamilyPanel.add(panel);
            _bottom1Panel.setVisible(true);
            _bottom2Panel.setVisible(false);
            _detectPanel.setVisible(true);
            _tablePanel.setVisible(true);
            _trainIdPanel.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                                                   ItemPalette.rbp.getString(_itemType)), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            _bottom1Panel.setVisible(false);
            _bottom2Panel.setVisible(true);
            _detectPanel.setVisible(false);
            _tablePanel.setVisible(false);
            _trainIdPanel.setVisible(false);
            createNewFamily();
        }
        add(_iconFamilyPanel, 1);
        if (log.isDebugEnabled()) log.debug("initIconFamiliesPanel done");
    }

    /**
    * Make matrix of icons - each row has a button to change icons
    */
    protected void addIcons2Panel(Hashtable<String, Hashtable<String, NamedIcon>> map) {
        GridBagLayout gridbag = new GridBagLayout();
        _iconPanel.setLayout(gridbag);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridy = -1;

        Iterator<Entry<String, Hashtable<String, NamedIcon>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            c.gridx = 0;
            c.gridy++;

            Entry<String, Hashtable<String, NamedIcon>> entry = it.next();
            String stateName = entry.getKey();
            JPanel panel = new JPanel();
            panel.add(new JLabel(ItemPalette.convertText(stateName)));
            gridbag.setConstraints(panel, c);
            _iconPanel.add(panel);
            c.gridx++;
            Hashtable<String, NamedIcon> iconMap = entry.getValue();
            Iterator<Entry<String, NamedIcon>> iter = iconMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, NamedIcon> ent = iter.next();
                String borderName = ItemPalette.convertText(ent.getKey());
                NamedIcon icon = new NamedIcon(ent.getValue());    // make copy for possible reduction
                icon.reduceTo(100, 100, 0.2);
                panel = new JPanel();
                panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                                 borderName));
                //if (log.isDebugEnabled()) log.debug("addIcons2Panel: "+borderName+" icon at ("
                //                                    +c.gridx+","+c.gridy+") width= "+icon.getIconWidth()+
                //                                    " height= "+icon.getIconHeight());
                JLabel image = new JLabel(icon);
                if (icon.getIconWidth()<1 || icon.getIconHeight()<1) {
                    image.setText(ItemPalette.rbp.getString("invisibleIcon"));
                    image.setForeground(Color.lightGray);
                }
                panel.add(image);
                int width = Math.max(85, panel.getPreferredSize().width);
                panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
                gridbag.setConstraints(panel, c);
                _iconPanel.add(panel);
                c.gridx++;
            }
            panel = new JPanel();
            JButton button = new JButton(ItemPalette.rbp.getString("EditIcons"));
            button.addActionListener(new ActionListener() {
                    String key;
                    public void actionPerformed(ActionEvent a) {
                        openEditDialog(key);
                    }
                    ActionListener init(String k) {
                        key = k;
                        return this;
                    }
            }.init(stateName));
            button.setToolTipText(ItemPalette.rbp.getString("ToolTipEditIcons"));
            panel.add(button);
            gridbag.setConstraints(panel, c);
            _iconPanel.add(panel);
            //if (log.isDebugEnabled()) log.debug("addIcons2Panel: row "+c.gridy+" has "+iconMap.size()+" icons");
        }
    }

    protected JPanel makeBottom2Panel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        _familyName = new JTextField();
        buttonPanel.add(ItemPalette.makeBannerPanel("IconSetName", _familyName));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton newFamilyButton = new JButton(ItemPalette.rbp.getString("createNewFamily"));
        newFamilyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    //check text
                    String family = _familyName.getText();
                    if (ItemPalette.addLevel4Family(_paletteFrame, _itemType, family, _iconGroupsMap)) {
                        updateFamiliesPanel();
                        setFamily(family);
                    }
                }
            });
        newFamilyButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddFamily"));
        panel.add(newFamilyButton);

        JButton cancelButton = new JButton(ItemPalette.rbp.getString("cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    updateFamiliesPanel();
                }
        });
        panel.add(cancelButton);
        buttonPanel.add(panel);
        return buttonPanel;
    }

    protected JPanel makeBottom1Panel() {
        JPanel buttonPanel = new JPanel();
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        JButton addFamilyButton = new JButton(ItemPalette.rbp.getString("addNewFamily"));
        addFamilyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    createNewFamily();
                }
            });
        addFamilyButton.setToolTipText(ItemPalette.rbp.getString("ToolTipAddFamily"));
        panel1.add(addFamilyButton);
        JButton deleteButton = new JButton(ItemPalette.rbp.getString("deleteFamily"));
        deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    ItemPalette.removeLevel4IconMap(_itemType, _family, null);
                    _family = null;
                    updateFamiliesPanel();
                }
            });
        deleteButton.setToolTipText(ItemPalette.rbp.getString("ToolTipDeleteFamily"));
        panel1.add(deleteButton);

        _showIconsButton = new JButton(ItemPalette.rbp.getString("ShowIcons"));
        _showIconsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (_iconPanel.isVisible()) {
                        hideIcons();
                    } else {
                        _iconPanel.setVisible(true);
                        _detectPanel.setVisible(false);
                        _tablePanel.setVisible(false);
                        _trainIdPanel.setVisible(false);
                        _showIconsButton.setText(ItemPalette.rbp.getString("HideIcons"));
                    }
                    _paletteFrame.pack();
                }
        });
        _showIconsButton.setToolTipText(ItemPalette.rbp.getString("ToolTipShowIcons"));
        panel1.add(_showIconsButton);
        buttonPanel.add(panel1);
        return buttonPanel;
    }

    protected void hideIcons() {
        _tablePanel.setVisible(true);
        _detectPanel.setVisible(true);
        _trainIdPanel.setVisible(true);
        super.hideIcons();
    }

    void createNewFamily() {
        removeIconFamiliesPanel();
        Hashtable<String, Hashtable<String, NamedIcon>> groupsMap =
                         new Hashtable<String, Hashtable<String, NamedIcon>>();
        for (int i=0; i<STATUS_KEYS.length; i++) {
            groupsMap.put(STATUS_KEYS[i], makeNewIconMap("Turnout"));
        }
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        _iconPanel = new JPanel();
        addIcons2Panel(groupsMap);
        _iconFamilyPanel.add(_iconPanel);
        _iconPanel.setVisible(true);
        _bottom1Panel.setVisible(false);
        _bottom2Panel.setVisible(true);
        _detectPanel.setVisible(false);
        add(_iconFamilyPanel, 1);
        reset();
        validate();
        repaint();
        _paletteFrame.pack();
        _iconGroupsMap = groupsMap;
    }

    protected void setFamily(String family) {
        _family = family;
        if (log.isDebugEnabled()) log.debug("setFamily: for type \""+_itemType+"\", family \""+family+"\"");
        _iconFamilyPanel.remove(_iconPanel);
        _iconPanel = new JPanel();
        Hashtable<String, Hashtable<String, NamedIcon>> iconMaps =
                     ItemPalette.getLevel4Family(_itemType, _family);
        addIcons2Panel(iconMaps);
        _updateWithSameMap = false;     // not using saved update map
        _iconFamilyPanel.add(_iconPanel, 0);
        hideIcons();
    }

    protected void openEditDialog(String key) {
        if (log.isDebugEnabled()) log.debug("openEditDialog for family \""+_family+"\" and \""+key+"\"");
        new IndicatorTOIconDialog(_itemType, _family, this, key);
    }

    /****************** pseudo inheritance *********************/

    public boolean getShowTrainName() {
        return _showTrainName.isSelected();
    }

    public void setShowTrainName(boolean show) {
        _showTrainName.setSelected(show);
    }

    public String getErrSensor() {
        return _detectPanel.getErrSensor();
    }

    public void setErrSensor(String name) {
        _detectPanel.setErrSensor(name);
    }

    public String getOccSensor() {
        return _detectPanel.getOccSensor();
    }

    public String getOccBlock() {
        return _detectPanel.getOccBlock();
    }

    public void setOccDetector(String name) {
        _detectPanel.setOccDetector(name);
    }
    
    public ArrayList<String> getPaths() {
        return _detectPanel.getPaths();
    }

    public void setPaths(ArrayList<String> paths) {
        _detectPanel.setPaths(paths);
    }

    public Hashtable <String, Hashtable<String, NamedIcon>> getIconMaps() {
        Hashtable<String, Hashtable<String, NamedIcon>> iconMaps;
        if (_updateWithSameMap) {
            iconMaps = _updateGroupsMap;
        } else {
            iconMaps = ItemPalette.getLevel4FamilyMaps(_itemType).get(_family);
        }
        if (iconMaps==null) {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                   ItemPalette.rbp.getString(_itemType), _family), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
        }
        return iconMaps;
    }

    /**
    * Export a Positionable item from PickListTable 
    */
    public class DnDIndicatorTOHandler extends DnDTableItemHandler {

        public DnDIndicatorTOHandler(Editor editor) {
            super(editor);
        }
        
        public Transferable createPositionableDnD(JTable table) {
            NamedBean bean = getNamedBean(table);
            if (bean==null) {
                return null;
            }
            Hashtable <String, Hashtable <String, NamedIcon>> iconMap = getIconMaps();

            IndicatorTurnoutIcon t = new IndicatorTurnoutIcon(_editor);

            t.setOccBlock(_detectPanel.getOccBlock());
            t.setOccSensor(_detectPanel.getOccSensor());
            t.setErrSensor(_detectPanel.getErrSensor());                
            t.setShowTrain(_showTrainName.isSelected());
            t.setTurnout(bean.getSystemName());
            t.setFamily(_family);

            Iterator<Entry<String, Hashtable<String, NamedIcon>>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Hashtable<String, NamedIcon>> entry = it.next();
                String status = entry.getKey();
                Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, NamedIcon> ent = iter.next();
                    t.setIcon(status, ent.getKey(), ent.getValue());
                }
            }
            t.setLevel(Editor.TURNOUTS);
            return new PositionableDnD(t, bean.getDisplayName());
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTOItemPanel.class.getName());
}
