package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import jmri.InstanceManager;
import jmri.Route;
import jmri.jmrit.beantable.routetable.RouteAddFrame;
import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a Route Table.
 *
 * Based in part on {@link SignalHeadTableAction} by Bob Jacobsen
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016
 * @author Paul Bender Colyright (C) 2020
 */
public class RouteTableAction extends AbstractTableAction<Route> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s title of the action
     */
    public RouteTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Route manager available
        if (InstanceManager.getNullableDefault(jmri.RouteManager.class) == null) {
            super.setEnabled(false);
        }
    }

    public RouteTableAction() {
        this(Bundle.getMessage("TitleRouteTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Routes.
     */
    @Override
    protected void createModel() {
        m = new RouteTableDataModel();
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleRouteTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.RouteTable";
    }


    @Override
    protected void addPressed(ActionEvent e) {

        final JmriJFrame addFrame = new RouteAddFrame();
        // display the window
        addFrame.setVisible(true);
        addFrame.setEscapeKeyClosesWindow(true);
    }

    @Override
    public void setMessagePreferencesDetails() {
        InstanceManager.getDefault(jmri.UserPreferencesManager.class)
                .setPreferenceItemDetails(getClassName(), "remindSaveRoute", Bundle.getMessage("HideSaveReminder"));
        super.setMessagePreferencesDetails();
    }

    @Override
    protected String getClassName() {
        return RouteTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleRouteTable");
    }

}
