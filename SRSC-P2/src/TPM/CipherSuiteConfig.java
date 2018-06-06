package TPM;

import java.io.Serializable;

import com.google.gson.Gson;

public class CipherSuiteConfig implements Serializable{

	private static final long serialVersionUID = 1L;
		
	
	//hash and symmetric encryption key
	private String hashAlg;
	private String hashProvider;
	private String symmAlg;
	private String symmProvider;
	private int symmKeySize;
	private byte[] iv;

	public CipherSuiteConfig(String hashAlg, String hashProvider, String symmAlg, String symmProvider,
			int symmKeySize, byte[] iv) {

		this.hashAlg = hashAlg;
		this.hashProvider = hashProvider;
		this.symmAlg = symmAlg;
		this.symmProvider = symmProvider;
		this.symmKeySize = symmKeySize;
		this.iv = iv;
	}

	public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		
		return json;
	}

	public String getHashAlg() {
		return hashAlg;
	}
	public void setHashAlg(String hashAlg) {
		this.hashAlg = hashAlg;
	}


	public String getHashProvider() {
		return hashProvider;
	}
	public void setHashProvider(String hashProvider) {
		this.hashProvider = hashProvider;
	}


	public String getSymmAlg() {
		return symmAlg;
	}
	public void setSymmAlg(String symmAlg) {
		this.symmAlg = symmAlg;
	}


	public String getSymmProvider() {
		return symmProvider;
	}
	public void setSymmProvider(String symmProvider) {
		this.symmProvider = symmProvider;
	}


	public int getSymmKeySize() {
		return symmKeySize;
	}
	public void setSymmKeySize(int symmKeySize) {
		this.symmKeySize = symmKeySize;
	}
	
	public byte[] getIV() {
		return iv;
	}
	public void setIV(byte[] iv) {
		this.iv = iv;
	}
}