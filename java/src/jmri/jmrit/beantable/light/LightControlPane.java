package jmri.jmrit.beantable.light;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.Light;
import jmri.LightControl;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Pane to add / edit Light Controls for a new or given Light.
 * <p>
 * Light Control Table with new control / edit individual Control buttons.
 * Uses separate JFrame to Edit a Single Control.
 * <p>
 * Defaults to No Light Controls for a New Light.
 * <p>
 * Code originally within LightTableAction.
 * 
 * @author Dave Duchamp Copyright (C) 2004
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class LightControlPane extends JPanel {

    private LightControlTableModel lightControlTableModel;
    private JButton addControl;
    
    /**
     * Create a Panel for Light Controls.
     * No Controls as default.
     */
    public LightControlPane(){
        super();
        init();
    }
    
    /**
     * Create a Panel for Light Controls.
     * @param l Light to display Light Controls for.
     */
    public LightControlPane(Light l){
        super();
        init();
        setToLight(l);
    }
    
    private void init(){
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        lightControlTableModel = new LightControlTableModel(this);
        JTable lightControlTable = new JTable(lightControlTableModel);
        
        lightControlTableModel.configureJTable(lightControlTable);
        lightControlTable.setPreferredScrollableViewportSize(new java.awt.Dimension(600, 100));
        JScrollPane lightControlTableScrollPane = new JScrollPane(lightControlTable);
        add(lightControlTableScrollPane);

        addControl = new JButton(Bundle.getMessage("LightAddControlButton"));
        addControl.addActionListener(this::addControlPressed);
        addControl.setToolTipText(Bundle.getMessage("LightAddControlButtonHint"));
        
        JPanel panel35 = new JPanel();
        panel35.setLayout(new FlowLayout());
        panel35.add(addControl);
        add(panel35);
        
    }
    
    /**
     * Respond to pressing the Add Control button.
     *
     * @param e the event containing the press action
     */
    protected void addControlPressed(ActionEvent e) {
        // Use separate Runnable so window is created on top
        javax.swing.SwingUtilities.invokeLater(() -> {
                addEditControlWindow(null);
        });
    }
    
    /**
     * Respond to pressing the Update Control button in the New / Edit Control Frame.
     *
     * @param oldControl the LightControl to remove
     * @param newControl the LightControl to add
     */
    protected void updateControlPressed(LightControl oldControl, LightControl newControl) {
        lightControlTableModel.removeControl(oldControl);
        lightControlTableModel.addControl(newControl);
    }
    
    /**
     * Set Controls from the Control Table to the Light.
     * Removes any existing Light Controls on the Light.
     * @param g Light to set Controls to.
     */
    public void setLightFromControlTable(Light g) {
        g.deactivateLight();
        g.clearLightControls(); // clear list on Light
        getControlList().stream().map(control -> {
            control.setParentLight(g);
            return control;
        }).forEachOrdered(control -> {
            g.addLightControl(control);
        });
        g.activateLight();
    }
    
    /**
     * Respond to Edit button on row in the Light Control Table.
     *
     * @param lc the Light Control to edit.
     */
    protected void editControlAction(@Nonnull LightControl lc) {
        addEditControlWindow(lc);
    }
    
    /**
     * Add a Single Light Control to the Table.
     * @param lc the Light Control to add.
     */
    protected void addControlToTable(LightControl lc) {
        lightControlTableModel.addControl(lc);
    }

    /**
     * Get Light Control List currently displayed in the Table.
     * Returned by the TableModel as unmodifiable.
     * @return List of Light Controls.
     */
    public List<LightControl> getControlList(){
        return lightControlTableModel.getControlList();
    }
    
    /**
     * Set the Table to the Light Controls of a single Light.
     * @param l the Light to set display for.
     */
    public final void setToLight(Light l){
        lightControlTableModel.setTableToLight(l);
    }
    
    private AddEditSingleLightControlFrame addEditCtrlFrame;
    
    /**
     * UI Function to get Last Selected Light Control Index within
     * AddEditSingleLightControl.java
     * @return Light Control Index.
     */
    protected int getLastSelectedControlIndex(){
        return defaultControlIndex;
    }
    
    protected void setLastSelectedControlIndex(int newIndex){
        defaultControlIndex = newIndex;
    }
    
    private int defaultControlIndex = Light.NO_CONTROL;
    
    /**
     * Create the Add/Edit Light Control pane.
     */
    private void addEditControlWindow(LightControl lc) {
        closeEditControlWindow();
        addEditCtrlFrame = new AddEditSingleLightControlFrame(this, lc);
    }
    
    protected void closeEditControlWindow(){
        if (!(addEditCtrlFrame == null)) {
            addEditCtrlFrame.dispose();
            addEditCtrlFrame = null;
        }
    }
    
    public void dispose(){
        closeEditControlWindow();
    }
    
    // private final static Logger log = LoggerFactory.getLogger(LightControlPane.class);
    
}
