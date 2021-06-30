package jmri.jmrit.operations.routes.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.swing.JTablePersistenceManager;

/**
 * Frame for user edit of q route's blocking order.
 *
 * @author Dan Boudreau Copyright (C) 2021
 */
public class RouteBlockingOrderEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    RouteBlockingOrderEditTableModel routeModel = new RouteBlockingOrderEditTableModel();
    JTable routeTable = new JTable(routeModel);
    JScrollPane routePane;

    RouteManager routeManager;

    Route _route = null;
    Train _train = null;

    // major buttons
    JButton saveRouteButton = new JButton(Bundle.getMessage("SaveRoute"));
    JButton resetRouteButton = new JButton(Bundle.getMessage("ButtonReset"));
    
    JLabel routeName = new JLabel();
    JLabel routeComment = new JLabel();

    public RouteBlockingOrderEditFrame(Route route) {
        super(Bundle.getMessage("MenuBlockingOrder"));
        initComponents(route);
    }

    private void initComponents(Route route) {
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
            routeName.setText(_route.getName());
            routeComment.setText(_route.getComment());
            enableButtons(!route.getStatus().equals(Route.TRAIN_BUILT)); // do not allow user to modify a built train
        }

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        JScrollPane p1Pane = new JScrollPane(p1);
        p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        p1Pane.setMaximumSize(new Dimension(2000, 200));
        p1Pane.setBorder(BorderFactory.createTitledBorder(""));

        // name panel
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, routeName, 0, 0);

        // comment panel
        JPanel pComment = new JPanel();
        pComment.setLayout(new GridBagLayout());
        pComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        addItem(pComment, routeComment, 0, 0);

        p1.add(pName);
        p1.add(pComment);
        
        JPanel pF = new JPanel();
        addItem(pF, new JLabel(Bundle.getMessage("TrainFront")), 0, 0);
        
        JPanel pR = new JPanel();
        addItem(pR, new JLabel(Bundle.getMessage("TrainRear")), 0, 0);

        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        JScrollPane pBPane = new JScrollPane(pB);
        pBPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        pBPane.setMaximumSize(new Dimension(2000, 200));
        pBPane.setBorder(BorderFactory.createTitledBorder(""));

        addItem(pB, resetRouteButton, 2, 0);
        addItem(pB, saveRouteButton, 3, 0);

        getContentPane().add(p1Pane);
        getContentPane().add(pF);
        getContentPane().add(routePane);
        getContentPane().add(pR);
        getContentPane().add(pBPane);

        // setup buttons
        addButtonAction(resetRouteButton);
        addButtonAction(saveRouteButton);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_RouteBlockingOrder", true); // NOI18N

        // set frame size and route for display
        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight400));
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveRouteButton) {
            log.debug("route save button activated");
            saveRoute();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == resetRouteButton) {
            log.debug("route reset button activated");
            if (_route != null) {
                _route.resetBlockingOrder();
            }
        }
    }

    private void saveRoute() {
        if (routeTable.isEditing()) {
            log.debug("route table edit true");
            routeTable.getCellEditor().stopCellEditing();
        }
        // save route file
        OperationsXml.save();
    }

    private void enableButtons(boolean enabled) {
        resetRouteButton.setEnabled(enabled);
        saveRouteButton.setEnabled(enabled);
        routeTable.setEnabled(enabled);
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

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Route.ROUTE_STATUS_CHANGED_PROPERTY)) {
            enableButtons(!_route.getStatus().equals(Route.TRAIN_BUILT)); // do not allow user to modify a built train
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RouteBlockingOrderEditFrame.class);
}
