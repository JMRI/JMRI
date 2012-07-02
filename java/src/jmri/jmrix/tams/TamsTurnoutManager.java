// TamsTurnoutManager.java

package jmri.jmrix.tams;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import jmri.Turnout;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.util.ResourceBundle;

/**
 * Implement turnout manager for Tams systems.
 * <P>
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version	$Revision: 19646 $
 */
public class TamsTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public TamsTurnoutManager(TamsSystemConnectionMemo memo) {

        adaptermemo=memo;
        prefix = adaptermemo.getSystemPrefix();
        tc = adaptermemo.getTrafficController();
    }
    
    TamsTrafficController tc;
    TamsSystemConnectionMemo adaptermemo;
    
    String prefix;
    
    public String getSystemPrefix() { return prefix; }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr;
        try {
            addr = Integer.valueOf(systemName.substring(getSystemPrefix().length()+1)).intValue();
        } catch (java.lang.NumberFormatException e){
            log.error("failed to convert systemName " + systemName + " to a turnout address");
            return null;
        }
        Turnout t = new TamsTurnout(addr, getSystemPrefix(), tc);
        t.setUserName(userName);
        return t;
    }
    
    boolean noWarnDelete = false;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TamsTurnoutManager.class.getName());
}

/* @(#)TamsTurnoutManager.java */
