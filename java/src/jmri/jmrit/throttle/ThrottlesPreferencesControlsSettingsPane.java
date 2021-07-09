package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import jmri.swing.JTitledSeparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A preferences panel to display and edit JMRI throttle keyboard shortcuts
 * 
 * @author Lionel Jeanson - 2021
 * 
 */
public class ThrottlesPreferencesControlsSettingsPane extends JPanel {
    
    private ShortCutsField tfNextThrottleWindow;
    private ShortCutsField tfPrevThrottleWindow;
    private ShortCutsField tfNextThrottleFrame;
    private ShortCutsField tfPrevThrottleFrame;
    private ShortCutsField tfNextRunningThrottleFrame;
    private ShortCutsField tfPrevRunningThrottleFrame;
    private ShortCutsField tfNextThrottleInternalWindow;
    private ShortCutsField tfPrevThrottleInternalWindow;
    private ShortCutsField tfGotoControl;
    private ShortCutsField tfGotoFunctions;
    private ShortCutsField tfGotoAddress;    
    private ShortCutsField tfForward;
    private ShortCutsField tfReverse;
    private ShortCutsField tfSwitchDir;
    private ShortCutsField tfSpeedIdle;
    private ShortCutsField tfSpeedStop;
    private ShortCutsField tfSpeedUp;
    private ShortCutsField tfSpeedDown;        
    private ShortCutsField tfSpeedUpMore;
    private ShortCutsField tfSpeedDownMore;
    
    private JTextField tfSpeedMultiplier;
    private float origSpeedMultiplier;
    
    private ShortCutsField[] tfFunctionKeys;
    private ThrottlesPreferencesWindowKeyboardControls _tpwkc;
        
    public ThrottlesPreferencesControlsSettingsPane(ThrottlesPreferences tp) {
        try {
            _tpwkc = tp.getThrottlesKeyboardControls().clone();
        } catch (CloneNotSupportedException ex) {
            log.debug("Couldn't clone ThrottlesPreferencesWindowKeyboardControls");
        }
        initComponents();
    }

