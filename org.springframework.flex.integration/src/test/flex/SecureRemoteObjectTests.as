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
  	
  	public class SecureRemoteObjectTests extends TestCase {

  		private var securedPingService:RemoteObject = new RemoteObject();
  	
  		private var cs:ChannelSet = new ChannelSet();
  	
  		private var responseChecker:ResponseChecker;
  	
	  	override public function setUp():void  {
			
			cs.addChannel(new AMFChannel("myAmf", 
			"http://{server.name}:{server.port}/flex-integration/spring/messagebroker/amf"));
			
			securedPingService.channelSet = cs;
			
			responseChecker = new ResponseChecker();
		}
	  	
	  	public function testSecureMethod_NotAuthenticated():void {
  			
  			securedPingService.destination = "pingSecureService";
  			
  			securedPingService.ping.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.result(event);
  			});
  			
  			securedPingService.addEventListener("fault", function(event:FaultEvent):void {
  				responseChecker.expected = true;
           		responseChecker.result(event);
        	});
  			
  			responseChecker.addEventListener("resultReceived",addAsync(function(event:Event):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a FaultEvent",responseChecker.resultEvent is FaultEvent);
        		//Alert.show(FaultEvent(responseChecker.resultEvent).toString());
        		assertEquals("The fault code was incorrect", "Client.Authentication",FaultEvent(responseChecker.resultEvent).fault.faultCode);
        	},5000));
  			
  			securedPingService.ping();
  		
	  	}
	  	
	  	public function testSecureMethod_NotAuthorized():void {
	  		
	  		login(cs, "keith", "melbourne");
	  		
  			securedPingService.destination = "pingSecureService";
  			
  			securedPingService.ping.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.result(event);
  			});
  			
  			securedPingService.addEventListener("fault", function(event:FaultEvent):void {
  				responseChecker.expected = true;
           		responseChecker.result(event);
        	});
  			
  			responseChecker.addEventListener("resultReceived2",addAsync(function(event:Event):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a FaultEvent",responseChecker.resultEvent is FaultEvent);
        		Alert.show(FaultEvent(responseChecker.resultEvent).toString());
        		assertEquals("The fault code was incorrect", "Client.Authorization",FaultEvent(responseChecker.resultEvent).fault.faultCode);
        	},5000));
  			
  			securedPingService.ping();
  			
  			logout(cs, 3);
  		
	  	}
	  	
	  	public function testSecuredMethod_Authorized():void {
	  		
	  		login(cs, "jeremy", "atlanta");
	  		
	  		securedPingService.destination = "pingSecureService";
  			
  			securedPingService.ping.addEventListener("result", function(event:ResultEvent):void {	
  				responseChecker.expected=true;
  				responseChecker.result(event);
  			});
  			
  			securedPingService.addEventListener("fault", function faultHandler (event:FaultEvent):void {
           		responseChecker.result(event);
        	});            
        	
        	responseChecker.addEventListener("resultReceived2",addAsync(function(event:Event):void{ 
        		assertTrue("The expected response was not received.  Result event was: "+responseChecker.resultEvent,responseChecker.expected);
        		assertTrue("Event was not a ResultEvent",responseChecker.resultEvent is ResultEvent);
        		assertEquals("Unexpected response from service call", "pong", ResultEvent(responseChecker.resultEvent).result);
        	},5000));
  			
  			securedPingService.ping();
  			
  			logout(cs, 3)
	  	}
	  	
	  	
	  	
	  	private function login(protectedCs:ChannelSet, name:String, password:String):void {
	  		assertFalse("Client already authenticated",protectedCs.authenticated);
	  		var token:AsyncToken = protectedCs.login(name,password);
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
        	},5000)); 			
	  	}
	  	
	  	private function logout(protectedCs:ChannelSet, count:int):void {
	  		if (!protectedCs.authenticated) {
	  			return;
	  		}
	  		
	  		var token:AsyncToken = protectedCs.logout();
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
	  		
	  		responseChecker.addEventListener("resultReceived"+count,addAsync(function(event:Event):void{ 
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