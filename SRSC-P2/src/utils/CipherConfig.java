package utils;

public class CipherConfig {

	private String macAlgorithm;

	private String cipherSuite;
	private String macProvider;
	private String cipherProvider;
	private int cipherKeySize;
	private int macKeySize;
	private String cipherAlg; 


	public CipherConfig(String macAlgorithm, String cipherSuite, String macProvider, String cipherProvider,
			int cipherKeySize, int macKeySize) {
		this.macAlgorithm = macAlgorithm;
		this.cipherSuite = cipherSuite;
		this.macProvider = macProvider;
		this.cipherProvider = cipherProvider;
		this.cipherKeySize = cipherKeySize;
		this.macKeySize = macKeySize;
		this.cipherAlg = cipherSuite.split("/")[0];
	}

	public String getMacAlgorithm() {
		return macAlgorithm;
	}

	public String getCipherSuite() {
		return cipherSuite;
	}

	public String getMacProvider() {
		return macProvider;
	}

	public String getCipherProvider() {
		return cipherProvider;
	}

	public int getCipherKeySize() {
		return cipherKeySize;
	}

	public int getMacKeySize() {
		return macKeySize;
	}

	public String getCipherAlg() {
		return cipherAlg;
	}
}