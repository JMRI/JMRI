package jmri.jmrix.jmriclient.json.swing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.jmrix.jmriclient.json.JsonClientListener;
import jmri.jmrix.jmriclient.json.JsonClientMessage;
import jmri.jmrix.jmriclient.json.JsonClientReply;
import jmri.jmrix.jmriclient.json.JsonClientTrafficController;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood 2014
 */
class JsonPacketGenFrame extends JmriJFrame implements JsonClientListener {

    private final JLabel jLabel1 = new JLabel();
    private final JButton sendButton = new JButton();
    private final JTextField packetTextField = new JTextField(12);
    private final ObjectMapper mapper = new ObjectMapper();

    private JsonClientTrafficController trafficController = null;

    private final static Logger log = LoggerFactory.getLogger(JsonPacketGenFrame.class);

    public JsonPacketGenFrame() {
        super();
    }

    @Override
    public void initComponents() throws Exception {
        // set the frame's initial state

        this.jLabel1.setText("Command:");
        this.jLabel1.setVisible(true);

        this.sendButton.setText("Send");
        this.sendButton.setVisible(true);
        this.sendButton.setToolTipText("Send packet");

        this.packetTextField.setText("");
        this.packetTextField.setToolTipText("Enter command as ASCII string");
        this.packetTextField.setMaximumSize(
                new Dimension(this.packetTextField.getMaximumSize().width,
                        this.packetTextField.getPreferredSize().height
                )
        );

        this.setTitle(Bundle.getMessage("JsonClientSendCommand")); // NOI18N
        this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        this.getContentPane().add(jLabel1);
        this.getContentPane().add(packetTextField);
        this.getContentPane().add(sendButton);

        this.sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(ActionEvent e) {
        try {
            this.trafficController.sendJsonClientMessage(new JsonClientMessage(mapper.readTree(packetTextField.getText())), this);
        } catch (IOException ex) {
            log.error("Unable to parse command: {}", ex.getMessage());
        }
    }

    @Override
    public void message(JsonClientMessage m) {
    }  // ignore replies

    @Override
    public void reply(JsonClientReply r) {
    } // ignore replies

    // connect to the TrafficController
    public void connect(JsonClientTrafficController t) {
        this.trafficController = t;
    }

    @Override
    public void message(JsonNode message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reply(JsonNode reply) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
