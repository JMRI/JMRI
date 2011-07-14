package jmri.jmrit.display;

import jmri.jmrit.catalog.NamedIcon;
import jmri.NamedBeanHandle;
import jmri.NamedBean;
import jmri.Sensor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.event.ListSelectionEvent;
import javax.swing.TransferHandler;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;

/**
 * Provides a simple editor for creating a MultiSensorIcon object.  Allows
 * drops from icons dragged from a Catalog preview pane.  Also implements
 * dragging a row from the Sensor table to be dropped on a Sensor label
 * <p>
 * To work right, the MultiSensorIcon needs to have all
 * images the same size, but this is not enforced here. 
 * It should be.  -Done 6/16/09
 *
 * @author  Bob Jacobsen  Copyright (c) 2007
 * @author  Pete Cressman  Copyright (c) 2009
 * 
 */

public class MultiSensorIconAdder extends IconAdder {

    JRadioButton _updown;
    JRadioButton _rightleft;

    HashMap <String, NamedBeanHandle<Sensor>>_sensorMap = new HashMap <String, NamedBeanHandle<Sensor>>();
    int _lastIndex = 0;
    
    public static final String NamedBeanFlavorMime = DataFlavor.javaJVMLocalObjectMimeType +
               ";class=jmri.NamedBean";

    public MultiSensorIconAdder() {
        super();
    }

    public MultiSensorIconAdder(String type) {
        super(type);
    }

    public void reset() {
        _sensorMap = new HashMap <String, NamedBeanHandle<Sensor>>();
        _lastIndex = 0;
        super.reset();
    }
    
    /**
    *  Override.  First three calls MUST be 'inactive', 'inconsistent', 'unknown'.
    * Labeling of active sensors depends on the deletes and adds, if any. 
    *
    public void setIcon(int index, String label, String name) {
        String key = label;
        if (index > 2) {
            //make a unique name (multisensor has deletes so fix the key)
            key = "MultiSensorPosition " +_lastIndex++; 
        }

        if (log.isDebugEnabled()) {
            if (_order.size() > 0) {
                log.debug("SetIcon: order size= "+_order.size()+", _lastIndex= "+_lastIndex);
            }
        }
        super.setIcon(index, key, name);
        if (log.isDebugEnabled()) log.debug(key+" inserted at "+index);
    } */
    protected void setIcon(int order, String label, NamedIcon icon) {
        String key = label;
        if (order > 2) {
            //make a unique name (multisensor has deletes so fix the key)
            key = "MultiSensorPosition " +_lastIndex++; 
        }
        super.setIcon(order, key, icon);
        if (log.isDebugEnabled()) log.debug(key+" inserted at "+order);

    }

    void setMultiIcon(List <MultiSensorIcon.Entry> icons) {
        for (int i=0; i<icons.size(); i++) {
            MultiSensorIcon.Entry entry = icons.get(i);
            String label = "MultiSensorPosition " +_lastIndex; 
            super.setIcon(i+3, label, entry.icon.getURL());
            _sensorMap.put(label, entry.namedSensor);
        }
        if (log.isDebugEnabled()) log.debug("setMultiIcon: Size: sensors= "+_sensorMap.size()+
                                            ", icons= "+_iconMap.size());
    }

    /**
    *  Override.  First look for a table selection to set the sensor.
    *   If not, then look to change the icon image (super). 
    */
    @SuppressWarnings("null")
    public void makeIconPanel() {
        if (_iconPanel != null) {
            this.remove(_iconPanel);
        }
        if (log.isDebugEnabled()) log.debug("makeIconPanel: Sizes: _iconMap= "+_iconMap.size()+
                                            " _order.size()= "+_order.size()+
                                            ", _sensorMap.size()= "+_sensorMap.size());
        Dimension dim = null;
        _iconPanel = new JPanel();
        _iconPanel.setLayout(new BoxLayout(_iconPanel, BoxLayout.Y_AXIS));

        JPanel rowPanel = null;
        int cnt=0;
        for (int i=3; i<_order.size(); i++) {
            if (rowPanel == null) {
                rowPanel = new JPanel();
                rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
                rowPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            }
            String key = _order.get(i);
            JPanel p1 =new JPanel(); 
            p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
            String label = java.text.MessageFormat.format(rb.getString("MultiSensorPosition"),
                                                   new Object[] { Integer.valueOf(i-2) }); 
            p1.add(new JLabel(label));
            p1.add(_iconMap.get(key));

            JPanel p2 =new JPanel();
            JButton delete = new JButton(rb.getString("ButtonDeleteIcon"));
            ActionListener action = new ActionListener() {
                    int index;
                    public void actionPerformed(ActionEvent a) {
                        delete(index);
                    }
                    ActionListener init(int k) {
                        index = k;
                        return this;
                    }
            }.init(i);
            delete.addActionListener(action);
            p2.add(delete);

            JPanel p3 = new DropPanel();
            p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
            JLabel k = new JLabel(key);
            k.setName(key);
            k.setVisible(false);
            p3.add(k);
            JPanel p4 = new JPanel();
            p4.add(new JLabel(rb.getString("Sensor")));
            p3.add(p4);
            p4 = new JPanel();
            NamedBeanHandle<Sensor> sensor = _sensorMap.get(key);
            String name = rb.getString("notSet");
            java.awt.Color color = java.awt.Color.RED;
            if (sensor != null) {
                name = sensor.getName();
                /*name = sensor.getUserName();
                if (name == null)  {
                    name = sensor.getSystemName();
                }*/
                color = java.awt.Color.BLACK;
            }
            p4.setBorder(BorderFactory.createLineBorder(color));
            p4.add(new JLabel(name));
            p4.setMaximumSize(p4.getPreferredSize());
            p3.add(p4);
            if (log.isDebugEnabled()) log.debug("makeIconPanel: i= "+i+" label= "+label+
                                                ", key= "+key+", sensor = "+name);

            JPanel p13 =new JPanel();
            p13.setLayout(new BoxLayout(p13, BoxLayout.X_AXIS));
            p13.add(p3);
            p13.add(p1);

            JPanel panel =new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(p13);
            panel.add(p2);
            panel.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));

