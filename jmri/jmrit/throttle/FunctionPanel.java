package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import jmri.DccThrottle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.InstanceManager;

import org.jdom.Element;

/**
 * A JInternalFrame that contains buttons for each decoder function.
 */
public class FunctionPanel extends JInternalFrame
        implements ThrottleListener, AddressListener, FunctionListener
{
    public static final int NUM_FUNCTION_BUTTONS = 10;
    private final Integer BUTTON_LAYER = new Integer(1);
    private ThrottleManager throttleManager;
    private DccThrottle throttle;
    private int requestedAddress;

    private FunctionButton functionButton[];

    /**
     * Constructor
     */
    public FunctionPanel()
    {
        initGUI();
    }

    /**
     * In addition to super.dispose() this method cancels any requests
     * for throttles.
     */
    public void dispose()
    {
        if (throttleManager != null)
        {
            throttleManager.cancelThrottleRequest(requestedAddress, this);
        }
        super.dispose();
    }

    /**
     * Get notification that a throttle has been found as we requested.
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
    {
        this.throttle = t;
        functionButton[0].setState(throttle.getF0());
        functionButton[1].setState(throttle.getF1());
        functionButton[2].setState(throttle.getF2());
        functionButton[3].setState(throttle.getF3());
        functionButton[4].setState(throttle.getF4());
        functionButton[5].setState(throttle.getF5());
        functionButton[6].setState(throttle.getF6());
        functionButton[7].setState(throttle.getF7());
        functionButton[8].setState(throttle.getF8());
        functionButton[9].setState(false); // No F9?
        this.setEnabled(true);
    }

    /**
     * Get notification that the decoder address value has changed.
     * @param newAddress The new address.
     */
    public void notifyAddressChanged(int oldAddress, int newAddress)
    {
        if (throttleManager == null)
        {
            throttleManager = InstanceManager.throttleManagerInstance();
        }
        throttleManager.cancelThrottleRequest(oldAddress, this);
        throttleManager.requestThrottle(newAddress, this);
        requestedAddress = newAddress;
        this.setEnabled(false);
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
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainPanel.setLayout(new GridBagLayout());
        functionButton = new FunctionButton[NUM_FUNCTION_BUTTONS];
        for (int i=0; i<NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i] = new FunctionButton(i, false);
            functionButton[i].setFunctionListener(this);
            functionButton[i].setText("F"+String.valueOf(i));
        }
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(0, 0, 0, 0);
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;

        int i = 1;
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 3; col++)
            {
                constraints.gridx = col;
                constraints.gridy = row;
                mainPanel.add(functionButton[i++], constraints);
            }
        }
        constraints.gridx = 1;
        constraints.gridy = 3;
        mainPanel.add(functionButton[0], constraints);

/**
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        JMenu viewMenu = new JMenu("View");
        JMenuItem arrangeItem = new JMenuItem(new Action(("Arrange Buttons")));
        menuBar.add(viewMenu);
        viewMenu.add(arrangeItem);
 **/
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
            Element buttonElement = new Element("FunctionButton");
            buttonElement.addAttribute("id", String.valueOf(i));
            buttonElement.addAttribute("text", functionButton[i].getText());
            buttonElement.addAttribute("isLockable",
                                       String.valueOf(functionButton[i].getIsLockable()));
            children.add(buttonElement);
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
        try
        {
            Element window = e.getChild("window");
            WindowPreferences wp = new WindowPreferences();
            wp.setPreferences(this, window);

            com.sun.java.util.collections.List buttonElements =
                    e.getChildren("FunctionButton");

            for (com.sun.java.util.collections.Iterator iter =
             buttonElements.iterator(); iter.hasNext();)
            {
                Element buttonElement = (Element)iter.next();
                int id = buttonElement.getAttribute("id").getIntValue();
                String text = buttonElement.getAttribute("text").getValue();
                functionButton[id].setText(text);
                boolean isLockable = buttonElement.getAttribute("isLockable").getBooleanValue();
                functionButton[id].setIsLockable(isLockable);
            }

        }
        catch (org.jdom.DataConversionException ex)
        {
            System.out.println("Ugh");
        }
    }

}