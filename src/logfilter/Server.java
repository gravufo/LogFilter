/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package logfilter;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author cartin
 */
public class Server implements Serializable
{
    public Server(String name, String hostname)
    {
	this.enabled = false;
	this.useSSH = true;
	this.name = name;
	this.hostname = hostname;
	this.logList = new ArrayList<>();
    }

    public Server(String name, String hostname, Server server)
    {
	this.enabled = server.enabled;
	this.useSSH = server.useSSH;
	this.name = name;
	this.hostname = hostname;
	this.logList = new ArrayList<>(server.logList);
    }
    
    public String getHostname()
    {
	return hostname;
    }

    public String getName()
    {
	return name;
    }

    public void setEnabled(boolean enabled)
    {
	this.enabled = enabled;
    }

    public void setUseSSH(boolean useSSH)
    {
	this.useSSH = useSSH;
    }

    public boolean isEnabled()
    {
	return enabled;
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

    public ArrayList<String> getLogList()
    {
	return logList;
    }
    
    private ArrayList<String> logList;
    private boolean enabled;
    private boolean useSSH;
    private final String hostname;
    private final String name;
}
