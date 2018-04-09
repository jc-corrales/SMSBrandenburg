package source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class ProtocoloCliente
{
	public final static String OK = "OK";
	public final static String ERROR = "ERROR";
	public final static String ESTADO = "ESTADO";
	public final static String ALGORITMOS = "ALGORITMOS";
	public final static String HOLA = "HOLA";
	public final static String INICIO = "INICIO";
	public final static String CERTCLNT = "CERTCLNT";
	public final static String CERTSRV = "CERTSRV";
	public final static Integer FINALSTATE = 5;
	public final static String ALGORITMOSIMETRICO = "AES";
	public final static String ALGORITMOASIMETRICO = "RSA";
	public final static String ALGORITMOHMAC = "HmacMD5";
	private static Socket socket;
	private static byte[] certificado;
	
	private static PublicKey llaveServidor;
	
	private static ClaseSecretaAsimetrico claseSecretaAsimetrico;
	private static ClaseSecretaSimetrico claseSecretaSimetrico;
	private static GeneradorDeCertificados certificateGenerator;
	public ProtocoloCliente()
	{
		claseSecretaAsimetrico = new ClaseSecretaAsimetrico(ALGORITMOASIMETRICO);
		claseSecretaSimetrico = new ClaseSecretaSimetrico(ALGORITMOSIMETRICO, "");
		certificateGenerator = new GeneradorDeCertificados(claseSecretaAsimetrico.getKeys());
	}

	public static void procesar(//BufferedReader pIn,PrintWriter pOut
			) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
		String inputLine, outputLine;
		int estado = 0;
		output.println(HOLA);
		while (estado < FINALSTATE && (inputLine = input.readLine()) != null) {
			switch (estado) {
			case 0:
				if (!inputLine.equalsIgnoreCase(INICIO)) {
					outputLine = "ERROR-EsperabaHola";
					estado = 0;
					break;
				}
				String respuesta = obtenerAlgoritmos();
				outputLine = respuesta;
				estado++;
				break;
			case 1:
				String[] entrada1 = inputLine.split(":"); 
				if(!(entrada1[0].equals(ESTADO) &&entrada1[1].equals(OK)))
				{
					outputLine = "ERROR-EsperabaHola";
					estado = 0;
					break;
				}
				else if(entrada1[0].equals(ESTADO) &&entrada1[1].equals(ERROR))
				{
					outputLine = "ERROR-EsperabaHola";
					estado = 0;
					break;
				}
				output.println(CERTCLNT);
				X509Certificate certCliente = generarCertificado();
				if(certCliente == null)
				{
					outputLine = ERROR;
					estado = FINALSTATE;
					break;
				}
				byte[] mybyte;
				try {
					mybyte = certCliente.getEncoded();
				} catch (CertificateEncodingException e1) {
					System.out.println("ERROR: " + e1.getMessage());
					outputLine = ERROR;
					estado = FINALSTATE;
					break;
				}
				outputLine = mybyte.toString();
				estado++;
				break;
			case 2:
				String[] entrada2 = inputLine.split(":");
				if(!(entrada2[0].equals(ESTADO) &&entrada2[1].equals(OK)))
				{
					outputLine = "ERROR-EsperabaHola";
					estado = 0;
					break;
				}
				else if(entrada2[0].equals(ESTADO) && entrada2[1].equals(ERROR))
				{
					outputLine = "ERROR-EsperabaHola";
					estado = 0;
					break;
				}
				inputLine = input.readLine();
				if(!inputLine.equals(CERTSRV))
				{
					outputLine = "ERROR-EsperabaHola";
					estado = 0;
					break;
				}
//				inputLine = input.readLine();
//				byte[] entradaCertServidor = inputLine.getBytes();
				InputStream certInput = socket.getInputStream();
				revisarCertificado(certInput);
				
//				if(revisarCertificado(certificado))
//				{
//					outputLine = ESTADO + ":" + OK;
//				}
//				else
//				{
//					outputLine = ESTADO + ":" + ERROR;
//				}
				estado++;
				break;
			case 3:
				try
				{
					if(!inputLine.startsWith(INICIO))
					{
						outputLine = "ERROR-EsperabaHola";
						estado = 0;
						break;
					}
					//TODO ACTO 1
					output.println("");
					//TODO ACTO 2
					outputLine = "";
					estado++;
					break;
				}
				catch(Exception e)
				{
					outputLine = ERROR;
					estado = FINALSTATE;
				}
				break;
			case 4:
				try
				{
					String[] entrada3 = inputLine.split(":");
					if(!(entrada3[0].equals(ESTADO) && entrada3[1].equals(OK)))
					{
						outputLine = "ERROR-EsperabaHola";
						estado = 0;
						break;
					}
					outputLine = "";
					estado++;
					break;
				}
				catch(Exception e)
				{
					estado++;
					outputLine = ERROR;
					break;
				}
			default:
				outputLine = "ERROR";
				estado = 0;
				break;
			}
			output.println(outputLine);
			output.flush();
		}
	}
	
	public static String obtenerAlgoritmos()
	{
		//TODO implementar m�todo para obtener los algoritmos necesarios.
		String respuesta = ALGORITMOS + ":" + ALGORITMOSIMETRICO+ ":" + ALGORITMOASIMETRICO+ ":" + ALGORITMOHMAC;
		return respuesta;
	}
	
	public static X509Certificate generarCertificado()
	{
		X509Certificate respuesta = null;
		//TODO implementar m�todo para obtener los algoritmos necesarios.
		try {
			respuesta = GeneradorDeCertificados.generateV3Certificate(claseSecretaAsimetrico.getKeys());
		} catch (InvalidKeyException | NoSuchProviderException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return respuesta;
	}	
	public static boolean revisarCertificado (InputStream certificado)
	{
		//TODO implementar m�todo para revisar la veracidad del certificado.
		try
		{
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			X509Certificate certificadoServidor = (X509Certificate) cf.generateCertificate(certificado);
			llaveServidor = certificadoServidor.getPublicKey();
			byte[] firma = certificadoServidor.getSignature();
			certificadoServidor.getSigAlgName();
			claseSecretaAsimetrico.descifrarCertificado(firma, llaveServidor);
		}
		catch(Exception e)
		{
			System.out.println("Error: " + e.getMessage());
		}

		return true;
	}
	
	public static byte[] generarLlaveSimetrica()
	{
		//TODO implementar m�todo para generar llave sim�trica
		return "hola".getBytes();
	}
	
}
