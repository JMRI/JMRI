package jmri;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Check all the property files used for translations.
 *
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public class CheckPropertyFilesTest {

    enum Level {
        /** Ignore any occurrency */
        Ignore,
        
        /** Only count the number of occurrences */
        OnlyCount,
        
        /** Warn of every occurrency */
        Warn,
        
        /** Erro if occurrency */
        Error,
    }

    /**
     * The same property exists twice in the same file
     */
    private static final Level duplicateProperty_SameFile = Level.Warn;
//    private static final Level duplicateProperty_SameFile = Level.Error;

    // The property exists in two different files, there one of the files is in a sub folder of the folder of the other file. The two properties have the same value.
    /**
     * The same property exists twice in the same file
     */
    private static final Level duplicateProperty_ParentChild_SameValues = Level.Error;
//    private static final Level duplicateProperty_ParentChild_SameValues = Level.Warn;

    // The property exists in two different files, there one of the files is in a sub folder of the folder of the other file. The two properties have different values.
    /**
     * The same property exists twice in the same file
     */
    private static final Level duplicateProperty_ParentChild_DifferentValues = Level.Error;
//    private static final Level duplicateProperty_ParentChild_DifferentValues = Level.Warn;

    /**
     * The property exists in two different files, there one of the files is in a sub folder of the folder of the other file. The two properties are translated different or that one property is translated but not the other.
     */
    private static final Level duplicateProperty_ParentChild_DifferentTranslations = Level.Error;
//    private static final Level duplicateProperty_ParentChild_DifferentTranslations = Level.Warn;

    /**
     * Set of property files to ignore.
     */
    private static final Set<String> filesToIgnore = getFilesToIgnore();


    private static Set<String> getFilesToIgnore() {
        String programPath = FileUtil.getProgramPath().replace('\\', '/');
        Set<String> ignore = new HashSet<>();
        ignore.add(programPath + "java/src/javax.usb.properties");
        return ignore;
    }
    
    
    
    private static class FolderProperties {
        
        private final FolderProperties _parent;
        private final String _fileName;
        private final Map<String, Properties> _properties;
        
        public FolderProperties(FolderProperties parent, String fileName) {
            this._parent = parent;
            this._fileName = fileName;
            this._properties = new HashMap<>();
        }
        
        public boolean addPropertyFile(File file, String lang) throws IOException {
            
            AtomicBoolean testFailed = new AtomicBoolean(false);
            
            Set<String> keys = new HashSet<>();
            
            try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
                
                AtomicBoolean multiLine = new AtomicBoolean(false);
                
                stream.forEach(s -> {
                    // Ignore comment
//                    if (s.trim().startsWith("#")) return;
//                    if (s.trim().isEmpty()) return;
                    
                    if (!s.trim().startsWith("#"))
                    if (!s.trim().isEmpty())
                    if (multiLine.get()) {
                        if (!s.trim().endsWith("\\")) {
                            multiLine.set(false);
                        }
                    } else {
                        if (s.trim().endsWith("\\")) {
                            multiLine.set(true);
                        }
//                        System.out.format("s: %s%n", s);
                        String[] parts = s.split("=", 2);
                        String newKey = parts[0].trim();
//                        System.out.format("File: %s, Key: %s%n", file.getAbsolutePath(), s);
                        if (keys.contains(newKey)) {
                            System.out.format("Key %s already exists%n", newKey);
                            if (duplicateProperty_SameFile == Level.Error) testFailed.set(true);
//                            throw new RuntimeException(String.format("Key %s already exists%n", newKey));
                        }
                        keys.add(newKey);
                    }
                });
            }
            
            try (InputStream input = new FileInputStream(file)) {
                Properties prop = new Properties();
                prop.load(input);
                System.out.format("Added property file: %s, lang: '%s'%n", file.getName(), lang);
                _properties.put(lang, prop);
            }
            
            return !testFailed.get();
        }
        
        /**
         * Check the property.
         * @param lang the language
         * @param key the key
         * @param value the value
         * @return true if success, false if an error
         */
        public boolean checkProperty(String lang, String key, String value) {
            
            return true;
        }
        
        /**
         * Check the properties for a language.
         * @param lang the language
         * @return true if success, false if an error
         */
        public boolean checkProperties(String lang, boolean searchingParent) {
            Map<String, String> foundKeys = new HashMap<>();
//            System.out.format("Aaaaa lang: '%s'%n", lang);
            if (!_properties.containsKey(lang)) {
                System.out.format("ERROR: The lang '%s' is missing%n", lang);
                return false;
            }
            for (Map.Entry<Object, Object> entry : _properties.get(lang).entrySet()) {
                System.out.format("Lang: '%s', key: %s, value: %s%n", lang, entry.getKey(), entry.getValue());
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                if (foundKeys.containsKey(key)) {
                    if (value.equals(foundKeys.get(key))) {
                        System.out.format("Key %s already exists with the same value %s%n", key, value);
                        throw new RuntimeException("DanielAAA");
                    } else {
                        System.out.format("Key %s already exists with a different value %s than the value %s%n", key, foundKeys.get(key), value);
                        if (1==1) throw new RuntimeException("DanielBBB");
                    }
                    return false;
                }
                
                //
                // Search parent too.
                //
                
                foundKeys.put(key, value);
            }
            return true;
        }
        
        /**
         * Check the properties for all languages.
         * @return true if success, false if an error
         */
        public boolean checkProperties() {
            boolean result = true;
            
            // Check base language first
            if (_properties.containsKey("")) {
                result = result && checkProperties("", false);
            }
            
            for (String lang : _properties.keySet()) {
                if (lang.isEmpty()) continue;
                result = result && checkProperties(lang, false);
            }
            return result;
        }
        
    }
    
    
    
    
    

    private boolean searchFolders(
            String rootFolder,
            String folder,
            FolderProperties parentFolderProperties)
            throws IOException {
        
        boolean result = true;
        
        Path path = FileSystems.getDefault().getPath(folder);
        Set<File> files = Stream.of(path.toFile().listFiles())
                  .filter(file -> !file.isDirectory())
                  .collect(Collectors.toSet());

        System.out.format("%n%n%nFolder: %s%n", folder);
        
        Map<String, FolderProperties> folderPropertiesMap = new HashMap<>();
        
        boolean propertiesFound = false;
        
        for (File file : files) {
//            System.out.format("File: %s%n", file.getAbsolutePath().replace('\\', '/'));
            if (filesToIgnore.contains(file.getAbsolutePath().replace('\\', '/'))) {
                System.out.format("Ignoring file %s%n", file.getAbsolutePath());
                continue;
            }
            
//            System.out.format("Not Ignoring file %s%n", file.getAbsolutePath());
            
            String fileName = file.getName();
            if (fileName.endsWith(".properties")) {
                propertiesFound = true;
//                System.out.format("Properties found: File: %s%n", file.getAbsolutePath().replace('\\', '/'));
//                String regex = "(\\w*)Bundle(_\\w\\w)?.properties";

                fileName = fileName.substring(0, fileName.length() - ".properties".length());
                
                String lang = "";
                
                String regex = "^\\w+(_\\w\\w)$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    lang = matcher.group(1);
//                    System.out.format("AA FileName: '%s', lang: '%s'%n", fileName, lang);
                    fileName = fileName.substring(0, fileName.length() - lang.length());
                    matcher = pattern.matcher(fileName);
                    if (matcher.matches()) {
                        lang = matcher.group(1) + lang;
                        fileName = fileName.substring(0, fileName.length() - lang.length());
                    }
                    lang = lang.substring(1);
                }
                
                System.out.format("%nFileName: %s, lang: %s%n", fileName, lang);
                
                FolderProperties folderProperties;
                if (folderPropertiesMap.containsKey(fileName)) {
                    folderProperties = folderPropertiesMap.get(fileName);
                } else {
                    folderProperties = new FolderProperties(parentFolderProperties, fileName);
                    folderPropertiesMap.put(fileName, folderProperties);
                }
                result = result && folderProperties.addPropertyFile(file, lang);
            }
        }
        
        for (FolderProperties folderProperties : folderPropertiesMap.values()) {
            if (propertiesFound) {
                result = result && folderProperties.checkProperties();
            }
            
            Set<String> folders = Stream.of(path.toFile().listFiles())
                      .filter(file -> file.isDirectory())
                      .map(File::getName)
                      .collect(Collectors.toSet());
            
            for (String aFolder : folders) {
                result = result
                        && searchFolders(rootFolder, folder + aFolder + "/", folderProperties);
            }
        }
        
        return result;
    }

    @Test
    public void testProperties() throws IOException {
        String folder = FileUtil.getProgramPath() + "java/src/";
        System.out.format("Program path: %s%n", FileUtil.getProgramPath());
        System.out.format("folder: %s%n", folder);
        Assert.assertTrue("All properties files are valid", searchFolders(folder, folder, null));
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CheckPropertyFilesTest.class);
}
