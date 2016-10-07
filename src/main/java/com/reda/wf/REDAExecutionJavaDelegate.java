package com.reda.wf;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;

/**
 * Common class for all Service Task type of activity delegation listeners
 * @author Zoran Milakovic
 *
 */
public class REDAExecutionJavaDelegate extends BaseJavaDelegate {

	private static Logger LOGGER = Logger.getLogger(REDAExecutionJavaDelegate.class);
	
	private NodeService nodeServiceInternal;
	
	public NodeService getNodeServiceInternal() {
		return nodeServiceInternal;
	}

	public void setNodeServiceInternal(NodeService nodeServiceInternal) {
		this.nodeServiceInternal = nodeServiceInternal;
	}
	
	public void execute(DelegateExecution execution) throws Exception {
		//some common logic for all execution listeners in the project
		LOGGER.debug("Execution = "+execution);
		LOGGER.debug("ServiceRegistry = "+ getServiceRegistry());
		LOGGER.debug("Event Name = "+ execution.getEventName());
	}
	
}
