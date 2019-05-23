package jmri.jmrit.throttle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import jmri.util.FileUtil;
import jmri.util.swing.ResizableImagePanel;
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
public class FunctionButton extends JToggleButton implements ActionListener {

    private ArrayList<FunctionListener> listeners = new ArrayList<FunctionListener>();
    private int identity; // F0, F1, etc?
    private boolean isOn;
    private boolean isLockable = true;
    private boolean isDisplayed = true;
    private boolean dirty = false;
    private boolean isImageOK = false;
    private boolean isSelectedImageOK = false;
    private int actionKey;
    private String buttonLabel;
    private JPopupMenu popup;
    private String iconPath;
    private String selectedIconPath;

    static int BUT_HGHT = 30;
    static int BUT_WDTH = 56;
    final static int BUT_IMG_SIZE = 45;

    static {
        JButton sample = new JButton(" Light ");
        BUT_HGHT = java.lang.Math.max(sample.getPreferredSize().height, BUT_HGHT);
        BUT_WDTH = java.lang.Math.max(sample.getPreferredSize().width, BUT_WDTH);
    }

    public static int getButtonHeight() {
        return BUT_HGHT;
    }

    public static int getButtonWidth() {
        return BUT_WDTH;
    }

    /**
     * Construct the FunctionButton.
     */
    public FunctionButton() {
        popup = new JPopupMenu();

        JMenuItem propertiesItem = new JMenuItem(Bundle.getMessage("MenuItemProperties"));
        propertiesItem.addActionListener(this);
        popup.add(propertiesItem);

        //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();
        addMouseListener(popupListener);
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        setMargin(new Insets(2, 2, 2, 2));
        setRolloverEnabled(false);
        updateLnF();
    }

    /**
     * Set the function number this button will operate
     *
     * @param id An integer from 0 to 28.
     */
    public void setIdentity(int id) {
        this.identity = id;
    }

    /**
     * Get the function number this button operates
     *
     * @return An integer from 0 to 28.
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
     */
    public void setKeyCode(int key) {
        actionKey = key;
    }

    /**
     * Check to see whether a particular KeyCode corresponds to this function
     * button.
     *
     * @return true if the button should respond to this key
     */
    public boolean checkKeyCode(int keycode) {
        return keycode == actionKey;
    }

    /**
     * Set the state of the function button. Does NOT notify any listeners
     *
     * @param isOn True if the function should be active.
     */
    public void setState(boolean isOn) {
        this.isOn = isOn;
        this.setSelected(isOn);
    }

    /**
     * get the state of the function
     *
     * @return true if the function is active.
     */
    public boolean getState() {
        return isOn;
    }

