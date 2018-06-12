package TPM.server;

public class VMSMain {

	public static void main(String[] args) {

		int port = -1;
		String configPath = null,
				attestCommandsPath = null;


		if(args.length < 3)
			System.out.println("Usage: <port> <TPMServerConfigPath> <TPMServerAttestationCommandsPath>");
		else {
			port = Integer.parseInt(args[0]);
			configPath = args[1];
			attestCommandsPath = args[2];

		}

		new VMSTPMServer(port, configPath, attestCommandsPath);	

		//for debug use
		//new VMSTPMServer(4443, "VMSTPMServerConfig.json", "VMSTPMAttestCommands.json");

	}

}
