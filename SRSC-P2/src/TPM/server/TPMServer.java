package TPM.server;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import utils.KeyManager;
import utils.MyCache;
import utils.Utils;


public abstract class TPMServer {

	private TPMServerConfig tpmConfig;
	private  AttestationCommand[] attestationCommands;

	private static final char ATTESTATION_REQUEST_CODE = '0';
	private static final char ATTESTATION_RESPONSE_CODE = '1';

	private MyCache cache;
	private static final int TIME_TO_EXPIRE = 10000;

	private SSLSocket c;
	private SSLServerSocket s;

	private SecretKey key;
	private PublicKey bPubNumber;
	private PublicKey aPubNumber;
	private int nonceC;

	private ObjectOutputStream w;
	private byte[] encryptedSnapBytes;


	private BigInteger g512 = new BigInteger(
			"153d5d6172adb43045b68ae8e1de1070b6137005686d29d3d73a7"
					+ "749199681ee5b212c9b96bfdcfa5b20cd5e3fd2044895d609cf9b"
					+ "410b7a0f12ca1cb9a428cc", 16);

	private BigInteger p512 = new BigInteger(
			"9494fec095f3b85ee286542b3836fc81a5dd0a0349b4c239dd387"
					+ "44d488cf8e31db8bcb7d33b41abb9e5a33cca9144b1cef332c94b"
					+ "f0573bf047a3aca98cdf3b", 16);


	public TPMServer(int port, String configFilePath, String attestCommandsPath) {
		System.out.println(">TPM server: initializing...");

		cache = new MyCache();
		tpmConfig = getConfiguration(configFilePath);
		attestationCommands = getAttestationCommands(attestCommandsPath);
		s = establishSecureConnection(port);
	}

	private TPMServerConfig getConfiguration(String fileName) {
		System.out.println(">TPM server: retrieving server configuration.");
		Gson gson = new Gson();
		JsonReader reader;
		TPMServerConfig tpmConfig = null;
		try {
			reader = new JsonReader(new FileReader(fileName));
			tpmConfig = gson.fromJson(reader, TPMServerConfig.class);
		} catch (FileNotFoundException e) {
			System.err.println(">TPM server: configuration file for TPM not found.");
			e.printStackTrace();
		}
		return tpmConfig;
	}

	protected AttestationCommand[] getAttestationCommands(String fileName) {
		System.out.println(">TPM server: retrieving attestation commands from configuration.");
		Gson gson = new Gson();
		JsonReader reader;
		AttestationCommand[] commands = null;
		try {
			reader = new JsonReader(new FileReader(fileName));
			commands = gson.fromJson(reader, AttestationCommand[].class);
		} catch (FileNotFoundException e) {
			System.err.println(">TPM server: commands for TPM not found.");
			e.printStackTrace();
		}
		return commands;
	}

