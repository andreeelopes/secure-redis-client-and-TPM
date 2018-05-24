package json;

import java.security.PublicKey;

public class TPMClientMsg {
	public char attestRequestCode;
	public PublicKey pubDH;
	public int nonceC;
	
	public TPMClientMsg(char attestRequestCode, PublicKey pubDH, int nonceC) {
		this.attestRequestCode = attestRequestCode;
		this.pubDH = pubDH;
		this.nonceC = nonceC;
	}
	
	
	
}
