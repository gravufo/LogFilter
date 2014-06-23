package logfilter;

import java.awt.Color;
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
	messagesBefore = 0;
	messagesAfter = 0;
	highlightColor = Color.GREEN;
    }
    
    public Filter(Filter filter)
    {
	name = filter.name;
	enabled = filter.enabled;
	keyword = filter.keyword;
	messagesBefore = filter.messagesBefore;
	messagesAfter = filter.messagesAfter;
	highlightColor = filter.highlightColor;
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

    public int getMessagesBefore()
    {
	return messagesBefore;
    }

    public void setMessagesBefore(int messagesBefore)
    {
	this.messagesBefore = messagesBefore;
    }

    public int getMessagesAfter()
    {
	return messagesAfter;
    }

    public void setMessagesAfter(int messagesAfter)
    {
	this.messagesAfter = messagesAfter;
    }

    public Color getHighlightColor()
    {
	return highlightColor;
    }

    public void setHighlightColor(Color highlightColor)
    {
	this.highlightColor = highlightColor;
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
     * Contains the number of messages that should be displayed before the
     * keyword
     */
    private int messagesBefore;

    /**
     * Contains the number of messages that should be displayed after the
     * keyword
     */
    private int messagesAfter;

    /**
     * Contains the color of the foreground text to use when this filter's
     * keyword is matched
     */
    private Color highlightColor;
}
