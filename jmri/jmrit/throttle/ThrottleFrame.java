package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import javax.swing.*;
import javax.swing.event.*;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;
import jmri.ThrottleManager;

import org.jdom.Element;

/**
 *  A JFrame to contain throttle elements such as speed control, address
 *  chooser, function panel, and maybe others. <p>
 *
 *  This class requests a DccThrottle and calls methods in that object as
 *  directed by the interface.
 *
 * @author     Glen Oberhauser
 * @created    March 25, 2003
 * @version
 */
public class ThrottleFrame extends JFrame 
{
	private final Integer PANEL_LAYER = new Integer(1);

	private ControlPanel controlPanel;
	private FunctionPanel functionPanel;
	private AddressPanel addressPanel;

	private JCheckBoxMenuItem viewControlPanel;
	private JCheckBoxMenuItem viewFunctionPanel;
	private JCheckBoxMenuItem viewAddressPanel;

	/**
	 *  Default constructor
	 */
	public ThrottleFrame()
	{
		initGUI();
	}

	/**
	 *  Place and initialize the GUI elements.
	 *  <ul>
	 *    <li> ControlPanel
	 *    <li> FunctionPanel
	 *    <li> AddressPanel
	 *    <li> JMenu
	 *  </ul>
	 *
	 */
	private void initGUI()
	{
		setTitle("Throttle");
		JDesktopPane desktop = new JDesktopPane();
		this.setContentPane(desktop);
		this.addWindowListener(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					destroyThrottleFrame();
					e.getWindow().dispose();
				}
			});

		initializeMenu();
			
		FrameListener frameListener = new FrameListener();
		FrameCyclingKeyListener frameCycler = new FrameCyclingKeyListener();

		controlPanel = new ControlPanel();
		controlPanel.setResizable(true);
		controlPanel.setClosable(true);
		controlPanel.setIconifiable(true);
		controlPanel.setTitle("Control Panel");
		controlPanel.setSize(100, 320);
		controlPanel.setVisible(true);
		controlPanel.setEnabled(false);
		controlPanel.addInternalFrameListener(frameListener);

		functionPanel = new FunctionPanel();
		functionPanel.setResizable(true);
		functionPanel.setClosable(true);
		functionPanel.setIconifiable(true);
		functionPanel.setTitle("Function Panel");
		functionPanel.setSize(200, 200);
		functionPanel.setLocation(100, 0);
		functionPanel.setVisible(true);
		functionPanel.setEnabled(false);
		functionPanel.addInternalFrameListener(frameListener);
		functionPanel.addExternalKeyListener(frameCycler);

		addressPanel = new AddressPanel();
		addressPanel.setResizable(true);
		addressPanel.setClosable(true);
		addressPanel.setIconifiable(true);
		addressPanel.setTitle("Address Panel");
		addressPanel.addInternalFrameListener(frameListener);

		addressPanel.setSize(200, 120);
		addressPanel.setLocation(100, 200);
		addressPanel.setVisible(true);

		/*
		 *    Make controlPanel and functionPanel listen to addressPanel
		 *    for address changes.
		 */
		addressPanel.addAddressListener(controlPanel);
		addressPanel.addAddressListener(functionPanel);
		addressPanel.addAddressListener(addressPanel);

		desktop.add(controlPanel, PANEL_LAYER);
		desktop.add(functionPanel, PANEL_LAYER);
		desktop.add(addressPanel, PANEL_LAYER);

		desktop.setPreferredSize(new Dimension(300, 340));
		
		this.addKeyListener(frameCycler);

		try
		{
			addressPanel.setSelected(true);
		}
		catch (java.beans.PropertyVetoException ex)
		{
			System.out.println(ex.getMessage());
		}

	}

	
	
	private void initializeMenu()
	{
		JMenu viewMenu = new JMenu("View");
		viewAddressPanel = new JCheckBoxMenuItem("Address Panel");
		viewAddressPanel.setSelected(true);
		viewAddressPanel.addItemListener(
			new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					addressPanel.setVisible(e.getStateChange() == e.SELECTED);
				}
			});

		viewControlPanel = new JCheckBoxMenuItem("Control Panel");
		viewControlPanel.setSelected(true);
		viewControlPanel.addItemListener(
			new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					controlPanel.setVisible(e.getStateChange() == e.SELECTED);
				}
			});
		viewFunctionPanel = new JCheckBoxMenuItem("Function Panel");
		viewFunctionPanel.setSelected(true);
		viewFunctionPanel.addItemListener(
			new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					functionPanel.setVisible(e.getStateChange() == e.SELECTED);
				}
			});

		viewMenu.add(viewAddressPanel);
		viewMenu.add(viewControlPanel);
		viewMenu.add(viewFunctionPanel);
		
		JMenu editMenu = new JMenu("Edit");
		
		this.setJMenuBar(new JMenuBar());
		this.getJMenuBar().add(viewMenu);
		this.getJMenuBar().add(editMenu);
	}
	
	
	/**
	 *  Handle my own destruction.
	 * <ol><li> dispose of sub windows.
	 * <li> notify my manager of my demise.
	 * </ol>
	 */
	public void destroyThrottleFrame()
	{
		controlPanel.dispose();
		functionPanel.dispose();
		addressPanel.dispose();
		ThrottleFrameManager manager = InstanceManager.throttleFrameManagerInstance();
		manager.notifyDestroyThrottleFrame(this);
	}


	class FrameCyclingKeyListener extends KeyAdapter
	{
		public void keyTyped(KeyEvent e)
		{
			System.out.println("Typed:"+e.getKeyChar());
		}
	}
	
	
	/**
	 *  An extension of InternalFrameAdapter for listening to the closing of
	 * of this frame's internal frames.
	 *
	 * @author     glen
	 * @created    March 25, 2003
	 */
	class FrameListener extends InternalFrameAdapter
	{
		/**
		 *  Listen for the closing of an internal frame and set the "View"
		 * menu appropriately. Then hide the closing frame
		 *
		 * @param  e  The InternalFrameEvent leading to this action
		 */
		public void internalFrameClosing(InternalFrameEvent e)
		{
			if (e.getSource() == controlPanel)
			{
				viewControlPanel.setSelected(false);
				controlPanel.setVisible(false);
			}
			else if (e.getSource() == addressPanel)
			{
				viewAddressPanel.setSelected(false);
				addressPanel.setVisible(false);
			}
			else if (e.getSource() == functionPanel)
			{
				viewFunctionPanel.setSelected(false);
				functionPanel.setVisible(false);
			}
		}
	}


	/**
	 *  Collect the prefs of this object into XML Element
	 *  <ul>
	 *    <li> Window prefs
	 *    <li> ControlPanel
	 *    <li> FunctionPanel
	 *    <li> AddressPanel
	 *  </ul>
	 *
	 *
	 * @return    the XML of this object.
	 */
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

	/**
	 *  Set the preferences based on the XML Element.
	 *  <ul>
	 *    <li> Window prefs
	 *    <li> ControlPanel
	 *    <li> FunctionPanel
	 *    <li> AddressPanel
	 *  </ul>
	 *
	 *
	 * @param  e  The Element for this object.
	 */
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
