package com.afp.medialab.weverify.social.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

public class SetStringNormalizerDeserializer extends StdDeserializer<Set<String>> {
	
	public SetStringNormalizerDeserializer() {
		this(null);
	}
	
	public SetStringNormalizerDeserializer(Class<?> vc) {
		super(vc);
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7616490862955541690L;

	@Override
	public Set<String> deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = p.getCodec().readTree(p);
		
		if(!node.isArray()) {
			 throw new JsonParseException(p, "Unsupported formats");
		}
		SortedSet<String> elements = new TreeSet<String>();
		Iterator<JsonNode> iter = node.elements();
		while(iter.hasNext()) {
			JsonNode nod = iter.next();
			if(nod instanceof TextNode) {
				String text = ((TextNode)nod).asText();
				elements.add(text.toLowerCase());
			}
		}
		
		return elements;
	}

}
