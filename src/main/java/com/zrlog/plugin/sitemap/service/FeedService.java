package com.zrlog.plugin.sitemap.service;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.sitemap.vo.Article;
import com.zrlog.plugin.sitemap.vo.ArticleFeedResponse;
import com.zrlog.plugin.sitemap.vo.SiteMapResultInfo;
import com.zrlog.plugin.type.ActionType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class FeedService {

    private final IOSession session;

    public FeedService(IOSession session) {
        this.session = session;
    }

    public SiteMapResultInfo feed() {
        PublicInfo publicInfo = session.getResponseSync(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO, PublicInfo.class);
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            String publicHomeUrl = resolvePublicHomeUrl(httpClient, publicInfo.getHomeUrl());
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(publicInfo.getApiHomeUrl() + "/api/article?size=50000&feed=true")).build();
            HttpResponse<byte[]> send = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            ArticleFeedResponse info = new Gson().fromJson(new String(send.body()), ArticleFeedResponse.class);
            List<Article> articles = new ArrayList<>();
            info.rows().forEach(e -> {
                String pubDate = e.getReleaseTime();
                articles.add(new Article(e.getTitle(), resolveArticleUrl(publicHomeUrl,
                                e.getUrl(), e.getNoSchemeUrl()),
                        Objects.requireNonNullElse(e.getContent(), ""), pubDate, e.idText()));
            });
            //httpClient.close();
            return SiteMapGenerator.generateSitemap(publicInfo.getTitle(), publicInfo.getHomeUrl(), "", articles);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static String resolvePublicHomeUrl(HttpClient httpClient, String homeUrl) throws InterruptedException {
        URI homeUri = requireAbsoluteWebUrl(URI.create(homeUrl.trim()), homeUrl);
        HttpRequest request = HttpRequest.newBuilder(homeUri)
                .timeout(Duration.ofSeconds(10))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            URI detectedUri = requireAbsoluteWebUrl(
                    httpClient.send(request, HttpResponse.BodyHandlers.discarding()).uri(), homeUrl);
            return new URI(detectedUri.getScheme(), homeUri.getAuthority(), homeUri.getPath(), null, null).toString();
        } catch (IOException | URISyntaxException e) {
            return homeUri.toString();
        }
    }

    static String resolveArticleUrl(String homeUrl, String articleUrl, String noSchemeUrl) {
        if (homeUrl == null || homeUrl.isBlank()) {
            throw new IllegalArgumentException("Website home URL is required");
        }
        if ((articleUrl == null || articleUrl.isBlank()) && (noSchemeUrl == null || noSchemeUrl.isBlank())) {
            throw new IllegalArgumentException("Article URL is required");
        }

        URI homeUri = requireAbsoluteWebUrl(URI.create(homeUrl.trim()), homeUrl);

        URI articleUri = articleUrl == null || articleUrl.isBlank() ? null : URI.create(articleUrl.trim());
        if (articleUri != null && articleUri.isAbsolute()) {
            return requireAbsoluteWebUrl(articleUri, articleUrl).toString();
        }

        String candidateUrl = noSchemeUrl == null || noSchemeUrl.isBlank() ? articleUrl : noSchemeUrl;
        URI candidateUri = URI.create(candidateUrl.trim());
        if (candidateUri.isAbsolute()) {
            return requireAbsoluteWebUrl(candidateUri, candidateUrl).toString();
        }

        String basePath = Objects.requireNonNullElse(homeUri.getPath(), "");
        if (basePath.isEmpty()) {
            basePath = "/";
        } else if (!basePath.endsWith("/")) {
            basePath += "/";
        }

        try {
            URI baseUri = new URI(homeUri.getScheme(), homeUri.getAuthority(), basePath, null, null);
            return requireAbsoluteWebUrl(baseUri.resolve(candidateUri), candidateUrl).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Website home URL is invalid: " + homeUrl, e);
        }
    }

    private static URI requireAbsoluteWebUrl(URI uri, String sourceUrl) {
        if (!uri.isAbsolute() || uri.getAuthority() == null
                || (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalArgumentException("Article URL could not be resolved: " + sourceUrl);
        }
        return uri;
    }
}
