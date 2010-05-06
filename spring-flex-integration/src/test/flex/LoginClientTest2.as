package {
  
	import net.digitalprimates.fluint.tests.TestCase;
	
    import flash.events.Event;
    import flash.events.EventDispatcher;
  	import mx.messaging.ChannelSet;
  	import mx.messaging.channels.AMFChannel;
  	import mx.rpc.AsyncToken;
    import mx.rpc.AsyncResponder;
  	import mx.rpc.events.ResultEvent;
    import mx.rpc.events.FaultEvent;  	
  	import mx.rpc.remoting.RemoteObject;
  	import mx.controls.Alert;

  	public class LoginClientTest2 extends TestCase {
  	
  		private var protectedPingService:RemoteObject;
  	
  		private var protectedCs:ChannelSet;
  	
  		private var protectedByChannelIdPingService:RemoteObject;
  	
		private var protectedByChannelIdCs:ChannelSet;
  	
  		private var responseChecker:ResponseChecker;
  	
  		private var asyncTimeout:int = 60000;
  		
  		override protected function setUp():void  {
  			
  			protectedPingService = new RemoteObject();
  			protectedCs = new ChannelSet();
  			protectedByChannelIdPingService = new RemoteObject();
  			protectedByChannelIdCs = new ChannelSet();
			
			protectedCs.addChannel(new AMFChannel("myAmf", 
    		"http://{server.name}:{server.port}/flex-integration/spring/protected/messagebroker/amf"));
			
			protectedPingService.channelSet = protectedCs;
			
			protectedByChannelIdCs.addChannel(new AMFChannel("myAmf", 
    		"http://{server.name}:{server.port}/flex-integration/spring/protected2/messagebroker/amf"));
			
			protectedByChannelIdPingService.channelSet = protectedByChannelIdCs;
			
			
			responseChecker = new ResponseChecker();
  		}
  		
  		public function testSpringManagedSecureChannel_LoginLogoutValidCredentials():void {
  			assertFalse("Client already authenticated",protectedCs.authenticated);
  			
  			var token:AsyncToken = protectedCs.login("keith","melbourne");
  			token.addResponder(
  				new AsyncResponder(
  					function(result:ResultEvent, token:Object = null):void{
  						responseChecker.expected = true;
  						responseChecker.result(result);
  					},
  					function(result:FaultEvent, token:Object = null):void{
  						responseChecker.result(result);
  					}
  				)
  			);
  			
  			responseChecker.addEventListener("resultReceived",asyncHandler(function(event:Event, data:Object):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a ResultEvent",responseChecker.resultEvent is ResultEvent);
        		//Alert.show(ResultEvent(responseChecker.resultEvent).result[0].toString());
        		//Alert.show(ResultEvent(responseChecker.resultEvent).result[1].toString());
        		assertEquals("ResultEvent does not contain expected roles", "ROLE_USER", ResultEvent(responseChecker.resultEvent).result.authorities[0]);
        		assertTrue("Login was not successful", protectedCs.authenticated);
//        		token = protectedCs.logout();
//        		token.addResponder(
//          			new AsyncResponder(
//          				function(result:ResultEvent, token:Object = null):void{
//          					responseChecker.result(result);
//          				},
//          				function(result:FaultEvent, token:Object = null):void{
//          					responseChecker.result(result);
//          				}
//          			)
//          		);
        	},asyncTimeout)); 			
  			
//			responseChecker.addEventListener("resultReceived2",asyncHandler(function(event:Event, data:Object):void{ 
//        		assertTrue("Event was not a ResultEvent",responseChecker.resultEvent is ResultEvent);
//        		//Alert.show(ResultEvent(responseChecker.resultEvent).result.toString());
//        		assertEquals("ResultEvent does not indicate success", "success", ResultEvent(responseChecker.resultEvent).result);
//        		assertFalse("Not logged out",protectedCs.authenticated);
//			},asyncTimeout));
  			
  		}
  	
	}	
}

import flash.events.Event;
import flash.events.EventDispatcher;
    
class ResponseChecker extends EventDispatcher {
	public var expected:Boolean = false;
	public var resultEvent:Event = null;
	public var count:int = 0;
	
	public function result(event:Event):void {
		count++;
		resultEvent = event;
		if(count == 1) {
			dispatchEvent(new Event("resultReceived"));
		} else {
			dispatchEvent(new Event("resultReceived"+count));
		}
	}
}