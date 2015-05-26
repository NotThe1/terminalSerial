package terminal;

import java.awt.Color;
import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Arrays;

public class TerminalSettings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String portName;
	private int baudRate;
	private int dataBits;
	private int stopBits;
	private int parity;
	private Color screenForeground;
	private Color screenBackground;

	public TerminalSettings() {

	}// Constuctor

	TerminalSettings(String portName, int baudRate, int dataBits, int stopBits, int parity) {
		this.setPortName(portName);
		this.setBaudRate(baudRate);
		this.setDataBits(dataBits);
		this.setStopBits(stopBits);
		this.setParity(parity);
	}// Constructor

	TerminalSettings(String portName, int baudRate, int dataBits, int stopBits, int parity, Color foreGround,
			Color backGround) {
		this(portName, baudRate, dataBits, stopBits, parity);
		this.setScreenBackground(backGround);
		this.setScreenForeground(foreGround);
	}// Constructor

	public void setDefaultSettings() {
		this.setPortName("COM1");
		this.setBaudRate(9600);
		this.setDataBits(8);
		this.setStopBits(1);
		this.setParity(0);
		setDefaultScreenColors();
	}

	public void setDefaultScreenColors() {
		this.setScreenColors(Color.BLACK, Color.LIGHT_GRAY);
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public int getBaudRate() {
		return baudRate;
	}// getBaudRate

	public void setBaudRate(int baudRate) {
		// if (VALID_BAUD_RATES.contains(baudRate)) {
		// this.baudRate = baudRate;
		// } else {
		// this.baudRate = 9600; // default
		// }// if
		this.baudRate = baudRate;
	}// setBaudRate

	public int getDataBits() {
		return dataBits;
	}// getDataBits

	public void setDataBits(int dataBits) {
		if ((dataBits >= 5) && (dataBits <= 8)) {
			this.dataBits = dataBits;
		} else {
			this.dataBits = 8; // default
		}// if
	}// setDataBits

	public int getStopBits() {
		return stopBits;
	}// getStopBits

	public void setStopBits(int stopBits) {
		if ((stopBits >= 1) && (stopBits <= 3)) {
			this.stopBits = stopBits;
		} else {
			this.stopBits = 1; // default
		}// if }
	}// setStopBits

	public int getParity() {
		return parity;
	}// getParity

	public void setParity(int parity) {
		if ((parity >= 0) && (parity <= 5)) {
			this.parity = parity;
		} else {
			this.parity = 0; // default
		}// if
	}// setParity

	public Color getScreenForeground() {
		return screenForeground;
	}// getScreenForeground

	public void setScreenForeground(Color screenForeground) {
		this.screenForeground = screenForeground;
	}// setScreenForeground

	public Color getScreenBackground() {
		return screenBackground;
	}// getScreenBackground

	public void setScreenBackground(Color screenBackground) {
		this.screenBackground = screenBackground;
	}// setScreenBackground

	public void setScreenColors(Color screenForeground, Color screenBackground) {
		setScreenForeground(screenForeground);
		setScreenBackground(screenBackground);
	}// setScreenColors

	// private static final ArrayList<Integer> VALID_BAUD_RATES =
	// (ArrayList<Integer>) Arrays.asList(110, 300, 600, 1200,
	// 4800, 9600, 14400, 19200, 38400, 57600, 115200);
}
