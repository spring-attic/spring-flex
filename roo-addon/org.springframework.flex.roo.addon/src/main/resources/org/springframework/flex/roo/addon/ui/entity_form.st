<?xml version="1.0" encoding="utf-8"?>
<s:TitleWindow xmlns:fx="http://ns.adobe.com/mxml/2009" 
        xmlns:s="library://ns.adobe.com/flex/spark" 
        xmlns:mx="library://ns.adobe.com/flex/mx" close="handleCloseEvent();" creationComplete="$flexScaffoldMetadata.entityReference$Form_creationCompleteHandler(event)" title="$entityType.simpleTypeName$ Form" width="400" height="300">
    <fx:Script>
        <![CDATA[
            import mx.collections.ArrayCollection;
            import mx.controls.Alert;
            import mx.events.FlexEvent;
            import mx.managers.PopUpManager;
            import mx.messaging.ChannelSet;
            import mx.rpc.events.FaultEvent;
            import mx.rpc.events.ResultEvent;
            
            import mx.validators.Validator;
            
            import $entityType.fullyQualifiedTypeName$;
            
            $relatedTypes:{$if(it.importRequired)$import $it.type.fullyQualifiedTypeName$;$\n$$endif$}$
            
            [Bindable]
            public var remotingChannels:ChannelSet;
            
            [Bindable]
            public var $flexScaffoldMetadata.entityReference$:$entityType.simpleTypeName$;
            
            private function handleCloseEvent():void {
                PopUpManager.removePopUp(this);
            }            
            
            private function processSave():void {
                if (!validateAndCommit()){
                    return;
                }
                this.dispatchEvent(new $entityType.simpleTypeName$Event(((!$flexScaffoldMetadata.entityReference$.id || $flexScaffoldMetadata.entityReference$.id <= 0) ? $entityType.simpleTypeName$Event.CREATE : $entityType.simpleTypeName$Event.UPDATE), $flexScaffoldMetadata.entityReference$));
                PopUpManager.removePopUp(this);
            }
            
            private function validateAndCommit():Boolean{
                var validatorErrors:Array = Validator.validateAll(validators.source);
                if (validatorErrors.length > 0){
                    return false;
                }
                $fields:org/springframework/flex/roo/addon/ui/entity_form_field_value_commit()$
                return true;
            }
            
            protected function $flexScaffoldMetadata.entityReference$Form_creationCompleteHandler(event:FlexEvent):void
            {
                $relatedTypes:{$it.serviceReference$.list();$\n$}$
            }
            
            $relatedTypes:org/springframework/flex/roo/addon/ui/entity_form_service_handlers(currentType=it)$
        ]]>
    </fx:Script>
    
    <fx:Declarations>
    	$relatedTypes:org/springframework/flex/roo/addon/ui/entity_form_service_declaration()$
        <s:ArrayList id="validators">
            $fields:org/springframework/flex/roo/addon/ui/entity_form_field_validator()$
        </s:ArrayList>
    </fx:Declarations>
    
    <s:layout>
        <s:VerticalLayout/>
    </s:layout>
    
    <mx:Form id="$flexScaffoldMetadata.entityReference$Form">
        $fields:org/springframework/flex/roo/addon/ui/entity_form_field()$
    </mx:Form>
    
    <s:Group>
        <s:layout>
            <s:HorizontalLayout/>
        </s:layout>
        <s:Button label="Save" click="processSave();"/>
        <s:Button label="Cancel" click="handleCloseEvent();"/>
    </s:Group>
    
    
</s:TitleWindow>