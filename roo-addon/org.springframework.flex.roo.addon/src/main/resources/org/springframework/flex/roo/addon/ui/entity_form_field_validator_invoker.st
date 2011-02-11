$if(it.validations)$
vResult = $it.metadata.fieldName.symbolName$Validator.validate();
if (vResult.type == ValidationResultEvent.INVALID){
	return false;
}$\n$
$endif$