            rowPanel.add(panel);
            rowPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

            cnt++;
            if ((cnt%3)==0) {
                _iconPanel.add(rowPanel);
                rowPanel = null;
            }
            dim = panel.getPreferredSize();
        }
        while ((cnt%3)!=0)
        {
            try {
                rowPanel.add(Box.createRigidArea(dim));
                cnt++;
            } catch (NullPointerException npe) { /* never */}
        }
        if (rowPanel != null) {
            _iconPanel.add(rowPanel);
            _iconPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        }
        rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        for (int i=0; i<3; i++) {
            String key = _order.get(i);
            JPanel p =new JPanel(); 
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(new JLabel(rbean.getString(key)));
            p.add(_iconMap.get(key));
            rowPanel.add(p);
            rowPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        }
        _iconPanel.add(rowPanel);
        _iconPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        this.add(_iconPanel, 0);
        valueChanged(null);
        pack();
    }

    /**
    * Override
    *
    */
    public void complete(ActionListener addIconAction, boolean changeIcon,
                          boolean addToTable, boolean update) {
        ButtonGroup group = new ButtonGroup();
        _updown = new JRadioButton(rb.getString("UpDown"));
        _rightleft = new JRadioButton(rb.getString("RightLeft"));
        _rightleft.setSelected(true);
        group.add(_updown);
        group.add(_rightleft);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(_updown);
        p.add(_rightleft);
        p.add(Box.createHorizontalStrut(STRUT_SIZE));
        JButton addIcon = new JButton(rb.getString("AddMultiSensorIcon"));
        addIcon.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addIcon();
                }
           });
        p.add(addIcon);
        this.add(p);
        this.add(new JSeparator());
        super.complete(addIconAction, changeIcon, addToTable, update);
        _table.setDragEnabled(true);
        _table.setTransferHandler(new ExportHandler());
        valueChanged(null);
    }

    class ExportHandler extends TransferHandler{
        public int getSourceActions(JComponent c) {
            return COPY;
        }
        public Transferable createTransferable(JComponent c) {
            return new TransferableNamedBean();
        }
        public void exportDone(JComponent c, Transferable t, int action) {
        }
    }
    class TransferableNamedBean implements Transferable {
        DataFlavor dataFlavor;
        TransferableNamedBean() {
            try {
                dataFlavor = new DataFlavor(NamedBeanFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
        public DataFlavor[] getTransferDataFlavors() {
            //if (log.isDebugEnabled()) log.debug("TransferableNamedBean.getTransferDataFlavors ");
            return new DataFlavor[] { dataFlavor };
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            //if (log.isDebugEnabled()) log.debug("TransferableNamedBean.isDataFlavorSupported ");
            return dataFlavor.equals(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (log.isDebugEnabled()) log.debug("TransferableNamedBean.getTransferData ");
            if (isDataFlavorSupported(flavor)) {
                return getTableSelection();
            }
            return null;
        }
    }

    private void addIcon() {
        int index = _order.size();
        String name = "resources/icons/misc/X-red.gif"; //"resources/icons/USS/plate/levers/l-vertical.gif";
        super.setIcon(index, "MultiSensorPosition " +_lastIndex++, new NamedIcon(name, name));
        if (log.isDebugEnabled()) {
            if (_order.size() > 0) {
                log.debug("addIcon: order size= "+_order.size()+", _lastIndex= "+_lastIndex);
            }
        }
        valueChanged(null);
        makeIconPanel();
        this.invalidate();
    }

    /**
    *  Override.  Activate Add to Panel button when all icons are
    *  assigned sensors. 
    */
    public void valueChanged(ListSelectionEvent e) {
        if (_addButton == null) {
            return;
        }
        if (_sensorMap.size() == (_iconMap.size()-3)) {
            _addButton.setEnabled(true);
            _addButton.setToolTipText(null);
            //checkIconSizes();
        } else {
            _addButton.setEnabled(false);
            _addButton.setToolTipText(rb.getString("ToolTipAssignSensors"));
        }
    }

    /**
    *
    */
    void delete(int index) {
        String key = _order.get(index);
        if (log.isDebugEnabled()) log.debug("delete("+index+") key= "+key+" Sizes: _iconMap= "+_iconMap.size()
                  +", _sensorMap= "+_sensorMap.size()+", _order= "+_order.size());
        _iconMap.remove(key);
        _sensorMap.remove(key);
        _order.remove(index);
        _lastIndex--;
        makeIconPanel();
    }

    /**
    *  Override.  First look for a table selection to set the sensor.
    */
    public NamedBean getTableSelection() {
        int row = _table.getSelectedRow();
        if (row >= 0) {
            NamedBean b = _pickListModel.getBeanAt(row);
            _table.clearSelection();
            _addButton.setEnabled(false);
            _addButton.setToolTipText(null);
//            makeIconMap(b);
//            doIconPanel();
//            this.validate();
            if (log.isDebugEnabled()) log.debug("getTableSelection: row= "+row+", bean= "+b.getDisplayName());
            return b;
        } else if (log.isDebugEnabled()) log.debug("getTableSelection: row=0");
        return null;
    }

    /**
     * Returns a new NamedIcon object for your own use.
     * see NamedIcon getIcon(String key) in super
     * @param index of key
     * @return Unique object
     */
    public NamedIcon getIcon(int index) {
        if (index>=_order.size()) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                           rb.getString("NoIconAt"), index-2), 
                                           rb.getString("errorTitle"), 
                                           JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return (NamedIcon)_iconMap.get(_order.get(index)).getIcon();
    }

    /**
     * Returns a Sensor object for your own use.
     * see NamedIcon getIcon(String key) in super
     * @param index of key
     * @return Unique object
     */
    public NamedBeanHandle<Sensor> getSensor(int index) {
        if (index>=_order.size()) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                           rb.getString("NoSensorAt"), index-2), 
                                           rb.getString("errorTitle"), 
                                           JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return _sensorMap.get(_order.get(index));
    }

    public boolean getUpDown() {
        return _updown.isSelected();
    }
    private boolean putSensor(String key, Sensor sensor) {
        String name = sensor.getDisplayName();
        if (log.isDebugEnabled()) log.debug("putSensor: key= "+key+" sensor= "+name);
        Iterator<NamedBeanHandle<Sensor>> iter = _sensorMap.values().iterator();
        while (iter.hasNext()) {
            if (name.equals(iter.next().getName())) {
                JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                               rb.getString("DupSensorName"), 
                                               new Object[] {name}),
                                               rb.getString("errorTitle"), 
                                               JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        _sensorMap.put(key, jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, sensor));
        return true;
    }

    /**
    *  Enables the active MultiSensor icons to receive dragged icons
    */
    class DropPanel extends JPanel implements DropTargetListener {
        DataFlavor dataFlavor;
        DropPanel () {
            try {
                dataFlavor = new DataFlavor(NamedBeanFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            //if (log.isDebugEnabled()) log.debug("DropPanel ctor");
        }
        public void dragExit(DropTargetEvent dte) {
        }
        public void dragEnter(DropTargetDragEvent dtde) {
        }
        public void dragOver(DropTargetDragEvent dtde) {
            //if (log.isDebugEnabled()) log.debug("DropPanel.dragOver");
        }
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if(e.isDataFlavorSupported(dataFlavor)) {
                    Sensor sensor = (Sensor)tr.getTransferData(dataFlavor);
                    if (sensor !=null) {
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        DropTarget target = (DropTarget)e.getSource();
                        JPanel panel = (JPanel)target.getComponent();
                        JComponent comp = (JLabel)panel.getComponent(0);
                        if (putSensor(comp.getName(), sensor)) { 
                            makeIconPanel();
                        }
                        e.dropComplete(true);
                        if (log.isDebugEnabled()) log.debug("DropPanel.drop COMPLETED for "+
                                                             comp.getName());
                        return;
                    } else {
                        if (log.isDebugEnabled()) log.debug("DropPanel.drop REJECTED!");
                        e.rejectDrop();
                    }
                }
            } catch(IOException ioe) {
                if (log.isDebugEnabled()) log.debug("DropPanel.drop REJECTED!");
                e.rejectDrop();
            } catch(UnsupportedFlavorException ufe) {
                if (log.isDebugEnabled()) log.debug("DropPanel.drop REJECTED!");
                e.rejectDrop();
            }
        }
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MultiSensorIconAdder.class.getName());
}

