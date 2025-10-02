package org.link_uuid.miningContestMod2026.client.HUD;
import org.xhtmlrenderer.swing.Java2DRenderer;

import java.awt.image.BufferedImage;

public class HtmlToImage_useless {

    public static BufferedImage renderHtml(String html, int width, int height) {
        // Java2DRenderer can take HTML directly
        Java2DRenderer renderer = new Java2DRenderer(html, width);
        // Returns a BufferedImage of the rendered HTML
        return renderer.getImage();
    }
}