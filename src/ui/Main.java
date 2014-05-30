package ui;

/**
 *
 * @author cartin
 */
public class Main
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
	    // Set System Look and Feel
	    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
	    @Override
            public void run()
            {
                new MainWindow().setVisible(true);
            }
        });
    }
}
