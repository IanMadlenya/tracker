package ca.uhnresearch.pughlab.tracker.dao.impl;

import static org.hamcrest.Matchers.containsString;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;

import org.hamcrest.Matchers;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlInsertWithKeyCallback;
import org.springframework.data.jdbc.query.SqlUpdateCallback;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.types.Expression;

import ca.uhnresearch.pughlab.tracker.dao.CasePager;
import ca.uhnresearch.pughlab.tracker.dao.InvalidValueException;
import ca.uhnresearch.pughlab.tracker.dao.NotFoundException;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dao.StudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.domain.QAuditLog;
import ca.uhnresearch.pughlab.tracker.domain.QCases;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;
import ca.uhnresearch.pughlab.tracker.dto.Cases;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:**/testContextDatabase.xml" })
public class StudyRepositoryImplTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(getClass());
		
	@Autowired
    private StudyRepositoryImpl studyRepository;
	
	@Autowired
    private AuditLogRepositoryImpl auditLogRepository;

	private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
	
	private static ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testWiring() {
		Assert.assertNotNull(studyRepository);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudy() {
		Study s = studyRepository.getStudy("DEMO");
		Assert.assertNotNull(s);
		Assert.assertEquals("DEMO", s.getName());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyOptions() {
		Study s = studyRepository.getStudy("DEMO");
		Assert.assertNotNull(s);
		Assert.assertNotNull(s.getOptions());
		Assert.assertTrue(s.getOptions().has("stateLabels"));
		Assert.assertTrue(s.getOptions().get("stateLabels").isObject());
		Assert.assertTrue(s.getOptions().get("stateLabels").has("pending"));
		Assert.assertTrue(s.getOptions().get("stateLabels").get("pending").isTextual());
		Assert.assertEquals("label1", s.getOptions().get("stateLabels").get("pending").asText());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyAbout() {
		Study s = studyRepository.getStudy("DEMO");
		Assert.assertNotNull(s);
		Assert.assertNotNull(s.getAbout());
		Assert.assertEquals("#### Markdown-based description", s.getAbout());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSaveStudyNew() {
		Study s = new Study();
		s.setName("TEST");
		s.setDescription("A test study");
				
		studyRepository.saveStudy(s, "morag");
		Assert.assertNotNull(s);
		Assert.assertNotNull(s.getId());
		
		Study second = studyRepository.getStudy("TEST");
		Assert.assertNotNull(second);
		Assert.assertEquals("TEST", second.getName());
		Assert.assertEquals("A test study", second.getDescription());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSaveStudyUpdate() {
		Study s = studyRepository.getStudy("DEMO");
		s.setDescription("Another test");
				
		Study result = studyRepository.saveStudy(s, "morag");
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getId());
		Assert.assertEquals(result.getId(), s.getId());

		Study second = studyRepository.getStudy("DEMO");
		Assert.assertNotNull(second);
		Assert.assertEquals("Another test", second.getDescription());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSaveStudyUpdateAbout() {
		Study s = studyRepository.getStudy("DEMO");
		s.setAbout("#### Markdown about text");
				
		Study result = studyRepository.saveStudy(s, "morag");
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getId());
		Assert.assertEquals(result.getId(), s.getId());

		Study second = studyRepository.getStudy("DEMO");
		Assert.assertNotNull(second);
		Assert.assertEquals("#### Markdown about text", second.getAbout());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetMissingStudy() {
		Study s = studyRepository.getStudy("DEMOX");
		Assert.assertNull(s);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetMissingStudyOptions() {
		Study s = studyRepository.getStudy("ALPHABETICAL");
		Assert.assertNotNull(s);
		Object options = s.getOptions();
		Assert.assertNotNull(options);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudies() {
		List<Study> list = studyRepository.getAllStudies();
		Assert.assertNotNull(list);
		Assert.assertEquals(4, list.size());
		
		Assert.assertEquals("ADMIN", list.get(0).getName());
		Assert.assertEquals("ALPHABETICAL", list.get(1).getName());
		Assert.assertEquals("DEMO", list.get(2).getName());
		Assert.assertEquals("SECOND", list.get(3).getName());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyViews() {
		Study study = studyRepository.getStudy("DEMO");
		List<View> list = studyRepository.getStudyViews(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(4, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyView() {
		Study study = studyRepository.getStudy("DEMO");
		View v = studyRepository.getStudyView(study, "complete");
		Assert.assertNotNull(v);
		Assert.assertEquals("complete", v.getName());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyViewOptions() {
		Study study = studyRepository.getStudy("DEMO");
		View v = studyRepository.getStudyView(study, "secondary");
		Assert.assertNotNull(v);
		Assert.assertEquals("secondary", v.getName());
		
		Assert.assertNotNull(v.getOptions());
		Assert.assertNotNull(v.getOptions().get("rows"));
		Assert.assertTrue(v.getOptions().get("rows").isArray());
		Assert.assertEquals(1, v.getOptions().get("rows").size());
		Assert.assertNotNull(v.getOptions().get("rows").get(0));
		Assert.assertTrue(v.getOptions().get("rows").get(0).isObject());
		Assert.assertEquals("study", v.getOptions().get("rows").get(0).get("attribute").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyViewOptions() {
		Study study = studyRepository.getStudy("DEMO");
		View v = studyRepository.getStudyView(study, "track");
		Assert.assertNotNull(v);
		Assert.assertEquals("track", v.getName());
		
		ObjectNode viewOptions = objectMapper.createObjectNode();
		ObjectNode viewOptionDescriptor = objectMapper.createObjectNode();
		ArrayNode viewArray = objectMapper.createArrayNode();
		viewOptionDescriptor.put("attribute", "dateEntered");
		viewOptionDescriptor.put("value", "test");
		viewArray.add(viewOptionDescriptor);
		viewOptions.set("rows", viewArray);
		
		v.setOptions(viewOptions);
		
		try {
			studyRepository.setStudyView(study, v);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		View modifiedView = studyRepository.getStudyView(study, "track");
		
		Assert.assertNotNull(modifiedView.getOptions());
		Assert.assertNotNull(modifiedView.getOptions().get("rows"));
		Assert.assertTrue(modifiedView.getOptions().get("rows").isArray());
		Assert.assertEquals(1, modifiedView.getOptions().get("rows").size());
		Assert.assertNotNull(modifiedView.getOptions().get("rows").get(0));
		Assert.assertTrue(modifiedView.getOptions().get("rows").get(0).isObject());
		Assert.assertEquals("dateEntered", modifiedView.getOptions().get("rows").get(0).get("attribute").asText());
		Assert.assertEquals("test", modifiedView.getOptions().get("rows").get(0).get("value").asText());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyViewOptionsInvalidView() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View v = studyRepository.getStudyView(study, "track");
		Assert.assertNotNull(v);
		Assert.assertEquals("track", v.getName());
		
		ObjectNode viewOptions = objectMapper.createObjectNode();
		ObjectNode viewOptionDescriptor = objectMapper.createObjectNode();
		ArrayNode viewArray = objectMapper.createArrayNode();
		viewOptionDescriptor.put("attribute", "dateEntered");
		viewOptionDescriptor.put("value", "test");
		viewArray.add(viewOptionDescriptor);
		viewOptions.set("rows", viewArray);
		
		v.setOptions(viewOptions);
		v.setStudyId(100);
	
		thrown.expect(NotFoundException.class);
		thrown.expectMessage(containsString("Can't update view for a different study"));

		studyRepository.setStudyView(study, v);
	}


	@Test
	@Transactional
	@Rollback(true)
	public void testGetMissingStudyView() {
		Study study = studyRepository.getStudy("DEMO");
		View v = studyRepository.getStudyView(study, "completed");
		Assert.assertNull(v);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyAttributes() {
		Study study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(28, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetViewAttributes() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(28, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSmallerGetViewAttributes() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(15, list.size());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetData() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		CasePager pager = new CasePager();
		pager.setOffset(0);
		pager.setLimit(10);
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.applyPager(query, pager);
		List<ObjectNode> list = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(10, list.size());
	}
	
	/**
	 * Regression test for #53 -- checks that only legitimate view attributes are 
	 * returned.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataSecurity() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		CasePager pager = new CasePager();
		pager.setOffset(0);
		pager.setLimit(10);
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.applyPager(query, pager);
		List<ObjectNode> list = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(10, list.size());
		Assert.assertFalse(list.get(0).has("mrn"));
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataNoLimit() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		CasePager pager = new CasePager();
		pager.setOffset(0);
		pager.setLimit(null);
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.applyPager(query, pager);
		List<ObjectNode> list = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(20, list.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataNoOffset() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		CasePager pager = new CasePager();
		pager.setOffset(null);
		pager.setLimit(5);
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.applyPager(query, pager);
		List<ObjectNode> list = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(5, list.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataOrdered() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		CasePager pager = new CasePager();
		pager.setOffset(0);
		pager.setLimit(5);
		pager.setOrderField("consentDate");
		pager.setOrderDirection(CasePager.OrderDirection.DESC);
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.applyPager(query, pager);
		List<ObjectNode> list = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(5, list.size());
	}
	
	/**
	 * Checks that when an attribute filter is applied, only the specified attributes are returned. 
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetDataFiltered() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> attributes = studyRepository.getViewAttributes(study, view);
		List<ViewAttributes> filteredAttributes = attributes.subList(0, 3);
		
		CasePager pager = new CasePager();
		pager.setOffset(0);
		pager.setLimit(null);
		
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.applyPager(query, pager);
		
		List<ObjectNode> list = studyRepository.getCaseData(query, filteredAttributes);
		Assert.assertNotNull(list);
		Assert.assertEquals(20, list.size());
		for(int i = 0; i < 5; i++) {
			Assert.assertFalse(list.get(i).has("physician"));
			Assert.assertFalse(list.get(i).has("tissueSite"));
			Assert.assertFalse(list.get(i).has("specimenAvailable"));
		}
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testRecordCount() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		Long count = studyRepository.getRecordCount(study, view);
		Assert.assertEquals(20, count.intValue());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCase() {
		Study study = studyRepository.getStudy("DEMO");
		Cases caseValue = studyRepository.getStudyCase(study, 1);
		Assert.assertNotNull(caseValue);
		Assert.assertEquals(1, caseValue.getId().intValue());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleMissingCase() {
		Study study = studyRepository.getStudy("DEMO");
		Cases caseValue = studyRepository.getStudyCase(study, 100);
		Assert.assertNull(caseValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleFromDifferentStudy() {
		Study study = studyRepository.getStudy("DEMO");
		Cases caseValue = studyRepository.getStudyCase(study, 22);
		Assert.assertNull(caseValue);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseValues() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addStudyCaseSelector(query, 1);
		List<ObjectNode> data = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(data);
		Assert.assertEquals(1,  data.size());
		
		ObjectNode single = data.get(0);
		
		String date = single.get("dateEntered").asText();
		Assert.assertNotNull(date);
		Assert.assertEquals("2014-08-20", date);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseStateNull() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, 3);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(1, dataList.size());
		
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);

		Assert.assertTrue(data.has("$state"));
		Assert.assertTrue(data.get("$state").isNull());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseStatePending() {
		Study study = studyRepository.getStudy("SECOND");
		View view = studyRepository.getStudyView(study, "complete");
		
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, 21);

		List<ObjectNode> dataList = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(1, dataList.size());

		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);
		
		Assert.assertTrue(data.has("$state"));
		Assert.assertTrue(data.get("$state").isTextual());
		Assert.assertEquals("pending", data.get("$state").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseNumberValues() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
		
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addStudyCaseSelector(query, 1);
		
		List<ObjectNode> data = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(data);
		Assert.assertEquals(1,  data.size());
		
		ObjectNode single = data.get(0);

		Assert.assertTrue(single.has("numberCores"));
		Double cores = single.get("numberCores").asDouble();
		Assert.assertNotNull(cores);
		Assert.assertTrue(Math.abs(cores - 2.0) < 0.00000001);
	}

	
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseValuesNotes() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addStudyCaseSelector(query, 1);

		List<ObjectNode> data = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(data);
		Assert.assertEquals(1,  data.size());
		
		ObjectNode single = data.get(0);

		JsonNode notes = single.get("$notes");
		Assert.assertNotNull(notes);
		
		// No notes here
		Assert.assertNull(notes.get("specimenAvailable"));

		// Notes here
		JsonNode consentDateNotes = notes.get("consentDate");
		Assert.assertNotNull(consentDateNotes);

		JsonNode consentDateLocked = consentDateNotes.get("locked");
		Assert.assertNotNull(consentDateLocked);
		Assert.assertTrue(consentDateLocked.isBoolean());
		Assert.assertTrue(consentDateLocked.asBoolean());
	}


	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseValuesFiltered() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> attributes = studyRepository.getViewAttributes(study, view);
		List<ViewAttributes> filteredAttributes = attributes.subList(0, 3);
		
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, 1);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(query, filteredAttributes);
		Assert.assertEquals(1, dataList.size());
		
		ObjectNode data = dataList.get(0);
		Assert.assertNotNull(data);
		
		for(ViewAttributes va : attributes) {
			Boolean filtered = filteredAttributes.contains(va);
			Assert.assertEquals("Failed to filter attribute: " + va.getName(),filtered, data.has(va.getName()));
		}
	}
	
	private ObjectNode getCaseAttributeValue(Study study, View view, Integer caseId) {
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, caseId);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(query, view);
		Assert.assertEquals(1, dataList.size());
		
		return dataList.get(0);
	}


	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeValues() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		
		ObjectNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertNotNull(data);
		Assert.assertEquals("2014-08-20", data.get("dateEntered").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeValuesNotAvailable() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");
		
		ObjectNode data = getCaseAttributeValue(study, view, 2);
		Assert.assertTrue(data.get("trackerDate").isObject());
		Assert.assertTrue(data.get("trackerDate").has("$notAvailable"));
		Assert.assertEquals("true", data.get("trackerDate").get("$notAvailable").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeMissing() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		
		ObjectNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertNotNull(data);
		Assert.assertFalse(data.has("bloodCollDate"));
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testAuditLog() {
		Study study = studyRepository.getStudy("DEMO");
		
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(0, auditEntries.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testAuditLogWithNoLimits() {
		Study study = studyRepository.getStudy("DEMO");
		
		CasePager query = new CasePager();
		query.setOffset(null);
		query.setLimit(null);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(0, auditEntries.size());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testAuditLogWithBadData() {
		Study study = studyRepository.getStudy("DEMO");
		
		CasePager query = new CasePager();
		query.setOffset(null);
		query.setLimit(null);
		
		List<AuditLogRecord> data = new ArrayList<AuditLogRecord>();
		AuditLogRecord entry = new AuditLogRecord();
		entry.setEventTime(Timestamp.from(Instant.now()));
		entry.setEventArgs("{");
		data.add(entry);
		
		QueryDslJdbcTemplate originalTemplate = studyRepository.getTemplate();
		QueryDslJdbcTemplate mockTemplate = createMock(QueryDslJdbcTemplate.class);
		expect(mockTemplate.newSqlQuery()).andStubReturn(originalTemplate.newSqlQuery());
		expect(mockTemplate.query(anyObject(SQLQuery.class), anyObject(QAuditLog.class))).andStubReturn(data);
		replay(mockTemplate);
		
		studyRepository.setTemplate(mockTemplate);
		List<JsonNode> auditEntries = null;

		try {
			auditEntries = auditLogRepository.getAuditData(study, query);
		} finally {
			studyRepository.setTemplate(originalTemplate);
		}
		
		Assert.assertNotNull(auditEntries);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testAuditLogWithGoodData() {
		Study study = studyRepository.getStudy("DEMO");
		
		CasePager query = new CasePager();
		query.setOffset(null);
		query.setLimit(null);
		
		List<AuditLogRecord> data = new ArrayList<AuditLogRecord>();
		AuditLogRecord entry = new AuditLogRecord();
		entry.setEventTime(Timestamp.from(Instant.now()));
		entry.setEventArgs("{\"old\":null,\"value\":100}");
		data.add(entry);
		
		QueryDslJdbcTemplate originalTemplate = studyRepository.getTemplate();
		QueryDslJdbcTemplate mockTemplate = createMock(QueryDslJdbcTemplate.class);
		expect(mockTemplate.newSqlQuery()).andStubReturn(originalTemplate.newSqlQuery());
		expect(mockTemplate.query(anyObject(SQLQuery.class), anyObject(QAuditLog.class))).andStubReturn(data);
		replay(mockTemplate);
		
		studyRepository.setTemplate(mockTemplate);
		List<JsonNode> auditEntries = null;

		try {
			auditEntries = auditLogRepository.getAuditData(study, query);
		} finally {
			studyRepository.setTemplate(originalTemplate);
		}
		
		Assert.assertNotNull(auditEntries);
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueDate() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("dateEntered", jsonNodeFactory.nullNode());
			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("dateEntered", entry.get("attribute").asText());
		Assert.assertEquals("2014-08-20", entry.get("eventArgs").get("old").asText());
		Assert.assertTrue(entry.get("eventArgs").get("new").isNull());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		ObjectNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertTrue(data.has("dateEntered"));
		Assert.assertTrue(data.get("dateEntered").isNull());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueDateInsert() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 6);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("procedureDate", jsonNodeFactory.textNode("2014-02-03"));
			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail(e.getMessage());
		}
		
		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("procedureDate", entry.get("attribute").asText());
		Assert.assertTrue(entry.get("eventArgs").get("old").isNull());
		Assert.assertEquals("2014-02-03", entry.get("eventArgs").get("new").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. 
		
		ObjectNode data = getCaseAttributeValue(study, view, 6);
		Assert.assertTrue(data.has("procedureDate"));
		Assert.assertEquals("2014-02-03", data.get("procedureDate").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueString() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("patientId", jsonNodeFactory.textNode("DEMO-XX"));
			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("patientId", entry.get("attribute").asText());
		Assert.assertEquals("DEMO-01", entry.get("eventArgs").get("old").asText());
		Assert.assertEquals("DEMO-XX", entry.get("eventArgs").get("new").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-XX", data.get("patientId").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueStringInsert() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 10);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("specimenNo", jsonNodeFactory.textNode("SMP-XX"));
			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenNo", entry.get("attribute").asText());
		Assert.assertTrue(entry.get("eventArgs").get("old").isNull());
		Assert.assertEquals("SMP-XX", entry.get("eventArgs").get("new").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = getCaseAttributeValue(study, view, 10);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("specimenNo"));
		Assert.assertEquals("SMP-XX", data.get("specimenNo").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueStringNull() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("patientId", jsonNodeFactory.nullNode());
			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("patientId", entry.get("attribute").asText());
		Assert.assertEquals("DEMO-01", entry.get("eventArgs").get("old").asText());
		Assert.assertTrue(entry.get("eventArgs").get("new").isNull());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("patientId"));
		Assert.assertTrue(data.get("patientId").isNull());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueOption() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.put("sampleAvailable", "St. Michaels");
			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("sampleAvailable", entry.get("attribute").asText());
		Assert.assertEquals("LMP", entry.get("eventArgs").get("old").asText());
		Assert.assertEquals("St. Michaels", entry.get("eventArgs").get("new").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("sampleAvailable"));
		Assert.assertEquals("St. Michaels", data.get("sampleAvailable").asText());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBoolean() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.put("specimenAvailable", false);
			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("true", entry.get("eventArgs").get("old").asText());
		Assert.assertEquals("false", entry.get("eventArgs").get("new").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("specimenAvailable"));
		Assert.assertTrue(data.get("specimenAvailable").isBoolean());
		Assert.assertEquals("false", data.get("specimenAvailable").asText());
	}

	
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBooleanValueError() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, 1);
		ObjectNode values = jsonNodeFactory.objectNode();
		values.replace("specimenAvailable", jsonNodeFactory.textNode("BAD"));
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid boolean"));
		
		studyRepository.setQueryAttributes(query, "stuart", values);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueStringValueError() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, 1);
		ObjectNode values = jsonNodeFactory.objectNode();
		values.replace("patientId", jsonNodeFactory.booleanNode(false));
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid string"));

		studyRepository.setQueryAttributes(query, "stuart", values);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueDateValueError() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, 1);
		ObjectNode values = jsonNodeFactory.objectNode();
		values.replace("dateEntered", jsonNodeFactory.booleanNode(false));
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid date"));

		studyRepository.setQueryAttributes(query, "stuart", values);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueDateValueFormatError() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, 1);
		ObjectNode values = jsonNodeFactory.objectNode();
		values.replace("dateEntered", jsonNodeFactory.textNode("2015-02-XX"));

		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid date"));

		studyRepository.setQueryAttributes(query, "stuart", values);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueOptionValueError() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, 1);
		ObjectNode values = jsonNodeFactory.objectNode();
		values.replace("sampleAvailable", jsonNodeFactory.booleanNode(false));

		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid string"));

		studyRepository.setQueryAttributes(query, "stuart", values);
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueOptionUnexpectedValueError() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addViewCaseMatcher(query, view);
		query = studyRepository.addStudyCaseSelector(query, 1);
		ObjectNode values = jsonNodeFactory.objectNode();
		values.replace("sampleAvailable", jsonNodeFactory.textNode("BAD"));

		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Invalid string"));

		studyRepository.setQueryAttributes(query, "stuart", values);
	}

	// Regression test for #6 -- check that multiple writes are handled correctly. 
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBooleanTwice() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("specimenAvailable", jsonNodeFactory.booleanNode(false));

			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("specimenAvailable", jsonNodeFactory.booleanNode(true));

			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}

		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(2, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("false", entry.get("eventArgs").get("old").asText());
		Assert.assertEquals("true", entry.get("eventArgs").get("new").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("specimenAvailable"));
		Assert.assertEquals("true", data.get("specimenAvailable").asText());
	}

	// Regression test for #6 -- check that multiple writes are handled correctly. 
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteSameValueBooleanTwice() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("specimenAvailable", jsonNodeFactory.booleanNode(false));

			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("specimenAvailable", jsonNodeFactory.booleanNode(false));

			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}

		// Check we now have an audit log entry
		CasePager pager = new CasePager();
		pager.setOffset(0);
		pager.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, pager);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("true", entry.get("eventArgs").get("old").asText());
		Assert.assertEquals("false", entry.get("eventArgs").get("new").asText());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("specimenAvailable"));
		Assert.assertEquals("false", data.get("specimenAvailable").asText());
	}

	// Regression test for #7 -- check that N/A writes are handled correctly. 
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBooleanNotAvailable() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			ObjectNode notAvailable = objectMapper.createObjectNode();
			notAvailable.put("$notAvailable", true);
			values.replace("specimenAvailable", notAvailable);

			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("true", entry.get("eventArgs").get("old").asText());
		Assert.assertTrue(entry.get("eventArgs").get("new").isObject());
		Assert.assertEquals(true, entry.get("eventArgs").get("new").get("$notAvailable").asBoolean());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("specimenAvailable"));
		Assert.assertTrue(data.get("specimenAvailable").isObject());
		Assert.assertEquals(true, data.get("specimenAvailable").get("$notAvailable").asBoolean());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteValueBooleanNull() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("specimenAvailable", jsonNodeFactory.nullNode());

			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("true", entry.get("eventArgs").get("old").asText());
		Assert.assertTrue(entry.get("eventArgs").get("new").isNull());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = getCaseAttributeValue(study, view, 1);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("specimenAvailable"));
		Assert.assertTrue(data.get("specimenAvailable").isNull());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteNonExistentValue() {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 15);
			ObjectNode values = jsonNodeFactory.objectNode();
			ObjectNode notAvailable = objectMapper.createObjectNode();
			notAvailable.put("$notAvailable", true);
			values.replace("specimenAvailable", notAvailable);

			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail();
		}
		
		// Check we now have an audit log entry
		CasePager query = new CasePager();
		query.setOffset(0);
		query.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, query);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		
		// Poke at the first audit log entry
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("stuart", entry.get("eventUser").asText());
		Assert.assertEquals("specimenAvailable", entry.get("attribute").asText());
		Assert.assertEquals("null", entry.get("eventArgs").get("old").asText());
		Assert.assertTrue(entry.get("eventArgs").get("new").isObject());
		Assert.assertEquals(true, entry.get("eventArgs").get("new").get("$notAvailable").asBoolean());
		
		// And now, we ought to be able to see the new audit entry in the database, and
		// the value should be correct too. Note that as we have set null, we get back a 
		// JSON null, not a Java one. 
		JsonNode data = getCaseAttributeValue(study, view, 15);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("specimenAvailable"));
		Assert.assertTrue(data.get("specimenAvailable").isObject());
		Assert.assertEquals(true, data.get("specimenAvailable").get("$notAvailable").asBoolean());
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testSingleCaseAttributeWriteMissingAttribute() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		Attributes attribute = studyRepository.getStudyAttribute(study, "dateEnteredX");
		
		Assert.assertNull(attribute);
	}

	/**
	 * Simple test of writing the exact same attributes back into the study. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(28, list.size());
		
		studyRepository.setStudyAttributes(study, list);

		List<Attributes> listAgain = studyRepository.getStudyAttributes(study);
		
		Assert.assertEquals(listAgain.size(), list.size());
		int size = list.size();
		for(int i = 0; i < size; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
	}
	
	/**
	 * Simple test of writing the exact same attributes back into the study. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyAttributeType() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<Attributes> list = studyRepository.getStudyAttributes(study);

		Assert.assertNotNull(list);
		List<Attributes> copy = new ArrayList<Attributes>();
		copy.addAll(list);
		Attributes attribute = copy.get(4);
		Attributes newAttribute = new Attributes();
		newAttribute.setId(attribute.getId());
		newAttribute.setStudyId(attribute.getStudyId());
		newAttribute.setName(attribute.getName());
		newAttribute.setLabel(attribute.getLabel());
		newAttribute.setDescription(attribute.getDescription());
		newAttribute.setOptions(attribute.getOptions());
		newAttribute.setRank(attribute.getRank());
		newAttribute.setType("string");
		copy.set(4, newAttribute);
		
		JsonNode oldData = getCaseAttributeValue(study, view, 1);

		studyRepository.setStudyAttributes(study, copy);

		List<Attributes> listAgain = studyRepository.getStudyAttributes(study);
		Assert.assertEquals(listAgain.size(), list.size());
		
		JsonNode newData = getCaseAttributeValue(study, view, 1);
		Assert.assertTrue(oldData.has("consentDate"));
		Assert.assertFalse(newData.has("consentDate"));
	}
	
	/**
	 * Simple test of writing the exact same attributes back into the study. After
	 * we do this, we should be able to write and read data correctly. 
	 */
	// Regression test for #179
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyAttributeTypeAndWrite() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<Attributes> list = studyRepository.getStudyAttributes(study);

		Assert.assertNotNull(list);
		List<Attributes> copy = new ArrayList<Attributes>();
		copy.addAll(list);
		Attributes attribute = copy.get(4);
		Attributes newAttribute = new Attributes();
		newAttribute.setId(attribute.getId());
		newAttribute.setStudyId(attribute.getStudyId());
		newAttribute.setName(attribute.getName());
		newAttribute.setLabel(attribute.getLabel());
		newAttribute.setDescription(attribute.getDescription());
		newAttribute.setOptions(attribute.getOptions());
		newAttribute.setRank(attribute.getRank());
		newAttribute.setType("string");
		copy.set(4, newAttribute);
		
		JsonNode oldData = getCaseAttributeValue(study, view, 1);
		Assert.assertTrue(oldData.has("consentDate"));

		studyRepository.setStudyAttributes(study, copy);

		// Now try to write a string
		try {
			StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
			query = studyRepository.addViewCaseMatcher(query, view);
			query = studyRepository.addStudyCaseSelector(query, 1);
			ObjectNode values = jsonNodeFactory.objectNode();
			values.replace("consentDate", jsonNodeFactory.textNode("Test"));
			studyRepository.setQueryAttributes(query, "stuart", values);
		} catch (RepositoryException e) {
			Assert.fail(e.getMessage());
		}
		
		// And now check we get the new value back
		JsonNode newData = getCaseAttributeValue(study, view, 1);
		Assert.assertTrue(newData.has("consentDate"));
		Assert.assertEquals("Test", newData.get("consentDate").asText());
	}
	
	/**
	 * Simple test of deleting a number of attributes.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteStudyAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(28, list.size());
		
		studyRepository.setStudyAttributes(study, list.subList(0, 10));

		List<Attributes> listAgain = studyRepository.getStudyAttributes(study);
		Assert.assertEquals(10, listAgain.size());
		
		for(int i = 0; i < 10; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testAddStudyAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<Attributes> list = studyRepository.getStudyAttributes(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(28, list.size());
		
		Attributes att1 = new Attributes();
		att1.setName("test");
		att1.setType("string");
		att1.setLabel("Test");
		att1.setDescription("First test attribute");
		
		List<Attributes> modified = list.subList(0, 10);
		modified.add(att1);
		
		studyRepository.setStudyAttributes(study, modified);

		List<Attributes> listAgain = studyRepository.getStudyAttributes(study);
		Assert.assertEquals(11, listAgain.size());
		
		for(int i = 0; i < 10; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
		Attributes loadedAtt1 = listAgain.get(10);
		
		// Cheatily clear the id, so we can compare all other fields
		loadedAtt1.setId(null);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(att1, loadedAtt1));
	}

	/**
	 * Simple test of writing the exact same attributes back into the study. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyViews() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<View> list = studyRepository.getStudyViews(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(4, list.size());
		
		studyRepository.setStudyViews(study, list);

		List<View> listAgain = studyRepository.getStudyViews(study);
		
		Assert.assertEquals(listAgain.size(), list.size());
		int size = list.size();
		for(int i = 0; i < size; i++) {
			View oldView = list.get(i);
			View newView = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldView, newView));
		}
	}
	
	/**
	 * Simple test of writing the exact same attributes back into the study. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetStudyViewsUpdateKey() throws RepositoryException {
		
		Study study = studyRepository.getStudy("DEMO");
		List<View> list = studyRepository.getStudyViews(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(4, list.size());
		
		View oldView = list.remove(2);
		View newView = new View();
		newView.setId(oldView.getId());
		newView.setStudyId(oldView.getStudyId());
		newView.setOptions(oldView.getOptions());
		newView.setName("testView");
		newView.setDescription("Test View");
		list.add(2, newView);
		Assert.assertEquals(4, list.size());
		
		studyRepository.setStudyViews(study, list);

		List<View> listAgain = studyRepository.getStudyViews(study);
		
		Assert.assertEquals(listAgain.size(), list.size());
		int size = list.size();
		for(int i = 0; i < size; i++) {
			View oldViewRead = list.get(i);
			View newViewRead = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldViewRead, newViewRead));
		}
	}
	
	/**
	 * Simple test of deleting a view.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteStudyView() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<View> list = studyRepository.getStudyViews(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(4, list.size());
		
		studyRepository.setStudyViews(study, list.subList(0, 2));

		List<View> listAgain = studyRepository.getStudyViews(study);
		Assert.assertEquals(2, listAgain.size());
		
		for(int i = 0; i < 2; i++) {
			View oldView = list.get(i);
			View newView = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldView, newView));
		}
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testAddStudyViews() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		List<View> list = studyRepository.getStudyViews(study);
		Assert.assertNotNull(list);
		Assert.assertEquals(4, list.size());
		
		View v1 = new View();
		v1.setName("test");
		v1.setDescription("First test attribute");
		
		List<View> modified = list.subList(0, 2);
		modified.add(v1);
		
		studyRepository.setStudyViews(study, modified);

		List<View> listAgain = studyRepository.getStudyViews(study);
		Assert.assertEquals(3, listAgain.size());
		
		for(int i = 0; i < 2; i++) {
			View oldAttribute = list.get(i);
			View newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
		View loadedV1 = listAgain.get(2);
		
		// Cheatily clear the id, so we can compare all other fields
		loadedV1.setId(null);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(v1, loadedV1));
	}

	/**
	 * Simple test of writing the exact same attributes back into the view. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testSetViewAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(15, list.size());
		
		studyRepository.setViewAttributes(study, view, list);

		List<ViewAttributes> listAgain = studyRepository.getViewAttributes(study, view);
		
		Assert.assertEquals(listAgain.size(), list.size());
		int size = list.size();
		for(int i = 0; i < size; i++) {
			ViewAttributes oldAttribute = list.get(i);
			ViewAttributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
	}
	
	/**
	 * Simple test of deleting a number of attributes.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteViewAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(15, list.size());
		
		studyRepository.setViewAttributes(study, view, list.subList(0, 10));

		List<ViewAttributes> listAgain = studyRepository.getViewAttributes(study, view);
		Assert.assertEquals(10, listAgain.size());
		
		for(int i = 0; i < 10; i++) {
			ViewAttributes oldAttribute = list.get(i);
			ViewAttributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testAddViewAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(15, list.size());
		
		ViewAttributes att1 = new ViewAttributes();
		att1.setId(8);
		att1.setName("specimenNo");
		att1.setType("string");
		att1.setLabel("Specimen #");
		att1.setStudyId(study.getId());
		
		List<ViewAttributes> modified = list.subList(0, 10);
		modified.add(att1);
		
		studyRepository.setViewAttributes(study, view, modified);

		List<ViewAttributes> listAgain = studyRepository.getViewAttributes(study, view);
		Assert.assertEquals(11, listAgain.size());
		
		for(int i = 0; i < 10; i++) {
			Attributes oldAttribute = list.get(i);
			Attributes newAttribute = listAgain.get(i);
			Assert.assertTrue(EqualsBuilder.reflectionEquals(oldAttribute, newAttribute));
		}
		Attributes loadedAtt1 = listAgain.get(10);
		
		// Cheatily clear the id, so we can compare all other fields
		loadedAtt1.setId(att1.getId());
		loadedAtt1.setRank(att1.getRank());
		Assert.assertTrue(EqualsBuilder.reflectionEquals(att1, loadedAtt1));
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testAddMissingViewAttributes() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		List<ViewAttributes> list = studyRepository.getViewAttributes(study, view);
		Assert.assertNotNull(list);
		Assert.assertEquals(15, list.size());
		
		ViewAttributes att1 = new ViewAttributes();
		att1.setId(600);
		att1.setName("unknown");
		att1.setType("string");
		att1.setLabel("Specimen #");
		att1.setStudyId(study.getId());
		
		List<ViewAttributes> modified = list.subList(0, 10);
		modified.add(att1);
	
		thrown.expect(NotFoundException.class);
		thrown.expectMessage(containsString("Missing attribute"));

		studyRepository.setViewAttributes(study, view, modified);
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testNewCase() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");

		Cases newCase = studyRepository.newStudyCase(study, "test");
		Assert.assertNotNull(newCase);
		Assert.assertNotNull(newCase.getId());
		Assert.assertNotNull(newCase.getStudyId());
		
		// And now let's dig out the new case -- mainly to check that we can actually
		// follow this identifier.
		Cases caseValue = studyRepository.getStudyCase(study, newCase.getId());
		Assert.assertNotNull(caseValue);
		Assert.assertEquals(newCase.getId(), caseValue.getId());
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testNewCaseOrdering() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		
		Cases foundCase = studyRepository.getStudyCase(study, 10);
		Integer foundCaseOrder = foundCase.getOrder();

		Cases newCase = studyRepository.newStudyCase(study, "test", foundCase);
		Assert.assertNotNull(newCase);
		Assert.assertNotNull(newCase.getId());
		Assert.assertNotNull(newCase.getStudyId());
		
		// And now let's dig out the new case -- mainly to check that we can actually
		// follow this identifier.
		Cases caseValue = studyRepository.getStudyCase(study, newCase.getId());
		Assert.assertNotNull(caseValue);
		Assert.assertEquals(newCase.getId(), caseValue.getId());
		Assert.assertEquals(foundCaseOrder, caseValue.getOrder());
		
		// And check we've bumped the order
		Cases refoundCase = studyRepository.getStudyCase(study, foundCase.getId());
		Assert.assertThat(caseValue.getOrder(), Matchers.lessThan(refoundCase.getOrder()));
		Assert.assertThat(foundCase.getOrder(), Matchers.not(refoundCase.getOrder()));
	}

	/**
	 * Simple test of adding a number of attributes as well as deleting.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testFailingNewCase() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		
		QueryDslJdbcTemplate mockTemplate = createMock(QueryDslJdbcTemplate.class);
		expect(mockTemplate.newSqlQuery()).andStubReturn(studyRepository.getTemplate().newSqlQuery());
		expect(mockTemplate.queryForObject(anyObject(SQLQuery.class), (Expression<?>) anyObject(Expression.class))).andStubReturn(null);
		expect(mockTemplate.update(eq(QCases.cases), anyObject(SqlUpdateCallback.class))).andStubReturn(new Long(1));
		expect(mockTemplate.insertWithKey((RelationalPath<?>) anyObject(RelationalPath.class), (SqlInsertWithKeyCallback<?>) anyObject(SqlInsertWithKeyCallback.class))).andStubReturn(null);
		replay(mockTemplate);
		
		thrown.expect(InvalidValueException.class);
		thrown.expectMessage(containsString("Can't create new case"));
		
		QueryDslJdbcTemplate originalTemplate = studyRepository.getTemplate();
		studyRepository.setTemplate(mockTemplate);

		try {
			studyRepository.newStudyCase(study, "test");
		} finally {
			studyRepository.setTemplate(originalTemplate);
		}
	}

	/**
	 * Simple test of writing the exact same attributes back into the view. After
	 * we do this, a second call should retrieve the exact same data.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testGetStudyAttribute() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		Attributes attributes = studyRepository.getStudyAttribute(study, "patientId");
		
		Assert.assertEquals("patientId", attributes.getName());
		Assert.assertEquals("Patient ID", attributes.getLabel());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testNewCaseWithoutManager() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		Cases caseValue = studyRepository.getStudyCase(study, 7);
		
		studyRepository.setStudyCaseState(study, caseValue, "morag", "pending");
		
		// Check we now have an audit log entry
		CasePager pager = new CasePager();
		pager.setOffset(0);
		pager.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, pager);
		Assert.assertNotNull(auditEntries);
		Assert.assertEquals(1, auditEntries.size());
		
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("morag", entry.get("eventUser").asText());
		Assert.assertTrue(entry.get("eventArgs").get("old_state").isNull());
		Assert.assertEquals("pending", entry.get("eventArgs").get("state").asText());
		
		// Check a re-read gets the new state
		Cases foundValue = studyRepository.getStudyCase(study, 7);
		Assert.assertEquals("pending", foundValue.getState());
	}
	
	/**
	 * Checks that basic filtering works, with an exact match to a string
	 * @throws RepositoryException
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicFiltering() throws RepositoryException {
		
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("patientId", jsonNodeFactory.textNode("DEMO-02"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(1, dataList.size());
		
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);
		
		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-02", data.get("patientId").asText());
	}

	/**
	 * Checks that basic filtering works, with an exact match to a string
	 * @throws RepositoryException
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicFilteringWithSpaces() throws RepositoryException {
		
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("physician", jsonNodeFactory.textNode("Dr. Z"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(2, dataList.size());
		
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);
		
		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-03", data.get("patientId").asText());
	}

	/**
	 * Checks that basic filtering works, with an exact match to a string
	 * failing to find any records at all.
	 * @throws RepositoryException
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicFilteringMiss() throws RepositoryException {
		
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("patientId", jsonNodeFactory.textNode("MISSING"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(0, dataList.size());
	}
	
	/**
	 * Checks that empty cases still have an identifier field set.
	 * @throws RepositoryException
	 */
	@Test
	@Transactional
	@Rollback(true)
	// Regression test for #162
	public void testEmptyCases() throws RepositoryException {
		
		Study study = studyRepository.getStudy("SECOND");
		View view = studyRepository.getStudyView(study, "complete");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		for(ObjectNode node : dataList) {
			Assert.assertTrue(node.has("id"));
		}
	}
	
	/**
	 * Blanks are a special case. They might be NULL or they might be an
	 * empty string, so we need to check for both in the underlying 
	 * query that we generate. 
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicFilteringBlank() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("mrn", jsonNodeFactory.textNode("\"\""));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(15, dataList.size());
	
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);

		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-05", data.get("patientId").asText());
	}

	/**
	 * N/A is a special case. The filter needs to check the missing
	 * value field rather than the usual value field.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicFilteringNA() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("tissueSite", jsonNodeFactory.textNode("N/A"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(1, dataList.size());
	
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);

		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-06", data.get("patientId").asText());
	}
	
	/**
	 * Wildcards are another filter option, and we should check both pre and
	 * postfix values.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicFilteringWildcardPrefix() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("patientId", jsonNodeFactory.textNode("*-05"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(1, dataList.size());
	
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);

		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-05", data.get("patientId").asText());
	}

	
	/**
	 * Wildcards are another filter option, and we should check both pre and
	 * postfix values.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicFilteringWildcardSuffix() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("patientId", jsonNodeFactory.textNode("DEMO-0*"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(10, dataList.size());
	
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);

		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-01", data.get("patientId").asText());
	}

	/**
	 * Wildcards are another filter option, and we should check both pre and
	 * postfix values.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicFilteringExpression() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("tissueSite", jsonNodeFactory.textNode("N/A OR *lung*"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(2, dataList.size());
	
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-03", data.get("patientId").asText());

		data = dataList.get(1);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-06", data.get("patientId").asText());
	}

	/**
	 * Booleans are another filter option,
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicBooleanFilteringExpression() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("specimenAvailable", jsonNodeFactory.textNode("No"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(3, dataList.size());
	
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-02", data.get("patientId").asText());
	}

	/**
	 * Dates are another filter option,
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicDateFilteringExpression() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("consentDate", jsonNodeFactory.textNode("2014-08-18"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(1, dataList.size());
	
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("DEMO-02", data.get("patientId").asText());
	}

	/**
	 * Dates are another filter option,
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicDateFilteringAfterExpression() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("dateEntered", jsonNodeFactory.textNode("AFTER 2014-08-23"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(13, dataList.size());

		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("2014-08-23", data.get("dateEntered").asText());
	}

	/**
	 * Dates are another filter option,
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicDateFilteringBeforeExpression() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("dateEntered", jsonNodeFactory.textNode("BEFORE 2014-08-27"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(12, dataList.size());
	
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("2014-08-20", data.get("dateEntered").asText());
	}

	/**
	 * Dates are another filter option,
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testBasicDateFilteringCombinedxpression() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("dateEntered", jsonNodeFactory.textNode("AFTER 2014-08-23 AND BEFORE 2014-08-27"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(5, dataList.size());
		
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.has("patientId"));
		Assert.assertEquals("2014-08-23", data.get("dateEntered").asText());
	}

	/**
	 * Wildcards are another filter option, and we should check both pre and
	 * postfix values.
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testMultipleFiltering1() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("patientId", jsonNodeFactory.textNode("DEMO-03"));
		filter.replace("sampleAvailable", jsonNodeFactory.textNode("LMP"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(1, dataList.size());
	
		JsonNode data = dataList.get(0);
		Assert.assertNotNull(data);

		Assert.assertTrue(data.has("specimenNo"));
		Assert.assertEquals("S12-3000", data.get("specimenNo").asText());
	}

	/**
	 * Multiple filters with a blank value seem to be an issue, so let's test
	 * that case too. 
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testMultipleFilteringBlankRegressionDate() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("physician", jsonNodeFactory.textNode(""));
		filter.replace("patientId", jsonNodeFactory.textNode("DEMO-0*"));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(10, dataList.size());
	}

	/**
	 * Multiple filters with a blank value seem to be an issue, so let's test
	 * that case too. 
	 * <p>
	 * Regression for #101 - error filtering by blank string for dates and booleans
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testMultipleFilteringBlankRegressionBoolean() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("sampleAvailable", jsonNodeFactory.textNode(""));
		filter.replace("consentDate", jsonNodeFactory.textNode("\"\""));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(15, dataList.size());
	}

	/**
	 * Multiple filters with a blank value seem to be an issue, so let's test
	 * that case too. 
	 * <p>
	 * Regression for #101 - error filtering by blank string for dates and booleans
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testMultipleFiltering3() throws RepositoryException {

		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "complete");

		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		
		ObjectNode filter = jsonNodeFactory.objectNode();
		filter.replace("sampleAvailable", jsonNodeFactory.textNode(""));
		filter.replace("specimenAvailable", jsonNodeFactory.textNode("\"\""));

		StudyCaseQuery filteredQuery = studyRepository.addStudyCaseFilterSelector(query, filter);
		
		List<ObjectNode> dataList = studyRepository.getCaseData(filteredQuery, view);
		Assert.assertNotNull(dataList);
		Assert.assertEquals(16, dataList.size());
	}
	
	/**
	 * Tests that cases can be deleted using the studyRepository
	 * @throws RepositoryException
	 */
	@Test
	@Transactional
	@Rollback(true)
	public void testDeleteCase() throws RepositoryException {
		Study study = studyRepository.getStudy("DEMO");
		View view = studyRepository.getStudyView(study, "track");
		
		// First check the data exists
		StudyCaseQuery query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addStudyCaseSelector(query, 1);
		
		List<ObjectNode> data = studyRepository.getCaseData(query, view);
		Assert.assertNotNull(data);
		Assert.assertEquals(1,  data.size());
		
		query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addStudyCaseSelector(query, 1);

		// Try the deletion
		studyRepository.deleteCases(query, "morag");
		
		// Now generate the query again, and confirm we can't find it
		query = studyRepository.newStudyCaseQuery(study);
		query = studyRepository.addStudyCaseSelector(query, 1);
		
		data = studyRepository.getCaseData(query, view);
		
		Assert.assertNotNull(data);
		Assert.assertEquals(0,  data.size());		
		
		CasePager pager = new CasePager();
		pager.setOffset(0);
		pager.setLimit(5);
		List<JsonNode> auditEntries = auditLogRepository.getAuditData(study, pager);
		
		Assert.assertEquals(1, auditEntries.size());
		JsonNode entry = auditEntries.get(0);
		Assert.assertEquals("delete", entry.get("eventType").asText());
		JsonNode entryData = entry.get("eventArgs").get("data");
		
		Assert.assertEquals("DEMO-01", entryData.get("patientId").asText());
	}
}
