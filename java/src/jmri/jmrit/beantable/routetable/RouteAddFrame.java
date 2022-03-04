package jmri.jmrit.beantable.routetable;

import jmri.InstanceManager;
import jmri.Route;
import jmri.routeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Add frame for the Route Table.
 *
 * Split from {@link jmri.jmrit.beantable.RouteTableAction}
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016
 * @author Paul Bender Copyright (C) 2020
 */
public class RouteAddFrame extends AbstractRouteAddEditFrame {


    public RouteAddFrame() {
        this(Bundle.getMessage("TitleAddRoute"));
    }

    public RouteAddFrame(String name) {
        this(name,false,true);
    }

    public RouteAddFrame(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
        super.initComponents();
    }

    @Override
    protected JPanel getButtonPanel() {
        final JButton copyButton = new JButton(Bundle.getMessage("ButtonCopyRoute"));
        final JButton createButton = new JButton(Bundle.getMessage("ButtonCreate"));
        final JButton editButton = new JButton(Bundle.getMessage("ButtonEdit"));
        final JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        final JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));

        // add Buttons panel
        JPanel pb = new JPanel();
        pb.setLayout(new BorderLayout());

        // Copy button
        JPanel left = new JPanel();
        left.add(copyButton);
        copyButton.addActionListener(this::copyPressed);

        JPanel right = new JPanel();
        right.setLayout(new FlowLayout(FlowLayout.TRAILING));

        // Cancel (Add) button
        right.add(cancelButton);
        cancelButton.addActionListener(this::cancelAddPressed);

        // Create button
        right.add(createButton);
        createButton.addActionListener(this::createPressed);
        createButton.setToolTipText(Bundle.getMessage("TooltipCreateRoute"));

        pb.add(left, BorderLayout.WEST);
        pb.add(right, BorderLayout.EAST);

        // Show the initial buttons, and hide the others
        copyButton.setVisible(true);
        cancelButton.setVisible(true);
        updateButton.setVisible(false);
        editButton.setVisible(false);
        createButton.setVisible(true);
        return pb;
    }

    /**
     * Respond to the CancelAdd button.
     *
     * @param e the action event
     */
    private void cancelAddPressed(ActionEvent e) {
        cancelAdd();
    }

    /**
     * Cancel Add mode.
     */
    private void cancelAdd() {
        if (routeDirty) {
            showReminderMessage();
        }
        curRoute = null;
        finishUpdate();
        status1.setText(Bundle.getMessage("RouteAddStatusInitial1", Bundle.getMessage("ButtonCreate"))); // I18N to include original button name in help string
        //status2.setText(Bundle.getMessage("RouteAddStatusInitial2", Bundle.getMessage("ButtonEdit")));
        routeDirty = false;
        // hide addFrame
        setVisible(false);
        _routeSensorModel.dispose();
        _routeTurnoutModel.dispose();
        closeFrame();
    }

    /**
     * Respond to the Create button.
     *
     * @param e the action event
     */
    private void createPressed(ActionEvent e) {
        if (!_autoSystemName.isSelected()) {
            if (!checkNewNamesOK()) {
                return;
            }
        }
        updatePressed(true); // close pane after creating
        //status2.setText(Bundle.getMessage("RouteAddStatusInitial2", Bundle.getMessage("ButtonEdit")));
        pref.setSimplePreferenceState(systemNameAuto, _autoSystemName.isSelected());
        // activate the route
        if (curRoute != null) {
            curRoute.activateRoute();
        }
        closeFrame();
    }

    /**
     * Respond to the Copy button.
     * Request a source route for the copy and load the page if possible.
     *
     * @param e the action event
     */
    private void copyPressed(ActionEvent e) {
        var selectedRoute = selectSourceRoute();
        if (selectedRoute != null) {
            var route = InstanceManager.getDefault(RouteManager.class).getRoute(selectedRoute);
            if (route != null) {
                setPageContent(route);
            }
        }
    }

    /**
     * Display an input dialog with a combo box of existing routes.
     *
     * @return the selected route name or null if none.
     */
    private String selectSourceRoute() {
        var routes = InstanceManager.getDefault(RouteManager.class).getNamedBeanSet();
        var routeList = new ArrayList<String>();
        routes.forEach((r) -> {
            routeList.add(r.getDisplayName());
        });
        Collections.sort(routeList);

        var routeArray = new String[routeList.size()];
        routeList.toArray(routeArray);

        try {
            var choice = JOptionPane.showInputDialog(
                    null,
                    Bundle.getMessage("LabelCopyRoute"),
                    Bundle.getMessage("TitleCopyRoute"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    routeArray,
                    null);
            return (String) choice;
        } catch (HeadlessException ex) {
            return null;
        }
    }

    /**
     * Check name for a new Route object using the _systemName field on the addFrame pane.
     *
     * @return whether name entered is allowed
     */
    private boolean checkNewNamesOK() {
        // Get system name and user name from Add Route pane
        String sName = _systemName.getText();
        String uName = _userName.getText();
        if (sName.length() == 0) {
            status1.setText(Bundle.getMessage("AddBeanStatusEnter"));
            status1.setForeground(Color.red);
            return false;
        }
        Route g;
        // check if a Route with the same user name exists
        if (!uName.isEmpty()) {
            g = routeManager.getByUserName(uName);
            if (g != null) {
                // Route already exists
                status1.setText(Bundle.getMessage("LightError8"));
                return false;
            }
        }
        // check if a Route with this system name already exists
        sName = routeManager.makeSystemName(sName);
        g = routeManager.getBySystemName(sName);
        if (g != null) {
            // Route already exists
            status1.setText(Bundle.getMessage("LightError1"));
            return false;
        }
        return true;
    }

}
