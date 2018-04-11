package source;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

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
	public final static String ALGORITMOHMAC = "HMACMD5";
	private static Socket socket;
	private static byte[] certificado;
	
	private static PublicKey llaveServidor;
	
	private static ClaseSecretaAsimetrico claseSecretaAsimetrico;
	private static ClaseSecretaSimetrico claseSecretaSimetrico;
	private static GeneradorDeCertificados certificateGenerator;
	public ProtocoloCliente(Socket pSocket)
	{
//		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		socket = pSocket;
		claseSecretaAsimetrico = new ClaseSecretaAsimetrico(ALGORITMOASIMETRICO);
		claseSecretaSimetrico = new ClaseSecretaSimetrico(ALGORITMOSIMETRICO, "");
		certificateGenerator = new GeneradorDeCertificados(claseSecretaAsimetrico.getKeys());
	}

	public void procesar(//BufferedReader pIn,PrintWriter pOut
			) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		InputStream bytesInput = socket.getInputStream();
		PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
		OutputStream bytesoutput = socket.getOutputStream();
		String inputLine, outputLine;
		int estado = 0;
		System.out.println("PRE");
		output.println(HOLA);
		System.out.println("POST");
		while (estado < FINALSTATE && (inputLine = input.readLine()) != null) {
			System.out.println("INPUTLINE: " + inputLine);
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
					outputLine = mybyte.toString();
				} catch (CertificateEncodingException e1) {
					System.out.println("ERROR: " + e1.getMessage());
					outputLine = ERROR;
					estado = FINALSTATE;
					break;
				}
//				outputLine = mybyte.toString();
				estado++;
				break;
			case 2:
				String[] entrada2 = inputLine.split(":");
				if(!(entrada2[0].equals(ESTADO) && entrada2[1].equals(OK)))
				{
					outputLine = "ERROR-EsperabaHola";
					estado = 0;
					break;
				}
				else if(entrada2[0].equals(ESTADO) && entrada2[1].equals(ERROR))
				{
					outputLine = "ERROR-EsperabaHola";
					estado = FINALSTATE;
					break;
				}
				inputLine = input.readLine();
				if(!inputLine.equals(CERTSRV))
				{
					outputLine = "ERROR-EsperabaHola";
					estado = 0;
					break;
				}
				//TODO
				byte[] arreglo = null;
				bytesInput.read(arreglo);
				if(revisarCertificado(arreglo))
				{
					outputLine = ESTADO + ":" + OK;
				}
				else
				{
					outputLine = ESTADO + ":" + ERROR;
				}
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
					String[] datos = inputLine.split(":");
					byte[] LSbytes = claseSecretaAsimetrico.descifrar(datos[1].getBytes()).getBytes();
					Cipher obtenedorDeLlave = Cipher.getInstance(ALGORITMOSIMETRICO);
					SecretKey LS = (SecretKey) obtenedorDeLlave.unwrap(LSbytes, ALGORITMOSIMETRICO, Cipher.SECRET_KEY);
					String coordenadas = "41 24.2028, 2 10.4418";
					claseSecretaSimetrico.cifrar(coordenadas);
					//TODO ACTO 1
//					output.println("");
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
		output.close();
		input.close();
	}
	
	public static String obtenerAlgoritmos()
	{
		//TODO implementar método para obtener los algoritmos necesarios.
		String respuesta = ALGORITMOS + ":" + ALGORITMOSIMETRICO+ ":" + ALGORITMOASIMETRICO+ ":" + ALGORITMOHMAC;
		return respuesta;
	}
	
	public static X509Certificate generarCertificado()
	{
		X509Certificate respuesta = null;
		//TODO implementar método para obtener los algoritmos necesarios.
		try {
			respuesta = GeneradorDeCertificados.generateV3Certificate(claseSecretaAsimetrico.getKeys());
		} catch (InvalidKeyException | NoSuchProviderException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return respuesta;
	}	
	public static boolean revisarCertificado (byte[] certificateBytes)
	{
		boolean respuesta = false;
		//TODO implementar método para revisar la veracidad del certificado.
		try
		{
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			ByteArrayInputStream stream = new ByteArrayInputStream(certificateBytes);
			X509Certificate certificadoServidor = (X509Certificate) cf.generateCertificate(stream);
			certificadoServidor.verify(certificadoServidor.getPublicKey());
//			llaveServidor = certificadoServidor.getPublicKey();
//			byte[] firma = certificadoServidor.getSignature();
//			certificadoServidor.getSigAlgName();
//			ClaseSecretaAsimetrico.descifrarCertificado(firma, llaveServidor);
			respuesta = true;
		}catch (CertificateException e)
		{
			System.out.println(e.getMessage());
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println(e.getMessage());
		}
		catch (InvalidKeyException e)
		{
			System.out.println(e.getMessage());
		}
		catch (NoSuchProviderException e)
		{
			System.out.println(e.getMessage());
		}
		catch (SignatureException e)
		{
			System.out.println(e.getMessage());
		}
		return respuesta;
	}
	
	public static byte[] generarLlaveSimetrica()
	{
		//TODO implementar método para generar llave simétrica
		return "hola".getBytes();
	}
	
}
