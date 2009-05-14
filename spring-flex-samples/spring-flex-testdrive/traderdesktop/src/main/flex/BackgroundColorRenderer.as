package {

    import mx.controls.Label;
    import flash.display.Graphics;

	public class BackgroundColorRenderer extends Label {
		
		public static var symbol:String;

		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
	 	{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
	 		
	 		var g:Graphics = graphics;
	 		
			g.clear();

			if (data && data.date && data.symbol == symbol)
			{
				if( data.change && data.change >= 0 ) 
				{
					g.beginFill(0x009900, 0.5);
					g.drawRect(0, 0, unscaledWidth, unscaledHeight);
		 			g.endFill();
				} 
				else 
				{
					g.beginFill(0xFF0000, 0.5);
					g.drawRect(0, 0, unscaledWidth, unscaledHeight);
		 			g.endFill();
				}
			}

		}
  	}

}