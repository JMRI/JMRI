package jmri;

/**
 * Object to handle "user" and "system" addresses. 
 * Manager classes
 * are primary consumer of these
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version
 */
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
        return _userName;
    }

    public void setSystemName(String s) {
        _systemName = s;
    }

    public String getSystemName() {
        return _systemName;
    }

    // to free resources when no longer used
    public void dispose() {
    }
    private String _systemName;
    private String _userName;
}
