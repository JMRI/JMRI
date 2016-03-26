// LocoDataPane.java
package jmri.jmrix.tams.swing.locodatabase;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.jmrix.tams.TamsMessage;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.util.JTableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame provinging access to the loco database on the Tams MC
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 * @version	$Revision: 17977 $
 */
public class LocoDataPane extends jmri.jmrix.tams.swing.TamsPanel {

    /**
     *
     */
    private static final long serialVersionUID = -7652937506195229419L;
    LocoDataModel locoModel;
    JTable locoTable;
    JScrollPane locoScroll;

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.tams.swing.locodatabase.TamsLocoBundle");

    String[] speed = {"126", "14", "27", "28"};
    String[] fmt = {"DCC", "MM1", "MM2"};

    JTextField addr = new JTextField(5);
    JTextField name = new JTextField(10);
    JComboBox<String> speedBox = new JComboBox<String>(speed);
    JComboBox<String> formatBox = new JComboBox<String>(fmt);
    JButton addButton = new JButton(rb.getString("AddLoco"));

    public LocoDataPane() {
        super();
    }

    public void initComponents(jmri.jmrix.tams.TamsSystemConnectionMemo memo) {
        super.initComponents(memo);

        locoModel = new LocoDataModel(128, 16, memo);
        locoTable = JTableUtil.sortableDataModel(locoModel);
        locoScroll = new JScrollPane(locoTable);

        locoModel.configureTable(locoTable);

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(new JLabel(rb.getString("Address")));
        pane1.add(addr);
        pane1.add(new JLabel(rb.getString("Name")));
        pane1.add(name);
        pane1.add(new JLabel(rb.getString("Steps")));
        pane1.add(speedBox);
        pane1.add(new JLabel(rb.getString("Format")));
        pane1.add(formatBox);
        pane1.add(addButton);

        // add listener object so checkboxes function
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addLoco();
            }
        });

        add(pane1);
        add(locoScroll);

        if (pane1.getMaximumSize().height > 0 && pane1.getMaximumSize().width > 0) {
            pane1.setMaximumSize(pane1.getPreferredSize());
        }
    }

    void addLoco() {
        if (addr.getText() == null || addr.getText().equals("")) {
            log.error("Require an address to be entered");
            JOptionPane.showMessageDialog(null, rb.getString("ErrorNullAddress"), "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;

        }
        try {
            new Integer(addr.getText());
        } catch (NumberFormatException nx) {
            log.error("Unable to convert " + addr.getText() + " to a number");
            JOptionPane.showMessageDialog(null, rb.getString("ErrorNotNumber"), "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("xLOCADD ");
        sb.append(addr.getText());
        sb.append(",");
        sb.append(speedBox.getSelectedItem());
        sb.append(",");
        sb.append(formatBox.getSelectedItem());
        sb.append(",'");
        sb.append(name.getText());
        sb.append("'");

        TamsMessage m = new TamsMessage(sb.toString());
        locoModel.addLoco(m);
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.tams.swing.locodatabase.LocoDataFrame";
    }

    public String getTitle() {
        return rb.getString("Title");
    }

    public void dispose() {
        locoModel.dispose();
        locoModel = null;
        locoTable = null;
        locoScroll = null;
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.tams.swing.TamsNamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = 8803207637297660717L;

        public Default() {
            super(rb.getString("Title"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    LocoDataPane.class.getName(),
                    jmri.InstanceManager.getDefault(TamsSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocoDataPane.class.getName());

}
