> How to run GOS TPM server:

java -jar GOSTPM.jar <port> <TPMServerConfigPath> <TPMServerAttestationCommandsPath>

	example:

	java -jar GOSTPM.jar 4446, "GOSTPMServerConfig.json", "GOSTPMAttestCommands.json"


> How to run VMS TPM server:

	java -jar VMSTPM.jar <port> <TPMServerConfigPath> <TPMServerAttestationCommandsPath>

	example:

	java -jar VMSTPM.jar 4446, "VMSTPMServerConfig.json", "VMSTPMAttestCommands.json"


	where: 

	> TPMServerConfigPath - is the path to the JSON file that stores all of the configuration for the TPM module (key stores, passwords, ciphersuites for TLS, etc...) except the shell commands to run during the attestation. 
	ATTENTION: use the exact JSON format provided in "GOSTPMServerConfig.json" or "VMSTPMServerConfig.json" (they have the same structure) and if you are not going to use the initialization vector, set as null on the JSON file.



	> TPMServerAttestationCommandsPath - is the path to the file that stores all the shell commands, and the respective columns to output, to serve has a attestation from TPM Server. 
	ATTENTION: If you do not want to filter any columns, set columns to null on the JSON file. Also, the index of the columns start at zero. These files: "GOSTPMAttestCommands.json" or "VMSTPMAttestCommands.json" serves as an example.


