package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Frame for Adding new Nodes
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dave Duchamp Copyright (C) 2004
 * @author Paul Bender Copyright (C) 2013,2016
 */
public class XBeeAddNodeFrame extends jmri.jmrix.ieee802154.swing.nodeconfig.AddNodeFrame {

    private XBeeTrafficController xtc = null;
    private final javax.swing.JTextField nodeIdentifierField = new javax.swing.JTextField();
    private XBeeNodeConfigFrame parent = null;


    /**
     * Constructor method
     *
     * @param tc the XBeeTrafficController associated with this connection.
     * @param source the XBeeNodeConfigFrame that started this add.
     */
    public XBeeAddNodeFrame(XBeeTrafficController tc,XBeeNodeConfigFrame source) {
        super(tc);
        xtc = tc;
        parent = source;
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("AddNodeWindowTitle"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up node address and node type
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage("LabelNodeAddress") + " "));
        panel.add(nodeAddrField);
        /*nodeAddrField.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            }
        });*/
        nodeAddrField.setToolTipText(Bundle.getMessage("TipNodeAddress"));
        panel.add(new JLabel(Bundle.getMessage("LabelNodeAddress64") + " "));
        panel.add(nodeAddr64Field);
        nodeAddr64Field.setToolTipText(Bundle.getMessage("TipNodeAddress64"));
        /*nodeAddr64Field.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
            }
        });*/
        panel.add(new JLabel(Bundle.getMessage("LabelNodeIdentifier") + " "));
        panel.add(nodeIdentifierField);
        nodeIdentifierField.setToolTipText(Bundle.getMessage("TipNodeIdentifier"));

        /*nodeIdentifierField.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                nodeAddrField.setSelectedIndex(nodeIdentifierField.getSelectedIndex());
            }
        });*/
        initAddressBoxes();
        contentPane.add(panel);

        // Set up buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        addButton.setText(Bundle.getMessage("ButtonAdd"));
        addButton.setVisible(true);
        addButton.setToolTipText(Bundle.getMessage("TipAddButton"));
        addButton.addActionListener((java.awt.event.ActionEvent e) -> {
            addButtonActionPerformed();
        });
        panel4.add(addButton);
        panel4.add(cancelButton);
        cancelButton.setText(Bundle.getMessage("ButtonCancel"));
        cancelButton.setVisible(true);
        cancelButton.setToolTipText(Bundle.getMessage("TipCancelButton"));
        panel4.add(cancelButton);
        cancelButton.addActionListener((java.awt.event.ActionEvent e) -> {
            cancelButtonActionPerformed();
        });
        contentPane.add(panel4);

        // pack for display
        pack();
    }

    /**
     * Method to handle add button
     */
    @Override
    public void addButtonActionPerformed() {
        if(nodeAddr64Field.getText().equals("") &&
           nodeAddrField.getText().equals("")) {
           // no address, just return.
           return;
        }
        // Check that a node with this address does not exist
        // if the 64 bit address field is blank, use the "Unknown" address".
        XBee64BitAddress guid;
        if(!(nodeAddr64Field.getText().equals(""))) {
           byte GUID[] = jmri.util.StringUtil.bytesFromHexString(nodeAddr64Field.getText());
           guid = new XBee64BitAddress(GUID);
        } else {
           guid = XBee64BitAddress.UNKNOWN_ADDRESS;
        }
        // if the 16 bit address field is blank, use the "Unknown" address".
        XBee16BitAddress address;
        if(!(nodeAddrField.getText().equals(""))){
           byte addr[] = jmri.util.StringUtil.bytesFromHexString(nodeAddrField.getText());
           address = new XBee16BitAddress(addr);
        } else {
           address = XBee16BitAddress.UNKNOWN_ADDRESS;
        }
        String Identifier = nodeIdentifierField.getText();
        // create the RemoteXBeeDevice for the node.
        RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(xtc.getXBee(),
              guid,address,Identifier);
        // get a XBeeNode corresponding to this node address if one exists
        curNode = (XBeeNode) xtc.getNodeFromXBeeDevice(remoteDevice);
        if (curNode != null) {
            javax.swing.JOptionPane.showMessageDialog(this,Bundle.getMessage("Error1",remoteDevice),Bundle.getMessage("AddNodeErrorTitle"),JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            // and then add it to the network
            xtc.getXBee().getNetwork().addRemoteDevice(remoteDevice);
            // create node (they register themselves)
            XBeeNode node = new XBeeNode(remoteDevice);

            xtc.registerNode(node);
            parent.nodeListChanged();
        } catch (TimeoutException toe) {
            log.error("Timeout adding node {}.",remoteDevice);
            javax.swing.JOptionPane.showMessageDialog(this,Bundle.getMessage("Error3"),Bundle.getMessage("AddNodeErrorTitle"),JOptionPane.ERROR_MESSAGE);
            log.error("Error creating XBee Node, constructor returned null");
            return;
        } catch (XBeeException xbe) {
            log.error("Exception adding node {}.",remoteDevice);
            javax.swing.JOptionPane.showMessageDialog(this,Bundle.getMessage("Error3"),Bundle.getMessage("AddNodeErrorTitle"),JOptionPane.ERROR_MESSAGE);
            log.error("Error creating XBee Node, constructor returned null");
            return;
        }

        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Method to handle cancel button
     */
    @Override
    public void cancelButtonActionPerformed() {
        // Reset
        curNode = null;
        // Switch buttons
        addButton.setVisible(true);
        cancelButton.setVisible(false);
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    // Initilize the text boxes for the addresses.
    @Override
    protected void initAddressBoxes() {
        nodeAddrField.setText("");
        nodeAddr64Field.setText("");
        nodeIdentifierField.setText("");
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeAddNodeFrame.class);

}
