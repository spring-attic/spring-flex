package {
	
	import net.digitalprimates.fluint.tests.TestSuite;
	
	public class IntegrationTestSuite extends TestSuite {
		
		public function IntegrationTestSuite() {
			addTestCase(new RemoteServiceTests());
			addTestCase(new SecureDestinationTests());
			addTestCase(new SecureRemoteObjectTests());
			addTestCase(new MessageServiceTests());
		}
	}
}