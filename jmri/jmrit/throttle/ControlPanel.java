package jmri.jmrit.throttle;

import jmri.DccThrottle;
import jmri.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;

/**
 *  A JInternalFrame that contains a JSlider to control loco speed, and buttons
 *  for forward, reverse and STOP.
 *  <P>
 *  TODO: fix speed increments (14, 28)
 *
 * @author     glen
 * @version    $Revision: 1.33 $
 */
public class ControlPanel extends JInternalFrame implements java.beans.PropertyChangeListener
{
	private DccThrottle throttle;

	private JSlider speedSlider;
	private GridBagConstraints sliderConstraints;
	private JRadioButton forwardButton, reverseButton;
	private JButton stopButton;
	private JButton idleButton;
	private JPanel buttonPanel;
	private int speedIncrement;
        private boolean internalAdjust = false;

	public int accelerateKey = 107; // numpad +;
	public int decelerateKey = 109; // numpad -;
        public int accelerateKey1 = KeyEvent.VK_LEFT ; // Left Arrow
        public int decelerateKey1 = KeyEvent.VK_RIGHT ; // Left Arrow
        public int accelerateKey2 = KeyEvent.VK_PAGE_UP ; // Left Arrow
        public int decelerateKey2 = KeyEvent.VK_PAGE_DOWN ; // Left Arrow
	public int reverseKey = KeyEvent.VK_DOWN;
	public int forwardKey = KeyEvent.VK_UP;
	public int stopKey = 111; // numpad /
	public int idleKey = 106; // numpad *

          // LocoNet really only has 126 speed steps i.e. 0..127 - 1 for em stop
	private static int MAX_SPEED = 126;
	//TODO: correct always?

	/**
	 *  Constructor.
	 */
	public ControlPanel()
	{
		speedSlider = new JSlider(0, MAX_SPEED);
		speedSlider.setValue(0);
		SwingUtil.setFocusable(speedSlider,false);
		forwardButton = new JRadioButton("Forward");
		reverseButton = new JRadioButton("Reverse");
		initGUI();
	}

	public void notifyThrottleDisposed()
	{
		this.setEnabled(false);
                this.throttle.removePropertyChangeListener(this);
		throttle = null;
	}

	public void destroy()
	{
		if (throttle != null)
		{
			throttle.setSpeedSetting(0);
		}
	}

	/**
	 *  Get notification that a throttle has been found as we requested.
	 *
	 * @param  t  An instantiation of the DccThrottle with the address requested.
	 */
	public void notifyThrottleFound(DccThrottle t)
	{
		this.throttle = t;
		this.setEnabled(true);
		this.setIsForward(throttle.getIsForward());
		this.setSpeedValues((int) throttle.getSpeedIncrement(),
				(int) throttle.getSpeedSetting());
                this.throttle.addPropertyChangeListener(this);
	}

	/**
	 *  Enable/Disable all buttons and slider.
	 *
	 * @param  isEnabled  True if the buttons/slider should be enabled, false
	 *      otherwise.
	 */
	public void setEnabled(boolean isEnabled)
	{
		//super.setEnabled(isEnabled);
		forwardButton.setEnabled(isEnabled);
		reverseButton.setEnabled(isEnabled);
		stopButton.setEnabled(isEnabled);
		idleButton.setEnabled(isEnabled);
		speedSlider.setEnabled(isEnabled);
	}

	/**
	 *  Set the GUI to match that the loco is set to forward.
	 *
	 * @param  isForward  True if the loco is set to forward, false otherwise.
	 */
	public void setIsForward(boolean isForward)
	{
		forwardButton.setSelected(isForward);
		reverseButton.setSelected(!isForward);
	}

	/**
	 *  Set the GUI to match that the loco speed.
	 *
	 * @param  speedIncrement  : TODO
	 * @param  speed           The speed value of the loco.
	 */
	public void setSpeedValues(int speedIncrement, int speed)
	{
		this.speedIncrement = speedIncrement;
		speedSlider.setValue(speed * speedIncrement);
	}

