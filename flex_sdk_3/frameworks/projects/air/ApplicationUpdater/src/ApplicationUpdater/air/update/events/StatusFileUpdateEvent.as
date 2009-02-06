/*
ADOBE SYSTEMS INCORPORATED
Copyright 2008 Adobe Systems Incorporated. All Rights Reserved.
 
NOTICE:   Adobe permits you to modify and distribute this file only in accordance with
the terms of Adobe AIR SDK license agreement.  You may have received this file from a
source other than Adobe.  Nonetheless, you may modify or distribute this file only in 
accordance with such agreement.
*/

package air.update.events
{
	import flash.events.Event;
	import flash.filesystem.File;
	
	public class StatusFileUpdateEvent extends UpdateEvent
	{
		/**
		 * The <code>StatusUpdateEvent.UPDATE_STATUS</code> constant defines the value of the  
		 * <code>type</code> property of the event object for a <code>updateStatus</code> event.
		 */				
		public static const FILE_UPDATE_STATUS:String = "fileUpdateStatus";
		
		public function StatusFileUpdateEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false, available:Boolean = false, version:String = "", path:String = "")
		{
			super(type, bubbles, cancelable);
			this.available = available;
			this.version = version;
			this.path = path;
		} 
		
		/**
	 	 * @inheritDoc
	 	 */
		override public function clone():Event
		{
			return new StatusFileUpdateEvent(type, bubbles, cancelable, available, version, path);
		}
		
		/**
	 	 * @inheritDoc
	 	 */
		override public function toString():String
		{
			return "[StatusFileUpdateEvent (type=" + type + " available=" + available + " version=" + version + " path=" + path + ")]";	
		}
		
		/**
		 * Indicates if an update is available. 
		 */
		public var available:Boolean = false;
		
		/**
		 * Indicates the version of the new update
		 */
		public var version:String = "";
		
		/**
		 * The file used to in installFromAIRFile
		 */
		public var path:String = null;
	}
}