    /**
     * Set the locking state of the button
     *
     * @param isLockable True if the a clicking and releasing the button changes
     *                   the function state. False if the state is changed back
     *                   when the button is released
     */
    public void setIsLockable(boolean isLockable) {
        this.isLockable = isLockable;
        if (isDirty()) {
            // Changes in this parameter should only be sent to the 
            // listeners if the dirty bit is set.
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).notifyFunctionLockableChanged(identity, isLockable);
            }
        }
    }

    /**
     * Get the locking state of the function
     *
     * @return True if the a clicking and releasing the button changes the
     *         function state. False if the state is changed back when the
     *         button is released
     */
    public boolean getIsLockable() {
        return isLockable;
    }

    /**
     * Set the display state of the button
     *
     * @param displayed True if the button exists False if the button has been
     *                  removed by the user
     */
    public void setDisplay(boolean displayed) {
        this.isDisplayed = displayed;
    }

    /**
     * Get the display state of the button
     *
     * @return True if the button exists False if the button has been removed by
     *         the user
     */
    public boolean getDisplay() {
        return isDisplayed;
    }

    /**
     * True when function button has been modified by user.
     *
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     *
     * @return true when function button has been modified by user.
     */
    public boolean isDirty() {
        return dirty;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public void setButtonLabel(String label) {
        buttonLabel = label;
    }

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
            return;
        }
    }

    /**
     * Decide if it should show the label or an image with text as tooltip
     * Button UI updated according to above result.
     */
    public void updateLnF() {
        if (isImageOK()) { // adjust button for image
            setBorderPainted(false);
            setContentAreaFilled(false);
            setText(null);
            setPreferredSize(new Dimension(FunctionButton.BUT_IMG_SIZE, FunctionButton.BUT_IMG_SIZE));
        } else // adjust button for text
        {
            setBorderPainted(true);
            setContentAreaFilled(true);
            setText(getButtonLabel());
            if (getButtonLabel() != null) {
                int butWidth = getFontMetrics(getFont()).stringWidth(getButtonLabel());
                butWidth = butWidth + 20; // pad out the width a bit
                if (butWidth < FunctionButton.BUT_WDTH) {
                    butWidth = FunctionButton.BUT_WDTH;
                }
                setPreferredSize(new Dimension(butWidth, FunctionButton.BUT_HGHT));
            } else {
                setPreferredSize(new Dimension(BUT_WDTH, BUT_HGHT));
            }
        }
    }

    /**
     * Handle the selection from the popup menu.
     *
     * @param e The ActionEvent causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        FunctionButtonPropertyEditor editor = new FunctionButtonPropertyEditor();
        editor.setFunctionButton(this);
        editor.setLocation(this.getLocationOnScreen());
        editor.setVisible(true);
    }

    /**
     * Change the state of the function.
     *
     * @param newState The new state. True = Is on, False = Is off.
     */
    public void changeState(boolean newState) {
        if (log.isDebugEnabled()) {
            log.debug("Change state to " + newState);
        }
        isOn = newState;
        this.setSelected(isOn);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).notifyFunctionStateChanged(identity, isOn);
        }
    }

    /**
     * Add a listener to this button, probably some sort of keypad panel.
     *
     * @param l The FunctionListener that wants notifications via the
     *          FunctionListener.notifyFunctionStateChanged.
     */
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
     * A PopupListener to handle mouse clicks and releases. Handles the popup
     * menu.
     */
    class PopupListener extends MouseAdapter {

        /**
         * If the event is the popup trigger, which is dependent on the
         * platform, present the popup menu. Otherwise change the state of the
         * function depending on the locking state of the button.
         *
         * @param e The MouseEvent causing the action.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("pressed " + (e.getModifiers() & MouseEvent.BUTTON1_MASK) + " " + e.isPopupTrigger()
                        + " " + (e.getModifiers() & (MouseEvent.ALT_MASK + MouseEvent.META_MASK + MouseEvent.CTRL_MASK))
                        + (" " + MouseEvent.ALT_MASK + "/" + MouseEvent.META_MASK + "/" + MouseEvent.CTRL_MASK));
            }
            JToggleButton button = (JToggleButton) e.getSource();
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                        e.getX(), e.getY());
            } /* Must check button mask since some platforms wait
             for mouse release to do popup. */ else if (button.isEnabled()
                    && ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
                    && ((e.getModifiers() & (MouseEvent.ALT_MASK + MouseEvent.META_MASK + MouseEvent.CTRL_MASK)) == 0)
                    && !isLockable) {
                changeState(true);
            }
            // force button to desired state; click might have changed it
            button.setSelected(isOn);
        }

        /**
         * If the event is the popup trigger, which is dependent on the
         * platform, present the popup menu. Otherwise change the state of the
         * function depending on the locking state of the button.
         *
         * @param e The MouseEvent causing the action.
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("released " + (e.getModifiers() & MouseEvent.BUTTON1_MASK) + " " + e.isPopupTrigger()
                        + " " + (e.getModifiers() & (MouseEvent.ALT_MASK + MouseEvent.META_MASK + MouseEvent.CTRL_MASK)));
            }
            JToggleButton button = (JToggleButton) e.getSource();
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                        e.getX(), e.getY());
            } // mouse events have to be unmodified; to change function, so that
            // we don't act on 1/2 of a popup request.
            else if (button.isEnabled()
                    && ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
                    && ((e.getModifiers() & (MouseEvent.ALT_MASK + MouseEvent.META_MASK + MouseEvent.CTRL_MASK)) == 0)) {
                if (!isLockable) {
                    changeState(false);
                } else {
                    changeState(!isOn);
                }
            }
            // force button to desired state
            button.setSelected(isOn);
        }
    }

    /**
     * Collect the prefs of this object into XML Element
     * <ul>
     * <li> identity
     * <li> text
     * <li> isLockable
     * </ul>
     *
     * @return the XML of this object.
     */
    public Element getXml() {
        Element me = new Element("FunctionButton");
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

    private boolean checkFile(String name) {
        File fp = new File(name);
        if (fp.exists()) {
            return true;
        }
        return false;
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
            boolean isLockable = e.getAttribute("isLockable").getBooleanValue();
            this.setIsLockable(isLockable);
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
            log.error("DataConverstionException in setXml: " + ex);
        }
    }

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

    public String getIconPath() {
        if (iconPath == null) {
            return "";
        }
        return iconPath;
    }

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

    public String getSelectedIconPath() {
        if (selectedIconPath == null) {
            return "";
        }
        return selectedIconPath;
    }

    public boolean isImageOK() {
        return isImageOK;
    }

    public boolean isSelectedImageOK() {
        return isSelectedImageOK;
    }

    private final static Logger log = LoggerFactory.getLogger(FunctionButton.class);

}
