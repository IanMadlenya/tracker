package ca.uhnresearch.pughlab.tracker.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.ScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.events.Event;
import ca.uhnresearch.pughlab.tracker.events.EventHandler;
import ca.uhnresearch.pughlab.tracker.scripting.ScriptManager;

public class FieldCalculationPlugin extends AbstractRepositoryPlugin implements EventHandler {
	
	private final Logger logger = LoggerFactory.getLogger(FieldCalculationPlugin.class);

	private static final JsonNodeFactory factory = JsonNodeFactory.instance;

	@Autowired
	ScriptManager scriptManager;
	
	public void setScriptManager(ScriptManager scriptManager) {
		this.scriptManager = scriptManager;
	}
	
	public ScriptManager getScriptManager() {
		return scriptManager;
	}

	@Override
	public void sendMessage(Event event) {
		Study study = getRepository().getStudy(event.getScope());
		if (study == null) {
			return;
		}
		
		logger.info("Received event");

		if (event.getType().equals(Event.EVENT_SET_FIELD)) {
			ObjectNode parameters = event.getData().getParameters();
			applyCalculations(study, parameters);
		}
	}
	
	private static boolean isCalculated(Attributes att) {
		return att.getOptions() != null && att.getOptions().has("calculated");
	}
	
	private static JsonNode JSToJSON(Object value) {
		if (value == null) {
			return factory.nullNode();
		} else if (value instanceof Double) {
			return factory.numberNode((Double) value);
		} else if (value instanceof String) {
			return factory.textNode((String) value);
		} else {
			throw new RuntimeException("Can't handle JS result type: " + value.getClass().getCanonicalName());
		}
	}
	
	// Main logic for the current case
	private void applyCalculations(Study study, ObjectNode parameters) {

		Integer caseId = parameters.get("case_id").asInt();
		
		// Now, we can read the current case. Do we have any calculated attributes
		// we need to update?
		List<Attributes> attributes = getRepository().getStudyAttributes(study);
		
		// Let's get the object data, and make a new ScriptContext from it
		StudyCaseQuery query = getRepository().newStudyCaseQuery(study);
		query = getRepository().addStudyCaseSelector(query, caseId);
		List<ObjectNode> cases = getRepository().getCaseData(query, attributes);
		if (cases.isEmpty()) {
			return;
		}
		ObjectNode caseNode = cases.get(0);
		Map<String, Object> mappings = new HashMap<String,Object>();
		mappings.put("record", caseNode);
		
		ScriptContext newContext = getScriptManager().withContext(mappings);
		
		// Oh look, a lambda
		List<Attributes> calculatedAttributes = attributes.stream().filter(att -> isCalculated(att)).collect(Collectors.toList());
		
		logger.info("In applyCalculations: checking attributes");

		// Now let's apply the calculations we have
		for(Attributes att : calculatedAttributes) {
			String name = att.getName();
			String calculation = att.getOptions().get("calculated").asText("");
			calculation = calculation.trim();
			if (calculation.length() == 0) {
				continue;
			}
			
			// Now we can do the evalling. 
			Object result = getScriptManager().evaluateString(calculation, newContext);
			JsonNode jsonResult = JSToJSON(result);
			logger.debug("Evaluated: {} => {}, result: {}", name, calculation, result);
			
			// Now, we should check to see if the value has changed. 
			JsonNode original = caseNode.get(name);
			
			original = (original == null) ? factory.nullNode() : original;
			jsonResult = (jsonResult == null) ? factory.nullNode() : jsonResult;
			
			if (! original.equals(jsonResult)) {
				// Value has changed
				
				ObjectNode changes = factory.objectNode();
				changes.replace(name, jsonResult);
				try {
					getRepository().setQueryAttributes(query, "system", changes);
				} catch (RepositoryException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
