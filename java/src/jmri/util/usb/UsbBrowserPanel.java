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
import java.util.Enumeration;
import java.util.List;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
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
import jmri.util.FileUtil;
import jmri.util.USBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2017
 */
public class UsbBrowserPanel extends javax.swing.JPanel {

    private final UsbTreeNode root;
    private final UsbDeviceTableModel deviceModel = new UsbDeviceTableModel();
    private final static Logger log = LoggerFactory.getLogger(UsbBrowserPanel.class);
    private final UsbServicesListener usbServicesListener = new UsbServicesListener() {
        @Override
        public void usbDeviceAttached(UsbServicesEvent use) {
            // subtler method to add usbDevice to tree
            UsbDevice usbDevice = use.getUsbDevice();
            UsbPort usbPort = usbDevice.getParentUsbPort();
            if (usbPort != null) {
                UsbHub parentUsbHub = usbPort.getUsbHub();
                UsbTreeNode parentNode = findNodeForDevice(root, parentUsbHub);
                if (parentNode != null) {
                    UsbTreeNode node = new UsbTreeNode(usbDevice);
                    parentNode.add(node);
                    if (usbTree != null) {
                        TreePath selection = usbTree.getSelectionPath();
                        ((DefaultTreeModel) usbTree.getModel()).nodeChanged(parentNode);
                        //Shouldn't have to do this… but .nodeChanged(parent) isn't enough
                        ((DefaultTreeModel) usbTree.getModel()).reload(root);
                        usbTree.setSelectionPath(selection);
                        expandAll(usbTree);
                    }
                    return;
                }
            }
            UsbTreeNode root = UsbBrowserPanel.this.root;
            root.removeAllChildren();
            UsbBrowserPanel.this.buildTree(root);
        }

        @Override
        public void usbDeviceDetached(UsbServicesEvent use) {
            // subtler method to remove usbDevice from tree
            UsbTreeNode root = UsbBrowserPanel.this.root;
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
        root = new UsbTreeNode();
        if (root.getUserObject() != null) {
            try {
                UsbHostManager.getUsbServices().addUsbServicesListener(usbServicesListener);
            } catch (UsbException | SecurityException ex) {
                log.error("Unable to get root USB hub.", ex);
            }
        }
        initComponents();
        buildTree(root);

        //
        // Change the default JTree icons
        //
        try {
            // get the tree cell renderer
            DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) usbTree.getCellRenderer();

            // get the size of the current icon
            Icon openIcon = renderer.getOpenIcon();
            int w = openIcon.getIconWidth();
            int h = openIcon.getIconHeight();

            // setup the usb open icon
            ImageIcon imageIcon = new ImageIcon(FileUtil.findURL("resources/icons/USB/USB_Open.jpg"));
            Image image = imageIcon.getImage(); // convert it to an image
            image = image.getScaledInstance(w, h, Image.SCALE_SMOOTH); // scale it the smooth way 
            imageIcon = new ImageIcon(image);  // convert it back to an icon
            renderer.setOpenIcon(imageIcon);

            // setup the usb closed icon
            imageIcon = new ImageIcon(FileUtil.findURL("resources/icons/USB/USB_Closed.jpg"));
            image = imageIcon.getImage(); // convert it to an image
            image = image.getScaledInstance(w, h, Image.SCALE_SMOOTH); // scale it the smooth way 
            imageIcon = new ImageIcon(image);  // convert it back to an icon
            renderer.setClosedIcon(imageIcon);

            // get the usb leaf icon
            imageIcon = new ImageIcon(FileUtil.findURL("resources/icons/USB/USB_Leaf.jpg"));
            image = imageIcon.getImage(); // convert it to an image
            image = image.getScaledInstance(w, h, Image.SCALE_SMOOTH); // scale it the smooth way 
            imageIcon = new ImageIcon(image);  // convert it back to an icon
            renderer.setLeafIcon(imageIcon);
        } catch (RuntimeException e) {
            throw e;    // runtime exceptions make me throw up
        } catch (Exception e) {
            // ignore others
        }
    }

    private void buildTree(UsbTreeNode root) {
        buildTree(root, 0);
        expandAll(usbTree);
    }

