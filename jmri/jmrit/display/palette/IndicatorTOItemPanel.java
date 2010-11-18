package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.datatransfer.Transferable; 
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.*;

import jmri.Sensor;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.util.JmriJFrame;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.display.*;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.picker.PickPanel;
import jmri.jmrit.logix.OBlock;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*/
public class IndicatorTOItemPanel extends TableItemPanel {

    JTextField  _occDetectorName = new JTextField();   // can be either a Sensor or OBlock name
    JTextField  _errSensorName = new JTextField();
    JFrame      _pickFrame;
    JButton     _openPicklistButton;
    JCheckBox   _showTrainName;


    protected Hashtable<String, Hashtable<String, NamedIcon>> _iconGroupsMap;
    JPanel _sensorPanel;
    
    /**
    * Constructor for all table types.  When item is a bean, the itemType is the name key 
    * for the item in jmri.NamedBeanBundle.properties
    */
    public IndicatorTOItemPanel(JmriJFrame parentFrame, String  itemType, PickListModel model, Editor editor) {
        super(parentFrame,  itemType, model, editor);
    }

    /**
    * Init for creation
    * _bottom1Panel and _bottom2Panel alternate visibility in bottomPanel depending on
    * whether icon families exist.  They are made first because they are referenced in
    * initIconFamiliesPanel()
    */
    public void init() {
        _sensorPanel = initSensorPanel();   // Create panel before calling initIconFamiliesPanel()
        super.init();
    }

    /**
    * Init for update of existing indicator turnout
    * _bottom3Panel has "Update Panel" button put into _bottom1Panel
    */
    public void init(ActionListener doneAction) {
        _sensorPanel = initSensorPanel();   // Create panel before calling initIconFamiliesPanel()
        super.init(doneAction);
    }

