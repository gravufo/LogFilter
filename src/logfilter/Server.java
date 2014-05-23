/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package logfilter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
	this.logMap = new HashMap<>();
    }

    public Server(String name, String hostname, Server server)
    {
	this.enabled = server.enabled;
	this.useSSH = server.useSSH;
	this.name = name;
	this.hostname = hostname;
	this.logMap = new HashMap<>(server.logMap);
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

    public Log getLog(String name)
    {
	return logMap.get(name);
    }

    public boolean addLog(Log log)
    {
	return logMap.putIfAbsent(log.getName(), log) == null;
    }
    
    public boolean removeLog(String name)
    {
	return logMap.remove(name) != null;
    }

    public Map<String, Log> getLogMap()
    {
	return logMap;
    }
    
    private Map<String, Log> logMap;
    private boolean enabled;
    private boolean useSSH;
    private final String hostname;
    private final String name;
}
