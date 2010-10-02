package jmri.jmrit.throttle;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import org.jdom.Element;


/**
 * A JButton to activate functions on the decoder. FunctionButtons
 * have a right-click popup menu with several configuration options:
 * <ul>
 * <li> Set the text
 * <li> Set the locking state
 * <li> Set visibilty
 * <li> Set Font
 * <li> Set function number identity
 * </ul>
 *
 * @author Glen Oberhauser
 * @author Bob Jacobsen   Copyright 2008
 */
public class FunctionButton extends JToggleButton implements ActionListener
{
    static final ResourceBundle rb = ThrottleBundle.bundle();
    private ArrayList<FunctionListener> listeners = new ArrayList<FunctionListener>();
    private int identity; // F0, F1, etc?
    private boolean isOn;
    private boolean isLockable = true;
    private boolean isDisplayed = true;
    private boolean dirty = false;
	private int actionKey;
	
	// the following two are directly accessed from jmri.jmrit.logix.LearnThrottleFrame,
	// which needs to be fixed
	static public int BUT_HGHT = 30;
	static public int BUT_WDTH = 56;

    static {
        JButton sample = new JButton(" Light ");
        BUT_HGHT = java.lang.Math.max(sample.getPreferredSize().height, BUT_HGHT);
        BUT_WDTH = java.lang.Math.max(sample.getPreferredSize().width, BUT_WDTH);
    }
    
    private JPopupMenu popup;
    /**
     * Construct the FunctionButton.
     */
    public FunctionButton()
    {
        popup = new JPopupMenu();

        JMenuItem propertiesItem = new JMenuItem(rb.getString("MenuItemProperties"));
        propertiesItem.addActionListener(this);
        popup.add(propertiesItem);

        //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();
        addMouseListener(popupListener);
        setFont(new Font("Monospaced",Font.PLAIN, 12));
        setPreferredSize(new Dimension(BUT_WDTH,BUT_HGHT));
        setMargin(new Insets(2,2,2,2));
    }
    
    public void setEnabled(boolean b) {
    	super.setEnabled(b);
    }


    /**
     * Set the function number this button will operate
     * @param id An integer from 0 to 28.
     */
    public void setIdentity(int id)
    {
        this.identity = id;
    }

    /**
     * Get the function number this button operates
     * @return An integer from 0 to 28.
     */
    public int getIdentity()
    {
        return identity;
    }

    /**
     * Set the keycode that this button should respond to.
     * <P>
     * Later, when a key is being processed, checkKeyCode
     * will determine if there's a match between the key that
     * was pressed and the key for this button
     */
	public void setKeyCode(int key)
	{
		actionKey = key;
	}


    /**
     * Check to see whether a particular KeyCode corresponds
     * to this function button.  
     * @return true if the button should respond to this key
     */
	public boolean checkKeyCode(int keycode)
	{
		return keycode == actionKey;
	}

    /**
     * Set the state of the function button. Does NOT 
     * notify any listeners
     * @param isOn True if the function should be active.
     */
    public void setState(boolean isOn)
    {
        this.isOn = isOn;
		this.setSelected(isOn);
    }

    /**
     * get the state of the function
     * @return true if the function is active.
     */
    public boolean getState()
    {
        return isOn;
    }

    /**
     * Set the locking state of the button
     * @param isLockable True if the a clicking and releasing the button
     * changes the function state. False if the state is changed
     * back when the button is released
     */
    public void setIsLockable(boolean isLockable)
    {
        this.isLockable = isLockable;
        if(isDirty()) {
            // Changes in this parameter should only be sent to the 
            // listeners if the dirty bit is set.
		for (int i=0; i<listeners.size();i++)
            listeners.get(i).notifyFunctionLockableChanged(identity, isLockable);
        }
    }

    /**
     * Get the locking state of the function
     * @return True if the a clicking and releasing the button
     * changes the function state. False if the state is changed
     * back when the button is released
     */
    public boolean getIsLockable()
    {
        return isLockable;
    }
    
    /**
     * Set the display state of the button
     * @param displayed True if the button exists 
     * False if the button has been removed by the user
     */
    public void setDisplay(boolean displayed)
    {
        this.isDisplayed = displayed;
    }

    /**
     * Get the display state of the button
     * @return True if the button exists  
     * False if the button has been removed by the user
     */
    public boolean getDisplay()
    {
        return isDisplayed;
    }
    
    /**
     * True when function button has been modified by user.
     *
     */
    public void setDirty(boolean dirty){
    	this.dirty = dirty;
    }
    
    /**
     * 
     * @return true when function button has been modified by user.
     */
    public boolean isDirty(){
    	return dirty;
    }

    /**
     * Handle the selection from the popup menu.
     * @param e The ActionEvent causing the action.
     */
    public void actionPerformed(ActionEvent e)
    {
        FunctionButtonPropertyEditor editor =
                ThrottleFrameManager.instance().getFunctionButtonEditor();
        editor.setFunctionButton(this);
        editor.setLocation(this.getLocationOnScreen());
        editor.setVisible(true);
    }

    /**
     * Change the state of the function.
     * @param newState The new state. True = Is on, False = Is off.
     */
    public void changeState(boolean newState)
    {
        if (log.isDebugEnabled()) {
        	log.debug("Change state to "+newState);
//        	new Exception().printStackTrace();
        }
        isOn = newState;
		this.setSelected(isOn);
		for (int i=0; i<listeners.size();i++)
            listeners.get(i).notifyFunctionStateChanged(identity, isOn);       
    }


