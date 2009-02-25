package {
  
    import flash.events.Event;
    import flash.events.EventDispatcher;
  	import mx.messaging.ChannelSet;
  	import mx.messaging.channels.AMFChannel;
  	import mx.rpc.AsyncToken;
    import mx.rpc.AsyncResponder;
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
  	
  		private var protectedByChannelIdPingService:RemoteObject = new RemoteObject();
  	
		private var protectedByChannelIdCs:ChannelSet = new ChannelSet();
  	
  		private var blazeService:RemoteObject = new RemoteObject();
  	
		private var blazeProtectedCs:ChannelSet = new ChannelSet();
  	
  		private var responseChecker:ResponseChecker;
  		
  		override public function setUp():void  {
			
			protectedCs.addChannel(new AMFChannel("myAmf", 
    		"http://{server.name}:{server.port}/flex-integration/spring/protected/messagebroker/amf"));
			
			protectedPingService.channelSet = protectedCs;
			
			protectedByChannelIdCs.addChannel(new AMFChannel("myAmf", 
    		"http://{server.name}:{server.port}/flex-integration/spring/protected2/messagebroker/amf"));
			
			protectedByChannelIdPingService.channelSet = protectedByChannelIdCs;
			
			blazeProtectedCs.addChannel(new AMFChannel("myAmf", 
			"http://{server.name}:{server.port}/flex-integration/spring/messagebroker/amf"));
			
			blazeService.channelSet = blazeProtectedCs;
			
			responseChecker = new ResponseChecker();
  		}
  		
  		public function testSpringManagedSecureChannelUrl_NotAuthenticated():void {
  			
  			protectedPingService.destination = "pingRemote";
  			
  			protectedPingService.ping.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.result(event);
  			});
  			
  			protectedPingService.addEventListener("fault", function(event:FaultEvent):void {
  				responseChecker.expected = true;
           		responseChecker.result(event);
        	});
  			
  			responseChecker.addEventListener("resultReceived",addAsync(function(event:Event):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a FaultEvent",responseChecker.resultEvent is FaultEvent);
        		//Alert.show(FaultEvent(responseChecker.resultEvent).toString());
        		assertEquals("The fault code was incorrect", "Client.Authentication",FaultEvent(responseChecker.resultEvent).fault.faultCode);
        	},5000));
  			
  			protectedPingService.ping();
  		}
  		
  		public function testSpringManagedSecureChannelId_NotAuthenticated():void {
  			
  			protectedByChannelIdPingService.destination = "pingRemote";
  			
  			protectedByChannelIdPingService.ping.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.result(event);
  			});
  			
  			protectedByChannelIdPingService.addEventListener("fault", function(event:FaultEvent):void {
  				responseChecker.expected = true;
           		responseChecker.result(event);
        	});
  			
  			responseChecker.addEventListener("resultReceived",addAsync(function(event:Event):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a FaultEvent",responseChecker.resultEvent is FaultEvent);
        		//Alert.show(FaultEvent(responseChecker.resultEvent).toString());
        		assertEquals("The fault code was incorrect", "Client.Authentication",FaultEvent(responseChecker.resultEvent).fault.faultCode);
        	},5000));
  			
  			protectedByChannelIdPingService.ping();
  		}
  		
  		public function testSpringManagedSecureChannel_LoginInvalidCredentials():void {
  			assertFalse(protectedCs.authenticated);
  			var token:AsyncToken = protectedCs.login("bogus_user","bogus_password");
  			token.addResponder(
  				new AsyncResponder(
  					function(result:ResultEvent, token:Object = null):void{
  						responseChecker.result(result);
  					},
  					function(result:FaultEvent, token:Object = null):void{
  						responseChecker.expected = true;
  						responseChecker.result(result);
  					}
  				)
  			);
  			
  			responseChecker.addEventListener("resultReceived",addAsync(function(event:Event):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a FaultEvent",responseChecker.resultEvent is FaultEvent);
        		//Alert.show(FaultEvent(responseChecker.resultEvent).toString());
        		assertEquals("The fault code was incorrect", "Client.Authentication",FaultEvent(responseChecker.resultEvent).fault.faultCode);
        		assertEquals("The fault detail was incorrect", "Bad credentials", FaultEvent(responseChecker.resultEvent).fault.faultString);
        	},5000));
  		}
  		
  		public function testSpringManagedSecureChannel_LoginLogoutValidCredentials():void {
  			assertFalse("Client already authenticated",protectedCs.authenticated);
  			
  			var token:AsyncToken = protectedCs.login("jeremy","atlanta");
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
  			
  			responseChecker.addEventListener("resultReceived",addAsync(function(event:Event):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a ResultEvent",responseChecker.resultEvent is ResultEvent);
        		//Alert.show(ResultEvent(responseChecker.resultEvent).result.toString());
        		assertEquals("ResultEvent does not indicate success", "success", ResultEvent(responseChecker.resultEvent).result);
        		assertTrue("Login was not successful", protectedCs.authenticated);
        		token = protectedCs.logout();
        		token.addResponder(
          			new AsyncResponder(
          				function(result:ResultEvent, token:Object = null):void{
          					responseChecker.result(result);
          				},
          				function(result:FaultEvent, token:Object = null):void{
          					responseChecker.result(result);
          				}
          			)
          		);
        	},5000)); 			
  			
			responseChecker.addEventListener("resultReceived2",addAsync(function(event:Event):void{ 
        		assertTrue("Event was not a ResultEvent",responseChecker.resultEvent is ResultEvent);
        		//Alert.show(ResultEvent(responseChecker.resultEvent).result.toString());
        		assertEquals("ResultEvent does not indicate success", "success", ResultEvent(responseChecker.resultEvent).result);
        		assertFalse("Not logged out",protectedCs.authenticated);
			},5000));
  			
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