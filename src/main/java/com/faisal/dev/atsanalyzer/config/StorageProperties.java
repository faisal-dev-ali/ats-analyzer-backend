package com.faisal.dev.atsanalyzer.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "app.storage")
@Validated
@Getter
@Setter
public class StorageProperties {

    @NotBlank
    private String uploadDir;

    @Min(1)
    private long maxFileSizeBytes;

    @NotEmpty
    private Set<String> allowedExtensions =
            new LinkedHashSet<>();

    @NotEmpty
    private Set<String> allowedContentTypes =
            new LinkedHashSet<>();
}
