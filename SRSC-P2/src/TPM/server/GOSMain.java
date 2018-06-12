package TPM.server;

import java.io.IOException;


public class GOSMain {



	public static void main(String[] args) throws IOException {

		int port = -1;
		String configPath = null,
				attestCommandsPath = null;


		if(args.length < 3)
			System.out.println("Usage: <port> <TPMServerConfigPath> <TPMServerAttestationCommandsPath");
		else {
			port = Integer.parseInt(args[0]);
			configPath = args[1];
			attestCommandsPath = args[2];

		}

		new GOSTPMServer(port, configPath, attestCommandsPath);

		//for debug use
		//new GOSTPMServer(4446, "GOSTPMServerConfig.json", "GOSTPMAttestCommands.json");

	}

}
