package jmri.jmrit.display.palette;
/*
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.*;
*/
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
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.picker.PickPanel;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.catalog.DragJLabel;

/**
*  ItemPanel for for plain icons and backgrounds 
*/
public class IndicatorItemPanel extends FamilyItemPanel {

    JTextField _occDetectorName = new JTextField();   // can be either a Sensor or OBlock name
    JTextField _errSensorName = new JTextField();
    JFrame _pickFrame;
    JButton _openPicklistButton;

    protected JPanel    _dndIconPanel;

    /**
    * Constructor for plain icons and backgrounds
    */
    public IndicatorItemPanel(ItemPalette parentFrame, String  itemType, Editor editor) {
        super(parentFrame,  itemType, editor);
        setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
    }

    public void init() {
        _bottom1Panel = makeBottom1Panel();
        _bottom2Panel = makeBottom2Panel();          // SOUTH Panel
        initSensorPanel();
        initIconFamiliesPanel();
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        add(bottomPanel);
        if (log.isDebugEnabled()) log.debug("init done for family "+_family);
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
        _iconFamilyPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
        makeDndIconPanel();
        add(_dndIconPanel, 1);
    }

    protected void makeDndIconPanel() {
        _dndIconPanel = new JPanel();
        _dndIconPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
        Hashtable<String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
        if (iconMap!=null) {
            NamedIcon ic = iconMap.get("ClearTrack");
            if (ic!=null) {
                NamedIcon icon = new NamedIcon(ic);
               JPanel panel = new JPanel();
               String borderName = ItemPalette.convertText("ClearTrack");
               panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                                borderName));
               try {
                   JLabel label = new IndicatorDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
                   label.setIcon(icon);
                   label.setName(borderName);
                   panel.add(label);
                   label.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
               } catch (java.lang.ClassNotFoundException cnfe) {
                   cnfe.printStackTrace();
               }
               int width = Math.max(100, panel.getPreferredSize().width);
               panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
               panel.setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
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
                }
            } 
            if (block==null && sensor==null) {
                JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("InvalidOccDetector"), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
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
                JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("AllFamiliesDeleted"), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            IndicatorTrackIcon t = new IndicatorTrackIcon(_editor);
            Sensor sensor = null;
            OBlock block = null;
            String name = _occDetectorName.getText();
            if (name!=null && name.trim().length()>0) {
                block = InstanceManager.oBlockManagerInstance().getOBlock(name);
                if (block!=null) {
                    t.setOccBlock(name);
                } else {
                    sensor = InstanceManager.sensorManagerInstance().getSensor(name);
                    if (sensor!= null) {
                        t.setOccSensor(name);                
                    }
                }
            }
            if (block==null && sensor==null) {
                JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("InvalidOccDetector"), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            name = _errSensorName.getText();
            if (name!=null && name.trim().length()>0) {
                sensor = InstanceManager.sensorManagerInstance().getSensor(name);
                if (sensor!= null) {
                    t.setErrSensor(name);                
                } else {
                    JOptionPane.showMessageDialog(_paletteFrame, ItemPalette.rbp.getString("InvalidErrSensor"), 
                            ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                    return null;
                }
            }

            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                t.setIcon(entry.getKey(), new NamedIcon(entry.getValue()));
            }
            t.setDisplayLevel(Editor.TURNOUTS);
            return t;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorItemPanel.class.getName());
}

