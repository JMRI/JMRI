package jmri.jmrit.signalsystemeditor.configurexml;

import java.util.List;

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
        for (Element child : element.getChildren()) {
            if ("text".equals(child.getName())) {
                int stringsSize = _strings.size();
                if (stringsSize > _links.size()) {
                    _strings.set(stringsSize-1, _strings.get(stringsSize-1)+child.getText());
                } else {
                    _strings.add(child.getValue());
                }
            } else if ("a".equals(child.getName())) {
                if (_strings.size() <= _links.size()) {
                    _strings.add("");
                }
                _links.add(new StringWithLinks.Link(child.getText(), child.getAttributeValue("href")));
            } else {
                throw new RuntimeException("Unkown tag: " + child.getName());
            }
        }
        return swl;
    }

    public static Element store(StringWithLinks stringWithLinks, String tagName) {

        Element element = new Element(tagName);

        List<String> _strings = stringWithLinks.getStrings();
        List<StringWithLinks.Link> _links = stringWithLinks.getLinks();

        int i=0;
        while (i < _strings.size() || i < _links.size()) {
            if (i < _strings.size()) {
                Element text = new Element("text");
                text.addContent(_strings.get(i));
                element.addContent(text);
            }
            if (i < _links.size()) {
                Element link = new Element("a");
                link.setText(_links.get(i).getName());
                link.setAttribute("href", _links.get(i).getHRef());
                element.addContent(link);
            }
            i++;
        }
        return !element.getChildren().isEmpty() ? element : null;
    }

}
