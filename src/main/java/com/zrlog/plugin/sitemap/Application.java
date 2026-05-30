package com.zrlog.plugin.sitemap;


import com.zrlog.plugin.client.NioClient;
import com.zrlog.plugin.render.SimpleTemplateRender;
import com.zrlog.plugin.sitemap.controller.SiteMapController;
import com.zrlog.plugin.sitemap.handle.AutoRefreshSiteMapFileRunnable;
import com.zrlog.plugin.sitemap.handle.ConnectHandler;
import com.zrlog.plugin.sitemap.service.SiteMapRefreshService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Application {

    private static final ConnectHandler sitemapConnectHandler = new ConnectHandler();

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        List<Class<?>> classList = new ArrayList<>();
        classList.add(SiteMapController.class);
        new NioClient(sitemapConnectHandler, new SimpleTemplateRender(), new SiteMapClientActionHandler())
                .connectServer(args, classList, SiteMapPluginAction.class, SiteMapRefreshService.class);
    }

    public static AutoRefreshSiteMapFileRunnable getAutoRefreshSiteMapFile() {
        return sitemapConnectHandler.getAutoRefreshFeedFile();
    }
}
