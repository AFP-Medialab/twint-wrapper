package com.afp.medialab.weverify.social.model.twint;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;


public class TwittieDeserializer extends StdDeserializer<TwittieResponse> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 54707317053927190L;

	public TwittieDeserializer() {
        this(null);
    }
    public TwittieDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TwittieResponse deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();

        TwittieResponse tr = new TwittieResponse();

        TreeNode node = parser.readValueAsTree();

        if (node.get(":Person") != null)
            tr.setPerson(mapper.readValue(node.get(":Person").traverse(), new TypeReference<List<TwittieResponse.TwittieEntityJson<TwittieResponse.Person>>>(){}));

        if (node.get(":UserID") != null)
        {
            tr.setUserID(mapper.readValue(node.get(":UserID").traverse(),new TypeReference<List<TwittieResponse.TwittieEntityJson<TwittieResponse.UserID>>>(){}));
        }

        if (node.get(":Location") != null)
            tr.setLocation(mapper.readValue(node.get(":Location").traverse(), new TypeReference<List<TwittieResponse.TwittieEntityJson<TwittieResponse.Location>>>(){}));


        if (node.get(":Organization") != null)
            tr.setOrganization(mapper.readValue(node.get(":Organization").traverse(),new TypeReference<List<TwittieResponse.TwittieEntityJson<TwittieResponse.Organization>>>(){}));

        return tr;
    }
}
