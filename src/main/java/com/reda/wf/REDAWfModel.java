package com.reda.wf;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Helper class for REDA workflow
 * @author Zoran Milakovic
 *
 */
public class REDAWfModel {
	
	public static final QName PO_CASE_TYPE_NAME = QName.createQName("http://www.redachem.com/model/content/1.0", "doc");
	
	public static String WF_DUE_DATE = "bpm_workflowDueDate";
	public static String WF_TASK_DUE_DATE = "bpm_dueDate";
	public static String WF_TOPS_DUE_DATE = "rdwf_topsDueDate";
	
	public static String WF_TOP_IS_SUBMISSION_OK = "rdwf_topsSubmissionOK";
	
	public static String WF_WORKFLOW_PRIORITY = "bpm_workflowPriority";
	public static String WF_TASK_PRIORITY = "bpm_priority";
	public static String WF_TASK_STATUS = "bpm_status";
	
	public static String WF_SEND_EMAIL_NOTIFICATIONS = "bpm_sendEMailNotifications";
	
	public static String WF_COMMENT = "bpm_comment";	
	public static String WF_HISTORY_COMMENT = "rdwf_historyComments";
	
	public static String WF_TOPS_MGR_APPR_STATUS = "rdwf_topsApprovalStatus";
	public static String WF_TOPS_MGR_APPR_STATUS_ADDITIONAL_REVIEW = "Additional Review";
	public static String WF_TOPS_MGR_APPR_STATUS_APPROVED = "Approved";
	
	public static String WF_TASK_STATUS_IN_PROGRESS = "In Progress";
	
	public static String WF_TOPS_NUMBER = "rdwf_topsNumber";
	public static String WF_ADDITIONAL_REVIEWER =  "rdwf_additionalreviewer";
	public static String WF_SELECTED_PROCESSOR =  "rdwf_selectedprocessor";
	public static String WF_USE_SELECTED_PROCESSOR =  "rdwf_useSelectedProcessor";
	public static String WF_GP_PO_NUMBER =  "rdwf_GPPONumber";

			
	public static String WF_DESCRIPTION = "bpm_workflowDescription";
			
	private static Logger LOGGER = Logger.getLogger(REDAWfModel.class);
	

	/**
	 * Copy variable value from Execution to Task
	 * @param task
	 * @param varName
	 * @throws Exception
	 */
	public static void copyExecToLocal(DelegateTask task, String varName) throws Exception {
		
		DelegateExecution execution = task.getExecution();
								
		Object varValue = execution.getVariable(varName);
		LOGGER.info(varName + " = " + varValue);
		
		if(varValue != null) {
			task.setVariable(varName, varValue);
		}
				
	}
	
	/**
	 * Copy variable value from Task to Execution
	 * @param task
	 * @param varName
	 * @throws Exception
	 */
	public static void copyLocalToExec(DelegateTask task, String varName) throws Exception {
		
		DelegateExecution execution = task.getExecution();
				
		Object varValue = task.getVariable(varName);
		LOGGER.debug(varName + " = " + varValue);
		
		if(varValue != null) {
			execution.setVariable(varName, varValue);
		}
				
	}
}
