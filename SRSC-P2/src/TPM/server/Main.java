package TPM.server;

public class Main {

	private static GOSTPMServer gosServer;
	private static VMSTPMServer vmsServer;

	
	public static void main(String[] args) {

		gosServer = new GOSTPMServer(4443);
		vmsServer = new VMSTPMServer(4443);
		
		gosServer.initiateAttestationProtocol();
		vmsServer.initiateAttestationProtocol();
	}

}
