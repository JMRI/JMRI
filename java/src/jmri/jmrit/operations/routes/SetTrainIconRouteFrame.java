package jmri.jmrit.operations.routes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainIcon;

/**
 * Frame for setting train icon coordinates for a location.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class SetTrainIconRouteFrame extends OperationsFrame implements PropertyChangeListener {

    RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);

    // labels
    JLabel textX = new JLabel("   X  ");
    JLabel textY = new JLabel("   Y  ");

    JLabel routeLocationName = new JLabel();

    // text field
    // check boxes
    // major buttons
    JButton previousButton = new JButton(Bundle.getMessage("Previous"));
    JButton nextButton = new JButton(Bundle.getMessage("Next"));
    JButton placeButton = new JButton(Bundle.getMessage("PlaceTestIcon"));
    JButton applyButton = new JButton(Bundle.getMessage("ButtonApply"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // combo boxes
    // Spinners
    JSpinner spinTrainIconX = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    JSpinner spinTrainIconY = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));

    Route _route;
    RouteLocation _rl;
    int _routeIndex = 0;
    List<RouteLocation> _routeList;

    // test train icon
    TrainIcon _tIon;

    public SetTrainIconRouteFrame(Route route) {
        super(Bundle.getMessage("MenuSetTrainIcon"));

        if (route == null) {
            return;
        }
        _route = route;
        _route.addPropertyChangeListener(this);

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // set tool tips
        placeButton.setToolTipText(Bundle.getMessage("TipPlaceButton") + " " + Setup.getPanelName());
        applyButton.setToolTipText(Bundle.getMessage("TipApplyButton"));
        saveButton.setToolTipText(Bundle.getMessage("TipSaveButton"));

        // Set up the panels
        JPanel pRoute = new JPanel();
        pRoute.setBorder(BorderFactory
                .createTitledBorder(Bundle.getMessage("Route") + " " + _route.getName()));
        pRoute.setLayout(new GridBagLayout());
        addItem(pRoute, previousButton, 0, 0);
        addItem(pRoute, routeLocationName, 1, 0);
        addItem(pRoute, nextButton, 2, 0);

        JPanel pSpin = new JPanel();
        pSpin.setLayout(new GridBagLayout());
        pSpin.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainIcon")));
        addItem(pSpin, textX, 0, 0);
        addItem(pSpin, spinTrainIconX, 1, 0);
        addItem(pSpin, textY, 2, 0);
        addItem(pSpin, spinTrainIconY, 3, 0);

        JPanel pControl = new JPanel();
        pControl.setLayout(new GridBagLayout());
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        addItem(pControl, placeButton, 0, 0);
        addItem(pControl, applyButton, 1, 0);
        addItem(pControl, saveButton, 2, 0);

        getContentPane().add(pRoute);
        getContentPane().add(pSpin);
        getContentPane().add(pControl);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_SetTrainIconCoordinates", true); // NOI18N

        // setup buttons
        addButtonAction(previousButton);
        addButtonAction(nextButton);
        addButtonAction(placeButton);
        addButtonAction(applyButton);
        addButtonAction(saveButton);

        // start off with save button disabled
        saveButton.setEnabled(false);

        updateRoute();

        // setup spinners
        addSpinnerChangeListerner(spinTrainIconX);
        addSpinnerChangeListerner(spinTrainIconY);

        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight400));

    }

    int value = JOptionPane.NO_OPTION;

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == previousButton) {
            updateRouteLocation(BACK);
        }
        if (ae.getSource() == nextButton) {
            updateRouteLocation(FORWARD);
        }
        if (ae.getSource() == placeButton) {
            placeTestIcons();
        }
        if (ae.getSource() == applyButton) {
            if (value != JOptionPane.YES_OPTION) {
                value = JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle
                        .getMessage("UpdateTrainIconRoute"), new Object[]{_route.getName()}), Bundle
                                .getMessage("DoYouWantThisRoute"),
                        JOptionPane.YES_NO_OPTION);
            }
            if (value == JOptionPane.YES_OPTION) {
                saveButton.setEnabled(true);
            }
            updateTrainIconCoordinates();
        }
        if (ae.getSource() == saveButton) {
            InstanceManager.getDefault(RouteManagerXml.class).writeOperationsFile();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    public void spinnerChangeEvent(javax.swing.event.ChangeEvent ae) {
        if (ae.getSource() == spinTrainIconX && _tIon != null) {
            _tIon.setLocation((Integer) spinTrainIconX.getValue(), _tIon.getLocation().y);
        }
        if (ae.getSource() == spinTrainIconY && _tIon != null) {
            _tIon.setLocation(_tIon.getLocation().x, (Integer) spinTrainIconY.getValue());
        }
    }

    private void loadSpinners(RouteLocation rl) {
        log.debug("Load spinners route location {}", rl.getName());
        spinTrainIconX.setValue(rl.getTrainIconX());
        spinTrainIconY.setValue(rl.getTrainIconY());
    }

    // place test markers on panel
    private void placeTestIcons() {
        Editor editor = InstanceManager.getDefault(PanelMenu.class).getEditorByName(Setup.getPanelName());
        if (editor == null) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("LoadPanel"),
                    new Object[]{Setup.getPanelName()}), Bundle.getMessage("PanelNotFound"),
                    JOptionPane.ERROR_MESSAGE);
        } else {
            if (_tIon != null) {
                _tIon.remove();
            }
            // icon
            _tIon = editor.addTrainIcon(_rl.getName());
            _tIon.getToolTip().setText(_route.getName());
            _tIon.getToolTip().setBackgroundColor(Color.white);
            _tIon.setLocation(_rl.getTrainIconX(), _rl.getTrainIconY());
            setTrainIconNameAndColor();
            addIconListener(_tIon);
        }
    }

    private void setTrainIconNameAndColor() {
        if (_tIon == null) {
            return;
        }
        _tIon.setText(_rl.getName());
        // set color based on train direction at current location
        if (_rl.getTrainDirection() == RouteLocation.NORTH) {
            _tIon.setLocoColor(Setup.getTrainIconColorNorth());
        }
        if (_rl.getTrainDirection() == RouteLocation.SOUTH) {
            _tIon.setLocoColor(Setup.getTrainIconColorSouth());
        }
        if (_rl.getTrainDirection() == RouteLocation.EAST) {
            _tIon.setLocoColor(Setup.getTrainIconColorEast());
        }
        if (_rl.getTrainDirection() == RouteLocation.WEST) {
            _tIon.setLocoColor(Setup.getTrainIconColorWest());
        }
    }

    private void updateRoute() {
        log.debug("Updating route");
        _routeList = _route.getLocationsBySequenceList();
        updateRouteLocation(NONE);
    }

    private final int FORWARD = 1;
    private final int BACK = -1;
    private final int NONE = 0;

    private void updateRouteLocation(int direction) {
        if (direction == FORWARD) {
            _routeIndex++;
        }
        if (direction == BACK) {
            _routeIndex--;
        }
        // Confirm that index is in range
        if (_routeIndex > _routeList.size() - 1) {
            _routeIndex = _routeList.size() - 1;
        }
        if (_routeIndex < 0) {
            _routeIndex = 0;
        }

        if (_rl != null) {
            _rl.removePropertyChangeListener(this);
        }
        if (_routeList.size() > 0) {
            _rl = _routeList.get(_routeIndex);
        }
        if (_rl != null) {
            _rl.addPropertyChangeListener(this);
            loadSpinners(_rl);
            routeLocationName.setText(_rl.getName());
        }
        // disable or enable previous and next buttons
        previousButton.setEnabled(_routeIndex != 0);
        nextButton.setEnabled(_routeIndex != _routeList.size() - 1);
        
        setTrainIconNameAndColor();
    }

    private void updateTrainIconCoordinates() {
        if (_rl != null) {
            _rl.removePropertyChangeListener(this);
            _rl.setTrainIconX((Integer) spinTrainIconX.getValue());
            _rl.setTrainIconY((Integer) spinTrainIconY.getValue());
            _rl.addPropertyChangeListener(this);
        }
    }

    private void addIconListener(TrainIcon tI) {
        tI.addComponentListener(new ComponentListener() {
            @Override
            public void componentHidden(java.awt.event.ComponentEvent e) {
            }

            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
            }

            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                trainIconMoved(e);
            }

            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
            }
        });
    }

    protected void trainIconMoved(java.awt.event.ComponentEvent ae) {
        if (ae.getSource() == _tIon) {
            log.debug("train icon X: {} Y: {}", _tIon.getLocation().x, _tIon.getLocation().y);
            spinTrainIconX.setValue(_tIon.getLocation().x);
            spinTrainIconY.setValue(_tIon.getLocation().y);
        }
    }

    private void removeIcons() {
        if (_tIon != null) {
            _tIon.remove();
        }
    }

    @Override
    public void dispose() {
        removeIcons();
        if (_route != null) {
            _route.removePropertyChangeListener(this);
        }
        if (_rl != null) {
            _rl.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        log.debug("Property change ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                e.getNewValue());
        if (e.getSource().equals(_route)) {
            updateRoute();
        }
        if (e.getSource().equals(_rl)) {
            updateRouteLocation(NONE);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SetTrainIconRouteFrame.class);
}