    protected void initTablePanel(PickListModel model, Editor editor) {
        super.initTablePanel(model, editor);
        _table.setTransferHandler(new DnDIndicatorTOHandler(editor));
    }
    /**
    * 
    */
    protected JPanel initSensorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(makeSensorPanel(_occDetectorName, "OccupancySensor", "ToolTipOccupancySensor"));
        panel.add(makeSensorPanel(_errSensorName, "ErrorSensor", "ToolTipErrorSensor"));
        _openPicklistButton = new JButton(ItemPalette.rbp.getString("OpenPicklist"));
        _openPicklistButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (_pickFrame==null) {
                        openPickList();
                    } else {
                        closePickList();
                    }
                    _paletteFrame.pack();
                }
        });
        _openPicklistButton.setToolTipText(ItemPalette.rbp.getString("ToolTipPickLists"));
        JPanel p = new JPanel();
        p.add(_openPicklistButton);
        panel.add(p);
        _showTrainName = new JCheckBox(ItemPalette.rbp.getString("ShowTrainName"));
        p = new JPanel();
        p.add(_showTrainName);
        panel.add(p);
        return panel;
    }

    JPanel makeSensorPanel(JTextField field, String text, String toolTip) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(ItemPalette.rbp.getString(text));
        panel.add(label);
        java.awt.Dimension dim = field.getPreferredSize();
        dim.width = 500;
        field.setMaximumSize(dim);
        dim.width = 200;
        field.setMinimumSize(dim);
        field.setColumns(20);
        field.setDragEnabled(true);
        field.setTransferHandler(new jmri.util.DnDStringImportHandler());
        label.setToolTipText(ItemPalette.rbp.getString(toolTip));
        field.setToolTipText(ItemPalette.rbp.getString(toolTip));
        panel.setToolTipText(ItemPalette.rbp.getString(toolTip));
        panel.add(field);
        return panel;
    }

    void openPickList() {
        _pickFrame = new JFrame();
        PickListModel[] models = { PickListModel.oBlockPickModelInstance(),
                                    PickListModel.sensorPickModelInstance()
                                 };
        _pickFrame.setContentPane(new PickPanel(models));
        _pickFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    closePickList();                   
                }
            });
        _pickFrame.setLocationRelativeTo(this);
        _pickFrame.toFront();
        _pickFrame.setVisible(true);
        _pickFrame.pack();
        _openPicklistButton.setText(ItemPalette.rbp.getString("ClosePicklist"));
    }

    void closePickList() {
        _pickFrame.dispose();
        _pickFrame = null;
        _openPicklistButton.setText(ItemPalette.rbp.getString("OpenPicklist"));
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
            _iconFamilyPanel.add(_sensorPanel);

            makeIconPanel();        // need to have family identified  before calling
            _iconFamilyPanel.add(_iconPanel);
            _iconPanel.setVisible(false);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            String txt = java.text.MessageFormat.format(ItemPalette.rbp.getString("IconFamilies"), _itemType);
            panel.add(new JLabel(txt));
            panel.add(buttonPanel);
            _iconFamilyPanel.add(panel);
            _bottom1Panel.setVisible(true);
            _bottom2Panel.setVisible(false);
            _sensorPanel.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), _itemType), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            _bottom1Panel.setVisible(false);
            _bottom2Panel.setVisible(true);
            _sensorPanel.setVisible(false);
            createNewFamily();
        }
        add(_iconFamilyPanel, 1);
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
        if (_iconGroupsMap==null) {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), _itemType), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            addIcons2Panel(_iconGroupsMap);
        }
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
                if (log.isDebugEnabled()) log.debug("addIcons2Panel: "+borderName+" icon at ("
                                                    +c.gridx+","+c.gridy+") width= "+icon.getIconWidth()+
                                                    " height= "+icon.getIconHeight());
                panel.add(new JLabel(icon));
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
            if (log.isDebugEnabled()) log.debug("addIcons2Panel: row "+c.gridy+" has "+iconMap.size()+" icons");
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
                    if (family==null || family.length()==0) {
                        JOptionPane.showMessageDialog(_paletteFrame, 
                                ItemPalette.rbp.getString("EnterFamilyName"), 
                                ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Iterator <String> it = ItemPalette.getLevel4FamilyMaps(_itemType).keySet().iterator();
                    while (it.hasNext()) {
                       if (family.equals(it.next())) {
                           JOptionPane.showMessageDialog(_paletteFrame,
                                java.text.MessageFormat.format(ItemPalette.rbp.getString("DuplicateFamilyName"), 
							    new Object[] { family, _itemType }), 
                                ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                           return;
                       }
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
                    } else {
                        _iconPanel.setVisible(true);
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

    void createNewFamily() {
        removeIconFamiliesPanel();
        _iconGroupsMap = new Hashtable<String, Hashtable<String, NamedIcon>>();
        _iconGroupsMap.put("ClearTrack", makeNewIconMap("Turnout"));
        _iconGroupsMap.put("OccupiedTrack", makeNewIconMap("Turnout"));
        _iconGroupsMap.put("PositionTrack", makeNewIconMap("Turnout"));
        _iconGroupsMap.put("AllocatedTrack", makeNewIconMap("Turnout"));
        _iconGroupsMap.put("DontUseTrack", makeNewIconMap("Turnout"));
        _iconGroupsMap.put("ErrorTrack", makeNewIconMap("Turnout"));

        _iconFamilyPanel = new JPanel();
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        _iconPanel = new JPanel();
        addIcons2Panel(_iconGroupsMap);
        _iconFamilyPanel.add(_iconPanel);
        _iconPanel.setVisible(true);
        _bottom1Panel.setVisible(false);
        _bottom2Panel.setVisible(true);
        _sensorPanel.setVisible(false);
        add(_iconFamilyPanel, 1);
        reset();
        validate();
        repaint();
        _paletteFrame.pack();
    }

    protected void openEditDialog(String key) {
        if (log.isDebugEnabled()) log.debug("openEditDialog for family \""+_family+"\" for \""+key+"\"");
        new IndicatorTOIconDialog(_itemType, _family, this, key);
    }

    /****************** getters & setters *********************/

    public boolean getShowTrainName() {
        return _showTrainName.isSelected();
    }

    public void setShowTrainName(boolean show) {
        _showTrainName.setSelected(show);
    }

    public String getErrSensor() {
        String name = _errSensorName.getText();
        if (name!=null && name.trim().length()>0) {
            Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(name);
            if (sensor!= null) {
                return name;                
            } else {
                JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("InvalidErrSensor"), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            }
        }
        return null;
    }

    public void setErrSensor(String name) {
        _errSensorName.setText(name);
    }

    public String getOccSensor() {
        if (!detectorIsOBlock()) {
            return _occDetectorName.getText();
        }
        return null;
    }

    public String getOccBlock() {
        if (detectorIsOBlock()) {
            return _occDetectorName.getText();
        }
        return null;
    }

    /**
    * Name of either Sensor or OBlock for detection
    */
    public void setOccDetector(String name) {
        _occDetectorName.setText(name);
    }

    public void setSelection(NamedBean bean) {
        int row = _model.getIndexOf(bean);
        _table.addRowSelectionInterval(row, row);
        _scrollPane.getVerticalScrollBar().setValue(row*ROW_HEIGHT);
    }

    public Hashtable <String, Hashtable<String, NamedIcon>> getIconMaps() {
        Hashtable <String, Hashtable <String, NamedIcon>> iconMap = ItemPalette.getLevel4FamilyMaps(_itemType).get(_family);
        if (iconMap==null) {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), _itemType), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return iconMap;
    }

    public boolean detectorIsOBlock() {
        String name = _occDetectorName.getText();
        if (name!=null && name.trim().length()>0) {
            OBlock block = InstanceManager.oBlockManagerInstance().getOBlock(name);
            if (block!=null) {
                return true;
            } else {
                Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(name);
                if (sensor!= null) {
                    return false;                
                } else {
                    JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("InvalidOccDetector"), 
                            ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        _occDetectorName.setText(null);
        // allow no detector
        return true;
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
            Hashtable <String, Hashtable <String, NamedIcon>> iconMap = ItemPalette.getLevel4FamilyMaps(_itemType).get(_family);
            if (iconMap==null) {
                JOptionPane.showMessageDialog(_paletteFrame, 
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), _itemType), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            PickListModel model = (PickListModel)table.getModel();
            NamedBean bean = model.getBeanAt(row);

            IndicatorTurnoutIcon t = new IndicatorTurnoutIcon(_editor);

            if (detectorIsOBlock()) {
                t.setOccBlock(_occDetectorName.getText());
                t.setOccSensor(null);
            } else {
                t.setOccBlock(null);
                t.setOccSensor(_occDetectorName.getText());
            }
            t.setErrSensor(getErrSensor());                
            t.setShowTrain(_showTrainName.isSelected());
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
