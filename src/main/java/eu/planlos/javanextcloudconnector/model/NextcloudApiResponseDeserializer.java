package eu.planlos.javanextcloudconnector.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class NextcloudApiResponseDeserializer<T> extends StdDeserializer<NextcloudApiResponse<T>> {

    public NextcloudApiResponseDeserializer() {
        super(NextcloudApiResponse.class);
    }

    @Override
    public NextcloudApiResponse<T> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode rootNode = mapper.readTree(jsonParser);
        JsonNode metaNode = rootNode.get("ocs").get("meta");
        JsonNode dataNode = rootNode.get("ocs").get("data");

        NextcloudMeta meta = mapper.treeToValue(metaNode, NextcloudMeta.class);
        T data;

        if (dataNode.has("users")) {
            data = (T) mapper.readValue(dataNode.traverse(), new TypeReference<NextcloudUserList>() {});
        } else if(dataNode.isArray() && dataNode.isEmpty()) {
            data = null;
        }
        else {
            data = (T) mapper.treeToValue(dataNode, NextcloudUser.class);
        }

        return new NextcloudApiResponse<>(meta, data);
    }
}