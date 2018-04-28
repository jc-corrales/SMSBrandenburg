package source;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class Generator
{
	public final static String NOMBRE = "";
	
	private LoadGenerator generator;
	
	public Generator()
	{
		Task work = createTask();
		int numberOfTasks = 100;
		int gapBetweenTasks = 1000;
		generator = new LoadGenerator(NOMBRE, numberOfTasks, work, gapBetweenTasks);
		generator.generate();
	}
	
	private Task createTask()
	{
		return new ClientServerTask();
	}
	
	public static void main(String args[])
	{
		@SuppressWarnings("unused")
		Generator gen = new Generator();
	}
}
