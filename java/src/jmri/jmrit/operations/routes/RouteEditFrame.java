package jmri.jmrit.operations.routes;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.swing.JTablePersistenceManager;

/**
 * Frame for user edit of route
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2014, 2016
 */
public class RouteEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    RouteEditTableModel routeModel = new RouteEditTableModel();
    JTable routeTable = new JTable(routeModel);
    JScrollPane routePane;

    RouteManager routeManager;

    Route _route = null;
    Train _train = null;

    // major buttons
    JButton addLocationButton = new JButton(Bundle.getMessage("AddLocation"));
    JButton saveRouteButton = new JButton(Bundle.getMessage("SaveRoute"));
    JButton deleteRouteButton = new JButton(Bundle.getMessage("DeleteRoute"));
    JButton addRouteButton = new JButton(Bundle.getMessage("AddRoute"));

    // radio buttons
    JRadioButton addLocAtTop = new JRadioButton(Bundle.getMessage("Top"));
    JRadioButton addLocAtMiddle = new JRadioButton(Bundle.getMessage("Middle"));
    JRadioButton addLocAtBottom = new JRadioButton(Bundle.getMessage("Bottom"));

    JRadioButton showWait = new JRadioButton(Bundle.getMessage("Wait"));
    JRadioButton showDepartTime = new JRadioButton(Bundle.getMessage("DepartTime"));

    // text field
    JTextField routeNameTextField = new JTextField(Control.max_len_string_route_name);
    JTextField commentTextField = new JTextField(35);

    // combo boxes
    JComboBox<Location> locationBox = InstanceManager.getDefault(LocationManager.class).getComboBox();
    
    JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));

    public static final String NAME = Bundle.getMessage("Name");
    public static final String DISPOSE = "dispose"; // NOI18N

    public RouteEditFrame() {
        super(Bundle.getMessage("TitleRouteEdit"));
    }

    public void initComponents(Route route, Train train) {
        _train = train; // assign route to this train
        initComponents(route);
    }

    public void initComponents(Route route) {

        _route = route;

        // load managers
        routeManager = InstanceManager.getDefault(RouteManager.class);

        // Set up the jtable in a Scroll Pane..
        routePane = new JScrollPane(routeTable);
        routePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        routePane.setBorder(BorderFactory.createTitledBorder(""));
        
        routeModel.initTable(this, routeTable, _route);

        if (_route != null) {
            _route.addPropertyChangeListener(this);
            routeNameTextField.setText(_route.getName());
            commentTextField.setText(_route.getComment());
            enableButtons(!route.getStatus().equals(Route.TRAIN_BUILT)); // do not allow user to modify a built train
            addRouteButton.setEnabled(false); // override and disable
        } else {
            setTitle(Bundle.getMessage("TitleRouteAdd"));
            enableButtons(false);
        }

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        JScrollPane p1Pane = new JScrollPane(p1);
        p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        p1Pane.setMinimumSize(new Dimension(300, 3 * routeNameTextField.getPreferredSize().height));
        p1Pane.setMaximumSize(new Dimension(2000, 200));
        p1Pane.setBorder(BorderFactory.createTitledBorder(""));

        // name panel
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, routeNameTextField, 0, 0);

        // comment panel
        JPanel pComment = new JPanel();
        pComment.setLayout(new GridBagLayout());
        pComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        addItem(pComment, commentTextField, 0, 0);

        p1.add(pName);
        p1.add(pComment);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        JScrollPane p2Pane = new JScrollPane(p2);
        p2Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        p2Pane.setMinimumSize(new Dimension(300, 3 * routeNameTextField.getPreferredSize().height));
        p2Pane.setMaximumSize(new Dimension(2000, 200));
        p2Pane.setBorder(BorderFactory.createTitledBorder(""));

        // location panel
        JPanel pLoc = new JPanel();
        pLoc.setLayout(new GridBagLayout());
        pLoc.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        addItem(pLoc, locationBox, 0, 1);
        addItem(pLoc, addLocationButton, 1, 1);
        addItem(pLoc, addLocAtTop, 2, 1);
        addItem(pLoc, addLocAtMiddle, 3, 1);
        addItem(pLoc, addLocAtBottom, 4, 1);

        // Wait or Depart Time panel
        JPanel pWait = new JPanel();
        pWait.setLayout(new GridBagLayout());
        pWait.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Display")));
        addItem(pWait, showWait, 0, 1);
        addItem(pWait, showDepartTime, 1, 1);

        p2.add(pLoc);
        p2.add(pWait);

        // row 12 buttons
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        JScrollPane pBPane = new JScrollPane(pB);
        pBPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        pBPane.setMinimumSize(new Dimension(300, 3 * routeNameTextField.getPreferredSize().height));
        pBPane.setMaximumSize(new Dimension(2000, 200));
        pBPane.setBorder(BorderFactory.createTitledBorder(""));

        addItem(pB, deleteRouteButton, 0, 0);
        addItem(pB, addRouteButton, 1, 0);
        addItem(pB, saveRouteButton, 3, 0);

        getContentPane().add(p1Pane);
        getContentPane().add(routePane);
        getContentPane().add(p2Pane);
        getContentPane().add(pBPane);

        // setup buttons
        addButtonAction(addLocationButton);
        addButtonAction(deleteRouteButton);
        addButtonAction(addRouteButton);
        addButtonAction(saveRouteButton);

        // setup radio buttons       
        ButtonGroup group = new ButtonGroup();
        group.add(addLocAtTop);
        group.add(addLocAtMiddle);
        group.add(addLocAtBottom);
        addLocAtBottom.setSelected(true);
        
        addRadioButtonAction(addLocAtTop); // to clear table row sorting
        addRadioButtonAction(addLocAtMiddle);
        addRadioButtonAction(addLocAtBottom); // to clear table row sorting
        
        ButtonGroup groupTime = new ButtonGroup();
        groupTime.add(showWait);
        groupTime.add(showDepartTime);
        addRadioButtonAction(showWait);
        addRadioButtonAction(showDepartTime);
        setTimeWaitRadioButtons();

        // build menu
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(toolMenu);
        loadToolMenu();
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_EditRoute", true); // NOI18N

        // get notified if combo box gets modified
        InstanceManager.getDefault(LocationManager.class).addPropertyChangeListener(this);

        // set frame size and route for display
        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight400));
    }
    
    private void loadToolMenu() {
        toolMenu.removeAll();
        toolMenu.add(new RouteCopyAction(Bundle.getMessage("MenuItemCopy"), _route));
        toolMenu.add(new SetTrainIconRouteAction(Bundle.getMessage("MenuSetTrainIconRoute"), _route));
        toolMenu.addSeparator();
        toolMenu.add(new PrintRouteAction(Bundle.getMessage("MenuItemPrint"), false, _route));
        toolMenu.add(new PrintRouteAction(Bundle.getMessage("MenuItemPreview"), true, _route));
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addLocationButton) {
            log.debug("route add location button activated");
            if (locationBox.getSelectedItem() != null) {
                addNewRouteLocation();
            }
        }
        if (ae.getSource() == saveRouteButton) {
            log.debug("route save button activated");
            Route route = routeManager.getRouteByName(routeNameTextField.getText());
            if (_route == null && route == null) {
                saveNewRoute();
            } else {
                if (route != null && route != _route) {
                    reportRouteExists(Bundle.getMessage("save"));
                    return;
                }
                saveRoute();
            }
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == deleteRouteButton) {
            log.debug("route delete button activated");
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("AreYouSure?"),
                    new Object[]{routeNameTextField.getText()}), Bundle.getMessage("DeleteRoute?"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            Route route = routeManager.getRouteByName(routeNameTextField.getText());
            if (route == null) {
                return;
            }

            routeManager.deregister(route);
            _route = null;

            enableButtons(false);
            routeModel.dispose();
            // save route file
            OperationsXml.save();
        }
        if (ae.getSource() == addRouteButton) {
            Route route = routeManager.getRouteByName(routeNameTextField.getText());
            if (route != null) {
                reportRouteExists(Bundle.getMessage("add"));
                return;
            }
            saveNewRoute();
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        routeModel.setWait(showWait.isSelected());
    }

    private void addNewRouteLocation() {
        if (routeTable.isEditing()) {
            log.debug("route table edit true");
            routeTable.getCellEditor().stopCellEditing();
        }
        // add location to this route
        Location l = (Location) locationBox.getSelectedItem();
        RouteLocation rl;
        if (addLocAtTop.isSelected()) {
            // add location to start
            rl = _route.addLocation(l, Route.START);
        } else if (addLocAtMiddle.isSelected()) {
            // add location to middle
            if (routeTable.getSelectedRow() >= 0) {
                int row = routeTable.getSelectedRow();
                rl = _route.addLocation(l, row + Route.START);
                // we need to reselect the table since the content has changed
                routeTable.getSelectionModel().setSelectionInterval(row + Route.START, row + Route.START);
            } else {
                rl = _route.addLocation(l, _route.size()/2 + Route.START);
            }
        } else {
            // add location to end
            rl = _route.addLocation(l);
        }
        rl.setTrainDirection(routeModel.getLastTrainDirection());
        rl.setMaxTrainLength(routeModel.getLastMaxTrainLength());
        if (rl.getLocation().isStaging()) {
            rl.setMaxCarMoves(50);
        } else {
            rl.setMaxCarMoves(routeModel.getLastMaxTrainMoves());
        }
        // set train icon location
        rl.setTrainIconCoordinates();
    }

    private void saveNewRoute() {
        if (!checkName(Bundle.getMessage("add"))) {
            return;
        }
        Route route = routeManager.newRoute(routeNameTextField.getText());
        routeModel.initTable(this, routeTable, route);
        _route = route;
        enableButtons(true);
        // assign route to a train?
        if (_train != null) {
            _train.setRoute(route);
        }
        if (_route != null) {
            _route.addPropertyChangeListener(this);
        }
        saveRoute();
        loadToolMenu();
    }

    private void saveRoute() {
        if (!checkName(Bundle.getMessage("save"))) {
            return;
        }
        _route.setName(routeNameTextField.getText());
        _route.setComment(commentTextField.getText());

        if (routeTable.isEditing()) {
            log.debug("route table edit true");
            routeTable.getCellEditor().stopCellEditing();
        }

        // save route file
        OperationsXml.save();
    }

    /**
     *
     * @return true if name is length is okay
     */
    private boolean checkName(String s) {
        if (routeNameTextField.getText().trim().equals("")) {
            log.debug("Must enter a name for the route");
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"), MessageFormat.format(Bundle
                    .getMessage("CanNotRoute"), new Object[]{s}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (routeNameTextField.getText().length() > Control.max_len_string_route_name) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("RouteNameLess"),
                    new Object[]{Control.max_len_string_route_name + 1}), MessageFormat.format(Bundle
                            .getMessage("CanNotRoute"), new Object[]{s}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void reportRouteExists(String s) {
        log.info("Can not " + s + ", route already exists");
        JOptionPane.showMessageDialog(this, Bundle.getMessage("ReportExists"), MessageFormat.format(Bundle
                .getMessage("CanNotRoute"), new Object[]{s}), JOptionPane.ERROR_MESSAGE);
    }

    private void enableButtons(boolean enabled) {
        toolMenu.setEnabled(enabled);
        locationBox.setEnabled(enabled);
        addLocationButton.setEnabled(enabled);
        addLocAtTop.setEnabled(enabled);
        addLocAtMiddle.setEnabled(enabled);
        addLocAtBottom.setEnabled(enabled);
        saveRouteButton.setEnabled(enabled);
        deleteRouteButton.setEnabled(enabled);
        routeTable.setEnabled(enabled);
        showWait.setEnabled(enabled);
        showDepartTime.setEnabled(enabled);
        // the inverse!
        addRouteButton.setEnabled(!enabled);
    }

    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(routeTable);
        });
        if (_route != null) {
            _route.removePropertyChangeListener(this);
        }
        routeModel.dispose();
        super.dispose();
    }

    private void updateComboBoxes() {
        InstanceManager.getDefault(LocationManager.class).updateComboBox(locationBox);
    }

    // if the route has a departure time in the first location set the showDepartTime radio button
    private void setTimeWaitRadioButtons() {
        showWait.setSelected(true);
        if (_route != null) {
            RouteLocation rl = _route.getDepartsRouteLocation();
            if (rl != null && !rl.getDepartureTime().equals(RouteLocation.NONE)) {
                showDepartTime.setSelected(true);
            }
            routeModel.setWait(showWait.isSelected());
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateComboBoxes();
        }
        if (e.getPropertyName().equals(Route.ROUTE_STATUS_CHANGED_PROPERTY)) {
            enableButtons(!_route.getStatus().equals(Route.TRAIN_BUILT)); // do not allow user to modify a built train
            addRouteButton.setEnabled(false); // override and disable
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RouteEditFrame.class);
}
