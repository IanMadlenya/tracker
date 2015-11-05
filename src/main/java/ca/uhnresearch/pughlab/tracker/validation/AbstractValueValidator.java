package ca.uhnresearch.pughlab.tracker.validation;

import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractValueValidator implements ValueValidator {

	public abstract WritableValue validate(Attributes a, JsonNode value) throws InvalidValueException;
	
	protected boolean isNotAvailable(JsonNode value) {
		return value.isObject() && value.has("$notAvailable");
	}

}
