package logfilter;

import java.io.Serializable;

/**
 * This class represents a filter profile
 *
 * @author cartin
 */
public class Filter implements Serializable
{

    public Filter(String name)
    {
	this.name = name;
	enabled = false;
	keyword = "";
	linesBefore = 0;
	lineAfter = 0;
    }
    
    public Filter(Filter filter)
    {
	name = filter.name;
	enabled = filter.enabled;
	keyword = filter.keyword;
	linesBefore = filter.linesBefore;
	lineAfter = filter.lineAfter;
    }

    public boolean isEnabled()
    {
	return enabled;
    }

    public void setEnabled(boolean enabled)
    {
	this.enabled = enabled;
    }

    public String getName()
    {
	return name;
    }

    public void setName(String name)
    {
	this.name = name;
    }

    public String getKeyword()
    {
	return keyword;
    }

    public void setKeyword(String keyword)
    {
	this.keyword = keyword.toLowerCase();
    }

    public int getLinesBefore()
    {
	return linesBefore;
    }

    public void setLinesBefore(int linesBefore)
    {
	this.linesBefore = linesBefore;
    }

    public int getLineAfter()
    {
	return lineAfter;
    }

    public void setLinesAfter(int lineAfter)
    {
	this.lineAfter = lineAfter;
    }

    /**
     * Determines whether this filter is activated (true) or not (false).
     */
    private boolean enabled;

    /**
     * Contains the name of the filter profile
     */
    private String name;

    /**
     * Contains the keyword that should be used to filter logs
     */
    private String keyword;

    /**
     * Contains the number of lines that should be displayed before the keyword
     */
    private int linesBefore;

    /**
     * Contains the number of lines that should be displayed after the keyword
     */
    private int lineAfter;
}
