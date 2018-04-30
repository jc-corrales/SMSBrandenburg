package source;

import java.io.IOException;
import java.net.Socket;
import java.security.cert.CertificateException;

import uniandes.gload.core.Task;

public class ClientServerTask extends Task
{
	@Override
	public void execute()
	{	
		try {
			Socket socket = new Socket(Principal.IP,Principal.PUERTO);
			Principal client = new Principal(socket);
			client.getProtocoloCliente().procesar();
			socket.close();
		} catch (IOException | CertificateException e) {
			fail();
//			e.printStackTrace();
		}
		
	}
	@Override
	public void fail()
	{
		System.out.println(Task.MENSAJE_FAIL);
	}
	@Override
	public void success()
	{
		System.out.println(Task.OK_MESSAGE);
	}
}
