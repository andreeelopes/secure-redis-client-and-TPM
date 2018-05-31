package TPM.client;

import com.google.gson.Gson;

public class TPMClientConfig {

	//trust store and both tpm modules certs entries
	private String trustStorePath;
	private String trustStorePwd;
	private String gosCertEntry;
	private String vmsCertEntry;

	//diffie hellman
	private String dhAlg;
	private String dhProvider;

	//digital signature
	private String signatureAlg;
	private String signatureProvider;


	public TPMClientConfig(String trustStorePath, String trustStorePwd, String gosCertEntry, String vmsCertEntry,
			String dhAlg, String dhProvider, String signatureAlg, String signatureProvider, String hashAlg,
			String hashProvider) {
		this.trustStorePath = trustStorePath;
		this.trustStorePwd = trustStorePwd;
		this.gosCertEntry = gosCertEntry;
		this.vmsCertEntry = vmsCertEntry;
		this.dhAlg = dhAlg;
		this.dhProvider = dhProvider;
		this.signatureAlg = signatureAlg;
		this.signatureProvider = signatureProvider;
	}
	
	public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		
		return json;
	}
	
	//gets and sets for all variables...
	public String getTrustStorePath() {
		return trustStorePath;
	}
	public void setTrustStorePath(String trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	public String getTrustStorePwd() {
		return trustStorePwd;
	}
	public void setTrustStorePwd(String trustStorePwd) {
		this.trustStorePwd = trustStorePwd;
	}

	public String getGosCertEntry() {
		return gosCertEntry;
	}
	public void setGosCertEntry(String gosCertEntry) {
		this.gosCertEntry = gosCertEntry;
	}

	public String getVmsCertEntry() {
		return vmsCertEntry;
	}
	public void setVmsCertEntry(String vmsCertEntry) {
		this.vmsCertEntry = vmsCertEntry;
	}

	public String getDhAlg() {
		return dhAlg;
	}
	public void setDhAlg(String dhAlg) {
		this.dhAlg = dhAlg;
	}

	public String getDhProvider() {
		return dhProvider;
	}
	public void setDhProvider(String dhProvider) {
		this.dhProvider = dhProvider;
	}

	public String getSignatureAlg() {
		return signatureAlg;
	}
	public void setSignatureAlg(String signatureAlg) {
		this.signatureAlg = signatureAlg;
	}

	public String getSignatureProvider() {
		return signatureProvider;
	}
	public void setSignatureProvider(String signatureProvider) {
		this.signatureProvider = signatureProvider;
	}
}
