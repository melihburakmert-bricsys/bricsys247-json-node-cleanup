package com.chapoo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class JsonNodeCleanup {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void removeJsonNode(final String key, final ObjectNode node) {
        node.remove(key);

        final Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            final Map.Entry<String, JsonNode> entry = fields.next();
            final JsonNode child = entry.getValue();
            if (child.isObject()) {
                removeJsonNode(key, (ObjectNode) child);
            } else if (child.isArray()) {
                for (final JsonNode element : child) {
                    if (element.isObject()) {
                        removeJsonNode(key, (ObjectNode) element);
                    }
                }
            }
        }
    }

    public static void processFile(final String key, final File file) throws IOException {
        final JsonNode root = mapper.readTree(file);
        if (root.isObject()) {
            removeJsonNode(key, (ObjectNode) root);
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
    }

    public static void main(final String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java -jar json-node-cleanup-xx.jar <json-node-key> <directory_with_json_files>");
            System.exit(1);
        }
        final String key = args[0];
        if (key.isEmpty()) {
            System.err.println("The provided JSON node key is empty.");
            System.exit(1);
        }
        final File dir = new File(args[1]);
        if (!dir.isDirectory()) {
            System.err.println("The provided path is not a directory: " + args[0]);
            System.exit(1);
        }
        final File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (final File f : files) {
                System.out.println("Processing " + f.getAbsolutePath());
                processFile(key, f);
            }
        }
    }
}