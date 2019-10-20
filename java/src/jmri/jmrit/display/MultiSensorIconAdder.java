package jmri.jmrit.display;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.jmrit.catalog.CatalogTreeLeaf;
import jmri.jmrit.catalog.CatalogTreeNode;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a simple editor for creating a MultiSensorIcon object. Allows drops
 * from icons dragged from a Catalog preview pane. Also implements dragging a
 * row from the Sensor table to be dropped on a Sensor label
 * <p>
 * To work right, the MultiSensorIcon needs to have all images the same size,
 * but this is not enforced here. It should be. -Done 6/16/09
 *
 * @author Bob Jacobsen Copyright (c) 2007
 * @author Pete Cressman Copyright (c) 2009
 *
 */
public class MultiSensorIconAdder extends IconAdder {

    JRadioButton _updown;
    JRadioButton _rightleft;

    HashMap<String, NamedBeanHandle<Sensor>> _sensorMap = new HashMap<>();

    public static final String NamedBeanFlavorMime = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=jmri.NamedBean";

    public MultiSensorIconAdder() {
        super();
    }

    public MultiSensorIconAdder(String type) {
        super(type);
    }

    @Override
    public void reset() {
        _sensorMap = new HashMap<>();
        super.reset();
    }

    /**
     * Build iconMap and orderArray from user's choice of defaults (override).
     */
    @Override
    protected void makeIcons(CatalogTreeNode n) {
        if (log.isDebugEnabled()) {
            log.debug("makeIcons from node= {}, numChildren= {}, NumLeaves= {}",
                    n.toString(), n.getChildCount(), n.getNumLeaves());
        }
        _iconMap = new HashMap<>(10);
        _iconOrderList = new ArrayList<>();
        ArrayList<CatalogTreeLeaf> list = n.getLeaves();
        // adjust order of icons
        for (int i = list.size() - 1; i >= 0; i--) {
            CatalogTreeLeaf leaf = list.get(i);
            String name = leaf.getName();
            String path = leaf.getPath();
            if ("BeanStateInconsistent".equals(name)) {
                setIcon(0, name, new NamedIcon(path, path));
            } else if ("BeanStateUnknown".equals(name)) {
                setIcon(1, name, new NamedIcon(path, path));
            } else if ("SensorStateInactive".equals(name)) {
                setIcon(2, name, new NamedIcon(path, path));
            } else {
                int k = Character.digit(name.charAt(name.length() - 1), 10);
                setIcon(k + 3, name, new NamedIcon(path, path));
            }
        }
    }

