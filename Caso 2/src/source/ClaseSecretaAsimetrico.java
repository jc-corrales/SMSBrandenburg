package source;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.Cipher;

public class ClaseSecretaAsimetrico
{
	//	public final static String ALGORITMO="RSA";
	public static String algoritmo;
	private KeyPair keyPair;
	public ClaseSecretaAsimetrico(String pAlgoritmo)
	{
		algoritmo = pAlgoritmo;
		KeyPairGenerator generator;
		try {
			generator = KeyPairGenerator.getInstance(algoritmo, Principal.PROVIDER);
			generator.initialize(1024);
			keyPair = generator.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] cifrar() {
		try {
			Cipher cipher = Cipher.getInstance(algoritmo);
			BufferedReader stdIn =
					new BufferedReader(new InputStreamReader(System.in));
			String pwd = stdIn.readLine();
			byte [] clearText = pwd.getBytes();
			String s1 = new String (clearText);
			System.out.println("clave original: " + s1);
			cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
			long startTime = System.nanoTime();
			byte [] cipheredText = cipher.doFinal(clearText);
			long endTime = System.nanoTime();
			System.out.println("clave cifrada: " + cipheredText);
			System.out.println("Tiempo asimetrico: " +
					(endTime - startTime));
			return cipheredText;
		}
		catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
			return null;
		}
	}
	public String descifrar(byte[] cipheredText) throws Exception
	{
		Cipher cipher = Cipher.getInstance(algoritmo);
		cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		byte [] clearText = cipher.doFinal(cipheredText);
		String s3 = new String(clearText);
		System.out.println("clave original: " + s3);
		return s3;
	}

	public static void descifrarCertificado(byte[] cipheredText, PublicKey llaveCertificado) {
		try {
			Cipher cipher = Cipher.getInstance(algoritmo);
			cipher.init(Cipher.DECRYPT_MODE, llaveCertificado);
			byte [] clearText = cipher.doFinal(cipheredText);
			String s3 = new String(clearText);
			System.out.println("clave original: " + s3);
		}
		catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
		}
	}

	public KeyPair getKeys()
	{
		return keyPair;
	}
}
