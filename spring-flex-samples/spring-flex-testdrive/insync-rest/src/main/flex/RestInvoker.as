package
{
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.net.ObjectEncoding;
	import flash.net.URLRequest;
	import flash.net.URLRequestHeader;
	import flash.net.URLRequestMethod;
	import flash.net.URLStream;
	import flash.utils.ByteArray;
	
	import mx.utils.URLUtil;

	public class RestInvoker
	{
		private static var AMF_TYPE:String = "application/x-amf";
		
		private static var BASE_URL:String = "http://localhost:8080/testdrive";
		
		public function invoke(path:String, resultHandler:Function, faultHandler:Function, params:Object = null, method:String = URLRequestMethod.GET, payload:Object = null):void {
			if (method == "PUT" || method == "DELETE") {
				if (params == null) {
					params = new Object();
				}
				params._method = method;
				method = URLRequestMethod.POST;
			}
			var paramStr:String = params != null ? "?"+URLUtil.objectToString(params, "&") : "";
			//Using the file extension ".amf" to work around Flash's inability to set the Accept header
			var url:String = BASE_URL + path + ".amf" + paramStr;
			var request:URLRequest = new URLRequest(url);
			request.method = method;
			request.contentType = AMF_TYPE;
			request.requestHeaders = new Array(new URLRequestHeader("Accept",AMF_TYPE));
			var stream:URLStream = new URLStream();
			stream.objectEncoding = ObjectEncoding.AMF3;
			stream.addEventListener(Event.COMPLETE, resultHandler);
			stream.addEventListener(IOErrorEvent.IO_ERROR, faultHandler);
			
			if (method == URLRequestMethod.POST) {
				var ba:ByteArray = new ByteArray();
				//Have to write *something* to the body (even null, as would be the typical case for DELETE), 
				//otherwise the POST will turn into a GET
				ba.writeObject(payload);
				request.data = ba;
			}
			
			stream.load(request);
		}
	}
}