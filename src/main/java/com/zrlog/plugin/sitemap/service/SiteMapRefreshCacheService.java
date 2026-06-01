package com.zrlog.plugin.sitemap.service;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.api.Capability;
import com.zrlog.plugin.api.IPluginService;
import com.zrlog.plugin.api.Service;
import com.zrlog.plugin.data.codec.MsgPacket;

@Service("sitemap.refreshCache")
@Capability(
        key = "sitemap.refreshCache",
        type = "event_handler",
        label = "刷新站点地图缓存",
        description = "响应系统缓存刷新事件，重新生成 sitemap.xml 并同步静态资源。",
        exposure = {"runtime_event"},
        riskLevel = "medium",
        timeoutSeconds = 120,
        channel = "system.refreshCache"
)
public class SiteMapRefreshCacheService implements IPluginService {

    @Override
    public void handle(IOSession session, MsgPacket msgPacket) {
        new SiteMapRefreshService().handle(session, msgPacket);
    }
}
