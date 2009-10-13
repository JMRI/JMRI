package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.powerpanel.PowerPane;
import jmri.util.JmriJFrame;
import jmri.util.RXCardLayout;

import org.jdom.Element;

// Should be named ThrottleFrame, but ThrottleFrame already exit, hence ThrottleWindow
public class ThrottleWindow extends JmriJFrame implements java.beans.PropertyChangeListener {
	private static final ResourceBundle throttleBundle = ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle");

    private JPanel throttlesPanel;
    private ThrottleFrame last; //last created ThrottleFrame, returned when getCurent not available
    private RXCardLayout throttlesLayout;
    
    private JCheckBoxMenuItem viewControlPanel;
    private JCheckBoxMenuItem viewFunctionPanel;
    private JCheckBoxMenuItem viewAddressPanel;
    private JMenuItem viewAllButtons;
    
    private JButton jbPrevious = null;
    private JButton jbNext = null;
    private JButton jbStop = null;
    private JButton jbNew = null;
    private JButton jbClose = null;
     
    private String titleText = "";
    private String titleTextType = "address";
    
    private PowerPane powerControl  = new PowerPane();
    private PowerManager powerMgr = null;
    private JButton powerLight;
    private NamedIcon powerXIcon;
    private NamedIcon powerOffIcon;
    private NamedIcon powerOnIcon;
    
    private ThrottlePanelCyclingKeyListener throttlePanelsCyclingKeyListener;
	private static int NEXT_THROTTLE_KEY = KeyEvent.VK_RIGHT;
	private static int PREV_THROTTLE_KEY = KeyEvent.VK_LEFT;
   
    /**
     *  Default constructor
     */
    public ThrottleWindow()
    {
        super();
        throttlePanelsCyclingKeyListener = new ThrottlePanelCyclingKeyListener();
        powerMgr = InstanceManager.powerManagerInstance();
        if (powerMgr == null) {
            log.info("No power manager instance found, panel not active");
        }
        else powerMgr.addPropertyChangeListener(this);
        initGUI();
    }
    
    private void initGUI()
    {
        setTitle("Throttle");
        setLayout(new BorderLayout());
        throttlesLayout = new RXCardLayout();
        throttlesPanel = new JPanel(throttlesLayout);
        
        if ( (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() ) 
        	&& ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isOneWindowForAll()) ) {
        	powerOnIcon = new NamedIcon("resources/icons/throttles/PowerGreen24.png", "resources/icons/throttles/PowerGreen24.png");
    		powerOffIcon = new NamedIcon("resources/icons/throttles/PowerRed24.png", "resources/icons/throttles/PowerRed24.png");
    		powerXIcon = new NamedIcon("resources/icons/throttles/PowerYellow24.png", "resources/icons/throttles/PowerYellow24.png");
        	initializeToolbar();
        }
        else {
        	powerOnIcon = new NamedIcon("resources/icons/throttles/GreenPowerLED.gif", "resources/icons/throttles/GreenPowerLED.gif");
    		powerOffIcon = new NamedIcon("resources/icons/throttles/RedPowerLED.gif", "resources/icons/throttles/RedPowerLED.gif");
    		powerXIcon = new NamedIcon("resources/icons/throttles/YellowPowerLED.gif", "resources/icons/throttles/YellowPowerLED.gif");
        }
        
        initializeMenu();
        
        last = new ThrottleFrame(this);
        throttlesPanel.add(last,"default");       
        add(throttlesPanel,BorderLayout.CENTER);
        KeyListenerInstaller.installKeyListenerOnAllComponents(throttlePanelsCyclingKeyListener, last);
        
