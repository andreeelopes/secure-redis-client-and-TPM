package TPM.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Arrays;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TPMClient {

	private static String oldSnapshotGOSTPM = "";
	private static String oldSnapshotVMSTPM = "";

	public static boolean atest(String ipGOSTPM, int portGOSTPM, String ipVMSTPM, int portVMSTPM) {

		String snapshotGOSTPM = getSnapshot(ipGOSTPM, portGOSTPM);
		//String snapshotVMSTPM = getSnapshot(ipVMSTPM, portVMSTPM);

		System.out.println("\n\n\n\n\nxxxxxxxx" + snapshotGOSTPM) ;
		
		return atestGOSTPM(snapshotGOSTPM); //&& atestVMSTPM(snapshotVMSTPM);
	}

	private static boolean atestVMSTPM(String snapshotVMSTPM) {
		if(oldSnapshotVMSTPM.equals("") )
			oldSnapshotVMSTPM = snapshotVMSTPM;

		return oldSnapshotVMSTPM.equals(snapshotVMSTPM);
	}

	private static boolean atestGOSTPM(String snapshotGOSTPM) {
		if(oldSnapshotGOSTPM.equals("") )
			oldSnapshotGOSTPM = snapshotGOSTPM;

		return oldSnapshotGOSTPM.equals(snapshotGOSTPM);
	}

	private static String getSnapshot(String ip, int port) {

		SSLSocket c = null;
		String snapshot = "";

		try {

			SSLSocketFactory f = 
					(SSLSocketFactory) SSLSocketFactory.getDefault();

			c =	(SSLSocket) f.createSocket(ip, port);

			printSocketInfo(c);

			c.startHandshake();

			BufferedReader r = new BufferedReader(
					new InputStreamReader(c.getInputStream()));
			String m;
			while ((m  = r.readLine()) != null) 
				snapshot += m;

			r.close();
			c.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}

		return snapshot;
	}

	private static void printSocketInfo(SSLSocket s) {

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
