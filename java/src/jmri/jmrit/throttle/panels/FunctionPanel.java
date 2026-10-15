package jmri.jmrit.throttle.panels;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.functiontable.FunctionTableModel;
import jmri.jmrit.throttle.interfaces.AddressListener;
import jmri.jmrit.throttle.interfaces.FunctionListener;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.util.FileUtil;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.swing.OptionallyTabbedPanel;

import org.jdom2.Element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A Panel that contains buttons for each decoder function.
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 */
public class FunctionPanel extends OptionallyTabbedPanel implements FunctionListener, PropertyChangeListener, AddressListener {

    private static final int DEFAULT_FUNCTION_BUTTONS = 24; // just enough to fill the initial pane
    private static final int MAX_FUNCTION_BUTTONS_PER_TAB = 33; 
    private DccThrottle mThrottle;

    private FunctionButton[] functionButtons;
    private int[] btnIdxFn; // fn button index to fn map (see function buttons display order)

    private boolean withPopupMenuOnFnButtons;
    private boolean fnBtnUpdatedFromRoster = false; // avoid to reinit function button twice (from throttle xml and from roster)

    private AddressPanel addressPanel = null; // to access roster infos

    /**
     * Constructor
     * 
     *  @param withPopupMenu  popup menu on function button available if true
     * 
     */
    public FunctionPanel(boolean withPopupMenu) {
        super(MAX_FUNCTION_BUTTONS_PER_TAB);
        InstanceManager.getDefault(ThrottlesPreferences.class).addPropertyChangeListener(this);
        withPopupMenuOnFnButtons = withPopupMenu;
        initGUI();        
    }

    public FunctionPanel() {
        this(true);
    }

