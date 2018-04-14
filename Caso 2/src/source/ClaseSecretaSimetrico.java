package source;

//import java.io.BufferedReader;
//import java.io.InputStreamReader;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class ClaseSecretaSimetrico
{
//	private SecretKey desKey;
//	private final static String ALGORITMO="AES";
	private final static String PADDING="AES/ECB/PKCS5Padding";
	private String algoritmo;
	public ClaseSecretaSimetrico(String pAlgoritmo, String pPadding)
	{
		algoritmo = pAlgoritmo;
	}

	/**
	 * Método para cifrar.
	 * @return
	 */
	public byte[] cifrar(String entrada, SecretKey llave) {
		byte [] cipheredText;
		try {
//			KeyGenerator keygen = KeyGenerator.getInstance(algoritmo);
//			desKey = keygen.generateKey();
			Cipher cipher = Cipher.getInstance(PADDING);
			//		BufferedReader stdIn = new BufferedReader(
			//		 new InputStreamReader(System.in));
			//		String pwd = stdIn.readLine();
			byte [] clearText = entrada.getBytes();
			String s1 = new String (clearText);
			System.out.println("entrada original: " + s1);
			cipher.init(Cipher.ENCRYPT_MODE, llave);
			long startTime = System.nanoTime();
			cipheredText = cipher.doFinal(clearText);
			long endTime = System.nanoTime();
			String s2 = new String (cipheredText);
			System.out.println("entrada cifrada: " + s2);
			System.out.println("Tiempo: " + (endTime - startTime));
			return cipheredText;
		}
		catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
			return null;
		}
	}
	/**
	 * Método para descifrar
	 * @param cipheredText
	 */
	public void descifrar(byte [] cipheredText, SecretKey llave) {
		try {
			Cipher cipher = Cipher.getInstance(PADDING);
			cipher.init(Cipher.DECRYPT_MODE, llave);
			byte [] clearText = cipher.doFinal(cipheredText);
			String s3 = new String(clearText);
			System.out.println("clave original: " + s3);
		}
		catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
		}
	}
	
//	public void descifrar(byte [] cipheredText, SecretKey llave) {
//		try {
//			Cipher cipher = Cipher.getInstance(PADDING);
//			cipher.init(Cipher.DECRYPT_MODE, llave);
//			byte [] clearText = cipher.doFinal(cipheredText);
//			String s3 = new String(clearText);
//			System.out.println("clave original: " + s3);
//		}
//		catch (Exception e) {
//			System.out.println("Excepcion: " + e.getMessage());
//		}
//	}
}
