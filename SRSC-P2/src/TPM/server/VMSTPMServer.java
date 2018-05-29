package TPM.server;

import java.security.MessageDigest;

import utils.Utils;

public class VMSTPMServer extends TPMServer{

	private static final String PROCESSES_WITH_ROOT_PERM = "ps -U root"; 
	private static final String LINUX_EXECUTABLES_PATH_WITH_FILTER = "ls -l /sbin";


	public VMSTPMServer(int port, String pathToKeyStore, String keyStorePwd, String entryPwd, String keyEntryName, String pathToConfigFile) {
		super(port, pathToKeyStore, keyStorePwd, pathToConfigFile);
		super.initiateAttestationProtocol(keyStorePwd, entryPwd, keyEntryName);
	}

	public byte[] getSnapshot() {
		byte [] attestations = null;
		MessageDigest hash;

		try {
			hash = MessageDigest.getInstance("SHA256", "BC");

			//String processes = executeShellCommand(PROCESSES_WITH_ROOT_PERM,
				//	new int[] {3});
			String executables = executeShellCommand(LINUX_EXECUTABLES_PATH_WITH_FILTER,
					null);

			//hash.update(Utils.toByteArray(processes));
			hash.update(Utils.toByteArray(executables));
			attestations = hash.digest();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return attestations;
	}
	
}
