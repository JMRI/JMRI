package jmri.jmrit.beantable.light;

import javax.swing.*;

import jmri.Light;
import jmri.VariableLight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel to display Light Intensity options.
 * 
 * Code originally within LightTableAction.
 * 
 * @author Dave Duchamp Copyright (C) 2004
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class LightIntensityPane extends JPanel {
    
    private JPanel minPan;
    private JPanel maxPan;
    private JPanel transitionPan;
    
    private JSpinner minIntensity;
    private JSpinner maxIntensity;
    private JSpinner transitionTime;
    
    private final JLabel status1 = new JLabel();
    
    /**
     * Create a new Light Intensity Panel.
     * 
     * @param vertical true for vertical, false for horizontal display.
     */
    public LightIntensityPane( boolean vertical){
        super();
        init(vertical);
    }
    
    
    private void init(boolean vertical){
    
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS ));
        minIntensity = new JSpinner();
        maxIntensity = new JSpinner();
        transitionTime = new JSpinner();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, ( vertical ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS)));
        
        minPan = new JPanel();
        minPan.add(new JLabel(" "));
        minPan.add(new JLabel(Bundle.getMessage("LightMinIntensity")));
        minIntensity.setModel(
                new SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), Double.valueOf(0.99d), Double.valueOf(0.01d))); // 0 - 99%
        minIntensity.setEditor(new JSpinner.NumberEditor(minIntensity, "##0 %"));
        minIntensity.setToolTipText(Bundle.getMessage("LightMinIntensityHint"));
        minIntensity.setValue(0.0d); // reset JSpinner1
        minPan.add(minIntensity);
        minPan.add(new JLabel("   "));
        mainPanel.add(minPan);
        
        maxPan = new JPanel();
        maxPan.add(new JLabel(Bundle.getMessage("LightMaxIntensity")));
        maxIntensity.setModel(
                new SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.01d), Double.valueOf(1.0d), Double.valueOf(0.01d))); // 100 - 1%
        maxIntensity.setEditor(new JSpinner.NumberEditor(maxIntensity, "##0 %"));
        maxIntensity.setToolTipText(Bundle.getMessage("LightMaxIntensityHint"));
        maxIntensity.setValue(1.0d); // reset JSpinner2
        maxPan.add(maxIntensity);
        maxPan.add(new JLabel("   "));
        mainPanel.add(maxPan);
        
        transitionPan = new JPanel();
        transitionPan.add(new JLabel(Bundle.getMessage("LightTransitionTime")));
        transitionTime.setModel(
                new SpinnerNumberModel(Double.valueOf(0d), Double.valueOf(0d), Double.valueOf(1000000d), Double.valueOf(0.01d)));
        transitionTime.setEditor(new JSpinner.NumberEditor(transitionTime, "###0.00"));
        transitionTime.setPreferredSize(new JTextField(8).getPreferredSize());
        transitionTime.setToolTipText(Bundle.getMessage("LightTransitionTimeHint"));
        transitionTime.setValue(0.0); // reset from possible previous use
        transitionPan.add(transitionTime);
        transitionPan.add(new JLabel(" "));
        mainPanel.add(transitionPan);
        
        add(mainPanel);
        
        JPanel statusPanel = new JPanel();
        statusPanel.add(status1);
        add(statusPanel);
    
    }
    
    /**
     * Set the panel to match a Light.
     * @param light the Light to set Panel for.
     */
    public void setToLight(Light light){
        if (light instanceof VariableLight) {
            minIntensity.setValue(((VariableLight)light).getMinIntensity()); // displayed as percentage
            maxIntensity.setValue(((VariableLight)light).getMaxIntensity());
            if (((VariableLight)light).isTransitionAvailable()) {
                transitionTime.setValue(((VariableLight)light).getTransitionTime()); // displays i18n decimal separator eg. 0,00 in _nl
            }
            setupVariableDisplay(true, ((VariableLight)light).isTransitionAvailable());
        } else {
            setupVariableDisplay(false, false);
        }
    }
    
    /**
     * Set a Light to match the Panel.
     * @param light The Light to edit details for.
     */
    public void setLightFromPane(VariableLight light){
    
        if ((Double) minIntensity.getValue() >= (Double) maxIntensity.getValue()) {
            log.error("minInt value entered: {}", minIntensity.getValue());
            // do not set intensity
            status1.setText(Bundle.getMessage("LightWarn9"));
            status1.setVisible(true);
        } else {
            light.setMinIntensity((Double) minIntensity.getValue());
            light.setMaxIntensity((Double) maxIntensity.getValue());
        }
        if (light.isTransitionAvailable()) {
            light.setTransitionTime((Double) transitionTime.getValue());
        }
    
    }
    
    /**
     * Set up panel for Variable Options.
     *
     * @param showIntensity  true to show light intensity; false otherwise
     * @param showTransition true to show time light takes to transition between
     *                       states; false otherwise
     */
    private void setupVariableDisplay(boolean showIntensity, boolean showTransition) {
        minPan.setVisible(showIntensity);
        maxPan.setVisible(showIntensity);
        transitionPan.setVisible(showTransition);
        setVisible(showIntensity || showTransition);
    }
    
    private final static Logger log = LoggerFactory.getLogger(LightIntensityPane.class);
    
}
