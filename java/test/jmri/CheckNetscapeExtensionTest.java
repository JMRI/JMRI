package jmri;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks that all the GIF images in the JMRI distribution either has:
 * * only one frame and not the Netscape extension
 * * or more than one frame and the Netscape extension
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class CheckNetscapeExtensionTest {

    private static final Set<String> IMAGES_TO_IGNORE = new HashSet<>();

    // Does this image has the Netscape extension?
    private boolean hasNetscapeExtension(ImageReader gifReader) throws IOException {
        IIOMetadata metaData = gifReader.getImageMetadata(0);
        Node tree = metaData.getAsTree("javax_imageio_gif_image_1.0");

        // Try to find the node "ApplicationExtensions"
        Node applicationExtensions = null;
        NodeList nodeList = tree.getChildNodes();
        for (int child=0; child < nodeList.getLength(); child++) {
            var tstNode = nodeList.item(child);
            if ( tstNode !=null && tstNode.getNodeName().equals("ApplicationExtensions")) {
                applicationExtensions = tstNode;
            }
        }
        if (applicationExtensions == null) return false;

        // Loop thru the extensions to try to find the "NETSCAPE" extension
        nodeList = applicationExtensions.getChildNodes();
        for (int child=0; child < nodeList.getLength(); child++) {
            var tstNode = nodeList.item(child);
            if ( tstNode != null && tstNode.getNodeName().equals("ApplicationExtension")) {
                org.w3c.dom.NamedNodeMap map = tstNode.getAttributes();
                if ( map != null ) {
                    for (int i=0; i < map.getLength(); i++) {
                        var mapItem = map.item(i);
                        if ( mapItem != null && mapItem.getNodeName().equals("applicationID")
                                && "NETSCAPE".equals(mapItem.getNodeValue())) {
                            return true;
                        }
                    }
                }
            }
        }

        // The Netscape extension is not found
        return false;
    }

    private void checkFileForNetscapeExtension(File file) throws IOException {
        Iterator<ImageReader> rIter = ImageIO.getImageReadersByFormatName("gif");
        ImageReader gifReader = rIter.next();

        try (InputStream is = file.toURI().toURL().openStream()) {
            ImageInputStream iis = ImageIO.createImageInputStream(is);
            gifReader.setInput(iis, false);

            ImageReaderSpi spiProv = gifReader.getOriginatingProvider();
            if (spiProv != null && spiProv.canDecodeInput(iis)) {

                int numFrames = gifReader.getNumImages(true);
                boolean hasNetscapeExtension = hasNetscapeExtension(gifReader);

                if (numFrames == 1 && hasNetscapeExtension) {
                    log.error("GIF image {} has one frame and the Netscape extension", file.getAbsolutePath());
                }
                if (numFrames > 1 && !hasNetscapeExtension) {
                    log.error("GIF image {} has more than one frame and not the Netscape extension", file.getAbsolutePath());
                }
            }
        }
    }

    @Test
    public void testGifFiles() throws IOException {
        java.nio.file.Path path = FileSystems.getDefault().getPath("resources/");

        List<File> files = new ArrayList<>();
        Files.walk(path)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".gif"))
                .forEach(p -> { files.add(p.toFile()); });

        for (File file : files) {
            if (! IMAGES_TO_IGNORE.contains(file.getAbsolutePath())) {
                checkFileForNetscapeExtension(file);
            } else {
                log.info("Ignore image {}", file.getAbsolutePath());
            }
        }
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();

        IMAGES_TO_IGNORE.add(FileSystems.getDefault().getPath("resources/icons/RGB-animated-once-Square.gif").toFile().getAbsolutePath());
        IMAGES_TO_IGNORE.add(FileSystems.getDefault().getPath("resources/icons/RGB-animated-once-Square2.gif").toFile().getAbsolutePath());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CheckNetscapeExtensionTest.class);
}
