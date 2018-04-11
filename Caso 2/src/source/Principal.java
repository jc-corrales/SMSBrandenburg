package source;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;
import java.security.cert.CertificateException;

public class Principal {
//	public static final String PROVIDER= "BC";
	public static final Integer PUERTO = 8080;
	public static final String IP = "192.168.56.1";

	public static void main(String[] arg0) throws CertificateException, IOException
	{
//		ServerSocket ss = new ServerSocket(PUERTO);
		Socket socket = new Socket(IP,PUERTO);
		ProtocoloCliente protocolo = new ProtocoloCliente(socket);
		protocolo.procesar();
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
}
