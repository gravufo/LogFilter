package connection;

/**
 * This class represents the template for a Session (abstraction)
 *
 * @author cartin
 */
public abstract class Session
{
    public abstract void execCommand(String cmd);

    public abstract void closeSession();
}
