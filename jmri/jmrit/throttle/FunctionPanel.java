package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyListener;
import jmri.DccThrottle;

import org.jdom.Element;

/**
 * A JInternalFrame that contains buttons for each decoder function.
 */
public class FunctionPanel extends JInternalFrame implements FunctionListener
{
    public static final int NUM_FUNCTION_BUTTONS = 13;
    private DccThrottle throttle;

    private FunctionButton functionButton[];

    /**
     * Constructor
     */
    public FunctionPanel()
    {
        initGUI();
    }

	public void destroy()
	{
		if (throttle != null)
		{
			throttle.setF0(false);
			throttle.setF1(false);
			throttle.setF2(false);
			throttle.setF3(false);
			throttle.setF4(false);
			throttle.setF5(false);
			throttle.setF6(false);
			throttle.setF7(false);
			throttle.setF8(false);
			throttle.setF9(false);
			throttle.setF10(false);
			throttle.setF11(false);
			throttle.setF12(false);
		}
	}

    /**
     * Get notification that a throttle has been found as we requested.
     * Use reflection to find the proper getF? method for each button.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
    {
        log.debug("Throttle found");
        this.throttle = t;
        for (int i=0; i<this.NUM_FUNCTION_BUTTONS; i++)
        {
           try
           {
                int functionNumber = functionButton[i].getIdentity();
                java.lang.reflect.Method getter =
                        throttle.getClass().getMethod("getF"+functionNumber,null);
                Boolean state = (Boolean)getter.invoke(throttle, null);
                functionButton[i].setState(state.booleanValue());
           }
           catch (java.lang.NoSuchMethodException ex1)
           {
               log.warn("Exception in notifyThrottleFound: "+ex1);
           }
           catch (java.lang.IllegalAccessException ex2)
           {
               log.warn("Exception in notifyThrottleFound: "+ex2);
           }
           catch (java.lang.reflect.InvocationTargetException ex3)
           {
               log.warn("Exception in notifyThrottleFound: "+ex3);
           }
        }
        this.setEnabled(true);
    }

	public void notifyThrottleDisposed()
	{
		this.setEnabled(false);
		throttle = null;
	}

    /**
     * Get notification that a function has changed state
     * @param functionNumber The function that has changed (0-9).
     * @param isSet True if the function is now active (or set).
     */
    public void notifyFunctionStateChanged(int functionNumber, boolean isSet)
    {
        switch (functionNumber)
        {
            case 0: throttle.setF0(isSet); break;
            case 1: throttle.setF1(isSet); break;
            case 2: throttle.setF2(isSet); break;
            case 3: throttle.setF3(isSet); break;
            case 4: throttle.setF4(isSet); break;
            case 5: throttle.setF5(isSet); break;
            case 6: throttle.setF6(isSet); break;
            case 7: throttle.setF7(isSet); break;
            case 8: throttle.setF8(isSet); break;
            case 9: throttle.setF9(isSet); break;
            case 10: throttle.setF10(isSet); break;
            case 11: throttle.setF11(isSet); break;
            case 12: throttle.setF12(isSet); break;
        }
    }

