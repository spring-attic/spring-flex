package
{
	import flash.events.Event;
	
	import mx.collections.ICollectionView;
	import mx.controls.ComboBox;
	import mx.events.CollectionEvent;
	
	public class ComboBox extends mx.controls.ComboBox
	{
		private var _value:Object;
		
		public var valueField:String = "data";

		public function set value(value:Object):void 
		{
			_value = value;
   			selectIndex();	
		}

		override public function get value():Object
		{
			return selectedItem[valueField];
		}
		
		override public function set dataProvider(dataProvider:Object):void 
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
	  
		override protected function createChildren():void
		{
			super.createChildren();
		}
	   	
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
		{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
		}

	}
}