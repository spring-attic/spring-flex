package
{
	[Bindable]
	[RemoteClass(alias="org.springframework.flex.samples.company.Company")]
	public class Company
	{
		public var id:int;
		public var name:String;
		public var address:String;
		public var city:String;
		public var state:String;
		public var zip:String;
		public var phone:String;
		public var industry:Industry;
	}
}