	/**
	 *  Create, initialize and place GUI components.
	 */
	private void initGUI()
	{
		JPanel mainPanel = new JPanel();
		this.setContentPane(mainPanel);
		mainPanel.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.ipadx = 0;
		constraints.ipady = 0;
		Insets insets = new Insets(2, 2, 2, 2);
		constraints.insets = insets;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		sliderPanel.add(speedSlider, constraints);
		this.getContentPane().add(sliderPanel, BorderLayout.CENTER);
		speedSlider.setOrientation(JSlider.VERTICAL);
		speedSlider.setMajorTickSpacing(MAX_SPEED/2);
		com.sun.java.util.collections.Hashtable labelTable = new com.sun.java.util.collections.Hashtable();
		labelTable.put(new Integer(MAX_SPEED/2), new JLabel("50%"));
		labelTable.put(new Integer(MAX_SPEED), new JLabel("100%"));
                labelTable.put(new Integer(0), new JLabel("Stop"));
		speedSlider.setLabelTable(labelTable);
		speedSlider.setPaintTicks(true);
		speedSlider.setPaintLabels(true);
		// remove old actions
		speedSlider.addChangeListener(
			new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
                                    if ( !internalAdjust) {
					if (!speedSlider.getValueIsAdjusting())
					{
                                                float newSpeed = (speedSlider.getValue() / ( MAX_SPEED * 1.0f ) ) ;
                                                log.debug( "stateChanged: slider pos: " + speedSlider.getValue() + " speed: " + newSpeed );
						throttle.setSpeedSetting( newSpeed );
					}
				   } else {
					internalAdjust=false;
				   }
				}
			});

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		ButtonGroup directionButtons = new ButtonGroup();
		directionButtons.add(forwardButton);
		directionButtons.add(reverseButton);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridy = 1;
		buttonPanel.add(forwardButton, constraints);
		constraints.gridy = 2;
		buttonPanel.add(reverseButton, constraints);

		forwardButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					throttle.setIsForward(true);
				}
			});

		reverseButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					throttle.setIsForward(false);
				}
			});

		stopButton = new JButton("STOP!");
		constraints.gridy = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		buttonPanel.add(stopButton, constraints);
		stopButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					stop();
				}
			});

		stopButton.addMouseListener(
			new MouseListener()
			{
				public void mousePressed(MouseEvent e)
				{
					stop();
				}
                                public void mouseExited(MouseEvent e) {}
                                public void mouseEntered(MouseEvent e) {}
                                public void mouseReleased(MouseEvent e) {}
                                public void mouseClicked(MouseEvent e) {}
			});

		idleButton = new JButton("Idle");
		constraints.gridy = 4;
		buttonPanel.add(idleButton, constraints);
		idleButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					speedSlider.setValue(0);
					throttle.setSpeedSetting(0);
				}
			});

		this.addComponentListener(
			new ComponentAdapter()
			{
				public void componentResized(ComponentEvent e)
				{
					changeOrientation();
				}
			});

		KeyListenerInstaller.installKeyListenerOnAllComponents(
				new ControlPadKeyListener(), this);

	}

	/**
	 *  Perform an emergency stop
	 */
	private void stop()
	{
		speedSlider.setValue(0);
		throttle.setSpeedSetting(-1);
	}

	/**
	 *  The user has resized the Frame. Possibly change from Horizontal to Vertical
	 *  layout.
	 */
	private void changeOrientation()
	{
		if (this.getWidth() > this.getHeight())
		{
			speedSlider.setOrientation(JSlider.HORIZONTAL);
			this.remove(buttonPanel);
			this.getContentPane().add(buttonPanel, BorderLayout.EAST);
		}
		else
		{
			speedSlider.setOrientation(JSlider.VERTICAL);
			this.remove(buttonPanel);
			this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		}
	}


	/**
	 *  A KeyAdapter that listens for the keys that work the control pad buttons
	 *
	 * @author     glen
         * @version    $Revision: 1.33 $
	 */
	class ControlPadKeyListener extends KeyAdapter
	{
		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void keyPressed(KeyEvent e)
		{
			if ( (e.getKeyCode() == accelerateKey) | (e.getKeyCode() == accelerateKey1) )
			{
				if (speedSlider.isEnabled())
				{
					if (speedSlider.getValue() != speedSlider.getMaximum())
					{
						speedSlider.setValue(speedSlider.getValue() + 1);
					}
				}
			}
                        if ( e.getKeyCode() == accelerateKey2 )
                        {
                                if (speedSlider.isEnabled())
                                {
                                        if (speedSlider.getValue() != speedSlider.getMaximum())
                                        {
                                                speedSlider.setValue(speedSlider.getValue() + 10);
                                        }
                                }
                        }
			else if ( (e.getKeyCode() == decelerateKey) | (e.getKeyCode() == decelerateKey1) )
			{
				if (speedSlider.isEnabled())
				{
					if (speedSlider.getValue() != speedSlider.getMinimum())
					{
						speedSlider.setValue(speedSlider.getValue() - 1);
					}
				}
			}
                        else if ( e.getKeyCode() == decelerateKey2 )
                        {
                                if (speedSlider.isEnabled())
                                {
                                        if (speedSlider.getValue() != speedSlider.getMinimum())
                                        {
                                                speedSlider.setValue(speedSlider.getValue() - 10);
                                        }
                                }
                        }
			else if (e.getKeyCode() == forwardKey)
			{
				if (forwardButton.isEnabled())
				{
					forwardButton.doClick();
				}
			}
			else if (e.getKeyCode() == reverseKey)
			{
				if (reverseButton.isEnabled())
				{
					reverseButton.doClick();
				}
			}
			else if (e.getKeyCode() == stopKey)
			{
				if (speedSlider.isEnabled())
				{
					stop();
				}
			}
			else if (e.getKeyCode() == idleKey)
			{
				if (speedSlider.isEnabled())
				{
					speedSlider.setValue(0);
				}
			}
		}
	}


	// update the state of this panel if any of the properties change
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (e.getPropertyName().equals("SpeedSetting")) {
	           internalAdjust=true;
		   float speed=((Float) e.getNewValue()).floatValue();
                    // multiply by MAX_SPEED, and round to find the new
                    //slider setting.
                   int newSliderSetting = java.lang.Math.round(speed * MAX_SPEED) ;
                   log.debug( "propertyChange: new speed float: " + speed + " slider pos: " + newSliderSetting ) ;
		   speedSlider.setValue( newSliderSetting );
		} else if (e.getPropertyName().equals("IsForward")) {
		   boolean Forward=((Boolean) e.getNewValue()).booleanValue();
	           setIsForward(Forward);
		}
	}


	/**
	 *  Collect the prefs of this object into XML Element
	 *  <ul>
	 *    <li> Window prefs
	 *  </ul>
	 *
	 *
	 * @return    the XML of this object.
	 */
	public Element getXml()
	{
		Element me = new Element("ControlPanel");
		Element window = new Element("window");
		WindowPreferences wp = new WindowPreferences();
		com.sun.java.util.collections.ArrayList children =
				new com.sun.java.util.collections.ArrayList(1);
		children.add(wp.getPreferences(this));
		me.setChildren(children);
		return me;
	}

	/**
	 *  Set the preferences based on the XML Element.
	 *  <ul>
	 *    <li> Window prefs
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
	}

        // initialize logging
        static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ControlPanel.class.getName());
}
