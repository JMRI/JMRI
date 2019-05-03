/*============================================================================*
 * WARNING      This class contains automatically modified code.      WARNING *
 *                                                                            *
 * The method initComponents() and the variable declarations between the      *
 * "// Variables declaration - do not modify" and                             *
 * "// End of variables declaration" comments will be overwritten if modified *
 * by hand. Using the NetBeans IDE to edit this file is strongly recommended. *
 *                                                                            *
 * See http://jmri.org/help/en/html/doc/Technical/NetBeansGUIEditor.shtml for *
 * more information.                                                          *
 *============================================================================*/
package jmri.util.usb;

import java.awt.Image;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbPort;
import javax.usb.event.UsbServicesEvent;
import javax.usb.event.UsbServicesListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2017
 */
public class UsbBrowserPanel extends javax.swing.JPanel {

    private UsbTreeNode root;
    private final UsbDeviceTableModel deviceModel = new UsbDeviceTableModel();
    private final static Logger log = LoggerFactory.getLogger(UsbBrowserPanel.class);
    private transient final UsbServicesListener usbServicesListener = new UsbServicesListener() {
        @Override
        public void usbDeviceAttached(UsbServicesEvent use) {
            // subtler method to add usbDevice to tree
            UsbDevice usbDevice = use.getUsbDevice();
            UsbPort usbPort = usbDevice.getParentUsbPort();
            if (usbPort != null) {
                UsbDevice parentUsbDevice = usbPort.getUsbHub();
                UsbTreeNode parentNode = findNodeForDevice(root, parentUsbDevice);
                if (parentNode != null) {
                    UsbTreeNode node = new UsbTreeNode(usbDevice);
                    parentNode.add(node);
                    if (usbTree != null) {
                        TreePath selection = usbTree.getSelectionPath();
                        ((DefaultTreeModel) usbTree.getModel()).nodeChanged(parentNode);
                        // .nodeChanged(parent) isn't enough
                        ((DefaultTreeModel) usbTree.getModel()).reload(root);
                        usbTree.setSelectionPath(selection);
                    }
                    return;
                }
            }
            UsbTreeNode root = UsbBrowserPanel.this.getRootNode();
            root.removeAllChildren();
            UsbBrowserPanel.this.buildTree(root);
        }

        @Override
        public void usbDeviceDetached(UsbServicesEvent use) {
            // subtler method to remove usbDevice from tree
            UsbTreeNode root = UsbBrowserPanel.this.getRootNode();
            UsbDevice usbDevice = use.getUsbDevice();
            UsbTreeNode usbTreeNode = findNodeForDevice(root, usbDevice);
            if (usbTreeNode != null) {
                TreeNode parentTreeNode = usbTreeNode.getParent();
                usbTreeNode.removeFromParent();
                if (usbTree != null) {
                    TreePath selection = usbTree.getSelectionPath();
                    if (parentTreeNode != null) {
                        ((DefaultTreeModel) usbTree.getModel()).reload(parentTreeNode);
                    } else {
                        ((DefaultTreeModel) usbTree.getModel()).reload(root);
                    }
                    usbTree.setSelectionPath(selection);
                }
            } else {
                root.removeAllChildren();
                UsbBrowserPanel.this.buildTree(root);
            }
        }
    };
    private final TreeSelectionListener treeSelectionListener = (TreeSelectionEvent e) -> {
        UsbTreeNode node = (UsbTreeNode) this.usbTree.getLastSelectedPathComponent();
        if (node != null) {
            deviceModel.setNode(node);
        } else {
            this.usbTree.setSelectionPath(e.getNewLeadSelectionPath());
        }
    };

    /**
     * Create new UsbBrowserPanel.
     */
    public UsbBrowserPanel() {
        root = getRootNode();
        buildTree(root);
        if (root.getUserObject() != null) {
            try {
                UsbHostManager.getUsbServices().addUsbServicesListener(usbServicesListener);
            } catch (UsbException | SecurityException ex) {
                log.error("Unable to get root USB hub.", ex);
            }
        }
        initComponents();
        for (int i = 0; i < usbTree.getRowCount(); i++) {
            usbTree.expandRow(i);
        }
    }

    /*
     * Protected method to set and retrieve the root node.
     *
     * This is partially in place for testing purposes, as
     * this allows injection of a pre-defined root node by
     * by overriding this method.
     */
    protected UsbTreeNode getRootNode() {
       if(root == null ) {
          root = new UsbTreeNode();
       }
       return root;
    }

