# zrlog-plugin-sitemap

ZrLog 站点地图插件。生成 `sitemap.xml`，并可在站点侧边栏显示站点地图入口。

## 功能

- 配置 sitemap 文件路径
- 配置站点地图入口说明文字
- 按文章列表生成 sitemap 文件
- 可将站点地图文件同步到静态资源存储

## Sitemap 协议

https://www.sitemaps.org/protocol.html

## 构建

```shell
export JAVA_HOME=${HOME}/dev/graalvm-jdk-latest
export PATH=${JAVA_HOME}/bin:$PATH
```

## 原生制品发布

Linux amd64/arm64 制品在上传前会调用 `zrlog-artifact-service`，通过与 `plugin-core`
相同的固定版本 `process-artifact` Action 完成压缩和 SHA-256、文件大小校验。
处理成功后才会生成最终制品的 MD5 并上传；处理失败会停止该平台的发布。
服务接收的版本号使用 `bin/build-info.sh` 生成的实际插件版本。

发布前需要配置 Actions Secret `ARTIFACT_SERVICE_TOKEN`，可在仓库中单独设置，
或授权该仓库使用同名组织 Secret。服务地址为 `https://webdav.zrlog.com/artifact`。
