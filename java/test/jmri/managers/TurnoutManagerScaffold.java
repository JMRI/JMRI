// TurnoutManagerScaffold.java

package jmri.managers;

import jmri.*;

 /**
 * Dummy implementation of TurnoutManager for testing purposes.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision$
 */
public class TurnoutManagerScaffold implements TurnoutManager {

    public Turnout provideTurnout(String name) { return null; }

    public Turnout getTurnout(String name)  { return null; }

    public Turnout getBySystemName(String systemName)  { return null; }

    public Turnout getByUserName(String userName)  { return null; }

    public Turnout newTurnout(String systemName, String userName)  { return null; }

    public java.util.List<String> getSystemNameList()  { return null; }
    
    public java.util.List<String> getNamedBeanList()  { return null; }
	
    public String[] getSystemNameArray() {return null; }

    public String getClosedText()  { return null; }
	
	 public String getThrownText()  { return null; }
	 
	 public String[] getValidOperationTypes()  { return null; }
	
	 public int askNumControlBits(String systemName)  { return -1; }
	
	 public int askControlType(String systemName)  { return -1; }

    public char systemLetter() { return ' '; }

    public String getSystemPrefix() { return " "; }
    public char typeLetter() { return ' '; }

    public String makeSystemName(String s)  { return null; }

    public void dispose() {}

    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {}

    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {}
    
    public void register(NamedBean n) {}

    public void deregister(NamedBean n) {}

    public String getNextValidAddress(String curAddress, String prefix) throws JmriException { return curAddress; }

    public boolean isControlTypeSupported(String systemName) {return false;}

    public boolean isNumControlBitsSupported(String systemName) {return false;}

    public boolean allowMultipleAdditions(String systemName) {return false;}

    public void setDefaultClosedSpeed(String speed) {}

    public void setDefaultThrownSpeed(String speed) {}

    public String getDefaultThrownSpeed() { return null;}

    public String getDefaultClosedSpeed() {return null; }
    
    public int getXMLOrder() { return -1; }
    
    public NamedBean getBeanBySystemName(String systemName) { return null; }
    
    public NamedBean getBeanByUserName(String userName) { return null; }
    
    public NamedBean getNamedBean(String name) { return null; }
    
    public String createSystemName(String curAddress, String prefix) throws JmriException {return " "; }

}


/* @(#)TurnoutManagerScaffold.java */
