var mail = actions.create("mail");
mail.parameters.to = "zoran.milakovic@gmail.com";
mail.parameters.subject = "New Purchase Order has been created "+document.name;
mail.parameters.from = "alfresco@reda.com";
mail.parameters.text = "Purchase Order " + document.name + " has been submitted. \n Link http://localhost:8080/share/page/site/mytestsite/documentlibrary#filter=path%7C%2FPurchase%2520Orders%2F" + document.name.replaceAll(' ','%2520') + "&page=1" ;
logger.log(mail.parameters.text);
mail.execute(document);


/*

document.downloadUrl = /d/a/workspace/SpacesStore/c1cae3b0-307b-42df-90da-f8e7fa982efc/PO%20Template.docx

  Working link for documents:
http://localhost:8080/share/proxy/alfresco/api/node/content/workspace/SpacesStore/c1cae3b0-307b-42df-90da-f8e7fa982efc/PO%20Template.docx

*/


/* Starting workflow with a document example */
function startWorkflow()
{
var nodeRef = "workspace://SpacesStore/25285e6c-2995-49fe-aa50-1270cefc806a";
var docNode = search.findNode(nodeRef);
   var workflowAct = actions.create("start-workflow");
   workflowAct.parameters.workflowName = "activiti$alfGroupReview";
   workflowAct.parameters["bpm:workflowDescription"] = "Please review ";
   workflowAct.parameters["bpm:groupAssignee"] = people.getGroup( "GROUP_aloha_collaborators");;
   var futureDate = new Date();
   futureDate.setDate(futureDate.getDate() + 7);
   workflowAct.parameters["bpm:workflowDueDate"] = futureDate; 
   workflowAct.execute(docNode);
   return ;
}


/* transform to pdf */
//var trans1 = doc1.transformDocument("application/pdf")

var workflowPackage = workflow.createPackage();
workflowPackage.addNode(node);


var propiedades = new Object();
propiedades["bpm:workflowDescription"] = "Solicitud de pago de factura " + document.name;
propiedades["bpm:assignee"] = person;
propiedades["bnvwf:departmentChief"] = people.getPerson('elacunza');
propiedades["bnvwf:mainChief"] = people.getPerson('jbastarrika');
propiedades["bnvwf:archiveFolder"] = companyhome.childByNamePath("Facturacion/No aceptadas");
propiedades["bnvwf:removeFolder"] = companyhome.childByNamePath("Facturacion/Pendientes de pago");
var workflowFactura = workflow.getDefinitionByName('jbpm$bnvwf:departamentalInvoicePayment');
var paquete = workflow.createPackage();
paquete.addNode(document);
var workflowNodoInicial = workflowFactura.startWorkflow(paquete, propiedades);
/**
 * Auto finalizar las tareas de inicio del workflow. 
 **/
var tareas = workflowNodoInicial.getTasks();
for (tarea in tareas) {
	tareas[tarea].endTask(null);
}





if (bpm_package != null &amp;&amp; bpm_package.children != null) {
    var i = 0;
    for (i = 0; i &lt; bpm_package.children.length; i++) {
	var document = bpm_package.children[i];
	document.addAspect("wf:workflowOutcomeAspect");
	document.properties["wf:workflowOutcome"] = "approved";
	document.save();
    }
}




function createSupplierPO() {
  	  
	      
  	  	var poTemplate = companyhome.childByNamePath("/data dictionary/space templates/PO KluberLubrication Template");
  	  	print(poTemplate);
  
  		
  		var caseFolder = companyhome.childByNamePath("/Sites/reda/documentLibrary/Purchase Orders/PO Case-3/Supplier PO");
  	  	print(caseFolder);
    
  
		create po from template
   		var supplierPO = poTemplate.copy(caseFolder);
   		if (supplierPO != null)
   		{
    	  	// remove Template from the name
	    	supplierPO.name = supplierPO.name.replace("Template", "").trim();
           	supplierPO.properties["cm:title"] = "";
   		   	supplierPO.save();
   		}
  
  
}

;

function getAllWf() {
  		
  		var myDoc = companyhome.childByNamePath("/Sites/reda/documentLibrary/Purchase Orders/PO Case-2/Customer PO/MoM Template.doc");
  		print(myDoc.activeWorkflows);
	  
  		for each ( wf in myDoc.activeWorkflows )
        { 
            print (wf.getId());
          	print (wf.isActive());
            print (wf.getPaths());
          
        } 
  
}



function main() {
   
  //createSupplierPO();
   	getAllWf();
}

var nodes = document.children;

for each(var node in nodes) {
  //rename to Subject
  var titleStr = node.properties["cm:title"];
  if(titleStr != null) {
    //node.name = titleStr;
    //node.save();
  	logger.log("New Name is " + node.properties["cm:title"]);
    logger.log("New adressee is " + node.properties["cm:addressee"]);
    logger.log("New addressees is " + node.properties["cm:addressees"]);
    logger.log("New originator is " + node.properties["cm:originator"]);
    logger.log("\n\r");
  } else {
  	logger.log("Title is null");
  }
  
  //logger.log(node.name + " (" + node.typeShort + "): " + node.nodeRef);
}


main();
