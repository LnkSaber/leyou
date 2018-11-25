package com.leyou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
@ConfigurationProperties(prefix = "ly.upload")
@Data
public class UploadProperties {
    private String baseUrl;
    private List<String> allowTypes;
}
