package com.zrlog.plugin.sitemap;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.client.ClientActionHandler;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.sitemap.controller.SiteMapController;

public class SiteMapClientActionHandler extends ClientActionHandler {

    /** Services wait for their owned work; legacy HTTP callbacks do not declare completed view rendering. */
    @Override
    public int capabilityLifecycleVersion() { return 1; }

    @Override
    public void httpMethod(IOSession session, MsgPacket msgPacket) {
        HttpRequestInfo httpRequestInfo = new Gson().fromJson(msgPacket.getDataStr(), HttpRequestInfo.class);
        if (httpRequestInfo.getUri().startsWith("/xml")) {
            new SiteMapController(session, msgPacket, httpRequestInfo).xml();
        } else {
            super.httpMethod(session, msgPacket);
        }
    }

    @Override
    public void refreshCache(IOSession session, MsgPacket msgPacket) {
        Application.getAutoRefreshSiteMapFile().run();
    }
}
