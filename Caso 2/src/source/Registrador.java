package source;

import java.util.ArrayList;

public class Registrador
{
	private static ArrayList<String> lista = new ArrayList<String>();
	
	public static void addRegister(String register)
	{
		lista.add(register);
	}
	
	public static void writeRegister()
	{
		for(int i = 0; i < lista.size(); i++)
		{
			System.out.println(i + ": " + lista.get(i));
		}
	}
}
