package cis.matrixclient.util.render.font;


import cis.matrixclient.util.render.font.adapter.impl.ClientFontRenderer;
import cis.matrixclient.util.render.font.render.GlyphPageFontRenderer;

import java.util.ArrayList;
import java.util.List;

public class FontRenderers {
    private static final List<ClientFontRenderer> fontRenderers = new ArrayList<>();

    public static ClientFontRenderer getCustomNormal(int size) {
        for (ClientFontRenderer fontRenderer : fontRenderers) {
            if (fontRenderer.getSize() == size) {
                return fontRenderer;
            }
        }
        ClientFontRenderer cfr = new ClientFontRenderer(GlyphPageFontRenderer.createFromID(  "Ubuntu-Regular.ttf", size, false, false, false));
        fontRenderers.add(cfr);
        return cfr;
    }
}
