## Final SRSC Project README

### Run Instructions:

######TODO

###Configuration

####VMSTPMAttestCommands.json
	* This file stores all shell commands and the respective columns to serve has a attestation from VMS TPM Server. If you do not want to filter any columns, set columns to null on the JSON file. Also, the index of the columns start at zero.
####GOSTPMAttestCommands.json
	* This file stores all shell commands and the respective columns to serve has a attestation from GOS TPM Server. If you do not want to filter any columns, set columns to null on the JSON file. Also, the index of the columns start at zero.

####GOSTPMServerConfig.json
	* This file stores all configurations for the Guest OS TPM server. (key stores, passwords, ciphersuites for TLS, etc...). If you are not going to use the initialization vector, set as null on the JSON file.
####VMSTPMServerConfig.json
	* This file stores all configurations for the VMS TPM server. (key stores, passwords, ciphersuites for TLS, etc...). If you are not going to use the initialization vector, set as null on the JSON file.

###Files

####TPMClientTrustStore
	* goscert (RSA - 4096)
	* vmscert (RSA - 4096)
####GOSTPMKeyStore
	* gospair (RSA - 4096)
####VMSKeyStore
	* vmskeypair (RSA - 4096)




#### Authors:
	* André Lopes nº 45617
	* Nelson Coquenim nº 45694
	* Simão Dolores nº 45020