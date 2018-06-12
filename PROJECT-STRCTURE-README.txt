
/SRSC-P2: project folder - holds all the certificates and config files;
/docs: contains the REPORT and all the pdfs provided by the teatcher (Enunciado, Background);


/SRSC-P2/src/TPM: TPM related folders and java classes;
/SRSC-P2/src/TPM/server: contains the code for the TPM modules, they are going to be seen has a server because they provide a attestation service;
/SRSC-P2/src/TPM/client: contains the code to interact with both of the TPM modules;
/SRSC-P2/src/benchmarkSafeRedis: contains the benchmark for "safe" Redis;
/SRSC-P2/src/redis/client: contains the code that extends JEDIS to make it "safe";
/SRSC-P2/src/utils: contains some classes with general utilities.



Files for the TPM Configurations:

VMSTPMAttestCommands.json
	* This file stores all shell commands and the respective columns to serve has a attestation from VMS TPM Server. If you do not want to filter any columns, set columns to null on the JSON file. Also, the index of the columns start at zero.
GOSTPMAttestCommands.json
	* This file stores all shell commands and the respective columns to serve has a attestation from GOS TPM Server. If you do not want to filter any columns, set columns to null on the JSON file. Also, the index of the columns start at zero.

GOSTPMServerConfig.json
	* This file stores all configurations for the Guest OS TPM server. (key stores, passwords, ciphersuites for TLS, etc...). If you are not going to use the initialization vector, set as null on the JSON file.
VMSTPMServerConfig.json
	* This file stores all configurations for the VMS TPM server. (key stores, passwords, ciphersuites for TLS, etc...). If you are not going to use the initialization vector, set as null on the JSON file.

Key Stores and TrustStore for the TPM servers and client:

TPMClientTrustStore
	* goscert (RSA - 4096)
	* vmscert (RSA - 4096)
GOSTPMKeyStore
	* gospair (RSA - 4096)
VMSKeyStore
	* vmskeypair (RSA - 4096)