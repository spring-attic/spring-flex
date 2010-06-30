<mx:FormItem id="$it.metadata.fieldName.symbolName$Field" label="$it.metadata.fieldName.readableSymbolName$">
$if(it.date)$
	<mx:DateField id="$it.metadata.fieldName.symbolName$Input" selectedDate="{$flexScaffoldMetadata.entityReference$.$it.metadata.fieldName.symbolName$}" />
$elseif(it.boolean)$
	<s:CheckBox id="$it.metadata.fieldName.symbolName$Input" selected="{$flexScaffoldMetadata.entityReference$.$it.metadata.fieldName.symbolName$}" />
$elseif(it.singleEndedRelationship)$
	<s:DropDownList id="$it.metadata.fieldName.symbolName$Input" labelField="$labelFields.(it.metadata.fieldName.symbolName)$" />	
$else$
	<s:TextInput id="$it.metadata.fieldName.symbolName$Input" text="{$flexScaffoldMetadata.entityReference$.$it.metadata.fieldName.symbolName$}" />
$endif$
</mx:FormItem>