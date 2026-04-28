package com.faisal.dev.atsanalyzer.parser;

import org.springframework.stereotype.Component;

@Component
public class TextCleaner {

    public String clean(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace('\u00A0', ' ')
                .replace('\u2022', '-')
                .replaceAll("[\\u200B-\\u200D\\uFEFF]", "")
                .replaceAll("\\r", "\n")
                .replaceAll("[\\t\\x0B\\f]+", " ")
                .replaceAll(" +", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
