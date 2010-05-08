package {
  
	import net.digitalprimates.fluint.tests.TestCase;
	
    import flash.events.Event;
    import flash.events.EventDispatcher;
  	import mx.messaging.ChannelSet;
  	import mx.messaging.channels.AMFChannel;
  	import mx.rpc.events.ResultEvent;
    import mx.rpc.events.FaultEvent;  	
  	import mx.rpc.remoting.RemoteObject;
  	import mx.controls.Alert;
  	
  	
  	public class RemoteServiceTests extends TestCase {
  		
  		private var pingService:RemoteObject;
  	
  		private var fooService:RemoteObject;
		
		private var cs:ChannelSet = new ChannelSet();
  	
  		private var responseChecker:ResponseChecker;
  		
  		override protected function setUp():void {
  			pingService = new RemoteObject();
  			fooService = new RemoteObject();
  			
  			cs.addChannel(new AMFChannel("myAmf", 
    		"http://localhost:8080/flex-integration/spring/messagebroker/amf"));
			pingService.channelSet = cs;
			fooService.channelSet = cs;
			
			responseChecker = new ResponseChecker();
  		}
		
		[Test]
		public function dummy():void {
		
		}
  	
  		public function testCallService():void {
 
  			pingService.destination = "pingService";
  			
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
        		assertEquals("Unexpected response from service call", "pong", ResultEvent(responseChecker.resultEvent).result);
        	},5000));
  			
  			pingService.ping();
  		}
  		
  		public function testCallAnnotatedService():void {
  			 
  			fooService.destination = "fooService";
  			
  			fooService.bar.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.expected=true;
  				responseChecker.result(event);
  			});
  			
  			fooService.addEventListener("fault", function faultHandler (event:FaultEvent):void {
           		responseChecker.result(event);
        	});            
        	
        	responseChecker.addEventListener("resultReceived",asyncHandler(function(event:Event, data:Object):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a ResultEvent",responseChecker.resultEvent is ResultEvent);
        		assertEquals("Unexpected response from service call", "bar", ResultEvent(responseChecker.resultEvent).result);
        	},5000));
  			
  			fooService.bar();
  		}
  		
  		public function testCallService_UnknownDestination():void {
  			
  			pingService.destination = "pingFoo";
  			
  			
  			pingService.ping.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.result(event);
  			});
  			
  			pingService.addEventListener("fault", function faultHandler (event:FaultEvent):void {
  				responseChecker.expected = true;
           		responseChecker.result(event);
        	});            
        	
        	responseChecker.addEventListener("resultReceived",asyncHandler(function(event:Event, data:Object):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a FaultEvent",responseChecker.resultEvent is FaultEvent);
        	},5000));
  			
  			pingService.ping();
  		}
  		
  		public function testCallService_ExcludedMethod():void {
  			
  			pingService.destination = "pingService";
  			
  			
  			pingService.ping.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.result(event);
  			});
  			
  			pingService.addEventListener("fault", function faultHandler (event:FaultEvent):void {
  				responseChecker.expected = true;
           		responseChecker.result(event);
        	});            
        	
        	responseChecker.addEventListener("resultReceived",asyncHandler(function(event:Event, data:Object):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a FaultEvent",responseChecker.resultEvent is FaultEvent);
        	},5000));
  			
  			pingService.foo();
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