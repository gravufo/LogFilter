package collections;

import java.io.Serializable;

/**
 * Custom Pair class
 *
 * @author cartin
 */
public class Pair<L, R> implements Serializable
{
    private L l;
    private R r;

    public Pair(L l, R r)
    {
	this.l = l;
	this.r = r;
    }

    public L getKey()
    {
	return l;
    }

    public R getValue()
    {
	return r;
    }

    public void setKey(L l)
    {
	this.l = l;
    }

    public void setValue(R r)
    {
	this.r = r;
    }
}
