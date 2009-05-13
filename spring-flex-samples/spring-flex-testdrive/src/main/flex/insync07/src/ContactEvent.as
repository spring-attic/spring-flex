package
{
	import flash.events.Event;

	public class ContactEvent extends Event
	{
		public static const CREATED:String = "contactCreated";
		public static const UPDATED:String = "contactUpdated";
		public static const DELETED:String = "contactDeleted";
	
		public var contact:Contact;
		
		public function ContactEvent(type:String, contact:Contact, bubbles:Boolean = true, cancelable:Boolean = false)
   		{
   			this.contact = contact;
			super(type, bubbles, cancelable);
		}
	}
}