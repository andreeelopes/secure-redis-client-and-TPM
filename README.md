## Final SRSC Project README

### Run Instructions:

######TODO

###Configuration

####VMSTPMAttestCommands.json
##### This file stores all shell commands and the respective columns to serve has a attestation from VMS TPM Server.
####GOSTPMAttestCommands.json
##### This file stores all shell commands and the respective columns to serve has a attestation from GOS TPM Server.

####GOSTPMServerConfig.json
##### This file stores all configurations for the Guest OS TPM server. (key stores, passwords, ciphersuites for TLS, etc...)
####VMSTPMServerConfig.json
##### This file stores all configurations for the VMS TPM server. (key stores, passwords, ciphersuites for TLS, etc...)

###Files

####TPMClientTrustStore
	* goscert (RSA - 4096)
	* vmscert (RSA - 4096)
####GOSTPMKeyStore
	*gospair (RSA - 4096)
####VMSKeyStore
	*vmskeypair (RSA - 4096)




#### Authors:

	* André Lopes nº 45617
	* Nelson Coquenim nº 45694
	* Simão Dolores nº 45020