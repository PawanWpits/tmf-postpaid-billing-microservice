package com.wpits.customerbill.utils;

import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Applies TMF REST "attribute selection" semantics: given a fully-populated
 * response DTO and a comma-separated {@code fields} query parameter, returns
 * a JSON tree pruned to only the requested top-level attributes.
 *
 * - If {@code fields} is provided -> return exactly those fields (+ "id", always included).
 * - If {@code fields} is absent/blank -> return the DEFAULT_FIELDS summary view
 *   (id, href, billingDate, @type) instead of the full object, matching the
 *   TMF "list" convention of a lightweight default representation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FieldFilterUtil {

    private static final String ALWAYS_INCLUDED_FIELD = "id";

    /** Default summary fields returned when no `fields` query param is supplied. */
    private static final Set<String> DEFAULT_FIELDS =
            Set.of("id", "href", "billingDate", "@type");

    private final JsonMapper objectMapper;

    /**
     * @param dto    the fully populated response DTO
     * @param fields raw {@code fields} query param value (nullable/blank => DEFAULT_FIELDS applied)
     * @return a JsonNode representing the pruned resource
     */
    public JsonNode applyFieldSelection(Object dto,
            String fields,
            Set<String> defaultFields) {

JsonNode node = objectMapper.valueToTree(dto);

if (!(node instanceof ObjectNode)) {
return node;
}
ObjectNode objectNode = (ObjectNode) node;

Set<String> selectedFields;

if (StringUtils.hasText(fields)) {
selectedFields = parseFields(fields);
} else {
selectedFields = defaultFields;
}

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