package jmri.jmrix.loconet.cmdstnconfig;

import java.io.*;
import org.jdom.*;
import com.sun.java.util.collections.*;

import jmri.jmrit.XmlFile;
import apps.Apps;

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
  static void dumpNode( Element node, int depth )
  {
    int leader ;
    for( leader = 0; leader < depth; leader++ )
      System.out.print( '\t' );

    System.out.print( node.getName() );
    Iterator attributes = node.getAttributes().iterator() ;
    Attribute attribute ;
    while( attributes.hasNext() )
    {
      attribute = (Attribute) attributes.next() ;
      System.out.print( " " + attribute.getName() + " = " + attribute.getValue() ) ;
    }
    System.out.println() ;
    Iterator children = node.getChildren().iterator() ;
    depth++ ;
    while( children.hasNext() )
    {
      dumpNode( (Element)children.next(), depth ) ;
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
            org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.ERROR);
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
    catch (FileNotFoundException ex) {
      log.warn( "Command Station Config XML File Not Found", ex );
    }
    catch (JDOMException ex) {
      log.warn( "XML Error", ex );
    }
  }

  // initialize logging
  static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XmlConfig.class.getName());
}