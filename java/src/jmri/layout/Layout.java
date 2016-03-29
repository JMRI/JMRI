package jmri.layout;

import java.util.TreeMap;

/**
 * @author Alex Shepherd Copyright (c) 2002
 */
public class Layout implements LayoutEventListener, LayoutEventInterface {

    private TreeMap<LayoutAddress, LayoutElement> mElementMap = new TreeMap<LayoutAddress, LayoutElement>();
    private LayoutElement mRootElement = new LayoutElement("Layouts", null);

    public Layout(String pHostname) {
        mRootElement = new LayoutElement(null);
    }

    public LayoutElement getLayoutTree() {
        return mRootElement;
    }

    public void addEventListener(LayoutEventListener pListener) {
        mRootElement.addEventListener(pListener);
    }

    public void removeEventListener(LayoutEventListener pListener) {
        mRootElement.removeEventListener(pListener);
    }

    public void message(LayoutEventData pLayoutEvent) {
        LayoutAddress vAddress = pLayoutEvent.getLayoutAddress();
        LayoutElement vElement = mElementMap.get(vAddress);
        if (vElement == null) {
            LayoutElement vParentElement = mRootElement;
            String vLayoutName = vAddress.getLayoutName();
            int vChildCount = vParentElement.getChildCount();
            for (int vChildIndex = 0; vChildIndex < vChildCount; vChildIndex++) {
                vElement = (LayoutElement) vParentElement.getChildAt(vChildIndex);
                if (vElement.getAddress().getLayoutName().equals(vLayoutName)) {
                    break;
                }

                vElement = null;
            }

            if (vElement == null) {
                vElement = new LayoutElement(vLayoutName, vAddress);
                vParentElement.add(vElement);
            }

            vParentElement = vElement;
            vElement = null;
            vChildCount = vParentElement.getChildCount();
            int vType = vAddress.getType();
            for (int vChildIndex = 0; vChildIndex < vChildCount; vChildIndex++) {
                vElement = (LayoutElement) vParentElement.getChildAt(vChildIndex);
                if (vElement.getAddress().getType() == vType) {
                    break;
                }

                vElement = null;
            }

            if (vElement == null) {
                vElement = new LayoutElement(LayoutAddress.getTypeDescription(vType), vAddress);
                vParentElement.add(vElement);
            }

            vParentElement = vElement;
            vElement = null;
            vChildCount = vParentElement.getChildCount();
            int vOffset = vAddress.getOffset();
            for (int vChildIndex = 0; vChildIndex < vChildCount; vChildIndex++) {
                vElement = (LayoutElement) vParentElement.getChildAt(vChildIndex);
                if (vElement.getAddress().getOffset() == vOffset) {
                    break;
                }

                vElement = null;
            }

            if (vElement == null) {
                vElement = new LayoutElement(Integer.toString(vOffset), vAddress);
                vParentElement.add(vElement);
            }

            log("Added: " + vAddress);
        }

        vElement.setData(pLayoutEvent);
    }

    private void log(String pMessage) {
        System.out.println(pMessage);
    }
}
