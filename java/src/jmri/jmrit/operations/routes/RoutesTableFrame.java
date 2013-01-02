// RoutesTableFrame.java

package jmri.jmrit.operations.routes;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JRadioButton;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.setup.Control;
import jmri.util.com.sun.TableSorter;

/**
 * Frame for adding and editing the route roster for operations.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class RoutesTableFrame extends OperationsFrame {

	RoutesTableModel routesModel = new RoutesTableModel();

	// labels
	JLabel textSort = new JLabel(Bundle.getString("SortBy"));
	JLabel textSep = new javax.swing.JLabel("          ");

	// radio buttons
	JRadioButton sortByName = new JRadioButton(Bundle.getString("Name"));
	JRadioButton sortById = new JRadioButton(Bundle.getString("Id"));

	// major buttons
	JButton addButton = new JButton(Bundle.getString("Add"));

	public RoutesTableFrame() {
		super(Bundle.getString("TitleRoutesTable"));
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
		controlPanel.setMaximumSize(new Dimension(Control.panelWidth, 50));

		getContentPane().add(controlPanel);

		sortByName.setSelected(true);

		// setup buttons
		addButtonAction(addButton);

		addRadioButtonAction(sortByName);
		addRadioButtonAction(sortById);

		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getString("Tools"));
		toolMenu.add(new RouteCopyAction(Bundle.getString("MenuItemCopy")));
		toolMenu.add(new SetTrainIconPositionAction(Bundle.getString("MenuSetTrainIcon")));
		toolMenu.add(new PrintRoutesAction(Bundle.getString("MenuItemPrint"), false));
		toolMenu.add(new PrintRoutesAction(Bundle.getString("MenuItemPreview"), true));

		menuBar.add(toolMenu);
		menuBar.add(new jmri.jmrit.operations.OperationsMenu());
		setJMenuBar(menuBar);

		// add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Routes", true); // NOI18N

		pack();
		setSize(730, getHeight());

		// now load the cars and engines
		CarManagerXml.instance();
		EngineManagerXml.instance();
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
			f.setTitle(Bundle.getString("TitleRouteAdd"));
			f.setVisible(true);
		}
	}

	public void dispose() {
		routesModel.dispose();
		super.dispose();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RoutesTableFrame.class
			.getName());
}
