package source;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Registrador
{
	public final static String NOMBREARCHIVO = "datos.csv";
	
	public static void registrar(String register) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(NOMBREARCHIVO, true));
		System.out.println(register);
		writer.write(register);
		writer.newLine();
		writer.close();
	}

}
