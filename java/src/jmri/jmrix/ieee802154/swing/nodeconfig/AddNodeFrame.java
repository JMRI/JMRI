package jmri.jmrix.ieee802154.swing.nodeconfig;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrix.ieee802154.IEEE802154Node;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for Adding new Nodes
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dave Duchamp Copyright (C) 2004
 * @author Paul Bender Copyright (C) 2013,2016
 */
public class AddNodeFrame extends jmri.util.JmriJFrame {

    protected javax.swing.JTextField nodeAddrField = new javax.swing.JTextField();
    protected javax.swing.JTextField nodeAddr64Field = new javax.swing.JTextField();
    protected javax.swing.JButton addButton = new javax.swing.JButton(Bundle.getMessage("ButtonAdd"));
    protected javax.swing.JButton cancelButton = new javax.swing.JButton(Bundle.getMessage("ButtonCancel"));

    protected IEEE802154Node curNode = null;    // IEEE802154 Node being editted

    private IEEE802154TrafficController itc = null;

    /**
     * Constructor method
     * @param tc tc for connection for node
     */
    public AddNodeFrame(IEEE802154TrafficController tc) {
        super();
        addHelpMenu("package.jmri.jmrix.ieee802154.swing.nodeconfig.AddNodeFrame", true);
        itc = tc;
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
     * Method to handle Add button
     */
    public void addButtonActionPerformed() {
        // Check that a node with this address does not exist
        String nodeAddress = readNodeAddress();
        if (nodeAddress.equals("")) {
            return;
        }
        // get a IEEE802154 Node corresponding to this node address if one exists
        curNode = (IEEE802154Node) itc.getNodeFromAddress(nodeAddress);
        if (curNode != null) {
            javax.swing.JOptionPane.showMessageDialog(this,Bundle.getMessage("Error1",nodeAddress),Bundle.getMessage("AddNodeErrorTitle"),JOptionPane.ERROR_MESSAGE);
            log.error("Error creating IEEE802154 Node, Node exists.");
            return;
        }
        // get node information from window

        // all ready, create the new node
        curNode = itc.newNode();
        if (curNode == null) {
            javax.swing.JOptionPane.showMessageDialog(this,Bundle.getMessage("Error3"),Bundle.getMessage("AddNodeErrorTitle"),JOptionPane.ERROR_MESSAGE);
            log.error("Error creating IEEE802154 Node, constructor returned null");
            return;
        }
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Method to handle cancel button
     */
    public void cancelButtonActionPerformed() {
        // Reset
        curNode = null;
        // Switch buttons
        addButton.setVisible(true);
        cancelButton.setVisible(false);
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Read node address from the nodeAddressField or nodeAddr64Field
     * as appropriate and return as a string.
     *
     * @return String containing the short (two byte) address of the node.
     *         if the two byte node address is either "FF FF" or "FF FE",
     *         returns the long (64 bit) address.
     */
    private String readNodeAddress() {
        String addr = nodeAddrField.getText();
        if (addr.equals("FF FF ") || addr.equals("FF FE ") || addr.equals("")) {
            addr = nodeAddr64Field.getText();
        }
        return (addr);
    }

    // Initilize the text boxes for the addresses.
    protected void initAddressBoxes() {
        nodeAddrField.setText("");
        nodeAddr64Field.setText("");
    }

    private final static Logger log = LoggerFactory.getLogger(AddNodeFrame.class);

}
