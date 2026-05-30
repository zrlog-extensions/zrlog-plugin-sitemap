package com.zrlog.plugin.sitemap.handle;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.api.IConnectHandler;
import com.zrlog.plugin.data.codec.MsgPacket;

public class ConnectHandler implements IConnectHandler {

    private AutoRefreshSiteMapFileRunnable autoRefreshSiteMapFileRunnable;

    @Override
    public void handler(IOSession ioSession, MsgPacket msgPacket) {
        this.autoRefreshSiteMapFileRunnable = new AutoRefreshSiteMapFileRunnable(ioSession);
    }

    public AutoRefreshSiteMapFileRunnable getAutoRefreshFeedFile() {
        return autoRefreshSiteMapFileRunnable;
    }
}
