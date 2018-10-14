package jmri.jmrit.operations.routes;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.swing.JTablePersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    JLabel textSep = new javax.swing.JLabel("          ");

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

        addRadioButtonAction(sortByName);
        addRadioButtonAction(sortById);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new RouteCopyAction(Bundle.getMessage("MenuItemCopy")));
        toolMenu.add(new SetTrainIconPositionAction(Bundle.getMessage("MenuSetTrainIcon")));
        toolMenu.addSeparator();
        toolMenu.add(new PrintRoutesAction(Bundle.getMessage("MenuItemPrint"), false));
        toolMenu.add(new PrintRoutesAction(Bundle.getMessage("MenuItemPreview"), true));

        menuBar.add(toolMenu);
        menuBar.add(new jmri.jmrit.operations.OperationsMenu());
        setJMenuBar(menuBar);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_Routes", true); // NOI18N

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
        clearTableSort(routesTable);
        if (ae.getSource() == sortByName) {
            sortByName.setSelected(true);
            sortById.setSelected(false);
            routesModel.setSort(routesModel.SORTBYNAME);
        }
        if (ae.getSource() == sortById) {
            sortByName.setSelected(false);
            sortById.setSelected(true);
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
            f.setTitle(Bundle.getMessage("TitleRouteAdd"));
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
