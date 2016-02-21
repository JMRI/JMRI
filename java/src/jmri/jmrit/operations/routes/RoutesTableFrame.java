// RoutesTableFrame.java
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
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.util.com.sun.TableSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for adding and editing the route roster for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class RoutesTableFrame extends OperationsFrame {

    /**
     *
     */
    private static final long serialVersionUID = -5308632111456022575L;

    RoutesTableModel routesModel = new RoutesTableModel();

    // labels
    JLabel textSort = new JLabel(Bundle.getMessage("SortBy"));
    JLabel textSep = new javax.swing.JLabel("          ");

    // radio buttons
    JRadioButton sortByName = new JRadioButton(Bundle.getMessage("Name"));
    JRadioButton sortById = new JRadioButton(Bundle.getMessage("Id"));

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("Add"));

    public RoutesTableFrame() {
        super(Bundle.getMessage("TitleRoutesTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the jtable in a Scroll Pane..
        TableSorter sorter = new TableSorter(routesModel);
        JTable routesTable = new JTable(sorter);
        sorter.setTableHeader(routesTable.getTableHeader());
        JScrollPane routesPane = new JScrollPane(routesTable);
        routesPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        routesModel.initTable(routesTable);
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
        JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
        toolMenu.add(new RouteCopyAction(Bundle.getMessage("MenuItemCopy")));
        toolMenu.add(new SetTrainIconPositionAction(Bundle.getMessage("MenuSetTrainIcon")));
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
            setSize(730, getHeight());
        }

        // create ShutDownTasks
        createShutDownTask();
    }

    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
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
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        // log.debug("route button activated");
        if (ae.getSource() == addButton) {
            RouteEditFrame f = new RouteEditFrame();
            f.initComponents(null);
            f.setTitle(Bundle.getMessage("TitleRouteAdd"));
        }
    }

    public void dispose() {
        routesModel.dispose();
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(RoutesTableFrame.class
            .getName());
}
