package com.reda.wf;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.log4j.Logger;


public class InitTOPS extends REDAExecutionJavaDelegate {

	private static Logger LOGGER = Logger.getLogger(InitTOPS.class);
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		final DelegateExecution executionLocal = execution;
		
		boolean isSubmissionOK = false;
		
		String eventName = executionLocal.getEventName(); 
		LOGGER.info("eventName = " + eventName);
		String activityId = executionLocal.getCurrentActivityId(); 
		LOGGER.info("activityId = " + activityId);
		String activityName = executionLocal.getCurrentActivityName(); 
		LOGGER.info("activityName = " + activityName);
		
		LOGGER.info("bpm_package = " + executionLocal.getVariable("bpm_package"));			
		LOGGER.info("bpm_context = " + executionLocal.getVariable("bpm_context"));
		
		Object wfDueDate = executionLocal.getVariable(REDAWfModel.WF_DUE_DATE);
		Object wfTopsDueDate = executionLocal.getVariable(REDAWfModel.WF_TOPS_DUE_DATE);
		LOGGER.info("wf Due Date " + wfDueDate+", tops due date "+wfTopsDueDate);	

		//set tops no
		execution.setVariable(REDAWfModel.WF_DUE_DATE, wfTopsDueDate);
		
				
		final NodeService nodeService = getServiceRegistry().getNodeService();
		final SearchService searchService = getServiceRegistry().getSearchService();
		final PermissionService permissionService = getServiceRegistry().getPermissionService();
		
		
		//getting documents attached to wf
		ActivitiScriptNode scriptNode = (ActivitiScriptNode)executionLocal.getVariable(WorkflowNotificationUtils.PROP_PACKAGE);
		NodeRef packagenode = scriptNode.getNodeRef();
		List<ChildAssociationRef> childRefList  = getServiceRegistry().getNodeService().getChildAssocs(packagenode);
		
		
		NodeRef nodeRef = null;
		
		String topsNo = "";
		for (ChildAssociationRef childAssocRef : childRefList ) {
			// do something with each document in the workflow package
			nodeRef = childAssocRef.getChildRef();
			LOGGER.debug("Node Ref = "+nodeRef);
			String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			LOGGER.debug("doc name = "+fileName);
			if(nodeService.hasAspect(nodeRef, REDAContentModel.TOPS_ASPECT_QNAME)) {
				LOGGER.debug("Setting approved value of TOPS");
				//at least one document is attached, and is of type TOPS
				isSubmissionOK = true;
				LOGGER.debug("TOPS NO QNAME = "+REDAContentModel.PROP_TOPS_NUMBER);
				topsNo = (String) nodeService.getProperty(nodeRef, REDAContentModel.PROP_TOPS_NUMBER);
				LOGGER.debug("TOPS Number = "+topsNo);
			}
			
		}
		
		//if submission is not OK, throw an exception
		if(!isSubmissionOK)
			throw new Exception("At least one attached document must be TOPS sheet");

