package com.zrlog.plugin.sitemap.service;

import com.zrlog.plugin.common.SecurityUtils;
import com.zrlog.plugin.sitemap.vo.Article;
import com.zrlog.plugin.sitemap.vo.SiteMapResultInfo;

import java.util.List;
import java.util.StringJoiner;

public class SiteMapGenerator {

    public static SiteMapResultInfo generateSitemap(String title, String link, String description, List<Article> articles) {
        String language = "zh-cn";

        StringBuilder sitemapContent = new StringBuilder();
        sitemapContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        sitemapContent.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        StringJoiner rawContent = new StringJoiner("\n");
        rawContent.add(title).add(link).add(language).add(description);
        for (Article article : articles) {
            sitemapContent.append("    <url>\n");
            sitemapContent.append("      <loc>").append(article.getLink()).append("</loc>\n");
            sitemapContent.append("      <lastmod>").append(article.getPubDate()).append("</lastmod>\n");
            sitemapContent.append("      <changefreq>").append("monthly").append("</changefreq>\n");
            sitemapContent.append("      <priority>").append("0.8").append("</priority>\n");
            sitemapContent.append("    </url>\n");
            rawContent.add(article.getLink());
            rawContent.add(article.getPubDate());
        }

        sitemapContent.append("</urlset>\n");

        return new SiteMapResultInfo(sitemapContent.toString(), SecurityUtils.md5(rawContent.toString()));
    }
}