    private void buildTree(UsbTreeNode root) {
        Object userObject = root.getUserObject();
        if (userObject != null && ((UsbDevice) userObject).isUsbHub()) {
            UsbHub usbHub = (UsbHub) userObject;
            
            @SuppressWarnings("unchecked") // cast required by UsbHub API
            List<UsbDevice> usbDevices = usbHub.getAttachedUsbDevices();
            
            usbDevices.forEach((UsbDevice usbDevice) -> {
                UsbTreeNode node = new UsbTreeNode(usbDevice);
                log.debug("Adding {} to {}, depth: {}", node, root, node.getLevel());
                buildTree(node);
                root.add(node);
            });
        }
        // prevent NPE if called in constructor
        if (usbTree != null) {
            TreePath selection = usbTree.getSelectionPath();
            ((DefaultTreeModel) usbTree.getModel()).reload(root);
            usbTree.setSelectionPath(selection);
        }
    }

    /*
     * recursively search all children of root for usb device
     */
    private UsbTreeNode findNodeForDevice(UsbTreeNode root, UsbDevice usbDevice) {
        if (!root.isLeaf()) {
            for (int idx = 0; idx < root.getChildCount(); idx++) {
                TreeNode treeNode = root.getChildAt(idx);
                if (treeNode instanceof UsbTreeNode) {
                    UsbTreeNode usbTreeNode = (UsbTreeNode) treeNode;
                    UsbDevice tryUsbDevice = usbTreeNode.getUsbDevice();
                    log.debug("trying device: {}", tryUsbDevice);
                    if ((tryUsbDevice != null) && (tryUsbDevice == usbDevice)) {
                        return usbTreeNode;
                    }
                    UsbTreeNode result = findNodeForDevice(usbTreeNode, usbDevice);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public void dispose() {
        try {
            UsbHostManager.getUsbServices().removeUsbServicesListener(usbServicesListener);
        } catch (UsbException | SecurityException ex) {
            // silently ignore, since it was logged when this panel was constructed
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane(this.usbTree);
        usbTree = new javax.swing.JTree(this.root);
        jScrollPane2 = new javax.swing.JScrollPane();
        detailsTable = new javax.swing.JTable();

        jSplitPane1.setBorder(null);
        jSplitPane1.setResizeWeight(0.5);

        jScrollPane1.setBorder(null);

        usbTree.setCellRenderer(new UsbTreeCellRenderer());
        usbTree.setRootVisible(false);
        usbTree.setShowsRootHandles(true);
        usbTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        usbTree.addTreeSelectionListener(this.treeSelectionListener);
        jScrollPane1.setViewportView(usbTree);

        jSplitPane1.setLeftComponent(jScrollPane1);

        detailsTable.setModel(this.deviceModel);
        detailsTable.setCellSelectionEnabled(true);
        jScrollPane2.setViewportView(detailsTable);

        jSplitPane1.setRightComponent(jScrollPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable detailsTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTree usbTree;
    // End of variables declaration//GEN-END:variables

    protected static class UsbTreeNode extends DefaultMutableTreeNode {

        public UsbTreeNode() {
            this(null);
        }

        public UsbTreeNode(UsbDevice usbDevice) {
            super();

            if (usbDevice == null) {
                try {
                    userObject = UsbHostManager.getUsbServices().getRootUsbHub();
                    log.debug("Using root usbDevice {}", userObject);
                } catch (UsbException | SecurityException ex) {
                    log.error("Unable to get root USB hub.", ex);
                    userObject = null;
                }
            } else {
                log.debug("Description of {} is\n{}", usbDevice, usbDevice.getUsbDeviceDescriptor());
                userObject = usbDevice;
            }
        }

        public UsbDevice getUsbDevice() {
            return (UsbDevice) userObject;
        }

        public void setUsbDevice(UsbDevice usbDevice) {
            userObject = usbDevice;
        }

        @Override
        public boolean isLeaf() {
            if (userObject instanceof UsbHub) {
                return false;
            }
            return super.isLeaf();
        }

        @Override
        public String toString() {
            if (userObject == null) {
                return Bundle.getMessage("UnableToGetUsbRootHub");
            } else if (userObject instanceof UsbDevice) {
                String name = UsbUtil.getFullProductName((UsbDevice) userObject);
                if (name != null) {
                    return name;
                }
            }
            return super.toString();
        }
    }   // class UsbTreeNode

    private static class UsbDeviceTableModel extends AbstractTableModel {

        private UsbTreeNode node = null;

        @Override
        public int getRowCount() {
            return ((node != null) && (node.getUsbDevice() != null)) ? 11 : 1;
        }

        @Override
        public int getColumnCount() {
            return ((node != null) && (node.getUsbDevice() != null)) ? 2 : 1;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return ""; // NOI18N
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if ((node == null) || (node.getUsbDevice() == null)) {
                return Bundle.getMessage("EmptySelection");
            }
            switch (columnIndex) {
                case 0:
                    switch (rowIndex) {
                        case 0:
                            return Bundle.getMessage("UsbDeviceManufacturer");
                        case 1:
                            return Bundle.getMessage("UsbDeviceProduct");
                        case 2:
                            return Bundle.getMessage("UsbDeviceSerial");
                        case 3:
                            return Bundle.getMessage("UsbDeviceVendorId");
                        case 4:
                            return Bundle.getMessage("UsbDeviceProductId");
                        case 5:
                            return Bundle.getMessage("UsbDeviceClass");
                        case 6:
                            return Bundle.getMessage("UsbDeviceSubClass");
                        case 7:
                            return Bundle.getMessage("UsbDeviceProtocol");
                        case 8:
                            return Bundle.getMessage("UsbDeviceReleaseNumber");
                        case 9:
                            return Bundle.getMessage("UsbDeviceNumConfigurations");
                        case 10:
                            return Bundle.getMessage("UsbDeviceLocation");
                        default:
                            break;
                    }
                    break;
                case -1:
                case 1:
                    try {
                        switch (rowIndex) {
                            case 0:
                                return node.getUsbDevice().getManufacturerString();
                            case 1:
                                return node.getUsbDevice().getProductString();
                            case 2:
                                return node.getUsbDevice().getSerialNumberString();
                            case 3:
                                return String.format("%04x", node.getUsbDevice().getUsbDeviceDescriptor().idVendor());
                            case 4:
                                return String.format("%04x", node.getUsbDevice().getUsbDeviceDescriptor().idProduct());
                            case 5:
                                return String.format("%02X", node.getUsbDevice().getUsbDeviceDescriptor().bDeviceClass());
                            case 6:
                                return String.format("%02X", node.getUsbDevice().getUsbDeviceDescriptor().bDeviceSubClass());
                            case 7:
                                return String.format("%02X", node.getUsbDevice().getUsbDeviceDescriptor().bDeviceProtocol());
                            case 8:
                                return String.format("%04x", node.getUsbDevice().getUsbDeviceDescriptor().bcdDevice());
                            case 9:
                                return node.getUsbDevice().getUsbDeviceDescriptor().bNumConfigurations();
                            case 10:
                                return UsbUtil.getLocation(node.getUsbDevice());
                            default:
                                return null;
                        }
                    } catch (UsbDisconnectedException ex) {
                        node.setUsbDevice(null);
                    } catch (UnsupportedEncodingException | UsbException ex) {
                        log.error("Unable to get USB device property.", ex);
                    }
                    break;
                default:
                    return null;
            }
            return null;
        }

        public void setNode(UsbTreeNode node) {
            UsbTreeNode old = this.node;
            this.node = node;
            if (((old == null) && (node != null)) || ((old != null) && (node == null))) {
                fireTableStructureChanged();
            }
            fireTableDataChanged();
        }
    }

    private final static class UsbTreeCellRenderer extends DefaultTreeCellRenderer {

        public UsbTreeCellRenderer() {
            int width = getOpenIcon().getIconWidth();
            int height = getOpenIcon().getIconHeight();
            try {
                setOpenIcon(new ImageIcon(new ImageIcon(getClass().getResource("/jmri/util/usb/topology.png"))
                        .getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH)));
            } catch (NullPointerException ex) {
                log.error("Unable to get resource /jmri/util/usb/topology.png from JMRI classpath");
            }
            try {
                setClosedIcon(new ImageIcon(new ImageIcon(getClass().getResource("/jmri/util/usb/topology.png"))
                        .getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH)));
            } catch (NullPointerException ex) {
                log.error("Unable to get resource /jmri/util/usb/topology.png from JMRI classpath");
            }
            try {
                setLeafIcon(new ImageIcon(new ImageIcon(getClass().getResource("/jmri/util/usb/usb.png"))
                        .getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH)));
            } catch (NullPointerException ex) {
                log.error("Unable to get resource /jmri/util/usb/usb.png from JMRI classpath");
            }
        }

    }
}
