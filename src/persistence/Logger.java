package persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * This class will provide a single instance to output to a single log file per
 * runtime
 *
 * @author cartin
 */
public class Logger
{
    private final String namePrefix = "log_";
    private PrintWriter pw;
    private static Logger instance = null;
    private boolean open = false;

    public static Logger getInstance()
    {
	if (instance == null)
	{
	    instance = new Logger();
	}
	return instance;
    }

    public void open(String path)
    {
	open = true;
	createFile(findFileName(), path);
    }

    public synchronized void write(String output)
    {
	String[] lines = output.split("\\n");

	for (String s : lines)
	{
	    pw.println(s);
	}

	pw.flush();
    }

    public void close()
    {
	open = false;
	pw.close();
    }

    public boolean isOpen()
    {
	return open;
    }

    private String findFileName()
    {
	SimpleDateFormat fmt = new SimpleDateFormat("'" + namePrefix + "'yyyy-MM-dd_HH-mm-ss'.txt'");
	return fmt.format(new Date());
    }

    private void createFile(String name, String filePath)
    {
	try
	{
	    File file = new File(filePath + name);
	    File path = new File(filePath);
	    path.mkdir();
	    pw = new PrintWriter(file, "UTF-8");
	}
	catch (FileNotFoundException | UnsupportedEncodingException ex)
	{
	    java.util.logging.Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
}
