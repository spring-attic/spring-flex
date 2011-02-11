package {
	
	import net.digitalprimates.fluint.tests.TestSuite;
	
	public class ClientAuthTestSuite2 extends TestSuite {
		
		public function ClientAuthTestSuite2() {
			addTestCase(new LoginClientTest2());
		}
	}
}