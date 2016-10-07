package com.reda.wf;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.repo.workflow.activiti.BaseJavaDelegate;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;


/**
 * Common class for all User Task type of activity delegation listeners
 * @author Zoran Milakovic
 *
 */
public class REDATaskJavaDelegate extends BaseJavaDelegate implements TaskListener {
	
	
	private static Logger LOGGER = Logger.getLogger(REDATaskJavaDelegate.class);
	
	private static final long serialVersionUID = -7004759031605286447L;
	
	private NodeService nodeServiceInternal;
	
	public NodeService getNodeServiceInternal() {
		return nodeServiceInternal;
	}

	public void setNodeServiceInternal(NodeService nodeServiceInternal) {
		this.nodeServiceInternal = nodeServiceInternal;
	}

	public void execute(DelegateExecution execution) throws Exception {
		throw new Exception ("It is not expected to use this class "+this.getName()+" for Execution Delegation. Use ExecutionJavaDelegate instead");
	}
	
	/**
	 * Implement this method is the sub-class. 
	 * Method in this class is used for common logic for all Task Delegations
	 */
	@Override
	public void notify(DelegateTask task) {
		
		try {
			
			
			String eventName = task.getEventName(); 
			LOGGER.debug("eventName = " + eventName);
			String activityId = task.getId();
			LOGGER.debug("activityId = " + activityId);
			String activityName = task.getName(); 
			LOGGER.debug("activityName = " + activityName);
			
			//call method for appropriate event name		
			if(TaskListener.EVENTNAME_CREATE.equals(eventName)) {
				this.onCreate(task);
			} else if(TaskListener.EVENTNAME_COMPLETE.equals(eventName)) {
				this.onComplete(task);
			}
			
			
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		
		
	}
	
	public void onCreate(DelegateTask task) throws Exception {
//		task.setVariable(REDAWfModel.WF_DUE_DATE, task.getVariable(REDAWfModel.WF_TOPS_DUE_DATE));
//		task.setVariable(REDAWfModel.WF_TASK_DUE_DATE, task.getVariable(REDAWfModel.WF_TOPS_DUE_DATE));
		
		task.setDueDate((Date)task.getVariable(REDAWfModel.WF_TOPS_DUE_DATE));
		task.setPriority(new Integer(task.getVariable(REDAWfModel.WF_WORKFLOW_PRIORITY).toString()).intValue());
		task.setVariable(REDAWfModel.WF_TASK_STATUS, REDAWfModel.WF_TASK_STATUS_IN_PROGRESS);
		
		task.setVariable(REDAWfModel.WF_TASK_PRIORITY, task.getVariable(REDAWfModel.WF_WORKFLOW_PRIORITY));	
		
	}
	
	
	public void onComplete(DelegateTask task) throws Exception {
		String historyComments = "";
		if(task.getExecution().getVariable(REDAWfModel.WF_HISTORY_COMMENT) != null) {
			historyComments = task.getExecution().getVariable(REDAWfModel.WF_HISTORY_COMMENT).toString();
		}
		
		//based on the task, choose proper outcome varibale name
//		task.getVariable(REDAWfModel.WF_TOPS_MGR_APPR_STATUS);
		String taskName = task.getName();
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		String formattedDate = formatter.format(new Date());
		Object currentComments = task.getVariable(REDAWfModel.WF_COMMENT);
		if(currentComments != null)
			currentComments = currentComments.toString().replaceAll("\n", " ");
		historyComments += "\n --> " + task.getAssignee() + ": " + formattedDate + ": " + currentComments + " : ";
		LOGGER.debug("History comments: "+historyComments);
		task.getExecution().setVariable(REDAWfModel.WF_HISTORY_COMMENT, historyComments);


	}
	
}