    public void dispose() {
        InstanceManager.getDefault(ThrottlesPreferences.class).removePropertyChangeListener(this);
        if (functionButtons != null) {
            for (FunctionButton fb : functionButtons) {
                fb.destroy();
                fb.removeFunctionListener(this);
            }
            functionButtons = null;
        }
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
            addressPanel = null;
        }
        if (mThrottle != null) {
            mThrottle.removePropertyChangeListener(this);
            mThrottle = null;
        }
    }

    public FunctionButton[] getFunctionButtons() {
        return Arrays.copyOf(functionButtons, functionButtons.length);
    }

    /**
     * Get notification that a function has changed state.
     *
     * @param functionNumber The function that has changed.
     * @param isSet          True if the function is now active (or set).
     */
    @Override
    public void notifyFunctionStateChanged(int functionNumber, boolean isSet) {
        log.debug("notifyFunctionStateChanged: fNumber={} isSet={} " ,functionNumber, isSet);
        if (mThrottle != null) {
            log.debug("setting throttle {} function {}", mThrottle.getLocoAddress(), functionNumber);
            mThrottle.setFunction(functionNumber, isSet);
        }
    }

    /**
     * Get notification that a function's lockable status has changed.
     *
     * @param functionNumber The function that has changed (0-28).
     * @param isLockable     True if the function is now Lockable (continuously
     *                       active).
     */
    @Override
    public void notifyFunctionLockableChanged(int functionNumber, boolean isLockable) {
        log.debug("notifyFnLockableChanged: fNumber={} isLockable={} " ,functionNumber, isLockable);
        if (mThrottle != null) {
            log.debug("setting throttle {} function momentary {}", mThrottle.getLocoAddress(), functionNumber);
            mThrottle.setFunctionMomentary(functionNumber, !isLockable);
        }
    }

    /**
     * Enable or disable all the buttons.
     * @param isEnabled true to enable, false to disable.
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        for (FunctionButton functionButton : functionButtons) {
            functionButton.setEnabled(isEnabled);
        }
    }

    /**
     * Enable or disable all the buttons depending on throttle status
     * If a throttle is assigned, enable all, else disable all
     */
    public void setEnabled() {
        setEnabled(mThrottle != null);
    }

    public void setAddressPanel(AddressPanel ap) {
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
        }
        addressPanel = ap;
        if (addressPanel != null) {
            addressPanel.addAddressListener(this);        
            if (addressPanel.getThrottle() != null ) {
                notifyAddressThrottleFound(addressPanel.getThrottle());
            } else {
                notifyAddressReleased(addressPanel.getCurrentAddress());
            }
        }
    }

    public void saveFunctionButtonsToRoster(RosterEntry rosterEntry) {
        log.debug("saveFunctionButtonsToRoster");
        if (rosterEntry == null) {
            return;
        }
        for (FunctionButton functionButton : functionButtons) {
            int functionNumber = functionButton.getIdentity();
            String text = functionButton.getButtonLabel();
            boolean lockable = functionButton.getIsLockable();
            boolean visible = functionButton.getDisplay();
            String imagePath = functionButton.getIconPath();
            String imageSelectedPath = functionButton.getSelectedIconPath();
            if (functionButton.isDirty()) {
                if (!text.equals(rosterEntry.getFunctionLabel(functionNumber))) {
                    if (text.isEmpty()) {
                        text = null;  // reset button text to default
                    }
                    rosterEntry.setFunctionLabel(functionNumber, text);
                }
                String fontSizeKey = "function"+functionNumber+"_ThrottleFontSize";
                if (rosterEntry.getAttribute(fontSizeKey) != null && functionButton.getFont().getSize() == InstanceManager.getDefault(GuiLafPreferencesManager.class).getFontSize()) {
                    rosterEntry.deleteAttribute(fontSizeKey);
                }
                if (functionButton.getFont().getSize() != InstanceManager.getDefault(GuiLafPreferencesManager.class).getFontSize()) {
                    rosterEntry.putAttribute(fontSizeKey, ""+functionButton.getFont().getSize());
                }
                String imgButtonSizeKey = "function"+functionNumber+"_ThrottleImageButtonSize";
                if (rosterEntry.getAttribute(imgButtonSizeKey) != null && functionButton.getButtonImageSize() == FunctionButton.DEFAULT_IMG_SIZE) {
                    rosterEntry.deleteAttribute(imgButtonSizeKey);
                }
                if (functionButton.getButtonImageSize() != FunctionButton.DEFAULT_IMG_SIZE) {
                    rosterEntry.putAttribute(imgButtonSizeKey, ""+functionButton.getButtonImageSize());
                }
                if (rosterEntry.getFunctionLabel(functionNumber) != null ) {
                    if( lockable != rosterEntry.getFunctionLockable(functionNumber)) {
                        rosterEntry.setFunctionLockable(functionNumber, lockable);
                    }
                    if( visible != rosterEntry.getFunctionVisible(functionNumber)) {
                        rosterEntry.setFunctionVisible(functionNumber, visible);
                    }
                    if ( (!imagePath.isEmpty() && rosterEntry.getFunctionImage(functionNumber) == null )
                            || (rosterEntry.getFunctionImage(functionNumber) != null && imagePath.compareTo(rosterEntry.getFunctionImage(functionNumber)) != 0)) {
                        rosterEntry.setFunctionImage(functionNumber, imagePath);
                    }
                    if ( (!imageSelectedPath.isEmpty() && rosterEntry.getFunctionSelectedImage(functionNumber) == null )
                            || (rosterEntry.getFunctionSelectedImage(functionNumber) != null && imageSelectedPath.compareTo(rosterEntry.getFunctionSelectedImage(functionNumber)) != 0)) {
                        rosterEntry.setFunctionSelectedImage(functionNumber, imageSelectedPath);
                    }
                }
                functionButton.setDirty(false);
            }
        }
        Roster.getDefault().writeRoster();
    }

    /**
     * Place and initialize all the buttons.
     */
    private void initGUI() {        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // init function buttons
        resizeFnButons(DEFAULT_FUNCTION_BUTTONS);
        // update according to throttle and roster (if any)
        applyFnButtonsLnF();
        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.getViewport().setOpaque(false); // container already gets this done (for play/edit mode)
        scrollPane.setOpaque(false);
        setOpaque(false);
        Border empyBorder = new EmptyBorder(0,0,0,0); // force look'n feel, no border
        scrollPane.setViewportBorder( empyBorder );
        scrollPane.setBorder( empyBorder );
        scrollPane.setWheelScrollingEnabled(false); // already used by speed slider
        scrollPane.getViewport().addChangeListener((e) -> revalidate());
    }

    /**
     * Resize or build inner function button array
     *
     */
    private void resizeFnButons(int numFns) {
        // Resize inner function buttons array
        FunctionButton[] newFunctionButtons = new FunctionButton[numFns];
        int prevl = 0;
        if (functionButtons != null) {
            System.arraycopy(functionButtons, 0, newFunctionButtons, 0, Math.min( functionButtons.length, numFns));
            prevl = functionButtons.length ;
        }
        if (numFns > prevl) {
            for (int i=prevl;i<numFns;i++) {
                newFunctionButtons[i] = new FunctionButton(withPopupMenuOnFnButtons);
                add(newFunctionButtons[i]);
                resetFnButton(newFunctionButtons[i],i);
                // Copy mouse and keyboard controls to new components
                for (MouseWheelListener mwl:getMouseWheelListeners()) {
                   newFunctionButtons[i].addMouseWheelListener(mwl);
                }
            }
        }
        functionButtons = newFunctionButtons;
    }

    /**
     * Update function buttons look'n feel
     *    - from default layout setting if any
     *    - from selected throttle setting and state
     *    - from roster entry if any
     */
    public void applyFnButtonsLnF() {
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);        
        final RosterEntry rosterEntry = (addressPanel != null?addressPanel.getRosterEntry():null);
        btnIdxFn = null;
        // 1 - reset to default xml layout if any
        setXml(preferences.getFunctionPanelDefaultXML()); 
        // retrieve function buttons order from roster entry if any
        if (rosterEntry != null) { 
            fnBtnUpdatedFromRoster = true;
            log.debug("RosterEntry found: {}", rosterEntry.getId());
            String fnDOAtt = rosterEntry.getAttribute(FunctionTableModel.FB_ORDER_ATTR);
            btnIdxFn = null;
            if (fnDOAtt != null) {
                try {
                    btnIdxFn = new ObjectMapper().readValue(fnDOAtt, int[].class );
                } catch (JsonProcessingException e) {
                    log.warn("Couldn't parse FnDisplayOrder attribute ",e);
                } 
            }    
        }
        for (int i = 0; i < functionButtons.length; i++) {
            if (preferences.getFunctionPanelDefaultXML() == null) { // we don't have a default xml layout, hard reset each button
                resetFnButton(functionButtons[i],i );
            }
            // update from selected throttle setting
            functionButtons[i].setEnabled(true);
            functionButtons[i].setIdentity(i); // full reset of function
            functionButtons[i].setDropFolder(FileUtil.getUserResourcePath());
            functionButtons[i].setThrottle(mThrottle);
            if (mThrottle != null) {
                functionButtons[i].setState(mThrottle.getFunction(i)); // reset button state
                if (preferences.getFunctionPanelDefaultXML() == null) { // we don't have a default layout, ask the throttle 
                    functionButtons[i].setIsLockable(!mThrottle.getFunctionMomentary(i));
                }
            }                
            // update from roster entry if any
            if (rosterEntry != null) {
                int fn = i;
                if ((btnIdxFn != null) && (i < btnIdxFn.length)) {
                    fn = btnIdxFn[i];
                }    
                functionButtons[i].setIdentity(fn);                
                functionButtons[i].setDropFolder(Roster.getDefault().getRosterFilesLocation());
                String imgButtonSize = rosterEntry.getAttribute("function"+fn+"_ThrottleImageButtonSize");
                if (imgButtonSize != null) {
                    try {
                        functionButtons[i].setButtonImageSize(Integer.parseInt(imgButtonSize));
                    } catch (NumberFormatException e) {
                        log.debug("setFnButtons(): can't parse button image size attribute ");
                    }
                }
                functionButtons[i].setButtonLabel( i<3 ? Bundle.getMessage(Throttle.getFunctionString(i)) : Throttle.getFunctionString(i) );
                String text = rosterEntry.getFunctionLabel(fn);
                if (text != null) {
                    functionButtons[i].setDisplay(rosterEntry.getFunctionVisible(fn));
                    functionButtons[i].setButtonLabel(text);
                    if (preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon()) {
                        functionButtons[i].setIconPath(rosterEntry.getFunctionImage(fn));
                        functionButtons[i].setSelectedIconPath(rosterEntry.getFunctionSelectedImage(fn));
                    } else {
                        functionButtons[i].setIconPath(null);
                        functionButtons[i].setSelectedIconPath(null);
                    }
                    functionButtons[i].setIsLockable(rosterEntry.getFunctionLockable(fn));
                } else if (preferences.isUsingExThrottle()
                        && preferences.isHidingUndefinedFuncButt()) {
                    functionButtons[i].setDisplay(false);
                } else {
                    functionButtons[i].setDisplay(true);
                    functionButtons[i].setButtonLabel( fn<3 ? Bundle.getMessage(Throttle.getFunctionString(fn)) : Throttle.getFunctionString(fn) );
                }
                String fontSize = rosterEntry.getAttribute("function"+fn+"_ThrottleFontSize");
                if (fontSize != null) {
                    try {
                        functionButtons[i].setFont(new Font("Monospaced", Font.PLAIN, Integer.parseInt(fontSize)));
                    } catch (NumberFormatException e) {
                        log.debug("setFnButtons(): can't parse font size attribute ");
                    }
                }
                functionButtons[i].updateLnF();                
            }
        }        
    }

    private void resetFnButton(FunctionButton fb, int i) {
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
        fb.setThrottle(mThrottle);
        if (mThrottle!=null) {
            fb.setState(mThrottle.getFunction(i)); // reset button state
            fb.setIsLockable(!mThrottle.getFunctionMomentary(i));
        }
        fb.setIdentity(i);
        fb.addFunctionListener(this);
        fb.setButtonLabel( i<3 ? Bundle.getMessage(Throttle.getFunctionString(i)) : Throttle.getFunctionString(i) );
        fb.setDisplay(true);
        if ((i == 0) && preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon()) {
            try {
                fb.setIconPath("resources/icons/functionicons/svg/lightsOff.svg");
                fb.setSelectedIconPath("resources/icons/functionicons/svg/lightsOn.svg");
            } catch (Exception e) {
                log.debug("Exception loading svg icon : {}", e.getMessage());
            } finally {
                if ((fb.getIcon() == null) || (fb.getSelectedIcon() == null)) {
                    log.debug("Issue loading svg icon, reverting to png");
                    fb.setIconPath("resources/icons/functionicons/transparent_background/lights_off.png");
                    fb.setSelectedIconPath("resources/icons/functionicons/transparent_background/lights_on.png");
                }
            } 
        } else {
            fb.setIconPath(null);
            fb.setSelectedIconPath(null);
        }
        fb.updateLnF();

        // always display f0, F1 and F2
        if (i < 3) {
            fb.setVisible(true);
        }
    }

    /**
     * Update the state of this panel if any of the functions change.
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e == null) {
            return;
        }
        log.debug("Property change event received {} / {}", e.getPropertyName(), e.getNewValue());        
        if (mThrottle!=null){
            for (int i = 0; i < mThrottle.getFunctions().length; i++) {
                if (e.getPropertyName().equals(Throttle.getFunctionString(i))) {
                    setButtonByFuncNumber(i,false,(Boolean) e.getNewValue());
                } else if (e.getPropertyName().equals(Throttle.getFunctionMomentaryString(i))) {
                    setButtonByFuncNumber(i,true,!(Boolean) e.getNewValue());
                }
            }
        }
        if (ThrottlesPreferences.prefPopertyName.compareTo(e.getPropertyName()) == 0) {
            applyFnButtonsLnF();
            setEnabled();
        }        
    }

    private void setButtonByFuncNumber(int function, boolean lockable, boolean newVal){
        for (FunctionButton button : functionButtons) {
            if (button.getIdentity() == function) {
                if (lockable) {
                    button.setIsLockable(newVal);
                } else {
                    button.setState(newVal);
                }
            }
        }
    }

    /**
     * Collect the prefs of this object into XML Element.
     * <ul>
     * <li> Window prefs
     * <li> Each button has id, text, lock state.
     * </ul>
     *
     * @return the XML of this object.
     */
    public Element getXml() {
        Element me = new Element("FunctionPanel"); // NOI18N
        java.util.ArrayList<Element> children = new java.util.ArrayList<>(1 + functionButtons.length);
        for (FunctionButton functionButton : functionButtons) {
            children.add(functionButton.getXml());
        }
        me.setContent(children);
        return me;
    }

    /**
     * Set the preferences based on the XML Element.
     * <ul>
     * <li> Window prefs
     * <li> Each button has id, text, lock state.
     * </ul>
     *
     * @param e The Element for this object.
     */
    public void setXml(Element e) {
        if (e == null) {
            return;
        }  
        if (! fnBtnUpdatedFromRoster) {
            java.util.List<Element> buttonElements = e.getChildren("FunctionButton");

            if (buttonElements != null && buttonElements.size() > 0) {
                // just in case
                resizeFnButons( buttonElements.size() );
                int i = 0;
                for (Element buttonElement : buttonElements) {
                    functionButtons[i++].setXml(buttonElement);
                }                
            }
            setEnabled(); // refresh state
        }
    }

    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        log.debug("Throttle found for {}",t);
        if (mThrottle != null) {
            mThrottle.removePropertyChangeListener(this);
        }
        mThrottle = t;
        mThrottle.addPropertyChangeListener(this);
        if (addressPanel!=null) {
            notifyRosterEntryUpdated(addressPanel.getRosterEntry() );
        } else {
            notifyRosterEntryUpdated(null);
        }
        setEnabled(true);
    }

    private void adressReleased() {
        if (mThrottle != null) {
            mThrottle.removePropertyChangeListener(this);
        }
        mThrottle = null;
        fnBtnUpdatedFromRoster = false;
        setEnabled(false);
    }

    @Override
    public void notifyAddressReleased(LocoAddress la) {
        log.debug("Throttle released");
        adressReleased();
    }

    @Override
    public void notifyAddressChosen(LocoAddress l) {
    }

    @Override
    public void notifyNewRosterEntryHighlighted(RosterEntry re) {     
    }

    @Override
    public void notifyRosterEntryUpdated(RosterEntry re) {
        int numFns = mThrottle.getFunctions().length;
        if (re != null) {
            // +1 because we want the _number_ of functions, and we have to count F0
            numFns = Math.min(numFns, re.getMaxFnNumAsInt()+1);
        }
        log.debug("notifyRosterEntryUpdated number of functions {}", numFns);
        resizeFnButons(numFns);
        applyFnButtonsLnF();
    }

    @Override
    public void notifyConsistAddressChosen(LocoAddress l) {
    }

    @Override
    public void notifyConsistAddressReleased(LocoAddress la) {
        log.debug("Consist throttle released");
        adressReleased();
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle t) {
        log.debug("Consist throttle found");
        if (mThrottle == null) {
            notifyAddressThrottleFound(t);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FunctionPanel.class);
}
