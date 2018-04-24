package source;

import java.io.BufferedReader;
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
import java.text.DecimalFormat;

import javax.crypto.Cipher;
import javax.crypto.Mac;
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
	public final static String ALGORITMOHMAC = "HMACSHA1";
	private static Socket socket;
	private X509Certificate certCliente;
	
	private static PublicKey llaveServidor;
	
	private static ClaseSecretaAsimetrico claseSecretaAsimetrico;
//	private static ClaseSecretaSimetrico claseSecretaSimetrico;
	private static GeneradorDeCertificados certificateGenerator;
	public ProtocoloCliente(Socket pSocket)
	{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		socket = pSocket;
		claseSecretaAsimetrico = new ClaseSecretaAsimetrico(ALGORITMOASIMETRICO);
//		claseSecretaSimetrico = new ClaseSecretaSimetrico(ALGORITMOSIMETRICO, "");
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
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
				System.out.println("PRE VERIFICACIÓN");
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
					Cipher cipherAsimetrico = Cipher.getInstance(ALGORITMOASIMETRICO);
					cipherAsimetrico.init(Cipher.DECRYPT_MODE, claseSecretaAsimetrico.getKeys().getPrivate());
					byte[] LSbytes = cipherAsimetrico.doFinal(Hex.decode(datos[1]));
					SecretKey LS = new SecretKeySpec(LSbytes, 0, LSbytes.length, ALGORITMOSIMETRICO);
					String coordenadas = obtenerCoordenadas();
					Cipher cipherSimetrico = Cipher.getInstance(ALGORITMOSIMETRICO);
					SecretKeySpec keySpec = new SecretKeySpec(LSbytes, ALGORITMOSIMETRICO);
					cipherSimetrico.init(Cipher.ENCRYPT_MODE, keySpec);
					
					byte[] respuesta1 = cipherSimetrico.doFinal(coordenadas.getBytes()); 
					
					
					System.out.println(respuesta1);
					String respuesta1PostHexadecimal = Hex.toHexString(respuesta1);
					outputLine = "ACT1:" + respuesta1PostHexadecimal;
					output.flush();
					System.out.println(outputLine);
					output.println(outputLine);
					//FIN ACT1
					//INICIO ACT2
					Mac mac = Mac.getInstance(ALGORITMOHMAC);
					mac.init(LS);
					byte[] hasheadoBytes = mac.doFinal(coordenadas.getBytes());
					Cipher cipherAsimetricoEncriptar = Cipher.getInstance(ALGORITMOASIMETRICO);
					cipherAsimetricoEncriptar.init(Cipher.ENCRYPT_MODE, llaveServidor);
					String respuesta2PostExadecimal = Hex.toHexString(cipherAsimetricoEncriptar.doFinal(hasheadoBytes));
					outputLine = "ACT2:" + respuesta2PostExadecimal;
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
						System.out.println(ERROR + ": falló confirmación final.");
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
		//TODO implementar método para obtener los algoritmos necesarios.
		String respuesta = ALGORITMOS + ":" + ALGORITMOSIMETRICO+ ":" + ALGORITMOASIMETRICO+ ":" + ALGORITMOHMAC;
		return respuesta;
	}
	
	public static boolean revisarCertificado (InputStream input)
	{
		boolean respuesta = false;
		//TODO implementar método para revisar la veracidad del certificado.
		try
		{
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			X509Certificate certificadoServidor = (X509Certificate) cf.generateCertificate(input);
			certificadoServidor.verify(certificadoServidor.getPublicKey());
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
	    String[] parte2 = df.format(longitude).split(",");
	    String parte2Corregida = parte2[0] + "." + parte2[1];
	    String respuesta = (parte1Corregida + "," + parte2Corregida);
	    System.out.println("Coordenadas: " + respuesta);
	    return respuesta;
	}
	
}
