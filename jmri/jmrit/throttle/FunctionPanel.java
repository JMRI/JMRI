package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
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
            functionButton[i].setIdentity(i);
            functionButton[i].setFunctionListener(this);
            functionButton[i].setText("F"+String.valueOf(i));
            if (i > 0)
            {
                mainPanel.add(functionButton[i]);
            }
        }
        mainPanel.add(functionButton[0]);
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