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
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.display.*;
import jmri.jmrit.picker.PickListModel;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*/
public class IndicatorTOItemPanel extends TableItemPanel {

    final static String[] STATUS_KEYS = {"ClearTrack", "OccupiedTrack", "PositionTrack", 
                            "AllocatedTrack", "DontUseTrack", "ErrorTrack"};

    private DetectionPanel  _detectPanel;
    private JPanel          _tablePanel;

    protected Hashtable<String, Hashtable<String, NamedIcon>> _iconGroupsMap;
    
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
    public void init(ActionListener doneAction) {
        _detectPanel= new DetectionPanel(this);
        add(_detectPanel);
        super.init(doneAction);
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

    /**
    *  CENTER Panel
    */
    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));

        Hashtable <String, Hashtable<String, Hashtable<String, NamedIcon>>> families = 
                            ItemPalette.getLevel4FamilyMaps(_itemType);
        if (families!=null && families.size()>0) {
            if (_family!=null) {
                _iconGroupsMap = families.get(_family);
                if (_iconGroupsMap==null) {
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
            if (_family==null) {
                _family = family;       // let last familiy be the selected one
                if (button != null) button.setSelected(true);
            }
            _iconFamilyPanel.add(_detectPanel);

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
            _detectPanel.setVisible(true);
            _tablePanel.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                                                   ItemPalette.rbp.getString(_itemType)), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            _bottom1Panel.setVisible(false);
            _bottom2Panel.setVisible(true);
            _detectPanel.setVisible(false);
            _tablePanel.setVisible(false);
            createNewFamily();
        }
        add(_iconFamilyPanel, 1);
        if (log.isDebugEnabled()) log.debug("initIconFamiliesPanel done");
    }

    protected void makeIconPanel() {
        _iconPanel = new JPanel();
        Hashtable <String, Hashtable<String, Hashtable<String, NamedIcon>>> families = 
                            ItemPalette.getLevel4FamilyMaps(_itemType);
        if (families==null) {
            if (log.isDebugEnabled()) log.debug("makeIconPanel: no families for type "+_itemType);
            return;
        }
        if (log.isDebugEnabled()) log.debug("makeIconPanel() _family= \""+_family+
                                            "\" families size= "+families.size());
        if (_family==null) {
            Iterator <String> it = families.keySet().iterator();
            while (it.hasNext()) {
                _family = it.next();
            }
        }
        _iconGroupsMap = families.get(_family);
        addIcons2Panel(_iconGroupsMap);
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
                if (icon==null || icon.getIconWidth()<1 || icon.getIconHeight()<1) {
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
            button.setToolTipText(ItemPalette.rbp.getString("ToolTipShowIcons"));
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
                    Iterator <String> iter = ItemPalette.getLevel4FamilyMaps(_itemType).keySet().iterator();
                    if (!ItemPalette.familyNameOK(_paletteFrame, _itemType, family, iter)) {
                        return;
                    }
                    ItemPalette.addLevel4Family(_itemType, family, _iconGroupsMap);
                    ImageIndexEditor.indexChanged(true);
                    updateFamiliesPanel();
                    setFamily(family);
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
        /*
        JButton editButton = new JButton(ItemPalette.rbp.getString("EditIcons"));
        editButton.addActionListener(new ActionListener() {
                String key;
                public void actionPerformed(ActionEvent a) {
                    openEditDialog(null);
                }
        });
        editButton.setToolTipText(ItemPalette.rbp.getString("ToolTipEditIcons"));
        panel1.add(editButton);
        */
        JButton deleteButton = new JButton(ItemPalette.rbp.getString("deleteFamily"));
        deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    ItemPalette.removeLevel4IconMap(_itemType, _family, null);
                    _family = null;
                    ImageIndexEditor.indexChanged(true);
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
                        _tablePanel.setVisible(true);
                        _detectPanel.setVisible(true);
                    } else {
                        _iconPanel.setVisible(true);
                        _detectPanel.setVisible(false);
                        _tablePanel.setVisible(false);
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
        _detectPanel.setVisible(true);
        _tablePanel.setVisible(true);
        super.hideIcons();
    }

    void createNewFamily() {
        removeIconFamiliesPanel();
        _iconGroupsMap = new Hashtable<String, Hashtable<String, NamedIcon>>();
        for (int i=0; i<STATUS_KEYS.length; i++) {
            _iconGroupsMap.put(STATUS_KEYS[i], makeNewIconMap("Turnout"));
        }
        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        _iconPanel = new JPanel();
        addIcons2Panel(_iconGroupsMap);
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
    }

    protected void openEditDialog(String key) {
        if (log.isDebugEnabled()) log.debug("openEditDialog for family \""+_family+"\" and \""+key+"\"");
        new IndicatorTOIconDialog(_itemType, _family, this, key);
    }

    /****************** pseudo inheritance *********************/

    public boolean getShowTrainName() {
        return _detectPanel.getShowTrainName();
    }

    public void setShowTrainName(boolean show) {
        _detectPanel.setShowTrainName(show);
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
        Hashtable <String, Hashtable <String, NamedIcon>> iconMap = ItemPalette.getLevel4FamilyMaps(_itemType).get(_family);
        if (iconMap==null) {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                   ItemPalette.rbp.getString(_itemType), _family), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return iconMap;
    }

    /**
    * Export a Positionable item from PickListTable 
    */
    public class DnDIndicatorTOHandler extends DnDTableItemHandler {

        public DnDIndicatorTOHandler(Editor editor) {
            super(editor);
        }
        
        public Transferable createPositionableDnD(JTable table) {
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            if (log.isDebugEnabled()) log.debug("TransferHandler.createTransferable: from table \""+_itemType+ "\" at ("
                                                +row+", "+col+") for data \""
                                                +table.getModel().getValueAt(row, col)+"\" in family \""+_family+"\".");
            if (col<0 || row<0) {
                return null;
            }            
            Hashtable <String, Hashtable <String, NamedIcon>> iconMap = 
                                            ItemPalette.getLevel4FamilyMaps(_itemType).get(_family);
            if (iconMap==null) {
                JOptionPane.showMessageDialog(_paletteFrame, 
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                       ItemPalette.rbp.getString(_itemType), _family), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            PickListModel model = (PickListModel)table.getModel();
            NamedBean bean = model.getBeanAt(row);

            IndicatorTurnoutIcon t = new IndicatorTurnoutIcon(_editor);

            t.setOccBlock(_detectPanel.getOccBlock());
            t.setOccSensor(_detectPanel.getOccSensor());
            t.setErrSensor(_detectPanel.getErrSensor());                
            t.setShowTrain(_detectPanel.getShowTrainName());
            t.setTurnout(bean.getSystemName());

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
