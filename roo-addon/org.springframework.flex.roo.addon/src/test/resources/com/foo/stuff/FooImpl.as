package com.foo.stuff
{
	import com.foo.Alpha;
	import com.foo.Bar;
	import com.foo.Foo;
	
	[ClassLevelTag1]
	[ClassLevelTag2(foo="bar")]
	public class FooImpl extends Alpha implements Foo
	{
		[FieldLevelTag1]
		public var field1:String;
		
		private var field2:Bar;
		
		public function FooImpl(){
			var localField1 = "localField1";
			field2 = new Bar(localField);
		}
		
		[MethodLevelTag1]
		private function method1():void{
		
		}
		
		public function fooFactory():FooImpl{
		
		}
		
		public function calculateStuff(bar:String, baz:Bar):String {
		
		}
	}
}