package source;

import java.io.IOException;
import java.net.Socket;
import java.security.cert.CertificateException;

public class Principal {
	public static final boolean SEGURIDAD = false; 
	public static final String PROVIDER= "BC";
	public static final Integer PUERTO = 8080;
//	public static final String IP = "192.168.1.103";
	public static final String IP = "192.168.1.101";
//	public static final String IP = "157.253.202.15";
//	public static final String IP = "157.253.202.33";
	private static ProtocoloCliente protocolo;
	private static ProtocoloClienteSinSeguridad protocoloSS;

	public static void main(String[] arg0) throws CertificateException, IOException
	{
//		certificateGenerator.getCertificate();
//		ServerSocket ss = new ServerSocket(PUERTO);
		if(SEGURIDAD)
		{
//			for(int i = 0; i < 30; i++)
//			{
				Socket socket = new Socket(IP,PUERTO);
				protocolo = new ProtocoloCliente(socket);
				protocolo.procesar();
				socket.close();
//			}
			Registrador.writeRegister();
		}
		else
		{
			for(int i = 0; i < 30; i++)
			{
				Socket socket = new Socket(IP,PUERTO);
				protocoloSS = new ProtocoloClienteSinSeguridad(socket);
				protocoloSS.procesar();
				socket.close();
			}
			Registrador.writeRegister();
		}
//		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	public Principal(Socket socket) throws CertificateException, IOException
	{
			protocolo = new ProtocoloCliente(socket);
	}
	/**
	 * Método que obtiene el protocoloCliente de este Cliente.
	 * @return
	 */
	public ProtocoloCliente getProtocoloCliente()
	{
		return protocolo;
	}
}
