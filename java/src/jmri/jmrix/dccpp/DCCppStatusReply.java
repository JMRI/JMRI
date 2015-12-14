/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.dccpp;

import java.util.regex.Matcher;
import static jmri.jmrix.dccpp.DCCppReply.match;

/**
 *
 * @author munderwd
 */
public class DCCppStatusReply extends DCCppReply {
    private String base_type;
    private String code_build;
    
    public DCCppStatusReply(char c, String regex) {
        super(c, regex);
    }
    
    @Override
    public void parseReply(String s) {
        DCCppStatusReply r = (DCCppStatusReply)DCCppStatusReply.parseDCCppReply(s);
        this.opcode = r.opcode;
        this.base_type = r.base_type;
        this.code_build = r.code_build;
        this.myRegex = r.myRegex;
        this.myReply = r.myReply;
        this._nDataChars = r._nDataChars;
    }
    
    public static DCCppReply parseDCCppReply(String s) {
        log.debug("Parsing Status Reply: {}", s);
        Matcher m;
        DCCppStatusReply r = new DCCppStatusReply(s.charAt(0), null);
        switch(s.charAt(0)) {
            case DCCppConstants.VERSION_REPLY:
                if ((m = match(s, DCCppConstants.STATUS_REPLY_REGEX, "ctor")) != null) {
                    r.base_type = m.group(1); // comm type
                    r.code_build = m.group(2); // comm type
                    r.myRegex = DCCppConstants.STATUS_REPLY_REGEX;
                    r.myReply = new StringBuilder(s);
                    log.debug("Base: {} Code: {}", r.base_type, r.code_build);
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);
            default:
                return(null);
        }
        
    }
    
    @Override
    public String toString() {
        log.debug("DCCppStatusReply.toString() {} {}", this.base_type, this.code_build);
        return(Character.toString('i') + base_type + ": BUILD " + code_build);
    }
    
    @Override
    public boolean isVersionReply() { return(true); }
}
