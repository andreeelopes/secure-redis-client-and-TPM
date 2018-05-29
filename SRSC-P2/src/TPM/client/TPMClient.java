package TPM.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.Arrays;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.rmi.CORBA.Util;

import utils.FileHelper;
import utils.KeyManager;
import utils.MyCache;
import utils.Utils;

public class TPMClient {

	private byte[] oldSnapshotGOSTPM;
	private byte[] oldSnapshotVMSTPM;

	private MyCache cache;
	private final int TIMETOEXPIRE = 10000;

	private int nonceC;

	private KeyPair aPair;
	private SecretKeySpec key;
	private PublicKey bPubNumber;

	private ObjectInputStream r;
	private ObjectOutputStream w;

	private SSLSocket c;

	private static final String GOS_SNAPSHOT_FILE_PATH = "gosSnapshot";
	private static final String VMS_SNAPSHOT_FILE_PATH = "vmsSnapshot";


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
		oldSnapshotGOSTPM = FileHelper.ToBytes(GOS_SNAPSHOT_FILE_PATH);
		oldSnapshotVMSTPM = FileHelper.ToBytes(VMS_SNAPSHOT_FILE_PATH);
	}


	public boolean attest(String ipGOSTPM, int portGOSTPM, String ipVMSTPM, int portVMSTPM,
			String pathtoTrustStore, String trustStorePwd,
			String gosCertName, String vmsCertName) {

		//byte[] snapshotGOSTPM = getSnapshot(ipGOSTPM, portGOSTPM, pathtoTrustStore, trustStorePwd, gosCertName);
		byte[] snapshotVMSTPM = getSnapshot(ipVMSTPM, portVMSTPM, pathtoTrustStore, trustStorePwd, vmsCertName);

		return //attestTPM(snapshotGOSTPM, oldSnapshotGOSTPM, GOS_SNAPSHOT_FILE_PATH) && 
				attestTPM(snapshotVMSTPM, oldSnapshotVMSTPM, VMS_SNAPSHOT_FILE_PATH); 
	}

	private boolean attestTPM(byte[] snapshot, byte[] oldSnapshot, String pathName ) {


		if(snapshot == null)
			return false;

		if(oldSnapshot == null) {//first time attesting
			oldSnapshot = snapshot;
			FileHelper.ToFile(oldSnapshot, pathName);//update file

			return true;
		}

		return Arrays.equals(oldSnapshot, snapshot);
	}

	private byte[] getSnapshot(String ip, int port, String pathtoTrustStore, String trustStorePwd, String certName) {

		byte[] snapshot = null;

		try {

			SSLSocketFactory f = 
					(SSLSocketFactory) SSLSocketFactory.getDefault();

			c =	(SSLSocket) f.createSocket(ip, port);
			printSocketInfo(c);

			c.startHandshake();

			requestSnapshotDH();
			snapshot = receiveSnapshotDH(pathtoTrustStore, trustStorePwd, certName); 


			w.close();
			c.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return snapshot;
	}

	private void requestSnapshotDH() {

		try {
			DHParameterSpec dhParams = new DHParameterSpec(p512, g512);
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", "BC"); 
			keyGen.initialize(dhParams, new SecureRandom());
			aPair = keyGen.generateKeyPair();

			w = new ObjectOutputStream(c.getOutputStream());

			w.writeChar('0');
			w.writeInt( nonceC = new SecureRandom().nextInt());
			w.writeObject(aPair.getPublic());

			w.flush();

		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException | IOException e1) {
			e1.printStackTrace();
		} 
	}

	private byte[] receiveSnapshotDH(String pathtoTrustStore, String trustStorePwd, String certName) {

		byte[] snapshot = null;
		try {
			r = new ObjectInputStream(c.getInputStream());

			char attestRequestCode = r.readChar();
			int nonceS = r.readInt();

			if(attestRequestCode != '1' || nonceS != nonceC + 1 || !cache.isValid( nonceS ) ) 
				return null;

			cache.add( nonceS, TIMETOEXPIRE);

			bPubNumber = (PublicKey) r.readObject();

			byte[] encryptedSnapBytes = new byte[r.readInt()];
			r.read(encryptedSnapBytes);

			byte [] signBytes = new byte[r.readInt()];
			r.read(signBytes);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream signStream = new ObjectOutputStream(out);
			signStream.writeInt(nonceS);			
			signStream.writeObject(bPubNumber);	
			signStream.writeInt(encryptedSnapBytes.length);
			signStream.write(encryptedSnapBytes);
			
			System.out.println("---------------");
			System.out.println();
			System.out.println(nonceS);
			System.out.println(Utils.toHex(bPubNumber.getEncoded()));
			System.out.println(encryptedSnapBytes.length);
			System.out.println(Utils.toHex(encryptedSnapBytes));
			System.out.println(signBytes.length);
			System.out.println(Utils.toHex(signBytes));
			System.out.println();
			System.out.println("---------------");

			byte[] msg = out.toByteArray();

			if(!verifySignature( signBytes, msg, pathtoTrustStore, trustStorePwd, certName ))
				return null;

			generateKeyDH();

			snapshot = decryptSnapshot( encryptedSnapBytes );

			r.close();

		} catch (IOException | ClassNotFoundException e ) {
			e.printStackTrace();
		}
		return snapshot;
	}

	private byte[] decryptSnapshot(byte[] encryptedSnapBytes) {

		byte[] snapshot = null;
		try {

			byte[]	ivBytes = 
					new byte[] { 0x08, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00 ,
							0x08, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00 
			}; 

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));
			byte text[] = cipher.doFinal(encryptedSnapBytes);

			ByteArrayInputStream in = new ByteArrayInputStream(text);
			ObjectInputStream snapStream = new ObjectInputStream(in);

			snapshot = (byte[]) snapStream.readObject();

		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | IllegalBlockSizeException |
				BadPaddingException | InvalidKeyException | IOException | ClassNotFoundException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		return snapshot;
	}

	private boolean verifySignature(byte[] signBytes, byte[] msg, String pathtoTrustStore, String trustStorePwd, String certName) {

		boolean verification = false;
		try {

			KeyStore keyStore = KeyManager.getOrCreateKeyStore(pathtoTrustStore, trustStorePwd);
			Certificate cert = keyStore.getCertificate(certName);
			PublicKey publicKey = cert.getPublicKey();
			
			System.out.println("BLABLABLA = " + Utils.toHex(publicKey.getEncoded()));
			
			Signature signature = Signature.getInstance("SHA256withRSA", "BC");

			signature.initVerify(publicKey);
			signature.update(msg);

			verification = signature.verify(signBytes);
			System.out.println("Is Signature Valid? " + verification);


		} catch (KeyStoreException | SignatureException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
			e.printStackTrace();
		}

		return verification;

	}

	private void generateKeyDH() {

		try {
			KeyAgreement aKeyAgree = KeyAgreement.getInstance("DH", "BC");
			aKeyAgree.init(aPair.getPrivate());
			aKeyAgree.doPhase(bPubNumber, true);
			MessageDigest hash = MessageDigest.getInstance("SHA256", "BC");

			byte[] keyBytes = hash.digest(aKeyAgree.generateSecret());
			key = new SecretKeySpec(keyBytes, "AES");

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
