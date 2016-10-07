package com.reda.wf.samples;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.log4j.Logger;

import com.reda.wf.REDAExecutionJavaDelegate;


public class TestJavaDelegate extends REDAExecutionJavaDelegate {

	private static Logger LOGGER = Logger.getLogger(TestJavaDelegate.class);
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		String eventName = execution.getEventName(); 
		LOGGER.info("eventName = " + eventName);
		String activityId = execution.getCurrentActivityId(); 
		LOGGER.info("activityId = " + activityId);
		String activityName = execution.getCurrentActivityName(); 
		LOGGER.info("activityName = " + activityName);
		
		LOGGER.info("bpm_package = " + execution.getVariable("bpm_package"));			
		LOGGER.info("bpm_context = " + execution.getVariable("bpm_context"));
				
		NodeService nodeService = getServiceRegistry().getNodeService();
		
		//getting documents attached to wf
		ActivitiScriptNode scriptNode = (ActivitiScriptNode)execution.getVariable(WorkflowNotificationUtils.PROP_PACKAGE);
		NodeRef packagenode = scriptNode.getNodeRef();
		List<ChildAssociationRef> childRefList  = getServiceRegistry().getNodeService().getChildAssocs(packagenode);
		
		NodeRef nodeRef = null;
		
		for (ChildAssociationRef childAssocRef : childRefList ) {
			// do something with each document in the workflow package
			nodeRef = childAssocRef.getChildRef();
			String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			LOGGER.debug("doc name = "+fileName);
		}
		
		ContentService contentSrv = getServiceRegistry().getContentService();
		contentSrv.getImageTransformer();
		
		
		SearchService searchService = getServiceRegistry().getSearchService();
		
		SearchParameters spf = new SearchParameters();
		spf.addStore(nodeRef.getStoreRef());
		spf.setLanguage(SearchService.LANGUAGE_LUCENE);
		
		StringBuffer queryF = new StringBuffer();
		queryF.append("TYPE:\"cm:folder\" AND @cm\\:name:\"");
		queryF.append("\"");
		
		LOGGER.debug("query: " + queryF.toString());
		
		spf.setQuery(queryF.toString());
		ResultSet resultsF = searchService.query(spf);
		LOGGER.debug("results.length: " + resultsF.length());
		if (resultsF.length() == 1)
		{
			for (ResultSetRow rowF : resultsF)
			{
				NodeRef folderNodeRef = rowF.getNodeRef();						
				//premjesti folder u dohvaćeni folder
//				nodeService.moveNode(actionNodeRef, folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN);
				// stavi da je likvidiran
//				nodeService.setProperty(actionNodeRef, IngentisConstants.FOLDER_RACUN_LIKVIDIRAN, new Boolean(true));
			}
		}
		
		
		// always call super to execute common logic
		super.execute(execution);
		

	}
	
//	private void setupSpaceRuleConvToPDF(NodeRef data){
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


	/**
	 * Example of sending email using Alfresco OOTB action
	 */
	private void sendEmail () {
		
		ActionService actionService = getServiceRegistry().getActionService();
		Action mailAction = actionService.createAction(MailActionExecuter.NAME);
		mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "");       
		mailAction.setParameterValue(MailActionExecuter.PARAM_TO, "");
		mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, "");
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, "");
 
		// Define Model
		String templatePATH = "PATH:\"/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:wf-custom-remind.ftl\"";
		ResultSet resultSet = getServiceRegistry().getSearchService().query(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), SearchService.LANGUAGE_LUCENE, templatePATH);
		if (resultSet.length()==0){
			LOGGER.error("Template "+ templatePATH+" not found.");
			return;
		}
		NodeRef template = resultSet.getNodeRef(0);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, template);
		// Define parameters for the model (set fields in the ftl like : args.workflowTitle)
		Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
		templateArgs.put("workflowTitle","a");
		templateArgs.put("workflowPooled", false);
		templateArgs.put("workflowPriority", 1);
		templateArgs.put("workflowDescription", "d");
		templateArgs.put("workflowId",1);
		Map<String, Serializable> templateModel = new HashMap<String, Serializable>();
		templateModel.put("args",(Serializable)templateArgs);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL,(Serializable)templateModel);
 
		actionService.executeAction(mailAction, null);
	}

}
