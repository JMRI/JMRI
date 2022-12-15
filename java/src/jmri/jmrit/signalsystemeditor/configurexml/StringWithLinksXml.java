package jmri.jmrit.signalsystemeditor.configurexml;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

import jmri.jmrit.signalsystemeditor.StringWithLinks;

/**
 * Load and store StringWithLinks
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class StringWithLinksXml {

    public static StringWithLinks load(Element element) {
        StringWithLinks swl = new StringWithLinks();
        List<String> _strings = swl.getStrings();
        List<StringWithLinks.Link> _links = swl.getLinks();
        for (Content content : element.getContent()) {
            if (content.getCType() == Content.CType.Text) {
                int stringsSize = _strings.size();
                if (stringsSize > _links.size()) {
                    _strings.set(stringsSize-1, _strings.get(stringsSize-1)+content.getValue());
                } else {
                    _strings.add(content.getValue());
                }
            } else if (content.getCType() == Content.CType.Element) {
                Element e = (Element) content;
                if ("a".equals(e.getName())) {
                    if (_strings.size() <= _links.size()) {
                        _strings.add("");
                    }
                    _links.add(new StringWithLinks.Link(e.getText(), e.getAttributeValue("href")));
                } else {
                    throw new RuntimeException("Unkown tag: " + e.getName());
                }
            } else {
                throw new RuntimeException("Unkown CType: " + content.getCType().name());
            }
        }

        // If no links, just read the text
        if (_links.isEmpty()) {
            _strings.clear();
            _strings.add(element.getText());
        }
        return swl;
    }

    public static Element store(StringWithLinks stringWithLinks, String tagName) {

        Element element = new Element(tagName);

        List<String> _strings = stringWithLinks.getStrings();
        List<StringWithLinks.Link> _links = stringWithLinks.getLinks();

        if (_strings.size() == 1 && _links.isEmpty()) {
            element.setText(_strings.get(0));
            return element;
        } else {
            int i=0;
            while (i < _strings.size() || i < _links.size()) {
                if (i < _strings.size()) {
                    StringBuilder sb = new StringBuilder(_strings.get(i));
                    // Replace spaces with non breaking spaces at the beginning of the string
                    for (int j=0; j < sb.length(); j++) {
                        if (sb.charAt(j) == ' ') {
                            sb.setCharAt(j, '\u00A0');
                        } else {
                            break;
                        }
                    }
                    // Replace spaces with non breaking spaces at the end of the string
                    for (int j=sb.length()-1; j > 0; j--) {
                        if (sb.charAt(j) == ' ') {
                            sb.setCharAt(j, '\u00A0');
                        } else {
                            break;
                        }
                    }
                    element.addContent(sb.toString());
                }
                if (i < _links.size()) {
                    Element link = new Element("a");
                    link.setText(_links.get(i).getName());
                    link.setAttribute("href", _links.get(i).getHref());
                    element.addContent(link);
                }
                i++;
            }
            return !element.getChildren().isEmpty() ? element : null;
        }
    }

}
