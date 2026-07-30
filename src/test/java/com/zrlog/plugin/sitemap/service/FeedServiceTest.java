package com.zrlog.plugin.sitemap.service;

import com.google.gson.Gson;
import com.zrlog.plugin.sitemap.vo.ArticleFeedResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FeedServiceTest {

    @Test
    public void shouldResolveUrlFromArticleApiResponse() {
        ArticleFeedResponse response = new Gson().fromJson(
                "{\"data\":{\"rows\":[{\"url\":\"/post.html\","
                        + "\"noSchemeUrl\":\"//blog.example.com/post.html\"}]}}",
                ArticleFeedResponse.class);
        ArticleFeedResponse.ArticleEntry entry = response.rows().get(0);

        assertEquals("https://blog.example.com/post.html",
                FeedService.resolveArticleUrl("https://fallback.example.com",
                        entry.getUrl(), entry.getNoSchemeUrl()));
    }

    @Test
    public void shouldResolveRootRelativeArticleUrlFromConfiguredHomeUrl() {
        assertEquals("https://blog.example.com/post.html",
                FeedService.resolveArticleUrl("https://blog.example.com", "/post.html", null));
    }

    @Test
    public void shouldUseDetectedPublicSchemeAndArticleHost() {
        assertEquals("https://blog.example.com/post.html",
                FeedService.resolveArticleUrl("https://fallback.example.com",
                        "/post.html", "//blog.example.com/post.html"));
    }

    @Test
    public void shouldSupportHttpSitesWithoutHardcodedScheme() {
        assertEquals("http://blog.example.com/post.html",
                FeedService.resolveArticleUrl("http://blog.example.com",
                        "/post.html", "//blog.example.com/post.html"));
    }

    @Test
    public void shouldResolveRelativeArticleUrlWithinConfiguredContextPath() {
        assertEquals("https://example.com/blog/post.html",
                FeedService.resolveArticleUrl("https://example.com/blog", "post.html", null));
    }

    @Test
    public void shouldResolveProtocolRelativeArticleUrlUsingConfiguredScheme() {
        assertEquals("https://static.example.com/post.html",
                FeedService.resolveArticleUrl("https://blog.example.com",
                        "//static.example.com/post.html", null));
    }

    @Test
    public void shouldKeepAbsoluteArticleUrl() {
        assertEquals("https://archive.example.com/post.html",
                FeedService.resolveArticleUrl("https://blog.example.com",
                        "https://archive.example.com/post.html", "//blog.example.com/post.html"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectHomeUrlWithoutHost() {
        FeedService.resolveArticleUrl("/blog", "/post.html", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectBlankArticleUrl() {
        FeedService.resolveArticleUrl("https://blog.example.com", " ", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNonWebArticleUrl() {
        FeedService.resolveArticleUrl("https://blog.example.com", "file:///tmp/post.html", null);
    }
}
