/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet.xmlio;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.jmrit.XmlFile;
import jmri.web.xmlio.XmlIO;
import jmri.web.xmlio.XmlIOFactory;
import jmri.web.xmlio.XmlIORequestor;
import jmri.web.xmlio.XmlIOServer;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Applications relying on XmlIO should migrate to JSON.
 * @see jmri.web.servlet.json.JsonServlet
 * @author rhwood
 */
public class XmlIOServlet extends HttpServlet implements XmlIORequestor {

    static XmlIOFactory factory = null;
    static Logger log = LoggerFactory.getLogger(XmlIOServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameterNames().hasMoreElements()) {
            Document doc = new Document(new Element(XmlIO.XMLIO));
            Element e = new Element(request.getPathInfo().substring(request.getPathInfo().indexOf("/") + 1)); // NOI18N
            Enumeration<String> parameters = request.getParameterNames();
            while (parameters.hasMoreElements()) {
                String parameter = parameters.nextElement();
                e.setAttribute(parameter, request.getParameter(parameter));
            }
            doc.getRootElement().addContent(e);
            this.doXmlIO(request, response, doc);
        } else {
            response.sendRedirect("/help/en/html/web/XMLIO.shtml"); // NOI18N
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        SAXBuilder builder = XmlFile.getBuilder(false);
        try {
            Document doc = builder.build(request.getInputStream());
            this.doXmlIO(request, response, doc);
        } catch (JDOMException e1) {
            log.error("JDOMException on input: " + e1, e1);
        }
    }

    protected void doXmlIO(HttpServletRequest request, HttpServletResponse response, Document doc) throws ServletException, IOException {
        XMLOutputter fmt = new XMLOutputter(org.jdom.output.Format.getCompactFormat());

        String client = request.getRemoteHost() + ":" + request.getRemotePort(); // NOI18N

        Element root = doc.getRootElement();
        doc.addContent(new Comment("XmlIO is deprecated and will be removed in JMRI 3.11. See the JMRI release notes."));

        if (log.isDebugEnabled()) {
            String s = "received " + root.getContentSize() + " elements from " + client; // NOI18N
            if (root.getContentSize() < 4) { //append actual xml to debug if only a "few" els
                s += " " + fmt.outputString(root.getContent()); // NOI18N
            }
            log.debug(s);
        }

        // start processing reply
        if (factory == null) {
            factory = new XmlIOFactory();
            log.warn("XmlIO is deprecated and will be removed in JMRI 3.11. See the JMRI release notes.");
        }
        XmlIOServer srv = factory.getServer();

        // if list or throttle elements present, or item elements that do set, do immediate operation
        boolean immediate = false;
        if (root.getChild(XmlIO.LIST) != null
                || root.getChild(XmlIO.THROTTLE) != null) {
            immediate = true;
        }
        if (!immediate) {
            for (Object e : root.getChildren()) {
                if (((Element) e).getAttributeValue(XmlIO.SET) != null
                        || ((Element) e).getChild(XmlIO.SET) != null) {
                    immediate = true;
                }
                break;
            }
        }
        try {
            if (immediate) {
                srv.immediateRequest(root);  // modifies 'doc' in place
                if (log.isDebugEnabled()) {
                    String s = "immediate reply " + root.getContentSize() + " elements to " + client; // NOI18N
                    if (root.getContentSize() < 4) { //append actual xml to debug if only a "few" els
                        s += " " + fmt.outputString(root.getContent()); // NOI18N
                    }
                    log.debug(s);
                }
            } else {
                Thread thread = Thread.currentThread();
                if (log.isDebugEnabled()) {
                    log.debug("stalling thread, waiting to reply to " + client);
                }
                srv.monitorRequest(root, this, client, thread);

                try {
                    //Thread.sleep(10000000000000L);  // really long
                    Thread.sleep(300000);  // not quite so long (5 minutes)
                    if (log.isDebugEnabled()) {
                        log.debug("Thread sleep completed for " + client);
                    }
                } catch (InterruptedException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Thread sleep interrupted for " + client);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("thread resumes and replies " + doc.getRootElement().getContentSize() + " elements to " + client);
                }
            }
        } catch (jmri.JmriException e1) {
            log.error("JmriException while creating reply: " + e1, e1);
        }

        response.setContentType("text/xml"); // NOI18N
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Cache-Control", "no-cache"); // NOI18N
        response.getWriter().write(fmt.outputString(doc));
    }

    @Override
    public void monitorReply(Element e, Thread thread) {
        if (log.isDebugEnabled()) {
            log.debug("Interrupting thread " + thread.getName() + " (" + thread.getState() + ")");
        }
        thread.interrupt();
    }
}
