package TPM.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import json.TPMClientMsg;
import utils.KeyManager;
import utils.MyCache;
import utils.Utils;

public class GOSTPMServer {

	private static Gson gson = new GsonBuilder().create();

	private static MyCache cache;
	private static final int TIMETOEXPIRE = 10000;

	private static PublicKey aPub;
	private static int nonceC;
	private static SSLSocket c;


	private static BigInteger g512 = new BigInteger(
			"153d5d6172adb43045b68ae8e1de1070b6137005686d29d3d73a7"
					+ "749199681ee5b212c9b96bfdcfa5b20cd5e3fd2044895d609cf9b"
					+ "410b7a0f12ca1cb9a428cc", 16);

	private static BigInteger p512 = new BigInteger(
			"9494fec095f3b85ee286542b3836fc81a5dd0a0349b4c239dd387"
					+ "44d488cf8e31db8bcb7d33b41abb9e5a33cca9144b1cef332c94b"
					+ "f0573bf047a3aca98cdf3b", 16);

	private static PublicKey bPub;

	private static SecretKey key;

	public static void main(String[] args) {

		//		String ksName = args[0];    // serverkeystore
		//		char[]  ksPass = args[1].toCharArray();   // password da keystore
		//		char[]  ctPass = args[2].toCharArray();  // password entry
		//		int port= Integer.parseInt(args[3]);


		cache = new MyCache();

		SSLServerSocket s  = establishSecureConnection(4443);

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


	private static SSLServerSocket establishSecureConnection(int port) {

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

	private static boolean receiveSnapRequestDH() {
		try {

			ObjectInputStream r = new ObjectInputStream(c.getInputStream());			

			char attestRequestCode = r.readChar();
			nonceC = r.readInt();

			if(attestRequestCode != '0' || !cache.isValid( nonceC) ) 
				return false;

			cache.add( nonceC, TIMETOEXPIRE);			
			aPub = (PublicKey) r.readObject();
			//System.out.println(Utils.toHex(pubDH.getEncoded()));


		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return true;

	}

	private static void sendSnapshotDH() {
		try {

			//			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", "BC"); //TODO criar cada vez que se concta ou guardar numa keystore
			//			keyGen.initialize(dhParams, new SecureRandom());
			//
			//			aKeyAgree = KeyAgreement.getInstance("DH", "BC");
			//			KeyPair      aPair = keyGen.generateKeyPair();
			//
			//			aKeyAgree.init(aPair.getPrivate());

			ObjectOutputStream w = new ObjectOutputStream(c.getOutputStream());			

			generateKeyDH();
			w.writeChar('1');
			//sign(w);

			w.flush();
			//w.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void sign(ObjectOutputStream w) {
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream signStream = new ObjectOutputStream(out);
			signStream.writeInt(nonceC + 1);			
			signStream.writeObject(bPub);	
			
			byte[] plaintext = out.toByteArray();
			
			KeyPair keypair = KeyManager.getKeyPair("keypair", "srscsrsc", "GOSTPMKeyStore.jks", "srscsrsc");
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}

	private static void generateKeyDH() {

		DHParameterSpec dhParams = new DHParameterSpec(p512, g512);

		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance("DH", "BC");
			keyGen.initialize(dhParams, new SecureRandom());

			KeyAgreement bKeyAgree = KeyAgreement.getInstance("DH", "BC");
			KeyPair      bPair = keyGen.generateKeyPair();
			bPub = bPair.getPublic();

			bKeyAgree.init(bPair.getPrivate());
			bKeyAgree.doPhase(aPub, true);
			MessageDigest hash = MessageDigest.getInstance("SHA1", "BC");

			byte[] keyBytes = hash.digest(bKeyAgree.generateSecret());
			key = new SecretKeySpec(keyBytes, "AES");

			//System.out.println(Utils.toHex(key.getEncoded()));

		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | InvalidKeyException e) {
			e.printStackTrace();
		} 

	}




	//	ATTESTATION SIGNATURE: é uma assinatura digital cobrindo:
	//		• Um número público Diffie-Hellman gerado pelo módulo em causa para a resposta
	//		• A resposta o NONCE do cliente (exemplo, NONCE+1)


	private static void printSocketInfo(SSLSocket s) {
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

	private static void printServerSocketInfo(SSLServerSocket s) {
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
