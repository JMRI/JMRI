package jmri.jmrit.operations.routes;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.routes.tools.ExportRoutesAction;
import jmri.jmrit.operations.routes.tools.PrintRoutesAction;
import jmri.jmrit.operations.routes.tools.RouteCopyAction;
import jmri.jmrit.operations.routes.tools.SetTrainIconPositionAction;
import jmri.jmrit.operations.setup.Control;
import jmri.swing.JTablePersistenceManager;

/**
 * Frame for adding and editing the route roster for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008, 2009
 */
public class RoutesTableFrame extends OperationsFrame {

    RoutesTableModel routesModel = new RoutesTableModel();
    JTable routesTable;

    // labels
    JLabel textSort = new JLabel(Bundle.getMessage("SortBy"));
    JLabel textSep = new JLabel("          ");

    // radio buttons
    JRadioButton sortByName = new JRadioButton(Bundle.getMessage("Name"));
    JRadioButton sortById = new JRadioButton(Bundle.getMessage("Id"));

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));

    public RoutesTableFrame() {
        super(Bundle.getMessage("TitleRoutesTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the jtable in a Scroll Pane..
        routesTable = new JTable(routesModel);
        JScrollPane routesPane = new JScrollPane(routesTable);
        routesPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        routesModel.initTable(this, routesTable);
        getContentPane().add(routesPane);

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
        addButton.setToolTipText(Bundle.getMessage("AddRoute"));

        addRadioButtonAction(sortByName);
        addRadioButtonAction(sortById);
        
        ButtonGroup bGroup = new ButtonGroup();
        bGroup.add(sortByName);
        bGroup.add(sortById);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new RouteCopyAction());
        toolMenu.add(new SetTrainIconPositionAction());
        toolMenu.add(new ExportRoutesAction());
        toolMenu.addSeparator();
        toolMenu.add(new PrintRoutesAction(false));
        toolMenu.add(new PrintRoutesAction(true));

        menuBar.add(toolMenu);
        menuBar.add(new jmri.jmrit.operations.OperationsMenu());
        setJMenuBar(menuBar);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_Routes", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight300));
 
        // create ShutDownTasks
        createShutDownTask();
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        // clear any sorts by column
        clearTableSort(routesTable);
        if (ae.getSource() == sortByName) {
            routesModel.setSort(routesModel.SORTBYNAME);
        }
        if (ae.getSource() == sortById) {
            routesModel.setSort(routesModel.SORTBYID);
        }
    }

    // add button
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        // log.debug("route button activated");
        if (ae.getSource() == addButton) {
            RouteEditFrame f = new RouteEditFrame();
            f.initComponents(null);
        }
    }

    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(routesTable);
        });
        routesModel.dispose();
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(RoutesTableFrame.class);
}