		final String ADMIN_USERNAME = "admin";
		
		
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>() {
			@Override
			public Boolean doWork() throws Exception {

				//getting documents attached to wf
				ActivitiScriptNode scriptNode = (ActivitiScriptNode)executionLocal.getVariable(WorkflowNotificationUtils.PROP_PACKAGE);
				NodeRef packagenode = scriptNode.getNodeRef();
				List<ChildAssociationRef> childRefList  = getServiceRegistry().getNodeService().getChildAssocs(packagenode);

				for (ChildAssociationRef childAssocRef : childRefList ) {
					// do something with each document in the workflow package
					final NodeRef nodeRef = childAssocRef.getChildRef();
					if(nodeService.hasAspect(nodeRef, REDAContentModel.TOPS_ASPECT_QNAME)) {
						LOGGER.debug("Setting approved value of TOPS");
						
						
						//get parent folder
						ChildAssociationRef childAssociationRef = nodeService.getPrimaryParent(nodeRef);
					    NodeRef parentFolder = childAssociationRef.getParentRef();
					    LOGGER.debug("Parent folder "+parentFolder);

						nodeService.setProperty(nodeRef, REDAContentModel.PROP_TOPS_STATUS, REDAContentModel.TOPS_STATUS_UNDER_APPROVAL);
						LOGGER.debug("Set status to Under Approval to "+nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));

						nodeService.setProperty(parentFolder, REDAContentModel.PROP_TOPS_STATUS_FOLDER, REDAContentModel.TOPS_STATUS_UNDER_APPROVAL);
						LOGGER.debug("Set status to Under Approval to FOLDER "+nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));

						String name = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
						name = name.replace("(" + REDAContentModel.TOPS_STATUS_DRAFT.toUpperCase()+ ")", "(" + REDAContentModel.TOPS_STATUS_UNDER_APPROVAL.toUpperCase()+ ")");
						nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
						LOGGER.debug("New name set to "+name);
						
						//assign category
						//add category Approved
						StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
						ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/cm:generalclassifiable/cm:TOPS_x0020_Status/cm:Under_x0020_Review\"");
						NodeRef categoryNode = null;
						
						try	{
							if (rs.length() == 0) 	{
								throw new AlfrescoRuntimeException("Didn't find Category");
							}
							categoryNode = rs.getNodeRef(0);
						}
						finally	{
							rs.close();
						}
						
						//add category
						ArrayList<NodeRef> categories = new ArrayList<NodeRef>(1);
						categories.add(categoryNode);
						nodeService.setProperty(nodeRef, ContentModel.PROP_CATEGORIES, categories);
					
						PermissionsUtils.setPermissionsForUNDERAPPROVAL(nodeRef, parentFolder, nodeService, permissionService, getServiceRegistry().getPersonService());
						
					} else {
						LOGGER.debug("No TOPS aspect for document "+nodeRef);
					}
					
				}

				return true;
			}
		}, 
		ADMIN_USERNAME);
		
		//set workflow description
		execution.setVariable(REDAWfModel.WF_DESCRIPTION, topsNo);
		//set tops no
		execution.setVariable(REDAWfModel.WF_TOPS_NUMBER, topsNo);
		
		//is submission OK
		execution.setVariable(REDAWfModel.WF_TOP_IS_SUBMISSION_OK, isSubmissionOK);
		
		LOGGER.debug("All variables: "+execution.getVariables());
		
		// always call super to execute common logic
		super.execute(execution);
		

	}
	
//	private void setupSpaceRuleConvToPDF(NodeRef data){
	
	
//	
//	ContentService contentSrv = getServiceRegistry().getContentService();
//	contentSrv.getImageTransformer();
//	
//	
//	SearchService searchService = getServiceRegistry().getSearchService();
//	
//	SearchParameters spf = new SearchParameters();
//	spf.addStore(nodeRef.getStoreRef());
//	spf.setLanguage(SearchService.LANGUAGE_LUCENE);
//	
//	StringBuffer queryF = new StringBuffer();
//	queryF.append("TYPE:\"cm:folder\" AND @cm\\:name:\"");
//	queryF.append("\"");
//	
//	LOGGER.debug("query: " + queryF.toString());
//	
//	spf.setQuery(queryF.toString());
//	ResultSet resultsF = searchService.query(spf);
//	LOGGER.debug("results.length: " + resultsF.length());
//	if (resultsF.length() == 1)
//	{
//		for (ResultSetRow rowF : resultsF)
//		{
//			NodeRef folderNodeRef = rowF.getNodeRef();						
//			//premjesti folder u dohvaćeni folder
////			nodeService.moveNode(actionNodeRef, folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN);
//			// stavi da je likvidiran
////			nodeService.setProperty(actionNodeRef, IngentisConstants.FOLDER_RACUN_LIKVIDIRAN, new Boolean(true));
//		}
//	}
	
	
	