    private void buildTree(@Nonnull UsbTreeNode root, int depth) {
        Object userObject = root.getUserObject();
        if (userObject != null && ((UsbDevice) userObject).isUsbHub()) {
            UsbHub usbHub = (UsbHub) userObject;
            List<UsbDevice> usbDevices = usbHub.getAttachedUsbDevices();
            for (UsbDevice usbDevice : usbDevices) {
                UsbTreeNode node = new UsbTreeNode(usbDevice);
                log.debug("Adding {} to {}, depth: {}", node, root, depth);
                buildTree(node, depth + 1);
                root.add(node);
            }
        }
        // prevent NPE when called in constructor
        if (usbTree != null) {
            TreePath selection = usbTree.getSelectionPath();
            ((DefaultTreeModel) usbTree.getModel()).reload(root);
            usbTree.setSelectionPath(selection);
        }
    }

    /**
     * expand all containers in tree
     *
     * @param tree the tree
     */
    public void expandAll(@Nullable JTree tree) {
        expandAll(tree, true);
    }

    /**
     * unexpand all containers in tree
     *
     * @param tree the tree
     */
    public void unexpandAll(@Nullable JTree tree) {
        expandAll(tree, false);
    }

    /**
     * (un)expand all containers in tree
     *
     * @param tree   the tree
     * @param expand set true to expand; false to unexpand
     */
    public void expandAll(@Nullable JTree tree, boolean expand) {
        if (tree != null) {
            TreeNode root = (TreeNode) tree.getModel().getRoot();
            expandAll(tree, new TreePath(root), expand);

            // if we just collapsed everything...
            if (!expand) {
                // reexpand the root TreeNode
                tree.expandPath(new TreePath(root));
            }
        }
    }

    private static void expandAll(JTree tree, TreePath parentTreePath, boolean expand) {
        TreeNode parentTreeNode = (TreeNode) parentTreePath.getLastPathComponent();
        if (parentTreeNode.getChildCount() > 0) {
            for (Enumeration e = parentTreeNode.children(); e.hasMoreElements();) {
                TreeNode treeNode = (TreeNode) e.nextElement();
                TreePath path = parentTreePath.pathByAddingChild(treeNode);
                expandAll(tree, path, expand);
            }
        }

        if (expand) {
            tree.expandPath(parentTreePath);
        } else {
            tree.collapsePath(parentTreePath);
        }
    }

