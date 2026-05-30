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
import com.zrlog.plugin.type.ActionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        session.sendMsg(new MsgPacket(requestInfo.simpleParam(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(), ActionType.SET_WEBSITE.name()), msgPacket -> {
            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            session.sendMsg(new MsgPacket(map, ContentType.JSON, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(), requestPacket.getMethodStr()));
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
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", CONFIG_KEYS);
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            Map map = gson.fromJson(msgPacket.getDataStr(), Map.class);
            if (Objects.isNull(map.get("uriPath"))) {
                map.put("uriPath", AutoRefreshSiteMapFileRunnable.DEFAULT_URI_PATH);
            }
            if (Objects.isNull(map.get("sitemapText"))) {
                map.put("sitemapText", "");
            }
            map.put("target", requestInfo.simpleParam().containsKey("preview") ? "_blank" : "_top");
            session.responseHtml("/widget", map, requestPacket.getMethodStr(), requestPacket.getMsgId());
        });
    }

    public void xml() {
        session.responseXmlStr(Application.getAutoRefreshSiteMapFile().doFeed(), requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    private Map<String, Object> pageData() {
        Map<String, Object> data = new HashMap<>();
        data.put("dark", isDarkMode());
        data.put("colorPrimary", getAdminColorPrimary());
        data.put("plugin", session.getPlugin());
        data.put("config", loadConfig());
        return successMap(data);
    }

    private Map<String, Object> loadConfig() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", CONFIG_KEYS);
        Map response = session.getResponseSync(ContentType.JSON, keyMap, ActionType.GET_WEBSITE, Map.class);
        Map<String, Object> config = response == null ? new HashMap<>() : new HashMap<>(response);
        if (Objects.isNull(config.get("uriPath"))) {
            config.put("uriPath", AutoRefreshSiteMapFileRunnable.DEFAULT_URI_PATH);
        }
        if (Objects.isNull(config.get("sitemapText"))) {
            config.put("sitemapText", "");
        }
        config.put("version", session.getPlugin().getVersion());
        return config;
    }

    private Map<String, Object> successMap(Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("data", data);
        return map;
    }

    private void response(Map<String, Object> map) {
        session.sendMsg(ContentType.JSON, map, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    private boolean isDarkMode() {
        return requestInfo.isDarkMode();
    }

    private String getAdminColorPrimary() {
        return requestInfo.getAdminColorPrimary();
    }
}
