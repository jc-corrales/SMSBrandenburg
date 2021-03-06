package source;

import java.io.IOException;
import java.net.Socket;
import java.security.cert.CertificateException;

import uniandes.gload.core.Task;

public class ClientServerTask extends Task
{
	private int contadorA = 0;
	private int contadorB = 0;
	@Override
	public void execute()
	{	
		System.out.println("Thread " + contadorA);
		contadorA++;
		try {
			Socket socket = new Socket(Principal.IP,Principal.PUERTO);
			Principal client = new Principal(socket);
			if(Principal.SEGURIDAD)
			{
				client.getProtocoloCliente().procesar();
			}
			else
			{
				client.getProtocoloClienteSinSeguridad().procesar();
			}
			socket.close();
			success();
		} catch (IOException | CertificateException e) {
			fail();
//			e.printStackTrace();
		}
		
	}
	@Override
	public void fail()
	{
		contadorB++;
		System.out.println(Task.MENSAJE_FAIL);
	}
	@Override
	public void success()
	{
		contadorB++;
		System.out.println(Task.OK_MESSAGE);
	}
}