    private void initComponents() {
        
        JPanel propertyPanel = new JPanel();
        propertyPanel.setLayout(new GridBagLayout());
        this.add(propertyPanel);
        
        GridBagConstraints constraintsL = new GridBagConstraints();        
        constraintsL.fill = GridBagConstraints.HORIZONTAL;
        constraintsL.gridheight = 1;
        constraintsL.gridwidth = 1;
        constraintsL.ipadx = 0;
        constraintsL.ipady = 0;
        constraintsL.insets = new Insets(2, 18, 2, 2);
        constraintsL.weightx = 1;
        constraintsL.weighty = 1;
        constraintsL.anchor = GridBagConstraints.WEST;
        constraintsL.gridx = 0;
        constraintsL.gridy = 0;
        
        GridBagConstraints constraintsR = (GridBagConstraints) constraintsL.clone();  
        constraintsR.anchor = GridBagConstraints.CENTER;
        constraintsR.gridx = 1;
        
        GridBagConstraints constraintsS = (GridBagConstraints) constraintsL.clone();  
        constraintsS.gridwidth = 2;
        constraintsS.insets = new Insets(18, 2, 2, 2);
        
        
        propertyPanel.add(new JTitledSeparator(Bundle.getMessage("ThrottleWindowControls")),constraintsS);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
        
        propertyPanel.add(new JLabel(Bundle.getMessage("NextThrottleWindow")), constraintsL);
        tfNextThrottleWindow = new ShortCutsField( _tpwkc.getNextThrottleWindowKeys());
        propertyPanel.add(tfNextThrottleWindow, constraintsR);        
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
        
        propertyPanel.add(new JLabel(Bundle.getMessage("PrevThrottleWindow")), constraintsL);
        tfPrevThrottleWindow = new ShortCutsField( _tpwkc.getPrevThrottleWindowKeys());
        propertyPanel.add(tfPrevThrottleWindow, constraintsR);        
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
              
        propertyPanel.add(new JLabel(Bundle.getMessage("NextThrottleFrame")), constraintsL);        
        tfNextThrottleFrame = new ShortCutsField( _tpwkc.getNextThrottleFrameKeys());
        propertyPanel.add(tfNextThrottleFrame, constraintsR);        
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
              
        propertyPanel.add(new JLabel(Bundle.getMessage("PrevThrottleFrame")), constraintsL);
        tfPrevThrottleFrame = new ShortCutsField( _tpwkc.getPrevThrottleFrameKeys());
        propertyPanel.add(tfPrevThrottleFrame, constraintsR);                
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
              
        propertyPanel.add(new JLabel(Bundle.getMessage("NextRunningThrottleFrame")), constraintsL);
        tfNextRunningThrottleFrame = new ShortCutsField( _tpwkc.getNextRunThrottleFrameKeys());
        propertyPanel.add(tfNextRunningThrottleFrame, constraintsR);        
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
              
        propertyPanel.add(new JLabel(Bundle.getMessage("PrevRunningThrottleFrame")), constraintsL);
        tfPrevRunningThrottleFrame = new ShortCutsField( _tpwkc.getPrevRunThrottleFrameKeys());
        propertyPanel.add(tfPrevRunningThrottleFrame, constraintsR);        
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
              
        propertyPanel.add(new JLabel(Bundle.getMessage("NextThrottleInternalWindow")), constraintsL);
        tfNextThrottleInternalWindow = new ShortCutsField( _tpwkc.getNextThrottleInternalWindowKeys());
        propertyPanel.add(tfNextThrottleInternalWindow, constraintsR);        
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
              
        propertyPanel.add(new JLabel(Bundle.getMessage("PrevThrottleInternalWindow")), constraintsL);
        tfPrevThrottleInternalWindow = new ShortCutsField( _tpwkc.getPrevThrottleInternalWindowKeys());
        propertyPanel.add(tfPrevThrottleInternalWindow, constraintsR);        
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
              
        propertyPanel.add(new JLabel(Bundle.getMessage("GotoControl")), constraintsL);
        tfGotoControl = new ShortCutsField( _tpwkc.getMoveToControlPanelKeys());
        propertyPanel.add(tfGotoControl, constraintsR);        
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
              
        propertyPanel.add(new JLabel(Bundle.getMessage("GotoFunctions")), constraintsL);
        tfGotoFunctions = new ShortCutsField( _tpwkc.getMoveToFunctionPanelKeys());
        propertyPanel.add(tfGotoFunctions, constraintsR);        
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
              
        propertyPanel.add(new JLabel(Bundle.getMessage("GotoAddress")), constraintsL);
        tfGotoAddress = new ShortCutsField( _tpwkc.getMoveToAddressPanelKeys());
        propertyPanel.add(tfGotoAddress, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;                

        propertyPanel.add(new JTitledSeparator(Bundle.getMessage("ThrottleSpeedControls")),constraintsS);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;

        propertyPanel.add(new JLabel(Bundle.getMessage("Forward")), constraintsL);
        tfForward = new ShortCutsField( _tpwkc.getForwardKeys());
        propertyPanel.add(tfForward, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;

        propertyPanel.add(new JLabel(Bundle.getMessage("Backward")), constraintsL);
        tfReverse = new ShortCutsField( _tpwkc.getReverseKeys());
        propertyPanel.add(tfReverse, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
        
        propertyPanel.add(new JLabel(Bundle.getMessage("SwitchDirection")), constraintsL);
        tfSwitchDir = new ShortCutsField( _tpwkc.getSwitchDirectionKeys());
        propertyPanel.add(tfSwitchDir, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;  
    
        propertyPanel.add(new JLabel(Bundle.getMessage("SpeedIdle")), constraintsL);
        tfSpeedIdle = new ShortCutsField( _tpwkc.getIdleKeys());
        propertyPanel.add(tfSpeedIdle, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;

        propertyPanel.add(new JLabel(Bundle.getMessage("SpeedStop")), constraintsL);
        tfSpeedStop = new ShortCutsField( _tpwkc.getStopKeys());
        propertyPanel.add(tfSpeedStop, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;

        propertyPanel.add(new JLabel(Bundle.getMessage("SpeedUp")), constraintsL);
        tfSpeedUp = new ShortCutsField( _tpwkc.getAccelerateKeys());
        propertyPanel.add(tfSpeedUp, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;

        propertyPanel.add(new JLabel(Bundle.getMessage("SpeedDown")), constraintsL);
        tfSpeedDown = new ShortCutsField( _tpwkc.getDecelerateKeys());
        propertyPanel.add(tfSpeedDown, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
        
        propertyPanel.add(new JLabel(Bundle.getMessage("SpeedUpMore")), constraintsL);
        tfSpeedUpMore = new ShortCutsField( _tpwkc.getAccelerateMoreKeys());
        propertyPanel.add(tfSpeedUpMore, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;

        propertyPanel.add(new JLabel(Bundle.getMessage("SpeedDownMore")), constraintsL);
        tfSpeedDownMore = new ShortCutsField( _tpwkc.getDecelerateMoreKeys());
        propertyPanel.add(tfSpeedDownMore, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;
        
        propertyPanel.add(new JLabel(Bundle.getMessage("SpeedMultiplier")), constraintsL);
        origSpeedMultiplier = _tpwkc.getMoreSpeedMultiplier();
        tfSpeedMultiplier = new JTextField(""+origSpeedMultiplier);
        tfSpeedMultiplier.setColumns(5);
        propertyPanel.add(tfSpeedMultiplier, constraintsR);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;

        propertyPanel.add(new JTitledSeparator(Bundle.getMessage("ThrottleFunctionsControls")),constraintsS);
        constraintsL.gridy++; 
        constraintsR.gridy++;
        constraintsS.gridy++;

        tfFunctionKeys = new ShortCutsField[_tpwkc.getNbFunctionsKeys()];
        for (int i=0; i<tfFunctionKeys.length; i++) {
            propertyPanel.add(new JLabel(Bundle.getMessage("Function")+" "+i), constraintsL);
            tfFunctionKeys[i] = new ShortCutsField( _tpwkc.getFunctionsKeys(i));
            propertyPanel.add(tfFunctionKeys[i], constraintsR);
            constraintsL.gridy++; 
            constraintsR.gridy++;
            constraintsS.gridy++;
        }        
    }

    public ThrottlesPreferences updateThrottlesPreferences(ThrottlesPreferences tp) {
        ThrottlesPreferencesWindowKeyboardControls tpwkc = tp.getThrottlesKeyboardControls();
        if (tfNextThrottleWindow.isDirty()) {
            tpwkc.setNextThrottleWindowKeys(tfNextThrottleWindow.getShortCuts() );
        }
        if (tfPrevThrottleWindow.isDirty()) {
            tpwkc.setPrevThrottleWindowKeys(tfPrevThrottleWindow.getShortCuts() );
        }
        if (tfNextThrottleFrame.isDirty()) {
            tpwkc.setNextTrottleFrameKeys( tfNextThrottleFrame.getShortCuts() );
        }
        if (tfPrevThrottleFrame.isDirty()) {
            tpwkc.setPrevThrottleFrameKeys( tfPrevThrottleFrame.getShortCuts() );
        }
        if (tfNextRunningThrottleFrame.isDirty()) {
            tpwkc.setNextRunThrottleFrameKeys( tfNextRunningThrottleFrame.getShortCuts() );
        }
        if (tfPrevRunningThrottleFrame.isDirty()) {
            tpwkc.setPrevRunThrottleFrameKeys( tfPrevRunningThrottleFrame.getShortCuts() );
        }
        if (tfNextThrottleInternalWindow.isDirty()) {
            tpwkc.setNextThrottleInternalWindowKeys( tfNextThrottleInternalWindow.getShortCuts() );
        }
        if (tfPrevThrottleInternalWindow.isDirty()) {
            tpwkc.setPrevThrottleInternalWindowKeys( tfPrevThrottleInternalWindow.getShortCuts() );
        }
        if (tfGotoControl.isDirty()) {
            tpwkc.setMoveToControlPanelKeys( tfGotoControl.getShortCuts() );
        }
        if (tfGotoFunctions.isDirty()) {
            tpwkc.setMoveToFunctionPanelKeys( tfGotoFunctions.getShortCuts() );
        }
        if (tfGotoAddress.isDirty()) {
            tpwkc.setMoveToAddressPanelKeys( tfGotoAddress.getShortCuts() );
        }
        if (tfForward.isDirty()) {
            tpwkc.setForwardKeys( tfForward.getShortCuts() );
        }
        if (tfReverse.isDirty()) {
            tpwkc.setReverseKeys( tfReverse.getShortCuts() );
        }
        if (tfSwitchDir.isDirty()) {
            tpwkc.setSwitchDirectionKeys( tfSwitchDir.getShortCuts() );
        }
        if (tfSpeedIdle.isDirty()) {
            tpwkc.setIdleKeys( tfSpeedIdle.getShortCuts() );
        }
        if (tfSpeedStop.isDirty()) {
            tpwkc.setStopKeys( tfSpeedStop.getShortCuts() );
        }
        if (tfSpeedUp.isDirty()) {
            tpwkc.setAccelerateKeys( tfSpeedUp.getShortCuts() );
        }
        if (tfSpeedDown.isDirty()) {
            tpwkc.setDecelerateKeys( tfSpeedDown.getShortCuts() );
        }
        if (tfSpeedUpMore.isDirty()) {
            tpwkc.setAccelerateMoreKeys( tfSpeedUpMore.getShortCuts() );
        }
        if (tfSpeedDownMore.isDirty()) {
            tpwkc.setDecelerateMoreKeys( tfSpeedDownMore.getShortCuts() );
        }
        for (int i=0; i<tfFunctionKeys.length; i++) {
            if (tfFunctionKeys[i].isDirty) {
                tpwkc.setFunctionsKeys (i, tfFunctionKeys[i].getShortCuts() );
            }
        }
        try {
            float sm = Float.parseFloat(tfSpeedMultiplier.getText());            
            if (Math.abs(sm - tpwkc.getMoreSpeedMultiplier()) > 0.0001) {
                tpwkc.setMoreSpeedMultiplier(sm);
            }
        }
        catch (NumberFormatException e) {
            log.error("Speed multiplier must be a numerical float value.");
        }
        return tp;
    }
    
    void resetComponents(ThrottlesPreferences tp) {
        try {
            _tpwkc = tp.getThrottlesKeyboardControls().clone();
        } catch (CloneNotSupportedException ex) {
            log.debug("Couldn't clone ThrottlesPreferencesWindowKeyboardControls");
        }
        this.removeAll();
        initComponents();
        revalidate();
    }

    boolean isDirty() {
        boolean ret = false;
        ret = tfNextThrottleWindow.isDirty() || ret;
        ret = tfPrevThrottleWindow.isDirty() || ret;
        ret = tfNextThrottleFrame.isDirty() || ret;
        ret = tfPrevThrottleFrame.isDirty() || ret;
        ret = tfNextRunningThrottleFrame.isDirty() || ret;
        ret = tfPrevRunningThrottleFrame.isDirty() || ret;
        ret = tfNextThrottleInternalWindow.isDirty() || ret;
        ret = tfPrevThrottleInternalWindow.isDirty() || ret;
        ret = tfGotoControl.isDirty() || ret;
        ret = tfGotoFunctions.isDirty() || ret;
        ret = tfGotoAddress.isDirty() || ret;
        ret = tfForward.isDirty() || ret;
        ret = tfReverse.isDirty() || ret;
        ret = tfSwitchDir.isDirty() || ret;
        ret = tfSpeedIdle.isDirty() || ret;
        ret = tfSpeedStop.isDirty() || ret;
        ret = tfSpeedDown.isDirty() || ret;
        ret = tfSpeedUp.isDirty() || ret;
        ret = tfSpeedDownMore.isDirty() || ret;
        ret = tfSpeedUpMore.isDirty() || ret;
        for (ShortCutsField tfFunctionKey : tfFunctionKeys) {
            if (tfFunctionKey.isDirty) {
                ret = tfFunctionKey.isDirty() || ret;
            }
        }
        try {
            float sm = Float.parseFloat(tfSpeedMultiplier.getText());            
            ret = (Math.abs(sm - origSpeedMultiplier) > 0.0001) || ret; 
        }
        catch (NumberFormatException e) {
            log.error("Speed multiplier must be a numerical float value.");
        }
        return ret;
    }

    final private class ShortCutsField extends JPanel {        
        int[][] shortcuts;
        boolean isDirty = false;
                
        ShortCutsField(int[][] values) {
            super();
            shortcuts = values;
            setLayout(new GridLayout());
            for (int[] v:shortcuts) {
                if (v[0]!=0 || v[1]!=0) {
                    add(new ShortCutPanel( this, v));
                }
            }
            add(new ShortCutTextField( this));        
        }

        private void addValue(int[] values, Component cmp) {            
            shortcuts = java.util.Arrays.copyOf(shortcuts, shortcuts.length+1);
            shortcuts[shortcuts.length-1]=values;
            add(new ShortCutPanel( this, shortcuts[shortcuts.length-1]));
            add(new ShortCutTextField( this));
            setDirty(true);
            remove(cmp);
            revalidate();                                
        }
        
        public boolean isDirty() {
            return isDirty;
        }
        
        public void setDirty(boolean b) {
            isDirty = b;
        }
        
        public int[][] getShortCuts() {
            return shortcuts;
        }
    }
    
    final private class ShortCutPanel extends JPanel {
        ShortCutsField shortCutsField;
        int[] shortcut; // [0]:modifier , [1]: extended key code
                
        ShortCutPanel(ShortCutsField scf, int[] sc) {
            super();
            shortCutsField = scf;
            shortcut = sc;
            setLayout(new BorderLayout());
            add(new ShortCutTextField(shortcut));
            JButton removeBtn = new JButton("X");
            removeBtn.addActionListener((ActionEvent e) -> {
                shortcut[0]=0;
                shortcut[1]=0;
                shortCutsField.setDirty(true);
                shortCutsField.remove(this);
                shortCutsField.revalidate();
            });
            add(removeBtn,BorderLayout.WEST);
            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        }
    }
    
    @SuppressWarnings("deprecation")    // Java 11 migration
    final private class ShortCutTextField extends JTextField {
        ShortCutsField shortCutsField;
        
        ShortCutTextField(int[] v) {
            super();
            setEditable(false);
            String text="";
            if (v[0]!=0) {
                text +=  KeyEvent.getKeyModifiersText(v[0])+" + ";
            }
            if (v[1]!=0) {
                text += KeyEvent.getKeyText(v[1]);
            }            
            super.setText(text);
        }
        
        ShortCutTextField(ShortCutsField scf) {
            super();
            setEditable(false);
            shortCutsField = scf;
            addKeyListener(new KeyAdapter() {
                    @Override
                    @SuppressWarnings("deprecation")    // Java 11 migration
                    public void keyReleased(KeyEvent e){
                        int[] values = new int[2];
                        values[0] = e.getModifiers();
                        values[1] = e.getExtendedKeyCode();
                        shortCutsField.addValue(values, e.getComponent());
                        log.debug("Key pressed: "+e.getKeyCode()+" / modifier: "+e.getModifiers()+" / ext. key code: "+e.getExtendedKeyCode()+" / location: "+e.getKeyLocation());
                    }
                });        
        }                          
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottlesPreferencesControlsSettingsPane.class);        
}
