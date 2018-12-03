package jmri.jmrit.operations.locations.schedules;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.tools.PrintLocationsAction;
import jmri.jmrit.operations.setup.Control;
import jmri.swing.JTablePersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for adding and editing the Schedule roster for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2009, 2012
 */
public class SchedulesTableFrame extends OperationsFrame {

    SchedulesTableModel schedulesModel = new SchedulesTableModel();
    javax.swing.JTable schedulesTable = new javax.swing.JTable(schedulesModel);
    JScrollPane schedulesPane;

    // labels
    javax.swing.JLabel textSort = new javax.swing.JLabel();
    javax.swing.JLabel textSep = new javax.swing.JLabel();

    // radio buttons
    javax.swing.JRadioButton sortByName = new javax.swing.JRadioButton(Bundle.getMessage("Name"));
    javax.swing.JRadioButton sortById = new javax.swing.JRadioButton(Bundle.getMessage("Id"));

    // major buttons
    // javax.swing.JButton addButton = new javax.swing.JButton();
    public SchedulesTableFrame() {
        super(Bundle.getMessage("TitleSchedulesTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the jtable in a Scroll Pane..
        schedulesPane = new JScrollPane(schedulesTable);
        schedulesPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        schedulesPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        schedulesModel.initTable(this, schedulesTable);
        getContentPane().add(schedulesPane);

        // Set up the control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        textSort.setText(Bundle.getMessage("SortBy"));
        controlPanel.add(textSort);
        controlPanel.add(sortByName);
        sortByName.setSelected(true);
        controlPanel.add(sortById);
        textSep.setText("          ");
        controlPanel.add(textSep);

        // TODO allow user to add schedule to a spur
        // addButton.setText(Bundle.getMessage("Add"));
        // addButton.setVisible(true);
        // controlPanel.add (addButton);
        controlPanel.setMaximumSize(new Dimension(Control.panelWidth1025, 50));
        getContentPane().add(controlPanel);

        // set up buttons
        // addButtonAction(addButton);
        addRadioButtonAction(sortByName);
        addRadioButtonAction(sortById);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new ScheduleCopyAction());
        toolMenu.add(new SchedulesByLoadAction(Bundle.getMessage("MenuItemShowSchedulesByLoad")));
        toolMenu.add(new SchedulesResetHitsAction(Bundle.getMessage("MenuItemResetHits")));
        toolMenu.add(new ExportSchedulesAction(Bundle.getMessage("MenuItemExportSchedules")));
        toolMenu.addSeparator();
        toolMenu.add(new PrintLocationsAction(Bundle.getMessage("MenuItemPrint"), false));
        toolMenu.add(new PrintLocationsAction(Bundle.getMessage("MenuItemPreview"), true));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_LocationSchedules", true); // NOI18N

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
        clearTableSort(schedulesTable);
        if (ae.getSource() == sortByName) {
            sortByName.setSelected(true);
            sortById.setSelected(false);
            schedulesModel.setSort(schedulesModel.SORTBYNAME);
        }
        if (ae.getSource() == sortById) {
            sortByName.setSelected(false);
            sortById.setSelected(true);
            schedulesModel.setSort(schedulesModel.SORTBYID);
        }
    }

    // add button
    // public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
    // log.debug("add schedule button activated");
    // if (ae.getSource() == addButton){
    // ScheduleEditFrame f = new ScheduleEditFrame();
    // f.setTitle(MessageFormat.format(Bundle.getMessage("TitleScheduleAdd"), new Object[]{"Track Name"}));
    // f.initComponents(null, null, null);
    // }
    // }
    @Override
    public void dispose() {
        schedulesModel.dispose();
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(schedulesTable);
        });
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SchedulesTableFrame.class);
}
