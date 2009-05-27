amalto.namespace("amalto.SynchronizationAction");

//loadResource("/SynchronizationAction/secure/js/Cookies.js", "" );

//contectid.applicaionid
amalto.SynchronizationAction.SynchronizationAction = function() {
	var SELECT_SYNCHRONIZATION='Select a Synchronization Name';
	
   
	function getSyncInfo(){
		if ($('serverURL').value=='') {
			//alert ('Server URL');
			$('serverURL').focus();
			return;
		}
		if ($('username').value=='') {
			//alert ('UserName');
			$('username').focus();
			return;
		}	
		if ($('password').value=='') {
			//alert ('password');
			$('password').focus();
			return;
		}
		
		var syncInfo = {
			serverURL:$('serverURL').value.trim(),
			username:$('username').value.trim(),
			password:$('password').value.trim(),
			syncName:$('syncName').value.trim()
		};
		return syncInfo;
	}

	function initSyncNames(){
		var syncinfo=getSyncInfo();
		if(syncinfo){
			
			SynchronizationActionInterface.getSyncNames(syncinfo,function(syncs){
				if(syncs){
					var syncnames=syncs;
					var tmp=[SELECT_SYNCHRONIZATION];
					DWRUtil.removeAllOptions("syncName");
					DWRUtil.addOptions("syncName",tmp);
					if(!syncnames)return;
					DWRUtil.addOptions("syncName",syncnames);
				}
			});		
		}
	};

	function updateStatus(syncStatus){
		if(syncStatus){
			synccode=syncStatus;
			Ext.getCmp('sync_status').getEl().update('[' + syncStatus.value + '] ' + syncStatus.message);
			if('RUNNING' == syncStatus.value || 'SCHEDULED' == syncStatus.value){
	    		Ext.getCmp('startFullButton').disable();
	    		Ext.getCmp('startDifferentButton').disable();
	    		Ext.getCmp('stopButton').enable();
	    		Ext.getCmp('resetButton').disable();
			}
			else if ("STOPPING" == syncStatus.value) {
	    		Ext.getCmp('startFullButton').disable();
	    		Ext.getCmp('startDifferentButton').disable();
	    		Ext.getCmp('stopButton').disable();
	    		Ext.getCmp('resetButton').enable();
	    		
	    	} else {
	    		Ext.getCmp('startFullButton').enable();
	    		Ext.getCmp('startDifferentButton').enable();
	    		Ext.getCmp('stopButton').disable();
	    		Ext.getCmp('resetButton').disable();
	    	}
		}
	};

	var synccode;
	
	function refreshStatus(syncInfo){
		var timer=setInterval(function(){
			SynchronizationActionInterface.getStatus(syncInfo,function(syncStatus){
				if(syncStatus==null){
					clearInterval(timer);
					return;
				}
				updateStatus(syncStatus);
			});
			if(synccode){
				if(!('RUNNING' == synccode.value || 'SCHEDULED' == synccode.value)){
					clearInterval(timer);
					updateStatus(synccode);
				}
			}			
		},1000);		
	};
	
	var store;
	function saveURLs(){
		
		if(store.indexOfId($('serverURL').value) ==-1){
			store.add([new Ext.data.Record({'id':$('serverURL').value,'name':$('serverURL').value})]);			
		}
		var urls="";
		var len=store.data.items.length;
		for(var i=0; i<len; i++){
			if(i<len-1){
				urls=urls+store.data.items[i].data.id+";";
			}else{
				urls=urls+store.data.items[i].data.id;
			}
		}		
		SynchronizationActionInterface.saveURLs(urls,function(){});
	};	
	
	function show() {
		Ext.QuickTips.init();
   		var recordType = Ext.data.Record.create([	  
		  	{name:"id"},{name:"name"}  
		  ]);

	    store = new Ext.data.Store({
	    proxy: new Ext.data.DWRProxy(SynchronizationActionInterface.getSavedURLs, false),
	    reader: new Ext.data.ListRangeReader( 
				{id:'id', totalProperty:'totalSize'}, recordType),
	    remoteSort: false
	  });
	  
  	    store.load({params:{start:0, limit:22}, arg:[]});
		var topPanel = new Ext.form.FormPanel(
		{
			region : 'center',
			border : false,
			bodyStyle : "padding: 8px; background-color: transparent;",
			labelAlign : "left",
			labelWidth : 150,
			autoScroll : true,
			defaultType : "textfield",
			buttonAlign : 'left',
			items : [ 
			          new Ext.form.FieldSet( {
				title : 'Remote system information',
				autoHeight : true,
				defaultType : 'textfield',
				items : [ 
						{
							fieldLabel : 'Server URL',	
						    store: store,
						    displayField:'name',
						    typeAhead: true,
						    id : 'serverURL',
						    mode: 'local',
						    forceSelection: false,
						    triggerAction: 'all',						    
						    selectOnFocus:true,
							xtype:'combo',
							width : 400
						},
						{
					fieldLabel : 'UserName',
					id : 'username',
					selectOnFocus:true,
					allowBlank:false,					
					width : 400
				}, {
					fieldLabel : 'Password',
					id : 'password',
					selectOnFocus:true,
					allowBlank:false,
					inputType:'password',
					width : 400,
			        listeners:{
			        	'blur': function(){										
							initSyncNames();
			    		}
					}
				}]
			}),
			new Ext.Panel({
					width : 400,
					border:false,
					html:'<div> Synchronization Name:    '+'<select id="syncName" ></select></div>' 
				}) 
			
			, {
				id : 'sync_status',
				xtype : 'box',
				autoEl : {
					cn : ''
				}
			} ],
			buttons : [ 							
					{	
						xtype:'button',
						id:'startFullButton',
						text : '<b>Start Full</b>',
						handler : function() {
							var syncInfo=getSyncInfo();
							if(syncInfo){
								saveURLs();
								SynchronizationActionInterface.startFull(refreshStatus(syncInfo),syncInfo);
							}
						},
						tooltip : 'Start synchronization'
					},
					{	xtype:'button',
						id:'startDifferentButton',
						text : '<b>Start Different</b>',
						handler : function() {
							var syncInfo=getSyncInfo();
							if(syncInfo){
								saveURLs();
								SynchronizationActionInterface.startDifferent(refreshStatus(syncInfo),syncInfo);
							}
						},
						tooltip : 'Start Different synchronization'
					},
					{	xtype:'button',
						id:'stopButton',
						text : '<b>Stop</b>',
						disabled:true,
						handler : function() {
							var syncInfo=getSyncInfo();
							if(syncInfo)
							SynchronizationActionInterface.stop(refreshStatus(syncInfo),syncInfo);
						},
						tooltip : 'Stop synchronization'
					},
					{	xtype:'button',
						id:'resetButton',
						text : '<b>Reset</b>',
						disabled:true,
						handler : function() {
							var syncInfo=getSyncInfo();
							if(syncInfo)
							SynchronizationActionInterface.reset(refreshStatus(syncInfo),syncInfo);
						},
						tooltip : 'Reset synchronization'
					}
					],
			listeners:{
				'render':function(){
					//if(Cookies.get('serverURL'))Ext.getCmp('serverURL').setValue(Cookies.get('serverURL'));
					//if(Cookies.get('username'))Ext.getCmp('username').setValue(Cookies.get('username'));
					
				}
			}		
		});
		
		 mainPanel = new Ext.Panel( {
			bodyStyle : 'padding:5px 5px 1',
			id:'synchronizationaction',
			border : true,
			closable:true,
			title : 'Synchronization Action',
			collapsible : true,
			items : [ topPanel ]
		});
				
	};
	var mainPanel;

   function showSync(){
    	var tabPanel = amalto.core.getTabPanel();
    	if(tabPanel.getItem('synchronizationaction') == undefined){
			show();			
    	}
    	 //refreshStatus();
		  tabPanel.add(mainPanel);
		  mainPanel.show();
		  //syncPanel.doLayout();
		  amalto.core.doLayout();   	
    };
    
    return {
    	//public 
        init : function(){
    		showSync();		  
        }
    };
}();


//Ext.onReady(SynchronizationAction.init, SynchronizationAction, true);