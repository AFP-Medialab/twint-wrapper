package com.afp.medialab.weverify.social.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

public class SetStringNormalizerDeserializer extends StdDeserializer<Set<String>> {

	private static Logger Logger = LoggerFactory.getLogger(SetStringNormalizerDeserializer.class);

	public SetStringNormalizerDeserializer() {
		this(null);
	}

	public SetStringNormalizerDeserializer(Class<?> vc) {
		super(vc);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7616490862955541690L;

	private String regex = "[,]";
	private List<String> stopWords = Arrays.asList("and", "or", "not");

	@Override
	public Set<String> deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = p.getCodec().readTree(p);

		if (!node.isArray()) {
			throw new JsonParseException(p, "Unsupported formats");
		}
		SortedSet<String> elements = new TreeSet<String>();
		Iterator<JsonNode> iter = node.elements();
		while (iter.hasNext()) {
			JsonNode nod = iter.next();
			if (nod instanceof TextNode) {
				String text = ((TextNode) nod).asText();
				text = text.replaceAll(regex, " ");
				Logger.debug("text :" + text);
				String[] token = text.split("\\s+");
				List<String> tokens = Arrays.asList(token);

				elements.addAll(tokens.stream().filter(tk -> stopWords.stream().noneMatch(tk::equals)).map(tk -> {
					if (tk.startsWith("http://") || tk.startsWith("https://")) {
						return tk;
					} else
						return tk.toLowerCase();
				}).distinct().collect(Collectors.toList()));

			}
		}

		return elements;
	}

}
