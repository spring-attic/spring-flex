package {
	
    import flash.events.Event;
    import flash.events.EventDispatcher;
  	import mx.messaging.ChannelSet;
  	import mx.messaging.channels.AMFChannel;
  	import mx.rpc.events.ResultEvent;
    import mx.rpc.events.FaultEvent;  	
  	import mx.rpc.remoting.RemoteObject;
  	import mx.controls.Alert;
  	
  	
  	public class HibernateRemoteObjectTests {
  		
  		private var personService:RemoteObject;
		
		private var cs:ChannelSet = new ChannelSet();
  	
  		private var responseChecker:ResponseChecker;
  		
		[Before]
  		public function setUp():void {
  			personService = new RemoteObject();
  			
  			cs.addChannel(new AMFChannel("myAmf", 
    		"http://localhost:8080/flex-integration/spring/messagebroker/amf"));
			personService.channelSet = cs;
			personService.destination = "";
				
			responseChecker = new ResponseChecker();
  		}
  	
		[Test]
  		public function testCallService():void {
 
  			pingService.destination = "personService";
  			
  			pingService.ping.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.expected=true;
  				responseChecker.result(event);
  			});
  			
  			pingService.addEventListener("fault", function faultHandler (event:FaultEvent):void {
           		responseChecker.result(event);
        	});            
        	
        	responseChecker.addEventListener("resultReceived",asyncHandler(function(event:Event, data:Object):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a ResultEvent",responseChecker.resultEvent is ResultEvent);
        		//assertEquals("Unexpected response from service call", "pong", ResultEvent(responseChecker.resultEvent).result);
        	},5000));
  			
  			
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