// NodeConfigToolPane.java
package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane to for setting node configuration
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 * @since 2.3.1
 */
public class NodeConfigToolPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    /**
     *
     */
    private static final long serialVersionUID = -5162692735562566371L;

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolBundle");

    JTextField number;
    JButton setNN;
    JTextField varnumber;
    JTextField varvalue;
    JButton read;
    JButton write;

    TrafficController tc;

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
    }

    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolBundle").getString("Title"));
        }
        return ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolBundle").getString("Title");
    }

    public NodeConfigToolPane() {
        super();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p1;

        // get event number
        p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(new JLabel(rb.getString("LabelNodeNumber")));
        number = new JTextField(5);
        p1.add(number);

        p1.setToolTipText(rb.getString("ToolTipNodeNumber"));
        setNN = new JButton(rb.getString("ButtonSetNodeNumber"));
        setNN.setToolTipText(rb.getString("ToolTipSetNodeNumber"));
        p1.add(setNN);

        p1.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderNodeNumber")));
        add(p1);

        // set node variables
        p1 = new JPanel();
        p1.setLayout(new java.awt.GridLayout(3, 2));

        p1.add(new JLabel(rb.getString("LabelVariableNumber")));
        varnumber = new JTextField(5);
        varnumber.setToolTipText(rb.getString("ToolTipVariableNumber"));
        p1.add(varnumber);

        p1.add(new JLabel(rb.getString("LabelVariableValue")));
        varvalue = new JTextField(5);
        varvalue.setToolTipText(rb.getString("ToolTipVariableValue"));
        p1.add(varvalue);

        read = new JButton(rb.getString("ButtonRead"));
        read.setToolTipText(rb.getString("ToolTipRead"));
        p1.add(read);
        write = new JButton(rb.getString("ButtonWrite"));
        write.setToolTipText(rb.getString("ToolTipWrite"));
        p1.add(write);

        p1.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderNodeVariables")));
        add(p1);

    }

    public void reply(jmri.jmrix.can.CanReply m) {
    }

    public void message(jmri.jmrix.can.CanMessage m) {
    }

    public void dispose() {
        // disconnect from the CBUS
        if (tc != null) {
            tc.removeCanListener(this);
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = 7778206986212539509L;

        public Default() {
            super(ResourceBundle.getBundle("jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolBundle").getString("Title"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    NodeConfigToolPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NodeConfigToolPane.class.getName());
}
