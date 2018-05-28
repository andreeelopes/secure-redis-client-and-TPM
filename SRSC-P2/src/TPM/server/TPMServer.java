package TPM.server;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import utils.KeyManager;
import utils.MyCache;
import utils.Utils;

public abstract class TPMServer {

	private MyCache cache;
	private final int TIMETOEXPIRE = 10000;

	private PublicKey aPubNumber;
	private int nonceC;
	private SSLSocket c;
	private SSLServerSocket s;

	private BigInteger g512 = new BigInteger(
			"153d5d6172adb43045b68ae8e1de1070b6137005686d29d3d73a7"
					+ "749199681ee5b212c9b96bfdcfa5b20cd5e3fd2044895d609cf9b"
					+ "410b7a0f12ca1cb9a428cc", 16);

	private BigInteger p512 = new BigInteger(
			"9494fec095f3b85ee286542b3836fc81a5dd0a0349b4c239dd387"
					+ "44d488cf8e31db8bcb7d33b41abb9e5a33cca9144b1cef332c94b"
					+ "f0573bf047a3aca98cdf3b", 16);

	private PublicKey bPubNumber;

	private SecretKey key;

	private ObjectOutputStream w;
	private byte[] encryptedSnapBytes;


	public TPMServer(int port) {
		cache = new MyCache();
		s  = establishSecureConnection(port);
	}

