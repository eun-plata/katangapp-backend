package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Map;

import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

/**
 * @author mdelapenya
 */
public class JsonPrettyPrinter {

	public JsonPrettyPrinter(Http.Request request, JsonNode jsonNode) {
		this.request = request;
		this.jsonNode = jsonNode;
	}

	public Result prettyPrintWhenNeeded() {
		if (isPrettyPrint()) {
			ObjectMapper mapper = new ObjectMapper();

			final ObjectWriter objectWriter =
				mapper.writerWithDefaultPrettyPrinter();

			try {
				String prettyPrint = objectWriter.writeValueAsString(jsonNode);

				return Results.ok(prettyPrint).as("application/json");
			}
			catch (JsonProcessingException e) {
				// fall back to default JSON print
			}
		}

		return Results.ok(jsonNode);
	}

	private boolean isPrettyPrint() {
		Map<String, String[]> queryStringParametersMap = request.queryString();

		boolean isPrettyPrint = queryStringParametersMap.containsKey(
			"prettyPrint");

		if (isPrettyPrint) {
			final String[] pretties = queryStringParametersMap.get(
				"prettyPrint");

			if (pretties[0].equalsIgnoreCase("true") ||
				pretties[0].equalsIgnoreCase("1")) {

				return true;
			}
		}

		return false;
	}

	private final Http.Request request;
	private final JsonNode jsonNode;

}