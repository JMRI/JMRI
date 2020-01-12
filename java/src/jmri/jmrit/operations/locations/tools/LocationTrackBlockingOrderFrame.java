package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of location
 *
 * @author Dan Boudreau Copyright (C) 2015
 * 
 */
public class LocationTrackBlockingOrderFrame extends OperationsFrame {

    LocationTrackBlockingOrderTableModel trackModel = new LocationTrackBlockingOrderTableModel();
    JTable trackTable = new JTable(trackModel);
    JScrollPane trackPane = new JScrollPane(trackTable);

    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    Location _location = null;
    
    JLabel locationName = new JLabel();

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton resetButton = new JButton(Bundle.getMessage("Reset"));
    JButton reorderButton = new JButton(Bundle.getMessage("Reorder"));

    public LocationTrackBlockingOrderFrame() {
        super(Bundle.getMessage("TitleTrackBlockingOrder"));
    }

    public void initComponents(Location location) {
        _location = location;
        
        trackPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        trackPane.setBorder(BorderFactory.createTitledBorder(""));

        if (_location != null) {
            trackModel.initTable(trackTable, location);
            locationName.setText(_location.getName());
            enableButtons(true);
        } else {
            enableButtons(false);
        }

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows       
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));

        addItem(pName, locationName, 0, 0);
        
        // row buttons
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        addItem(pB, resetButton, 0, 0);
        addItem(pB, reorderButton, 1, 0);
        addItem(pB, saveButton, 2, 0);
        
        // Notes
        JLabel note1 = new JLabel(Bundle.getMessage("ServiceOrderMessage"));
        JLabel note2 = new JLabel(Bundle.getMessage("ServiceOrderEastSouth"));

        getContentPane().add(pName);
        getContentPane().add(note1);
        getContentPane().add(note2);
        getContentPane().add(trackPane);
        getContentPane().add(pB);

        // setup buttons
        addButtonAction(resetButton);
        addButtonAction(reorderButton);
        addButtonAction(saveButton);

        // add tool tips
        resetButton.setToolTipText(Bundle.getMessage("TipResetButton"));
        reorderButton.setToolTipText(Bundle.getMessage("TipReorderButton"));

        // build menu
//        JMenuBar menuBar = new JMenuBar();
//        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
//        menuBar.add(toolMenu);
//        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_TrackBlockingOrder", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight500));

    }

    // Reset and Save
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == resetButton && _location != null) {
            _location.resetTracksByBlockingOrder();
        }
        if (ae.getSource() == reorderButton && _location != null) {
            _location.resequnceTracksByBlockingOrder();
        }
        if (ae.getSource() == saveButton) {
            if (trackTable.isEditing()) {
                log.debug("track table edit true");
                trackTable.getCellEditor().stopCellEditing();
            }
            // recreate all train manifests
            InstanceManager.getDefault(TrainManager.class).setTrainsModified();
            // save location file
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    private void enableButtons(boolean enabled) {
        resetButton.setEnabled(enabled);
        reorderButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
    }

    @Override
    public void dispose() {
        trackModel.dispose();
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(LocationTrackBlockingOrderFrame.class);
}
