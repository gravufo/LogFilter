package ui;

import java.util.Comparator;
import logfilter.Server;
import persistence.Preferences;

/**
 * This class allows you to compare List elements in order to sort them in a
 * custom way
 *
 * @author cartin
 */
public class ListComparator implements Comparator<String>
{
    public enum Type
    {
	SERVER,
	LOG,
	FILTER
    }

    Type type;

    public ListComparator(Type type)
    {
	this.type = type;
    }

    @Override
    public int compare(String t1, String t2)
    {
	int result;

	switch (type)
	{
	    case SERVER:

		Server s1 = Preferences.getInstance().getServer(t1);
		Server s2 = Preferences.getInstance().getServer(t2);

		// If both are enabled, sort by name
		if (s1.isEnabled() && s2.isEnabled() || (!s1.isEnabled() && !s2.isEnabled()))
		{
		    result = t1.compareToIgnoreCase(t2);
		}
		else if (s1.isEnabled() && !s2.isEnabled())
		{
		    result = -1;
		}
		else // if(!s1.isEnabled() && s2.isEnabled())
		{
		    result = 1;
		}

		break;

	    case LOG:

		result = t1.compareTo(t2);

		break;

	    case FILTER:

		result = t1.compareTo(t2);

		break;

	    default:

		result = 0;
	}

	return result;
    }
    
}
