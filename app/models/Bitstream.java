package models;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created with IntelliJ IDEA.
 * User: peterdietz
 * Date: 10/2/13
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class Bitstream {
    public String name, description, format, mimeType, bundleName, retrieveLink;
    public Long sizeBytes;

    public static Bitstream parseBitstreamFromJSON(JsonNode bitstreamNode) {
        Bitstream bitstream = new Bitstream();
        bitstream.name = bitstreamNode.get("name").asText();
        bitstream.description = bitstreamNode.get("description").asText();
        bitstream.format = bitstreamNode.get("format").asText();
        bitstream.mimeType = bitstreamNode.get("mimeType").asText();
        bitstream.bundleName = bitstreamNode.get("bundleName").asText();
        bitstream.sizeBytes = bitstreamNode.get("sizeBytes").asLong();
        bitstream.retrieveLink = bitstreamNode.get("retrieveLink").asText();

        return bitstream;
    }
}
