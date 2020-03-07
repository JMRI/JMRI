package jmri.jmrit.operations.automation;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.swing.JTablePersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for adding and editing the automation roster for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2016
 */
public class AutomationsTableFrame extends OperationsFrame {

    AutomationsTableModel automationsModel = new AutomationsTableModel();
    javax.swing.JTable automationsTable = new javax.swing.JTable(automationsModel);
    JScrollPane automationsPane;

    // labels
    javax.swing.JLabel textSort = new javax.swing.JLabel();
    javax.swing.JLabel textSep = new javax.swing.JLabel();

    // radio buttons
    javax.swing.JRadioButton sortByNameRadioButton = new javax.swing.JRadioButton(Bundle.getMessage("Name"));
    javax.swing.JRadioButton sortByIdRadioButton = new javax.swing.JRadioButton(Bundle.getMessage("Id"));

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("AddAutomation"));
    
    public AutomationsTableFrame() {
        super(Bundle.getMessage("TitleAutomationsTableFrame"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the jtable in a Scroll Pane..
        automationsPane = new JScrollPane(automationsTable);
        automationsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        automationsPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        automationsModel.initTable(this, automationsTable);
        getContentPane().add(automationsPane);

        // Set up the control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        textSort.setText(Bundle.getMessage("SortBy"));
        controlPanel.add(textSort);
        controlPanel.add(sortByNameRadioButton);
        sortByNameRadioButton.setSelected(true);
        controlPanel.add(sortByIdRadioButton);
        textSep.setText("          ");
        controlPanel.add(textSep);
        controlPanel.add(addButton);

        controlPanel.setMaximumSize(new Dimension(Control.panelWidth1025, 50));
        getContentPane().add(controlPanel);

        // setup buttons
        addButtonAction(addButton);
        addRadioButtonAction(sortByNameRadioButton);
        addRadioButtonAction(sortByIdRadioButton);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        menuBar.add(toolMenu);
        toolMenu.add(new AutomationCopyAction());
        toolMenu.add(new AutomationResumeAction());
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Automation", true); // NOI18N

        initMinimumSize();
        // make panel a bit wider than minimum if the very first time opened
        if (getWidth() == Control.panelWidth500) {
            setSize(Control.panelWidth1025, getHeight());
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        // clear any sorts by column
        clearTableSort(automationsTable);
        if (ae.getSource() == sortByNameRadioButton) {
            sortByNameRadioButton.setSelected(true);
            sortByIdRadioButton.setSelected(false);
            automationsModel.setSort(automationsModel.SORTBYNAME);
        }
        if (ae.getSource() == sortByIdRadioButton) {
            sortByNameRadioButton.setSelected(false);
            sortByIdRadioButton.setSelected(true);
            automationsModel.setSort(automationsModel.SORTBYID);
        }
    }

    // add button
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("add automation button activated");
        if (ae.getSource() == addButton) {
            new AutomationTableFrame(null);
        }
    }
    
    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(automationsTable);
        });
        automationsModel.dispose();
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(AutomationsTableFrame.class);
}
