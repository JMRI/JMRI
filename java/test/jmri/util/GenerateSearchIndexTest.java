package jmri.util;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assume;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.junit.jupiter.api.*;

/**
 * Generates the search index for local help files.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class GenerateSearchIndexTest {

    private static final boolean GENERATE_WHOLE_WORD_INDEX = true;
    private static final boolean GENERATE_BEGINNING_OF_WORD_INDEX = true;
    private static final boolean GENERATE_END_OF_WORD_INDEX = true;
    private static final boolean GENERATE_PART_OF_WORD_INDEX = false;

    private final Map<Integer, String> _fileIndex = new HashMap<>();
    private final Map<Integer, String> _fileHeaderIndex = new HashMap<>();
    private final Map<Integer, Map<String, Set<Integer>>> _searchIndexWholeWord = new HashMap<>();
    private final Map<Integer, Map<String, Set<Integer>>> _searchIndexBeginning = new HashMap<>();
    private final Map<Integer, Map<String, Set<Integer>>> _searchIndexEnd = new HashMap<>();
    private final Map<Integer, Map<String, Set<Integer>>> _searchIndexPart = new HashMap<>();
    private int _currentFileId = 0;


    // The main() method is used when this class is run directly from ant
    static public void main(String[] args) throws IOException {
        new GenerateSearchIndexTest().generateSearchIndex();
    }


    private void addWord(String word, int fileId) {
        if (word.length() < 3) return;

        // Whole word
        Map<String, Set<Integer>> wordMapWholeWord = _searchIndexWholeWord.get(word.length());
        if (wordMapWholeWord == null) {
            wordMapWholeWord = new HashMap<>();
            _searchIndexWholeWord.put(word.length(), wordMapWholeWord);
        }
        Set<Integer> fileIdListWholeWord = wordMapWholeWord.get(word);
        if (fileIdListWholeWord == null) {
            fileIdListWholeWord = new HashSet<>();
            wordMapWholeWord.put(word, fileIdListWholeWord);
        }
        fileIdListWholeWord.add(fileId);

        for (int i=3; i <= word.length(); i++) {
            Map<String, Set<Integer>> wordMapBeginning = _searchIndexBeginning.get(i);
            Map<String, Set<Integer>> wordMapEnd = _searchIndexEnd.get(i);
            Map<String, Set<Integer>> wordMapPart = _searchIndexPart.get(i);
            if (wordMapBeginning == null) {
                wordMapBeginning = new HashMap<>();
                _searchIndexBeginning.put(i, wordMapBeginning);
                wordMapEnd = new HashMap<>();
                _searchIndexEnd.put(i, wordMapEnd);
                wordMapPart = new HashMap<>();
                _searchIndexPart.put(i, wordMapPart);
            }

            for (int j=0; j <= word.length()-i; j++) {
                String part = word.substring(j, i+j);

                if (j==0) {
                    // Beginning of a word
                    Set<Integer> fileIdList = wordMapBeginning.get(part);
                    if (fileIdList == null) {
                        fileIdList = new HashSet<>();
                        wordMapBeginning.put(part, fileIdList);
                    }
                    fileIdList.add(fileId);
                }
                if (i+j == word.length()) {
                    // End of a word
                    Set<Integer> fileIdList = wordMapEnd.get(part);
                    if (fileIdList == null) {
                        fileIdList = new HashSet<>();
                        wordMapEnd.put(part, fileIdList);
                    }
                    fileIdList.add(fileId);
                }

                // Part of a word
                if (GENERATE_PART_OF_WORD_INDEX) {
                    Set<Integer> fileIdList = wordMapPart.get(part);
                    if (fileIdList == null) {
                        fileIdList = new HashSet<>();
                        wordMapPart.put(part, fileIdList);
                    }
                    fileIdList.add(fileId);
                }
            }
        }
    }

    private void parseHeader(Node node, String pad) {
        for (Node child : node.childNodes()) {
            if (child instanceof Element) {
                if ("title".equalsIgnoreCase(child.nodeName())) {
                    String text = ((Element) child).ownText().trim().replaceAll("\r", "").replace("\n", "").replace("\"", "\\\\\"");
                    if (!text.isBlank()) {
                        if (_fileHeaderIndex.containsKey(_currentFileId)) {
                            System.out.format("ERROR: Header already exists for file %s: %s --- %s%n", _fileIndex.get(_currentFileId), _fileHeaderIndex.get(_currentFileId), text);
                        } else {
                            _fileHeaderIndex.put(_currentFileId, text);
                        }
                    }
                }
            }

            parseHeader(child, pad+"    ");
        }
    }

    private void parseNode(Node node, String pad) {
        for (Node child : node.childNodes()) {
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode)child;
                String text = textNode.getWholeText().toLowerCase().trim();

                String[] parts = text.split("\\W+");
                for (String s : parts) {
                    addWord(s, _currentFileId);
                }
            }
            parseNode(child, pad+"    ");
        }
    }

    private void searchFolder(String folder) throws IOException {
        Path path = FileSystems.getDefault().getPath(folder);
        Set<String> files = Stream.of(path.toFile().listFiles())
                  .filter(file -> !file.isDirectory())
                  .map(File::getName)
                  .collect(Collectors.toSet());

        for (String file : files) {
            if (file.endsWith(".shtml")) {
                String fileName = folder + file;
                _fileIndex.put(_currentFileId, fileName);
                Path filePath = FileSystems.getDefault().getPath(fileName);
                Document doc = Jsoup.parse(filePath.toFile(), "UTF-8");
                parseHeader(doc.head(), "");
                parseNode(doc.body(), "");
                _currentFileId++;
            }
        }

        Set<String> folders = Stream.of(path.toFile().listFiles())
                  .filter(file -> file.isDirectory())
                  .map(File::getName)
                  .collect(Collectors.toSet());

        for (String aFolder : folders) {
            searchFolder(folder + aFolder + "/");
        }

    }

    private void createJsonFile() throws IOException {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(
                FileUtil.getProgramPath() + "help/en/local/search.json"))) {

            // Whole word
            if (GENERATE_WHOLE_WORD_INDEX) {
                printWriter.print("let searchIndexWholeWord = '{");
                printWriter.print("\"files\":{");
                for (Map.Entry<Integer, String> entry : _fileIndex.entrySet()) {
                    printWriter.format("\"%d\":[\"%s\",\"%s\"],", entry.getKey(), entry.getValue(), _fileHeaderIndex.get(entry.getKey()));
                }
                printWriter.print("\"-1\":\"\"");  // Dummy data since we have a comma character after each data
                printWriter.print("},");

                printWriter.print("\"words\":{");
                for (Map.Entry<Integer, Map<String, Set<Integer>>> searchIndexEntry : _searchIndexWholeWord.entrySet()) {
                    printWriter.format("\"%d\":{", searchIndexEntry.getKey());
                    for (Map.Entry<String, Set<Integer>> wordMapEntry : searchIndexEntry.getValue().entrySet()) {
                        printWriter.format("\"%s\":[", wordMapEntry.getKey());
                        for (int fileId : wordMapEntry.getValue()) {
        //                    System.out.format("%5d: %20s, %s%n", wordMap.getKey(), fileIdList.getKey(), _fileIndex.get(fileId));
                            printWriter.format("\"%d\",", fileId);
                        }
                        printWriter.print("\"-1\"");  // Dummy data since we have a comma character after each data
                        printWriter.print("],");
                    }
                    printWriter.print("\"\":\"\"");  // Dummy data since we have a comma character after each data
                    printWriter.print("},");
                }
                printWriter.print("\"-1\":\"\"");  // Dummy data since we have a comma character after each data
                printWriter.print("}");

                printWriter.println("}';");
            }


            // Beginning of a word
            if (GENERATE_BEGINNING_OF_WORD_INDEX) {
                printWriter.print("let searchIndexBeginning = '{");
                printWriter.print("\"files\":{");
                for (Map.Entry<Integer, String> entry : _fileIndex.entrySet()) {
                    printWriter.format("\"%d\":[\"%s\",\"%s\"],", entry.getKey(), entry.getValue(), _fileHeaderIndex.get(entry.getKey()));
                }
                printWriter.print("\"-1\":\"\"");  // Dummy data since we have a comma character after each data
                printWriter.print("},");

                printWriter.print("\"words\":{");
                for (Map.Entry<Integer, Map<String, Set<Integer>>> searchIndexEntry : _searchIndexBeginning.entrySet()) {
                    printWriter.format("\"%d\":{", searchIndexEntry.getKey());
                    for (Map.Entry<String, Set<Integer>> wordMapEntry : searchIndexEntry.getValue().entrySet()) {
                        printWriter.format("\"%s\":[", wordMapEntry.getKey());
                        for (int fileId : wordMapEntry.getValue()) {
        //                    System.out.format("%5d: %20s, %s%n", wordMap.getKey(), fileIdList.getKey(), _fileIndex.get(fileId));
                            printWriter.format("\"%d\",", fileId);
                        }
                        printWriter.print("\"-1\"");  // Dummy data since we have a comma character after each data
                        printWriter.print("],");
                    }
                    printWriter.print("\"\":\"\"");  // Dummy data since we have a comma character after each data
                    printWriter.print("},");
                }
                printWriter.print("\"-1\":\"\"");  // Dummy data since we have a comma character after each data
                printWriter.print("}");

                printWriter.println("}';");
            }


            // End of a word
            if (GENERATE_END_OF_WORD_INDEX) {
                printWriter.print("let searchIndexEnd = '{");
                printWriter.print("\"files\":{");
                for (Map.Entry<Integer, String> entry : _fileIndex.entrySet()) {
                    printWriter.format("\"%d\":[\"%s\",\"%s\"],", entry.getKey(), entry.getValue(), _fileHeaderIndex.get(entry.getKey()));
                }
                printWriter.print("\"-1\":\"\"");  // Dummy data since we have a comma character after each data
                printWriter.print("},");

                printWriter.print("\"words\":{");
                for (Map.Entry<Integer, Map<String, Set<Integer>>> searchIndexEntry : _searchIndexEnd.entrySet()) {
                    printWriter.format("\"%d\":{", searchIndexEntry.getKey());
                    for (Map.Entry<String, Set<Integer>> wordMapEntry : searchIndexEntry.getValue().entrySet()) {
                        printWriter.format("\"%s\":[", wordMapEntry.getKey());
                        for (int fileId : wordMapEntry.getValue()) {
        //                    System.out.format("%5d: %20s, %s%n", wordMap.getKey(), fileIdList.getKey(), _fileIndex.get(fileId));
                            printWriter.format("\"%d\",", fileId);
                        }
                        printWriter.print("\"-1\"");  // Dummy data since we have a comma character after each data
                        printWriter.print("],");
                    }
                    printWriter.print("\"\":\"\"");  // Dummy data since we have a comma character after each data
                    printWriter.print("},");
                }
                printWriter.print("\"-1\":\"\"");  // Dummy data since we have a comma character after each data
                printWriter.print("}");

                printWriter.println("}';");
            }


            // Part of a word
            if (GENERATE_PART_OF_WORD_INDEX) {
                printWriter.print("let searchIndexPart = '{");
                printWriter.print("\"files\":{");
                for (Map.Entry<Integer, String> entry : _fileIndex.entrySet()) {
                    printWriter.format("\"%d\":[\"%s\",\"%s\"],", entry.getKey(), entry.getValue(), _fileHeaderIndex.get(entry.getKey()));
                }
                printWriter.print("\"-1\":\"\"");  // Dummy data since we have a comma character after each data
                printWriter.print("},");

                printWriter.print("\"words\":{");
                for (Map.Entry<Integer, Map<String, Set<Integer>>> searchIndexEntry : _searchIndexPart.entrySet()) {
                    printWriter.format("\"%d\":{", searchIndexEntry.getKey());
                    for (Map.Entry<String, Set<Integer>> wordMapEntry : searchIndexEntry.getValue().entrySet()) {
                        printWriter.format("\"%s\":[", wordMapEntry.getKey());
                        for (int fileId : wordMapEntry.getValue()) {
        //                    System.out.format("%5d: %20s, %s%n", wordMap.getKey(), fileIdList.getKey(), _fileIndex.get(fileId));
                            printWriter.format("\"%d\",", fileId);
                        }
                        printWriter.print("\"-1\"");  // Dummy data since we have a comma character after each data
                        printWriter.print("],");
                    }
                    printWriter.print("\"\":\"\"");  // Dummy data since we have a comma character after each data
                    printWriter.print("},");
                }
                printWriter.print("\"-1\":\"\"");  // Dummy data since we have a comma character after each data
                printWriter.print("}");

                printWriter.println("}';");
            }
        }
    }

    private void generateSearchIndex() throws IOException {
        searchFolder("help/en/html/");
        searchFolder("help/en/package/");
        searchFolder("help/en/manual/");
        createJsonFile();
    }

    @Test
    public void testGenerateSearchIndex() throws IOException {
        Assume.assumeFalse("Ignoring GenerateSearchIndexTest", Boolean.getBoolean("jmri.skipBuildHelpFilesTest"));
        generateSearchIndex();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(GenerateSearchIndexTest.class);

}
