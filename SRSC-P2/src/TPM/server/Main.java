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


		//new GOSTPMServer(port, configPath);
		//new VMSTPMServer(port, configPath);


		//new GOSTPMServer(4446, "GOSTPMServerConfig.json");
		new VMSTPMServer(4443, "VMSTPMServerConfig.json");
		
//		TPMServerConfig tpmconfig = new TPMServerConfig("GOSTPMKeyStore.jks", "srscsrsc", "goskeypair", "srscsrsc",
//				new String[] {"TLS_RSA_WITH_AES_256_CBC_SHA256"}, new String[] {"TLSv1.2"}, "SunX509", "TLS",
//				"SHA256withRSA", "BC", "DH", "BC", "SHA256", "BC", "AES/CBC/PKCS7Padding", "BC", 256);
//		
//		System.out.println("JSON:\n" + tpmconfig.toJSON());

	}

}
