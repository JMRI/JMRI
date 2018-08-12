package jmri.jmrit.operations.routes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ComponentListener;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for setting train icon coordinates for a location.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class SetTrainIconPositionFrame extends OperationsFrame {

    RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);

    // labels
    JLabel textEastX = new JLabel("   X  ");
    JLabel textEastY = new JLabel("   Y  ");
    JLabel textWestX = new JLabel("   X  ");
    JLabel textWestY = new JLabel("   Y  ");
    JLabel textNorthX = new JLabel("   X  ");
    JLabel textNorthY = new JLabel("   Y  ");
    JLabel textSouthX = new JLabel("   X  ");
    JLabel textSouthY = new JLabel("   Y  ");
    
    JLabel textRangeX = new JLabel("   X +/-");
    JLabel textRangeY = new JLabel("   Y +/-");

    // major buttons
    JButton placeButton = new JButton(Bundle.getMessage("PlaceTestIcon"));
    JButton applyButton = new JButton(Bundle.getMessage("UpdateRoutes"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // combo boxes
    JComboBox<Location> locationBox = InstanceManager.getDefault(LocationManager.class).getComboBox();

    //Spinners  
    JSpinner spinTrainIconEastX = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    JSpinner spinTrainIconEastY = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    JSpinner spinTrainIconWestX = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    JSpinner spinTrainIconWestY = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    JSpinner spinTrainIconNorthX = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    JSpinner spinTrainIconNorthY = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    JSpinner spinTrainIconSouthX = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    JSpinner spinTrainIconSouthY = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    
    // detection range
    JSpinner spinTrainIconRangeX = new JSpinner(new SpinnerNumberModel(Location.RANGE_DEFAULT, 0, 1000, 1));
    JSpinner spinTrainIconRangeY = new JSpinner(new SpinnerNumberModel(Location.RANGE_DEFAULT, 0, 1000, 1));

    // Four test train icons
    TrainIcon _tIonEast;
    TrainIcon _tIonWest;
    TrainIcon _tIonNorth;
    TrainIcon _tIonSouth;

    public SetTrainIconPositionFrame() {
        super(Bundle.getMessage("MenuSetTrainIcon"));

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // set tool tips
        placeButton.setToolTipText(Bundle.getMessage("TipPlaceButton") + " \"" + Setup.getPanelName()  + "\"");  // NOI18N
        applyButton.setToolTipText(Bundle.getMessage("TipApplyAllButton"));
        saveButton.setToolTipText(Bundle.getMessage("TipSaveButton"));

        //      Set up the panels
        JPanel pLocation = new JPanel();
        pLocation.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        pLocation.add(locationBox);

        JPanel pEast = new JPanel();
        pEast.setLayout(new GridBagLayout());
        pEast.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("EastTrainIcon")));
        addItem(pEast, textEastX, 0, 0);
        addItem(pEast, spinTrainIconEastX, 1, 0);
        addItem(pEast, textEastY, 2, 0);
        addItem(pEast, spinTrainIconEastY, 3, 0);

        JPanel pWest = new JPanel();
        pWest.setLayout(new GridBagLayout());
        pWest.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("WestTrainIcon")));
        addItem(pWest, textWestX, 0, 0);
        addItem(pWest, spinTrainIconWestX, 1, 0);
        addItem(pWest, textWestY, 2, 0);
        addItem(pWest, spinTrainIconWestY, 3, 0);

        JPanel pNorth = new JPanel();
        pNorth.setLayout(new GridBagLayout());
        pNorth.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("NorthTrainIcon")));
        addItem(pNorth, textNorthX, 0, 0);
        addItem(pNorth, spinTrainIconNorthX, 1, 0);
        addItem(pNorth, textNorthY, 2, 0);
        addItem(pNorth, spinTrainIconNorthY, 3, 0);

        JPanel pSouth = new JPanel();
        pSouth.setLayout(new GridBagLayout());
        pSouth.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SouthTrainIcon")));
        addItem(pSouth, textSouthX, 0, 0);
        addItem(pSouth, spinTrainIconSouthX, 1, 0);
        addItem(pSouth, textSouthY, 2, 0);
        addItem(pSouth, spinTrainIconSouthY, 3, 0);
        
        JPanel pRange = new JPanel();
        pRange.setLayout(new GridBagLayout());
        pRange.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RangeTrainIcon")));
        addItem(pRange, textRangeX, 0, 0);
        addItem(pRange, spinTrainIconRangeX, 1, 0);
        addItem(pRange, textRangeY, 2, 0);
        addItem(pRange, spinTrainIconRangeY, 3, 0);

        JPanel pControl = new JPanel();
        pControl.setLayout(new GridBagLayout());
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        addItem(pControl, placeButton, 0, 0);
        addItem(pControl, applyButton, 1, 0);
        addItem(pControl, saveButton, 2, 0);

        // only show valid directions
        pEast.setVisible((Setup.getTrainDirection() & Setup.EAST) == Setup.EAST);
        pWest.setVisible((Setup.getTrainDirection() & Setup.WEST) == Setup.WEST);
        pNorth.setVisible((Setup.getTrainDirection() & Setup.NORTH) == Setup.NORTH);
        pSouth.setVisible((Setup.getTrainDirection() & Setup.SOUTH) == Setup.SOUTH);

        getContentPane().add(pLocation);
        getContentPane().add(pNorth);
        getContentPane().add(pSouth);
        getContentPane().add(pEast);
        getContentPane().add(pWest);
        getContentPane().add(pRange);
        getContentPane().add(pControl);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_SetTrainIconCoordinates", true); // NOI18N

        // setup buttons
        addButtonAction(placeButton);
        addButtonAction(applyButton);
        addButtonAction(saveButton);

        // setup combo box
        addComboBoxAction(locationBox);

        // setup spinners
        spinnersEnable(false);
        addSpinnerChangeListerner(spinTrainIconEastX);
        addSpinnerChangeListerner(spinTrainIconEastY);
        addSpinnerChangeListerner(spinTrainIconWestX);
        addSpinnerChangeListerner(spinTrainIconWestY);
        addSpinnerChangeListerner(spinTrainIconNorthX);
        addSpinnerChangeListerner(spinTrainIconNorthY);
        addSpinnerChangeListerner(spinTrainIconSouthX);
        addSpinnerChangeListerner(spinTrainIconSouthY);
        
        addSpinnerChangeListerner(spinTrainIconRangeX);
        addSpinnerChangeListerner(spinTrainIconRangeY);

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight400));

    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        // check to see if a location has been selected 
        if (locationBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("SelectLocationToEdit"), Bundle.getMessage("NoLocationSelected"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        Location l = (Location) locationBox.getSelectedItem();
        if (l == null) {
            return;
        }
        if (ae.getSource() == placeButton) {
            placeTestIcons();
        }
        if (ae.getSource() == applyButton) {
            // update all routes?
            int value = JOptionPane.showConfirmDialog(null,
                    MessageFormat.format(Bundle.getMessage("UpdateTrainIcon"), new Object[]{l.getName()}),
                    Bundle.getMessage("DoYouWantAllRoutes"),
                    JOptionPane.YES_NO_OPTION);
            if (value == JOptionPane.YES_OPTION) {
                saveSpinnerValues(l);
                updateTrainIconCoordinates(l);
            }
        }
        if (ae.getSource() == saveButton) {
            int value = JOptionPane.showConfirmDialog(null,
                    MessageFormat.format(Bundle.getMessage("UpdateTrainIcon"), new Object[]{l.getName()}),
                    Bundle.getMessage("UpdateDefaults"),
                    JOptionPane.YES_NO_OPTION);
            if (value == JOptionPane.YES_OPTION) {
                saveSpinnerValues(l);
            }
            OperationsXml.save(); // save location and route files
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (locationBox.getSelectedItem() == null) {
            resetSpinners();
            removeIcons();
        } else {
            Location l = (Location) locationBox.getSelectedItem();
            loadSpinners(l);
        }
    }

    @Override
    public void spinnerChangeEvent(javax.swing.event.ChangeEvent ae) {
        if (ae.getSource() == spinTrainIconEastX && _tIonEast != null) {
            _tIonEast.setLocation((Integer) spinTrainIconEastX.getValue(), _tIonEast.getLocation().y);
        }
        if (ae.getSource() == spinTrainIconEastY && _tIonEast != null) {
            _tIonEast.setLocation(_tIonEast.getLocation().x, (Integer) spinTrainIconEastY.getValue());
        }
        if (ae.getSource() == spinTrainIconWestX && _tIonWest != null) {
            _tIonWest.setLocation((Integer) spinTrainIconWestX.getValue(), _tIonWest.getLocation().y);
        }
        if (ae.getSource() == spinTrainIconWestY && _tIonWest != null) {
            _tIonWest.setLocation(_tIonWest.getLocation().x, (Integer) spinTrainIconWestY.getValue());
        }
        if (ae.getSource() == spinTrainIconNorthX && _tIonNorth != null) {
            _tIonNorth.setLocation((Integer) spinTrainIconNorthX.getValue(), _tIonNorth.getLocation().y);
        }
        if (ae.getSource() == spinTrainIconNorthY && _tIonNorth != null) {
            _tIonNorth.setLocation(_tIonNorth.getLocation().x, (Integer) spinTrainIconNorthY.getValue());
        }
        if (ae.getSource() == spinTrainIconSouthX && _tIonSouth != null) {
            _tIonSouth.setLocation((Integer) spinTrainIconSouthX.getValue(), _tIonSouth.getLocation().y);
        }
        if (ae.getSource() == spinTrainIconSouthY && _tIonSouth != null) {
            _tIonSouth.setLocation(_tIonSouth.getLocation().x, (Integer) spinTrainIconSouthY.getValue());
        }
    }

    private void resetSpinners() {
        spinnersEnable(false);
        spinTrainIconEastX.setValue(0);
        spinTrainIconEastY.setValue(0);
        spinTrainIconWestX.setValue(0);
        spinTrainIconWestY.setValue(0);
        spinTrainIconNorthX.setValue(0);
        spinTrainIconNorthY.setValue(0);
        spinTrainIconSouthX.setValue(0);
        spinTrainIconSouthY.setValue(0);
    }

    private void loadSpinners(Location l) {
        log.debug("Load spinners location {}", l.getName());
        spinnersEnable(true);
        spinTrainIconEastX.setValue(l.getTrainIconEast().x);
        spinTrainIconEastY.setValue(l.getTrainIconEast().y);
        spinTrainIconWestX.setValue(l.getTrainIconWest().x);
        spinTrainIconWestY.setValue(l.getTrainIconWest().y);
        spinTrainIconNorthX.setValue(l.getTrainIconNorth().x);
        spinTrainIconNorthY.setValue(l.getTrainIconNorth().y);
        spinTrainIconSouthX.setValue(l.getTrainIconSouth().x);
        spinTrainIconSouthY.setValue(l.getTrainIconSouth().y);
        
        spinTrainIconRangeX.setValue(l.getTrainIconRangeX());
        spinTrainIconRangeY.setValue(l.getTrainIconRangeY());
    }

    private void spinnersEnable(boolean enable) {
        spinTrainIconEastX.setEnabled(enable);
        spinTrainIconEastY.setEnabled(enable);
        spinTrainIconWestX.setEnabled(enable);
        spinTrainIconWestY.setEnabled(enable);
        spinTrainIconNorthX.setEnabled(enable);
        spinTrainIconNorthY.setEnabled(enable);
        spinTrainIconSouthX.setEnabled(enable);
        spinTrainIconSouthY.setEnabled(enable);
        
        spinTrainIconRangeX.setEnabled(enable);
        spinTrainIconRangeY.setEnabled(enable);
    }

    private void saveSpinnerValues(Location l) {
        log.debug("Save train icons coordinates for location {}", l.getName());
        l.setTrainIconEast(new Point((Integer) spinTrainIconEastX.getValue(), (Integer) spinTrainIconEastY.getValue()));
        l.setTrainIconWest(new Point((Integer) spinTrainIconWestX.getValue(), (Integer) spinTrainIconWestY.getValue()));
        l.setTrainIconNorth(new Point((Integer) spinTrainIconNorthX.getValue(), (Integer) spinTrainIconNorthY.getValue()));
        l.setTrainIconSouth(new Point((Integer) spinTrainIconSouthX.getValue(), (Integer) spinTrainIconSouthY.getValue()));
        
        l.setTrainIconRangeX((Integer)spinTrainIconRangeX.getValue());
        l.setTrainIconRangeY((Integer)spinTrainIconRangeY.getValue());
    }

    // place test markers on panel
    private void placeTestIcons() {
        removeIcons();
        if (locationBox.getSelectedItem() == null) {
            return;
        }
        Editor editor = InstanceManager.getDefault(PanelMenu.class).getEditorByName(Setup.getPanelName());
        if (editor == null) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("LoadPanel"), new Object[]{Setup.getPanelName()}),
                    Bundle.getMessage("PanelNotFound"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        Location l = (Location) locationBox.getSelectedItem();
        if (l != null) {
            // East icon
            if ((Setup.getTrainDirection() & Setup.EAST) == Setup.EAST) {
                _tIonEast = editor.addTrainIcon(Bundle.getMessage("East"));
                _tIonEast.getToolTip().setText(l.getName());
                _tIonEast.getToolTip().setBackgroundColor(Color.white);
                _tIonEast.setLocoColor(Setup.getTrainIconColorEast());
                _tIonEast.setLocation((Integer) spinTrainIconEastX.getValue(), (Integer) spinTrainIconEastY.getValue());
                addIconListener(_tIonEast);
            }
            // West icon
            if ((Setup.getTrainDirection() & Setup.WEST) == Setup.WEST) {
                _tIonWest = editor.addTrainIcon(Bundle.getMessage("West"));
                _tIonWest.getToolTip().setText(l.getName());
                _tIonWest.getToolTip().setBackgroundColor(Color.white);
                _tIonWest.setLocoColor(Setup.getTrainIconColorWest());
                _tIonWest.setLocation((Integer) spinTrainIconWestX.getValue(), (Integer) spinTrainIconWestY.getValue());
                addIconListener(_tIonWest);
            }
            // North icon
            if ((Setup.getTrainDirection() & Setup.NORTH) == Setup.NORTH) {
                _tIonNorth = editor.addTrainIcon(Bundle.getMessage("North"));
                _tIonNorth.getToolTip().setText(l.getName());
                _tIonNorth.getToolTip().setBackgroundColor(Color.white);
                _tIonNorth.setLocoColor(Setup.getTrainIconColorNorth());
                _tIonNorth.setLocation((Integer) spinTrainIconNorthX.getValue(), (Integer) spinTrainIconNorthY.getValue());
                addIconListener(_tIonNorth);
            }
            // South icon
            if ((Setup.getTrainDirection() & Setup.SOUTH) == Setup.SOUTH) {
                _tIonSouth = editor.addTrainIcon(Bundle.getMessage("South"));
                _tIonSouth.getToolTip().setText(l.getName());
                _tIonSouth.getToolTip().setBackgroundColor(Color.white);
                _tIonSouth.setLocoColor(Setup.getTrainIconColorSouth());
                _tIonSouth.setLocation((Integer) spinTrainIconSouthX.getValue(), (Integer) spinTrainIconSouthY.getValue());
                addIconListener(_tIonSouth);
            }
        }
    }

    public void updateTrainIconCoordinates(Location l) {
        for (Route route : InstanceManager.getDefault(RouteManager.class).getRoutesByIdList()) {
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                if (rl.getName().equals(l.getName())) {
                    log.debug("Updating train icon for route location {} in route {}", rl.getName(), route.getName());
                    rl.setTrainIconCoordinates();
                }
            }
        }
    }

    private void removeIcons() {
        if (_tIonEast != null) {
            _tIonEast.remove();
        }
        if (_tIonWest != null) {
            _tIonWest.remove();
        }
        if (_tIonNorth != null) {
            _tIonNorth.remove();
        }
        if (_tIonSouth != null) {
            _tIonSouth.remove();
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
        if (ae.getSource() == _tIonEast) {
            log.debug("East train icon X: {} Y: {}", _tIonEast.getLocation().x, _tIonEast.getLocation().y);
            spinTrainIconEastX.setValue(_tIonEast.getLocation().x);
            spinTrainIconEastY.setValue(_tIonEast.getLocation().y);
        }
        if (ae.getSource() == _tIonWest) {
            log.debug("West train icon X: {} Y: {}", _tIonWest.getLocation().x, _tIonWest.getLocation().y);
            spinTrainIconWestX.setValue(_tIonWest.getLocation().x);
            spinTrainIconWestY.setValue(_tIonWest.getLocation().y);
        }
        if (ae.getSource() == _tIonNorth) {
            log.debug("North train icon X: {} Y: {}", _tIonNorth.getLocation().x, _tIonNorth.getLocation().y);
            spinTrainIconNorthX.setValue(_tIonNorth.getLocation().x);
            spinTrainIconNorthY.setValue(_tIonNorth.getLocation().y);
        }
        if (ae.getSource() == _tIonSouth) {
            log.debug("South train icon X: {} Y: {}", _tIonSouth.getLocation().x, _tIonSouth.getLocation().y);
            spinTrainIconSouthX.setValue(_tIonSouth.getLocation().x);
            spinTrainIconSouthY.setValue(_tIonSouth.getLocation().y);
        }
    }

    @Override
    public void dispose() {
        removeIcons();
        super.dispose();
    }

    private final static Logger log = LoggerFactory
            .getLogger(SetTrainIconPositionFrame.class);
}
