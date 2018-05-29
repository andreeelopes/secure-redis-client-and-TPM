package utils;

import java.io.Serializable;

public class CipherConfig implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String macAlgorithm;

	private String cipherSuite;
	private String macProvider;
	private String cipherProvider;
	private int cipherKeySize;
	private int macKeySize;
	private String cipherAlg; 
	private String iv;


	public CipherConfig(String macAlgorithm, String cipherSuite, String macProvider, String cipherProvider,
			int cipherKeySize, int macKeySize, String iv) {
		this.macAlgorithm = macAlgorithm;
		this.cipherSuite = cipherSuite;
		this.macProvider = macProvider;
		this.cipherProvider = cipherProvider;
		this.cipherKeySize = cipherKeySize;
		this.macKeySize = macKeySize;
		this.iv=iv;
		this.cipherAlg = cipherSuite.split("/")[0];
	}
	public String getIv() {
		return iv;
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