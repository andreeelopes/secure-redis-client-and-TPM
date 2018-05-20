package client;

public class CipherConfig {
	
	private String macAlgorithm;
	private String cipherSuite;
	private String macProvider;
	private String cipherProvider;

	public CipherConfig(String cipherSuite,String macAlgorithm,String macProvider,String cipherProvider) {
		this.setCipherSuite(cipherSuite);
		this.setMacAlgorithm(macAlgorithm);
		this.setMacProvider(macProvider);
		this.setCipherProvider(cipherProvider);
	}

	public String getMacProvider() {
		return macProvider;
	}

	private void setMacProvider(String macProvider) {
		this.macProvider = macProvider;
	}

	public String getCipherSuite() {
		return cipherSuite;
	}

	private void setCipherSuite(String cipherSuite) {
		this.cipherSuite = cipherSuite;
	}

	public String getCipherProvider() {
		return cipherProvider;
	}

	private void setCipherProvider(String cipherProvider) {
		this.cipherProvider = cipherProvider;
	}

	public String getMacAlgorithm() {
		return macAlgorithm;
	}

	private void setMacAlgorithm(String macAlgorithm) {
		this.macAlgorithm = macAlgorithm;
	}
}
