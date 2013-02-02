package jmri.jmrix.loconet.cmdstnconfig;

import org.apache.log4j.Logger;
import java.io.*;
import org.jdom.*;
import java.util.*;

import jmri.jmrit.XmlFile;

/**
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
 *
 * @author Bob Jacobsen
 */
public class XmlConfig extends XmlFile
{
  public XmlConfig()
  {
  }

  static void dumpNodes( Element root )
  {
    int depth = 0 ;
    dumpNode( root, depth ) ;
  }
  @SuppressWarnings("unchecked")
static void dumpNode( Element node, int depth )
  {
    int leader ;
    for( leader = 0; leader < depth; leader++ )
      System.out.print( '\t' );

    System.out.print( node.getName() );
    Iterator<Attribute> attributes = node.getAttributes().iterator() ;
    Attribute attribute ;
    while( attributes.hasNext() )
    {
      attribute = attributes.next() ;
      System.out.print( " " + attribute.getName() + " = " + attribute.getValue() ) ;
    }
    System.out.println() ;
    Iterator<Element> children = node.getChildren().iterator() ;
    depth++ ;
    while( children.hasNext() )
    {
      dumpNode(children.next(), depth );
    }
  }

  public static void main(String[] args)
  {
    String logFile = "default.lcf";

    try {
        if (new java.io.File(logFile).canRead()) {
            org.apache.log4j.PropertyConfigurator.configure(logFile);
        } else {
            org.apache.log4j.BasicConfigurator.configure();
            org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ERROR);
        }
    }
    catch (java.lang.NoSuchMethodError e) { log.error("Exception starting logging: "+e); }

    XmlConfig xmlconfig1 = new XmlConfig();
    Element root ;
    try
    {
      root = xmlconfig1.rootFromName("digitrax-cs-config.xml");
      dumpNodes( root );
    }
    catch (IOException ex) {
      log.warn( "Command Station Config XML File Not Found", ex );
    }
    catch (JDOMException ex) {
      log.warn( "XML Error", ex );
    }
  }

  // initialize logging
  static private Logger log = Logger.getLogger(XmlConfig.class.getName());
}
