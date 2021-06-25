package jmri.util;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.junit.jupiter.api.*;

/**
 * Generates the search index for local help files.
 * 
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class GenerateSearchIndexTest {

    private final Map<Integer, String> _fileIndex = new HashMap<>();
    private final Map<Integer, Map<String, Set<Integer>>> _searchIndex = new HashMap<>();
    private int _currentFileId = 0;
    
    
    private void addWord(String word, int fileId) {
        if (word.length() < 3) return;
        
        for (int i=3; i <= word.length(); i++) {
            Map<String, Set<Integer>> wordMap = _searchIndex.get(i);
            if (wordMap == null) {
                wordMap = new HashMap<>();
                _searchIndex.put(i, wordMap);
            }
            for (int j=0; j <= word.length()-i; j++) {
                String part = word.substring(j, i+j);
                Set<Integer> fileIdList = wordMap.get(part);
                if (fileIdList == null) {
                    fileIdList = new HashSet<>();
                    wordMap.put(part, fileIdList);
                }
                fileIdList.add(fileId);
            }
        }
    }
    
    private void parseNode(Node node, String pad) {
        for (Node child : node.childNodes()) {
//            System.out.format("%s%s, %s%n", pad, child.nodeName(), child.getClass().getName());
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode)child;
                String text = textNode.getWholeText().toLowerCase();
                String[] parts = text.split("\\W+");
                for (String s : parts) {
                    addWord(s, _currentFileId);
                }
//                System.out.format("%sText: %s%n", pad, text.getWholeText());
//                System.out.println(text.getWholeText().trim());
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
//                Document doc = Jsoup.parse(html);
                Element body = doc.body();
                parseNode(body, "");
                _currentFileId++;
//                System.out.format("%n============================================================%n%n");
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
//        String newLine = System.getProperty("line.separator");
        FileWriter fileWriter = new FileWriter(FileUtil.getProgramPath() + "help/en/local/search.json");
        PrintWriter printWriter = new PrintWriter(fileWriter);
//        printWriter.println("<!DOCTYPE html>");
//        printWriter.println("<html lang=\"en\">");
//        printWriter.println("{files:[");
        printWriter.print("let searchIndex = '{");
        printWriter.print("\"files\":{");
        for (Map.Entry<Integer, String> entry : _fileIndex.entrySet()) {
            printWriter.format("\"%d\":\"%s\",", entry.getKey(), entry.getValue());
        }
        printWriter.print("\"-1\":\"\"");  // Dummy data since we have a comma character after each data
        printWriter.print("},");
        
        printWriter.print("\"words\":{");
        for (Map.Entry<Integer, Map<String, Set<Integer>>> searchIndexEntry : _searchIndex.entrySet()) {
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

        printWriter.print("}';");
        printWriter.close();
    }
    
    @Test
    public void testGenerateSearchIndex() throws IOException {
        searchFolder("help/en/");
        
        
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
//        Map<String, java.util.concurrent.atomic.AtomicInteger> wordFileCount = new HashMap<>();
        
        int count = 0;
        for (Map.Entry<Integer, Map<String, Set<Integer>>> wordMap : _searchIndex.entrySet()) {
            for (Map.Entry<String, Set<Integer>> fileIdList : wordMap.getValue().entrySet()) {
                int wfc = 0;
                for (int fileId : fileIdList.getValue()) {
//                    System.out.format("%5d: %20s, %s%n", wordMap.getKey(), fileIdList.getKey(), _fileIndex.get(fileId));
                    count++;
                    wfc++;
                }
//                java.util.concurrent.atomic.AtomicInteger ai = wordFileCount.get(fileIdList.getKey());
//                if (ai == null) {
//                    ai = new java.util.concurrent.atomic.AtomicInteger();
//                    wordFileCount.put(fileIdList.getKey(), ai);
//                }
//                ai.addAndGet(wfc);
                Map.Entry<String, Integer> entry = new HashMap.SimpleEntry<>(fileIdList.getKey(), wfc);
                list.add(entry);
            }
        }
        
        Collections.sort(list, (a,b) -> {return a.getValue().compareTo(b.getValue());});
        
        for (Map.Entry<String, Integer> entry : list) {
            System.out.format("%8d: %s%n", entry.getValue(), entry.getKey());
        }
        
        System.out.format("Word count: %d%n", count);
        
        createJsonFile();
        
//        String html = "<html><head><title>First parse</title></head>"
//                + "<body><p>Parsed HTML into a doc.</p></body></html>";
//        Document doc = Jsoup.parse(html);
//        Element body = doc.body();
//        parseNode(body, "");
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