    /*
     * recursively search all children of root for usb device
     */
    @CheckReturnValue
    private UsbTreeNode findNodeForDevice(@Nonnull UsbTreeNode root,
            @Nonnull UsbDevice usbDevice) {
        UsbTreeNode result = null;  // assume failure (pessimist!)
        if (!root.isLeaf()) {
            for (int idx = 0; idx < root.getChildCount(); idx++) {
                TreeNode treeNode = root.getChildAt(idx);
                if (treeNode instanceof UsbTreeNode) {
                    UsbTreeNode usbTreeNode = (UsbTreeNode) treeNode;
                    UsbDevice tryUsbDevice = usbTreeNode.getUsbDevice();
                    log.debug("findNodeForDevice-usbTreeNode device: " + tryUsbDevice);
                    if ((tryUsbDevice != null) && (tryUsbDevice == usbDevice)) {
                        result = usbTreeNode;
                        break;
                    }
                    result = findNodeForDevice(usbTreeNode, usbDevice);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        try {
            UsbHostManager.getUsbServices().
                    removeUsbServicesListener(usbServicesListener);
        } catch (UsbException | SecurityException ex) {
            // silently ignore, since it was logged when this panel was constructed
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane(this.usbTree);
        usbTree = new javax.swing.JTree(this.root);
        jScrollPane2 = new javax.swing.JScrollPane();
        detailTable = new javax.swing.JTable();

        jSplitPane1.setBorder(null);
        jSplitPane1.setResizeWeight(0.5);

        jScrollPane1.setBorder(null);

        usbTree.setRootVisible(false);
        usbTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        usbTree.addTreeSelectionListener(this.treeSelectionListener);
        jScrollPane1.setViewportView(usbTree);

        jSplitPane1.setLeftComponent(jScrollPane1);

        detailTable.setModel(this.deviceModel);
        jScrollPane2.setViewportView(detailTable);

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
    private javax.swing.JTable detailTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTree usbTree;
    // End of variables declaration//GEN-END:variables

    private static class UsbTreeNode extends DefaultMutableTreeNode {

        public UsbTreeNode() {
            this(null);
        }

        public UsbTreeNode(@Nullable UsbDevice usbDevice) {
            super();

            if (usbDevice == null) {
                try {
                    usbDevice = UsbHostManager.getUsbServices().getRootUsbHub();
                    log.debug("Using root usbDevice {}", usbDevice);
                } catch (UsbException | SecurityException ex) {
                    log.error("Unable to get root USB hub.", ex);
                }
            } else {
                log.debug("Description of {} is\n{}", usbDevice, usbDevice.getUsbDeviceDescriptor());
            }
            userObject = usbDevice;
        }

        public UsbDevice getUsbDevice() {
            return (UsbDevice) userObject;
        }

        public void setUsbDevice(@Nullable UsbDevice usbDevice) {
            userObject = usbDevice;
        }

        @Override
        @Nonnull
        public String toString() {
            if (userObject == null) {
                return Bundle.getMessage("UnableToGetUsbRootHub");
            } else if (userObject instanceof UsbDevice) {
                UsbDevice usbDevice = (UsbDevice) userObject;
                String fullProductName = USBUtil.getFullProductName(usbDevice);
                if ((fullProductName == null) || fullProductName.isEmpty()) {
                    return userObject.toString();
                }
                return fullProductName;
            }
            return super.toString();
        }
    }   // class UsbTreeNode

    private static class UsbDeviceTableModel extends AbstractTableModel {

        private UsbTreeNode node = null;
        //private UsbDevice usbDevice = null;

        @Override
        public int getRowCount() {
            return ((node != null) && (node.getUsbDevice() != null)) ? 10 : 1;
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
        @CheckReturnValue
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object result = null;

            if ((node == null) || (node.getUsbDevice() == null)) {
                return Bundle.getMessage("EmptySelection");
            }
            switch (columnIndex) {
                case 0: {
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
                            return Bundle.getMessage("UsbDeviceBCDDevice");
                        case 9:
                            return Bundle.getMessage("UsbDeviceLocationId");
                        default:
                            break;
                    }
                    break;
                }
                case -1:
                case 1: {
                    try {
                        UsbDevice usbDevice = node.getUsbDevice();
                        switch (rowIndex) {
                            case 0:
                                result = "?";
                                return usbDevice.getManufacturerString();
                            case 1:
                                result = "?";
                                return usbDevice.getProductString();
                            case 2:
                                result = "?";
                                return usbDevice.getSerialNumberString();
                            case 3:
                                result = "????";
                                return String.format("%04X", usbDevice.getUsbDeviceDescriptor().idVendor());
                            case 4:
                                result = "????";
                                return String.format("%04X", usbDevice.getUsbDeviceDescriptor().idProduct());
                            case 5:
                                result = "??";
                                return String.format("%02X", usbDevice.getUsbDeviceDescriptor().bDeviceClass());
                            case 6:
                                result = "??";
                                return String.format("%02X", usbDevice.getUsbDeviceDescriptor().bDeviceSubClass());
                            case 7:
                                result = "?";
                                return String.format("%d", usbDevice.getUsbDeviceDescriptor().bDeviceProtocol());
                            case 8:
                                log.info("*: " + usbDevice.getUsbDeviceDescriptor());
                                result = "##.##";
                                short version = usbDevice.getUsbDeviceDescriptor().bcdDevice();
                                byte hiVersion = (byte) (version >> 8);
                                byte loVersion = (byte) version;
                                return String.format("%X.%02X", hiVersion, loVersion);
                            case 9:
                                result = "########";
                                return USBUtil.getLocationID(usbDevice);
                            default:
                                break;
                        }
                    } catch (UsbDisconnectedException ex) {
                        node.setUsbDevice(null);
                    } catch (UnsupportedEncodingException | UsbException ex) {
                        log.error("Unable to get USB device property.", ex);
                    }
                    break;
                }
                default: {
                    break;
                }
            }
            return result;
        }   // getValueAt

        public void setNode(@Nullable UsbTreeNode node) {
            UsbTreeNode old = this.node;
            this.node = node;
            if (((old == null) && (node != null)) || ((old != null) && (node == null))) {
                fireTableStructureChanged();
            }
            fireTableDataChanged();
        }
    }
}
