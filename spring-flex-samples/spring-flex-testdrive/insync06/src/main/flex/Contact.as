package
{
	[Bindable]
	[RemoteClass(alias="org.springframework.flex.samples.contact.Contact")]
	public class Contact
	{
		public var id:int;
		public var firstName:String;
		public var lastName:String;
		public var email:String;
		public var phone:String;
		public var address:String;
		public var city:String;
		public var state:String;
		public var zip:String;
		public var dob:Date;
	}
}