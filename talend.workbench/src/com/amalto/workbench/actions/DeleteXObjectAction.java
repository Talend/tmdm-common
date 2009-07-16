package com.amalto.workbench.actions;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;

import com.amalto.workbench.models.TreeObject;
import com.amalto.workbench.utils.ESystemDefaultObjects;
import com.amalto.workbench.utils.IConstants;
import com.amalto.workbench.utils.ImageCache;
import com.amalto.workbench.utils.Util;
import com.amalto.workbench.views.ServerView;
import com.amalto.workbench.webservices.WSDataClusterPK;
import com.amalto.workbench.webservices.WSDataModelPK;
import com.amalto.workbench.webservices.WSDeleteDataCluster;
import com.amalto.workbench.webservices.WSDeleteDataModel;
import com.amalto.workbench.webservices.WSDeleteMenu;
import com.amalto.workbench.webservices.WSDeleteRole;
import com.amalto.workbench.webservices.WSDeleteRoutingRule;
import com.amalto.workbench.webservices.WSDeleteStoredProcedure;
import com.amalto.workbench.webservices.WSDeleteSynchronizationPlan;
import com.amalto.workbench.webservices.WSDeleteTransformerV2;
import com.amalto.workbench.webservices.WSDeleteUniverse;
import com.amalto.workbench.webservices.WSDeleteView;
import com.amalto.workbench.webservices.WSGetRole;
import com.amalto.workbench.webservices.WSGetRolePKs;
import com.amalto.workbench.webservices.WSMenuPK;
import com.amalto.workbench.webservices.WSPutRole;
import com.amalto.workbench.webservices.WSRole;
import com.amalto.workbench.webservices.WSRolePK;
import com.amalto.workbench.webservices.WSRoleSpecification;
import com.amalto.workbench.webservices.WSRoleSpecificationInstance;
import com.amalto.workbench.webservices.WSRoutingRulePK;
import com.amalto.workbench.webservices.WSStoredProcedurePK;
import com.amalto.workbench.webservices.WSSynchronizationPlanPK;
import com.amalto.workbench.webservices.WSTransformerV2PK;
import com.amalto.workbench.webservices.WSUniversePK;
import com.amalto.workbench.webservices.WSViewPK;
import com.amalto.workbench.webservices.XtentisPort;

public class DeleteXObjectAction extends Action{

	private ServerView view = null;
	
	
	public DeleteXObjectAction(ServerView view) {
		super();
		this.view = view;
		setImageDescriptor(ImageCache.getImage( "icons/delete_obj.gif"));
		setText("Delete");
		setToolTipText("Delete this instance of the "+IConstants.TALEND+" Object");
	}
	
