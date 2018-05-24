package TPM.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import json.TPMClientMsg;
import utils.MyCache;
import utils.Utils;

public class TPMClient {

	private String oldSnapshotGOSTPM = "";
	private String oldSnapshotVMSTPM = "";

	private MyCache cache;
	private final int TIMETOEXPIRE = 10000;
	
	private int nonceC;
	
	private KeyPair aPair;
	private SecretKeySpec key;
	private PublicKey bPub;

	private Gson gson = new GsonBuilder().create();

	private SSLSocket c;

	private BigInteger g512 = new BigInteger(
			"153d5d6172adb43045b68ae8e1de1070b6137005686d29d3d73a7"
					+ "749199681ee5b212c9b96bfdcfa5b20cd5e3fd2044895d609cf9b"
					+ "410b7a0f12ca1cb9a428cc", 16);

	private BigInteger p512 = new BigInteger(
			"9494fec095f3b85ee286542b3836fc81a5dd0a0349b4c239dd387"
					+ "44d488cf8e31db8bcb7d33b41abb9e5a33cca9144b1cef332c94b"
					+ "f0573bf047a3aca98cdf3b", 16);


	public TPMClient() {
		cache = new MyCache();

		//		if(!cache.isValid( nOnceHeader))
		//			return false;
		//		else 
		//			cache.add( nOnceHeader, TIMETOEXPIRE);

	}

	public boolean atest(String ipGOSTPM, int portGOSTPM, String ipVMSTPM, int portVMSTPM) {

		String snapshotGOSTPM = getSnapshot(ipGOSTPM, portGOSTPM);
		//String snapshotVMSTPM = getSnapshot(ipVMSTPM, portVMSTPM);

		//	System.out.println("\n\n\n\n\nxxxxxxxx" + snapshotGOSTPM) ;

		return atestGOSTPM(snapshotGOSTPM); //&& atestVMSTPM(snapshotVMSTPM);
	}

	private boolean atestVMSTPM(String snapshotVMSTPM) {
		if(oldSnapshotVMSTPM.equals("") )
			oldSnapshotVMSTPM = snapshotVMSTPM;

		return oldSnapshotVMSTPM.equals(snapshotVMSTPM);
	}

	private boolean atestGOSTPM(String snapshotGOSTPM) {
		if(oldSnapshotGOSTPM.equals("") )
			oldSnapshotGOSTPM = snapshotGOSTPM;

		return oldSnapshotGOSTPM.equals(snapshotGOSTPM);
	}

	private String getSnapshot(String ip, int port) {

		String snapshot = "";

		try {

			SSLSocketFactory f = 
					(SSLSocketFactory) SSLSocketFactory.getDefault();

			c =	(SSLSocket) f.createSocket(ip, port);

			printSocketInfo(c);

			c.startHandshake();

			requestSnapshotDH();
			snapshot = receiveSnapshotDH(); //e depois comparar

			//			BufferedReader r = new BufferedReader(
			//					new InputStreamReader(c.getInputStream()));
			//			String m;
			//			while ((m  = r.readLine()) != null) 
			//				snapshot += m;

			//			r.close();
			//System.out.println("close socket");
			//c.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}

		return snapshot;
	}

	private void requestSnapshotDH() {

		try {
			DHParameterSpec dhParams = new DHParameterSpec(p512, g512);
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", "BC"); //TODO criar cada vez que se concta ou guardar numa keystore
			keyGen.initialize(dhParams, new SecureRandom());
			aPair = keyGen.generateKeyPair();
			
			ObjectOutputStream w = new ObjectOutputStream(c.getOutputStream());

			//System.out.println(Utils.toHex(aPair.getPublic().getEncoded()));

			w.writeChar('0');
			w.writeInt( nonceC = new SecureRandom().nextInt());
			w.writeObject(aPair.getPublic());

			w.flush();
			//w.close();

		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException | IOException e1) {
			e1.printStackTrace();
		} 
	}

	private String receiveSnapshotDH() {

		try {
			ObjectInputStream r = new ObjectInputStream(c.getInputStream());


			char attestRequestCode = r.readChar();
			int nonceS = r.readInt();

			if(attestRequestCode != '1' || nonceS != nonceC + 1 || !cache.isValid( nonceS ) ) 
				return null;
			
			cache.add( nonceS, TIMETOEXPIRE);			
				
			bPub = (PublicKey) r.readObject();
			generateKeyDH();

			
			r.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return "lala";
	}

	private void generateKeyDH() {
	
		try {
			KeyAgreement aKeyAgree = KeyAgreement.getInstance("DH", "BC");
			aKeyAgree.init(aPair.getPrivate());
			aKeyAgree.doPhase(bPub, true);
			MessageDigest hash = MessageDigest.getInstance("SHA1", "BC");
			
			byte[] keyBytes = hash.digest(aKeyAgree.generateSecret());
			key = new SecretKeySpec(keyBytes, "AES");
			//System.out.println(Utils.toHex(key.getEncoded()));

		} catch (InvalidKeyException | IllegalStateException | NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
		}
        		
	}

	private void printSocketInfo(SSLSocket s) {

		System.out.println("\n------------------------------------------------------\n");
		System.out.println("Socket class: "+s.getClass());
		System.out.println("   Remote address = "
				+s.getInetAddress().toString());
		System.out.println("   Remote port = "+s.getPort());
		System.out.println("   Local socket address = "
				+s.getLocalSocketAddress().toString());
		System.out.println("   Local address = "
				+s.getLocalAddress().toString());
		System.out.println("   Local port = "+s.getLocalPort());
		System.out.println("   Need client authentication = "
				+s.getNeedClientAuth());
		System.out.println("   Client mode = "
				+s.getUseClientMode());
		System.out.println("\n------------------------------------------------------\n");

		System.out.println("   Enabled Protocols = "
				+Arrays.asList(s.getEnabledProtocols()));
		System.out.println("\n------------------------------------------------------\n");

		System.out.println("   Client Supprted Ciphersuites = "
				+Arrays.asList(s.getSupportedCipherSuites()));
		System.out.println("\n------------------------------------------------------\n");
		System.out.println("   Enabled Ciphersuites = "
				+Arrays.asList(s.getEnabledCipherSuites()));

		System.out.println("\n------------------------------------------------------\n");

		SSLSession ss = s.getSession();


		System.out.println("   Peer Host = "+ss.getPeerHost());
		System.out.println("   Peer Port = "+ss.getPeerPort());

		System.out.println("   Protocol = "+ss.getProtocol());
		System.out.println("   Cipher suite = "+ss.getCipherSuite());

		System.out.println("   Packet Buffer Size = "+ss.getPacketBufferSize());

		System.out.println("\n------------------------------------------------------\n");

	}

}
