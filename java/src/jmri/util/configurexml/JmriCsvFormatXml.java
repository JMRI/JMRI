package jmri.util.configurexml;

import jmri.util.JmriCsvFormat;

import org.jdom2.Element;

/**
 * Load and store a jmri.util.JmriCsvFormat.
 * 
 * @author Daniel Bergqvist (C) 2022
 */
public class JmriCsvFormatXml {

    private JmriCsvFormatXml() {
        // This class should never be instantiated.
    }

    /**
     * Store a JmriCsvFormat in an Element
     *
     * @param csvFormat the format to store
     * @return Element containing the format
     */
    public static Element store(JmriCsvFormat csvFormat) {
        return new Element("predefinedCvsFormat").addContent(csvFormat.getCSVPredefinedFormat().name());
    }
    
    /**
     * Load a JmriCsvFormat in an Element
     *
     * @param element the element with the format to load
     * @return the format
     */
    public static JmriCsvFormat load(Element element) {
        JmriCsvFormat.CSVPredefinedFormat csvPredefinedFormat = JmriCsvFormat.CSVPredefinedFormat.TabSeparated;
        Element cvsFormatElement = element.getChild("predefinedCvsFormat");
        if (cvsFormatElement != null) {
            csvPredefinedFormat = JmriCsvFormat.CSVPredefinedFormat.valueOf(cvsFormatElement.getTextTrim());
        }
        JmriCsvFormat csvFormat = new JmriCsvFormat(csvPredefinedFormat);
        return csvFormat;
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JmriCsvFormatXml.class);
}
