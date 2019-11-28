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
import javax.swing.table.TableRowSorter;
import jmri.jmrix.tams.TamsMessage;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing access to the loco database on the Tams MC
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public class LocoDataPane extends jmri.jmrix.tams.swing.TamsPanel {

    LocoDataModel locoModel;
    JTable locoTable;
    JScrollPane locoScroll;

    String[] speed = {"126", "14", "27", "28"};
    String[] fmt = {"DCC", "MM1", "MM2"}; // NOI18N

    JTextField addr = new JTextField(5);
    JTextField name = new JTextField(10);
    JComboBox<String> speedBox = new JComboBox<String>(speed);
    JComboBox<String> formatBox = new JComboBox<String>(fmt);
    JButton addButton = new JButton(Bundle.getMessage("AddLoco"));

    public LocoDataPane() {
        super();
    }

    @Override
    public void initComponents(jmri.jmrix.tams.TamsSystemConnectionMemo memo) {
        super.initComponents(memo);

        locoModel = new LocoDataModel(128, 16, memo);
        locoTable = new JTable(locoModel);
        locoTable.setRowSorter(new TableRowSorter<>(locoModel));
        locoScroll = new JScrollPane(locoTable);

        locoModel.configureTable(locoTable);

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(new JLabel(Bundle.getMessage("Address"))); // TODO reuse props key in NBB/JmrixBundle?
        pane1.add(addr);
        pane1.add(new JLabel(Bundle.getMessage("Name"))); // TODO reuse props key in NBB/JmrixBundle?
        pane1.add(name);
        pane1.add(new JLabel(Bundle.getMessage("Steps")));
        pane1.add(speedBox);
        pane1.add(new JLabel(Bundle.getMessage("Format")));
        pane1.add(formatBox);
        pane1.add(addButton);

        // add listener object so checkboxes function
        addButton.addActionListener(new ActionListener() {
            @Override
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
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNullAddress"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;

        }
        try {
            Integer.valueOf(addr.getText());
        } catch (NumberFormatException nx) {
            log.error("Unable to convert {} to a number", addr.getText());
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNotNumber"), Bundle.getMessage("ErrorTitle"),
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

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.tams.swing.locodatabase.LocoDataFrame";
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("Title");
    }

    @Override
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

        public Default() {
            super(Bundle.getMessage("Title"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    LocoDataPane.class.getName(),
                    jmri.InstanceManager.getDefault(TamsSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocoDataPane.class);

}