	public byte[] getSnapshot() {
		byte [] attestations = null;
		MessageDigest hash;

		try {
			hash = MessageDigest.getInstance(tpmConfig.getHashAlg(), tpmConfig.getHashProvider());

			String output;

			for(int i = 0; i < attestationCommands.length; i++) {
				output = executeShellCommand(attestationCommands[i].getCommand(),
						attestationCommands[i].getColumns());
				hash.update(Utils.toByteArray(output));
			}

			attestations = hash.digest();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return attestations;
	}

	protected void initiateAttestationProtocol() {

		while(true) {
			try {
				System.out.println(">TPM server: waiting for requests.");

				c = (SSLSocket) s.accept();
				printSocketInfo(c);

				if( !receiveSnapRequestDH() ) {
					c.close();
					continue;
				}
				sendSnapshotDH();
				System.out.println(">TPM server: response sent.");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}


	private SSLServerSocket establishSecureConnection(int port) {

		String [] confciphersuites = tpmConfig.getSslCipherSuites();
		String [] confprotocols = tpmConfig.getSslProtocols();
		String pathToKeyStore = tpmConfig.getKeyStorePath();
		String keyStorePwd = tpmConfig.getKeyStorePwd();

		SSLServerSocket s = null;

		try {
			KeyStore ks = KeyManager.getOrCreateKeyStore(pathToKeyStore, keyStorePwd);

			KeyManagerFactory kmf = 
					KeyManagerFactory.getInstance(tpmConfig.getKeyManageFactoryAlg());
			kmf.init(ks, keyStorePwd.toCharArray());

			SSLContext sc = SSLContext.getInstance(tpmConfig.getSecureSocketProtocol());
			sc.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			s = (SSLServerSocket) ssf.createServerSocket(port);

			s.setEnabledProtocols(confprotocols);
			s.setEnabledCipherSuites(confciphersuites);

			printServerSocketInfo(s);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	private boolean receiveSnapRequestDH() {
		try {

			ObjectInputStream r = new ObjectInputStream(c.getInputStream());			

			char attestRequestCode = r.readChar();
			nonceC = r.readInt();

			if(attestRequestCode != ATTESTATION_REQUEST_CODE || !cache.isValid( nonceC) ) 
				return false;

			cache.add( nonceC, TIME_TO_EXPIRE);			
			aPubNumber = (PublicKey) r.readObject();

			System.out.println(">TPM server: processing request.");

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return true;

	}


	private void sendSnapshotDH() {
		try {

			w = new ObjectOutputStream(c.getOutputStream());			

			generateKeyDH();
			w.writeChar(ATTESTATION_RESPONSE_CODE);
			encryptSnapshot();
			signAndSend();
			w.flush();


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void encryptSnapshot() {

		try {

			System.out.println(">TPM server: encrypting snapshot...");

			byte[] snapshot = getSnapshot();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream snapStream = new ObjectOutputStream(out);

			snapStream.writeObject(snapshot);
			byte[] snapshotBytes = out.toByteArray();

			byte[]	ivBytes = 
					new byte[] { 0x08, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00 ,
							0x08, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00 
			}; 


			Cipher cipher = Cipher.getInstance(tpmConfig.getSymmAlg(), tpmConfig.getSymmProvider());
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivBytes));//TODO passar para a configuracao assim fica da responsabilidade do cliente
			encryptedSnapBytes = cipher.doFinal(snapshotBytes);

			System.out.println(">TPM server: snapshot encryption has finished.");

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
			signStream.writeObject(tpmConfig.getSymEncrypConfig());
			byte[] msg = out.toByteArray();

			KeyPair keypair = KeyManager.getKeyPair(tpmConfig.getKeyPairEntry(), tpmConfig.getKeyPairPwd(), 
					tpmConfig.getKeyStorePath(), tpmConfig.getKeyStorePwd());
			Signature signature = Signature.getInstance(tpmConfig.getSignatureAlg(), tpmConfig.getSignatureProvider());
			signature.initSign(keypair.getPrivate());


			//			System.out.println("Public Key(bytes) = " + Utils.toHex(keypair.getPublic().getEncoded()));

			signature.update(msg);
			byte[] signBytes = signature.sign();

			System.out.println(">TPM server: finish signing mensage.");

			w.writeInt(nonceC + 1);
			w.writeObject(bPubNumber);
			w.writeInt(encryptedSnapBytes.length);
			w.write(encryptedSnapBytes);
			w.writeInt(signBytes.length);
			w.write(signBytes);
			w.writeObject(tpmConfig.getSymEncrypConfig());

			//			System.out.println("---------------");
			//			System.out.println();
			//			System.out.println(nonceC + 1);
			//			System.out.println(Utils.toHex(bPubNumber.getEncoded()));
			//			System.out.println(encryptedSnapBytes.length);
			//			System.out.println(Utils.toHex(encryptedSnapBytes));
			//			System.out.println(signBytes.length);
			//			System.out.println(Utils.toHex(signBytes));
			//			System.out.println();
			//			System.out.println("---------------");

		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
			e.printStackTrace();
		}			
	}

	private void generateKeyDH() {

		DHParameterSpec dhParams = new DHParameterSpec(p512, g512);

		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance(tpmConfig.getDhAlg(), tpmConfig.getDhProvider());
			keyGen.initialize(dhParams, new SecureRandom());

			KeyAgreement bKeyAgree = KeyAgreement.getInstance(tpmConfig.getDhAlg(), tpmConfig.getDhProvider());
			KeyPair      bPair = keyGen.generateKeyPair();
			bPubNumber = bPair.getPublic();

			bKeyAgree.init(bPair.getPrivate());
			bKeyAgree.doPhase(aPubNumber, true);
			MessageDigest hash = MessageDigest.getInstance(tpmConfig.getHashAlg(), tpmConfig.getHashProvider());

			byte[] keyBytes = hash.digest(bKeyAgree.generateSecret());
			key = new SecretKeySpec(keyBytes, tpmConfig.getSymmAlg());

			System.out.println(">TPM server: simmetric encryption key created based on DH.");

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

		//System.out.println(">TPM server: output from " + command + " : " + output + "\n");
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
