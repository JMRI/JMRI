package jmri.jmrix.grapevine.nodetable;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialTrafficController;

/**
 * Frame lets user renumber a Grapevine node
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class RenumberFrame extends jmri.util.JmriJFrame {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.grapevine.nodetable.NodeTableBundle");

    /**
     * Constructor method
     */
    public RenumberFrame() {
        super();
    }

    JTextField from;
    JTextField to;

    /**
     * Initialize the window
     */
    public void initComponents() {
        setTitle(rb.getString("WindowTitleRenumber"));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        contentPane.add(p);

        p.add(new JLabel(rb.getString("LabelFrom")));
        from = new JTextField(4);
        p.add(from);

        p.add(new JLabel(rb.getString("LabelTo")));
        to = new JTextField(4);
        p.add(to);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        contentPane.add(p);

        JButton b = new JButton(rb.getString("ButtonExec"));
        p.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                execute();
            }
        });

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.grapevine.nodetable.RenumberFrame", true);

        // pack for display
        pack();
    }

    /**
     * Send the message to change the address
     */
    void execute() {
        // get addresses
        int f = Integer.parseInt(from.getText());
        int t = Integer.parseInt(to.getText());
        // format the message
        SerialMessage m = new SerialMessage();
        m.setElement(0, 0x80 + (f & 0x7F));
        m.setElement(1, (t & 0x7F));
        m.setElement(2, 0x80 + (f & 0x7F));
        m.setElement(3, 0x60);
        m.setParity();
        SerialTrafficController.instance().sendSerialMessage(m, null);
    }
}
