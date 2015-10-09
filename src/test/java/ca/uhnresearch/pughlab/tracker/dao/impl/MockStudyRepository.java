package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;
import ca.uhnresearch.pughlab.tracker.events.EventHandler;

public class MockStudyRepository implements StudyRepository {

	private Logger logger = LoggerFactory.getLogger(MockStudyRepository.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private static final Integer caseCount = 10;

	Integer nextCaseId = caseCount;
	List<Study> studies = new ArrayList<Study>();
	List<Attributes> attributes = new ArrayList<Attributes>();
	List<View> views = new ArrayList<View>();
	Map<Integer, List<ViewAttributes>> viewAttributes = new HashMap<Integer, List<ViewAttributes>>();
	List<Cases> cases = new ArrayList<Cases>();
	List<MockCaseAttribute> strings = new ArrayList<MockCaseAttribute>();
	List<MockCaseAttribute> dates = new ArrayList<MockCaseAttribute>();
	List<MockCaseAttribute> booleans = new ArrayList<MockCaseAttribute>();
	
	Map<Integer, JsonObject> data = new HashMap<Integer, JsonObject>();

	public MockStudyRepository() {
		
		// Initialize the studies
		studies.add(mockStudy(1, "DEMO", "A demo clinical genomics study"));
		studies.add(mockStudy(1, "OTHER", "A different clinical genomics study"));
		
		views.add(mockView(1, "complete", "Manages the whole study", 1));
		views.add(mockView(2, "track", "Tracks the study", 1));
		views.add(mockView(3, "secondary", "Tracks only secondary", 1));
		
		// Initialize the attributes
		attributes.add(mockAttribute(1, "dateEntered", "Date Entered", 1, 1, "date"));
		attributes.add(mockAttribute(2, "patientId", "Patient", 2, 1, "string"));
		attributes.add(mockAttribute(3, "mrn", "MRN", 3, 1, "string"));
		attributes.add(mockAttribute(4, "consentDate", "Date of Consent", 4, 1, "date"));
		attributes.add(mockAttribute(5, "specimenAvailable", "Biobank Specimen Available? (Yes/No)", 5, 1, "boolean"));
		
		// And the view attribute mapping
		
		JsonNode mockClasses = null;
		try {
			mockClasses = mapper.readValue("{\"classes\": [\"label5\"]}", JsonNode.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		viewAttributes.put(1, new ArrayList<ViewAttributes>());
		viewAttributes.put(2, new ArrayList<ViewAttributes>());
		viewAttributes.put(3, new ArrayList<ViewAttributes>());
		
		viewAttributes.get(1).add(mockViewAttribute(attributes.get(0), null));
		viewAttributes.get(1).add(mockViewAttribute(attributes.get(1), null));
		viewAttributes.get(1).add(mockViewAttribute(attributes.get(2), null));
		viewAttributes.get(1).add(mockViewAttribute(attributes.get(3), null));
		viewAttributes.get(1).add(mockViewAttribute(attributes.get(4), null));
		
		viewAttributes.get(2).add(mockViewAttribute(attributes.get(0), null));
		viewAttributes.get(2).add(mockViewAttribute(attributes.get(1), null));
		viewAttributes.get(2).add(mockViewAttribute(attributes.get(4), mockClasses));

		viewAttributes.get(3).add(mockViewAttribute(attributes.get(0), null));
		viewAttributes.get(3).add(mockViewAttribute(attributes.get(1), null));
		viewAttributes.get(3).add(mockViewAttribute(attributes.get(4), mockClasses));

		// And finally add some cases
		for(Integer i = 0; i < caseCount; i++) {
			cases.add(mockCase(i));
		}
		
		// And case attributes, of different types - modelling the persistence (which in 
		// an ideal world we'd handle better by superimposing an interface on the generated
		// classes, but we can't really make things that simple.

		for(Integer i = 0; i < caseCount; i++) {
			Calendar date = Calendar.getInstance();
			date.set(2014, 8, i + 10);
			setDataAttribute(i, "dateEntered", new Date(date.getTimeInMillis()));
			setDataAttribute(i, "consentDate", new Date(date.getTimeInMillis()));
			setDataAttribute(i, "patientId", String.format("DEMO-%02d", i));
		}
		
		setDataAttribute(0, "mrn", "0101010");
		setDataAttribute(1, "mrn", "0202020");
		setDataAttribute(2, "mrn", "0303030");
		setDataAttribute(3, "mrn", "0404040");
		setDataAttribute(4, "mrn", "0505050");

		setDataAttribute(0, "specimenAvailable", true);
		setDataAttribute(1, "specimenAvailable", false);
		setDataAttribute(2, "specimenAvailable", true);
		setDataAttributeNotAvailable(3, "specimenAvailable");
		setDataAttribute(4, "specimenAvailable", false);
		
		JsonObject notes = new JsonObject();
		notes.addProperty("locked", true);
		JsonArray tagArray = new JsonArray();
		tagArray.add(new JsonPrimitive("label3"));
		notes.add("tags", tagArray);
		
		setDataAttributeNotes(4, "consentDate", notes);
	}
	
	private JsonObject getDataObject(Integer caseId) {
		if (! data.containsKey(caseId)) {
			data.put(caseId, new JsonObject());
		}
		return data.get(caseId);
	}
	
	private void setDataAttribute(Integer caseId, String property, String value) {
		getDataObject(caseId).addProperty(property, value);
	}
	
	private void setDataAttribute(Integer caseId, String property, Date value) {
		getDataObject(caseId).addProperty(property, value.toString());
	}
	
	private void setDataAttribute(Integer caseId, String property, Boolean value) {
		getDataObject(caseId).addProperty(property, value);
	}
	
	private void setDataAttributeNotes(Integer caseId, String property, JsonElement value) {
		JsonObject obj = getDataObject(caseId);
		if (! obj.has("$notes"))
			obj.add("$notes", new JsonObject());
		obj.getAsJsonObject("$notes").add(property, value);
	}

	private void setDataAttributeNotAvailable(Integer caseId, String property) {
		getDataObject(caseId).add(property, getNotAvailableValue());
	}

	private Cases mockCase(Integer id) {
		Cases c = new Cases();
		c.setId(id);
		if (id == 3) {
			c.setState("pending");
		}
		return c;
	}
	
	private ViewAttributes mockViewAttribute(Attributes att, JsonNode viewOptions) {
		ViewAttributes vatt = new ViewAttributes();
		vatt.setId(att.getId());
		vatt.setName(att.getName());
		vatt.setLabel(att.getLabel());
		vatt.setRank(att.getRank());
		vatt.setStudyId(att.getStudyId());
		vatt.setType(att.getType());
		vatt.setViewOptions(viewOptions);
		return vatt;
	}
	
	private Study mockStudy(Integer id, String name, String description) {
		Study study = new Study();
		study.setId(id);
		study.setName(name);
		study.setDescription(description);
		return study;
	}

	private Attributes mockAttribute(Integer id, String name, String label, Integer rank, Integer studyId, String type) {
		Attributes att = new Attributes();
		att.setId(id);
		att.setName(name);
		att.setLabel(label);
		att.setRank(rank);
		att.setStudyId(studyId);
		att.setType(type);
		return att;
	}

	private View mockView(Integer id, String name, String description, Integer studyId) {
		View view = new View();
		view.setId(id);
		view.setStudyId(studyId);
		view.setName(name);
		view.setDescription(description);
		return view;
	}

	// Mocked getStudy
	public Study getStudy(String name) {
		for(Study s : studies) {
			if (s.getName().equals(name)) {
				return s;
			}
		}
		return null;
	}

	// Mocked getAll
	public List<Study> getAllStudies() {
		return studies;
	}
	
	/**
	 * A mocked getStudyViews
	 */
	public List<View> getStudyViews(Study study) {
		List<View> result = new ArrayList<View>();
		for (View v : views) {
			if (v.getStudyId().equals(study.getId())) {
				result.add(v);
			}
		}
		logger.debug("Found views: " + result.size());
		return result;
	}

	/**
	 * A mocked setStudyViews
	 */
	public void setStudyViews(Study study, List<View> views) {
		this.views = views;
	}

	/**
	 * A mocked getStudyView
	 */
	public View getStudyView(Study study, String viewName) {
		for (View v : views) {
			if (v.getStudyId().equals(study.getId()) && v.getName().equals(viewName)) {
				return v;
			}
		}
		return null;
	}

	/**
	 * A mocked setStudyView
	 */
	@Override
	public void setStudyView(Study study, View view) throws RepositoryException {
		for (View v : views) {
			if (v.getId().equals(view.getId())) {
				v.setOptions(view.getOptions());
				return;
			}
		}
		throw new NotFoundException("Can't find study view: " + view.getName());
	}

	/**
	 * A mocked getStudyAttributes
	 */
	public List<Attributes> getStudyAttributes(Study study) {
		return attributes;
	}
	
	/**
	 * A mocked getStudyAttributes
	 */
	public Attributes getStudyAttribute(Study study, String name) {
		for (Attributes a : attributes) {
			if (name.equals(a.getName())) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * A mocked getStudyAttributes
	 */
	public void setStudyAttributes(Study study, List<Attributes> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * A mocked getViewAttributes
	 */
	public List<ViewAttributes> getViewAttributes(Study study, View view) {
		return viewAttributes.get(view.getId());
	}
	
	/**
	 * A mocked setViewAttributes
	 */
	public void setViewAttributes(Study study, View view, List<ViewAttributes> attributes) {
		return;
	}
	
	/**
	 * Returns all the attribute-level data associated with a study and a 
	 * view, all mocked of course.
	 * @param study
	 * @param view
	 * @return
	 */
	private Map<Integer, JsonObject> getAllData(Study study, View view) {
		return data;
	}
	
	private JsonObject getNotAvailableValue() {
		JsonObject value = new JsonObject();
		value.addProperty("$notAvailable", true);
		return value;
	}

	/**
	 * A mocked getData
	 */
	public List<ObjectNode> getData(Study study, View view, List<? extends Attributes> attributes, CaseQuery query) {
		
		// We build all the data in Gson, because it's easier
		Map<Integer, JsonObject> data = getAllData(study, view);
		
		Set<String> includedAttributes = new HashSet<String>();
		for(Attributes a : attributes) {
			includedAttributes.add(a.getName());
		}
		
		JsonArray result = new JsonArray();
		List<Integer> keys = new ArrayList<Integer>(data.keySet());
		Collections.sort(keys);
		Integer offset = query.getOffset();
		Integer limit = query.getLimit();
		Integer end = offset + limit;
		for(Integer i = offset; i < end; i++) {
			if (data.containsKey(i)) {
				Integer key = keys.get(i);
				result.add(data.get(key));
			}
		}
		
		// Now render it to a string (using Gson);
		String text = result.toString();
		List<ObjectNode> returnable = new ArrayList<ObjectNode>();
		
		// And now back into Jackson (!)
		try {
			Iterator<JsonNode> i = mapper.readTree(text).elements();
			while(i.hasNext()) {
				ObjectNode entry = (ObjectNode) i.next();
				ObjectNode copy = entry.deepCopy();
				Iterator<String> fields = entry.fieldNames();
				while(fields.hasNext()) {
					String nextField = fields.next();
					if (! includedAttributes.contains(nextField) && ! nextField.equals("$notes")) {
						copy.remove(nextField);
					}
				}
				returnable.add(copy);
			}
		} catch (JsonProcessingException e) {
			logger.error("Internal test error: {}", e.getMessage());
		} catch (IOException e) {
			logger.error("Internal test error: {}", e.getMessage());
		}
		
		return returnable;
	}

	@Override
	public Long getRecordCount(Study study, View view) {
		return new Long(caseCount);
	}

	@Override
	public Cases getStudyCase(Study study, View view, Integer caseId) {
		
		Cases result = null;
		for (Cases c : cases) {
			if (c.getId().equals(caseId)) {
				result = c;
				break;
			}
		}
		return result;		
	}
	

	@Override
	public void setStudyCaseState(Study study, View view, Cases cases, String userName, String state) {
		cases.setState(state);		
	}

	@Override
	public ObjectNode getCaseData(Study study, View view, Cases caseValue) {
		return getCaseData(study, view, null, caseValue);
	}

	@Override
	public ObjectNode getCaseData(Study study, View view, List<? extends Attributes> attributes, Cases caseValue) {
		// We build all the data in Gson, because it's easier
		Map<Integer, JsonObject> data = getAllData(study, view);
		if (! data.containsKey(caseValue.getId())) {
			return null;
		}
		return convertJsonElementToObjectNode(data.get(caseValue.getId()));
	}

	@Override
	public JsonNode getCaseAttributeValue(Study study, View view, Cases caseValue, Attributes attribute) {
		
		ObjectNode caseData = getCaseData(study, view, caseValue);
		return caseData.get(attribute.getName());
	}

	@Override
	public void setCaseAttributeValue(Study study, View view, Cases caseValue, Attributes attribute, String userName, JsonNode value) {
		
		// Well, yes, in theory we can just write in a new value, but this is all mocked
		// and it's actually a mirror of the correct value. Strictly, here, we need to 
		// get the type and then find and delete a real value. But hey, this is a mock
		// so we don't really care. Yet.

		return;
	}

	private ObjectNode convertJsonElementToObjectNode(JsonElement object) {
		String text = object.toString();
		try {
			return (ObjectNode) mapper.readTree(text);
		} catch (IOException e) {
			logger.error("Internal test error: {}", e.getMessage());
			return null;
		}

	}

	/**
	 * Mocked setter for an update event manager
	 */
	public void setEventHandler(EventHandler manager) {
		// Do nothing
	}

	@Override
	public Cases newStudyCase(Study study, View view, String userName) throws RepositoryException {
		return newStudyCase(study, view, userName, null);
	}

	@Override
	public Cases newStudyCase(Study study, View view, String userName, Cases afterCase) throws RepositoryException {
		Cases newCase = new Cases();
		newCase.setId(nextCaseId++);
		if (afterCase != null) {
			newCase.setOrder(afterCase.getOrder() + 1);
		}
		cases.add(newCase);
		return newCase;
	}

	@Override
	public Study saveStudy(Study study) {
		return study;
	}
}
