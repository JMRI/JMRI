package jmri.jmrix.rps.swing.polling;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SortOrder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.PollingFile;
import jmri.swing.RowSorterUtil;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for user management of RPS polling.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class PollTablePane extends javax.swing.JPanel {

    PollDataModel pollModel = null;
    jmri.ModifiedFlag modifiedFlag;

    /**
     * Constructor method
     */
    public PollTablePane(jmri.ModifiedFlag flag) {
        super();

        this.modifiedFlag = flag;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        pollModel = new PollDataModel(modifiedFlag);

        JTable pollTable = new JTable(pollModel);

        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        pollTable.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        pollTable.setDefaultEditor(JButton.class, buttonEditor);
        pollTable.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        pollTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

        TableRowSorter<PollDataModel> sorter = new TableRowSorter<>(pollModel);
        RowSorterUtil.setSortOrder(sorter, PollDataModel.ADDRCOL, SortOrder.ASCENDING);
        pollTable.setRowSorter(sorter);
        pollTable.setRowSelectionAllowed(false);
        pollTable.setPreferredScrollableViewportSize(new java.awt.Dimension(580, 80));

        JScrollPane scrollPane = new JScrollPane(pollTable);
        add(scrollPane);

        // status info on bottom
        JPanel p = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                int height = getPreferredSize().height;
                int width = super.getMaximumSize().width;
                return new Dimension(width, height);
            }
        };
        p.setLayout(new FlowLayout());

        polling = new JCheckBox(Bundle.getMessage("LabelPoll"));
        polling.setSelected(Engine.instance().getPolling());
        polling.setToolTipText(Bundle.getMessage("PollToolTip"));
        p.add(polling);
        polling.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                modifiedFlag.setModifiedFlag(true);
                checkPolling();
            }
        });

        JPanel m = new JPanel();
        m.setLayout(new BoxLayout(m, BoxLayout.Y_AXIS));
        ButtonGroup g = new ButtonGroup();
        bscMode = new JRadioButton(Bundle.getMessage("LabelBscMode"));
        bscMode.setSelected(Engine.instance().getBscPollMode());
        m.add(bscMode);
        g.add(bscMode);
        bscMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                modifiedFlag.setModifiedFlag(true);
                checkMode();
            }
        });
        directMode = new JRadioButton(Bundle.getMessage("LabelDirectMode"));
        directMode.setSelected(Engine.instance().getDirectPollMode());
        m.add(directMode);
        g.add(directMode);
        directMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                modifiedFlag.setModifiedFlag(true);
                checkMode();
            }
        });
        throttleMode = new JRadioButton(Bundle.getMessage("LabelThrottleMode"));
        throttleMode.setSelected(Engine.instance().getThrottlePollMode());
        m.add(throttleMode);
        g.add(throttleMode);
        throttleMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                modifiedFlag.setModifiedFlag(true);
                checkMode();
            }
        });
        p.add(m);

        p.add(Box.createHorizontalGlue());
        p.add(new JLabel(Bundle.getMessage("LabelDelay")));
        delay = new JTextField(5);
        delay.setText("" + Engine.instance().getPollingInterval());
        delay.setToolTipText(Bundle.getMessage("IntervalToolTip"));
        p.add(delay);
        delay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                modifiedFlag.setModifiedFlag(true);
                updateInterval();
            }
        });

        JButton b = new JButton(Bundle.getMessage("LabelSetDefault"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                modifiedFlag.setModifiedFlag(true);
                setDefaults();
            }
        });
        p.add(b);

        add(p);
    }

    JTextField delay;
    JCheckBox polling;
    JRadioButton bscMode;
    JRadioButton directMode;
    JRadioButton throttleMode;

    /**
     * Save the default value file
     */
    void setDefaults() {
        try {
            File file = new File(PollingFile.defaultFilename());
            log.info("located file {} for store", file);
            // handle the file
            Engine.instance().storePollConfig(file);
            modifiedFlag.setModifiedFlag(false);
        } catch (Exception e) {
            log.error("exception during storeDefault: ", e);
        }
    }

    /**
     * Start or stop the polling
     */
    void checkPolling() {
        Engine.instance().setPolling(polling.isSelected());
    }

    /**
     * Change the polling mode
     */
    void checkMode() {
        if (bscMode.isSelected()) {
            Engine.instance().setBscPollMode();
        } else if (throttleMode.isSelected()) {
            Engine.instance().setThrottlePollMode();
        } else {
            Engine.instance().setDirectPollMode();
        }
    }

    /**
     * The requested interval has changed, update it
     */
    void updateInterval() {
        int interval = Integer.parseInt(delay.getText());
        log.debug("set interval to {}", interval);
        Engine.instance().setPollingInterval(interval);
    }

    public void dispose() {
        pollModel.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(PollTablePane.class);

}
