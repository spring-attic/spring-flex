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
  	
  	import mx.messaging.Consumer;
  	import mx.messaging.events.MessageEvent;
  	import mx.messaging.events.MessageFaultEvent;

  	
  	
  	public class MessageServiceTests extends TestCase {
  		
  		private var pingService:RemoteObject;
  	
  		private var pingConsumer:Consumer;
	
  		private var cs:ChannelSet = new ChannelSet();
	
		private var responseChecker:ResponseChecker;
		
		override protected function setUp():void {
			pingService = new RemoteObject();
			pingConsumer = new Consumer();
			
			pingService.destination = "pingService";
			pingConsumer.destination = "event-bus";
			
			cs.addChannel(new AMFChannel("myPollingAmf", 
			"http://{server.name}:{server.port}/flex-integration/spring/messagebroker/amfpolling"));
			
			pingService.channelSet = cs;
			pingConsumer.channelSet = cs;
			
			responseChecker = new ResponseChecker();
		}
		
		public function testConsumeSimpleMessageTemplateMessage():void {
			
			pingService.destination = "pingService";
			
			pingConsumer.addEventListener("message", function(event:MessageEvent):void {	
  				responseChecker.expected=true;
  				responseChecker.result(event);
  			});
  			
			pingConsumer.addEventListener("fault", function faultHandler (event:MessageFaultEvent):void {
           		responseChecker.result(event);
        	});
			
			pingConsumer.subscribe();
			
  			pingService.addEventListener("fault", function faultHandler (event:FaultEvent):void {
           		responseChecker.result(event);
        	});            
        	
        	responseChecker.addEventListener("resultReceived",asyncHandler(function(event:Event, data:Object):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a MessageEvent",responseChecker.resultEvent is MessageEvent);
        		assertEquals("Unexpected response from service call", "fired", MessageEvent(responseChecker.resultEvent).message.body);
        	},5000));
  			
  			pingService.fireEvent();
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