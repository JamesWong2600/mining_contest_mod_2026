package org.link_uuid.miningContestMod2026.client.HUD;

import fi.iki.elonen.NanoHTTPD;

import java.io.InputStream;

public class LocalWebServer_useless extends NanoHTTPD {
    public LocalWebServer_useless(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri(); // e.g., "/phone.html"
        InputStream in = getClass().getResourceAsStream("/assets/mining-contest-mod-2026/web" + uri);
        if (in == null) return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
        return newChunkedResponse(Response.Status.OK, "text/html", in);
    }
}