package com.zrlog.plugin.sitemap.vo;

import com.zrlog.plugin.message.Plugin;

public class SiteMapPageData {

    private boolean dark;
    private String colorPrimary;
    private Plugin plugin;
    private SiteMapConfig config;

    public boolean isDark() {
        return dark;
    }

    public void setDark(boolean dark) {
        this.dark = dark;
    }

    public String getColorPrimary() {
        return colorPrimary;
    }

    public void setColorPrimary(String colorPrimary) {
        this.colorPrimary = colorPrimary;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public SiteMapConfig getConfig() {
        return config;
    }

    public void setConfig(SiteMapConfig config) {
        this.config = config;
    }
}
