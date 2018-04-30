package source;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class Generator
{
	public final static String NOMBRE = "Client - Server Load Test";
	/**
	 * Load generator Srevice (From GLoad 1.0)
	 */
	private LoadGenerator generator;
	/**
	 * Constructs a new Generator
	 */
	public Generator()
	{
		Task work = createTask();
		int numberOfTasks = 80;
		int gapBetweenTasks = 100;
		generator = new LoadGenerator(NOMBRE, numberOfTasks, work, gapBetweenTasks);
		generator.generate();
	}
	/**
	 * Helper that constructs a task.
	 * @return
	 */
	private Task createTask()
	{
		return new ClientServerTask();
	}
	/**
	 * Starts the application.
	 * @param args
	 */
	public static void main(String args[])
	{
		@SuppressWarnings("unused")
		Generator gen = new Generator();
	}
}
