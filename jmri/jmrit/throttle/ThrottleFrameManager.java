package jmri.jmrit.throttle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *  Interface for allocating and deallocating throttles frames. Not to be
 *  confused with ThrottleManager
 *
 * @author     Glen Oberhauser
 * @created    March 25, 2003
 * @version    $Revision: 1.5 $
 */
public class ThrottleFrameManager
{
	private static int NEXT_THROTTLE_KEY = KeyEvent.VK_RIGHT;
	private static int PREV_THROTTLE_KEY = KeyEvent.VK_LEFT;

	private int activeThrottle;
	private ThrottleCyclingKeyListener throttleCycler;

	private ArrayList throttleFrames;
	private FunctionButtonPropertyEditor functionButtonEditor;


	/**
	 *  Constructor for the ThrottleFrameManager object
	 */
	public ThrottleFrameManager()
	{
		throttleCycler = new ThrottleCyclingKeyListener();
	}

	/**
	 *  Tell this manager that a new ThrottleFrame was created.
	 *
	 */
	public void createThrottleFrame()
	{
		ThrottleFrame tf = new ThrottleFrame();
		tf.pack();
		tf.setVisible(true);
		KeyListenerInstaller.installKeyListenerOnAllComponents(throttleCycler, tf);
		if (throttleFrames == null)
		{
			throttleFrames = new ArrayList(2);
		}
		throttleFrames.add(tf);
		activeThrottle = throttleFrames.indexOf(tf);
	}

	/**
	 *  Tell this manager that a ThrottleFrame was destroyed
	 *
	 * @param  tf  The just-destroyed ThrottleFrame
	 */
	public void notifyDestroyThrottleFrame(ThrottleFrame tf)
	{
		throttleFrames.remove(throttleFrames.indexOf(tf));
	}

	/**
	 *  Retrieve an Iterator over all the ThrottleFrames in existence.
	 *
	 * @return    The Iterator on the list of ThrottleFrames.
	 */
	public Iterator getThrottleFrames()
	{
		return throttleFrames.iterator();
	}

	/**
	 *  Get a reference to the Function Editor Allows us to have one editor without
	 *  disposing and creating each time.
	 *
	 * @return    The functionButtonEditor value
	 */
	public jmri.jmrit.throttle.FunctionButtonPropertyEditor getFunctionButtonEditor()
	{
		if (functionButtonEditor == null)
		{
			functionButtonEditor = new FunctionButtonPropertyEditor();
		}
		return functionButtonEditor;
	}

	/**
	 *  Description of the Class
	 *
	 * @author     glen
	 * @created    March 29, 2003
	 */
	class ThrottleCyclingKeyListener extends KeyAdapter
	{
		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void keyPressed(KeyEvent e)
		{
			if (e.isShiftDown() && e.getKeyCode() == NEXT_THROTTLE_KEY)
			{
				activeThrottle = (activeThrottle + 1) % throttleFrames.size();
				ThrottleFrame tf = (ThrottleFrame) throttleFrames.get(activeThrottle);
				tf.requestFocus();
			}
			else if (e.isShiftDown() && e.getKeyCode() == PREV_THROTTLE_KEY)
			{
				activeThrottle--;
				if (activeThrottle < 0)
				{
					activeThrottle = throttleFrames.size() - 1;
				}
				ThrottleFrame tf = (ThrottleFrame) throttleFrames.get(activeThrottle);
				tf.requestFocus();
			}
		}
	}

}


