package com.faisal.dev.atsanalyzer.scoring.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.faisal.dev.atsanalyzer.scoring.config.DomainPackDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DomainPackRegistry {

    private static final String DOMAIN_RESOURCE_PATTERN =
            "classpath*:scoring/domains/*.json";

    private final Map<String, DomainPackDefinition>
            domainPacksByKey;

    public DomainPackRegistry(
            ObjectMapper objectMapper
    ) {

        this.domainPacksByKey = loadDomainPacks(objectMapper);
    }

    public List<DomainPackDefinition> activePacks() {

        return domainPacksByKey.values()
                .stream()
                .filter(pack -> !pack.fallback())
                .toList();
    }

    public DomainPackDefinition fallbackPack() {

        return domainPacksByKey.values()
                .stream()
                .filter(DomainPackDefinition::fallback)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Missing fallback domain pack definition"
                        )
                );
    }

    private Map<String, DomainPackDefinition> loadDomainPacks(
            ObjectMapper objectMapper
    ) {

        try {

            Resource[] resources =
                    new PathMatchingResourcePatternResolver()
                            .getResources(
                                    DOMAIN_RESOURCE_PATTERN
                            );

            if (resources.length == 0) {
                throw new IllegalStateException(
                        "No domain pack definitions found under scoring/domains"
                );
            }

            Map<String, DomainPackDefinition> domainPacks =
                    new LinkedHashMap<>();

            List<Resource> sortedResources =
                    List.of(resources)
                            .stream()
                            .sorted(Comparator.comparing(
                                    Resource::getFilename,
                                    Comparator.nullsLast(
                                            Comparator.naturalOrder()
                                    )
                            ))
                            .toList();

            for (Resource resource : sortedResources) {

                try (InputStream inputStream =
                             resource.getInputStream()) {

                    DomainPackDefinition domainPack =
                            objectMapper.readValue(
                                    inputStream,
                                    DomainPackDefinition.class
                            );

                    domainPacks.put(
                            domainPack.key(),
                            domainPack
                    );
                }
            }

            log.info(
                    "Loaded {} ATS domain packs.",
                    domainPacks.size()
            );

            return Map.copyOf(domainPacks);

        } catch (IOException ex) {
            throw new UncheckedIOException(
                    "Failed to load ATS domain packs",
                    ex
            );
        }
    }
}
