package logfilter;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class represents a server profile
 *
 * @author cartin
 */
public class Server implements Serializable
{
    public Server(String name, String hostname)
    {
	this.monitorLogs = false;
	this.useSSH = true;
	this.name = name;
	this.hostname = hostname;
	this.logList = new ArrayList<>();
	this.monitorAlarms = false;
    }

    public Server(String name, String hostname, Server server)
    {
	this.monitorLogs = server.monitorLogs;
	this.useSSH = server.useSSH;
	this.name = name;
	this.hostname = hostname;
	this.logList = new ArrayList<>(server.logList);
	this.monitorAlarms = server.monitorAlarms;
    }

    public Server(Server server)
    {
	this.monitorLogs = server.monitorLogs;
	this.useSSH = server.useSSH;
	this.name = server.name;
	this.hostname = server.hostname;
	this.logList = new ArrayList<>(server.logList);
	this.monitorAlarms = server.monitorAlarms;
    }
    
    public String getHostname()
    {
	return hostname;
    }

    public String getName()
    {
	return name;
    }

    public void setHostname(String hostname)
    {
	this.hostname = hostname;
    }

    public void setName(String name)
    {
	this.name = name;
    }

    public void setMonitorLogs(boolean monitorLogs)
    {
	this.monitorLogs = monitorLogs;
    }

    public void setUseSSH(boolean useSSH)
    {
	this.useSSH = useSSH;
    }

    public boolean isMonitorLogs()
    {
	return monitorLogs;
    }

    public boolean isEnabled()
    {
	return (monitorLogs && logList.size() > 0) || monitorAlarms;
    }

    public boolean isUseSSH()
    {
	return useSSH;
    }

    public boolean addLog(String log)
    {
	return logList.add(log);
    }
    
    public boolean removeLog(String name)
    {
	return logList.remove(name);
    }

    public void removeAllLogs()
    {
	logList.clear();
    }

    public ArrayList<String> getLogList()
    {
	return logList;
    }

    public boolean isMonitorAlarms()
    {
	return monitorAlarms;
    }

    public void setMonitorAlarms(boolean monitorAlarms)
    {
	this.monitorAlarms = monitorAlarms;
    }

    /**
     * Contains the name of the log templates that are associated with this
     * server.
     */
    private ArrayList<String> logList;

    /**
     * Contains the information whether the server should be monitored (true) or
     * not (false).
     */
    private boolean monitorLogs;

    /**
     * Determines if we should use SSH (true) or Telnet (false) for the
     * connection.
     */
    private boolean useSSH;

    /**
     * Contains the hostname or IP address to which we should connect.
     */
    private String hostname;

    /**
     * Contains the name of the profile.
     */
    private String name;

    /**
     * Determines whether we should monitor alarms on this server
     */
    private boolean monitorAlarms;
}
