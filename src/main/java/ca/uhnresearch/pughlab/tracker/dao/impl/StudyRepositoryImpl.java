package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;

import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLSubQuery;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.query.ListSubQuery;

import ca.uhnresearch.pughlab.tracker.dao.CaseQuery;
import ca.uhnresearch.pughlab.tracker.dao.StudyRepository;
import ca.uhnresearch.pughlab.tracker.domain.*;
import static ca.uhnresearch.pughlab.tracker.domain.QAttributes.attributes;
import static ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeBooleans.caseAttributeBooleans;
import static ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeDates.caseAttributeDates;
import static ca.uhnresearch.pughlab.tracker.domain.QCaseAttributeStrings.caseAttributeStrings;
import static ca.uhnresearch.pughlab.tracker.domain.QCases.cases;
import static ca.uhnresearch.pughlab.tracker.domain.QStudies.studies;
import static ca.uhnresearch.pughlab.tracker.domain.QViewAttributes.viewAttributes;
import static ca.uhnresearch.pughlab.tracker.domain.QViews.views;

public class StudyRepositoryImpl implements StudyRepository {
	
	private final Logger logger = LoggerFactory.getLogger(StudyRepositoryImpl.class);
	
	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

	private QueryDslJdbcTemplate template;

	@Required
    public void setTemplate(QueryDslJdbcTemplate template) {
        this.template = template;
    }

    /**
     * Returns a list of studies
     * @param study
     * @return
     */
    public List<Studies> getAllStudies() {
		logger.debug("Looking for all studies");

    	SQLQuery sqlQuery = template.newSqlQuery().from(studies);
    	List<Studies> studyList = template.query(sqlQuery, studies);
    	logger.info("Got some studies: {}", studyList.toString());

    	return studyList;
    }

    /**
     * Returns a named study
     * @param study
     * @return
     */
	public Studies getStudy(String name) {
		logger.debug("Looking for study by name: {}", name);
    	SQLQuery sqlQuery = template.newSqlQuery().from(studies).where(studies.name.eq(name));
    	Studies study = template.queryForObject(sqlQuery, studies);
    	
    	if (study != null) {
    		logger.info("Got a study: {}", study.toString());
    	} else {
    		logger.info("No study found");
    	}
    	
    	return study;
	}

    /**
     * Returns the list of views associated with a study
     * @param study
     * @return
     */
	public List<Views> getStudyViews(Studies study) {
		logger.debug("Looking for views for study: {}", study.getName());
    	SQLQuery sqlQuery = template.newSqlQuery().from(views).where(views.studyId.eq(study.getId()));
    	List<Views> viewList = template.query(sqlQuery, views);
    	logger.info("Got some views: {}", viewList.toString());

		return viewList;
	}

    /**
     * Returns the named view associated with a study
     * @param study
     * @return
     */
	public Views getStudyView(Studies study, String name) {
		logger.debug("Looking for study by name: {}", name);
    	SQLQuery sqlQuery = template.newSqlQuery().from(views).where(views.name.eq(name).and(views.studyId.eq(study.getId())));
    	Views view = template.queryForObject(sqlQuery, views);
    	
    	if (views != null) {
    		logger.info("Got a view: {}", views.toString());
    	} else {
    		logger.info("No study found");
    	}
    	
    	return view;
	}

    /**
     * Returns a list of all the attributes for a given view. 
     * @param study
     * @param view
     * @return
     */
	public List<Attributes> getViewAttributes(Studies study, Views view) {
		logger.debug("Looking for view attributes");
		SQLQuery sqlQuery = template.newSqlQuery().from(attributes)
    	    .innerJoin(viewAttributes).on(attributes.id.eq(viewAttributes.attributeId))
    	    .innerJoin(views).on(views.id.eq(viewAttributes.viewId))
    	    .where(attributes.studyId.eq(study.getId()).and(views.id.eq(view.getId())))
    	    .orderBy(viewAttributes.rank.asc());
		
		logger.info("Executing query: {}", sqlQuery.toString());

    	List<Attributes> attributeList = template.query(sqlQuery, attributes);
    	return attributeList;
	}

	/**
	 * Generates an SQLQuery on cases from a CaseQuery object. This can then be incorporated
	 * into the queries that are used to access data.
	 * @param query
	 * @return
	 */
	private ListSubQuery<Integer> getStudyCaseSubQuery(CaseQuery query) {
		SQLSubQuery sq = new SQLSubQuery().from(cases);
		
		// If we have an ordering, use a left join to get the attribute, and order it later
		if (query.orderField != null) {
			QCaseAttributeStrings c = new QCaseAttributeStrings("c");
			sq = sq.leftJoin(c).on(c.caseId.eq(cases.id).and(c.attribute.eq(query.orderField)));
			OrderSpecifier<String> ordering = (query.orderDirection == CaseQuery.OrderDirection.ASC) ? c.value.asc() : c.value.desc();
			sq = sq.orderBy(ordering);
		}
		
		if (query.offset != null) {
			sq = sq.offset(query.offset);
		}
		if (query.limit != null) {
			sq = sq.limit(query.limit);
		}
	
		return sq.list(cases.id);
	}
	
