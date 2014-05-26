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
public class Log implements Serializable
{
    public Log(String name)
    {
	this.name = name;
	filterMap = new HashMap<>();
	filePath = "";
	namePrefix = "";
    }
    
    public Log(Log log)
    {
	name = log.name;
	filterMap = new HashMap<>(log.filterMap);
	filePath = log.filePath;
	namePrefix = log.namePrefix;
    }
    
    public String getFilePath()
    {
	return filePath;
    }

    public void setFilePath(String filePath)
    {
	this.filePath = filePath;
    }

    public String getNamePrefix()
    {
	return namePrefix;
    }

    public void setNamePrefix(String namePrefix)
    {
	this.namePrefix = namePrefix;
    }

    public Map<String, Filter> getFilterMap()
    {
	return filterMap;
    }

    public String getName()
    {
	return name;
    }

    public void setName(String name)
    {
	this.name = name;
    }
    
    public Filter getFilter(String name)
    {
	return filterMap.get(name);
    }
    
    public boolean addFilter(Filter filter)
    {
	return filterMap.put(filter.getName(), filter) == null;
    }
    
    public boolean removeFilter(String name)
    {
	return filterMap.remove(name) != null;
    }
    
    private Map<String, Filter> filterMap;
    private String filePath;
    private String namePrefix;
    private String name;
}
