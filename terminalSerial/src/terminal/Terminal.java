package terminal;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.Rectangle;

import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Dimension;

import javax.swing.JTextArea;

import java.awt.Font;
import java.awt.Color;

import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.SystemColor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JSeparator;
import javax.swing.JCheckBoxMenuItem;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Terminal implements ActionListener {

	private void initApplicaion() {
		loadSettings();
		closeConnection();
		inputBuffer = new LinkedList<Byte>();
		keyBoardBuffer = new LinkedList<Byte>();
		screen.setText("");
		updateScreenPositionData();
		patternForPrintableChars = Pattern
				.compile(allPrintableCharactersPattern);
	}// initApplicaion

	/*
	 * Screen activity - using JTextAre
	 */

	private void keyReceived(Byte value) {
		// supressCaretUpdate = true;
		lblLastByteSent.setText(String.format("Last Byte sent : %02X - %s",
				value, value.toString()));
		int cp = screen.getCaretPosition(); // where are we

		String ck = new String(new byte[] { value });
		lblLastByteSent.setText(String.format("Last Byte sent : %02X - %s",
				value, ck));

		matcher = patternForPrintableChars.matcher(ck); // looking for printable
														// characters
		if (matcher.matches() || ck.equals(SPACE)) { // printable
			if (cp >= (MAX_CHARACTERS - 1)) { // If last character on screen
				scrollLine();
				cp = ROW_23_COLUMN_0;
			}// inner if
			screen.replaceRange(ck, cp, cp);
			screen.setCaretPosition(cp + 1);
		} else { // special & non printable chars
			switch (value) {
			case 0X07: // Bell \a
				java.awt.Toolkit.getDefaultToolkit().beep();
				break;
			case 0X08: // Backspace \b
				screen.setCaretPosition(Math.max(screen.getCaretPosition() - 1,
						0));
				break;
			case 0X09: // Tab \t
				for (int i = screen.getCaretPosition() % tabWidth; i < tabWidth; i++) {
					keyReceived(SPACE_BYTE);
				}// for
				break;
			case 0X0A: // Newline \n
				for (int i = 0; i < MAX_COLUMNS; i++) {
					keyReceived(SPACE_BYTE);
				}// for
				break;
			case 0X0B: // Vertical Tab \v
				break;
			case 0X0C: // Form feed \f
				screen.setText("");
				break;
			case 0X0D: // Carriage return \r
				screen.setCaretPosition(screen.getCaretPosition()
						- (screen.getCaretPosition() % MAX_COLUMNS));
				break;
			case 0X1B: // Escape \e
				break;
			default:
			}// switch
		}// if - matcher
		updateRowCol();
		// supressCaretUpdate = false;
	}// keyIn

	private void scrollLine() {
		screen.setEnabled(false);

		screen.replaceRange(null, 0, MAX_COLUMNS);
		String screenTemp = screen.getText();
		screen.setText(screenTemp.substring(0,
				((MAX_ROWS - 1) * MAX_COLUMNS) - 1));
		screen.setEnabled(true);
	}// scrollLine

	private void updateRowCol() {
		int cp = screen.getCaretPosition();
		lastCaretPosition = cp;
		this.row = (int) (cp / MAX_COLUMNS);
		this.col = cp - (this.row * MAX_COLUMNS);
		updateScreenPositionData();
	}// updateRowCol

	private void updateScreenPositionData() {
		String spd = String.format("Row: %02d  Column: %02d  Cursor %04d", row,
				col, lastCaretPosition);
		lblScreenPositionInfo.setText(spd);
	}// updateScreenPositionData

	/*
	 * Hardware aspects of Terminal using SerialPort
	 */

	private void readInputBuffer() {
		Byte inByte = inputBuffer.poll();
		while (inByte != null) {
			keyReceived(inByte);
			inByte = inputBuffer.poll();
		}// while
	}// readInputBuffer

	private void sendOutputBuffer() {
		if (serialPort == null) {
			String msg = String.format("Serial Port %s is not opened",
					terminalSettings.getPortName());
			JOptionPane.showMessageDialog(null, "Keyboard In", msg,
					JOptionPane.WARNING_MESSAGE);
		} else {
			Byte outByte = keyBoardBuffer.poll();
			while (outByte != null) {
				try {
					serialPort.writeByte(outByte);
				} catch (SerialPortException e) {
					String msg = String
							.format("Failed to write byte %02d to port %s with exception %s",
									outByte, terminalSettings.getPortName(),
									e.getExceptionType());
					JOptionPane.showMessageDialog(null, msg, "Keyboard In",
							JOptionPane.WARNING_MESSAGE);
					// e.printStackTrace();
				}
				// sendByte(outByte);
				outByte = keyBoardBuffer.poll();
			}// while
		}

	}// sendOutputBuffer

	private void processKeyTyped(Character keyIn) {
		Byte byteIn = (byte) keyIn.charValue();
		// System.out.printf("keyIn = %s , %02X%n", keyIn, byteIn);
		if (!fullDuplex) {
			keyReceived(byteIn);
		}// if
		keyBoardBuffer.add(byteIn);
		sendOutputBuffer();
	}// processKeyTyped

	// //////////***********
	public class SerialPortReader implements SerialPortEventListener {

		@Override
		public void serialEvent(SerialPortEvent spe) {
			if (spe.isRXCHAR()) {
				// System.out.printf(" spe.getEventValue() = %d%n",
				// spe.getEventValue());
				if (spe.getEventValue() > 0) {// data available
					try {
						byte[] buffer = serialPort.readBytes();
						for (Byte b : buffer) {
							inputBuffer.add(b);
						}
						readInputBuffer();
						// System.out.println(Arrays.toString(buffer));

					} catch (SerialPortException speRead) {
						System.out.println(speRead);
					}// try
				}// inner if

			} else if (spe.isCTS()) { // CTS line has changed state
				String msg = (spe.getEventValue() == 1) ? "CTS - On"
						: "CTS - Off";
				System.out.println(msg);
			} else if (spe.isDSR()) { // DSR line has changed state
				String msg = (spe.getEventValue() == 1) ? "DSR - On"
						: "DSR - Off";
				System.out.println(msg);
			} else {
				System.out.printf("Unhandled event : %s%n", spe.toString());
			}

		}// serialEvent

	}// class SerialPortReader
		// ////////***********

	/*
	 * Terminal class independent of screen and I/O
	 */

//	public void establishSettings(TerminalSettings ts) {
//		this.terminalSettings = ts;
//		showConnectionString();
//	}// establishSettings

	private void doBtnSettings() {
		PortSetupDetails psd = new PortSetupDetails( terminalSettings);
		psd.setVisible(true);
		showConnectionString();
	}// doBtnSettings

	private void doBtnOpenClose() {
		if (btnOpenClose.getText().equals(BTN_OPEN)) {
			openConnection();
		} else {
			closeConnection();
		}// else
	}// doBtnOpenClose

	private void openConnection() {
		if (serialPort != null) {
			String msg = String.format("Serial Port %s is already opened",
					terminalSettings.getPortName());
			JOptionPane.showMessageDialog(null, msg);
			showConnectionString();
			return;
		}
		serialPort = new SerialPort(terminalSettings.getPortName());

		try {
			serialPort.openPort();// Open serial port
			serialPort.setParams(terminalSettings.getBaudRate(),
					terminalSettings.getDataBits(),
					terminalSettings.getStopBits(),
					terminalSettings.getParity());
			serialPort.addEventListener(new SerialPortReader());
		} catch (SerialPortException ex) {
			System.out.println(ex);
		}// try

		connectionStateClosed(false);
	}// openConnection

	private void closeConnection() {
		if (serialPort != null) {
			try {
				serialPort.closePort();
			} catch (SerialPortException e) {
				e.printStackTrace();
			}// try
			serialPort = null;
		}// if
		connectionStateClosed(true);
		showConnectionString();
	}// closeConnection

	private void connectionStateClosed(boolean closed) {
		btnSettings.setEnabled(closed);
		mnuFileLoad.setEnabled(closed);
		mnuFileDefaultSettings.setEnabled(closed);

		if (closed) {
			btnOpenClose.setText(BTN_OPEN);
		} else {
			btnOpenClose.setText(BTN_CLOSE);

		}
	}

	private void showConnectionString() {
		String strStopBits = "0";
		switch (terminalSettings.getStopBits()) {
		case 1:
			strStopBits = "1";
			break;
		case 2:
			strStopBits = "2";
			break;
		case 3:
			strStopBits = "1.5";
			break;
		}// switch - stopBits
		String[] strParity = new String[] { "None", "Odd", "Even", "Mark",
				"Space" };
		String con = String.format("%s-%d-%d-%s-%s",
				terminalSettings.getPortName(), terminalSettings.getBaudRate(),
				terminalSettings.getDataBits(), strStopBits,
				strParity[terminalSettings.getParity()]);

		lblCurrentSettings.setText(con);
	}// showConnectionString

	private void loadSettings() {
		loadSettings(DEFAULT_STATE_FILE);
	}

	private void loadSettings(String fileName) {

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				fileName + FILE_SUFFIX_PERIOD))) {
			terminalSettings = (TerminalSettings) ois.readObject();
			currentSettingsFileName = fileName;
		} catch (ClassNotFoundException | IOException cnfe) {
			String msg = String.format(
					"Could not find: %s, will proceed with default settings",
					fileName);
			JOptionPane.showMessageDialog(null, msg);
			if (terminalSettings != null) {
				terminalSettings = null;
			}//
			terminalSettings = new TerminalSettings();
			terminalSettings.setDefaultSettings();
			currentSettingsFileName = DEFAULT_STATE_FILE;
		}// try
		screen.setForeground(terminalSettings.getScreenForeground());
		screen.setBackground(terminalSettings.getScreenBackground());
	}// loadSettings

	private void saveSettings() {
		saveSettings(DEFAULT_STATE_FILE);
	}

	private void saveSettings(String fileName) {
		try (ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(fileName + FILE_SUFFIX_PERIOD))) {
			oos.writeObject(terminalSettings);
		} catch (Exception e) {
			String msg = String.format("Could not save to : %s%S. ", fileName,
					FILE_SUFFIX_PERIOD);
			JOptionPane.showMessageDialog(null, msg);
		}
	}

	private void closeCleanly() {
		saveSettings();
		if (serialPort != null) {
			try {
				serialPort.closePort();
			} catch (SerialPortException e) {
				// ignore if it is already closed
			}// try
		}// if
		serialPort = null;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		switch (ae.getActionCommand()) {
		case "btnSettings":
			doBtnSettings();
			break;
		case "btnOpenClose":
			doBtnOpenClose();
			break;
		}// switch - actionCommand
	}// actionPerformed

	// ------------------------------------------------------------------------------------

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Terminal window = new Terminal();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Terminal() {
		initialize();
		initApplicaion();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(new Rectangle(100, 100, 825, 573));
		frame.setTitle("CRT");
		// frame.addWindowListener(this);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				closeCleanly();
			}
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		mnuFileLoad = new JMenuItem("Load ...");
		mnuFileLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Saved State Files", FILE_SUFFIX);
				chooser.setFileFilter(filter);
				int returnValue = chooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String absolutePath = chooser.getSelectedFile()
							.getAbsolutePath();
					// need to strip the file suffix off (will replace later)
					int periodLocation = absolutePath.indexOf(".");
					if (periodLocation != -1) {// this selection has a suffix
						absolutePath = absolutePath
								.substring(0, periodLocation); // removed
																// suffix
					}// inner if
					loadSettings(absolutePath);
				} else {
					String msg = String.format("You cancelled the Load...%n");
					JOptionPane.showMessageDialog(null, msg);
				}// if - returnValue
			}
		});
		mnuFile.add(mnuFileLoad);

		JSeparator separator = new JSeparator();
		mnuFile.add(separator);

		JMenuItem mnuFileSave = new JMenuItem("Save");
		mnuFile.add(mnuFileSave);

		JMenuItem mnuFileSaveAs = new JMenuItem("Save as ...");
		mnuFileSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Saved State Files", FILE_SUFFIX);
				chooser.setFileFilter(filter);
				int returnValue = chooser.showSaveDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String absolutePath = chooser.getSelectedFile()
							.getAbsolutePath();
					// need to strip the file suffix off (will replace later)
					int periodLocation = absolutePath.indexOf(".");
					if (periodLocation != -1) {// this selection has a suffix
						absolutePath = absolutePath
								.substring(0, periodLocation); // removed
																// suffix
					}// inner if
					saveSettings(absolutePath);
				} else {
					JOptionPane.showMessageDialog(null,
							"You cancelled the Save as...");
				}// if - returnValue
			}
		});
		mnuFile.add(mnuFileSaveAs);

		JSeparator separator_1 = new JSeparator();
		mnuFile.add(separator_1);

		JMenuItem mnuFileResetColor = new JMenuItem("Reset colors");
		mnuFileResetColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				terminalSettings.setDefaultScreenColors();
				screen.setForeground(terminalSettings.getScreenForeground());
				screen.setBackground(terminalSettings.getScreenBackground());
			}
		});
		mnuFile.add(mnuFileResetColor);

		mnuFileDefaultSettings = new JMenuItem("Restore Default Settings");
		mnuFileDefaultSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				terminalSettings.setDefaultSettings();
				screen.setForeground(terminalSettings.getScreenForeground());
				screen.setBackground(terminalSettings.getScreenBackground());
				showConnectionString();
			}
		});
		mnuFile.add(mnuFileDefaultSettings);

		JSeparator separator_3 = new JSeparator();
		mnuFile.add(separator_3);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeCleanly();
				System.exit(0);
			}
		});
		mnuFile.add(mnuFileExit);

		JSeparator separator_2 = new JSeparator();
		mnuFile.add(separator_2);

		JMenu mnuOptions = new JMenu("Options");
		menuBar.add(mnuOptions);

		JMenuItem mnuOptionBackgroundColor = new JMenuItem(
				"Background Color ...");
		mnuOptionBackgroundColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(null, "cc",
						screen.getBackground());
				// JColorChooser cc = new JColorChooser();
				// Color newColor = cc.showDialog(null,
				// "Choose the screen background color",
				// screen.getBackground());
				if (newColor == null) {
					return;
				} else {
					screen.setBackground(newColor);
					terminalSettings.setScreenBackground(newColor);
				}
			}
		});
		mnuOptions.add(mnuOptionBackgroundColor);

		JMenuItem mnuOptionsFontColor = new JMenuItem("Font Color ...");
		mnuOptionsFontColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(null,
						"Choose the font color", screen.getForeground());
				// JColorChooser cc = new JColorChooser();
				// Color newColor = cc.showDialog(null, "Choose the font color",
				// screen.getForeground());
				if (newColor == null) {
					return;
				} else {
					screen.setForeground(newColor);
					terminalSettings.setScreenForeground(newColor);
				}
			}
		});
		mnuOptions.add(mnuOptionsFontColor);

		JMenu mnuSetUp = new JMenu("SetUp");
		menuBar.add(mnuSetUp);

		JMenu mnuUtility = new JMenu("Utilities");
		menuBar.add(mnuUtility);

		JCheckBoxMenuItem mnuUtilFullDuplex = new JCheckBoxMenuItem(
				"FullDuplex");
		mnuUtilFullDuplex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JCheckBoxMenuItem thisItem = (JCheckBoxMenuItem) ae.getSource();
				fullDuplex = thisItem.isSelected();
			}
		});
		mnuUtility.add(mnuUtilFullDuplex);

		JMenuItem mnuUtilClearScreen = new JMenuItem("Clear Screen");
		mnuUtilClearScreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				keyReceived((byte) 0X0C); // Form feed
			}
		});
		mnuUtility.add(mnuUtilClearScreen);

		JSeparator separator_4 = new JSeparator();
		mnuUtility.add(separator_4);

		JMenuItem mnuUtilFlushBuffers = new JMenuItem("Flush Buffers");
		mnuUtilFlushBuffers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int inCount = inputBuffer.size();
				int outCount = keyBoardBuffer.size();
				inputBuffer.clear();
				keyBoardBuffer.clear();
				String msg = String.format(
						"Input Buffer: %d bytes, keyBoardBuffer: %d bytes",
						inCount, outCount);
				JOptionPane.showMessageDialog(null, msg, "Flush Buffers",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		mnuUtility.add(mnuUtilFlushBuffers);
		frame.getContentPane().setLayout(null);

		JPanel panelConnection = new JPanel();
		panelConnection.setBorder(new BevelBorder(BevelBorder.RAISED, null,
				null, null, null));
		panelConnection.setBounds(4, 0, 805, 31);
		frame.getContentPane().add(panelConnection);
		panelConnection.setLayout(null);

		btnSettings = new JButton("Settings");
		btnSettings.setActionCommand("btnSettings");
		btnSettings.addActionListener(this);
		btnSettings.setBounds(709, 4, 89, 23);
		panelConnection.add(btnSettings);

		btnOpenClose = new JButton("Open");
		btnOpenClose.setActionCommand("btnOpenClose");
		btnOpenClose.addActionListener(this);
		btnOpenClose.setBounds(610, 4, 89, 23);
		panelConnection.add(btnOpenClose);

		lblCurrentSettings = new JLabel("Port-baudRate-ataBits-StopBits-Parity");
		lblCurrentSettings.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrentSettings.setBounds(369, 8, 216, 14);
		panelConnection.add(lblCurrentSettings);

		JButton btnTest = new JButton("Test");
		btnTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (serialPort != null) {
					// int[] status = serialPort.getLinesStatus();
					// screen.append(String.format("CTS  line status = %s%n",
					// status[0]));
					// screen.append(String.format("DSR  line status = %s%n",
					// status[1]));
					// screen.append(String.format("RING line status = %s%n",
					// status[2]));
					// screen.append(String.format("RLSD line status = %s%n",
					// status[3]));
				}

			}
		});
		btnTest.setName("btnTest");
		btnTest.setActionCommand("btnTest");
		btnTest.setBounds(10, 4, 89, 23);
		panelConnection.add(btnTest);

		JPanel panelScreen = new JPanel();
		panelScreen.setMaximumSize(new Dimension(814, 450));
		panelScreen.setBorder(new LineBorder(new Color(0, 0, 255)));
		panelScreen.setMinimumSize(new Dimension(814, 450));
		panelScreen.setBounds(new Rectangle(0, 32, 808, 457));
		frame.getContentPane().add(panelScreen);
		panelScreen.setLayout(null);

		screen = new JTextArea();
		screen.setEditable(false);
		screen.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent key) {
				processKeyTyped(key.getKeyChar());
			}
		});
		screen.setLineWrap(true);
		screen.setFont(new Font("Courier New", Font.PLAIN, 16));
		screen.setText("1234567890123456789012345678901234567890123456789012345678901234567890123456789012\r\n3\r\n4\r\n5\r\n6\r\n7\r\n8\r\n9\r\n10\r\n11\r\n12\r\n13\r\n14\r\n15\r\n16\r\n17\r\n18\r\n19\r\n20\r\n21\r\n22\r\n23\r\n24\r\n25\r\n");
		screen.setBackground(Color.LIGHT_GRAY);
		screen.setBounds(3, 1, 803, 455);
		panelScreen.add(screen);

		JPanel panelStatus = new JPanel();
		panelStatus.setBackground(SystemColor.control);
		panelStatus.setBounds(2, 490, 805, 20);
		panelStatus.setMaximumSize(new Dimension(800, 450));
		panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,
				null, null));
		panelStatus.setMinimumSize(new Dimension(10, 20));
		frame.getContentPane().add(panelStatus);
		panelStatus.setLayout(null);

		lblScreenPositionInfo = new JLabel("Row :");
		lblScreenPositionInfo.setForeground(new Color(0, 0, 128));
		lblScreenPositionInfo.setHorizontalAlignment(SwingConstants.LEFT);
		lblScreenPositionInfo.setBounds(10, 3, 220, 14);
		panelStatus.add(lblScreenPositionInfo);

		lblLastByteSent = new JLabel("Last Byte Sent");
		lblLastByteSent.setHorizontalAlignment(SwingConstants.LEFT);
		lblLastByteSent.setForeground(new Color(0, 0, 128));
		lblLastByteSent.setBounds(259, 3, 220, 14);
		panelStatus.add(lblLastByteSent);
	}

	// Static values
	// private static int LOCATION_X = 2000;
	// private static int LOCATION_Y = 250;
	private static String BTN_OPEN = "Open";
	private static String BTN_CLOSE = "Close";
	/*
	 * Constants and Variable for the terminal proper
	 */
	// private final static String LF = "\n";
	private final static String DEFAULT_STATE_FILE = "defaultTerminalSettings";
	private final static String FILE_SUFFIX = "ser";
	private final static String FILE_SUFFIX_PERIOD = "." + FILE_SUFFIX;

	private JButton btnOpenClose;
	private JButton btnSettings;
	private JLabel lblCurrentSettings;
	private JTextArea screen;
	private JFrame frame;
	public SerialPort serialPort;
	private TerminalSettings terminalSettings;

	String currentSettingsFileName;
	private JMenuItem mnuFileLoad;
	private JMenuItem mnuFileDefaultSettings;

	/*
	 * COnstants and Variables for the screen operation
	 */
	private static int MAX_ROWS = 24;
	private static int MAX_COLUMNS = 80;
	// private static int MAX_LINE_SIZE = MAX_COLUMNS + 1; // include /r
	private static int MAX_CHARACTERS = MAX_ROWS * MAX_COLUMNS;
	// private static int MAX_CONTENTS = (MAX_LINE_SIZE * MAX_ROWS); // -1
	private static int ROW_23_COLUMN_0 = (MAX_CHARACTERS - MAX_COLUMNS) - 1;
	private static int DEFAULT_TAB_WIDTH = 10;

	// private static String CR = "\r";
	private static String SPACE = " ";
	private static Byte SPACE_BYTE = 0X20;
	private static String allPrintableCharactersPattern = "^([a-zA-Z0-9!@#$%^&amp;*()-_=+;:'&quot;|~`&lt;&gt;?/{}]{1,1})$";
	private Pattern patternForPrintableChars;
	private Matcher matcher;

	private boolean fullDuplex = false;

	public int count = 0;

	private int tabWidth = DEFAULT_TAB_WIDTH;
	private int row = 0;
	private int col = 0;

	private int lastCaretPosition = 0;

	Queue<Byte> keyBoardBuffer;
	Queue<Byte> inputBuffer;
	private JLabel lblScreenPositionInfo;
	private JLabel lblLastByteSent;

	public void byteFromCPU(Byte address, Byte value) {
		// TODO Auto-generated method stub

	}

	public byte byteToCPU(Byte address) {
		// TODO Auto-generated method stub
		return 0;
	}
}
