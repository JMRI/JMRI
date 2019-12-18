package jmri.jmris.simpleserver.parser;

import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* This class provides an interface between the JavaTree/JavaCC 
 * parser for the SimpleServer protocol and the JMRI back end.
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleVisitor extends JmriServerParserDefaultVisitor {

    private String outputString = null;

    public String getOutputString() {
        return outputString;
    }

    @Override
    public Object visit(ASTpowercmd node, Object data){
        log.debug("Power Command Production " + node.jjtGetValue());
        if(node.jjtGetNumChildren()==0) {
            // this is just a request for status
               try{
                  ((jmri.jmris.simpleserver.SimplePowerServer)
                   data).sendStatus(InstanceManager.getDefault(jmri.PowerManager.class).getPower());
               } catch(java.io.IOException ioe){
               } catch(jmri.JmriException je){
               }
        }else{
            if (((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("ON"))
               ((jmri.jmris.simpleserver.SimplePowerServer)data).setOnStatus();
            if (((SimpleNode)node.jjtGetChild(1)).jjtGetValue().equals("OFF"))
               ((jmri.jmris.simpleserver.SimplePowerServer)data).setOffStatus();
        }
            
        return data;
    }
    
    private final static Logger log = LoggerFactory.getLogger(SimpleVisitor.class);

}
