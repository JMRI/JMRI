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
 * @version    $Revision: 1.8 $
 */
public class ThrottleFrameManager
{
    /** record the single instance of Roster **/
    private static ThrottleFrameManager instance = null;

	private static int NEXT_THROTTLE_KEY = KeyEvent.VK_RIGHT;
	private static int PREV_THROTTLE_KEY = KeyEvent.VK_LEFT;

	private int activeFrame;
	private ThrottleCyclingKeyListener throttleCycler;

	private ArrayList throttleFrames;
	private FunctionButtonPropertyEditor functionButtonEditor;
	private ThrottleFramePropertyEditor throttleFramePropertyEditor;


	/**
	 *  Constructor for the ThrottleFrameManager object
	 */
	public ThrottleFrameManager()
	{
		throttleCycler = new ThrottleCyclingKeyListener();
		throttleFrames = new ArrayList(0);
	}

	public static ThrottleFrameManager instance()
	{
		if (instance == null)
		{
			instance = new ThrottleFrameManager();
		}
		return instance;
	}
		
	
	/**
	 *  Tell this manager that a new ThrottleFrame was created.
	 * @return The newly created ThrottleFrame
	 */
	public ThrottleFrame createThrottleFrame()
	{
		ThrottleFrame tf = new ThrottleFrame();
		tf.pack();
		KeyListenerInstaller.installKeyListenerOnAllComponents(throttleCycler, tf);
		throttleFrames.add(tf);
		activeFrame = throttleFrames.indexOf(tf);
		return tf;
	}

	/**
	 *  Request that this manager destroy a throttle frame.
	 *
	 * @param  frame  The to-be-destroyed ThrottleFrame
	 */
	public void requestThrottleFrameDestruction(ThrottleFrame frame)
	{
		if (frame != null)
		{
			throttleFrames.remove(throttleFrames.indexOf(frame));
			destroyThrottleFrame(frame);
			if (throttleFrames.size() > 0)
			{
				requestFocusForNextFrame();
			}
		}
	}

	public void requestAllThrottleFramesDestroyed()
	{
		for (Iterator i = throttleFrames.iterator(); i.hasNext();)
		{
			ThrottleFrame frame = (ThrottleFrame)i.next();
			destroyThrottleFrame(frame);
		}
		throttleFrames = new ArrayList(0);
	}

	/**
	 * Perform the destruction of a ThrottleFrame. This method will not
	 * affect the throttleFrames list, thus ensuring no synchronozation problems.
	 * @param frame The ThrottleFrame to be destroyed.
	 */
	private void destroyThrottleFrame(ThrottleFrame frame)
	{
		frame.dispose();
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
	 */
	public FunctionButtonPropertyEditor getFunctionButtonEditor()
	{
		if (functionButtonEditor == null)
		{
			functionButtonEditor = new FunctionButtonPropertyEditor();
		}
		return functionButtonEditor;
	}

	/**
	 *  Get a reference to the ThrottleFrame Editor. Allows us to have one editor without
	 *  disposing and creating each time.
	 */
	public ThrottleFramePropertyEditor getThrottleFrameEditor()
	{
		if (throttleFramePropertyEditor == null)
		{
			throttleFramePropertyEditor = new ThrottleFramePropertyEditor();
		}
		return throttleFramePropertyEditor;
	}

	private void requestFocusForNextFrame()
	{
		activeFrame = (activeFrame + 1) % throttleFrames.size();
		ThrottleFrame tf = (ThrottleFrame) throttleFrames.get(activeFrame);
		tf.requestFocus();
	}
	
	private void requestFocusForPreviousFrame()
	{
		activeFrame--;
		if (activeFrame < 0)
		{
			activeFrame = throttleFrames.size() - 1;
		}
		ThrottleFrame tf = (ThrottleFrame) throttleFrames.get(activeFrame);
		tf.requestFocus();
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
				requestFocusForNextFrame();
			}
			else if (e.isShiftDown() && e.getKeyCode() == PREV_THROTTLE_KEY)
			{
				requestFocusForPreviousFrame();
			}
		}
	}

}


