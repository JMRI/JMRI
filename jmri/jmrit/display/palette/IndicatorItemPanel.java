package jmri.jmrit.display.palette;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
//import java.awt.dnd.*;
import java.io.IOException;

import javax.swing.*;

//import jmri.Sensor;
import jmri.util.JmriJFrame;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.catalog.DragJLabel;

/**
*  ItemPanel for for Indicating track blocks 
*/
public class IndicatorItemPanel extends FamilyItemPanel {

    private JButton         _updateButton;
    private DetectionPanel  _detectPanel;
    private JPanel          _dndIconPanel;
    private boolean         _update;
    private JPanel          _trainIdPanel;
    private JCheckBox   _showTrainName;

    /**
    * Constructor for plain icons and backgrounds
    */
    public IndicatorItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame,  type, family, editor);
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
        _detectPanel= new DetectionPanel(this);
        add(_detectPanel);
        initIconFamiliesPanel();
        _trainIdPanel = makeTrainIdPanel();
        _iconFamilyPanel.add(_trainIdPanel, 0);
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
    public void init(ActionListener doneAction, Hashtable<String, NamedIcon> iconMap) {
        if (iconMap!=null) {
            checkCurrentMap(iconMap);   // is map in families?, does user want to add it? etc
        }
        _update = true;
        _bottom2Panel = makeBottom2Panel();
        _bottom1Panel = makeBottom3Panel(doneAction, makeBottom1Panel());
        _detectPanel= new DetectionPanel(this);
        add(_detectPanel);
        initIconFamiliesPanel();
        _trainIdPanel = makeTrainIdPanel();
        _iconFamilyPanel.add(_trainIdPanel, 0);
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(_bottom1Panel);
        bottomPanel.add(_bottom2Panel);
        add(bottomPanel);
        if (log.isDebugEnabled()) log.debug("init done for family "+_family);
    }

    // add update buttons to  bottom1Panel
    protected JPanel makeBottom3Panel(ActionListener doneAction, JPanel bottom1Panel) {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(bottom1Panel);
        JPanel updatePanel = new JPanel();
        _updateButton = new JButton(ItemPalette.rbp.getString("updateButton"));
        _updateButton.addActionListener(doneAction);
        updatePanel.add(_updateButton);
        bottomPanel.add(updatePanel);
        return bottomPanel;
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

    public void dispose() {
        if (_detectPanel!=null) {
            _detectPanel.dispose();
        }
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
    
    protected void setFamily(String family) {
        super.setFamily(family);
        if (_dndIconPanel!=null) {
            remove(_dndIconPanel);
        }
        makeDndIconPanel();        // need to have family identified  before calling
        add(_dndIconPanel, 1);
    }

    /*************** pseudo inheritance to DetectionPanel *******************/

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

    /*******************************************************/
/*
    public Hashtable<String, NamedIcon> getIconMap() {
        Hashtable<String, NamedIcon> iconMap = ItemPalette.getIconMap(_itemType, _family);
        if (iconMap==null) {
            JOptionPane.showMessageDialog(_paletteFrame, 
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                   ItemPalette.rbp.getString(_itemType), _family), 
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return iconMap;
    }
*/
    public class IndicatorDragJLabel extends DragJLabel {

        public IndicatorDragJLabel(DataFlavor flavor) {
            super(flavor);
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
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
                        java.text.MessageFormat.format(ItemPalette.rbp.getString("FamilyNotFound"), 
                                                       ItemPalette.rbp.getString(_itemType), _family), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            IndicatorTrackIcon t = new IndicatorTrackIcon(_editor);

            t.setOccBlock(_detectPanel.getOccBlock());
            t.setOccSensor(_detectPanel.getOccSensor());
            t.setErrSensor(_detectPanel.getErrSensor());                
            t.setShowTrain(_showTrainName.isSelected());
            t.setFamily(_family);

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

