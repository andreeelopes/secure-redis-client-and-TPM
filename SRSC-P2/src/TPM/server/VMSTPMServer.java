package TPM.server;

public class VMSTPMServer extends TPMServer{


	public VMSTPMServer(int port, String configFilePath, String attestCommandsPath) {
		super(port, configFilePath, attestCommandsPath);
		super.initiateAttestationProtocol();
	}
}
