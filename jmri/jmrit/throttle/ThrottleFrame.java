package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.PrintWriter;


import jmri.DccThrottle;
import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.InstanceManager;

import org.jdom.Element;

/**
 * A JFrame to contain throttle elements such as speed control, address chooser,
 * function panel, and maybe others.
 * <p>
 * This class requests a DccThrottle and calls methods in that object as
 * directed by the interface.
 *
 * @author			Glen Oberhauser
 * @version
 */
public class ThrottleFrame extends JFrame
{
    private final Integer PANEL_LAYER = new Integer(1);

    private ControlPanel controlPanel;
    private FunctionPanel functionPanel;
    private AddressPanel addressPanel;


    /**
     * Default constructor
     */
    public ThrottleFrame()
    {
        initGUI();
    }

    /**
     * Place and initialize the GUI elements.
     * <ul>
     * <li> ControlPanel
     * <li> FunctionPanel
     * <li> AddressPanel
     * </ul>
     */
    private void initGUI()
    {
        setTitle("Throttle");
        JDesktopPane desktop = new JDesktopPane();
        this.setContentPane(desktop);
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e)
            {
                Window w = e.getWindow();
                w.setVisible(false);
                controlPanel.dispose();
                functionPanel.dispose();
                w.dispose();
            }
        });

        controlPanel = new ControlPanel();
        controlPanel.setResizable(true);
        controlPanel.setClosable(true);
        controlPanel.setIconifiable(true);
        controlPanel.setTitle("Control Panel");
        controlPanel.setSize(100,320);
        controlPanel.setVisible(true);
        controlPanel.setEnabled(false);

        functionPanel = new FunctionPanel();
        functionPanel.setResizable(true);
        functionPanel.setClosable(true);
        functionPanel.setIconifiable(true);
        functionPanel.setTitle("Function Panel");
        functionPanel.setSize(200,200);
        functionPanel.setLocation(100, 0);
        functionPanel.setVisible(true);
        functionPanel.setEnabled(false);

        addressPanel = new AddressPanel();
        addressPanel.setResizable(true);
        addressPanel.setClosable(true);
        addressPanel.setIconifiable(true);
        addressPanel.setTitle("Address Panel");
        addressPanel.setSize(200,120);
        addressPanel.setLocation(100, 200);
        addressPanel.setVisible(true);


        addressPanel.addAddressListener(controlPanel);
        addressPanel.addAddressListener(functionPanel);

        desktop.add(controlPanel, PANEL_LAYER);
        desktop.add(functionPanel, PANEL_LAYER);
        desktop.add(addressPanel, PANEL_LAYER);

        desktop.setPreferredSize(new Dimension(300, 340));

        try
        {
            addressPanel.setSelected(true);
        }
        catch (java.beans.PropertyVetoException ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    public Element getXml()
    {
        Element me = new Element("ThrottleFrame");
        com.sun.java.util.collections.ArrayList children =
                new com.sun.java.util.collections.ArrayList(1);
        WindowPreferences wp = new WindowPreferences();

        children.add(wp.getPreferences(this));
        children.add(controlPanel.getXml());
        children.add(functionPanel.getXml());
        children.add(addressPanel.getXml());
        me.setChildren(children);
        return me;
    }

    public void setXml(Element e)
    {
        Element window = e.getChild("window");
        WindowPreferences wp = new WindowPreferences();
        wp.setPreferences(this, window);
        Element controlPanelElement = e.getChild("ControlPanel");
        controlPanel.setXml(controlPanelElement);
        Element functionPanelElement = e.getChild("FunctionPanel");
        functionPanel.setXml(functionPanelElement);
        Element addressPanelElement = e.getChild("AddressPanel");
        addressPanel.setXml(addressPanelElement);
    }

}