package jmri.util;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.junit.Assume;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Builds the help index page for the JMRI help pages that's accessed as files
 * by the user's web browser.
 *
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public class BuildHelpFilesTest {

    private String _lang;
    private String _stubFolder;
    private String _template;
    private PrintWriter _mapJhmWriter;
    private TreeSet<String> _helpKeys;
    private TreeSet<String> _htmlPagesHelpKeys;
    private Properties _alternateMap;


    // The main() method is used when this class is run directly from ant
    static public void main(String[] args) throws IOException, JDOMException {
        boolean enResult = new BuildHelpFilesTest().buildIndex("en");
        boolean frResult = new BuildHelpFilesTest().buildIndex("fr");
        if (enResult && frResult) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }


    private class XmlFile extends jmri.jmrit.XmlFile {
    }

    private void generateStubFile(String helpKey) throws IOException {
        Path path = Paths.get(_stubFolder + helpKey + ".html");
        if (Files.exists(path)) {
            return;
        }
        FileWriter fileWriter = new FileWriter(FileUtil.getProgramPath()
                + "help/" + _lang + "/local/stub/"+helpKey+".html");
        try (PrintWriter printWriter = new PrintWriter(fileWriter)) {
            String contents = _template.replaceFirst("<!--HELP_KEY-->", helpKey);
            printWriter.print(contents);
        }
    }

    private void parseNode(String helpKey, Node node, String pad) {
        for (Node child : node.childNodes()) {
//            System.out.format("%s%s, %s%n", pad, child.nodeName(), child.getClass().getName());
            if ("a".equals(child.nodeName().toLowerCase())) {
                String name = child.attributes().get("name");
                if ((name != null) && !name.isEmpty()) {
                    String subHelpKey = helpKey+"_"+name;
                    _htmlPagesHelpKeys.add(subHelpKey);
//                    System.out.format("HelpKey: %s%n", subHelpKey);
                }
            }
            parseNode(helpKey, child, pad+"    ");
        }
    }

    private void searchHelpFolder(String rootFolder, String folder) throws IOException {
        Path path = FileSystems.getDefault().getPath(folder);
        Set<File> files = Stream.of(path.toFile().listFiles())
                  .filter(file -> !file.isDirectory())
                  .collect(Collectors.toSet());

        for (File file : files) {
            if (file.getName().endsWith(".shtml")) {
                String fileName = file.getAbsolutePath().substring(rootFolder.length());
                String helpKey = fileName.substring(0, fileName.indexOf(".shtml"))
                            .replace('\\', '.').replace('/', '.');
                _htmlPagesHelpKeys.add(helpKey);
//                System.out.format("HelpKey: %s%n", helpKey);
                Document doc = Jsoup.parse(file, "UTF-8");

                org.jsoup.nodes.Element body = doc.body();
                parseNode(helpKey, body, "");
            }
        }

        Set<String> folders = Stream.of(path.toFile().listFiles())
                  .filter(file -> file.isDirectory())
                  .map(File::getName)
                  .collect(Collectors.toSet());

        for (String aFolder : folders) {
            searchHelpFolder(rootFolder, folder + aFolder + "/");
        }

    }

    private void parseElement(Element e) throws IOException {
        String helpKey = e.getAttributeValue("target");
        if (helpKey != null) _helpKeys.add(helpKey);

        for (Element child : e.getChildren()) {
            parseElement(child);
        }
    }

    private boolean buildIndex(String lang) throws JDOMException, IOException {
        boolean result = true;

        _lang = lang;
        _stubFolder = FileUtil.getProgramPath() + "help/" + _lang + "/local/stub/";

        Path path = Paths.get(FileUtil.getProgramPath() + "help/" + _lang + "/local/stub_template.html");
        List<String> temp = Files.readAllLines(path);
        _template = String.join("\n", temp) + "\n";

        _alternateMap = new Properties();
        try (InputStream input = new FileInputStream(FileUtil.getProgramPath() + "help/" + _lang + "/local/alternate_map.txt")) {
            _alternateMap.load(input);
        }

        _helpKeys = new TreeSet<>();
        _htmlPagesHelpKeys = new TreeSet<>();

        String folder = FileUtil.getProgramPath() + "help/" + _lang + "/";
        searchHelpFolder(folder, folder);

        XmlFile xmlFile = new XmlFile();
        Assert.assertNotNull(xmlFile);
        Element e = xmlFile.rootFromName(FileUtil.getProgramPath()
                + "help/" + _lang + "/JmriHelp_" + _lang + "TOC.xml");
        Assert.assertNotNull(e);
        parseElement(e);

        xmlFile = new XmlFile();
        Assert.assertNotNull(xmlFile);
        e = xmlFile.rootFromName(FileUtil.getProgramPath()
                + "help/" + _lang + "/JmriHelp_" + _lang + "Index.xml");
        Assert.assertNotNull(e);
        parseElement(e);

        TreeSet<String> _stubHelpKeys = new TreeSet<>();
        _stubHelpKeys.addAll(_helpKeys);
        _stubHelpKeys.addAll(_htmlPagesHelpKeys);

        // Generate the stub files
        for (String helpKey : _stubHelpKeys) {
            try {
                if (!_alternateMap.containsKey(helpKey)) {
                    generateStubFile(helpKey);
                }
            } catch (IOException ex) {
                System.out.format("Failed to create stub file for key \"%s\"%n", helpKey);
                result = false;
            }
        }

        // Generate jmri_map.xml
        FileWriter fileWriter = new FileWriter(FileUtil.getProgramPath() + "help/" + _lang + "/local/jmri_map.xml");
        _mapJhmWriter = new PrintWriter(fileWriter);

        _mapJhmWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        _mapJhmWriter.println("<map xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://jmri.org/xml/schema/help-map.xsd\">");

        for (String helpKey : _helpKeys) {
            String expandedHelpKey;
            if (_alternateMap.containsKey(helpKey)) {
                expandedHelpKey = _alternateMap.getProperty(helpKey);
            } else {
                expandedHelpKey = helpKey.replace(".", "/");
                int pos = expandedHelpKey.indexOf('_');
                if (pos == -1) {
                    expandedHelpKey = expandedHelpKey + ".shtml";
                } else {
                    expandedHelpKey = expandedHelpKey.substring(0, pos) + ".shtml"
                            + "#" + expandedHelpKey.substring(pos+1);
                }
            }
            _mapJhmWriter.format("<mapID target=\"%s\" url=\"%s\"/>%n", helpKey, expandedHelpKey);
        }

        _mapJhmWriter.println("</map>");
        _mapJhmWriter.close();

        return result;
    }


    @Test
    public void testBuildIndex() throws JDOMException, IOException {
        Assume.assumeFalse("Ignoring BuildHelpFilesTest", Boolean.getBoolean("jmri.skipBuildHelpFilesTest"));
        buildIndex("en");
        buildIndex("fr");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BuildHelpFilesTest.class);
}
