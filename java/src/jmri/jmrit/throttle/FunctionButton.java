package jmri.jmrit.throttle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import jmri.Throttle;
import jmri.util.FileUtil;
import jmri.util.swing.ResizableImagePanel;
import jmri.util.swing.ToggleOrPressButtonModel;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JButton to activate functions on the decoder. FunctionButtons have a
 * right-click popup menu with several configuration options:
 * <ul>
 * <li> Set the text
 * <li> Set the locking state
 * <li> Set visibility
 * <li> Set Font
 * <li> Set function number identity
 * </ul>
 *
 * @author Glen Oberhauser
 * @author Bob Jacobsen Copyright 2008
 */
public class FunctionButton extends JToggleButton {

    private final ArrayList<FunctionListener> listeners;
    private int identity; // F0, F1, etc
    private boolean isDisplayed = true;
    private boolean dirty = false;
    private boolean isImageOK = false;
    private boolean isSelectedImageOK = false;
    private int actionKey;
    private String buttonLabel;
    private final JPopupMenu popup;
    private String iconPath;
    private String selectedIconPath;
    private ToggleOrPressButtonModel _model;
    private Throttle _throttle;

    final static int BUT_HGHT;
    final static int BUT_WDTH;
    final static int BUT_IMG_SIZE = 45;

    static {
        JButton sample = new JButton(" Light ");
        BUT_HGHT = java.lang.Math.max(sample.getPreferredSize().height, 30);
        BUT_WDTH = java.lang.Math.max(sample.getPreferredSize().width, 56);
    }

    /**
     * Get Button Height.
     * @return height.
     */
    public static int getButtonHeight() {
        return BUT_HGHT;
    }

    /**
     * Get the Button Width.
     * @return width.
     */
    public static int getButtonWidth() {
        return BUT_WDTH;
    }

    /**
     * Construct the FunctionButton.
     */
    public FunctionButton() {
        super();
        popup = new JPopupMenu();
        listeners = new ArrayList<>();
        init();
    }
    
    final void init(){
        
        _model = new ToggleOrPressButtonModel(this, true);
        setModel(_model);
        
        JMenuItem propertiesItem = new JMenuItem(Bundle.getMessage("MenuItemProperties"));
        propertiesItem.addActionListener((ActionEvent e) -> {
            FunctionButtonPropertyEditor editor = new FunctionButtonPropertyEditor();
            editor.setFunctionButton(this);
            editor.setLocation(this.getLocationOnScreen());
            editor.setVisible(true);
          });
        popup.add(propertiesItem);
        //Add listener to components that can bring up popup menus.
        addMouseListener(new PopupListener());
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        setMargin(new Insets(2, 2, 2, 2));
        setRolloverEnabled(false);
        updateLnF();
    }

    /**
     * Set the function number this button will operate.
     *
     * @param id An integer, minimum 0.
     */
    public void setIdentity(int id) {
        this.identity = id;
    }

    /**
     * Get the function number this button operates.
     *
     * @return An integer, minimum 0.
     */
    public int getIdentity() {
        return identity;
    }

    /**
     * Set the keycode that this button should respond to.
     * <p>
     * Later, when a key is being processed, checkKeyCode will determine if
     * there's a match between the key that was pressed and the key for this
     * button
     * @param key KeyCode value.
     */
    public void setKeyCode(int key) {
        actionKey = key;
    }

    /**
     * Check to see whether a particular KeyCode corresponds to this function
     * button.
     *
     * @param keycode keycode to check against.
     * @return true if the button should respond to this key
     */
    public boolean checkKeyCode(int keycode) {
        return keycode == actionKey;
    }

    /**
     * Set the state of the function button.
     * Does not send update to layout, just updates button status.
     * <p>
     * To update AND send to layout use setSelected(boolean).
     * 
     * @param isOn True if the function should be active.
     */
    public void setState(boolean isOn) {
        super.setSelected(isOn);
        _model.updateSelected(isOn);
    }

    /**
     * Get the state of the function.
     *
     * @return true if the function is active.
     */
    public boolean getState() {
        return isSelected();
    }

