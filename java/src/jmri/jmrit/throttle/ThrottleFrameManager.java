package jmri.jmrit.throttle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;

import jmri.util.JmriJFrame;

/**
 *  Interface for allocating and deallocating throttles frames. Not to be
 *  confused with ThrottleManager
 *
 * @author     Glen Oberhauser
 * @version    $Revision$
 */
public class ThrottleFrameManager
{	
    /** record the single instance of Roster **/
    private static ThrottleFrameManager instance = null;

	private static int NEXT_THROTTLE_KEY = KeyEvent.VK_RIGHT;
	private static int PREV_THROTTLE_KEY = KeyEvent.VK_LEFT;

	private int activeFrame;
	private ThrottleCyclingKeyListener throttleCycler;

	private ArrayList<ThrottleWindow> throttleWindows;
	
	private ThrottlesPreferences throttlesPref;
	private JmriJFrame throttlePreferencesFrame;
	private JmriJFrame throttlesListFrame;
	private ThrottlesListPanel throttlesListPanel;

	/**
	 *  Constructor for the ThrottleFrameManager object
	 */
	private ThrottleFrameManager() // can only be created by instance() => private
	{
		throttleCycler = new ThrottleCyclingKeyListener();
		throttleWindows = new ArrayList<ThrottleWindow>(0);
        if(jmri.InstanceManager.getDefault(jmri.jmrit.throttle.ThrottlesPreferences.class)==null){
            jmri.InstanceManager.store(new jmri.jmrit.throttle.ThrottlesPreferences(), jmri.jmrit.throttle.ThrottlesPreferences.class);
        }
        throttlesPref = jmri.InstanceManager.getDefault(jmri.jmrit.throttle.ThrottlesPreferences.class);
		buildThrottleListFrame();
	}

	/**
	 * Get the singleton instance of this class.
	 */
	public static ThrottleFrameManager instance()
	{
		if (instance == null)
		{
			instance = new ThrottleFrameManager();
		}
		return instance;
	}


	/**
	 *  Tell this manager that a new ThrottleWindow was created.
	 * @return The newly created ThrottleWindow
	 */
	public ThrottleWindow createThrottleWindow() {
		ThrottleWindow tw = new ThrottleWindow();
		tw.pack();
		KeyListenerInstaller.installKeyListenerOnAllComponents(throttleCycler, tw);
		throttleWindows.add(tw);
		activeFrame = throttleWindows.indexOf(tw);
		return tw ;
	}
	
	/**
	 *  Tell this manager that a new ThrottleFrame was created.
	 * @return The newly created ThrottleFrame
	 */
	public ThrottleFrame createThrottleFrame() {
		return createThrottleWindow().getCurrentThrottleFrame() ;
	}
	
	/**
	 *  Request that this manager destroy a throttle frame.
	 *
	 * @param  frame  The to-be-destroyed ThrottleFrame
	 */
	public void requestThrottleWindowDestruction(ThrottleWindow frame)
	{
		if (frame != null)
		{
			destroyThrottleWindow(frame);
            throttleWindows.remove(throttleWindows.indexOf(frame));
			if (throttleWindows.size() > 0)
			{
				requestFocusForNextFrame();
			}
            
		}
	}

	public void requestAllThrottleWindowsDestroyed()
	{
		for (Iterator<ThrottleWindow> i = throttleWindows.iterator(); i.hasNext();)
		{
			ThrottleWindow frame = i.next();
			destroyThrottleWindow(frame);
		}
		throttleWindows = new ArrayList<ThrottleWindow>(0);
	}

	/**
	 * Perform the destruction of a ThrottleFrame. This method will not
	 * affect the throttleFrames list, thus ensuring no synchronozation problems.
	 * @param window The ThrottleFrame to be destroyed.
	 */
	private void destroyThrottleWindow(ThrottleWindow window)
	{
		window.dispose();
	}

	/**
	 *  Retrieve an Iterator over all the ThrottleFrames in existence.
	 *
	 * @return    The Iterator on the list of ThrottleFrames.
	 */
	public Iterator<ThrottleWindow> getThrottleWindows()
	{
		return throttleWindows.iterator();
	}
	
	public int getNumberThrottleWindows(){
		return throttleWindows.size();
	}

	private void requestFocusForNextFrame()
	{
		activeFrame = (activeFrame + 1) % throttleWindows.size();
		ThrottleWindow tf = throttleWindows.get(activeFrame);
		tf.requestFocus();
		tf.toFront();
	}

	private void requestFocusForPreviousFrame()
	{
		activeFrame--;
		if (activeFrame < 0)
		{
			activeFrame = throttleWindows.size() - 1;
		}
		ThrottleWindow tf = throttleWindows.get(activeFrame);
		tf.requestFocus();
		tf.toFront();
	}
	
	public ThrottleWindow getCurrentThrottleFrame() {
		if (throttleWindows == null) return null;
		if (throttleWindows.size() == 0) return null;
		return throttleWindows.get(activeFrame);
	}
	
	public ThrottlesPreferences getThrottlesPreferences() {
		return throttlesPref; 
	}

	/**
	 *  Description of the Class
	 *
	 * @author     glen
	 */
	class ThrottleCyclingKeyListener extends KeyAdapter	{
		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void keyReleased(KeyEvent e)	{
			if (e.isShiftDown() && e.getKeyCode() == NEXT_THROTTLE_KEY)		
				requestFocusForNextFrame();			
			else if (e.isShiftDown() && e.getKeyCode() == PREV_THROTTLE_KEY)
				requestFocusForPreviousFrame();			
		}
	}
	
	public ThrottlesListPanel getThrottlesListPanel() {
		return throttlesListPanel ;
	}
	
	private void buildThrottlePreferencesFrame() {
		throttlePreferencesFrame = new JmriJFrame(Bundle.getMessage("ThrottlePreferencesFrameTitle"));
		ThrottlesPreferencesPane tpP = new ThrottlesPreferencesPane();
		throttlePreferencesFrame.add(tpP);
		tpP.setContainer(throttlePreferencesFrame);
		throttlePreferencesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		throttlePreferencesFrame.pack();
	}
	
	private void buildThrottleListFrame() {
		throttlesListFrame = new JmriJFrame(Bundle.getMessage("ThrottleListFrameTile"));
		throttlesListPanel = new ThrottlesListPanel();
		throttlesListFrame.setContentPane(throttlesListPanel);
		throttlesListFrame.pack();
	}
	
	public void showThrottlesList() {
		if (throttlesListFrame == null)
			buildThrottleListFrame();
		throttlesListFrame.setVisible( ! throttlesListFrame.isVisible() );
	}
	
	public void showThrottlesPreferences() {
		if (throttlePreferencesFrame == null)
			buildThrottlePreferencesFrame();
		throttlePreferencesFrame.setVisible( true );
		throttlePreferencesFrame.requestFocus();
	}
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottleFrameManager.class.getName());
}


