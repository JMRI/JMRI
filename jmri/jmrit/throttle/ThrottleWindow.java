package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.jython.Jynstrument;
import jmri.jmrit.jython.JynstrumentFactory;
import jmri.util.JmriJFrame;
import jmri.util.iharder.dnd.FileDrop;
import jmri.util.iharder.dnd.FileDrop.Listener;

import org.jdom.Element;

// Should be named ThrottleFrame, but ThrottleFrame already exit, hence ThrottleWindow
public class ThrottleWindow extends JmriJFrame {
	private static final ResourceBundle throttleBundle = ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle");

    private JPanel throttlesPanel;
    private ThrottleFrame curentThrottleFrame;
    private CardLayout throttlesLayout;
    
    private JCheckBoxMenuItem viewControlPanel;
    private JCheckBoxMenuItem viewFunctionPanel;
    private JCheckBoxMenuItem viewAddressPanel;
    private JMenuItem viewAllButtons;
    private JMenuItem fileMenuSave;
    private JMenuItem editMenuExportRoster;
    
    private JButton jbPrevious = null;
    private JButton jbNext = null;
    private JButton jbThrottleList = null;
    private JButton jbNew = null;
    private JButton jbClose = null;
    private JToolBar throttleToolBar;
    
    private String titleText = "";
    private String titleTextType = "rosterID";
   
    private PowerManager powerMgr = null;
    
    private ThrottlePanelCyclingKeyListener throttlePanelsCyclingKeyListener;
	private static int NEXT_THROTTLE_KEY = KeyEvent.VK_RIGHT;
	private static int PREV_THROTTLE_KEY = KeyEvent.VK_LEFT;
	
	private int cardCounterID = 0; // to generate unique names for each card
	private int cardCounterNB = 1; // real counter
	
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
	
    /**
     *  Default constructor
     */
    public ThrottleWindow()
    {
        super();
        throttlePanelsCyclingKeyListener = new ThrottlePanelCyclingKeyListener();
    	powerMgr = InstanceManager.powerManagerInstance();
        if (powerMgr == null)
            log.info("No power manager instance found, panel not active");
        initGUI();
    }
    
    private void initGUI()
    {
        setTitle("Throttle");
        setLayout(new BorderLayout());
        throttlesLayout = new CardLayout();
        throttlesPanel = new JPanel(throttlesLayout);
        throttlesPanel.setDoubleBuffered(true);
        if ( (jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() ) 
        	&& ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isOneWindowForAll()) )
        	initializeToolbar();

        initializeMenu();
        
        setCurentThrottleFrame ( new ThrottleFrame(this) );
        getCurentThrottleFrame().setTitle("default");
        throttlesPanel.add(getCurentThrottleFrame(),"default");       
        add(throttlesPanel,BorderLayout.CENTER);
        KeyListenerInstaller.installKeyListenerOnAllComponents(throttlePanelsCyclingKeyListener, getCurentThrottleFrame());

