package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalAppearanceMap;
import jmri.Turnout;
import jmri.implementation.TurnoutSignalMast;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for TurnoutSignalMast objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 */
public class TurnoutSignalMastXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public TurnoutSignalMastXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        TurnoutSignalMast p = (TurnoutSignalMast) o;
        Element e = new Element("turnoutsignalmast");
        e.setAttribute("class", this.getClass().getName());
        e.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, e);
        Element unlit = new Element("unlit");
        if (p.allowUnLit()) {
            unlit.setAttribute("allowed", "yes");
            unlit.addContent(new Element("turnout").addContent(p.getUnLitTurnoutName()));
            if (p.getUnLitTurnoutState() == Turnout.CLOSED) {
                unlit.addContent(new Element("turnoutstate").addContent("closed"));
            } else {
                unlit.addContent(new Element("turnoutstate").addContent("thrown"));
            }
        } else {
            unlit.setAttribute("allowed", "no");
        }
        e.addContent(unlit);
        SignalAppearanceMap appMap = p.getAppearanceMap();
        if (appMap != null) {
            java.util.Enumeration<String> aspects = appMap.getAspects();
            while (aspects.hasMoreElements()) {
                String key = aspects.nextElement();
                Element el = new Element("aspect");
                el.setAttribute("defines", key);
                el.addContent(new Element("turnout").addContent(p.getTurnoutName(key)));
                if (p.getTurnoutState(key) == Turnout.CLOSED) {
                    el.addContent(new Element("turnoutstate").addContent("closed"));
                } else {
                    el.addContent(new Element("turnoutstate").addContent("thrown"));
                }
                e.addContent(el);
            }
        }
        List<String> disabledAspects = p.getDisabledAspects();
        if (disabledAspects != null) {
            Element el = new Element("disabledAspects");
            for (String aspect : disabledAspects) {
                Element ele = new Element("disabledAspect");
                ele.addContent(aspect);
                el.addContent(ele);
            }
            if (disabledAspects.size() != 0) {
                e.addContent(el);
            }
        }
        if (p.resetPreviousStates()) {
            e.addContent(new Element("resetPreviousStates").addContent("yes"));
        }
        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        TurnoutSignalMast m;
        String sys = getSystemName(shared);
        try {
            m = (TurnoutSignalMast) InstanceManager.getDefault(jmri.SignalMastManager.class)
                    .provideCustomSignalMast(sys, TurnoutSignalMast.class);
        } catch (JmriException e) {
            log.error("Failed to load TurnoutSignalMast {}: {}", sys, e);
            return false;
        }

        if (getUserName(shared) != null) {
            m.setUserName(getUserName(shared));
        }

        loadCommon(m, shared);

        if (shared.getChild("unlit") != null) {
            Element unlit = shared.getChild("unlit");
            if (unlit.getAttribute("allowed") != null) {
                if (unlit.getAttribute("allowed").getValue().equals("no")) {
                    m.setAllowUnLit(false);
                } else {
                    m.setAllowUnLit(true);
                    String turnout = unlit.getChild("turnout").getText();
                    String turnoutState = unlit.getChild("turnoutstate").getText();
                    int turnState = Turnout.THROWN;
                    if (turnoutState.equals("closed")) {
                        turnState = Turnout.CLOSED;
                    }
                    m.setUnLitTurnout(turnout, turnState);
                }
            }
        }

        List<Element> list = shared.getChildren("aspect");
        for (int i = 0; i < list.size(); i++) {
            Element e = list.get(i);
            String aspect = e.getAttribute("defines").getValue();
            String turnout = e.getChild("turnout").getText();
            String turnoutState = e.getChild("turnoutstate").getText();
            int turnState = Turnout.THROWN;
            if (turnoutState.equals("closed")) {
                turnState = Turnout.CLOSED;
            }
            m.setTurnout(aspect, turnout, turnState);
        }
        Element e = shared.getChild("disabledAspects");
        if (e != null) {
            list = e.getChildren("disabledAspect");
            for (Element aspect : list) {
                m.setAspectDisabled(aspect.getText());
            }
        }
        if ((shared.getChild("resetPreviousStates") != null)
                && shared.getChild("resetPreviousStates").getText().equals("yes")) {
            m.resetPreviousStates(true);
        }

        return true;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutSignalMastXml.class);
}
