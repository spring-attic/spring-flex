$if(it.validations)$
$if(it.number)$
<mx:NumberValidator id="$it.metadata.fieldName.symbolName$Validator" source="{$it.metadata.fieldName.symbolName$Input}" property="text"
$elseif(it.date)$
<mx:DateValidator id="$it.metadata.fieldName.symbolName$Validator" source="{$it.metadata.fieldName.symbolName$Input}" property="selectedDate"
$else$
<mx:StringValidator id="$it.metadata.fieldName.symbolName$Validator" source="{$it.metadata.fieldName.symbolName$Input}" property="text" 
$endif$
$it.validations.keys,it.validations.values:{k,v| $\ $$k$="$v$"}$ />
$endif$