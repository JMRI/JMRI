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

import java.io.UnsupportedEncodingException;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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
            // TODO: use sublter method to add device to tree
            UsbTreeNode root = UsbBrowserPanel.this.root;
            root.removeAllChildren();
            UsbBrowserPanel.this.buildTree(root);
        }

        @Override
        public void usbDeviceDetached(UsbServicesEvent use) {
            // TODO: use sublter method to remove device from tree
            UsbTreeNode root = UsbBrowserPanel.this.root;
            root.removeAllChildren();
            UsbBrowserPanel.this.buildTree(root);
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
     * <p>
     */
    public UsbBrowserPanel() {
        root = new UsbTreeNode();
        buildTree(root);
        if (root.getUserObject() != null) {
            try {
                UsbHostManager.getUsbServices().addUsbServicesListener(usbServicesListener);
            } catch (UsbException | SecurityException ex) {
                log.error("Unable to get root USB hub.", ex);
            }
        }
        initComponents();
    }

    private void buildTree(UsbTreeNode root) {
        buildTree(root, 0);
    }

    private void buildTree(UsbTreeNode root, int depth) {
        Object userObject = root.getUserObject();
        if (userObject != null && ((UsbDevice) userObject).isUsbHub()) {
            UsbHub usbHub = (UsbHub) userObject;
            for (byte portIndex = 0; portIndex <= usbHub.getNumberOfPorts(); portIndex++) {
                UsbPort usbPort = usbHub.getUsbPort(portIndex);
                if (usbPort != null) {
                    if (usbPort.isUsbDeviceAttached()) {
                        UsbDevice usbDevice = usbPort.getUsbDevice();
                        UsbTreeNode node = new UsbTreeNode(usbDevice, portIndex);
                        byte portNumber = usbPort.getPortNumber();
                        log.debug("*	Adding {} to {} on port #{}/{} (depth = {}",
                                node, root, portIndex, portNumber, depth);
                        buildTree(node, depth + 1);
                        root.add(node);
                    }
                }
            }
        }
        // prevent NPE if called in constructor
        if (usbTree != null) {
            TreePath selection = usbTree.getSelectionPath();
            ((DefaultTreeModel) usbTree.getModel()).reload(root);
            usbTree.setSelectionPath(selection);
        }
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane(this.usbTree);
        usbTree = new javax.swing.JTree(this.root);
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jSplitPane1.setBorder(null);
        jSplitPane1.setResizeWeight(0.5);

        jScrollPane1.setBorder(null);

        usbTree.setRootVisible(false);
        usbTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        usbTree.addTreeSelectionListener(this.treeSelectionListener);
        jScrollPane1.setViewportView(usbTree);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jTable1.setModel(this.deviceModel);
        jScrollPane2.setViewportView(jTable1);

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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTree usbTree;
    // End of variables declaration//GEN-END:variables

    private static class UsbTreeNode extends DefaultMutableTreeNode {

        public UsbTreeNode() {
            this(null, (byte) 0);
        }

        public UsbTreeNode(UsbDevice usbDevice, byte portIndex) {
            super();

            if (usbDevice == null) {
                try {
                    usbDevice = UsbHostManager.getUsbServices().getRootUsbHub();
                    log.debug("Using root usbDevice {}", usbDevice);
                    usbPortIndex = 0;
                } catch (UsbException | SecurityException ex) {
                    log.error("Unable to get root USB hub.", ex);
                }
            } else {
                log.debug("Description of {} is\n{}", usbDevice, usbDevice.getUsbDeviceDescriptor());
                usbPortIndex = portIndex;
            }
            userObject = usbDevice;
        }

        public UsbDevice getUsbDevice() {
            return (UsbDevice) userObject;
        }

        public void setUsbDevice(UsbDevice usbDevice) {
            userObject = usbDevice;
        }

        private byte usbPortIndex = 0;

        public byte getUsbPortIndex() {
            return usbPortIndex;
        }

        @Override
        public String toString() {
            if (userObject == null) {
                return Bundle.getMessage("UnableToGetUsbRootHub");
            } else if (userObject instanceof UsbDevice) {
                try {
                    UsbDevice device = ((UsbDevice) userObject);
                    String manufacturerString = device.getManufacturerString();
                    manufacturerString = (manufacturerString == null) ? "" : manufacturerString;
                    String productString = device.getProductString();
                    productString = (productString == null) ? "" : productString;
                    if (productString.isEmpty()) {
                        if (!manufacturerString.isEmpty()) {
                            return manufacturerString;
                        }
                    } else if (manufacturerString.isEmpty() || productString.startsWith(manufacturerString)) {
                        return productString;
                    } else {
                        return manufacturerString + " " + productString;
                    }
                } catch (UsbException | UnsupportedEncodingException | UsbDisconnectedException ex) {
                    log.error("Unable to get USB device properties for {}", userObject);
                }
            }
            return super.toString();
        }
    }   // class UsbTreeNode

    private static class UsbDeviceTableModel extends AbstractTableModel {

        private UsbTreeNode node = null;
        //private UsbDevice device = null;

        @Override
        public int getRowCount() {
            return ((node != null) && (node.getUsbDevice() != null)) ? 6 : 1;
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
                            return Bundle.getMessage("UsbDeviceLocationId");
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
                                return String.format("0x%04X", node.getUsbDevice().getUsbDeviceDescriptor().idVendor());
                            case 4:
                                return String.format("0x%04X", node.getUsbDevice().getUsbDeviceDescriptor().idProduct());
                            case 5:
                                return USBUtil.getLocationID(node.getUsbDevice());
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

        // public void setUsbDevice(UsbDevice device) {
        //     UsbDevice old = device;
        //     this.device = device;
        //     if ((old == null && device != null) || (old != null && device == null)) {
        //         fireTableStructureChanged();
        //     }
        //     fireTableDataChanged();
        // }
        //
        // private byte usbPortIndex = 0;
        //
        // public void setUsbPortIndex(byte usbPortIndex) {
        //     this.usbPortIndex = usbPortIndex;
        // }
        //
        // public byte getUsbPortIndex() {
        //     return usbPortIndex;
        // }
    }
}