	public void initiateAttestationProtocol() {

		while(true) {
			try {
				c = (SSLSocket) s.accept();
				printSocketInfo(c);

				if( !receiveSnapRequestDH() ) {
					c.close();
					continue;
				}
				sendSnapshotDH();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}


	private SSLServerSocket establishSecureConnection(int port) {

		String[] confciphersuites={"TLS_RSA_WITH_AES_256_CBC_SHA256"};
		String[] confprotocols={"TLSv1.2"};

		SSLServerSocket s = null;

		try {
			KeyStore ks = KeyManager.getOrCreateKeyStore("GOSTPMKeyStore.jks", "srscsrsc");

			KeyManagerFactory kmf = 
					KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, "srscsrsc".toCharArray());

			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			s = (SSLServerSocket) ssf.createServerSocket(port);

			s.setEnabledProtocols(confprotocols);
			s.setEnabledCipherSuites(confciphersuites);

			printServerSocketInfo(s);

		} catch (Exception e) {
			System.err.println(e.toString());
		}
		return s;
	}

	private boolean receiveSnapRequestDH() {
		try {

			ObjectInputStream r = new ObjectInputStream(c.getInputStream());			

			char attestRequestCode = r.readChar();
			nonceC = r.readInt();

			if(attestRequestCode != '0' || !cache.isValid( nonceC) ) 
				return false;

			cache.add( nonceC, TIMETOEXPIRE);			
			aPubNumber = (PublicKey) r.readObject();


		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return true;

	}

	private void sendSnapshotDH() {
		try {

			w = new ObjectOutputStream(c.getOutputStream());			

			generateKeyDH();
			w.writeChar('1');
			encryptSnapshot();
			signAndSend();
			//snapshot();
			w.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	abstract byte[] getSnapshot();

	private void encryptSnapshot() {

		try {

			byte[] snapshot = getSnapshot();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream snapStream = new ObjectOutputStream(out);

			snapStream.writeObject(snapshot);
			byte[] snapshotBytes = out.toByteArray();

			byte[]	ivBytes = 
					new byte[] { 0x08, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00 ,
							0x08, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00 
			}; 


			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivBytes));
			encryptedSnapBytes = cipher.doFinal(snapshotBytes);


		} catch (IOException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | 
				NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

	}

	private void signAndSend() {

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream signStream = new ObjectOutputStream(out);
			signStream.writeInt(nonceC + 1);			
			signStream.writeObject(bPubNumber);	
			signStream.writeInt(encryptedSnapBytes.length);
			signStream.write(encryptedSnapBytes);
			byte[] msg = out.toByteArray();

			KeyPair keypair = KeyManager.getKeyPair("keypair", "srscsrsc", "GOSTPMKeyStore.jks", "srscsrsc");
			Signature signature = Signature.getInstance("SHA256withRSA", "BC");
			signature.initSign(keypair.getPrivate());

			signature.update(msg);
			byte[] signBytes = signature.sign();

			w.writeInt(nonceC + 1);
			w.writeObject(bPubNumber);
			w.writeInt(encryptedSnapBytes.length);
			w.write(encryptedSnapBytes);
			w.writeInt(signBytes.length);
			w.write(signBytes);

			//			System.out.println(nonceC + 1);
			//			System.out.println(Utils.toHex(bPubNumber.getEncoded()));
			//			System.out.println(encryptedSnapBytes.length);
			//			System.out.println(Utils.toHex(encryptedSnapBytes));
			//			System.out.println(signBytes.length);
			//			System.out.println(Utils.toHex(signBytes));

		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
			e.printStackTrace();
		}			
	}

	private void generateKeyDH() {

		DHParameterSpec dhParams = new DHParameterSpec(p512, g512);

		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance("DH", "BC");
			keyGen.initialize(dhParams, new SecureRandom());

			KeyAgreement bKeyAgree = KeyAgreement.getInstance("DH", "BC");
			KeyPair      bPair = keyGen.generateKeyPair();
			bPubNumber = bPair.getPublic();

			bKeyAgree.init(bPair.getPrivate());
			bKeyAgree.doPhase(aPubNumber, true);
			MessageDigest hash = MessageDigest.getInstance("SHA256", "BC");

			byte[] keyBytes = hash.digest(bKeyAgree.generateSecret());
			key = new SecretKeySpec(keyBytes, "AES");

		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | InvalidKeyException e) {
			e.printStackTrace();
		} 

	}
	
	
	/**
	 * 
	 * @param command - the shell command to be executed
	 * @param filterColumns - the index of the columns that you want to output
	 * @return a string with the output
	 * @throws Exception
	 */
	protected static String executeShellCommand(String command, int[] filterColumns) throws Exception {

		Process process = Runtime.getRuntime().exec(command);

		BufferedReader r = 
				new BufferedReader(new InputStreamReader(process.getInputStream()));

		String output = "";
		String line = "";	
		String [] splitedRes;
		while ((line = r.readLine()) != null) {
			line = line.replaceAll("\\s+", " ");//regex to remove whitespace duplicates
			line = line.trim();
			splitedRes = line.split(" ");
			if(filterColumns != null)//if has filter on columns
				for (int column : filterColumns) {
					output += splitedRes[column];
				}
			else//if no filters on columns
				output += line;
		}

		System.out.println("Output from " + command + " : " + output + "\n");
		return output;
	}


	private void printSocketInfo(SSLSocket s) {
		System.out.println("Socket class: "+s.getClass());
		System.out.println("   Remote address = "
				+s.getInetAddress().toString());
		System.out.println("   Remote port = "+s.getPort());
		System.out.println("   Local socket address = "				+s.getLocalSocketAddress().toString());
		System.out.println("   Local address = "
				+s.getLocalAddress().toString());
		System.out.println("   Local port = "+s.getLocalPort());
		System.out.println("   Need client authentication = "
				+s.getNeedClientAuth());
		SSLSession ss = s.getSession();
		System.out.println("   Cipher suite = "+ss.getCipherSuite());
		System.out.println("   Protocol = "+ss.getProtocol());
	}

	private void printServerSocketInfo(SSLServerSocket s) {
		System.out.println("Server socket class: "+s.getClass());
		System.out.println("   Socker address = "
				+s.getInetAddress().toString());
		System.out.println("   Socker port = "
				+s.getLocalPort());
		System.out.println("   Need client authentication = "
				+s.getNeedClientAuth());
		System.out.println("   Want client authentication = "
				+s.getWantClientAuth());
		System.out.println("   Use client mode = "
				+s.getUseClientMode());
	} 

}