//
//		Rule rule=new Rule();
//	    rule.setRuleType("inbound");
//	    rule.setTitle("Transform to PDF n copy to Data-PDF");
//	    rule.setDescription("Transform to PDF n copy to Data-PDF");
//	    rule.applyToChildren(false);
//          // rule.setExecuteAsynchronously(true);
//
//        CompositeAction compositeAction = getServiceRegistry().getActionService().createCompositeAction();
//        rule.setAction(compositeAction);
//        ActionCondition actionCondition = getServiceRegistry().getActionService().createActionCondition("no-condition");
//        actionCondition.setParameterValues(new HashMap());
//        compositeAction.addActionCondition(actionCondition);
//        IHandler handler=null;
//        Map repoActionParams = new HashMap();
//        Map actionParams=new HashMap();
//
//        NodeRef destination = new NodeRef(Repository.getStoreRef(),(String)getServiceRegistry().getNodeService().getProperty(data, ContentModel.ASPECT_COPIEDFROM));
//        actionParams.put("actionName", "transform");
//        actionParams.put("transformer", "application/pdf");
//        actionParams.put("destinationLocation",destination); // prntDestinationNodeRef
//		actionParams.put("actionSummary", "Transform and copy content to a specific space");
//		try {
//
//			 handler=(IHandler)Class.forName("org.alfresco.web.bean.actions.handlers.TransformHandler").newInstance();
//		} catch (Exception e) {
//			LOGGER.debug("Class Generation Exception:"+e);
//		}
//
//		if(handler != null){
//			handler.prepareForSave(actionParams, repoActionParams);
//		}
//
//		 //LOGGER.debug("B4 saving action");
//
//		Action action = getServiceRegistry().getActionService().createAction("transform");
//		action.setParameterValues(repoActionParams);
//        compositeAction.addAction(action);
//        getServiceRegistry().getRuleService().saveRule(data, rule);
//        
//	}

//	public void transformToPdf() {
//		ContentService contentService = getServiceRegistry().getContentService();
//		ContentReader pptReader = contentService.getReader(pptNodeRef, ContentModel.PROP_CONTENT);
//		ContentWriter pdfWriter = contentService.getWriter(pdfNodeRef, ContentModel.PROP_CONTENT, true);
//		ContentTransformer pptToPdfTransformer =
//				contentService.getTransformer(MimetypeMap.MIMETYPE_PPT, MimetypeMap.MIMETYPE_PDF);
//		pptToPdfTransformer.transform(pptReader, pdfWriter);
//	}


//	/**
//	 * Example of sending email using Alfresco OOTB action
//	 */
//	private void sendEmail () {
//		
//		ActionService actionService = getServiceRegistry().getActionService();
//		Action mailAction = actionService.createAction(MailActionExecuter.NAME);
//		mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "");       
//		mailAction.setParameterValue(MailActionExecuter.PARAM_TO, "");
//		mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "");
//		mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, "");
// 
//		// Define Model
//		String templatePATH = "PATH:\"/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:wf-custom-remind.ftl\"";
//		ResultSet resultSet = getServiceRegistry().getSearchService().query(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), SearchService.LANGUAGE_LUCENE, templatePATH);
//		if (resultSet.length()==0){
//			LOGGER.error("Template "+ templatePATH+" not found.");
//			return;
//		}
//		NodeRef template = resultSet.getNodeRef(0);
//		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, template);
//		// Define parameters for the model (set fields in the ftl like : args.workflowTitle)
//		Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
//		templateArgs.put("workflowTitle","a");
//		templateArgs.put("workflowPooled", false);
//		templateArgs.put("workflowPriority", 1);
//		templateArgs.put("workflowDescription", "d");
//		templateArgs.put("workflowId",1);
//		Map<String, Serializable> templateModel = new HashMap<String, Serializable>();
//		templateModel.put("args",(Serializable)templateArgs);
//		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL,(Serializable)templateModel);
// 
//		actionService.executeAction(mailAction, null);
//	}

}
