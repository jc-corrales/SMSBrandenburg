package source;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;

public class Hash
{
	public Hash()
	{
		
	}
	
	private byte[] getKeyedDigest(byte[] buffer, String algoritmo, String proveedor) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(buffer);
			return md5.digest();
		} catch (Exception e) {
			return null;
		}
	}
	public byte[] calcular(String informacion, String algoritmo, String proveedor) {
		try {
//			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			String dato = informacion;
			byte[] text = dato.getBytes();
			String s1 = new String(text);
			System.out.println("dato original: " + s1);
			byte [] digest = getKeyedDigest(text, algoritmo, proveedor);
			String s2 = new String(digest);
			System.out.println("digest: "+ s2);
			return digest;
		}
		catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
			return null;
		}
	}
	
	public boolean verificar(byte[] algo1,byte[] algo2)
	{
		if(algo1.length != algo2.length)
		{
			System.err.println("TAMAÑOS NO COINCIDEN");
			return false;
		}
		for(int i = 0; i < algo1.length && i < algo2.length; i++)
		{
			if(algo1[i] != algo2[i])
			{
				System.err.println("ERROR EN COMPARACIÓN");
				return false;
			}
		}
		return true;
	}
}