    /**
     * Add a listener to this button, probably some sort of keypad panel.
     * @param l The FunctionListener that wants notifications via the
     * FunctionListener.notifyFunctionStateChanged.
     */
    public void setFunctionListener(FunctionListener l) {
        addFunctionListener(l);
    }
    
    /**
     * Add a listener to this button, probably some sort of keypad panel.
     * @param l The FunctionListener that wants notifications via the
     * FunctionListener.notifyFunctionStateChanged.
     */
    public void addFunctionListener(FunctionListener l) {
		if (!listeners.contains(l))
			listeners.add(l);
    }
    
    /**
     * Remove a listener from this button.
     * @param l The FunctionListener to be removed
     */
    public void removeFunctionListener(FunctionListener l) {
    	if (listeners.contains(l))
    		listeners.remove(l);
    }
    /**
     * A PopupListener to handle mouse clicks and releases. Handles
     * the popup menu.
     */
    class PopupListener extends MouseAdapter
    {
        /**
         * If the event is the popup trigger, which is dependent on
         * the platform, present the popup menu. Otherwise change
         * the state of the function depending on the locking state
         * of the button.
         * @param e The MouseEvent causing the action.
         */
        public void mousePressed(MouseEvent e)
        {
            if (log.isDebugEnabled()) log.debug("pressed "+(e.getModifiers() & MouseEvent.BUTTON1_MASK)+" "+e.isPopupTrigger()
                    +" "+(e.getModifiers() & (MouseEvent.ALT_MASK+MouseEvent.META_MASK+MouseEvent.CTRL_MASK))
                    +(" "+MouseEvent.ALT_MASK+"/"+MouseEvent.META_MASK+"/"+MouseEvent.CTRL_MASK));
            JToggleButton button = (JToggleButton)e.getSource();
            if (e.isPopupTrigger())
            {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
            /* Must check button mask since some platforms wait
            for mouse release to do popup. */
            else if (button.isEnabled()
                     && ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
                    && ((e.getModifiers() & (MouseEvent.ALT_MASK+MouseEvent.META_MASK+MouseEvent.CTRL_MASK)) == 0)
                     && !isLockable)
            {
                changeState(true);
            }
            // force button to desired state; click might have changed it
            button.setSelected(isOn);
        }

        /**
         * If the event is the popup trigger, which is dependent on
         * the platform, present the popup menu. Otherwise change
         * the state of the function depending on the locking state
         * of the button.
         * @param e The MouseEvent causing the action.
         */
        public void mouseReleased(MouseEvent e)
        {
            if (log.isDebugEnabled()) log.debug("released "+(e.getModifiers() & MouseEvent.BUTTON1_MASK)+" "+e.isPopupTrigger()
                    +" "+(e.getModifiers() & (MouseEvent.ALT_MASK+MouseEvent.META_MASK+MouseEvent.CTRL_MASK)));
            JToggleButton button = (JToggleButton)e.getSource();
            if (e.isPopupTrigger())
            {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
            // mouse events have to be unmodified; to change function, so that
            // we don't act on 1/2 of a popup request.
            else if (button.isEnabled()
                    && ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
                    && ((e.getModifiers() & (MouseEvent.ALT_MASK+MouseEvent.META_MASK+MouseEvent.CTRL_MASK)) == 0) )
            {
                if (!isLockable)
                {
                    changeState(false);
                }
                else
                {
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
     * @return the XML of this object.
     */
    public Element getXml()
    {
        Element me = new Element("FunctionButton");
        me.setAttribute("id", String.valueOf(this.getIdentity()));
        me.setAttribute("text", this.getText());
        me.setAttribute("isLockable", String.valueOf(this.getIsLockable()));
        me.setAttribute("isVisible", String.valueOf(this.getDisplay()));
        me.setAttribute("fontSize", String.valueOf(this.getFont().getSize()));
        return me;
    }

    /**
     * Set the preferences based on the XML Element.
     * <ul>
     * <li> identity
     * <li> text
     * <li> isLockable
      * </ul>
     * @param e The Element for this object.
     */
    public void setXml(Element e)
    {
        try
        {
            this.setIdentity(e.getAttribute("id").getIntValue());
            this.setText(e.getAttribute("text").getValue());
            boolean isLockable = e.getAttribute("isLockable").getBooleanValue();
            this.setIsLockable(isLockable);
            boolean isVisible = e.getAttribute("isVisible").getBooleanValue();
            this.setDisplay(isVisible);
            if (this.getIdentity() < FunctionPanel.NUM_FUNC_BUTTONS_INIT)
            	this.setVisible(isVisible);
            else
            	this.setVisible(false);
            this.setFont(new Font("Monospaced", Font.PLAIN, e.getAttribute("fontSize").getIntValue()));
            int butWidth = this.getFontMetrics(this.getFont()).stringWidth(this.getText());
            butWidth = butWidth + 20;	// pad out the width a bit
            if (butWidth < BUT_WDTH) butWidth = BUT_WDTH;
            this.setPreferredSize(new Dimension(butWidth,BUT_HGHT));

        }
        catch (org.jdom.DataConversionException ex)
        {
            log.error("DataConverstionException in setXml: "+ex);
        }
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FunctionButton.class.getName());
}
