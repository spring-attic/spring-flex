private function select$it.fieldName.symbolNameCapitalisedFirstLetter$():void {
	if ($flexScaffoldMetadata.entityReference$.$it.fieldName.symbolName$ == null) {
		return;
	}
	for each(var $currentType.typeReference$Item:$currentType.typeName$ in $it.fieldName.symbolName$Input.dataProvider) {
		if ($flexScaffoldMetadata.entityReference$.$it.fieldName.symbolName$.id == $currentType.typeReference$Item.id) {
			$it.fieldName.symbolName$Input.selectedItem = $currentType.typeReference$Item;
			return;
		}
	}
}
$\n$