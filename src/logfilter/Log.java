package logfilter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a log file monitoring profile
 *
 * @author cartin
 */
public class Log implements Serializable
{
    public Log(String name)
    {
	this.name = name;
	filterMap = new HashMap<>();

	// Create a new default exceptions filter for any new log file
	Filter exceptionFilter = new Filter("Exceptions");
	exceptionFilter.setEnabled(true);
	exceptionFilter.setKeyword("exception");
	exceptionFilter.setLinesAfter(10);
	exceptionFilter.setLinesBefore(10);
	filterMap.put(exceptionFilter.getName(), exceptionFilter);

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

    /**
     * This map contains the Filters that should be applied to this log
     */
    private Map<String, Filter> filterMap;

    /**
     * Contains the full path <u>with the trailing slash</u> to the log file on
     * the server.
     */
    private String filePath;

    /**
     * Contains the prefix of the log file name. E.g.: XSLog for XSLog-DATE-.log
     */
    private String namePrefix;

    /**
     * Contains the name of the log file profile
     */
    private String name;
}
