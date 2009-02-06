package {
  
    import flash.events.Event;
    import flash.events.EventDispatcher;
  	import mx.messaging.ChannelSet;
  	import mx.messaging.channels.AMFChannel;
  	import mx.rpc.events.ResultEvent;
    import mx.rpc.events.FaultEvent;  	
  	import mx.rpc.remoting.RemoteObject;
  	import flexunit.framework.Assert;
  	import flexunit.framework.TestCase;
  	import mx.controls.Alert;
  	
  	
  	public class RemoteServiceTests extends TestCase {
  	
  		public function testPingService():void {
  			
  			var pingService:RemoteObject = new RemoteObject();
  			
  			var cs:ChannelSet = new ChannelSet();
  			
  			var responseChecker:ResponseChecker = new ResponseChecker();
  			 
    		cs.addChannel(new AMFChannel("myAmf", 
        		"http://{server.name}:{server.port}/flex-integration/spring/messagebroker/amf"));
  			
  			pingService.channelSet = cs;
  			pingService.destination = "pingRemote";
  			
  			
  			pingService.ping.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.expected=true;
  				responseChecker.result(event);
  			});
  			
  			pingService.addEventListener("fault", function faultHandler (event:FaultEvent):void {
           		responseChecker.result(event);
        	});            
        	
        	responseChecker.addEventListener("resultReceived",addAsync(function(event:Event):void{ 
        		assertTrue("A successful response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a ResultEvent",responseChecker.resultEvent is ResultEvent);
        		assertEquals("Unexpected response from service call", "pong", ResultEvent(responseChecker.resultEvent).result);
        	},5000));
  			
  			pingService.ping();
  		}
  		
  		public function testPingService_UnknownDestination():void {
  			
  			var pingService:RemoteObject = new RemoteObject();
  			
  			var cs:ChannelSet = new ChannelSet();
  			
  			var responseChecker:ResponseChecker = new ResponseChecker();
  			 
    		cs.addChannel(new AMFChannel("myAmf", 
        		"http://{server.name}:{server.port}/flex-integration/spring/messagebroker/amf"));
  			
  			pingService.channelSet = cs;
  			pingService.destination = "pingService";
  			
  			
  			pingService.ping.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.result(event);
  			});
  			
  			pingService.addEventListener("fault", function faultHandler (event:FaultEvent):void {
  				responseChecker.expected = true;
           		responseChecker.result(event);
        	});            
        	
        	responseChecker.addEventListener("resultReceived",addAsync(function(event:Event):void{ 
        		assertTrue("A successful response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a FaultEvent",responseChecker.resultEvent is FaultEvent);
        	},5000));
  			
  			pingService.ping();
  		}
  	
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