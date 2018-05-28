package TPM.server;

import java.io.IOException;

import utils.FileHelper;
import utils.Utils;

public class Main {

	private static GOSTPMServer gosServer;
	private static VMSTPMServer vmsServer;

	
	public static void main(String[] args) throws IOException {

		gosServer = new GOSTPMServer(4443);
		vmsServer = new VMSTPMServer(4443);
		
		gosServer.initiateAttestationProtocol();
		vmsServer.initiateAttestationProtocol();
	}

}
