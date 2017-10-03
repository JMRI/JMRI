package jmri.util.docbook.configurexml;

import java.util.ArrayList;
import jmri.util.docbook.RevHistory;
import jmri.util.docbook.Revision;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Load/Store RevHistory objects.
 * <p>
 * This interacts somewhat differently with the ConfigureXML system. RevHistory
 * objects are _not_ registed with the manager, but rather handled explicitly by
 * them. The "load()" method is therefore a null-op here.
 *
 * @author Bob Jacobsen Copyright (c) 2010
 */
public class RevHistoryXml extends jmri.configurexml.AbstractXmlAdapter {

    private static final String NAMESPACE = "http://docbook.org/ns/docbook"; // NOI18N
    
    /**
     * Usual configurexml method, this one doesn't do anything because the
     * content is explicitly loaded from the file
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        return true;
    }

    static public RevHistory loadRevHistory(Element e) {
        RevHistory r = new RevHistory();

        java.util.List<Element> list = e.getChildren("revision", Namespace.getNamespace(NAMESPACE));
        for (int i = 0; i < list.size(); i++) {
            loadRevision(r, list.get(i));
        }
        return r;
    }

    static void loadRevision(RevHistory r, Element e) {
        Element s;
        Namespace n = Namespace.getNamespace(NAMESPACE);
        int revnumber = 0;
        s = e.getChild("revnumber", n);
        if (s != null) {
            String c = s.getText();
            revnumber = Integer.parseInt(c);
        }

        String date = null;
        s = e.getChild("date", n);
        if (s != null) {
            date = s.getText();
        }

        String authorinitials = null;
        s = e.getChild("authorinitials", n);
        if (s != null) {
            authorinitials = s.getText();
        }

        String revremark = null;
        s = e.getChild("revremark", n);
        if (s != null) {
            revremark = s.getText();
        }

        r.addRevision(revnumber, date, authorinitials, revremark);
    }

    /**
     * This method is not implemented: See class comment above for more discussion.
     */
    @Override
    public void load(Element e, Object o) {
        throw new UnsupportedOperationException("Method not coded");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element store(Object o) {
        return storeDirectly(o);
    }

    static public Element storeDirectly(Object o) {
        final RevHistory r = (RevHistory) o;
        if (r == null) {
            return null;
        }

        Element e = historyElement(r);

        // and return
        return e;
    }

    static Element historyElement(RevHistory r) {
        ArrayList<Revision> list = r.getList();

        Element e = new Element("revhistory", NAMESPACE);

        for (int i = 0; i < list.size(); i++) {
            Element revision = revisionElement(list.get(i));
            e.addContent(revision);
        }

        return e;
    }

    static Element revisionElement(Revision r) {
        Element rev = new Element("revision", NAMESPACE);

        Element revnumber = new Element("revnumber", NAMESPACE);
        revnumber.addContent("" + r.revnumber);
        rev.addContent(revnumber);

        Element date = new Element("date", NAMESPACE);
        date.addContent(r.date);
        rev.addContent(date);

        Element authorinitials = new Element("authorinitials", NAMESPACE);
        authorinitials.addContent(r.authorinitials);
        rev.addContent(authorinitials);

        Element revremark = new Element("revremark", NAMESPACE);
        revremark.addContent(r.revremark);
        rev.addContent(revremark);

        return rev;
    }
}
