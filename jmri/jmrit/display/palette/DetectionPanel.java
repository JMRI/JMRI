
package jmri.jmrit.display.palette;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListSelectionModel;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.picker.PickPanel;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OPath;

/**
*  Panel for Occupancy and Error detection,  
*/
public class DetectionPanel extends JPanel implements ListSelectionListener {


    private JTextField  _occDetectorName = new JTextField();   // can be either a Sensor or OBlock name
    private JTextField  _errSensorName = new JTextField();
    private JFrame      _pickFrame;
    private JButton     _openPicklistButton;
    private JCheckBox   _showTrainName;
    private OBlock      _block;
    private JPanel      _blockPathPanel;
    private JList       _pathList;
    private ItemPanel   _parent;

    /**
    */
    public DetectionPanel(ItemPanel parent) {
        super();
        _parent = parent;
        _occDetectorName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkDetection();
            }
        });
        _occDetectorName.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                checkDetection();
            }
        });
        _errSensorName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkErrorSensor();
            }
        });                        
        _errSensorName.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                checkErrorSensor();
            }
        });
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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
                }
        });
        _openPicklistButton.setToolTipText(ItemPalette.rbp.getString("ToolTipPickLists"));
        JPanel p = new JPanel();
        p.add(_openPicklistButton);
        p.setToolTipText(ItemPalette.rbp.getString("ToolTipPickLists"));
        panel.add(p);
        add(panel);

        _blockPathPanel = new JPanel();
        _blockPathPanel.setLayout(new BoxLayout(_blockPathPanel, BoxLayout.Y_AXIS));
        _showTrainName = new JCheckBox(ItemPalette.rbp.getString("ShowTrainName"));
        _showTrainName.setToolTipText(ItemPalette.rbp.getString("ToolTipShowTrainName"));
        p = new JPanel();
        p.add(_showTrainName);
        p.setToolTipText(ItemPalette.rbp.getString("ToolTipShowTrainName"));
        p = new JPanel();
        p.add(new JLabel(ItemPalette.rbp.getString("SelectPathIcons")));
        _blockPathPanel.add(p);
        _pathList = new JList();
        _pathList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _pathList.addListSelectionListener(this);
        _blockPathPanel.add(new JScrollPane(_pathList));
        JButton clearButton = new JButton(ItemPalette.rbp.getString("ClearSelection"));
        clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    clearListSelection();
                }
        });
        clearButton.setToolTipText(ItemPalette.rbp.getString("ToolTipClearSelection"));
        _blockPathPanel.add(clearButton);
        _blockPathPanel.setVisible(false);
        _blockPathPanel.setToolTipText(ItemPalette.rbp.getString("ToolTipSelectPathIcons"));
        add(_blockPathPanel);
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

    public void dispose() {
        if (_pickFrame!=null) {
            _pickFrame.dispose();
            _pickFrame = null;
        }
    }

    /****************** Getters & Setters ***************************/

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
                JOptionPane.showMessageDialog(null, ItemPalette.rbp.getString("InvalidErrSensor"), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            }
        }
        return null;
    }

    public void setErrSensor(String name) {
        _errSensorName.setText(name);
    }

    public String getOccSensor() {
        String name = _occDetectorName.getText();
        if (name!=null && name.trim().length()>0) {
            if (InstanceManager.sensorManagerInstance().getSensor(name)!=null) {
                return name;
            }
        }
        return null;
    }

    public String getOccBlock() {
        String name = _occDetectorName.getText();
        if (name!=null && name.trim().length()>0) {
            if (InstanceManager.oBlockManagerInstance().getOBlock(name)!=null) {
                return name;
            }
        }
        return null;
    }

    /**
    * Name of either Sensor or OBlock for detection
    */
    public void setOccDetector(String name) {
        _occDetectorName.setText(name);
        checkDetection(); 
    }

    public ArrayList<String> getPaths() {
        ArrayList<String> paths = new ArrayList<String>();
        Object[] p = _pathList.getSelectedValues();
        if (log.isDebugEnabled()) log.debug("getPaths size="+p.length);
        for (int i=0; i<p.length; i++) {
            paths.add( ((OPath)p[i]).getName());
        }
        if (log.isDebugEnabled()) log.debug("getPaths length="+p.length+", size="+paths.size());
        return paths;
    }

    public void setPaths(ArrayList<String> iconPath) {
        if (iconPath==null || _block==null) {
            return;
        }
        List<Path> paths = _block.getPaths();
        int[] indices = new int[iconPath.size()];
        int lastIdx = 0;
        for (int k=0; k<iconPath.size(); k++) {
            for (int i=0; i<paths.size(); i++) {
                OPath p = (OPath) paths.get(i);
                if (iconPath.get(k).equals(p.getName()) ) {
                    indices[lastIdx++] = i;
                    if (log.isDebugEnabled()) log.debug("setPaths index="+i);
                }
            }
        }
        if (lastIdx>0) {
            _pathList.setSelectedIndices(indices);
        }
    }

    /*********************************************/

    private void checkDetection() { 
        String name = _occDetectorName.getText();
        if (name!=null && name.trim().length()>0) {
            OBlock block = InstanceManager.oBlockManagerInstance().getOBlock(name);
            if (block!=null) {
                if (block.equals(_block)) {
                    return;
                }
                makePathList(block);
            } else {
                Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(name);
                if (sensor==null) {
                    JOptionPane.showMessageDialog(_parent._paletteFrame, java.text.MessageFormat.format(
                        ItemPalette.rbp.getString("InvalidOccDetector"), name), 
                            ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                    _occDetectorName.setText(null);
                }
                _blockPathPanel.setVisible(false);
                _block = null;
            }
        } else {
            _blockPathPanel.setVisible(false);
            _block = null;
        }
        _parent._paletteFrame.pack();
    }

    protected void makePathList(OBlock block) {
        if (_blockPathPanel.getComponentCount()>1) {
            _blockPathPanel.remove(1);
            _pathList.removeListSelectionListener(this);
        }
        _block = block;
        _pathList = new JList(new PathListModel());
        _pathList.addListSelectionListener(this);
        _pathList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _pathList.setToolTipText(ItemPalette.rbp.getString("ToolTipSelectPathIcons"));
        _blockPathPanel.add(new JScrollPane(_pathList), 1);
        _blockPathPanel.setVisible(true);
        _blockPathPanel.validate();
    }

    //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SIC_INNER_SHOULD_BE_STATIC")
    // passing just the path list instead of using _block saves a call 
    class PathListModel extends DefaultListModel {
        List<Path> _paths;
        PathListModel() {
            _paths = _block.getPaths();
        }
        public int getSize() {
//            if (log.isDebugEnabled()) log.debug("PathListModel getSize()="+_paths.size());
            return _paths.size();
        }
        public Object getElementAt(int index) {
//            if (log.isDebugEnabled()) log.debug("PathListModel getElementAt("+index+")="+_paths.get(index));
            return _paths.get(index);
        }
    }

    private void clearListSelection() {
        _pathList.clearSelection();
    }

    private void checkErrorSensor() { 
        String name = _errSensorName.getText();
        if (name!=null && name.trim().length()>0) {
            Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(name);
            if (sensor== null) {
                JOptionPane.showMessageDialog(_parent._paletteFrame, java.text.MessageFormat.format(
                    ItemPalette.rbp.getString("InvalidErrSensor"), name), 
                        ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
                _errSensorName.setText(null);
                return;
            }
        }
    }

    public int getRowCount() {
        return _block.getPaths().size() + 1;
    }

    /**
    *  When a 
    */
    public void valueChanged(ListSelectionEvent e) {
//        if (log.isDebugEnabled()) log.debug("valueChanged");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DetectionPanel.class.getName());
}

