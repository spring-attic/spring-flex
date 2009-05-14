package
{
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.ui.Keyboard;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Image;
	import mx.controls.List;
	import mx.controls.TextInput;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.core.mx_internal;
	import mx.events.FlexMouseEvent;
	import mx.events.ListEvent;
	import mx.managers.IFocusManagerComponent;
	import mx.managers.PopUpManager;
	import mx.styles.ISimpleStyleClient;
	
	use namespace mx_internal;
	
	[Event(name="textChange", type="flash.events.Event")]
	[Event(name="select", type="flash.events.Event")]
	
	public class SearchBox extends UIComponent
	{
    	mx_internal var border:IFlexDisplayObject;		
		
		private var textInput:TextInput;

		[Embed(source="assets/search.png")]
		private var bSkin:Class;

		private var list:List;
		
		private var isListVisible:Boolean = false;
		
		private var _dataProvider:ArrayCollection;
		
		public var text:String;
		
		private var _labelFunction:Function;
		
		public function SearchBox()
		{
		}
		
		public function set dataProvider(dp:ArrayCollection):void
		{
			_dataProvider = dp;
			list.dataProvider = dp;
			if (dp != null && dp.length > 0)
			{
				 if (!isListVisible) popup();
				 list.selectedIndex = 0;
			}
			else
			{
				 if (isListVisible) removePopup();
			}
		}
		
		public function get dataProvider():ArrayCollection
		{
			return _dataProvider;
		}		
		
		public function set labelFunction(lf:Function):void
		{
			_labelFunction = lf;
			if (list)
			{
				list.labelFunction = lf;
			}
		}
		
		public function get labelFunction():Function
		{
			return _labelFunction;
		}		
		
		public function get selectedItem():Object
		{
			return list.selectedItem;
		}
		
		override protected function createChildren():void
		{
			super.createChildren();

			if (!border)
	        {
	            //var borderClass:Class = getStyle("borderSkin");
	            var borderClass:Class = bSkin;
	
	            if (borderClass)
	            {
	                border = new borderClass();
	
	                if (border is IFocusManagerComponent)
	                    IFocusManagerComponent(border).focusEnabled = false;
	
	                if (border is ISimpleStyleClient)
	                    ISimpleStyleClient(border).styleName = this;
	
	                addChild(DisplayObject(border));
	            }
	        }

            textInput = new TextInput();
            setStyle("backgroundColor", 0xFFFFFF);
            textInput.setStyle("focusThickness", 0);
            textInput.setStyle("borderSkin", null);
            setStyle("backgroundImage", "assets/search.png");
            
			textInput.addEventListener(Event.CHANGE, textInput_changeHandler);
			textInput.addEventListener(KeyboardEvent.KEY_DOWN, textInput_keyDownHandler);

   	    	textInput.move(0,0);
            addChild(textInput);
   	    	textInput.move(20,0);
			
			list = new List();
			list.height = 300;
			list.setStyle("dropShadowEnabled", true);
			if (_labelFunction != null)
			{
				list.labelFunction = _labelFunction;
			}
			list.doubleClickEnabled = true;
			list.addEventListener(ListEvent.ITEM_DOUBLE_CLICK, list_doubleClickHandler);
		}
	   	
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
		{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			border.setActualSize(unscaledWidth, unscaledHeight);
			textInput.width = unscaledWidth;
			textInput.height = unscaledHeight;
		}

		private function textInput_keyDownHandler(event:KeyboardEvent):void
		{
			switch (event.keyCode) 
			{
				case Keyboard.DOWN:
					if (!isListVisible)
					{
						popup();
					}
					else 
					{
						list.selectedIndex++;
						list.scrollToIndex(list.selectedIndex);
					}
    				break;
				case Keyboard.UP:
					if (isListVisible && list.selectedIndex > 0)
					{
						list.selectedIndex--;
						list.scrollToIndex(list.selectedIndex);
					}
					textInput.setSelection(textInput.text.length, textInput.text.length);
    				break;
				case Keyboard.ENTER:
			    	dispatchEvent(new Event("select"));	
			    	removePopup();
    				break;
				case Keyboard.ESCAPE:
					if (isListVisible) removePopup();
    				break;
			}
		}

	    private function textInput_changeHandler(event:Event):void
	    {
	    	text = textInput.text;
	    	dispatchEvent(new Event("textChange"));	
	    }

	    private function list_mouseDownOutsideHandler(event:MouseEvent):void
	    {
    		removePopup();
	    }

	    private function list_doubleClickHandler(event:ListEvent):void
	    {
	    	dispatchEvent(new Event("select"));	
	    	removePopup();
	    }

		private function popup():void
		{
			PopUpManager.addPopUp(list, this);
			//list.width = unscaledWidth;
			list.width = 224;
			
	        var point:Point = new Point(0, unscaledHeight);
    	    point = localToGlobal(point);
            point = parent.globalToLocal(point);
        	
        	list.move(point.x - 30, point.y + 24);
        	
            list.addEventListener(FlexMouseEvent.MOUSE_DOWN_OUTSIDE, list_mouseDownOutsideHandler);
        	
        	isListVisible = true;
		}

		private function removePopup():void
		{
			PopUpManager.removePopUp(list);
            list.removeEventListener(FlexMouseEvent.MOUSE_DOWN_OUTSIDE, list_mouseDownOutsideHandler);
			isListVisible = false;	
		}

	}
}