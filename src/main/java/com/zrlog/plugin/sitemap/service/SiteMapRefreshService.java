package com.zrlog.plugin.sitemap.service;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.api.IPluginService;
import com.zrlog.plugin.api.ScheduledCapability;
import com.zrlog.plugin.api.Service;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.message.CapabilityInvokeResult;
import com.zrlog.plugin.sitemap.handle.AutoRefreshSiteMapFileRunnable;

import java.util.HashMap;
import java.util.Map;

@Service("sitemap.refreshSitemap")
@ScheduledCapability(
        key = "sitemap.refreshSitemap",
        label = "刷新站点地图",
        description = "重新生成 sitemap.xml，并同步到配置的静态资源存储。",
        defaultCron = "*/5 * * * *",
        timeoutSeconds = 120
)
public class SiteMapRefreshService implements IPluginService {

    @Override
    public void handle(IOSession session, MsgPacket msgPacket) {
        CapabilityInvokeResult result = new CapabilityInvokeResult();
        Map<String, Object> data = new HashMap<>();
        try {
            new AutoRefreshSiteMapFileRunnable(session).run();
            result.setSuccess(true);
            data.put("message", "Sitemap refresh completed");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            data.put("message", e.getMessage());
        }
        result.setData(data);
        session.sendJsonMsg(result, msgPacket.getMethodStr(), msgPacket.getMsgId(),
                result.isSuccess() ? MsgPacketStatus.RESPONSE_SUCCESS : MsgPacketStatus.RESPONSE_ERROR);
    }
}
