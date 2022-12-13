package jmri.jmrit.signalsystemeditor.configurexml;

import org.jdom2.Attribute;
import org.jdom2.Element;

import jmri.jmrit.signalsystemeditor.StringWithComment;

/**
 * Load and store StringWithComment
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class StringWithCommentXml {

    public static StringWithComment load(Element element) {
        StringWithComment stringWithComment = new StringWithComment(element.getText());
        Attribute commentAttr = element.getAttribute("comment");
        if (commentAttr != null) {
            stringWithComment.setComment(commentAttr.getValue());
        }
        return stringWithComment;
    }

    public static Element store(StringWithComment stringWithComment, String tagName) {

        Element element = new Element(tagName);
        element.setText(stringWithComment.getString());

        if (stringWithComment.getComment() != null) {
            element.setAttribute("comment", stringWithComment.getComment());
        }

        return element;
    }

}
