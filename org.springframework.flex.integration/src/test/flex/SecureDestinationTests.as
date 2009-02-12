package {
  
    import flash.events.Event;
    import flash.events.NetStatusEvent;
    import flash.events.EventDispatcher;
    import flash.events.HTTPStatusEvent;
  	import mx.messaging.ChannelSet;
  	import mx.messaging.channels.AMFChannel;
  	import mx.messaging.Channel;
  	import mx.rpc.events.ResultEvent;
    import mx.rpc.events.FaultEvent;  	
  	import mx.rpc.remoting.RemoteObject;
  	import flexunit.framework.Assert;
  	import flexunit.framework.TestCase;
  	import mx.controls.Alert;
  	
  	import flash.net.URLLoader;
  	import flash.net.URLRequest;

  	import mx.rpc.http.HTTPService;
  	
  	public class SecureDestinationTests extends TestCase {
  	
  		private var protectedPingService:RemoteObject = new RemoteObject();
  	
  		private var protectedCs:ChannelSet = new ChannelSet();
  	
  		private var responseChecker:ResponseChecker;
  		
  		override public function setUp():void {
			
			protectedCs.addChannel(new AMFChannel("myAmf", 
    		"http://{server.name}:{server.port}/flex-integration/spring/protected/messagebroker/amf"));
			
			protectedPingService.channelSet = protectedCs;
			
			responseChecker = new ResponseChecker();
  		}
  		
  		public function testProtectedChannelConnect():void {
  			var params:Object = new Object();
  			params.param1 = 'val1';
  			
  			var service:HTTPService = new HTTPService();
  			service.url="http://localhost:8080/flex-integration/spring/protected/messagebroker/amf";
  			service.method="POST";
  			
  			service.addEventListener("result",function(event:Event):void {
  				Alert.show("result received: "+event.toString());
  			});
  			
  			service.addEventListener("fault",function(event:Event):void {
  				Alert.show("fault received: "+event.toString());
  			});
  			
  			service.send(params);
  		}
  		
//  		public function testPingService_ProtectedDestination():void {
//  			
//  			protectedPingService.destination = "pingRemote";
//  			
//  			protectedPingService.ping.addEventListener("result", function(event:ResultEvent):void {	
//  				responseChecker.result(event);
//  			});
//  			
//  			protectedPingService.addEventListener("fault", function faultHandler (event:FaultEvent):void {
//  				responseChecker.expected = true;
//           		responseChecker.result(event);
//        	});
//  			
//  			responseChecker.addEventListener("resultReceived",addAsync(function(event:Event):void{ 
//        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
//        		assertTrue("Event was not a FaultEvent",responseChecker.resultEvent is FaultEvent);
//        	},5000));
//  			
//  			protectedPingService.ping();
//  		}
  	
	}	
}

import flash.events.Event;
import flash.events.EventDispatcher;
    
class ResponseChecker extends EventDispatcher {
	public var expected:Boolean = false;
	public var resultEvent:Event = null;
	
	public function result(event:Event):void {
		resultEvent = event;
		dispatchEvent(new Event("resultReceived"));
	}
}