package
{
	
	[RemoteClass(alias="org.springframework.flex.integration.domain.Person"]
	public class Person
	{
		public var id : int;
		public var spouse : Person;
		public var name : String;
		public var children : ArrayCollection;
		
		public function Person()
		{
		}
	}
}