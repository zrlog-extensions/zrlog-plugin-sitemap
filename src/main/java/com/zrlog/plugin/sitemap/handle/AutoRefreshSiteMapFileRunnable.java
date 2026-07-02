package com.zrlog.plugin.sitemap.handle;

import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.common.IOUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.model.BlogRunTime;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.sitemap.service.FeedService;
import com.zrlog.plugin.sitemap.vo.SiteMapConfig;
import com.zrlog.plugin.sitemap.vo.SiteMapResultInfo;
import com.zrlog.plugin.type.ActionType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class AutoRefreshSiteMapFileRunnable implements Runnable {

    public static final String DEFAULT_URI_PATH = "/sitemap.xml";

    private static final Logger LOGGER = LoggerUtil.getLogger(AutoRefreshSiteMapFileRunnable.class);

    private final IOSession ioSession;
    private String uploadedFeedVersion;

    public AutoRefreshSiteMapFileRunnable(IOSession ioSession) {
        this.ioSession = ioSession;
    }

    @Override
    public void run() {
        SiteMapResultInfo feed = new FeedService(ioSession).feed();
        if (Objects.equals(uploadedFeedVersion, feed.getVersion())) {
            return;
        }
        doHandle(feed);
    }

    private String doHandle(SiteMapResultInfo feed){
        uploadedFeedVersion = feed.getVersion();
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "uriPath");
        SiteMapConfig config = ioSession.getResponseSync(ContentType.JSON, keyMap, ActionType.GET_WEBSITE, SiteMapConfig.class);
        String uriPath = config == null || config.getUriPath() == null || config.getUriPath().trim().isEmpty()
                ? DEFAULT_URI_PATH
                : config.getUriPath();
        String path = ioSession.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.BLOG_RUN_TIME, BlogRunTime.class).getPath();
        File sitemapFile = new File(path + uriPath);
        sitemapFile.getParentFile().mkdirs();
        IOUtil.writeBytesToFile(feed.getContent().getBytes(), sitemapFile);
        try {
            Map<String, String[]> map = new HashMap<>();
            map.put("fileInfo", new String[]{sitemapFile + ",/" + sitemapFile.getName() + ",true"});
            ioSession.requestService("uploadService", map);
        } catch (Exception e) {
            LOGGER.warning("upload to service failed " + e.getMessage());
        }
        return feed.getContent();
    }

    public String doFeed(){
        SiteMapResultInfo feed = new FeedService(ioSession).feed();
        return doHandle(feed);
    }
}
