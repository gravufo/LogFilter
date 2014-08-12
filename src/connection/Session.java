package connection;

import java.io.InputStream;

/**
 * This class represents the template for a Session (abstraction)
 *
 * @author cartin
 */
public abstract class Session
{
    public abstract void execCommand(String cmd);

    public abstract void execCommand(int cmd);

    public abstract void closeSession();

    public abstract String readUntil(String pattern);

    public abstract InputStream getInputStream();
}
