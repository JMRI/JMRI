package jmri.jmrit.operations.routes;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;

/**
 * Frame for copying a route for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 */
public class RouteCopyFrame extends OperationsFrame {

    RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);

    // labels
    javax.swing.JLabel textCopyRoute = new javax.swing.JLabel(Bundle.getMessage("CopyRoute"));
    javax.swing.JLabel textRouteName = new javax.swing.JLabel(Bundle.getMessage("RouteName"));

    // text field
    javax.swing.JTextField routeNameTextField = new javax.swing.JTextField(Control.max_len_string_route_name);

    // check boxes
    javax.swing.JCheckBox invertCheckBox = new javax.swing.JCheckBox(Bundle.getMessage("Invert"));

    // major buttons
    javax.swing.JButton copyButton = new javax.swing.JButton(Bundle.getMessage("ButtonCopy"));

    // combo boxes
    JComboBox<Route> routeBox = InstanceManager.getDefault(RouteManager.class).getComboBox();
    
    public RouteCopyFrame(Route route) {
        super(Bundle.getMessage("TitleRouteCopy"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());

        // Layout the panel by rows
        // row 1 textRouteName
        addItem(p1, textRouteName, 0, 1);
        addItemWidth(p1, routeNameTextField, 3, 1, 1);

        // row 2
        addItem(p1, textCopyRoute, 0, 2);
        addItemWidth(p1, routeBox, 3, 1, 2);

        // row 4
        addItem(p1, invertCheckBox, 0, 4);
        addItem(p1, copyButton, 1, 4);

        getContentPane().add(p1);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_CopyRoute", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight200));

        // setup buttons
        addButtonAction(copyButton);
        
        routeBox.setSelectedItem(route);
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == copyButton) {
            log.debug("copy route button activated");
            if (!checkName()) {
                return;
            }

            Route newRoute = routeManager.getRouteByName(routeNameTextField.getText());
            if (newRoute != null) {
                reportRouteExists(Bundle.getMessage("add"));
                return;
            }
            if (routeBox.getSelectedItem() == null) {
                reportRouteDoesNotExist();
                return;
            }
            Route oldRoute = (Route) routeBox.getSelectedItem();
            if (oldRoute == null) {
                reportRouteDoesNotExist();
                return;
            }

            // now copy
            newRoute = routeManager.copyRoute(oldRoute, routeNameTextField.getText(),
                    invertCheckBox.isSelected());

            RouteEditFrame f = new RouteEditFrame();
            f.initComponents(newRoute);
        }
    }

    private void reportRouteExists(String s) {
        log.info("Can not " + s + ", route already exists");
        JOptionPane.showMessageDialog(this, Bundle.getMessage("ReportExists"),
                MessageFormat.format(Bundle.getMessage("CanNotRoute"), new Object[]{s}),
                JOptionPane.ERROR_MESSAGE);
    }

    private void reportRouteDoesNotExist() {
        log.debug("route does not exist");
        JOptionPane.showMessageDialog(this, Bundle.getMessage("CopyRoute"),
                Bundle.getMessage("CopyRoute"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     *
     * @return true if name length is okay
     */
    private boolean checkName() {
        if (routeNameTextField.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("EnterRouteName"),
                    Bundle.getMessage("EnterRouteName"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (routeNameTextField.getText().length() > Control.max_len_string_route_name) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(
                    Bundle.getMessage("RouteNameLess"),
                    new Object[]{Control.max_len_string_route_name + 1}), Bundle
                    .getMessage("CanNotAddRoute"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(RouteCopyFrame.class
            .getName());
}
