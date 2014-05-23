/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package logfilter;

import java.io.Serializable;

/**
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
	this.keyword = keyword;
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

    public void setLineAfter(int lineAfter)
    {
	this.lineAfter = lineAfter;
    }
    
    private boolean enabled;
    private String name;
    private String keyword;
    private int linesBefore;
    private int lineAfter;
}
