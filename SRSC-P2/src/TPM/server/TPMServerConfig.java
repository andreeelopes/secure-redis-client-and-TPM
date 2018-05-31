package TPM.server;

import com.google.gson.Gson;

import TPM.CipherSuiteConfig;
import utils.CipherConfig;

public class TPMServerConfig {

	//key store and public-private key pair
	private String keyStorePath;
	private String keyStorePwd;
	private String keyPairEntry;
	private String keyPairPwd;
	
	//secure socket
	private String[] sslCipherSuites;
	private String[] sslProtocols;
	private String keyManageFactoryAlg; //to be used in KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"); 
	private String secureSocketProtocol;
	
	//digital signature
	private String signatureAlg;
	private String signatureProvider;
	
	//diffie hellman
	private String dhAlg;
	private String dhProvider;
	
	//hash and symmetric encryption key
	private String hashAlg;
	private String hashProvider;
	private String symmAlg;
	private String symmProvider;
	private int symmKeySize;
	
			
	
	
	public TPMServerConfig(String keyStorePath, String keyStorePwd, String keyPairEntry, String keyPairPwd,
			String[] sslCipherSuites, String[] sslProtocols, String keyManageFactoryAlg, String secureSocketProtocol,
			String signatureAlg, String signatureProvider, String dhAlg, String dhProvider, String hashAlg,
			String hashProvider, String symmAlg, String symmProvider, int symmKeySize) {
		this.keyStorePath = keyStorePath;
		this.keyStorePwd = keyStorePwd;
		this.keyPairEntry = keyPairEntry;
		this.keyPairPwd = keyPairPwd;
		this.sslCipherSuites = sslCipherSuites;
		this.sslProtocols = sslProtocols;
		this.keyManageFactoryAlg = keyManageFactoryAlg;
		this.secureSocketProtocol = secureSocketProtocol;
		this.signatureAlg = signatureAlg;
		this.signatureProvider = signatureProvider;
		this.dhAlg = dhAlg;
		this.dhProvider = dhProvider;
		this.hashAlg = hashAlg;
		this.hashProvider = hashProvider;
		this.symmAlg = symmAlg;
		this.symmProvider = symmProvider;
		this.symmKeySize = symmKeySize;
	}
	
	public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		
		return json;
	}
	
	public CipherSuiteConfig getSymEncrypConfig() {
		return new CipherSuiteConfig(getHashAlg(), getHashProvider(), getSymmAlg(), getSymmProvider(), getSymmKeySize());		
	}
	
	
	//gets and sets for all variables...

	public String getKeyStorePath() {
		return keyStorePath;
	}
	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}
	
	public String getKeyStorePwd() {
		return keyStorePwd;
	}
	public void setKeyStorePwd(String keyStorePwd) {
		this.keyStorePwd = keyStorePwd;
	}
	
	public String getKeyPairEntry() {
		return keyPairEntry;
	}
	public void setKeyPairEntry(String keyPairEntry) {
		this.keyPairEntry = keyPairEntry;
	}
	
	public String getKeyPairPwd() {
		return keyPairPwd;
	}
	public void setKeyPairPwd(String keyPairPwd) {
		this.keyPairPwd = keyPairPwd;
	}
	
	public String[] getSslCipherSuites() {
		return sslCipherSuites;
	}
	public void setSslCipherSuites(String[] sslCipherSuites) {
		this.sslCipherSuites = sslCipherSuites;
	}
	
	public String[] getSslProtocols() {
		return sslProtocols;
	}
	public void setSslProtocols(String[] sslProtocols) {
		this.sslProtocols = sslProtocols;
	}
	
	public String getKeyManageFactoryAlg() {
		return keyManageFactoryAlg;
	}
	public void setKeyManageFactoryAlg(String keyManageFactoryAlg) {
		this.keyManageFactoryAlg = keyManageFactoryAlg;
	}
	
	public String getSecureSocketProtocol() {
		return secureSocketProtocol;
	}
	public void setSecureSocketProtocol(String secureSocketProtocol) {
		this.secureSocketProtocol = secureSocketProtocol;
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
	
	
	
}