        this.addWindowListener(
                               new WindowAdapter()
                               {
                                   public void windowClosing(WindowEvent e)
                                   {
                                	   ThrottleWindow me = (ThrottleWindow)e.getSource();
                                       ThrottleFrameManager.instance().requestThrottleFrameDestruction(me);
                                   }
                               });
    	updateGUI();
    }
    
    private void updateGUI() {
    	// menu items
    	viewAddressPanel.setSelected( getCurentThrottleFrame().getAddressPanel().isVisible() );
    	viewControlPanel.setSelected( getCurentThrottleFrame().getControlPanel().isVisible() );
    	viewFunctionPanel.setSelected( getCurentThrottleFrame().getFunctionPanel().isVisible() );
    	// toolbar items
    	if (jbPrevious != null)
    		if ( throttlesLayout.size() > 1 ) {
    			jbPrevious.setEnabled( true );
    			jbNext.setEnabled( true );
    			jbClose.setEnabled( true );
    		}
    		else {
    			jbPrevious.setEnabled( false );
    			jbNext.setEnabled( false );
    			jbClose.setEnabled( false );
    		}
    	// window title
    	getCurentThrottleFrame().setFrameTitle();
    	//TODO window size
 /*       if ( (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() ) 
            	&& ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isResizingWindow()) ) {
        	this.getCurentThrottleFrame().setSize( this.getCurentThrottleFrame().getPreferredSize() );
        }
*/      
    }
    
    private void initializeToolbar()
    {
    	JToolBar throttleToolBar = new JToolBar("Throttles toolbar");
    	
    	jbPrevious = new JButton();
 //   	previous.setText(throttleBundle.getString("ThrottleToolBarPrev"));
    	jbPrevious.setIcon(new NamedIcon("resources/icons/throttles/Back24.gif","resources/icons/misc/Back24.gif"));
    	jbPrevious.setVerticalTextPosition(JButton.BOTTOM);
    	jbPrevious.setHorizontalTextPosition(JButton.CENTER);
    	jbPrevious.setToolTipText(throttleBundle.getString("ThrottleToolBarPrevToolTip"));
    	jbPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				previousThrottleFrame();
			}
		});
    	throttleToolBar.add(jbPrevious);
    	
    	jbNext = new JButton();
 //   	next.setText(throttleBundle.getString("ThrottleToolBarNext"));
    	jbNext.setIcon(new NamedIcon("resources/icons/throttles/Forward24.gif","resources/icons/throttles/Forward24.gif"));
    	jbNext.setToolTipText(throttleBundle.getString("ThrottleToolBarNextToolTip"));
    	jbNext.setVerticalTextPosition(JButton.BOTTOM);
    	jbNext.setHorizontalTextPosition(JButton.CENTER);
    	jbNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextThrottleFrame();
			}
		});
    	throttleToolBar.add(jbNext);
		
    	throttleToolBar.addSeparator();
    	
    	jbNew = new JButton();
 //   	nouveau.setText(throttleBundle.getString("ThrottleToolBarNew"));
    	jbNew.setIcon(new NamedIcon("resources/icons/throttles/Add24.gif","resources/icons/throttles/Add24.gif"));
    	jbNew.setToolTipText(throttleBundle.getString("ThrottleToolBarNewToolTip"));
    	jbNew.setVerticalTextPosition(JButton.BOTTOM);
    	jbNew.setHorizontalTextPosition(JButton.CENTER);
    	jbNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addThrottleFrame();
			}
		});
    	throttleToolBar.add(jbNew);
    	
    	jbClose = new JButton();
