package jmri.jmrit.throttle;
import java.util.ArrayList;

import java.util.Iterator;

/**
 *  Interface for allocating and deallocating throttles frames. Not to be
 *  confused with ThrottleManager
 *
 * @author     Glen Oberhauser
 * @created    March 25, 2003
 * @version    $Revision: 1.4 $
 */
public class ThrottleFrameManager
{
	private ArrayList throttleFrames;
	private FunctionButtonPropertyEditor functionButtonEditor;

	/**
	 *  Tell this manager that a new ThrottleFrame was created.
	 *
	 * @param  tf  The new ThrottleFrame.
	 */
	public void createThrottleFrame()
	{
        ThrottleFrame tf = new ThrottleFrame();
        tf.pack();
        tf.setVisible(true);
		if (throttleFrames == null)
		{
			throttleFrames = new ArrayList(2);
		}
		throttleFrames.add(tf);
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

}


