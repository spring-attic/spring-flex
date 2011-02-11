protected function $it.serviceReference$_faultHandler(event:FaultEvent):void
{
    Alert.show(event.fault.faultString, "Remote Operation Failed");
}

protected function $it.serviceReference$_listResultHandler(event:ResultEvent):void
{
    $it.fieldsOfType:org/springframework/flex/roo/addon/ui/entity_form_service_list_handler_body()$
}

$it.fieldsOfType:org/springframework/flex/roo/addon/ui/entity_form_field_selector()$