/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.dccpp;

import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author munderwd
 */
public class DCCppCommTypeReply extends DCCppReply {
    
    private String comm_type;
    private String port_name;
    private final static Logger log = LoggerFactory.getLogger(DCCppCommTypeReply.class);

    public DCCppCommTypeReply(char o, String regex) {
        super(o, regex);
    }
    
    @Override
    public String toString() {
        log.debug("DCCppCommTypeReply.toString()");
        return(Character.toString('N') + comm_type + ": " + port_name);
    }
    
    @Override
    public void parseReply(String s) {
        DCCppCommTypeReply r = (DCCppCommTypeReply)DCCppCommTypeReply.parseDCCppReply(s);
        this.comm_type = r.comm_type;
        this.port_name = r.port_name;
        this.myRegex = r.myRegex;
        this.myReply = r.myReply;
        this._nDataChars = r._nDataChars;
    }
    
    public static DCCppReply parseDCCppReply(String s) {
        log.debug("Parsing CommTypeReply... {}", s);
        Matcher m;
        DCCppCommTypeReply r = new DCCppCommTypeReply(s.charAt(0), null);
        switch(s.charAt(0)) {
            case DCCppConstants.COMM_TYPE_REPLY:
                if ((m = match(s, DCCppConstants.COMM_TYPE_REPLY_REGEX, "ctor")) != null) {
                    r.comm_type = m.group(1); // comm type
                    r.port_name = m.group(2); // comm type
                    r.myRegex = DCCppConstants.COMM_TYPE_REPLY_REGEX;
                    r.myReply = new StringBuilder(s);
		    log.debug("Type: {} Port: {}", r.comm_type, r.port_name);
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);
            default:
                return(null);
        }
    }
    
    
}
