package jmri.jmrit.catalog;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
//import java.awt.image.ColorModel;
//import java.awt.image.MemoryImageSource;
//import java.awt.image.PixelGrabber;
//import java.awt.image.RenderedImage;
import java.io.*;
//import java.net.URL;
//import java.util.Iterator;

import java.net.URL;

import javax.annotation.CheckForNull;
import javax.swing.ImageIcon;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;

import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

public class NamedIconSVG extends NamedIcon {

    private final ImageIcon imageIcon;
    private Document svgImage = null;   // a place to store the original document that is a vector image (svg file)
    private String url;
    private String name;

    /**
     * Create a NamedIcon that is a complete copy of an existing NamedIcon
     *
     * @param pOld Object to copy i.e. copy of the original icon, but NOT a
     *             complete copy of pOld (no transformations done)
     */
    public NamedIconSVG(NamedIconSVG pOld) {
        this(pOld.url,pOld.name);
/*
        svgImage = (Document) pOld.svgImage.cloneNode(true);

        MyTranscoder transcoder = new MyTranscoder();
        try {
            transcoder.transcode(new TranscoderInput(svgImage), null);
        } catch (TranscoderException ex) {
            log.debug("Exception while transposing svg : {}", ex.getMessage());
        }
        imageIcon = new ImageIcon(transcoder.getImage());
*/
    }

    /* *
     * Create a NamedIcon that is really a complete copy of an existing
     * NamedIcon
     *
     * @param pOld Object to copy
     * @param comp the container the new icon is embedded in
     * /
    public NamedIconSVG(NamedIcon pOld, Component comp) {
    }

    /**
     * Create a named icon that includes an image to be loaded from a URL.
     * <p>
     * The default access form is "file:", so a bare pathname to an icon file
     * will also work for the URL argument.
     *
     * @param pUrl  URL of image file to load
     * @param pName Human-readable name for the icon
     */
    public NamedIconSVG(String pUrl, String pName) {
        this.url = pUrl;
        this.name = pName;

        ImageIcon icon;
        try {
            svgImage = createSVGDocument(pUrl);

            MyTranscoder transcoder = new MyTranscoder();
            try {
                transcoder.transcode(new TranscoderInput(svgImage), null);
            } catch (TranscoderException ex) {
                log.debug("Exception while transposing svg : {}", ex.getMessage());
            }
            icon = new ImageIcon(transcoder.getImage());
        } catch (IOException e) {
            log.error("Cannot open svg image file: {}", e.getMessage());
            icon = new ImageIcon("Error");
        }
        imageIcon = icon;
    }

    /**
     * Create a named icon that includes an image to be loaded from a URL.
     *
     * @param pUrl  String-form URL of image file to load
     * @param pName Human-readable name for the icon
     */
    public NamedIconSVG(URL pUrl, String pName) {
        this.url = pUrl.toString();
        this.name = pName;

        ImageIcon icon;
        try {
            svgImage = createSVGDocument(pUrl.toString());

            MyTranscoder transcoder = new MyTranscoder();
            try {
                transcoder.transcode(new TranscoderInput(svgImage), null);
            } catch (TranscoderException ex) {
                log.debug("Exception while transposing svg : {}", ex.getMessage());
            }
            icon = new ImageIcon(transcoder.getImage());
        } catch (IOException e) {
            log.error("Cannot open svg image file: {}", e.getMessage());
            icon = new ImageIcon("Error");
        }
        imageIcon = icon;
    }


    /**
     * Create a named icon from an Image. N.B. NamedIcon's create
     * using this constructor can NOT be animated GIFs
     * @param im Image to use
     */
    public NamedIconSVG(Image im) {
        imageIcon = new NamedIconImage(im);
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(@CheckForNull String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public String getURL() {
        return url;
    }

    /** {@inheritDoc} */
    @Override
    public void setURL(@CheckForNull String url) {
        this.url = url;
    }

    /** {@inheritDoc} */
    @Override
    public int getRotation() {
        return 0;
//        return imageIcon.getRotation();
    }

    /** {@inheritDoc} */
    @Override
    public void setRotation(int pRotation, Component comp) {
//        imageIcon.setRotation(pRotation, comp);
    }

    /** {@inheritDoc} */
    @Override
    public Image createRotatedImage(Image pImage, Component pComponent, int pRotation) {
        return null;
//        return imageIcon.createRotatedImage(pImage, pComponent, pRotation);
    }

    /** {@inheritDoc} */
    @Override
    public int getDegrees() {
        return 0;
//        return imageIcon.getDegrees();
    }

    /** {@inheritDoc} */
    @Override
    public double getScale() {
        return 1.0;
//        return imageIcon.getScale();
    }

    /** {@inheritDoc} */
    @Override
    public void setLoad(int d, double s, Component comp) {
//        imageIcon.setLoad(d, s, comp);
    }

    /** {@inheritDoc} */
    @Override
    public void transformImage(int w, int h, AffineTransform t, Component comp) {
//        imageIcon.transformImage(w, h, t, comp);
    }

    /** {@inheritDoc} */
    @Override
    public void scale(double scale, Component comp) {
//        imageIcon.scale(scale, comp);
    }

    /** {@inheritDoc} */
    @Override
    public void rotate(int degree, Component comp) {
//        imageIcon.rotate(degree, comp);
    }

    /** {@inheritDoc} */
    @Override
    public double reduceTo(int width, int height, double limit) {
        return 1.0;
//        return imageIcon.reduceTo(width, height, limit);
    }

    /** {@inheritDoc} */
    @Override
    public void flip(int flip, Component comp) {
//        imageIcon.flip(flip, comp);
    }

    /* * {@inheritDoc} * /
    @Override
    public void setImageObserver(ImageObserver observer) {
        imageIcon.setImageObserver(observer);
    }

    /** {@inheritDoc} */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        imageIcon.paintIcon(c, g, x, y);
    }

    /** {@inheritDoc} */
    @Override
    public Image getImage() {
        return imageIcon.getImage();
    }

    /** {@inheritDoc} */
    @Override
    public void setImage(Image image) {
        imageIcon.setImage(image);
    }

    /** {@inheritDoc} */
    @Override
    public int getIconWidth() {
        return imageIcon.getIconWidth();
    }

    /** {@inheritDoc} */
    @Override
    public int getIconHeight() {
        return imageIcon.getIconHeight();
    }

    /**
     * Read vector image
     * Use the SAXSVGDocumentFactory to parse the given URI into a DOM.
     *
     * @param uri The path to the SVG file to read.
     * @return A Document instance that represents the SVG file.
     * @throws IOException The file could not be read.
     */
    private Document createSVGDocument( String uri ) throws IOException {
      String parser = XMLResourceDescriptor.getXMLParserClassName();
      SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory( parser );
      return factory.createDocument( uri );
    }


    // to handle svg transformation to displayable images
    private static class MyTranscoder extends ImageTranscoder {
        private BufferedImage image = null;
        @Override
        public BufferedImage createImage(int w, int h) {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            return image;
        }
        public BufferedImage getImage() {
            return image;
        }
        @Override
        public void writeImage(BufferedImage bi, TranscoderOutput to) throws TranscoderException {
            //not required here, do nothing
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NamedIconSVG.class);

}
