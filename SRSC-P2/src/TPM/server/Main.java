package TPM.server;

import java.io.IOException;


public class Main {



	public static void main(String[] args) throws IOException {

		int port = -1;
		String keyStore = null, keyStorePwd = null,
				keyPairEntry = null, keyPairPwd = null,
				pathConfig = null;

		
		if(args.length < 6)
			System.out.println("Usage: <port> <keyStoreLocation> <keyStorePassword> <keyPairEntry> <keyPairPwd> <TPMServerConfigPath>");
		else {
		
			port = Integer.parseInt(args[0]);
			
			keyStore = args[1];
			keyStorePwd = args[2];

			keyPairEntry = args[3];
			keyPairPwd = args[4];

			pathConfig = args[5];
			
		}


		//new GOSTPMServer(port, keyStore, keyStorePwd, keyPairPwd, keyPairEntry, pathConfig);
		//new VMSTPMServer(port, keyStore, keyStorePwd, keyPairPwd, keyPairEntry, pathConfig);


		new GOSTPMServer(4446, "GOSTPMKeyStore.jks", "srscsrsc", "srscsrsc", "goskeypair", "GOSTPMConfig.xml");
		new VMSTPMServer(4443, "VMSTPMKeyStore.jks", "srscsrsc", "srscsrsc", "vmskeypair", "VMSTPMConfig.xml");

	}

}
