package
{
	import flash.events.Event;
	
	import mx.collections.IList;
	import mx.events.CollectionEvent;
	
	import spark.components.ComboBox;
	
	public class BindableComboBox extends ComboBox
	{
		private var _value:Object;
		
		public var valueField:String = "data";
		
		public function set value(value:Object):void 
		{
			_value = value;
			selectIndex();	
		}
		
		public function get value():Object
		{
			return selectedItem[valueField];
		}
		
		override public function set dataProvider(dataProvider:IList):void 
		{
			super.dataProvider = dataProvider;
			dataProvider.addEventListener(CollectionEvent.COLLECTION_CHANGE, 
				function(event:Event):void
				{
					selectIndex();
				});
			selectIndex();
		}
		
		private function selectIndex():void
		{
			if (!_value || !dataProvider)
			{
				return;
			}
			for (var i:int = 0; i < dataProvider.length; i++) 
			{
				if (_value == dataProvider[i][valueField])
				{
					selectedIndex = i;
					return;
				}
			}
		}
		
	}
}