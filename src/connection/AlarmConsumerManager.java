package connection;

import java.util.HashMap;
import ui.MainWindow;

/**
 *
 * @author cartin
 */
public class AlarmConsumerManager
{
    private HashMap<String, AlarmConsumer> serversToMonitor;
    private static AlarmConsumerManager instance = null;

    private AlarmConsumerManager()
    {
	serversToMonitor = new HashMap<>();
    }

    public static AlarmConsumerManager getInstance()
    {
	if (instance == null)
	{
	    instance = new AlarmConsumerManager();
	}

	return instance;
    }

    public void addServer(String serverName)
    {
	if (!serversToMonitor.containsKey(serverName))
	{
	    AlarmConsumer ac = new AlarmConsumer(serverName);
	    ac.start();

	    serversToMonitor.put(serverName, ac);
	}
    }

    public void removeServer(String serverName)
    {
	AlarmConsumer ac = serversToMonitor.remove(serverName);

	if (ac != null)
	{
	    ac.stopConsumer();

	    try
	    {
		ac.join(500);
	    }
	    catch (InterruptedException ex)
	    {
		MainWindow.writeToConsole("Alarm onnection killed: " + serverName + "\n");
		ac.stop();
	    }
	}
    }

    public void removeServers()
    {
	serversToMonitor.clear();
    }
}
