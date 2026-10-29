package com.wpits.accountmanagement.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class FieldFilterUtil {

    private static final String ALWAYS_INCLUDED_FIELD = "id";

    private final ObjectMapper objectMapper;

    public JsonNode applyFieldSelection(Object dto, String fields, Set<String> defaultFields) {
        JsonNode node = objectMapper.valueToTree(dto);

        if (!(node instanceof ObjectNode objectNode)) {
            return node;
        }

        Set<String> selectedFields = StringUtils.hasText(fields) ? parseFields(fields) : defaultFields;

        ObjectNode result = objectMapper.createObjectNode();
        selectedFields.forEach(field -> {
            if (objectNode.has(field)) {
                result.set(field, objectNode.get(field));
            }
        });

        return result;
    }

    private Set<String> parseFields(String fields) {
        List<String> requested = Arrays.stream(fields.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        Set<String> result = new LinkedHashSet<>(requested);
        result.add(ALWAYS_INCLUDED_FIELD);
        return result;
    }
}
