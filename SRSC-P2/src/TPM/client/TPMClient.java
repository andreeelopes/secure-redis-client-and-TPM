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

	public static void main(String[] args) {

		BufferedReader in = new BufferedReader(
				new InputStreamReader(System.in));
		PrintStream out = System.out;

		SSLSocketFactory f = 
				(SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			SSLSocket c =
					(SSLSocket) f.createSocket("localhost", 4443);

			printSocketInfo(c);


			c.startHandshake();
			BufferedWriter w = new BufferedWriter(
					new OutputStreamWriter(c.getOutputStream()));
			BufferedReader r = new BufferedReader(
					new InputStreamReader(c.getInputStream()));
			String m = null;
			while ((m=r.readLine())!= null) {
				out.println(m);
				m = in.readLine();
				w.write(m,0,m.length());
				w.newLine();
				w.flush();
			}
			w.close();
			r.close();
			c.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}

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
