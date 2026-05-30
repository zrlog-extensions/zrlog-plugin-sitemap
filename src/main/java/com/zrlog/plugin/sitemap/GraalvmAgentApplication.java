package com.zrlog.plugin.sitemap;

import com.zrlog.plugin.sitemap.controller.SiteMapController;
import com.zrlog.plugin.common.PluginNativeImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class GraalvmAgentApplication {


    public static void main(String[] args) throws IOException {
        String basePath = System.getProperty("user.dir").replace("\\target","").replace("/target", "");
        //PathKit.setRootPath(basePath);
        File file = new File(basePath + "/src/main/resources");
        PluginNativeImageUtils.doLoopResourceLoad(file.listFiles(), file.getPath()  + "/", "/");
        //Application.nativeAgent = true;
        PluginNativeImageUtils.exposeController(Collections.singletonList(SiteMapController.class));
        PluginNativeImageUtils.usedGsonObject();
        Application.main(args);

    }
}