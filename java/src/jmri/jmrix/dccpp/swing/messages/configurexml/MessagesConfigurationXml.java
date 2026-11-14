package jmri.jmrix.dccpp.swing.messages.configurexml;

import java.awt.Dimension;
import java.util.List;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.swing.messages.MessagesConfiguration;
import jmri.util.ColorUtil;

import org.jdom2.Element;

/** Handle configuration for MessagesConfiguration objects. */
public class MessagesConfigurationXml {

    public static Element store(MessagesConfiguration p) {

        Element element = new Element("dccex_messages_config");

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }
        element.addContent(new Element("expirySeconds")
                .addContent(Integer.toString(p.getExpirySeconds())));
        element.addContent(new Element("maxLines")
                .addContent(Integer.toString(p.getMaxLines())));
        element.addContent(new Element("autoOpen")
                .addContent(p.isAutoOpen() ? "true" : "false"));
        element.addContent(new Element("playSound")
                .addContent(p.isPlaySound() ? "true" : "false"));
        element.addContent(new Element("soundFile")
                .addContent(p.getSoundFile()));
        element.addContent(new Element("foreground")
                .addContent(ColorUtil.colorToColorName(p.getMessageForeground())));
        element.addContent(new Element("background")
                .addContent(ColorUtil.colorToColorName(p.getMessageBackground())));
        Dimension size = p.getMessagesSize();
        if (size != null) {
            element.addContent(new Element("numColumns")
                    .addContent(Integer.toString(size.width)));
            element.addContent(new Element("numLines")
                    .addContent(Integer.toString(size.height)));
        }

        return element;
    }

    public static void load(MessagesConfiguration p, Element elem, boolean ignoreMemo)
            throws JmriConfigureXmlException {

        Element element = elem.getChild("dccex_messages_config");

        if (!ignoreMemo) {
            DCCppSystemConnectionMemo memo = null;
            String systemConnectionName = "Unknown connection";
            Element systemConnection = element.getChild("systemConnection");
            if (systemConnection != null) {
                systemConnectionName = systemConnection.getTextTrim();
                List<DCCppSystemConnectionMemo> systemConnections =
                        jmri.InstanceManager.getList(DCCppSystemConnectionMemo.class);
                for (DCCppSystemConnectionMemo m : systemConnections) {
                    if (m.getSystemPrefix().equals(systemConnectionName)) {
                        memo = m;
                        break;
                    }
                }
            }
            if (memo == null) {
                throw new JmriConfigureXmlException("Cannot find connection: " + systemConnectionName);
            }
            p.setMemo(memo);
        }

        String value = element.getChildTextTrim("expirySeconds");
        if (value != null) {
            p.setExpirySeconds(Integer.parseInt(value));
        }
        value = element.getChildTextTrim("maxLines");
        if (value != null) {
            p.setMaxLines(Integer.parseInt(value));
        }
        value = element.getChildTextTrim("autoOpen");
        if (value != null) {
            p.setAutoOpen("true".equals(value));
        }
        value = element.getChildTextTrim("playSound");
        if (value != null) {
            p.setPlaySound("true".equals(value));
        }
        value = element.getChildTextTrim("soundFile");
        if (value != null) {
            p.setSoundFile(value);
        }
        value = element.getChildTextTrim("foreground");
        if (value != null) {
            p.setMessageForeground(ColorUtil.stringToColor(value));
        }
        value = element.getChildTextTrim("background");
        if (value != null) {
            p.setMessageBackground(ColorUtil.stringToColor(value));
        }
        String numColumns = element.getChildTextTrim("numColumns");
        String numLines = element.getChildTextTrim("numLines");
        if (numColumns != null && numLines != null) {
            p.setMessagesSize(new Dimension(Integer.parseInt(numColumns), Integer.parseInt(numLines)));
        }
    }

}
