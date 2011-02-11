package $entityEventType.package.fullyQualifiedPackageName$
{
    import flash.events.Event;

    import $entityType.fullyQualifiedTypeName$;

    public class $entityEventType.simpleTypeName$ extends Event
    {
        public static const CREATE:String = "$flexScaffoldMetadata.entityReference$Create";
        public static const UPDATE:String = "$flexScaffoldMetadata.entityReference$Update";
        public static const DELETE:String = "$flexScaffoldMetadata.entityReference$Delete";

        public var $flexScaffoldMetadata.entityReference$:$entityType.simpleTypeName$;
        
        public function $entityEventType.simpleTypeName$(type:String, $flexScaffoldMetadata.entityReference$:$entityType.simpleTypeName$, bubbles:Boolean = true, cancelable:Boolean = false)
        {
            this.$flexScaffoldMetadata.entityReference$ = $flexScaffoldMetadata.entityReference$;
            super(type, bubbles, cancelable);
        }
    }
}