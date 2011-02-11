<?xml version="1.0" encoding="utf-8"?>
<s:Panel xmlns:fx="http://ns.adobe.com/mxml/2009" 
		 xmlns:s="library://ns.adobe.com/flex/spark" 
		 xmlns:mx="library://ns.adobe.com/flex/mx" 
		 width="100%" height="100%"
		 title="$entityType.simpleTypeName$ List" creationComplete="$flexScaffoldMetadata.entityReference$View_creationCompleteHandler(event)">
	
	<fx:Script>
		<![CDATA[
			import mx.collections.ArrayCollection;
			import mx.controls.Alert;
			import mx.controls.ProgressBar;
			import mx.events.FlexEvent;
			import mx.managers.PopUpManager;
			import mx.messaging.ChannelSet;
			import mx.rpc.events.FaultEvent;
			import mx.rpc.events.ResultEvent;
			
			import $entityType.fullyQualifiedTypeName$;
			
			import spark.components.TitleWindow;
			
			[Bindable]
			public var remotingChannels:ChannelSet;
			
			private var progressBar:ProgressBar;

			protected function $flexScaffoldMetadata.entityReference$View_creationCompleteHandler(event:FlexEvent):void
			{
				$flexScaffoldMetadata.entityReference$Service.list();
			}
			
			protected function $flexScaffoldMetadata.entityReference$View_$flexScaffoldMetadata.entityReference$CreateEventHandler(event:$entityType.simpleTypeName$Event):void
			{
				$flexScaffoldMetadata.entityReference$Service.create(event.$flexScaffoldMetadata.entityReference$);
				showProgressBar();
			}
			
			protected function $flexScaffoldMetadata.entityReference$View_$flexScaffoldMetadata.entityReference$UpdateEventHandler(event:$entityType.simpleTypeName$Event):void
			{
				$flexScaffoldMetadata.entityReference$Service.update(event.$flexScaffoldMetadata.entityReference$);
				showProgressBar();
			}
			
			private function showForm($flexScaffoldMetadata.entityReference$:$entityType.simpleTypeName$ = null):void 
			{
				var form:$entityType.simpleTypeName$Form = PopUpManager.createPopUp(this, $entityType.simpleTypeName$Form, true) as $entityType.simpleTypeName$Form;
				form.remotingChannels = remotingChannels;
				form.$flexScaffoldMetadata.entityReference$ = $flexScaffoldMetadata.entityReference$ != null ? $flexScaffoldMetadata.entityReference$ : new $entityType.simpleTypeName$();
				form.addEventListener($entityType.simpleTypeName$Event.CREATE, $flexScaffoldMetadata.entityReference$View_$flexScaffoldMetadata.entityReference$CreateEventHandler);
				form.addEventListener($entityType.simpleTypeName$Event.UPDATE, $flexScaffoldMetadata.entityReference$View_$flexScaffoldMetadata.entityReference$UpdateEventHandler);
				PopUpManager.centerPopUp(form);
			}
			
			private function showProgressBar():void {
				progressBar = PopUpManager.createPopUp(this, ProgressBar, true) as ProgressBar;
				progressBar.indeterminate = true;
				progressBar.mode="manual";
				PopUpManager.centerPopUp(progressBar);
			}
			
			private function closeProgressBar():void {
				PopUpManager.removePopUp(progressBar);
			}

			protected function $flexScaffoldMetadata.entityReference$Service_createResultHandler(event:ResultEvent):void
			{
				var created$entityType.simpleTypeName$:$entityType.simpleTypeName$ = event.result as $entityType.simpleTypeName$;
				var people:ArrayCollection = $flexScaffoldMetadata.entityReference$Grid.dataProvider as ArrayCollection;
				people.addItem(created$entityType.simpleTypeName$);
				closeProgressBar();
			}
			
			protected function $flexScaffoldMetadata.entityReference$Service_updateResultHandler(event:ResultEvent):void
			{
				var updated$entityType.simpleTypeName$:$entityType.simpleTypeName$ = event.result as $entityType.simpleTypeName$;
				var people:ArrayCollection = $flexScaffoldMetadata.entityReference$Grid.dataProvider as ArrayCollection;
				for (var i:int=0; i<people.length; i++) {
					var $flexScaffoldMetadata.entityReference$:$entityType.simpleTypeName$ = people.getItemAt(i) as $entityType.simpleTypeName$;
					if ($flexScaffoldMetadata.entityReference$.id == updated$entityType.simpleTypeName$.id) {
						people.removeItemAt(i);
						people.addItemAt(updated$entityType.simpleTypeName$, i);
					}
				}
				closeProgressBar();
			}
			
			protected function $flexScaffoldMetadata.entityReference$Service_removeResultHandler(event:ResultEvent):void
			{
				var people:ArrayCollection = $flexScaffoldMetadata.entityReference$Grid.dataProvider as ArrayCollection;
				people.removeItemAt($flexScaffoldMetadata.entityReference$Grid.selectedIndex);
				closeProgressBar();
			}

			protected function $flexScaffoldMetadata.entityReference$Service_faultHandler(event:FaultEvent):void
			{
				closeProgressBar();
				Alert.show(event.fault.faultString, "Remote Operation Failed");
			}

			protected function edit$entityType.simpleTypeName$Btn_clickHandler(event:MouseEvent):void
			{
				if ($flexScaffoldMetadata.entityReference$Grid.selectedIndex > -1) {
					var selected$entityType.simpleTypeName$:$entityType.simpleTypeName$ = $flexScaffoldMetadata.entityReference$Grid.selectedItem as $entityType.simpleTypeName$;
					showForm(selected$entityType.simpleTypeName$);
				}
			}

			protected function delete$entityType.simpleTypeName$Btn_clickHandler(event:MouseEvent):void
			{
				if ($flexScaffoldMetadata.entityReference$Grid.selectedIndex > -1) {
					var selected$entityType.simpleTypeName$:$entityType.simpleTypeName$ = $flexScaffoldMetadata.entityReference$Grid.selectedItem as $entityType.simpleTypeName$;
					showProgressBar();
					$flexScaffoldMetadata.entityReference$Service.remove(selected$entityType.simpleTypeName$.id);
				}
			}

		]]>
	</fx:Script>
	
	<fx:Declarations>
		<s:RemoteObject id="$flexScaffoldMetadata.entityReference$Service" destination="$flexScaffoldMetadata.entityReference$Service" channelSet="{remotingChannels}" fault="$flexScaffoldMetadata.entityReference$Service_faultHandler(event)">
			<s:method name="create" result="$flexScaffoldMetadata.entityReference$Service_createResultHandler(event)"/>
			<s:method name="update" result="$flexScaffoldMetadata.entityReference$Service_updateResultHandler(event)"/>
			<s:method name="remove" result="$flexScaffoldMetadata.entityReference$Service_removeResultHandler(event)"/>
		</s:RemoteObject>
		
	</fx:Declarations>
	
	<s:layout>
		<s:VerticalLayout/>
	</s:layout>
	
	<s:Button id="new$entityType.simpleTypeName$Btn" label="New $entityType.simpleTypeName$" click="showForm();"/>
	
	<mx:DataGrid id="$flexScaffoldMetadata.entityReference$Grid" dataProvider="{$flexScaffoldMetadata.entityReference$Service.list.lastResult}" height="80%" width="80%">
		<mx:columns>
			$fields:{<mx:DataGridColumn id="$it.fieldName.symbolName$Column" dataField="$it.fieldName.symbolName$" headerText="$it.fieldName.readableSymbolName$" />}$
		</mx:columns>
	</mx:DataGrid>
	
	<s:Group>
		<s:layout>
			<s:HorizontalLayout/>
		</s:layout>
		<s:Button id="edit$entityType.simpleTypeName$Btn" label="Edit" click="edit$entityType.simpleTypeName$Btn_clickHandler(event)"/>
		<s:Button id="delete$entityType.simpleTypeName$Btn" label="Delete" click="delete$entityType.simpleTypeName$Btn_clickHandler(event)"/>
	</s:Group>
</s:Panel>