    void setMultiIcon(List<MultiSensorIcon.Entry> icons) {
        for (int i = 0; i < icons.size(); i++) {
            MultiSensorIcon.Entry entry = icons.get(i);
            String label = "MultiSensorPosition " + i;
            String url = entry.icon.getURL();
            if (url != null) {
                setIcon(i + 3, label, url);
                _sensorMap.put(label, entry.namedSensor);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("setMultiIcon: Size: sensors= {}, icons= {}",
                    _sensorMap.size(), _iconMap.size());
        }
    }

    /**
     * First look for a table selection to set the sensor. If not, then look to
     * change the icon image (super).
     */
    @Override
    protected void doIconPanel() {
        if (log.isDebugEnabled()) {
            log.debug("doIconPanel: Sizes: _iconMap= {} _iconOrderList.size()= {}, _sensorMap.size()= {}",
                    _iconMap.size(), _iconOrderList.size(), _sensorMap.size());
        }
        Dimension dim = null;
        JPanel rowPanel = null;
        int cnt = 0;
        for (int i = 3; i < _iconOrderList.size(); i++) {
            if (rowPanel == null) {
                rowPanel = new JPanel();
                rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
                rowPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
            }
            String key = _iconOrderList.get(i);
            if (key.equals("placeHolder")) {
                continue;
            }
            JPanel p1 = new JPanel();
            p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
            String label = MessageFormat.format(Bundle.getMessage("MultiSensorPosition"),
                    new Object[]{cnt + 1});
            p1.add(new JLabel(label));
            p1.add(_iconMap.get(key));

            JPanel p2 = new JPanel();
            JButton delete = new JButton(Bundle.getMessage("ButtonDelete"));
            ActionListener action = new ActionListener() {
                String key;

                @Override
                public void actionPerformed(ActionEvent a) {
                    delete(key);
                }

                ActionListener init(String k) {
                    key = k;
                    return this;
                }
            }.init(key);
            delete.addActionListener(action);
            p2.add(delete);

            JPanel p3 = new DropPanel();
            p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
            JLabel k = new JLabel(key);
            k.setName(key);
            k.setVisible(false);
            p3.add(k);
            JPanel p4 = new JPanel();
            p4.add(new JLabel(Bundle.getMessage("BeanNameSensor")));
            p3.add(p4);
            p4 = new JPanel();
            NamedBeanHandle<Sensor> sensor = _sensorMap.get(key);
            String name = Bundle.getMessage("notSet");
            Color color = Color.RED;
            if (sensor != null) {
                name = sensor.getName();
                /*name = sensor.getUserName();
                 if (name == null)  {
                 name = sensor.getSystemName();
                 }*/
                color = Color.BLACK;
            }
            p4.setBorder(BorderFactory.createLineBorder(color));
            p4.add(new JLabel(name));
            p4.setMaximumSize(p4.getPreferredSize());
            p3.add(p4);
            JPanel p13 = new JPanel();
            p13.setLayout(new BoxLayout(p13, BoxLayout.X_AXIS));
            p13.add(p3);
            p13.add(p1);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(p13);
            panel.add(p2);
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            rowPanel.add(panel);
            rowPanel.add(Box.createHorizontalStrut(STRUT_SIZE));

            cnt++;
            if ((cnt % 3) == 0) {
                _iconPanel.add(rowPanel);
                rowPanel = null;
            }
            dim = panel.getPreferredSize();
        }
        while ((cnt % 3) != 0) {
            Objects.requireNonNull(rowPanel, "should not have found rowPanel null here");
            rowPanel.add(Box.createRigidArea(dim));
            cnt++;
        }
        if (rowPanel != null) {
            _iconPanel.add(rowPanel);
            _iconPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        }
        rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.add(Box.createHorizontalStrut(STRUT_SIZE));
        for (int i = 0; i < 3; i++) {
            String key = _iconOrderList.get(i);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(new JLabel(Bundle.getMessage(key)));
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

    @Override
    public void complete(ActionListener addIconAction, boolean changeIcon,
            boolean addToTable, boolean update) {
        ButtonGroup group = new ButtonGroup();
        _updown = new JRadioButton(Bundle.getMessage("UpDown"));
        _rightleft = new JRadioButton(Bundle.getMessage("RightLeft"));
        _rightleft.setSelected(true);
        group.add(_updown);
        group.add(_rightleft);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(_updown);
        p.add(_rightleft);
        p.add(Box.createHorizontalStrut(STRUT_SIZE));
        JButton addIcon = new JButton(Bundle.getMessage("AddMultiSensorIcon"));
        addIcon.addActionListener((ActionEvent e) -> {
            addIcon();
        });
        p.add(addIcon);
        this.add(p);
        this.add(new JSeparator());
        super.complete(addIconAction, changeIcon, addToTable, update);
        _table.setDragEnabled(true);
        _table.setTransferHandler(new ExportHandler());
        valueChanged(null);
    }

    class ExportHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        @Override
        public Transferable createTransferable(JComponent c) {
            return new TransferableNamedBean();
        }

        @Override
        public void exportDone(JComponent c, Transferable t, int action) {
        }
    }

    class TransferableNamedBean implements Transferable {

        DataFlavor dataFlavor;

        TransferableNamedBean() {
            try {
                dataFlavor = new DataFlavor(NamedBeanFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                log.error("Unable to find class", cnfe);
            }
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            //log.debug("TransferableNamedBean.getTransferDataFlavors");
            return new DataFlavor[]{dataFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            //log.debug("TransferableNamedBean.isDataFlavorSupported");
            return dataFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            log.debug("TransferableNamedBean.getTransferData");
            if (isDataFlavorSupported(flavor)) {
                return getTableSelection();
            }
            return null;
        }
    }

    private void addIcon() {
        int index = _iconOrderList.size();
        String path = "resources/icons/misc/X-red.gif"; //"resources/icons/USS/plate/levers/l-vertical.gif";
        String label = "MultiSensorPosition " + (index - 3);
        super.setIcon(index, label, new NamedIcon(path, path));
        valueChanged(null);
        if (!_update) {
            _defaultIcons.addLeaf(label, path);
            InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
        }
        makeIconPanel(!_update);
        this.invalidate();
    }

    /**
     * Activate Add to Panel button when all icons are assigned sensors.
     *
     * @param e the triggering event
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (_addButton == null) {
            return;
        }
        if (_sensorMap.size() == (_iconMap.size() - 3)) {
            _addButton.setEnabled(true);
            _addButton.setToolTipText(null);
            //checkIconSizes();
        } else {
            _addButton.setEnabled(false);
            _addButton.setToolTipText(Bundle.getMessage("ToolTipAssignSensors"));
        }
    }

    void delete(String key) {
        _iconMap.remove(key);
        _sensorMap.remove(key);
        int index = _iconOrderList.indexOf(key);
        _iconOrderList.remove(key);
        if (!_update) {
            _defaultIcons.deleteLeaves(key);
            //  update labels
            for (int k = index; k < _iconOrderList.size(); k++) {
                String label = _iconOrderList.get(k);
                ArrayList<CatalogTreeLeaf> leaves = _defaultIcons.getLeaves(label);
                for (int i = 0; i < leaves.size(); i++) {
                    String path = leaves.get(i).getPath();
                    _defaultIcons.deleteLeaves(label);
                    _defaultIcons.addLeaf("MultiSensorPosition " + (k - 3), path);
                    // break;
                }
            }
            InstanceManager.getDefault(CatalogTreeManager.class).indexChanged(true);
        }
        makeIconPanel(!_update);
    }

    /**
     * Get a new NamedIcon object for your own use. see NamedIcon
     * getIcon(String key) in super.
     *
     * @param index of key
     * @return Unique object
     */
    public NamedIcon getIcon(int index) {
        if (index >= _iconOrderList.size()) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    Bundle.getMessage("NoIconAt"), index - 2),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return (NamedIcon) _iconMap.get(_iconOrderList.get(index)).getIcon();
    }

    /**
     * Get a Sensor object for your own use. see NamedIcon getIcon(String
     * key) in super.
     *
     * @param index of key
     * @return Unique object
     */
    public NamedBeanHandle<Sensor> getSensor(int index) {
        if (index >= _iconOrderList.size()) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    Bundle.getMessage("NoSensorAt"), index - 2),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return _sensorMap.get(_iconOrderList.get(index));
    }

    public boolean getUpDown() {
        return _updown.isSelected();
    }

    private boolean putSensor(String key, Sensor sensor) {
        String name = sensor.getDisplayName();
        log.debug("putSensor: key= {} sensor= {}", key, name);
        Iterator<NamedBeanHandle<Sensor>> iter = _sensorMap.values().iterator();
        while (iter.hasNext()) {
            if (name.equals(iter.next().getName())) {
                JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                        Bundle.getMessage("DupSensorName"),
                        new Object[]{name}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        _sensorMap.put(key, jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, sensor));
        return true;
    }

    /**
     * Enable the active MultiSensor icons to receive dragged icons.
     */
    class DropPanel extends JPanel implements DropTargetListener {

        DataFlavor dataFlavor;

        DropPanel() {
            try {
                dataFlavor = new DataFlavor(NamedBeanFlavorMime);
            } catch (ClassNotFoundException cnfe) {
                log.error("Class not found.", cnfe);
            }
            new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
            //log.debug("DropPanel ctor");
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            //log.debug("DropPanel.dragOver");
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                if (e.isDataFlavorSupported(dataFlavor)) {
                    Sensor sensor = (Sensor) tr.getTransferData(dataFlavor);
                    if (sensor != null) {
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        DropTarget target = (DropTarget) e.getSource();
                        JPanel panel = (JPanel) target.getComponent();
                        JComponent comp = (JLabel) panel.getComponent(0);
                        if (putSensor(comp.getName(), sensor)) {
                            makeIconPanel(!_update);
                        }
                        e.dropComplete(true);
                        if (log.isDebugEnabled()) {
                            log.debug("DropPanel.drop COMPLETED for {}", comp.getName());
                        }
                    } else {
                        log.debug("DropPanel.drop REJECTED!");
                        e.rejectDrop();
                    }
                }
            } catch (IOException | UnsupportedFlavorException ioe) {
                log.debug("DropPanel.drop REJECTED!");
                e.rejectDrop();
            }
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(MultiSensorIconAdder.class);

}