    /**
     * Enable or disable all the buttons
     */
    public void setEnabled(boolean isEnabled)
    {
        //super.setEnabled(isEnabled);
        for (int i=0; i < NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i].setEnabled(isEnabled);
        }
    }


    /**
     * Place and initialize all the buttons.
     */
    private void initGUI()
    {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainPanel.setLayout(new FlowLayout());
        functionButton = new FunctionButton[NUM_FUNCTION_BUTTONS];
        for (int i=0; i<NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i] = new FunctionButton();
            try {   // not present in old JVMs
                functionButton[i].setActionMap(new ActionMap());
            } catch (NoClassDefFoundError ec) {}
            functionButton[i].setIdentity(i);
            functionButton[i].setFunctionListener(this);
            functionButton[i].setText("F"+String.valueOf(i));
            if (i > 0)
            {
                mainPanel.add(functionButton[i]);
            }
        }
        mainPanel.add(functionButton[0]);

		functionButton[0].setKeyCode(KeyEvent.VK_NUMPAD0);
		functionButton[1].setKeyCode(KeyEvent.VK_NUMPAD1);
		functionButton[2].setKeyCode(KeyEvent.VK_NUMPAD2);
		functionButton[3].setKeyCode(KeyEvent.VK_NUMPAD3);
		functionButton[4].setKeyCode(KeyEvent.VK_NUMPAD4);
		functionButton[5].setKeyCode(KeyEvent.VK_NUMPAD5);
		functionButton[6].setKeyCode(KeyEvent.VK_NUMPAD6);
		functionButton[7].setKeyCode(KeyEvent.VK_NUMPAD7);
		functionButton[8].setKeyCode(KeyEvent.VK_NUMPAD8);
		functionButton[9].setKeyCode(KeyEvent.VK_NUMPAD9);
		functionButton[10].setKeyCode(110); // numpad decimal (f10 button causes problems)
		functionButton[11].setKeyCode(KeyEvent.VK_F11);
		functionButton[12].setKeyCode(KeyEvent.VK_F12);
		KeyListenerInstaller.installKeyListenerOnAllComponents(
				new FunctionButtonKeyListener(), this);
    }


	/**
	 *  A KeyAdapter that listens for the keys that work the function buttons
	 *
	 * @author     glen
	 * @created    March 30, 2003
	 */
	class FunctionButtonKeyListener extends KeyAdapter
	{
		private boolean keyReleased = true;

		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void keyPressed(KeyEvent e)
		{
			if (keyReleased)
			{
				System.out.println("Pressed");
				for (int i=0; i<NUM_FUNCTION_BUTTONS; i++)
				{
					if (e.getKeyCode() == functionButton[i].getKeyCode())
					{
						functionButton[i].changeState(!functionButton[i].isSelected());
					}
				}
			}
			keyReleased = false;
		}

		public void keyTyped(KeyEvent e)
		{
			System.out.println("Typed");
		}

		public void keyReleased(KeyEvent e)
		{
			System.out.println("Released");
			for (int i=0; i<NUM_FUNCTION_BUTTONS; i++)
			{
				if (e.getKeyCode() == functionButton[i].getKeyCode())
				{
					if (!functionButton[i].getIsLockable())
					{
						functionButton[i].changeState(!functionButton[i].isSelected());
					}
				}
			}
			keyReleased = true;
		}
	}


    /**
     * Collect the prefs of this object into XML Element
     * <ul>
     * <li> Window prefs
     * <li> Each button has id, text, lock state.
     * </ul>
     * @return the XML of this object.
     */
    public Element getXml()
    {
        Element me = new Element("FunctionPanel");
        Element window = new Element("window");
        WindowPreferences wp = new WindowPreferences();
        com.sun.java.util.collections.ArrayList children =
                new com.sun.java.util.collections.ArrayList(1);
        children.add(wp.getPreferences(this));
        for (int i=0; i<this.NUM_FUNCTION_BUTTONS; i++)
        {
            children.add(functionButton[i].getXml());
        }
        me.setChildren(children);
        return me;
    }

    /**
     * Set the preferences based on the XML Element.
     * <ul>
     * <li> Window prefs
     * <li> Each button has id, text, lock state.
     * </ul>
     * @param e The Element for this object.
     */
    public void setXml(Element e)
    {
        Element window = e.getChild("window");
        WindowPreferences wp = new WindowPreferences();
        wp.setPreferences(this, window);

        com.sun.java.util.collections.List buttonElements =
                e.getChildren("FunctionButton");

        int i = 0;
        for (com.sun.java.util.collections.Iterator iter =
             buttonElements.iterator(); iter.hasNext();)
        {
            Element buttonElement = (Element)iter.next();
            functionButton[i++].setXml(buttonElement);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(FunctionPanel.class.getName());
}