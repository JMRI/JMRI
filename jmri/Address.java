/** 
 * Address.java
 *
 * Description:		Object to handle "user" and "system" addresses
 *                  Manager classes are primary consumer of these
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public class Address {

	public Address(String system, String user) {
		_userName = user;
		_systemName = system;
	}
	
	/**
	 * both names are the same in this ctor
	 */
	public Address(String name) {
		_userName = _systemName = name;
	}

	public void setUserName(String s) {
		_userName = s;
	}
	public String getUserName() {
		return new String(_userName);
	}

	public void setSystemName(String s) {
		_systemName = s;
	}
	public String getSystemName() {
		return new String(_systemName);
	}

	// to free resources when no longer used
	public void dispose() throws JmriException {};
	private String _systemName;
	private String _userName;
}


/* @(#)Address.java */
