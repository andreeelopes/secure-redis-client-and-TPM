package TPM.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.security.KeyStore;
import java.security.PublicKey;

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

	private static PublicKey pubDH;
	private static int nonceC;
	private static SSLSocket c;


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
				//sendSnapshot( GOSTPMResources.snapshot(), s );
				c.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private static boolean receiveSnapRequestDH() {
		try {

			ObjectInputStream w = new ObjectInputStream(c.getInputStream());			

			
			
			char attestRequestCode = '0';
			nonceC = w.readInt();

			if(attestRequestCode != '0' || !cache.isValid( nonceC) ) 
				return false;
			else {
				cache.add( nonceC, TIMETOEXPIRE);			
				pubDH = (PublicKey) w.readObject();
				System.out.println(Utils.toHex(pubDH.getEncoded()));
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return true;

	}

	private static void sendSnapshot(String snapshot, SSLServerSocket s) {

		try {
			SSLSocket c = (SSLSocket) s.accept();
			printSocketInfo(c);

			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
					c.getOutputStream()));

			w.write(snapshot,0,snapshot.length());
			w.flush();
			w.close();
			c.close();
		} catch (IOException e) {
			e.printStackTrace();
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
