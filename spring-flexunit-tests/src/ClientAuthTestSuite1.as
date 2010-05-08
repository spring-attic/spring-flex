package {
	
	import net.digitalprimates.fluint.tests.TestSuite;
	
	public class ClientAuthTestSuite1 extends TestSuite {
		
		public function ClientAuthTestSuite1() {
			addTestCase(new LoginClientTest1());
		}
	}
}