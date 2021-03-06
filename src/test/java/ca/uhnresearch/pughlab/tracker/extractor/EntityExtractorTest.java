package ca.uhnresearch.pughlab.tracker.extractor;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.containsString;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;

import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyCaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.impl.MockStudyRepository;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;

public class EntityExtractorTest {

	private class TraceRestlet extends Restlet {
		// Does snothing, but prevents warning shouts
	}

	private EntityExtractor extractor;
	private StudyRepository repository = new MockStudyRepository();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initialize() {
		
		Restlet mock = new TraceRestlet();
		extractor = new EntityExtractor();
		extractor.setRepository(repository);
		extractor.setNext(mock);
	}

	@Test
	public void testBasicExtraction() {
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		
		Study study = repository.getStudy("DEMO");
		View view = repository.getStudyView(study, "complete");
		RequestAttributes.setRequestStudy(request, study);
		RequestAttributes.setRequestView(request, view);
		request.getAttributes().put("entityId", "5");

		extractor.handle(request, response);
		
		MockStudyCaseQuery query = (MockStudyCaseQuery) RequestAttributes.getRequestCaseQuery(request);
		
		assertEquals(1, query.getCases().size());
		assertEquals(5, query.getCases().get(0).intValue());
	}

	@Test
	public void testInvalidEntity() {
		
		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		
		Study study = repository.getStudy("DEMO");
		View view = repository.getStudyView(study, "complete");
		RequestAttributes.setRequestStudy(request, study);
		RequestAttributes.setRequestView(request, view);
		request.getAttributes().put("entityId", "");

		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Bad Request"));

		extractor.handle(request, response);
	}
}