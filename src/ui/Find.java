/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

/**
 *
 * @author cartin
 */
public class Find extends JDialog
{
    private final JLabel label;
    private final JTextField textField;
    private final JCheckBox caseCheckBox;
    private final JCheckBox wrapCheckBox;
    private final JCheckBox wholeCheckBox;
    private final JCheckBox backCheckBox;
    private final JButton findButton;
    private final JButton cancelButton;
    private final JTextPane console;
    private int lastI = -1;
    private static Find instance = null;

    public Find()
    {
	super(MainWindow.getFrames()[0], false);
	ImageIcon img = new ImageIcon(getClass().getResource("/images/Log_icon.png"));
	setIconImage(img.getImage());
	console = MainWindow.getConsolePane();
	label = new JLabel("Find What:");
	textField = new JTextField();
	caseCheckBox = new JCheckBox("Match Case");
	wrapCheckBox = new JCheckBox("Wrap Around");
	wholeCheckBox = new JCheckBox("Whole Words");
	backCheckBox = new JCheckBox("Search Backwards");
	findButton = new JButton("Find");
	cancelButton = new JButton("Cancel");

	// Set default settings
	backCheckBox.setSelected(true);

	// Set the escape character to close the dialog
	ActionListener escListener = new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
		setVisible(false);
	    }
	};

	getRootPane().registerKeyboardAction(escListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

	textField.addKeyListener(new KeyAdapter()
	{
	    @Override
	    public void keyPressed(KeyEvent ke)
	    {
		if (ke.getKeyCode() == KeyEvent.VK_ENTER)
		{
		    find();
		}
	    }
	});

	findButton.addActionListener(new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent ae)
	    {
		find();
	    }
	});

	cancelButton.addActionListener(new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent ae)
	    {
		setVisible(false);
	    }
	});

	// remove redundant default border of check boxes - they would hinder
	// correct spacing and aligning (maybe not needed on some look and feels)
	caseCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	wrapCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	wholeCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	backCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

	GroupLayout layout = new GroupLayout(getContentPane());
	getContentPane().setLayout(layout);
	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(true);

	layout.setHorizontalGroup(layout.createSequentialGroup()
		.addComponent(label)
		.addGroup(layout.createParallelGroup(LEADING)
			.addComponent(textField)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(LEADING)
					.addComponent(caseCheckBox)
					.addComponent(wholeCheckBox))
				.addGroup(layout.createParallelGroup(LEADING)
					.addComponent(wrapCheckBox)
					.addComponent(backCheckBox))))
		.addGroup(layout.createParallelGroup(LEADING)
			.addComponent(findButton)
			.addComponent(cancelButton))
	);

	layout.linkSize(SwingConstants.HORIZONTAL, findButton, cancelButton);

	layout.setVerticalGroup(layout.createSequentialGroup()
		.addGroup(layout.createParallelGroup(BASELINE)
			.addComponent(label)
			.addComponent(textField)
			.addComponent(findButton))
		.addGroup(layout.createParallelGroup(LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(BASELINE)
					.addComponent(caseCheckBox)
					.addComponent(wrapCheckBox))
				.addGroup(layout.createParallelGroup(BASELINE)
					.addComponent(wholeCheckBox)
					.addComponent(backCheckBox)))
			.addComponent(cancelButton))
	);

	setTitle("Find");
	pack();
	setLocationRelativeTo(MainWindow.getFrames()[0]);
	setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    public static Find getInstance()
    {
	if (instance == null)
	{
	    instance = new Find();
	}

	return instance;
    }

    public void display()
    {
	textField.setSelectionStart(0);
	textField.setSelectionEnd(textField.getText().length());
	setVisible(true);
    }

    private void find()
    {
	if (textField.getText().isEmpty())
	{
	    return;
	}

	String keyword = textField.getText();
	String text = "";

	try
	{
	    text = console.getStyledDocument().getText(0, console.getStyledDocument().getLength());
	}
	catch (BadLocationException ex)
	{
	    Logger.getLogger(Find.class.getName()).log(Level.SEVERE, null, ex);
	}

	int initialPosition = console.getCaretPosition();

	// If we are searching in reverse
	if (backCheckBox.isSelected())
	{
	    initialPosition -= keyword.length();
	    if (initialPosition < 0)
	    {
		if (wrapCheckBox.isSelected())
		{
		    initialPosition = text.length() - 1 - keyword.length();
		}
		else
		{
		    return;
		}
	    }
	}
	else if (initialPosition == text.length() && wrapCheckBox.isSelected())
	{
	    initialPosition = 0;
	}

	if (!caseCheckBox.isSelected())
	{
	    text = text.toLowerCase();
	    keyword = keyword.toLowerCase();
	}

	boolean foundMatch = false;
	boolean wrapComplete = false;

	for (int i = initialPosition; i < text.length() && i >= 0;)
	{
	    int temp = i;

	    for (int j = 0; j < keyword.length(); ++j)
	    {
		if (text.charAt(temp) != keyword.charAt(j))
		{
		    break;
		}

		if (j == keyword.length() - 1 && i != lastI)
		{
		    if (wholeCheckBox.isSelected())
		    {
			foundMatch = verifyWholeWord(i, keyword.length(), text);
		    }
		    else
		    {
			foundMatch = true;
		    }
		}

		++temp;
	    }

	    if (foundMatch)
	    {
		console.setCaretPosition(i);
		console.setSelectionStart(i);
		console.setSelectionEnd(i + keyword.length());
		lastI = i;
//
//		JScrollBar scroll = MainWindow.getScrollBar().getVerticalScrollBar();
//		if (backCheckBox.isSelected())
//		{
//		    scroll.setValue(scroll.getValue() + (int) (scroll.getVisibleAmount() / 2));
//		}
//		else
//		{
//		    scroll.setValue(scroll.getValue() - (int) (scroll.getVisibleAmount() / 2));
//		}
//
//		// Go 10 \n after in order to see more of the context
//		int newPos = i;
//		for (int newLine = 0; newLine < 10; ++newLine)
//		{
//		    newPos = text.indexOf("\n", newPos);
//		}
//
//		console.setCaretPosition(newPos);


		return;
	    }

	    // If we never wraped yet
	    if (wrapCheckBox.isSelected() && !wrapComplete)
	    {
		if (!backCheckBox.isSelected() && i == text.length() - 1)
		{
		    // Reset counter to the top and make sure we don't wrap indefinitely
		    wrapComplete = true;
		    i = -1;
		}
		else if (backCheckBox.isSelected() && i == 0)
		{
		    // Reset counter to the top and make sure we don't wrap indefinitely
		    wrapComplete = true;
		    i = text.length() - keyword.length() + 1;
		}
	    }
	    // If wrapped AND back to initial position
	    else if (wrapComplete && ((!backCheckBox.isSelected() && i == initialPosition)
		    || (backCheckBox.isSelected() && i == initialPosition - keyword.length())))
	    {
		JOptionPane.showMessageDialog(this, "No match found.");
		return;
	    }

	    if (backCheckBox.isSelected())
	    {
		--i;
	    }
	    else
	    {
		++i;
	    }
	}

	JOptionPane.showMessageDialog(this, "No match found.");
    }

    private boolean verifyWholeWord(int i, int length, String text)
    {
	boolean left = false,
		right = false;

	// Check the char before
	if (i - 1 >= 0)
	{
	    char before = text.charAt(i - 1);

	    switch (before)
	    {
		case ' ':
		case '.':
		case '\'':
		case '"':
		case ';':
		case ',':
		case ':':
		case '(':
		case '?':
		case '!':
		case '@':
		    break;
		default:
		    return false;
	    }
	}

	// Check the char after
	if (i + length < text.length())
	{
	    char after = text.charAt(i + length);

	    switch (after)
	    {
		case ' ':
		case '.':
		case '\'':
		case '"':
		case ';':
		case ',':
		case ':':
		case ')':
		case '?':
		case '!':
		case '@':
		    break;
		default:
		    return false;
	    }
	}

	return true;
    }
}
