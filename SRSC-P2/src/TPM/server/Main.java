package TPM.server;

import java.io.IOException;

import utils.FileHelper;
import utils.Utils;

public class Main {


	
	public static void main(String[] args) throws IOException {

		new GOSTPMServer(4443, "GOSTPMKeyStore.jks", "srscsrsc", "srscsrsc", "goskeypair");
		new VMSTPMServer(4443, "VMSTPMKeyStore.jks", "srscsrsc", "srscsrsc", "vmskeypair");
		
	}

}
