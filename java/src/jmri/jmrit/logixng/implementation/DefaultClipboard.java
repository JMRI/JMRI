package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.*;

/**
 * Default implementation of the clipboard
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class DefaultClipboard implements Clipboard {

    private Many clipboardItems = new Many("", null);
    
    private FemaleAnySocket _femaleRootSocket = new DefaultFemaleAnySocket(null, new FemaleSocketListener() {
        @Override
        public void connected(FemaleSocket socket) {
            // Do nothing
        }

        @Override
        public void disconnected(FemaleSocket socket) {
            // Do nothing
        }
    }, "*");
    
    
    @Override
    public boolean isEmpty() {
        return clipboardItems.getChildCount() == 0;
    }

    @Override
    public MaleSocket getTopItem() {
        if (!isEmpty()) {
            MaleSocket maleSocket = clipboardItems.getChild(0).getConnectedSocket();
            clipboardItems.getChild(0).disconnect();
            return maleSocket;
        }
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public FemaleSocket getRoot() {
        return _femaleRootSocket;
    }

    @Override
    public void moveItemToTop(MaleSocket maleSocket) {
        throw new UnsupportedOperationException("Not supported");
    }
    
}
