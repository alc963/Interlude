package com.naugroup3.interlude.models.itunes;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ITunesResultDeserializer extends JsonDeserializer<ITunesResultBase> {
    @Override public ITunesResultBase deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        final JsonNode node = mapper.readTree(parser);

        final String wrapper = node.path("wrapperType").asText("");
        final String kind = node.path("kind").asText("");

        final Class<? extends ITunesResultBase> target = switch(wrapper) {
            case "artist" -> ITunesProxyArtist.class;
            case "collection" -> ITunesProxyCollection.class;
            case "track" -> "song".equals(kind) ? ITunesProxyTrack.class : null;
            default -> null;
        };
        if(target == null) return null;
        return mapper.treeToValue(node, target);
    }
}
