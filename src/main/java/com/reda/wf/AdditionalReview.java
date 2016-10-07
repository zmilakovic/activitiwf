package com.reda.wf;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.runtime.Execution;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;


public class AdditionalReview extends REDATaskJavaDelegate {

	
	private static final long serialVersionUID = 5232647705622239470L;
	
	private static Logger LOGGER = Logger.getLogger(AdditionalReview.class);
	
	/**
	 * execution.setVariable('rdwf_poFinanceApprovalStatus', task.getVariable('rdwf_poFinanceApprovalStatus'));
logger.log( "execution var rdwf_poFinanceApprovalStatus = "+execution.getVariable('rdwf_poFinanceApprovalStatus') );

execution.setVariable('rdwf_finComments', task.getVariable('rdwf_finComments'));
logger.log( "execution var rdwf_finComments = "+execution.getVariable('rdwf_finComments') );
	   
	 */
	
	
	public void onCreate(DelegateTask task) throws Exception {
		super.onCreate(task);
	}
	
	
	public void onComplete(DelegateTask task) throws Exception {
//		try {
//			
//			LOGGER.debug("ENTER");
//			
//			REDAWfModel.copyLocalToExec(task, REDAWfModel.WF_GP_PO_NUMBER);
//			
//			LOGGER.debug("LEAVE");
//			
//		} catch (Exception e) {
//			LOGGER.error(e);
//			throw e;
//		}
		super.onComplete(task);
	}
	

}