        this.addWindowListener(	new WindowAdapter() {
        	public void windowClosing(WindowEvent e) {
        		ThrottleWindow me = (ThrottleWindow) e.getSource();
        		ThrottleFrameManager.instance().requestThrottleWindowDestruction(me);
        		if (throttleToolBar != null) {
        			Component[] cmps = throttleToolBar.getComponents();
        			if (cmps != null)
        				for (int i=0; i<cmps.length; i++)
        					if (cmps[i] instanceof Jynstrument)
        						((Jynstrument)cmps[i]).exit();
        		}
        	}
        });
        updateGUI();
    }
    
    public void updateGUI() {
    	// title bar
    	getCurentThrottleFrame().setFrameTitle();
    	// menu items
    	viewAddressPanel.setSelected( getCurentThrottleFrame().getAddressPanel().isVisible() );
		viewControlPanel.setSelected( getCurentThrottleFrame().getControlPanel().isVisible() );
		viewFunctionPanel.setSelected( getCurentThrottleFrame().getFunctionPanel().isVisible() );
		fileMenuSave.setEnabled( getCurentThrottleFrame().getLastUsedSaveFile() != null || getCurentThrottleFrame().getRosterEntry() != null);
		editMenuExportRoster.setEnabled( getCurentThrottleFrame().getRosterEntry() != null);
    	// toolbar items
    	if (jbPrevious != null) // means toolbar enabled
    		if ( cardCounterNB > 1 ) {
    			jbPrevious.setEnabled( true );
    			jbNext.setEnabled( true );
    			jbClose.setEnabled( true );
    		}
    		else {
    			jbPrevious.setEnabled( false );
    			jbNext.setEnabled( false );
    			jbClose.setEnabled( false );
    		}
    }
    
    private void initializeToolbar()
    {
    	throttleToolBar = new JToolBar("Throttles toolbar");
    	
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
    	
    	throttleToolBar.add(new StopAllButton());
    	
		if (powerMgr != null)
			throttleToolBar.add(new LargePowerManagerButton());
		
    	throttleToolBar.addSeparator();
    	
    	jbThrottleList = new JButton();
 //   	stop.setText(throttleBundle.getString("ThrottleToolBarOpenThrottleList"));
    	jbThrottleList.setIcon(new NamedIcon("resources/icons/throttles/Movie24.gif","resources/icons/throttles/Movie24.gif"));
    	jbThrottleList.setToolTipText(throttleBundle.getString("ThrottleToolBarOpenThrottleListToolTip"));
    	jbThrottleList.setVerticalTextPosition(JButton.BOTTOM);
    	jbThrottleList.setHorizontalTextPosition(JButton.CENTER);
    	jbThrottleList.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			jmri.jmrit.throttle.ThrottleFrameManager.instance().showThrottlesList();
    		}
    	});
    	throttleToolBar.add(jbThrottleList);

    	new FileDrop(throttleToolBar, new Listener() {
    		public void filesDropped(File[] files) {
        		for (int i=0; i<files.length; i++)
        			ynstrument(files[i].getPath());
        		}
    	});

    	add(throttleToolBar, BorderLayout.PAGE_START);
    }

    private void ynstrument(String path) {
    	Jynstrument it = JynstrumentFactory.createInstrument(path, this);
    	if (it == null) {
    		log.error("Error while creating Jynstrument "+path);
    		return ;
    	}
    	ThrottleFrame.setTransparent(it);
    	it.setVisible(true);
    	throttleToolBar.add(it);
    	throttleToolBar.repaint();
    }
    
    /**
     *  Set up View, Edit and Power Menus
     */
    private void initializeMenu() {       
		JMenu fileMenu = new JMenu(throttleBundle.getString("ThrottleFileMenu"));
		JMenuItem fileMenuLoad = new JMenuItem(throttleBundle.getString("ThrottleFileMenuLoadThrottle"));
		fileMenuLoad.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				getCurentThrottleFrame().loadThrottle(null);
			}
		});
		fileMenuSave = new JMenuItem(throttleBundle.getString("ThrottleFileMenuSaveThrottle"));
		fileMenuSave.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				getCurentThrottleFrame().saveThrottle();
			}
		});
		JMenuItem fileMenuSaveAs = new JMenuItem(throttleBundle.getString("ThrottleFileMenuSaveAsThrottle"));
		fileMenuSaveAs.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				getCurentThrottleFrame().saveThrottleAs();
			}
		});

		fileMenu.add(fileMenuLoad);
		fileMenu.add(fileMenuSave);
		fileMenu.add(fileMenuSaveAs);
		
		JMenu viewMenu = new JMenu(throttleBundle.getString("ThrottleMenuView"));		
		viewAddressPanel = new JCheckBoxMenuItem(throttleBundle.getString("ThrottleMenuViewAddressPanel"));
		viewAddressPanel.setSelected(true);
		viewAddressPanel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				getCurentThrottleFrame().getAddressPanel()
						.setVisible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		viewControlPanel = new JCheckBoxMenuItem(throttleBundle.getString("ThrottleMenuViewControlPanel"));
		viewControlPanel.setSelected(true);
		viewControlPanel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				getCurentThrottleFrame().getControlPanel()
						.setVisible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		viewFunctionPanel = new JCheckBoxMenuItem(throttleBundle.getString("ThrottleMenuViewFunctionPanel"));
		viewFunctionPanel.setSelected(true);
		viewFunctionPanel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				getCurentThrottleFrame().getFunctionPanel()
						.setVisible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		viewAllButtons = new JMenuItem(throttleBundle.getString("ThrottleMenuViewAllFunctionButtons"));
		viewAllButtons.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent ev) {
				getCurentThrottleFrame().getFunctionPanel().showAllFnButtons();
			}
		});
		
		JMenuItem makeAllComponentsInBounds = new JMenuItem(throttleBundle.getString("ThrottleMenuViewMakeAllComponentsInBounds"));
		makeAllComponentsInBounds.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent ev) {
				getCurentThrottleFrame().makeAllComponentsInBounds();
			}
		});

		viewMenu.add(viewAddressPanel);
		viewMenu.add(viewControlPanel);
		viewMenu.add(viewFunctionPanel);
		viewMenu.addSeparator();
		viewMenu.add(viewAllButtons);
		viewMenu.add(makeAllComponentsInBounds);

		JMenu editMenu = new JMenu(throttleBundle.getString("ThrottleMenuEdit"));
		JMenuItem preferencesItem = new JMenuItem(throttleBundle.getString("ThrottleMenuEditFrameProperties"));
		editMenu.add(preferencesItem);
		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editPreferences();
			}
		});
		JMenuItem resetFuncButtonsItem = new JMenuItem(throttleBundle.getString("ThrottleMenuEditResetFunctionButtons"));
		editMenu.add(resetFuncButtonsItem);
		resetFuncButtonsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getCurentThrottleFrame().getFunctionPanel().initGUI();
				getCurentThrottleFrame().getFunctionPanel().setEnabled(false);
			}
		});
		editMenuExportRoster = new JMenuItem(throttleBundle.getString("ThrottleMenuEditSaveCustoms"));
		editMenu.add(editMenuExportRoster);
		editMenuExportRoster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getCurentThrottleFrame().saveRosterChanges();
			}
		});

		this.setJMenuBar(new JMenuBar());
		this.getJMenuBar().add(fileMenu);
		this.getJMenuBar().add(editMenu);
		this.getJMenuBar().add(viewMenu);

		if (powerMgr != null) {
			JMenu powerMenu = new JMenu(throttleBundle.getString("ThrottleMenuPower"));
			JMenuItem powerOn = new JMenuItem(throttleBundle.getString("ThrottleMenuPowerOn"));
			powerMenu.add(powerOn);
			powerOn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						powerMgr.setPower(PowerManager.ON);
					} catch (JmriException e1) {
						log.error("Error when setting power "+e1);
					}
				}
			});

			JMenuItem powerOff = new JMenuItem(throttleBundle.getString("ThrottleMenuPowerOff"));
			powerMenu.add(powerOff);
			powerOff.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						powerMgr.setPower(PowerManager.OFF);
					} catch (JmriException e1) {
						log.error("Error when setting power "+e1);
					}
				}
			});

			this.getJMenuBar().add(powerMenu);

			if ( (! jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() ) 
					|| ( ! jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isOneWindowForAll()) )
				this.getJMenuBar().add(new SmallPowerManagerButton());
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
       Component[] comps = throttlesPanel.getComponents() ;
       for (int i=0; i<comps.length; i++)
    	   try {
        	((ThrottleFrame)comps[i]).dispose();
    	   } catch (Exception e) {
    		   log.info("Got exception :"+e);
    	   }
        throttlesPanel.removeAll();
        removeAll();
        super.dispose();
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
		return curentThrottleFrame;
	}

	public void setCurentThrottleFrame(ThrottleFrame tf) {
		if (getCurentThrottleFrame() != null)
			log.debug("setCurentThrottleFrame from "+ getCurentThrottleFrame().getAddressPanel().getCurrentAddress()+" to "+ tf.getAddressPanel().getCurrentAddress());
		pcs.firePropertyChange("ThrottleFrame", getCurentThrottleFrame(), tf);
		curentThrottleFrame = tf;
	}
	
	public void removeThrottleFrame(ThrottleFrame tf) {
		if ( cardCounterNB > 1 ) // we don't like empty ThrottleWindow
		{
			cardCounterNB--;
			if (getCurentThrottleFrame() == tf)	{
				log.debug("Closing last created");
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

	public void addThrottleFrame(ThrottleFrame tp) {
		cardCounterID++; cardCounterNB++;
		String txt = "Card-"+cardCounterID;
		tp.setTitle(txt);
        throttlesPanel.add(tp,txt);
        throttlesLayout.show(throttlesPanel, txt);
		updateGUI();
	}
	
	public ThrottleFrame addThrottleFrame() {
		setCurentThrottleFrame ( new ThrottleFrame(this) );
        KeyListenerInstaller.installKeyListenerOnAllComponents(throttlePanelsCyclingKeyListener, getCurentThrottleFrame() );
		addThrottleFrame(getCurentThrottleFrame());
		return getCurentThrottleFrame();
	}

	public void toFront(String throttleFrameTitle) {
		throttlesLayout.show(throttlesPanel, throttleFrameTitle);
		setVisible(true);
		requestFocus();
		toFront();
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
	
	public Element getXml() { 	
		Element me  = new Element("ThrottleWindow");
        me.setAttribute("title", titleText);
        me.setAttribute("titleType", titleTextType);	
        
        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);        
        children.add(WindowPreferences.getPreferences(this));

        Component[] cmps = throttlesPanel.getComponents();
        if (cmps!=null) {
        	for (int i=0; i<cmps.length; i++)
        	try {
        		if (cmps[i] instanceof ThrottleFrame) {
	        		ThrottleFrame tf = (ThrottleFrame) cmps[i];
	        		Element tfe = tf.getXmlFile();
	        		if (tfe == null)
	        			tfe =  tf.getXml();
	        		children.add( tfe ); 
        		}
        	} catch (Exception ex) {
        		log.debug("Got exception (no panic): "+ex);
        	}
        }
        
        me.setContent(children);        
        return me;
	}

	@SuppressWarnings("unchecked")
	public void setXml(Element e) {
		if (e.getAttribute("title") != null)
			setTitle(e.getAttribute("title").getValue());
		if (e.getAttribute("title") != null)
			setTitleText ( e.getAttribute("title").getValue() );
		if (e.getAttribute("titleType") != null)
			setTitleTextType ( e.getAttribute("titleType").getValue()) ;
        
        Element window = e.getChild("window");
        if (window != null)
        	WindowPreferences.setPreferences(this, window);
        
        List<Element> tfes = e.getChildren("ThrottleFrame");
        if ((tfes != null) && (tfes.size()>0))
        	for (int i=0; i<tfes.size(); i++) {
        		ThrottleFrame tf;
        		if (i == 0)
        			tf = getCurentThrottleFrame();
        		else
        			tf = addThrottleFrame();
        		tf.setXml(tfes.get(i));
        	}
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

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottleWindow.class.getName());
}
