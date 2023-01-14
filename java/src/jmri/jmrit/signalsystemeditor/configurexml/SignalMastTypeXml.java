package jmri.jmrit.signalsystemeditor.configurexml;

import java.io.*;
import java.net.URL;

import jmri.jmrit.XmlFile;
import jmri.jmrit.signalsystemeditor.*;
import jmri.util.FileUtil;

import org.jdom2.*;

/**
 * Load and store a signal mast type from/to xml
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class SignalMastTypeXml {

    public SignalMastType load(SignalSystem signalSystem, File file) {

//        System.out.format("Load: %s%n", file.getAbsolutePath());

        Namespace namespace = Namespace.getNamespace("http://docbook.org/ns/docbook");

        SignalMastType signalMastType = new SignalMastType(file.getName());

        URL url = FileUtil.findURL(file.getAbsolutePath(), "resources", "xml");
        if (url == null) {
            log.error("appearance file (xml/{}) doesn't exist", file);
            throw new IllegalArgumentException("appearance file (xml/" + file + ") doesn't exist");
        }

        jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile();
        Element root;
        try {
            root = xf.rootFromURL(url);

            signalMastType.setProcessingInstructionType(xf.getProcessingInstructionType());
            signalMastType.setProcessingInstructionHRef(xf.getProcessingInstructionHRef());

            Element aspecttable = root;

            assert "appearancetable".equals(aspecttable.getName());

            Attribute attr = aspecttable.getAttribute("noNamespaceSchemaLocation");
            if (attr == null) {
                for (Attribute a : aspecttable.getAttributes()) {
                    if ("noNamespaceSchemaLocation".equals(a.getName())) {
                        attr = a;
                    }
                }
                if (attr == null) {
                    throw new RuntimeException("Attribute 'noNamespaceSchemaLocation' is not found for element 'appearancetable'");
                }
            }

            signalMastType.setAppearanceSchema(attr.getValue());

            Element copyright = aspecttable.getChild("copyright", namespace);
            signalMastType.getCopyright().getDates().clear();
            if (copyright != null) {
                for (Element date : copyright.getChildren("year", namespace)) {
                    signalMastType.getCopyright().getDates().add(date.getTextTrim());
                }
                signalMastType.getCopyright().setHolder(copyright.getChildText("holder", namespace));
            }

            Element authors = aspecttable.getChild("authorgroup", namespace);
            signalMastType.getAuthors().clear();
            if (authors != null) {
                for (Element author : authors.getChildren("author", namespace)) {
                    Element personName = author.getChild("personname", namespace);
                    signalMastType.getAuthors().add(
                            new Author(personName.getChildText("firstname", namespace),
                                    personName.getChildText("surname", namespace),
                                    author.getChildText("email", namespace)));
                }
            }

            Element revhistory = aspecttable.getChild("revhistory", namespace);
            signalMastType.getRevisions().clear();
            if (revhistory != null) {
                for (Element revision : revhistory.getChildren("revision", namespace)) {
                    signalMastType.getRevisions().add(
                            new Revision(revision.getChildText("revnumber", namespace),
                                    revision.getChildText("date", namespace),
                                    revision.getChildText("authorinitials", namespace),
                                    revision.getChildText("revremark", namespace)));
                }
            }


            signalMastType.setAspectTable(aspecttable.getChildText("aspecttable"));

            signalMastType.setName(aspecttable.getChildText("name"));

            signalMastType.getReferences().clear();
            for (Element referenceElement : aspecttable.getChildren("reference")) {
                signalMastType.getReferences().add(StringWithLinksXml.load(referenceElement));
            }

            signalMastType.getDescriptions().clear();
            for (Element descriptionElement : aspecttable.getChildren("description")) {
                signalMastType.getDescriptions().add(StringWithLinksXml.load(descriptionElement));
            }


            Element appearances = aspecttable.getChild("appearances");
            signalMastType.getAppearances().clear();
            if (appearances != null) {
                for (Element appearanceElement : appearances.getChildren("appearance")) {
                    Appearance appearance = new Appearance(
                            appearanceElement.getChildText("aspectname"));

                    appearance.getShowList().clear();
                    for (Element e : appearanceElement.getChildren("show")) {
                        appearance.getShowList().add(e.getText());
                    }
                    appearance.getReferences().clear();
                    for (Element e : appearanceElement.getChildren("reference")) {
                        appearance.getReferences().add(e.getText());
                    }
                    appearance.getComments().clear();
                    for (Element e : appearanceElement.getChildren("comment")) {
                        appearance.getComments().add(e.getText());
                    }
                    if (appearanceElement.getChild("delay") != null) {
                        appearance.setDelay(appearanceElement.getChildText("delay"));
                    }
                    appearance.getImageLinks().clear();
                    for (Element imageLinkElement : appearanceElement.getChildren("imagelink")) {
                        ImageType imageType = null;
                        if (imageLinkElement.getAttribute("type") != null) {
                            try {
                                imageType = signalSystem.getImageType(imageLinkElement.getAttributeValue("type"));
                            } catch (IllegalArgumentException ex) {
                                log.error("ERROR: image type {} does not exists, {}", imageLinkElement.getAttributeValue("type"), file.toString());
                            }
                        }
                        appearance.getImageLinks().add(new ImageLink(imageLinkElement.getTextTrim(), imageType));
                    }
                    signalMastType.getAppearances().add(appearance);
                }
            }


            Element specificappearances = aspecttable.getChild("specificappearances");
            if (specificappearances != null) {
                Element appearanceDanger = specificappearances.getChild("danger");
                if (appearanceDanger != null) {
                    signalMastType.getAppearanceDanger().setAspectName(
                            appearanceDanger.getChildText("aspect"));
                    for (Element imagelink : appearanceDanger.getChildren("imagelink")) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
                        }
                        signalMastType.getAppearanceDanger().getImageLinks().add(new ImageLink(
                                imagelink.getTextTrim(), imageType));
                    }
                }

                Element appearancePermissive = specificappearances.getChild("permissive");
                if (appearancePermissive != null) {
                    signalMastType.getAppearancePermissive().setAspectName(
                            appearancePermissive.getChildText("aspect"));
                    for (Element imagelink : appearancePermissive.getChildren("imagelink")) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
                        }
                        signalMastType.getAppearancePermissive().getImageLinks().add(new ImageLink(
                                appearancePermissive.getChildText("imagelink"), imageType));
                    }
                }

                Element appearanceHeld = specificappearances.getChild("held");
                if (appearanceHeld != null) {
                    signalMastType.getAppearanceHeld().setAspectName(
                            appearanceHeld.getChildText("aspect"));
                    for (Element imagelink : appearanceHeld.getChildren("imagelink")) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
                        }
                        signalMastType.getAppearanceHeld().getImageLinks().add(new ImageLink(
                                imagelink.getTextTrim(), imageType));
                    }
                }

                Element appearanceDark = specificappearances.getChild("dark");
                if (appearanceDark != null) {
                    signalMastType.getAppearanceDark().setAspectName(
                            appearanceDark.getChildText("aspect"));
                    for (Element imagelink : appearanceDark.getChildren("imagelink")) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
                        }
                        signalMastType.getAppearanceDark().getImageLinks().add(new ImageLink(
                                imagelink.getTextTrim(), imageType));
                    }
                }
            }


            Element aspectMappings = aspecttable.getChild("aspectMappings");
            signalMastType.getAspectMappings().clear();
            if (aspectMappings != null) {
                for (Element aspectMappingElement : aspectMappings.getChildren("aspectMapping")) {
                    AspectMapping aspectMapping = new AspectMapping(
                            aspectMappingElement.getChildText("advancedAspect"));
                    for (Element ourAspectElement : aspectMappingElement.getChildren("ourAspect")) {
                        aspectMapping.getOurAspects().add(ourAspectElement.getText());
                    }
                    signalMastType.getAspectMappings().add(aspectMapping);
                }
            }

            log.debug("loading complete");
        } catch (java.io.IOException | org.jdom2.JDOMException e) {
            log.error("error reading file {}", url.getPath(), e);
            return null;
        }


        return signalMastType;
    }


    public void save(SignalSystem signalSystem, SignalMastType signalMastType, boolean makeBackup) {
        save(signalSystem, signalMastType, FileUtil.getProfilePath(), makeBackup);
    }

    public void save(SignalSystem signalSystem, SignalMastType signalMastType, String path, boolean makeBackup) {
        String fileName = path + "xml/signals/" + signalSystem.getFolderName() + "/" + signalMastType.getFileName();

        XmlFile xmlFile = new XmlFile();
        if (makeBackup) {
            xmlFile.makeBackupFile(fileName);
        }
        File file = new File(fileName);

//        System.out.format("Store: %s%n", file.getAbsolutePath());

        try {
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    log.warn("Could not create parent directories for signal file :{}", fileName);
                    return;
                }
            }
            if (file.createNewFile()) {
                log.debug("Creating new signal file: {}", fileName);
            }
        } catch (IOException ea) {
            log.error("Could not create signal file at {}.", fileName, ea);
        }

        try {
            Element root = new Element("appearancetable");
            Document doc = new Document(root);

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/panelfile"+schemaVersion+".xsl"?>
            java.util.Map<String, String> m = new java.util.HashMap<>();
            m.put("type", signalMastType.getProcessingInstructionType() != null
                    ? signalMastType.getProcessingInstructionType() : "text/xsl");
            m.put("href", signalMastType.getProcessingInstructionHRef() != null
                    ? signalMastType.getProcessingInstructionHRef() : "../../XSLT/appearancetable.xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0, p);

            root.setAttribute("noNamespaceSchemaLocation",
                    signalMastType.getAppearanceSchema() != null ? signalMastType.getAppearanceSchema() : "http://jmri.org/xml/schema/appearancetable.xsd",
                    Namespace.getNamespace("xsi",
                            "http://www.w3.org/2001/XMLSchema-instance"));

            if (store(signalMastType, root)) {
                xmlFile.writeXML(file, doc);
            }
        } catch (IOException eb) {
            log.warn("Exception in storing signal xml", eb);
        }
    }

    public boolean store(SignalMastType signalMastType, Element root) {
        Namespace namespace = Namespace.getNamespace("http://docbook.org/ns/docbook");

        Element copyright = new Element("copyright", namespace);
        for (String date : signalMastType.getCopyright().getDates()) {
            copyright.addContent(new Element("year", namespace).setText(date));
        }
        copyright.addContent(new Element("holder", namespace).setText(signalMastType.getCopyright().getHolder()));
        root.addContent(copyright);

        Element authorGroup = new Element("authorgroup", namespace);
        for (Author author : signalMastType.getAuthors()) {
            Element authorElement = new Element("author", namespace);
            Element personName = new Element("personname", namespace);
            personName.addContent(new Element("firstname", namespace).setText(author.getFirstName()));
            personName.addContent(new Element("surname", namespace).setText(author.getSurName()));
            authorElement.addContent(personName);
            if (author.getEmail() != null && !author.getEmail().isBlank()) {
                authorElement.addContent(new Element("email", namespace).addContent(author.getEmail()));
            }
            authorGroup.addContent(authorElement);
        }
        root.addContent(authorGroup);

        Element revhistory = new Element("revhistory", namespace);
        for (Revision revision : signalMastType.getRevisions()) {
            Element revisionElement = new Element("revision", namespace);
            revisionElement.addContent(new Element("revnumber", namespace).setText(revision.getRevNumber()));
            revisionElement.addContent(new Element("date", namespace).setText(revision.getDate()));
            revisionElement.addContent(new Element("authorinitials", namespace).setText(revision.getAuthorInitials()));
            revisionElement.addContent(new Element("revremark", namespace).setText(revision.getRemark()));
            revhistory.addContent(revisionElement);
        }
        root.addContent(revhistory);


        root.addContent(new Element("aspecttable").setText(signalMastType.getAspectTable()));

        root.addContent(new Element("name").setText(signalMastType.getName()));

        for (StringWithLinks reference : signalMastType.getReferences()) {
            Element e = StringWithLinksXml.store(reference, "reference");
            if (e != null) {
                root.addContent(e);
            }
        }

        for (StringWithLinks description : signalMastType.getDescriptions()) {
            Element e = StringWithLinksXml.store(description, "description");
            if (e != null) {
                root.addContent(e);
            }
        }


        Element appearancesElement = new Element("appearances");
        for (Appearance appearance : signalMastType.getAppearances()) {
            Element appearanceElement = new Element("appearance");
            appearanceElement.addContent(new Element("aspectname").setText(appearance.getAspectName()));

            for (String show : appearance.getShowList()) {
                appearanceElement.addContent(new Element("show").setText(show));
            }
            for (String reference : appearance.getReferences()) {
                appearanceElement.addContent(new Element("reference").setText(reference));
            }
            for (String comment : appearance.getComments()) {
                appearanceElement.addContent(new Element("comment").setText(comment));
            }
            if (appearance.getDelay() != null && !appearance.getDelay().isBlank()) {
                appearanceElement.addContent(new Element("delay").setText(appearance.getDelay()));
            }
            for (ImageLink imageLink : appearance.getImageLinks()) {
                Element imageLinkElement = new Element("imagelink");
                imageLinkElement.setText(imageLink.getImageLink());
                if (imageLink.getType() != null) {
                    imageLinkElement.setAttribute("type", imageLink.getType().getType());
                }
                appearanceElement.addContent(imageLinkElement);
            }
            appearancesElement.addContent(appearanceElement);
        }
        root.addContent(appearancesElement);


        Element specificAppearancesElement = new Element("specificappearances");

        String dangerAspect = signalMastType.getAppearanceDanger().getAspectName();
        if (dangerAspect != null && !dangerAspect.isBlank()) {
            Element specificAppearancesDanger = new Element("danger");
            specificAppearancesDanger.addContent(new Element("aspect").setText(dangerAspect));
            for (ImageLink imageLink : signalMastType.getAppearanceDanger().getImageLinks()) {
                Element imageLinkElement = new Element("imagelink");
                imageLinkElement.setText(imageLink.getImageLink());
                if (imageLink.getType() != null) {
                    imageLinkElement.setAttribute("type", imageLink.getType().getType());
                }
                specificAppearancesDanger.addContent(imageLinkElement);
            }
            specificAppearancesElement.addContent(specificAppearancesDanger);
        }

        String permissiveAspect = signalMastType.getAppearancePermissive().getAspectName();
        if (permissiveAspect != null && !permissiveAspect.isBlank()) {
            Element specificAppearancesPermissive = new Element("permissive");
            specificAppearancesPermissive.addContent(new Element("aspect").setText(permissiveAspect));
            for (ImageLink imageLink : signalMastType.getAppearancePermissive().getImageLinks()) {
                Element imageLinkElement = new Element("imagelink");
                imageLinkElement.setText(imageLink.getImageLink());
                if (imageLink.getType() != null) {
                    imageLinkElement.setAttribute("type", imageLink.getType().getType());
                }
                specificAppearancesPermissive.addContent(imageLinkElement);
            }
            specificAppearancesElement.addContent(specificAppearancesPermissive);
        }

        String heldAspect = signalMastType.getAppearanceHeld().getAspectName();
        if (heldAspect != null && !heldAspect.isBlank()) {
            Element specificAppearancesHeld = new Element("held");
            specificAppearancesHeld.addContent(new Element("aspect").setText(heldAspect));
            for (ImageLink imageLink : signalMastType.getAppearanceHeld().getImageLinks()) {
                Element imageLinkElement = new Element("imagelink");
                imageLinkElement.setText(imageLink.getImageLink());
                if (imageLink.getType() != null) {
                    imageLinkElement.setAttribute("type", imageLink.getType().getType());
                }
                specificAppearancesHeld.addContent(imageLinkElement);
            }
            specificAppearancesElement.addContent(specificAppearancesHeld);
        }

        String darkAspect = signalMastType.getAppearanceDark().getAspectName();
        if (darkAspect != null && !darkAspect.isBlank()) {
            Element specificAppearancesDark = new Element("dark");
            specificAppearancesDark.addContent(new Element("aspect").setText(darkAspect));
            for (ImageLink imageLink : signalMastType.getAppearanceDark().getImageLinks()) {
                Element imageLinkElement = new Element("imagelink");
                imageLinkElement.setText(imageLink.getImageLink());
                if (imageLink.getType() != null) {
                    imageLinkElement.setAttribute("type", imageLink.getType().getType());
                }
                specificAppearancesDark.addContent(imageLinkElement);
            }
            specificAppearancesElement.addContent(specificAppearancesDark);
        }

        if (!specificAppearancesElement.getChildren().isEmpty()) {
            root.addContent(specificAppearancesElement);
        }


        Element aspectMappingsElement = new Element("aspectMappings");
        for (AspectMapping aspectMapping : signalMastType.getAspectMappings()) {
            Element aspectMappingElement = new Element("aspectMapping");
            aspectMappingElement.addContent(new Element("advancedAspect").setText(aspectMapping.getAdvancedAspect()));
            for (String ourAspect : aspectMapping.getOurAspects()) {
                aspectMappingElement.addContent(new Element("ourAspect").setText(ourAspect));
            }
            aspectMappingsElement.addContent(aspectMappingElement);
        }
        if (!aspectMappingsElement.getChildren().isEmpty()) {
            root.addContent(aspectMappingsElement);
        }


        return true;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalMastTypeXml.class);
}
