package TPM.server;


public class GOSTPMServer extends TPMServer{
	
	public GOSTPMServer(int port, String configFilePath, String attestCommandsPath) {
		super(port, configFilePath, attestCommandsPath);
		super.initiateAttestationProtocol();
	}
}
