/** 
 * SlotListener.java
 *
 * Description:		<describe the SlotListener class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			
 */

package loconet;


public interface SlotListener extends java.util.EventListener{
	public void notifyChangedSlot(LocoNetSlot s);
}


/* @(#)SlotListener.java */
