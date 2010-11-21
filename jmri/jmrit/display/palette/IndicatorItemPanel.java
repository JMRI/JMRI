package jmri.jmrit.display.palette;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
//import java.awt.dnd.*;
import java.io.IOException;

import javax.swing.*;

import jmri.Sensor;
import jmri.util.JmriJFrame;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.picker.PickPanel;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.catalog.DragJLabel;

/**
*  ItemPanel for for Indicating track blocks 
*/
public class IndicatorItemPanel extends FamilyItemPanel {

    protected JTextField  _occDetectorName = new JTextField();   // can be either a Sensor or OBlock name
    protected JTextField  _errSensorName = new JTextField();
    JFrame      _pickFrame;
    JButton     _openPicklistButton;
    JButton     _updateButton;
    JCheckBox   _showTrainName;

    protected JPanel    _dndIconPanel;
    boolean _update;

    /**
    * Constructor for plain icons and backgrounds
    */
    public IndicatorItemPanel(JmriJFrame parentFrame, String  itemType, Editor editor) {
        super(parentFrame,  itemType, editor);
//        setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
    }

    /**
    * Init for creation
    * _bottom1Panel and _bottom2Panel alternate visibility in bottomPanel depending on
    * whether icon families exist.  They are made first because they are referenced in
    * initIconFamiliesPanel()
    */
    public void init() {
        _update = false;
        _bottom1Panel = makeBottom1Panel();
        _bottom2Panel = makeBottom2Panel();
        initSensorPanel();
        initIconFamiliesPanel();
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        add(bottomPanel);
        if (log.isDebugEnabled()) log.debug("init done for family "+_family);
    }

    /**
    * Init for update of existing track block
    * _bottom3Panel has "Update Panel" button put into _bottom1Panel
    */
    public void init(ActionListener doneAction) {
        _update = true;
        _bottom1Panel = makeBottom1Panel();
        _bottom2Panel = makeBottom2Panel();
        _bottom1Panel = makeBottom3Panel(doneAction);
        initSensorPanel();
        initIconFamiliesPanel();
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        add(bottomPanel);
        if (log.isDebugEnabled()) log.debug("init done for family "+_family);
    }

    protected JPanel makeBottom3Panel(ActionListener doneAction) {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(_bottom1Panel);
        JPanel updatePanel = new JPanel();
        _updateButton = new JButton(ItemPalette.rbp.getString("updateButton"));
        _updateButton.addActionListener(doneAction);
        updatePanel.add(_updateButton);
        bottomPanel.add(updatePanel);
        return bottomPanel;
    }
    
    protected void initSensorPanel() {
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
        p.setToolTipText(ItemPalette.rbp.getString("ToolTipPickLists"));
        panel.add(p);
        _showTrainName = new JCheckBox(ItemPalette.rbp.getString("ShowTrainName"));
        _showTrainName.setToolTipText(ItemPalette.rbp.getString("ToolTipShowTrainName"));
        p = new JPanel();
        p.add(_showTrainName);
        p.setToolTipText(ItemPalette.rbp.getString("ToolTipShowTrainName"));
        panel.add(p);
        add(panel);
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

    protected void initIconFamiliesPanel() {
        super.initIconFamiliesPanel();
        if (_dndIconPanel!=null) {
            remove(_dndIconPanel);
        }
        if (_family==null) {
            return;
        }
        if (!_update) {
            _iconFamilyPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
        }
        makeDndIconPanel();
        add(_dndIconPanel, 1);
    }

    protected void makeDndIconPanel() {
        _dndIconPanel = new JPanel();
        if (!_update) {
            _dndIconPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
        }
        Hashtable<String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
        if (iconMap!=null) {
            NamedIcon ic = iconMap.get("ClearTrack");
            if (ic!=null) {
                NamedIcon icon = new NamedIcon(ic);
               JPanel panel = new JPanel();
               String borderName = ItemPalette.convertText("ClearTrack");
               panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                                borderName));
               JLabel label;
               if (_update) {
                   label = new JLabel();
               } else {
                   try {
                       label = new IndicatorDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
                       label.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
                   } catch (java.lang.ClassNotFoundException cnfe) {
                       cnfe.printStackTrace();
                       label = new JLabel();
                   }
               }
               label.setIcon(icon);
               label.setName(borderName);
               panel.add(label);
               int width = Math.max(100, panel.getPreferredSize().width);
               panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
               if (!_update) {
                   panel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
               }
               _dndIconPanel.add(panel);
               return;
            }
        }
        log.error("Item type \""+_itemType+"\", family \""+_family+"\"");
    }

    protected void openEditDialog() {
        if (log.isDebugEnabled()) log.debug("openEditDialog for family \""+_family+"\"");
        new IconDialog(_itemType, _family, this);
    }

    protected void setFamily(String family) {
        super.setFamily(family);
        remove(_dndIconPanel);
        makeDndIconPanel();        // need to have family identified  before calling
        add(_dndIconPanel, 1);
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

    public Hashtable<String, NamedIcon> getIconMap() {

        Hashtable<String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
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

    public class IndicatorDragJLabel extends DragJLabel {

        public IndicatorDragJLabel(DataFlavor flavor) {
            super(flavor);
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            // must have an occupancy sensor
            String name = _occDetectorName.getText();
            Sensor sensor = null;
            OBlock block = null;
            if (name!=null && name.trim().length()>0) {
                block = InstanceManager.oBlockManagerInstance().getOBlock(name);
                if (block==null) {
                    sensor = InstanceManager.sensorManagerInstance().getSensor(name);
                    if (sensor==null) {
                        JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("InvalidOccDetector"), 
                                ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                }
            } 
            name = _errSensorName.getText();
            if (name!=null && name.trim().length()>0) {
                sensor = InstanceManager.sensorManagerInstance().getSensor(name);
                if (sensor== null) {
                    JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("InvalidErrSensor"), 
                            ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
            return super.isDataFlavorSupported(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            if (log.isDebugEnabled()) log.debug("IndicatorDragJLabel.getTransferData");
            Hashtable <String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
            if (iconMap==null) {
                JOptionPane.showMessageDialog(_paletteFrame, 
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("AllFamiliesDeleted"), _itemType), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            IndicatorTrackIcon t = new IndicatorTrackIcon(_editor);
            if (detectorIsOBlock()) {
                t.setOccBlock(_occDetectorName.getText());
                t.setOccSensor(null);
            } else {
                t.setOccBlock(null);
                t.setOccSensor(_occDetectorName.getText());
            }
            t.setErrSensor(getErrSensor());                
            t.setShowTrain(_showTrainName.isSelected());

            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                t.setIcon(entry.getKey(), new NamedIcon(entry.getValue()));
            }
            t.setLevel(Editor.TURNOUTS);
            return t;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorItemPanel.class.getName());
}