	/**
	 * Writes values from a set of data tuples retrieved with a very specific order within getData,
	 * and wires them into JSON values for returning. 
	 * @param table
	 * @param values
	 */
	private void writeTupleAttributes(Map<Integer, ObjectNode> table, List<Tuple> values) {
		for(Tuple v : values) {
			Integer caseId = v.get(0, Integer.class);
			String attributeName = v.get(1, String.class);
			Object value = v.get(2, Object.class);
			Boolean notAvailable = v.get(3, Boolean.class);
			ObjectNode obj = table.get(caseId);
			assert obj != null;
			if (notAvailable != null && notAvailable) {
				ObjectNode marked = jsonNodeFactory.objectNode();
				marked.put("$notAvailable", Boolean.TRUE);
				obj.put(attributeName, marked);
			} else if (value == null) {
				obj.put(attributeName, (String) null);
			} else if (value instanceof String) {
				obj.put(attributeName, (String) value);
			} else if (value instanceof Date) {
				obj.put(attributeName, ((Date) value).toString());
			} else if (value instanceof Boolean) {
				obj.put(attributeName, (Boolean) value);
			} else {
				throw new RuntimeException("Invalid attribute type: " + value.getClass().getCanonicalName());
			}
		}
	}
	
	public List<JsonNode> getData(Studies study, Views view, List<Attributes> attributes, CaseQuery query) {
		// This method retrieves the attributes we needed. In most implementations, we've done 
		// this as a UNION in SQL and accepted dynamic types. We probably can't assume this, and
		// since UNIONs generally aren't indexable, we are probably genuinely better off running
		// separate queries for each primitive attribute type, and then assembling them in this
		// method. This hugely reduces the complexity of the DSL here too. 
		
		ListSubQuery<Integer> caseQuery = getStudyCaseSubQuery(query);
		Map<Integer, ObjectNode> table = new HashMap<Integer, ObjectNode>();
		
		SQLQuery caseIdQuery = template.newSqlQuery().from(caseQuery.as(cases));
		List<Integer> caseIds = template.query(caseIdQuery, cases.id);

		List<JsonNode> objects = new ArrayList<JsonNode>(caseIds.size());
		
		Integer index = 0;
		for(Integer id : caseIds) {
			ObjectNode obj = jsonNodeFactory.objectNode();
			objects.add(index++, (JsonNode) obj);
			table.put(id, obj);
		}
		
		// Right. Now we can add in the attributes from a set of related queries, using the same basic
		// case query as a starting point. Yes, we're re-doing this query more times than I'd like, but
		// we can optimize later.
				
		SQLQuery sqlQuery;
		List<Tuple> values;
		
		sqlQuery = template.newSqlQuery().from(caseQuery.as(cases)).innerJoin(caseAttributeStrings).on(cases.id.eq(caseAttributeStrings.caseId))
				.where(caseAttributeStrings.active.eq(true));
		values = template.query(sqlQuery, new QTuple(caseAttributeStrings.caseId, caseAttributeStrings.attribute, caseAttributeStrings.value, caseAttributeStrings.notAvailable, caseAttributeStrings.notes));
		writeTupleAttributes(table, values);

		sqlQuery = template.newSqlQuery().from(caseQuery.as(cases)).innerJoin(caseAttributeDates).on(cases.id.eq(caseAttributeDates.caseId))
			.where(caseAttributeDates.active.eq(true));
		values = template.query(sqlQuery, new QTuple(caseAttributeDates.caseId, caseAttributeDates.attribute, caseAttributeDates.value, caseAttributeDates.notAvailable, caseAttributeDates.notes));
		writeTupleAttributes(table, values);

		sqlQuery = template.newSqlQuery().from(caseQuery.as(cases)).innerJoin(caseAttributeBooleans).on(cases.id.eq(caseAttributeBooleans.caseId))
			.where(caseAttributeBooleans.active.eq(true));
		values = template.query(sqlQuery, new QTuple(caseAttributeBooleans.caseId, caseAttributeBooleans.attribute, caseAttributeBooleans.value, caseAttributeBooleans.notAvailable, caseAttributeBooleans.notes));
		writeTupleAttributes(table, values);

		return objects;
	}	
}

