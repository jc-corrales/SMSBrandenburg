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
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Hex;

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
	public final static Integer FINALSTATE = 6;
	public final static String ALGORITMOSIMETRICO = "AES";
	public final static String ALGORITMOASIMETRICO = "RSA";
	public final static String ALGORITMOHMAC = "HMACMD5";
	private static Socket socket;
	private X509Certificate certCliente;
	
	private static PublicKey llaveServidor;
	
	private static ClaseSecretaAsimetrico claseSecretaAsimetrico;
	private static ClaseSecretaSimetrico claseSecretaSimetrico;
	private static GeneradorDeCertificados certificateGenerator;
	public ProtocoloCliente(Socket pSocket)
	{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		socket = pSocket;
		claseSecretaAsimetrico = new ClaseSecretaAsimetrico(ALGORITMOASIMETRICO);
		claseSecretaSimetrico = new ClaseSecretaSimetrico(ALGORITMOSIMETRICO, "");
		certificateGenerator = new GeneradorDeCertificados(claseSecretaAsimetrico.getKeys());
		certCliente = certificateGenerator.getCertificate();
	}

	public void procesar(//BufferedReader pIn,PrintWriter pOut
			) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		InputStream bytesInput = socket.getInputStream();
		PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
		OutputStream bytesOutput = socket.getOutputStream();
		String inputLine, outputLine;
		int estado = 0;
		System.out.println("PRE");
		output.println(HOLA);
		System.out.println("POST");
		while (estado < FINALSTATE && (inputLine = input.readLine()) != null)
		{
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
				
//				System.out.println("Certificado Cliente: " + certCliente);
//				if(certCliente == null)
//				{
//					outputLine = ERROR;
//					estado = FINALSTATE;
//					break;
//				}
				byte[] mybyte;
				try {
					mybyte = certCliente.getEncoded();
					bytesOutput.write(mybyte);
					bytesOutput.flush();
					outputLine = "";
				} catch (CertificateEncodingException e1) {
					System.out.println("ERROR: " + e1.getMessage());
					outputLine = ERROR;
					estado = FINALSTATE;
					break;
				}
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
				outputLine = "";
				estado++;
				break;
			case 3:
				if(!inputLine.equals(CERTSRV))
				{
					outputLine = "ERROR-EsperabaHola";
					estado = 0;
					break;
				}
				System.out.println("PRE VERIFICACI�N");
				if(revisarCertificado(bytesInput))
				{
					System.out.println("Entro");
					outputLine = ESTADO + ":" + OK;
					System.out.println("salio");
				}
				else
				{
					outputLine = ESTADO + ":" + ERROR;
				}
				estado++;
				break;
			case 4:
				try
				{
					if(!inputLine.startsWith(INICIO))
					{
						outputLine = "ERROR-EsperabaHola";
						estado = 0;
						break;
					}
					String[] datos = inputLine.split(":");
					//INICIO ACT1
					byte[] hexString = Hex.decode(datos[1]);
					byte[] LSbytes = claseSecretaAsimetrico.descifrar(hexString).getBytes();
					SecretKey LS = new SecretKeySpec(LSbytes, 0, LSbytes.length, ALGORITMOSIMETRICO);
					System.out.println("Llave sim�trica: " + LS.toString());
//					String coordenadas = "40.7127837,74.00594130000002";
//					String coordenadas = "41 24.2028, 2 10.4418";
					String coordenadas = obtenerCoordenadas();
					byte[] respuesta1 = claseSecretaSimetrico.cifrar(coordenadas, LS);
					System.out.println(respuesta1);
					byte[] respuesta1PostHexadecimal = Hex.encode(respuesta1);
					System.out.println(new String (respuesta1));
					System.out.println(new String (respuesta1PostHexadecimal));
					outputLine = "ACT1:" + new String(respuesta1PostHexadecimal);
					output.flush();
//					outputLine = respuesta12.toUpperCase();
					System.out.println(outputLine);
					output.println(outputLine);
					//FIN ACT1
					//INICIO ACT2
					Hash hashObject = new Hash();
					byte[] hasheadoBytes = hashObject.calcular(coordenadas);
					String hasheado = new String(hasheadoBytes);
					byte[] respuesta2 = claseSecretaAsimetrico.cifrar(hasheado, llaveServidor);
					byte[] respuesta2PostExadecimal = Hex.encode(respuesta2);
					outputLine = "ACT2:" + new String(respuesta2PostExadecimal);
					//FIN ACT2
					estado++;
					break;
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.out.println("ERROR: "+e.getMessage());
					outputLine = ERROR;
					estado = FINALSTATE;
				}
				break;
			case 5:
				try
				{
					String[] entrada3 = inputLine.split(":");
					if(!(entrada3[0].equals(ESTADO) && entrada3[1].equals(OK)))
					{
						outputLine = "ERROR-EsperabaHola";
						System.out.println(ERROR + ": fall� confirmaci�n final.");
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
			if(!outputLine.equals(""))
			{
				output.println(outputLine);
				output.flush();
			}
		}
		output.close();
		input.close();
	}
	
	public static String obtenerAlgoritmos()
	{
		//TODO implementar m�todo para obtener los algoritmos necesarios.
		String respuesta = ALGORITMOS + ":" + ALGORITMOSIMETRICO+ ":" + ALGORITMOASIMETRICO+ ":" + ALGORITMOHMAC;
		return respuesta;
	}
	
//	public static X509Certificate generarCertificado()
//	{
//		X509Certificate respuesta = null;
//		//TODO implementar m�todo para obtener los algoritmos necesarios.
//		try {
//			respuesta = GeneradorDeCertificados.generateV3Certificate(claseSecretaAsimetrico.getKeys());
//		} catch (InvalidKeyException | NoSuchProviderException | SignatureException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return respuesta;
//	}	
	public static boolean revisarCertificado (InputStream input)
	{
		boolean respuesta = false;
		//TODO implementar m�todo para revisar la veracidad del certificado.
		try
		{
			CertificateFactory cf = CertificateFactory.getInstance("X509");
//			ByteArrayInputStream stream = new ByteArrayInputStream(certificateBytes);
			X509Certificate certificadoServidor = (X509Certificate) cf.generateCertificate(input);
			certificadoServidor.verify(certificadoServidor.getPublicKey());
//			CertificateBuilder (bouncycastle)
//			llaveServidor = certificadoServidor.getPublicKey();
//			byte[] firma = certificadoServidor.getSignature();
//			certificadoServidor.getSigAlgName();
//			ClaseSecretaAsimetrico.descifrarCertificado(firma, llaveServidor);
			respuesta = true;
			llaveServidor = certificadoServidor.getPublicKey();
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
		System.out.println("booleanoControl:" + respuesta);
		return respuesta;
	}
	
	public String obtenerCoordenadas()
	{
		double minLat = -90.00;
	    double maxLat = 90.00;      
	    double latitude = minLat + (double)(Math.random() * ((maxLat - minLat) + 1));
	    double minLon = 0.00;
	    double maxLon = 180.00;     
	    double longitude = minLon + (double)(Math.random() * ((maxLon - minLon) + 1));
	    DecimalFormat df = new DecimalFormat("#.#####");
	    
	    String[] parte1 = df.format(latitude).split(",");
	    String parte1Corregida = parte1[0] + "." + parte1[1];
	    String[] parte2 = df.format(latitude).split(",");
	    String parte2Corregida = parte2[0] + "." + parte2[1];
	    String respuesta = (parte1Corregida + "," + parte2Corregida);
	    System.out.println("Coordenadas: " + respuesta);
	    return respuesta;
	}
	
}
