package jmri.jmrix.ieee802154.swing.nodeconfig;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrix.ieee802154.IEEE802154Node;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for Editing Nodes
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dave Duchamp Copyright (C) 2004
 * @author Paul Bender Copyright (C) 2013,2016
 */
public class EditNodeFrame extends jmri.util.JmriJFrame {

    protected javax.swing.JTextField nodeAddrField = new javax.swing.JTextField();
    protected javax.swing.JTextField nodeAddr64Field = new javax.swing.JTextField();
    protected javax.swing.JButton editButton = new javax.swing.JButton(Bundle.getMessage("ButtonEdit"));
    protected javax.swing.JButton cancelButton = new javax.swing.JButton(Bundle.getMessage("ButtonCancel"));

    protected javax.swing.JPanel panel = new JPanel();

    protected IEEE802154Node curNode = null;    // IEEE802154 Node being editted

    @SuppressWarnings("unused")
    private IEEE802154TrafficController itc = null;

    /**
     * Constructor method
     * @param tc traffic controller
     * @param node node to edit
     */
    public EditNodeFrame(IEEE802154TrafficController tc, IEEE802154Node node) {
        super();
        addHelpMenu("package.jmri.jmrix.ieee802154.swing.nodeconfig.EditNodeFrame", true);
        itc = tc;
        curNode = node;
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("EditNodeWindowTitle"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up node address and node type
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage("LabelNodeAddress") + " "));
        panel.add(nodeAddrField);
        nodeAddrField.setToolTipText(Bundle.getMessage("TipNodeAddress"));
        panel.add(new JLabel(Bundle.getMessage("LabelNodeAddress64") + " "));
        panel.add(nodeAddr64Field);
        nodeAddr64Field.setToolTipText(Bundle.getMessage("TipNodeAddress64"));

        initAddressBoxes();
        contentPane.add(panel);

        // Set up buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        editButton.setText(Bundle.getMessage("ButtonEdit"));
        editButton.setVisible(true);
        editButton.setToolTipText(Bundle.getMessage("TipEditButton"));
        editButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editButtonActionPerformed();
            }
        });
        panel4.add(editButton);
        panel4.add(cancelButton);
        cancelButton.setText(Bundle.getMessage("ButtonCancel"));
        cancelButton.setVisible(true);
        cancelButton.setToolTipText(Bundle.getMessage("TipCancelButton"));
        panel4.add(cancelButton);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });
        contentPane.add(panel4);

        // pack for display
        pack();
    }

    /**
     * Method to handle edit button
     */
    public void editButtonActionPerformed() {
        // get node information from window
        // all ready, update the node
        curNode.setUserAddress(jmri.util.StringUtil.bytesFromHexString(nodeAddrField.getText()));
        curNode.setGlobalAddress(jmri.util.StringUtil.bytesFromHexString(nodeAddr64Field.getText()));
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Method to handle cancel button
     */
    public void cancelButtonActionPerformed() {
        // Reset 
        curNode = null;
        // Switch buttons
        editButton.setVisible(true);
        cancelButton.setVisible(false);
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    // Initilize the text boxes for the addresses.
    protected void initAddressBoxes() {
        nodeAddrField.setText(jmri.util.StringUtil.hexStringFromBytes(curNode.getUserAddress()));
        nodeAddr64Field.setText(jmri.util.StringUtil.hexStringFromBytes(curNode.getGlobalAddress()));
    }

    @SuppressWarnings("unused")
    private final static Logger log = LoggerFactory.getLogger(AddNodeFrame.class);

}
