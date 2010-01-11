package jmri.jmrit.revhistory.configurexml;

import org.jdom.Element;
import org.jdom.Namespace;
import java.util.ArrayList;

import jmri.jmrit.revhistory.RevHistory;

/**
 * Load/Store RevHistory objects.
 * <p>
 * This interacts somewhat differently with the ConfigureXML system.
 * RevHistory objects are _not_ registed with the manager, but rather 
 * handled explicitly by them.  The "load()" method is therefore a null-op
 * here.
 *
 * @author Bob Jacobsen  Copyright (c) 2010
 * @version $Revision: 1.2 $
 */

public class RevHistoryXml extends jmri.configurexml.AbstractXmlAdapter {
    
    /**
     * Usual configurexml method, this one doesn't
     * do anything because the content is explicitly loaded 
     * from the file
     */
    public boolean load(Element e) throws Exception {
        return true;
    }
    
    /**
     * Load RevHistory from an element.
     *
     * <p>If no RevHistory already present in InstanceManager,
     * creates one and adds this.  
     * <P>
     * Then adds, instead of replacing, the history information
     */
    public boolean loadDirectly(Element e) throws Exception {
        if (!e.getName().equals("revhistory"))
            throw new Exception("Unexpected element name: "+e.getName());
        
        RevHistory rmain = jmri.InstanceManager.getDefault(RevHistory.class);
        
        RevHistory r = loadRevHistory(e);
        System.out.println(r);
        rmain.addRevision("loaded by JMRI "+jmri.Version.name(), r);
        
        return true;
    }
    
    static public RevHistory loadRevHistory(Element e) {
        RevHistory r = new RevHistory();
        
        @SuppressWarnings("unchecked")
        java.util.List<Element> list = (java.util.List<Element>)(e.getChildren("revision", Namespace.getNamespace("http://docbook.org/ns/docbook")));
        for (int i = 0; i<list.size(); i++) {
            loadRevision(r, list.get(i));
        }
        return r;
    }

    static void loadRevision(RevHistory r, Element e) {
        Element s;
        Namespace n = Namespace.getNamespace("http://docbook.org/ns/docbook");
        int revnumber = 0;
        s = e.getChild("revnumber",n);
        if (s!=null) {
            String c = s.getText();
            revnumber = Integer.parseInt(c);
        }
        
        String date = null;
        s = e.getChild("date",n);
        if (s!=null) {
            date = s.getText();
        }
        
        String authorinitials = null;
        s = e.getChild("authorinitials",n);
        if (s!=null) {
            authorinitials = s.getText();
        }
        
        String revremark = null;
        s = e.getChild("revremark",n);
        if (s!=null) {
            revremark = s.getText();
        }
        
        RevHistory revhistory = null;
        s = e.getChild("revhistory",n);
        if (s!=null) {
            revhistory = loadRevHistory(s);
        }
        
        r.addRevision(revnumber, date, authorinitials, revremark, revhistory);
    }


    /**
     * Create a set of configured objects from their
     * XML description, using an auxiliary object.
     * <P>
     * For example, the auxilary object o might be a manager or GUI of some type
     * that needs to be informed as each object is created.
     *
     * @param e Top-level XML element containing the description
     * @param o Implementation-specific Object needed for the conversion
     * @throws Exception when a error prevents creating the objects as
     *          as required by the input XML.  
     */
    public void load(Element e, Object o) throws Exception {
        throw new Exception("Method not coded");
    }

    /**
     * Store the
     * @param o The object to be recorded.  Specific XmlAdapter
     *          implementations will require this to be of a specific
     *          type; that binding is done in ConfigXmlManager.
     * @return The XML representation Element
     */
    public Element store(Object o) {
        return storeDirectly(o);
    }
    
    static public Element storeDirectly(Object o) {
        final RevHistory r = (RevHistory)o;
        if (r == null) return null;
        
        Element e = historyElement(r);
        
        // add one more element for this store
        RevHistory.Revision rev = 
                r.new Revision() {{
                    revnumber = r.maxNumber()+1;
                    date = (new java.util.Date()).toString();
                    authorinitials = System.getProperty("user.name");
                    revremark = "Store from JMRI "+jmri.Version.name();
                    history = null;
                }};

        e.addContent(
            revisionElement(rev)
        );
        // and return
        return e;
    }
    static Element historyElement(RevHistory r) {
        ArrayList<RevHistory.Revision> list = r.getList();
        
        Element e = new Element("revhistory","http://docbook.org/ns/docbook");

        for (int i = 0; i<list.size(); i++) {
            Element revision = revisionElement(list.get(i));
            e.addContent(revision);            
        }
        
        return e;
    }
    
    static Element revisionElement(RevHistory.Revision r) {
        Element rev = new Element("revision","http://docbook.org/ns/docbook");

        Element revnumber = new Element("revnumber","http://docbook.org/ns/docbook");
        revnumber.addContent(""+r.revnumber);
        rev.addContent(revnumber);
        
        Element date = new Element("date","http://docbook.org/ns/docbook");
        date.addContent(r.date);
        rev.addContent(date);

        Element authorinitials = new Element("authorinitials","http://docbook.org/ns/docbook");
        authorinitials.addContent(r.authorinitials);
        rev.addContent(authorinitials);

        Element revremark = new Element("revremark","http://docbook.org/ns/docbook");
        revremark.addContent(r.revremark);
        rev.addContent(revremark);
        
        // not strictly docbook, this nesting is an extension
        if (r.history != null) 
            rev.addContent(historyElement(r.history));
            
        return rev;
    }
}