    /**
     * Set the locking state of the button.
     * <p>
     * Changes in this parameter are only be sent to the 
     * listeners if the dirty bit is set.
     *
     * @param isLockable True if the a clicking and releasing the button changes
     *                   the function state. False if the state is changed back
     *                   when the button is released
     */
    public void setIsLockable(boolean isLockable) {
        _model.setLockable(isLockable);
        if (isDirty()) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).notifyFunctionLockableChanged(identity, isLockable);
            }
        }
    }

    /**
     * Get the locking state of the function.
     *
     * @return True if the a clicking and releasing the button changes the
     *         function state. False if the state is changed back when the
     *         button is released
     */
    public boolean getIsLockable() {
        return _model.getLockable();
    }

    /**
     * Set the display state of the button.
     *
     * @param displayed True if the button exists False if the button has been
     *                  removed by the user
     */
    public void setDisplay(boolean displayed) {
        this.isDisplayed = displayed;
    }

    /**
     * Get the display state of the button.
     *
     * @return True if the button exists False if the button has been removed by
     *         the user
     */
    public boolean getDisplay() {
        return isDisplayed;
    }

    /**
     * Set Function Button Dirty.
     *
     * @param dirty True when button has been modified by user, else false.
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Get if Button is Dirty.
     * @return true when function button has been modified by user.
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Get the Button Label.
     * @return Button Label text.
     */
    public String getButtonLabel() {
        return buttonLabel;
    }

    /**
     * Set the Button Label.
     * @param label Label Text.
     */
    public void setButtonLabel(String label) {
        buttonLabel = label;
    }
    
    /**
     * Set Button Text.
     * {@inheritDoc}
     */
    @Override
    public void setText(String s) {
        if (s != null) {
            buttonLabel = s;
            if (isImageOK) {
                setToolTipText(buttonLabel);
                super.setText(null);
            } else {
                super.setText(s);
            }
            return;
        }
        super.setText(null);
        if (buttonLabel != null) {
            setToolTipText(buttonLabel);
        }
    }

    /**
     * Update Button Look and Feel.
     * Decide if it should show the label or an image with text as tooltip.
     * Button UI updated according to above result.
     */
    public void updateLnF() {
        setBorderPainted(!isImageOK());
        setContentAreaFilled(!isImageOK());
        setText( isImageOK() ? null : getButtonLabel() );
        if (isImageOK()) { // adjust button for image
            setPreferredSize(new Dimension(FunctionButton.BUT_IMG_SIZE, FunctionButton.BUT_IMG_SIZE));
        }
        else { // adjust button for text
            if (getButtonLabel() != null) {
                int butWidth = getFontMetrics(getFont()).stringWidth(getButtonLabel()) + 20; // pad out the width a bit
                setPreferredSize(new Dimension( Math.max(butWidth, FunctionButton.BUT_WDTH), FunctionButton.BUT_HGHT));
            } else {
                setPreferredSize(new Dimension(BUT_WDTH, BUT_HGHT));
            }
        }
    }    

    /**
     * Change the state of the function.
     * Sets internal state, setSelected, and sends to listeners.
     *
     * @param newState The new state. True = Is on, False = Is off.
     * @deprecated since 4.19.6; use 
     * {@link jmri.jmrit.throttle.FunctionButton#setSelected(boolean) } instead
     */
    @Deprecated
    public void changeState(boolean newState) {
        setSelected(newState);
    }
    
    /**
     * Change the state of the function.
     * Sets internal state, setSelected, and sends to listeners.
     * <p>
     * To update this button WITHOUT sending to layout, use setState.
     *
     * @param newState true = Is Function on, False = Is Function off.
     */
    @Override
    public void setSelected(boolean newState){
        log.debug("function selected {}", newState);
        super.setSelected(newState);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).notifyFunctionStateChanged(identity, newState);
        }
    }

    /**
     * Add a listener to this button, probably some sort of keypad panel.
     *
     * @param l The FunctionListener that wants notifications via the
     *          FunctionListener.notifyFunctionStateChanged.
     * @deprecated since 4.19.6; use 
     * {@link jmri.jmrit.throttle.FunctionButton#addFunctionListener(jmri.jmrit.throttle.FunctionListener) } instead
     */
    @Deprecated
    public void setFunctionListener(FunctionListener l) {
        addFunctionListener(l);
    }

    /**
     * Add a listener to this button, probably some sort of keypad panel.
     *
     * @param l The FunctionListener that wants notifications via the
     *          FunctionListener.notifyFunctionStateChanged.
     */
    public void addFunctionListener(FunctionListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Remove a listener from this button.
     *
     * @param l The FunctionListener to be removed
     */
    public void removeFunctionListener(FunctionListener l) {
        if (listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /**
     * A PopupListener to handle mouse clicks and releases.
     * Handles the popup menu.
     */
    private class PopupListener extends MouseAdapter {

        /**
         * If the event is the popup trigger, which is dependent on the
         * platform, present the popup menu.
         * @param e The MouseEvent causing the action.
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            checkTrigger(e);
        }
        
        /**
         * If the event is the popup trigger, which is dependent on the
         * platform, present the popup menu.
         * @param e The MouseEvent causing the action.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            checkTrigger( e);
        }

        /**
         * If the event is the popup trigger, which is dependent on the
         * platform, present the popup menu.
         * @param e The MouseEvent causing the action.
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            checkTrigger( e);
        }
        
        private void checkTrigger( MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /**
     * Collect the prefs of this object into XML Element.
     * <ul>
     * <li> identity
     * <li> text
     * <li> isLockable
     * </ul>
     *
     * @return the XML of this object.
     */
    public Element getXml() {
        Element me = new Element("FunctionButton"); // NOI18N
        me.setAttribute("id", String.valueOf(this.getIdentity()));
        me.setAttribute("text", this.getButtonLabel());
        me.setAttribute("isLockable", String.valueOf(this.getIsLockable()));
        me.setAttribute("isVisible", String.valueOf(this.getDisplay()));
        me.setAttribute("fontSize", String.valueOf(this.getFont().getSize()));
        if (this.getIconPath().startsWith(FileUtil.getUserResourcePath())) {
            me.setAttribute("iconPath", this.getIconPath().substring(FileUtil.getUserResourcePath().length()));
        } else {
            me.setAttribute("iconPath", this.getIconPath());
        }
        if (this.getSelectedIconPath().startsWith(FileUtil.getUserResourcePath())) {
            me.setAttribute("selectedIconPath", this.getSelectedIconPath().substring(FileUtil.getUserResourcePath().length()));
        } else {
            me.setAttribute("selectedIconPath", this.getSelectedIconPath());
        }
        return me;
    }

    /**
     * Check if File exists.
     * @param name File name
     * @return true if exists, else false.
     */
    private boolean checkFile(String name) {
        File fp = new File(name);
        return fp.exists();
    }

    /**
     * Set the preferences based on the XML Element.
     * <ul>
     * <li> identity
     * <li> text
     * <li> isLockable
     * </ul>
     *
     * @param e The Element for this object.
     */
    public void setXml(Element e) {
        try {
            this.setIdentity(e.getAttribute("id").getIntValue());
            this.setText(e.getAttribute("text").getValue());
            this.setIsLockable(e.getAttribute("isLockable").getBooleanValue());
            boolean isVisible = e.getAttribute("isVisible").getBooleanValue();
            this.setDisplay(isVisible);
            if (this.getIdentity() < FunctionPanel.NUM_FUNC_BUTTONS_INIT) {
                this.setVisible(isVisible);
            } else {
                this.setVisible(false);
            }
            this.setFont(new Font("Monospaced", Font.PLAIN, e.getAttribute("fontSize").getIntValue()));
            if ((e.getAttribute("iconPath") != null) && (e.getAttribute("iconPath").getValue().length() > 0)) {
                if (checkFile(FileUtil.getUserResourcePath() + e.getAttribute("iconPath").getValue())) {
                    this.setIconPath(FileUtil.getUserResourcePath() + e.getAttribute("iconPath").getValue());
                } else {
                    this.setIconPath(e.getAttribute("iconPath").getValue());
                }
            }
            if ((e.getAttribute("selectedIconPath") != null) && (e.getAttribute("selectedIconPath").getValue().length() > 0)) {
                if (checkFile(FileUtil.getUserResourcePath() + e.getAttribute("selectedIconPath").getValue())) {
                    this.setSelectedIconPath(FileUtil.getUserResourcePath() + e.getAttribute("selectedIconPath").getValue());
                } else {
                    this.setSelectedIconPath(e.getAttribute("selectedIconPath").getValue());
                }
            }
            updateLnF();
        } catch (org.jdom2.DataConversionException ex) {
            log.error("DataConverstionException in setXml: {}", ex);
        }
    }

    /**
     * Set the Icon Path, NON selected.
     * <p>
     * Checks image and sets isImageOK flag.
     * @param fnImg icon path.
     */
    public void setIconPath(String fnImg) {
        iconPath = fnImg;
        ResizableImagePanel fnImage = new ResizableImagePanel();
        fnImage.setBackground(new Color(0, 0, 0, 0));
        fnImage.setRespectAspectRatio(true);
        fnImage.setSize(new Dimension(FunctionButton.BUT_IMG_SIZE, FunctionButton.BUT_IMG_SIZE));
        fnImage.setImagePath(fnImg);
        if (fnImage.getScaledImage() != null) {
            setIcon(new ImageIcon(fnImage.getScaledImage()));
            isImageOK = true;
        } else {
            setIcon(null);
            isImageOK = false;
        }
    }

    /**
     * Get the Icon Path, NON selected.
     * @return Icon Path, else empty string if null.
     */
    @Nonnull
    public String getIconPath() {
        if (iconPath == null) {
            return "";
        }
        return iconPath;
    }

    /**
     * Set the Selected Icon Path.
     * <p>
     * Checks image and sets isSelectedImageOK flag.
     * @param fnImg selected icon path.
     */
    public void setSelectedIconPath(String fnImg) {
        selectedIconPath = fnImg;
        ResizableImagePanel fnSelectedImage = new ResizableImagePanel();
        fnSelectedImage.setBackground(new Color(0, 0, 0, 0));
        fnSelectedImage.setRespectAspectRatio(true);
        fnSelectedImage.setSize(new Dimension(FunctionButton.BUT_IMG_SIZE, FunctionButton.BUT_IMG_SIZE));
        fnSelectedImage.setImagePath(fnImg);
        if (fnSelectedImage.getScaledImage() != null) {
            ImageIcon icon = new ImageIcon(fnSelectedImage.getScaledImage());
            setSelectedIcon(icon);
            setPressedIcon(icon);
            isSelectedImageOK = true;
        } else {
            setSelectedIcon(null);
            setPressedIcon(null);
            isSelectedImageOK = false;
        }
    }

    /**
     * Get the Selected Icon Path.
     * @return selected Icon Path, else empty string if null.
     */
    @Nonnull
    public String getSelectedIconPath() {
        if (selectedIconPath == null) {
            return "";
        }
        return selectedIconPath;
    }

    /**
     * Get if isImageOK.
     * @return true if isImageOK.
     */
    public boolean isImageOK() {
        return isImageOK;
    }

    /**
     * Get if isSelectedImageOK.
     * @return true if isSelectedImageOK.
     */
    public boolean isSelectedImageOK() {
        return isSelectedImageOK;
    }
    
    /** 
     * Set Throttle.
     * @param throttle the throttle that this button is associated with.
     */
    protected void setThrottle( Throttle throttle) {
        _throttle = throttle;
    }
    
    /**
     * Get Throttle for this button.
     * @return throttle associated with this button.  May be null if no throttle currently associated.
     */
    @CheckForNull
    protected Throttle getThrottle() {
        return _throttle;
    }

    private final static Logger log = LoggerFactory.getLogger(FunctionButton.class);

}
