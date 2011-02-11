package
{
	import flash.events.Event;

	public class CompanyEvent extends Event
	{
		public static const CREATED:String = "companyCreated";
		public static const UPDATED:String = "companyUpdated";
		public static const DELETED:String = "companyDeleted";
	
		public var company:Company;
		
		public function CompanyEvent(type:String, company:Company, bubbles:Boolean = true, cancelable:Boolean = false)
   		{
   			this.company = company;
			super(type, bubbles, cancelable);
		}
	}
}