	public void run() {
		try {
			super.run();
			IStructuredSelection selection = (IStructuredSelection)view.getViewer().getSelection();
			//add the node here
			

			if(selection.isEmpty()){
				return;
			}
			else{
				int size = selection.size();
				String s = new String();
				
				if(size>1)
					s="Instances";
				else
					s="Instance";
				
				boolean isnotdefault = false;
				for (Iterator<TreeObject> iter = selection.iterator(); iter.hasNext(); ) {
					TreeObject xobject = iter.next();
					
		            if (!xobject.isXObject()) return;
		            if(!ESystemDefaultObjects.isExist(xobject.getType(), xobject.getDisplayName())){
		            	isnotdefault=true;
		            	break;
		            }//if there are items which are not default, isnotdefault is true
				}
				if(isnotdefault){
					if (! MessageDialog.openConfirm(
		            		this.view.getSite().getShell(),
		            		"Delete "+IConstants.TALEND+" Object Instance",
		            		"Are you sure you want to delete the "+ selection.size() + " " +s +"?"
		            )) return;
					
				}//if the isnotdefault is true,open this dialog
				
				
			}//end of if(selection...)
			
			for (Iterator<TreeObject> iter = selection.iterator(); iter.hasNext(); ) {
				TreeObject xobject = iter.next();
				
	            if (!xobject.isXObject()) return;
	            if(ESystemDefaultObjects.isExist(xobject.getType(), xobject.getDisplayName())){
	            	continue;
	            }
	            
	            //ask for confimation
	                        
	//          Access to server and get port
				XtentisPort port = Util.getPort(
						new URL(xobject.getEndpointAddress()),
						xobject.getUniverse(),
						xobject.getUsername(),
						xobject.getPassword()
				);
	              
	            switch(xobject.getType()) {
	            
		           	case TreeObject.DATA_MODEL:
		           		port.deleteDataModel(new WSDeleteDataModel((WSDataModelPK)xobject.getWsKey()));
		           		deleteSpecificationFromAttachedRole(port, xobject, "Data Model");
		           		break;
		          	case TreeObject.VIEW:
		           		port.deleteView(new WSDeleteView((WSViewPK)xobject.getWsKey()));
		           		deleteSpecificationFromAttachedRole(port, xobject, "View");
		           		break;           		
		          	case TreeObject.DATA_CLUSTER:
		           		port.deleteDataCluster(new WSDeleteDataCluster((WSDataClusterPK)xobject.getWsKey()));
		           		deleteSpecificationFromAttachedRole(port, xobject, "Data Cluster");
		           		break;      
		          	case TreeObject.STORED_PROCEDURE:
		           		port.deleteStoredProcedure(new WSDeleteStoredProcedure((WSStoredProcedurePK)xobject.getWsKey()));
		           		break;  
		          	case TreeObject.ROLE:
		           		port.deleteRole(new WSDeleteRole((WSRolePK)xobject.getWsKey()));
		           		break;  
		          	case TreeObject.ROUTING_RULE:
		           		port.deleteRoutingRule(new WSDeleteRoutingRule((WSRoutingRulePK)xobject.getWsKey()));
		           		break;  
		          	case TreeObject.TRANSFORMER:
		           		port.deleteTransformerV2(new WSDeleteTransformerV2((WSTransformerV2PK)xobject.getWsKey()));
		           		break;
		          	case TreeObject.MENU:
		           		port.deleteMenu(new WSDeleteMenu((WSMenuPK)xobject.getWsKey()));
		           		deleteSpecificationFromAttachedRole(port, xobject, "Menu");
		           		break;  	 
		          	case TreeObject.UNIVERSE:
		           		port.deleteUniverse(new WSDeleteUniverse((WSUniversePK)xobject.getWsKey()));
		           		break;  
		          	case TreeObject.SYNCHRONIZATIONPLAN:
		           		port.deleteSynchronizationPlan(new WSDeleteSynchronizationPlan((WSSynchronizationPlanPK)xobject.getWsKey()));
		           		break; 
		          	default:
		           		MessageDialog.openError(view.getSite().getShell(), "Error", "Unknown "+IConstants.TALEND+" Object Type: "+xobject.getType());
		           		return;
	            }//switch
	            
	       		xobject.getParent().removeChild(xobject);
	       		view.getViewer().refresh();
			}//for
       		//view.getSite().getWorkbenchWindow().get
                   
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(
					view.getSite().getShell(),
					"Error", 
					"An error occured trying to delete the "+IConstants.TALEND+" object: "+e.getLocalizedMessage()
			);
		}finally {
			//refresh view
			try {
				TreeObject selected = (TreeObject)((IStructuredSelection)view.getViewer().getSelection()).getFirstElement();
				if (selected == null)
				{
					selected = view.getTreeContentProvider().getInvisibleRoot().getChildren()[0];
				}
				(new ServerRefreshAction(this.view,selected.getServerRoot())).run();
			} catch (Exception e) {}
		}		
	}
	
	private void deleteSpecificationFromAttachedRole(XtentisPort port, TreeObject xobject, String objectType) throws RemoteException
	{
   		WSRolePK[] pks = port.getRolePKs(new WSGetRolePKs(".*")).getWsRolePK();
   		for (WSRolePK pk : pks) {
			WSGetRole getRole = new WSGetRole();
			getRole.setWsRolePK(new WSRolePK(pk.getPk()));
			WSRole role = port.getRole(getRole);
			for (WSRoleSpecification spec : role.getSpecification()) {
				if (spec.getObjectType().equals(objectType)) {
					WSRoleSpecificationInstance[] specInstance = spec
							.getInstance();
					List<WSRoleSpecificationInstance> newSpecInstanceLst = new ArrayList<WSRoleSpecificationInstance>();
					for (WSRoleSpecificationInstance specIns : specInstance) {
						if (!specIns.getInstanceName().equals(
								xobject.getDisplayName())) {
							newSpecInstanceLst.add(specIns);
						}
					}
					if (newSpecInstanceLst.size() < specInstance.length) {
						spec.setInstance(newSpecInstanceLst
								.toArray(new WSRoleSpecificationInstance[] {}));
						WSPutRole putRole = new WSPutRole();
						putRole.setWsRole(role);
						port.putRole(putRole);
						break;
					}
				}
			}
		}
	}
	
	public void runWithEvent(Event event) {
		super.runWithEvent(event);
	}
	


}