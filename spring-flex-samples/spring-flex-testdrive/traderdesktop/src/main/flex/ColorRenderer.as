package {

    import mx.controls.Label;
    import mx.controls.dataGridClasses.*;

	public class ColorRenderer extends Label {

		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
	 	{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			if (data && listData && data[DataGridListData(listData).dataField] < 0)
			{
	 			setStyle("color", 0xFF0000);
	 	    }
	 	    else
	 	    {
	 			setStyle("color", 0x009900);
	 	    }
		}
  	}

}