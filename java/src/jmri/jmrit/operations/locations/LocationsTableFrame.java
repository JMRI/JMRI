package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.schedules.SchedulesTableAction;
import jmri.jmrit.operations.locations.tools.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.swing.JTablePersistenceManager;

/**
 * Frame for adding and editing the location roster for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class LocationsTableFrame extends OperationsFrame {

    LocationsTableModel locationsModel = new LocationsTableModel();
    javax.swing.JTable locationsTable = new javax.swing.JTable(locationsModel);
    JScrollPane locationsPane;

    // labels
    JLabel textSort = new JLabel(Bundle.getMessage("SortBy"));
    JLabel textSep = new JLabel("          ");

    // radio buttons
    javax.swing.JRadioButton sortByName = new javax.swing.JRadioButton(Bundle.getMessage("Name"));
    javax.swing.JRadioButton sortById = new javax.swing.JRadioButton(Bundle.getMessage("Id"));

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));

    public LocationsTableFrame() {
        super(Bundle.getMessage("TitleLocationsTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the jtable in a Scroll Pane..
        locationsPane = new JScrollPane(locationsTable);
        locationsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        locationsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        locationsModel.initTable(this, locationsTable);
        getContentPane().add(locationsPane);

        // Set up the control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        controlPanel.add(textSort);
        controlPanel.add(sortByName);
        controlPanel.add(sortById);
        controlPanel.add(textSep);
        controlPanel.add(addButton);
        controlPanel.setMaximumSize(new Dimension(Control.panelWidth1025, 50));

        getContentPane().add(controlPanel);

        sortByName.setSelected(true);

        // setup buttons
        addButtonAction(addButton);

        addRadioButtonAction(sortByName);
        addRadioButtonAction(sortById);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new LocationCopyAction());
        toolMenu.add(new TrackCopyAction());
        toolMenu.add(new SchedulesTableAction());
        toolMenu.add(new ModifyLocationsAction());
        toolMenu.add(new ModifyLocationsCarLoadsAction());
        toolMenu.add(new ShowCarsByLocationAction(false, null, null));
        toolMenu.add(new ExportLocationsRosterAction());
        toolMenu.add(new ImportLocationsRosterAction() );
        if (Setup.isVsdPhysicalLocationEnabled()) {
            toolMenu.add(new SetPhysicalLocationAction(null));
        }
        toolMenu.addSeparator();
        toolMenu.add(new PrintLocationsAction(false));
        toolMenu.add(new PrintLocationsAction(true));
        menuBar.add(toolMenu);
        menuBar.add(new jmri.jmrit.operations.OperationsMenu());
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Locations", true); // NOI18N

        initMinimumSize();
        // make panel a bit wider than minimum if the very first time opened
        if (getWidth() == Control.panelWidth500) {
            setSize(Control.panelWidth700, getHeight());
        }

        // create ShutDownTasks
        createShutDownTask();
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        // clear any sorts by column
        clearTableSort(locationsTable);
        if (ae.getSource() == sortByName) {
            sortByName.setSelected(true);
            sortById.setSelected(false);
            locationsModel.setSort(locationsModel.SORTBYNAME);
        }
        if (ae.getSource() == sortById) {
            sortByName.setSelected(false);
            sortById.setSelected(true);
            locationsModel.setSort(locationsModel.SORTBYID);
        }
    }

    // add button
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//  log.debug("location button activated");
        if (ae.getSource() == addButton) {
            LocationEditFrame f = new LocationEditFrame(null);
            f.setTitle(Bundle.getMessage("TitleLocationAdd"));
        }
    }
    
    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(locationsTable);
        });
        locationsModel.dispose();
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(LocationsTableFrame.class);
}
