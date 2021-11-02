package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.*;
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
import jmri.util.FileUtil;
import jmri.util.swing.WrapLayout;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JInternalFrame that contains buttons for each decoder function.
 */
public class FunctionPanel extends JInternalFrame implements FunctionListener, java.beans.PropertyChangeListener, AddressListener {

    private static final int DEFAULT_FUNCTION_BUTTONS = 24; // just enough to fill the initial pane
    private DccThrottle mThrottle;
    
    private JPanel mainPanel;
    private FunctionButton[] functionButtons;
    
    private AddressPanel addressPanel = null; // to access roster infos

    /**
     * Constructor
     */
    public FunctionPanel() {
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            log.debug("Creating new ThrottlesPreference Instance");
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        initGUI();
        applyPreferences();
    }

    public void destroy() {
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
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
     * Resize inner function buttons array
     * 
     */
    private void resizeFnButonsArray(int n) {
        FunctionButton[] newFunctionButtons = new FunctionButton[n];
        System.arraycopy(functionButtons, 0, newFunctionButtons, 0, Math.min( functionButtons.length, n));
        if (n > functionButtons.length) {
            for (int i=functionButtons.length;i<n;i++) {
                newFunctionButtons[i] = new FunctionButton();
                mainPanel.add(newFunctionButtons[i]);
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
     * Get notification that a function has changed state.
     *
     * @param functionNumber The function that has changed.
     * @param isSet          True if the function is now active (or set).
     */
    @Override
    public void notifyFunctionStateChanged(int functionNumber, boolean isSet) {
        log.debug("notifyFunctionStateChanged: fNumber={} isSet={} " ,functionNumber, isSet);
        if (mThrottle != null) {
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

    public void setAddressPanel(AddressPanel addressPanel) {
        this.addressPanel = addressPanel;
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
                if (rosterEntry.getAttribute(fontSizeKey) != null && functionButton.getFont().getSize() == FunctionButton.DEFAULT_FONT_SIZE) {
                    rosterEntry.deleteAttribute(fontSizeKey);
                }
                if (functionButton.getFont().getSize() != FunctionButton.DEFAULT_FONT_SIZE) {
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new WrapLayout(FlowLayout.CENTER, 2, 2));
        resetFnButtons();
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getViewport().setOpaque(false); // container already gets this done (for play/edit mode)
        scrollPane.setOpaque(false);
        Border empyBorder = new EmptyBorder(0,0,0,0); // force look'n feel, no border
        scrollPane.setViewportBorder( empyBorder ); 
        scrollPane.setBorder( empyBorder );
        scrollPane.setWheelScrollingEnabled(false); // already used by speed slider
        setContentPane(scrollPane);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
    private void setUpDefaultLightFunctionButton() {
        try {
            functionButtons[0].setIconPath("resources/icons/functionicons/svg/lightsOff.svg");
            functionButtons[0].setSelectedIconPath("resources/icons/functionicons/svg/lightsOn.svg");
        } catch (Exception e) {
            log.debug("Exception loading svg icon : " + e.getMessage());
        } finally {
            if ((functionButtons[0].getIcon() == null) || (functionButtons[0].getSelectedIcon() == null)) {
                log.debug("Issue loading svg icon, reverting to png");
                functionButtons[0].setIconPath("resources/icons/functionicons/transparent_background/lights_off.png");
                functionButtons[0].setSelectedIconPath("resources/icons/functionicons/transparent_background/lights_on.png");
            }
        }
    }
    
    /**
     * Apply preferences
     *   + global throttles preferences
     *   + this throttle settings if any
     */
    public final void applyPreferences() {
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
        RosterEntry re = null;
        if (mThrottle != null && addressPanel != null) {
            re = addressPanel.getRosterEntry();
        }
        for (int i = 0; i < functionButtons.length; i++) {
            if ((i == 0) && preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon()) {
                setUpDefaultLightFunctionButton();
            } else {
                functionButtons[i].setIconPath(null);
                functionButtons[i].setSelectedIconPath(null);
            }
            if (re != null) {
                if (re.getFunctionLabel(i) != null) {
                    functionButtons[i].setDisplay(true);
                    functionButtons[i].setButtonLabel(re.getFunctionLabel(i));
                    if (preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon()) {
                        functionButtons[i].setIconPath(re.getFunctionImage(i));
                        functionButtons[i].setSelectedIconPath(re.getFunctionSelectedImage(i));
                    } else {
                        functionButtons[i].setIconPath(null);
                        functionButtons[i].setSelectedIconPath(null);
                    }
                    functionButtons[i].setIsLockable(re.getFunctionLockable(i));
                } else {
                    functionButtons[i].setDisplay( ! (preferences.isUsingExThrottle() && preferences.isHidingUndefinedFuncButt()) );
                }
            }
            functionButtons[i].updateLnF();
        }
    }
    
    /**
     * Rebuild function buttons 
     * 
     */
    private void rebuildFnButons(int n) {
        mainPanel.removeAll();
        functionButtons = new FunctionButton[n];
        for (int i = 0; i < functionButtons.length; i++) {
            functionButtons[i] = new FunctionButton();
            resetFnButton(functionButtons[i],i);
            mainPanel.add(functionButtons[i]);
            // Copy mouse and keyboard controls to new components
            for (MouseWheelListener mwl:getMouseWheelListeners()) {
                functionButtons[i].addMouseWheelListener(mwl);
            }
        }
    }

    /**
     * Update function buttons
     *    - from selected throttle setting and state
     *    - from roster entry if any
     */    
    private void updateFnButtons() {
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
        if (mThrottle != null && addressPanel != null) {                
            RosterEntry rosterEntry = addressPanel.getRosterEntry();
            if (rosterEntry != null) {
                log.debug("RosterEntry found: {}", rosterEntry.getId());
            }
            for (int i = 0; i < functionButtons.length; i++) {
                // update from selected throttle setting
                functionButtons[i].setEnabled(true);
                functionButtons[i].setIdentity(i); // full reset of function
                functionButtons[i].setThrottle(mThrottle);
                functionButtons[i].setState(mThrottle.getFunction(i)); // reset button state
                functionButtons[i].setIsLockable(!mThrottle.getFunctionMomentary(i));
                functionButtons[i].setDropFolder(FileUtil.getUserResourcePath());
                // update from roster entry if any
                if (rosterEntry != null) { 
                    functionButtons[i].setDropFolder(Roster.getDefault().getRosterFilesLocation());
                    boolean needUpdate = false;
                    String imgButtonSize = rosterEntry.getAttribute("function"+i+"_ThrottleImageButtonSize");
                    if (imgButtonSize != null) {
                        try {
                            functionButtons[i].setButtonImageSize(Integer.parseInt(imgButtonSize));
                            needUpdate = true;                            
                        } catch (NumberFormatException e) {
                            log.debug("setFnButtons(): can't parse button image size attribute ");
                        }
                    }                    
                    String text = rosterEntry.getFunctionLabel(i);
                    if (text != null) {
                        functionButtons[i].setDisplay(true);
                        functionButtons[i].setButtonLabel(text);
                        if (preferences.isUsingExThrottle() && preferences.isUsingFunctionIcon()) {
                            functionButtons[i].setIconPath(rosterEntry.getFunctionImage(i));
                            functionButtons[i].setSelectedIconPath(rosterEntry.getFunctionSelectedImage(i));
                        } else {
                            functionButtons[i].setIconPath(null);
                            functionButtons[i].setSelectedIconPath(null);
                        }
                        functionButtons[i].setIsLockable(rosterEntry.getFunctionLockable(i));
                        needUpdate = true;
                    } else if (preferences.isUsingExThrottle()
                            && preferences.isHidingUndefinedFuncButt()) {
                        functionButtons[i].setDisplay(false);
                        needUpdate = true;
                    }
                    String fontSize = rosterEntry.getAttribute("function"+i+"_ThrottleFontSize");
                    if (fontSize != null) {
                        try {
                            functionButtons[i].setFont(new Font("Monospaced", Font.PLAIN, Integer.parseInt(fontSize)));
                            needUpdate = true;                            
                        } catch (NumberFormatException e) {
                            log.debug("setFnButtons(): can't parse font size attribute ");
                        }
                    }                   
                    if (needUpdate) {
                        functionButtons[i].updateLnF();
                    }
                }                
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
            setUpDefaultLightFunctionButton();                
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
     * Reset function buttons : 
     *    - rebuild function buttons
     *    - reset their properties to default
     *    - update according to throttle and roster (if any)
     * 
     */
    public void resetFnButtons() {
        // rebuild function buttons
        if (mThrottle == null) {
            rebuildFnButons(DEFAULT_FUNCTION_BUTTONS);
        } else {
            rebuildFnButons(mThrottle.getFunctions().length);
        }
        // reset their properties to defaults
        for (int i = 0; i < functionButtons.length; i++) {  
            resetFnButton(functionButtons[i],i);
        }
        // update according to throttle and roster (if any)
        updateFnButtons();
        repaint();
    }

    /**
     * Update the state of this panel if any of the functions change.
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (mThrottle!=null){
            for (int i = 0; i < mThrottle.getFunctions().length; i++) {
                if (e.getPropertyName().equals(Throttle.getFunctionString(i))) {
                    setButtonByFuncNumber(i,false,(Boolean) e.getNewValue());
                } else if (e.getPropertyName().equals(Throttle.getFunctionMomentaryString(i))) {
                    setButtonByFuncNumber(i,true,!(Boolean) e.getNewValue());
                }
            }
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
        children.add(WindowPreferences.getPreferences(this));
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
        Element window = e.getChild("window");
        WindowPreferences.setPreferences(this, window);

        java.util.List<Element> buttonElements = e.getChildren("FunctionButton");

        if (buttonElements != null && buttonElements.size() > 0) {
            // just in case
            rebuildFnButons( buttonElements.size() );
            int i = 0;
            for (Element buttonElement : buttonElements) {
                functionButtons[i++].setXml(buttonElement);
            }
        }
    }

    /**
     * Get notification that a throttle has been found as we requested.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        log.debug("Throttle found");
        mThrottle = t;        
        mThrottle.addPropertyChangeListener(this);
        resizeFnButonsArray(mThrottle.getFunctions().length);
        updateFnButtons();
        setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAddressReleased(LocoAddress la) {
        log.debug("Throttle released");        
        if (mThrottle != null) {
            mThrottle.removePropertyChangeListener(this);
        }
        mThrottle = null;
        resetFnButtons(); 
        setEnabled(false);
    }

    /**
     * Ignored.
     * {@inheritDoc}
     */
    @Override
    public void notifyAddressChosen(LocoAddress l) {
    }

    /**
     * Ignored.
     * {@inheritDoc}
     */
    @Override
    public void notifyConsistAddressChosen(int newAddress, boolean isLong) {
    }

    /**
     * Ignored.
     * {@inheritDoc}
     */
    @Override
    public void notifyConsistAddressReleased(int address, boolean isLong) {
    }

    /**
     * Ignored.
     * {@inheritDoc}
     */
    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
    }
    
    private final static Logger log = LoggerFactory.getLogger(FunctionPanel.class);

}
