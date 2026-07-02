package com.zrlog.plugin.sitemap.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.sitemap.Application;
import com.zrlog.plugin.sitemap.handle.AutoRefreshSiteMapFileRunnable;
import com.zrlog.plugin.sitemap.vo.SiteMapApiResponse;
import com.zrlog.plugin.sitemap.vo.SiteMapConfig;
import com.zrlog.plugin.sitemap.vo.SiteMapPageData;
import com.zrlog.plugin.sitemap.vo.WebsiteKeyRequest;
import com.zrlog.plugin.type.ActionType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SiteMapController {

    private static final String CONFIG_KEYS = "uriPath,sitemapText";

    private final IOSession session;
    private final MsgPacket requestPacket;
    private final HttpRequestInfo requestInfo;
    private final Gson gson = new Gson();

    public SiteMapController(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    public void update() {
        session.sendMsg(new MsgPacket(normalizeConfig(configFromParams()), ContentType.JSON,
                MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(), ActionType.SET_WEBSITE.name()), msgPacket -> {
            response(SiteMapApiResponse.success(Boolean.TRUE));
            Application.getAutoRefreshSiteMapFile().doFeed();
            session.sendJsonMsg(new HashMap<>(), ActionType.REFRESH_CACHE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST);
        });
    }

    public void info() {
        response(loadConfig());
    }

    public void index() {
        Map<String, Object> data = new HashMap<>();
        data.put("theme", isDarkMode() ? "dark" : "light");
        data.put("data", gson.toJson(pageData()));
        session.responseHtml("/templates/index", data, requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    public void json() {
        response(pageData());
    }

    public void widget() {
        session.sendJsonMsg(WebsiteKeyRequest.of(CONFIG_KEYS), ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            Map<String, Object> data = configMap(normalizeConfig(gson.fromJson(msgPacket.getDataStr(), SiteMapConfig.class)));
            data.put("target", hasParam("preview") ? "_blank" : "_top");
            session.responseHtml("/widget", data, requestPacket.getMethodStr(), requestPacket.getMsgId());
        });
    }

    public void xml() {
        session.responseXmlStr(Application.getAutoRefreshSiteMapFile().doFeed(), requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    private SiteMapApiResponse<SiteMapPageData> pageData() {
        SiteMapPageData data = new SiteMapPageData();
        data.setDark(isDarkMode());
        data.setColorPrimary(getAdminColorPrimary());
        data.setPlugin(session.getPlugin());
        data.setConfig(loadConfig());
        return SiteMapApiResponse.success(data);
    }

    private SiteMapConfig loadConfig() {
        SiteMapConfig config = session.getResponseSync(ContentType.JSON, WebsiteKeyRequest.of(CONFIG_KEYS), ActionType.GET_WEBSITE, SiteMapConfig.class);
        config = normalizeConfig(config);
        config.setVersion(session.getPlugin().getVersion());
        return config;
    }

    private SiteMapConfig configFromParams() {
        SiteMapConfig config = new SiteMapConfig();
        config.setUriPath(paramValue("uriPath"));
        config.setSitemapText(paramValue("sitemapText"));
        return config;
    }

    private SiteMapConfig normalizeConfig(SiteMapConfig config) {
        if (config == null) {
            config = new SiteMapConfig();
        }
        if (config.getUriPath() == null || config.getUriPath().trim().isEmpty()) {
            config.setUriPath(AutoRefreshSiteMapFileRunnable.DEFAULT_URI_PATH);
        }
        if (config.getSitemapText() == null) {
            config.setSitemapText("");
        }
        return config;
    }

    private Map<String, Object> configMap(SiteMapConfig config) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uriPath", config.getUriPath());
        map.put("sitemapText", config.getSitemapText());
        if (config.getVersion() != null) {
            map.put("version", config.getVersion());
        }
        return map;
    }

    private void response(Object data) {
        session.sendMsg(ContentType.JSON, data, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    private boolean hasParam(String key) {
        return requestInfo.getParam() != null && requestInfo.getParam().containsKey(key);
    }

    private String paramValue(String key) {
        if (requestInfo.getParam() == null || requestInfo.getParam().get(key) == null || requestInfo.getParam().get(key).length == 0) {
            return "";
        }
        return requestInfo.getParam().get(key)[0];
    }

    private boolean isDarkMode() {
        return requestInfo.isDarkMode();
    }

    private String getAdminColorPrimary() {
        return requestInfo.getAdminColorPrimary();
    }
}
