package com.reda.wf;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;


public class ProcessTOPS extends REDATaskJavaDelegate {

	
	private static final long serialVersionUID = 5232647705622239470L;
	
	private static Logger LOGGER = Logger.getLogger(ProcessTOPS.class);
	
	public void onCreate(DelegateTask task) throws Exception {
		super.onCreate(task);
	}
	
	
	public void onComplete(final DelegateTask task) throws Exception {
		try {
			
			LOGGER.debug("ENTER");
			
			AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>() {
				@Override
				public Boolean doWork() throws Exception {
					
					AuthenticationUtil.setFullyAuthenticatedUser(ReviewTOPS.ADMIN_USER_NAME);
					
					//check if the current user belongs to TOPS_APPROVERS authority
					AuthorityService authService = getServiceRegistry().getAuthorityService();	
					Set<String> groupsForUser = authService.getAuthoritiesForUser(task.getAssignee());
					if(!groupsForUser.contains(REDAContentModel.GROUP_REDA_TOPS_PROCESSORS)) {
						task.getExecution().setVariable(REDAContentModel.INVALID_PROCESSOR, "INVALID_VALUE");
						throw new Exception("User is not allowed to finish this task");
					}
					
					return true;
				}
			}, 
			ReviewTOPS.ADMIN_USER_NAME);
			
			REDAWfModel.copyLocalToExec(task, REDAWfModel.WF_GP_PO_NUMBER);
			
			LOGGER.debug("LEAVE");
			
		} catch (Exception e) {
			LOGGER.error(e);
			throw e;
		}
		
		super.onComplete(task);
	}
	

}
