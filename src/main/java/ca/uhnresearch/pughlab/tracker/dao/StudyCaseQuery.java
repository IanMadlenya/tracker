package ca.uhnresearch.pughlab.tracker.dao;

import ca.uhnresearch.pughlab.tracker.dto.Study;

/**
 * This represents an object that is generated by the StudyRepository to
 * select a set of cases, usually starting with the whole of a study.
 * 
 * @author stuartw
 */
public interface StudyCaseQuery {

	/**
	 * Retrieves a study.
	 * @return the study
	 */
	Study getStudy();
	
	/**
	 * Sets the study.
	 * @param study the study
	 */
	void setStudy(Study study);
	
}
