package jmri.jmrit.catalog;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.gvt.GraphicsNode;

import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

/**
 * NamedIcon that handles SVG images.
 *
 * @author Daniel Bergqvist Copyright (c) 2026
 */
class NamedIconSVG extends NamedIcon {

    private GraphicsNode rootNode;

    /**
     * Create a NamedIcon that is a complete copy of an existing NamedIcon
     *
     * @param pOld Object to copy i.e. copy of the original icon, but NOT a
     *             complete copy of pOld (no transformations done)
     */
    NamedIconSVG(NamedIconSVG pOld) {
        this(pOld.mURL,pOld.mName);
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
    NamedIconSVG(String pUrl, String pName) {
        super(Inherited.Yes);

        this.mURL = pUrl;
        this.mName = pName;

        try {
//            String uri = new File("animated.svg").toURI().toString();
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
            SVGDocument doc = factory.createSVGDocument(mURL);

    // 2. Setup rendering bridge
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext ctx = new BridgeContext(userAgent, loader);
            ctx.setDynamicState(BridgeContext.DYNAMIC); // Crucial for dynamic updates
            GVTBuilder builder = new GVTBuilder();
            rootNode = builder.build(ctx, doc);
/*
    // 3. In your Swing Timer or animation thread:
            javax.swing.Timer timer = new javax.swing.Timer(16, e -> {
                // Modify DOM element by ID
                Element element = doc.getElementById("my-animated-shape");
                if (element != null) {
                    int xPos = 0;   //AAAA
                    element.setAttribute("transform", "translate(" + xPos + ", 0)");
                }

                // Force Batik to re-evaluate the dynamic changes into the GVT tree
                // (For complex updates, you may need to re-assoc or update specific nodes)
    //AAAA            panel.repaint();
            });
            timer.start();
*/
        } catch (IOException e) {
            log.error("Cannot load image", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getRotation() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void setRotation(int pRotation, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public Image createRotatedImage(Image pImage, Component pComponent, int pRotation) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int getDegrees() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public double getScale() {
        return 1.0;
    }

    /** {@inheritDoc} */
    @Override
    public void setLoad(int d, double s, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public void transformImage(int w, int h, AffineTransform t, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public void scale(double scale, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public void rotate(int degree, Component comp) {
    }

    /** {@inheritDoc} */
    @Override
    public double reduceTo(int width, int height, double limit) {
        return 1.0;
    }

    /** {@inheritDoc} */
    @Override
    public void flip(int flip, Component comp) {
    }

    /* * {@inheritDoc} * /
    @Override
    public void setImageObserver(ImageObserver observer) {
        imageIcon.setImageObserver(observer);
    }

    /** {@inheritDoc} */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
//        g2d.scale(5.0, 5.0);
//        g2d.rotate(Math.toRadians(45));
        rootNode.paint(g2d);
        g2d.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public Image getImage() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setImage(Image image) {
    }

    /** {@inheritDoc} */
    @Override
    public int getIconWidth() {
        return (int) Math.round(rootNode.getBounds().getMaxX()) + 1;
    }

    /** {@inheritDoc} */
    @Override
    public int getIconHeight() {
        return (int) Math.round(rootNode.getBounds().getMaxY()) + 1;
    }









/*
    private void test() throws IOException {
// 1. Load document
        String uri = new File("animated.svg").toURI().toString();
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        SVGDocument doc = factory.createSVGDocument(uri);

// 2. Setup rendering bridge
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC); // Crucial for dynamic updates
        GVTBuilder builder = new GVTBuilder();
        GraphicsNode rootNode = builder.build(ctx, doc);

// 3. In your Swing Timer or animation thread:
        javax.swing.Timer timer = new javax.swing.Timer(16, e -> {
            // Modify DOM element by ID
            Element element = doc.getElementById("my-animated-shape");
            if (element != null) {
                int xPos = 0;   //AAAA
                element.setAttribute("transform", "translate(" + xPos + ", 0)");
            }

            // Force Batik to re-evaluate the dynamic changes into the GVT tree
            // (For complex updates, you may need to re-assoc or update specific nodes)
//AAAA            panel.repaint();
        });
        timer.start();
    }

// 4. Inside your JComponent's paintComponent(Graphics g):
//    @Override
    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
//AAAA        rootNode.paint(g2d);
        g2d.dispose();
    }
*/




    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NamedIconSVG.class);

}
