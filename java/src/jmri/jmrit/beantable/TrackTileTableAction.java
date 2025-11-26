package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;

import javax.swing.JTable;

import jmri.InstanceManager;
import jmri.tracktiles.TrackTileManager;


/**
 * Swing action to create and register a TrackTile table GUI.
 * 
 * @author Ralf Lang Copyright (C) 2025
 */
public class TrackTileTableAction extends javax.swing.AbstractAction {

    private TrackTileTableFrame frame = null;

    /**
     * Create an action with a specific title.
     * 
     * @param actionName title of the action
     */
    public TrackTileTableAction(String actionName) {
        super(actionName);
    }

    /**
     * Default constructor
     */
    public TrackTileTableAction() {
        this(Bundle.getMessage("TitleTrackTilesTable"));
    }

    /**
     * Create the JTable DataModel and the frame to display it.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Get the manager from InstanceManager
        TrackTileManager manager = InstanceManager.getDefault(TrackTileManager.class);
        
        // Create the table model
        TrackTileTableDataModel model = new TrackTileTableDataModel(manager);
        
        // Create the table
        JTable table = new JTable(model);
        model.configureTable(table);
        
        // Create and show the frame
        if (frame == null) {
            frame = new TrackTileTableFrame(model, table, getClass().getName());
        }
        frame.setVisible(true);
    }

    /**
     * Get the frame, if one has been created.
     * 
     * @return the frame or null
     */
    public TrackTileTableFrame getFrame() {
        return frame;
    }
}