//    	close.setText(throttleBundle.getString("ThrottleToolBarClose"));
    	jbClose.setIcon(new NamedIcon("resources/icons/throttles/Remove24.gif","resources/icons/throttles/Remove24.gif"));
    	jbClose.setToolTipText(throttleBundle.getString("ThrottleToolBarCloseToolTip"));
    	jbClose.setVerticalTextPosition(JButton.BOTTOM);
    	jbClose.setHorizontalTextPosition(JButton.CENTER);
    	jbClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeThrottleFrame();
			}
		});
    	throttleToolBar.add(jbClose);
    	
    	throttleToolBar.addSeparator();
    	
    	jbStop = new JButton();
 //   	stop.setText(throttleBundle.getString("ThrottleToolBarStopAll"));
    	jbStop.setIcon(new NamedIcon("resources/icons/throttles/Stop24.gif","resources/icons/throttles/Stop24.gif"));
    	jbStop.setToolTipText(throttleBundle.getString("ThrottleToolBarStopAllToolTip"));
    	jbStop.setVerticalTextPosition(JButton.BOTTOM);
    	jbStop.setHorizontalTextPosition(JButton.CENTER);
    	jbStop.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			Iterator<JComponent> tpi = throttlesLayout.getIterator() ;
    			while ( tpi.hasNext() )
    			{
    				ThrottleFrame tf = (ThrottleFrame) tpi.next();
    				if (tf.getControlPanel() != null)
    					tf.getControlPanel().stop();		
    			}
    		}
    	});
    	throttleToolBar.add(jbStop);
    	
		if (powerMgr != null) {
			powerLight = new JButton();
			setPowerIcons();
			// make the button itself invisible, just display the power LED
			//powerLight.setBorderPainted(false);
			powerLight.setContentAreaFilled(false);
			powerLight.setFocusPainted(false);
			throttleToolBar.add(powerLight);
			powerLight.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if (powerMgr.getPower() == PowerManager.ON)
							powerControl.offButtonPushed();
						else if (powerMgr.getPower() == PowerManager.OFF)
							powerControl.onButtonPushed();
						else if (powerMgr.getPower() == PowerManager.UNKNOWN)
							powerControl.offButtonPushed();
					} catch (JmriException ex) {
						powerLight.setIcon(powerXIcon);
					}
				}
			});
		}
    	
        add(throttleToolBar, BorderLayout.PAGE_START);
    }
    
    /**
     *  Set up View, Edit and Power Menus
     */
    private void initializeMenu() {                
		JMenu viewMenu = new JMenu("View");
		
		viewAddressPanel = new JCheckBoxMenuItem("Address Panel");
		viewAddressPanel.setSelected(true);
		viewAddressPanel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				getCurentThrottleFrame().getAddressPanel()
						.setVisible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		viewControlPanel = new JCheckBoxMenuItem("Control Panel");
		viewControlPanel.setSelected(true);
		viewControlPanel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				getCurentThrottleFrame().getControlPanel()
						.setVisible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		viewFunctionPanel = new JCheckBoxMenuItem("Function Panel");
		viewFunctionPanel.setSelected(true);
		viewFunctionPanel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				getCurentThrottleFrame().getFunctionPanel()
						.setVisible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		viewAllButtons = new JMenuItem("Show All Function Buttons");
		viewAllButtons.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent ev) {
				getCurentThrottleFrame().getFunctionPanel().showAllFnButtons();
			}
		});

		viewMenu.add(viewAddressPanel);
		viewMenu.add(viewControlPanel);
		viewMenu.add(viewFunctionPanel);
		viewMenu.add(viewAllButtons);

		JMenu editMenu = new JMenu("Edit");
		JMenuItem preferencesItem = new JMenuItem("Frame Properties");
		editMenu.add(preferencesItem);
		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editPreferences();
			}
		});
		JMenuItem resetFuncButtonsItem = new JMenuItem("Reset Function Buttons");
		editMenu.add(resetFuncButtonsItem);
		resetFuncButtonsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((ThrottleFrame)throttlesLayout.getCurrentCard()).resetFuncButtons();
			}
		});
		JMenuItem saveFuncButtonsItem = new JMenuItem("Export Customizations To Roster");
		editMenu.add(saveFuncButtonsItem);
		saveFuncButtonsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((ThrottleFrame)throttlesLayout.getCurrentCard()).saveRosterChanges();
		        if ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() ) {
		        	((ThrottleFrame)throttlesLayout.getCurrentCard()).saveThrottle();
		        }
			}
		});

		this.setJMenuBar(new JMenuBar());
		this.getJMenuBar().add(viewMenu);
		this.getJMenuBar().add(editMenu);

		if (powerMgr != null) {
			JMenu powerMenu = new JMenu("  Power");
			JMenuItem powerOn = new JMenuItem("Power On");
			powerMenu.add(powerOn);
			powerOn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					powerControl.onButtonPushed();
				}
			});

			JMenuItem powerOff = new JMenuItem("Power Off");
			powerMenu.add(powerOff);
			powerOff.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					powerControl.offButtonPushed();
				}
			});

			this.getJMenuBar().add(powerMenu);
			
			if ( (! jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() ) 
				|| ( ! jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isOneWindowForAll()) ) {
				powerLight = new JButton();
				setPowerIcons();
				// make the button itself invisible, just display the power LED
				powerLight.setBorderPainted(false);
				powerLight.setContentAreaFilled(false);
				powerLight.setFocusPainted(false);
				this.getJMenuBar().add(powerLight);
				powerLight.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							if (powerMgr.getPower() == PowerManager.ON)
								powerControl.offButtonPushed();
							else if (powerMgr.getPower() == PowerManager.OFF)
								powerControl.onButtonPushed();
							else if (powerMgr.getPower() == PowerManager.UNKNOWN)
								powerControl.offButtonPushed();
						} catch (JmriException ex) {
							powerLight.setIcon(powerXIcon);
						}
					}
				});
			}
		}

		// add help selection
		addHelpMenu("package.jmri.jmrit.throttle.ThrottleFrame", true);
	}
        
    private void editPreferences(){
        ThrottleFramePropertyEditor editor =
            ThrottleFrameManager.instance().getThrottleFrameEditor();
        editor.setThrottleFrame(this);
        // editor.setLocation(this.getLocationOnScreen());
        editor.setLocationRelativeTo(this);
        editor.setVisible(true);
    }
    
    /**
	 * Handle my own destruction.
	 * <ol>
	 * <li> dispose of sub windows.
	 * <li> notify my manager of my demise.
	 * </ol>
	 * 
	 */
    public void dispose()
    {
    	log.debug("Disposing");

        if (powerMgr!=null) powerMgr.removePropertyChangeListener(this);

        Iterator<JComponent> tpi = throttlesLayout.getIterator() ;
        while ( tpi.hasNext() )
        	((ThrottleFrame)tpi.next()).dispose();
        
        super.dispose();
    }
    
    /**
     *  implement a property change listener to monitor the power state and change
     *  the power LED displayed as appropriate
     */
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        setPowerIcons();
    }
    

    /**
     *  change the power LED displayed as appropriate and set corresponding tooltip
     *  
     */
    private void setPowerIcons() {
    	if (powerMgr==null) return;
        try {
            if (powerMgr.getPower()==PowerManager.ON) {
                powerLight.setIcon(powerOnIcon);
                powerLight.setToolTipText("Layout Power On.  Click light to turn off, or use Power menu");
            }
            else if (powerMgr.getPower()==PowerManager.OFF) {
                powerLight.setIcon(powerOffIcon);
                powerLight.setToolTipText("Layout Power Off.  Click light to turn on, or use Power menu");
            }
            else if (powerMgr.getPower()==PowerManager.UNKNOWN) {
                powerLight.setIcon(powerXIcon);
                powerLight.setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
            }
            else {
                powerLight.setIcon(powerXIcon);
                powerLight.setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
                log.error("Unexpected state value: +"+powerMgr.getPower());
            }
        } catch (JmriException ex) {
            powerLight.setIcon(powerXIcon);
            powerLight.setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
        }
    }
    
	public JCheckBoxMenuItem getViewControlPanel() {
		return viewControlPanel;
	}

	public JCheckBoxMenuItem getViewFunctionPanel() {
		return viewFunctionPanel;
	}

	public JCheckBoxMenuItem getViewAddressPanel() {
		return viewAddressPanel;
	}
	
	public ThrottleFrame getCurentThrottleFrame() {
		ThrottleFrame tf = (ThrottleFrame)throttlesLayout.getCurrentCard();
		if (tf == null) // if called before setVisible then nothing on top yet
			tf = last;
		return (tf);
	}
	
	public void removeThrottleFrame(ThrottleFrame tf) {
		if ( throttlesLayout.size() > 1 ) // we don't like empty ThrottleWindow
		{
			if (last == tf)	{
				log.debug("Closing last created");
				last = null;
			}
			throttlesPanel.remove( tf );
			tf.dispose();
			throttlesLayout.invalidateLayout(throttlesPanel);
		}
		updateGUI();
	}
	
	public void nextThrottleFrame() {
		throttlesLayout.next(throttlesPanel);
		updateGUI();
	}
	
	public void previousThrottleFrame() {
		throttlesLayout.previous(throttlesPanel);
		updateGUI();
	}
	
	public void removeThrottleFrame() {
		removeThrottleFrame( getCurentThrottleFrame() );
	}
	
	private int cardcounter = 0; // to generate unique names for each card
	public void addThrottleFrame(ThrottleFrame tp) {
		cardcounter++;
		String txt = "Card-"+cardcounter;
        throttlesPanel.add(tp,txt);
        throttlesLayout.show(throttlesPanel, txt);
		updateGUI();
	}
	
	public ThrottleFrame addThrottleFrame() {
		last = new ThrottleFrame(this);
        KeyListenerInstaller.installKeyListenerOnAllComponents(throttlePanelsCyclingKeyListener, last);
		addThrottleFrame(last);
		return last;
	}

	public Element getXml() {
    	
		Element me  = new Element("ThrottleFrame");
        me.setAttribute("title", titleText);
        me.setAttribute("titleType", titleTextType);	
        
        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);
        WindowPreferences wp = new WindowPreferences();
        
        children.add(wp.getPreferences(this));

        Iterator<JComponent> ite = throttlesLayout.getIterator() ;
        while (ite.hasNext() )
        	children.add( ((ThrottleFrame)ite.next()).getXml() );

        me.setContent(children);        
        return me;
	}

	public String getTitleTextType() {
		return titleTextType;
	}

	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	public void setTitleTextType(String titleTextType) {
		this.titleTextType = titleTextType;
	}

	public void setXml(Element e) {
    	setTitle(e.getAttribute("title").getValue());
        setTitleText ( e.getAttribute("title").getValue() );
        setTitleTextType ( e.getAttribute("titleType").getValue()) ;
        
        Element window = e.getChild("window");
        WindowPreferences wp = new WindowPreferences();
        wp.setPreferences(this, window);		
	}
	
	/**
	 * A KeyAdapter that listens for the key that cycles through the
	 * ThrottlePanels.
	 */
	class ThrottlePanelCyclingKeyListener extends KeyAdapter {
		/**
		 * Description of the Method
		 * 
		 * @param e
		 *            Description of the Parameter
		 */
		public void keyReleased(KeyEvent e) {
			if (e.isAltDown() && e.getKeyCode() == NEXT_THROTTLE_KEY) {
				log.debug("next");
				nextThrottleFrame();                    
			}
			else if (e.isAltDown() && e.getKeyCode() == PREV_THROTTLE_KEY) {
				log.debug("previous");
				previousThrottleFrame();
			}
		}
	}
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottleWindow.class.getName());
}
