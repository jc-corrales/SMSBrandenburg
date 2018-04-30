package source;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Registrador
{
	private static ArrayList<String> lista = new ArrayList<String>();
	public final static String NOMBREARCHIVO = "datos.csv";
	public static void addRegister(String register)
	{
		lista.add(register);
	}
	
	public static void writeRegister() throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(NOMBREARCHIVO));
		for(int i = 0; i < lista.size(); i++)
		{
			String tupla = i + ":;" + lista.get(i);
			System.out.println(tupla);
			writer.write(tupla);
			writer.newLine();
		}
		writer.close();
	}
}
