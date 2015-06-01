package terminal;


import java.awt.FlowLayout;
//import java.awt.Dialog.ModalityType;


import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import jssc.SerialPortList;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class PortSetupDetails extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private TerminalSettings ts;

	
	public void showDialog() {
		this.setVisible(true);
		return;
	}

	private void doAppInit() {

		String[] ports = SerialPortList.getPortNames();
		for (String port : ports) {
			cbPort.addItem(port);
		}
//		cbPort.addItem("COM1");
		// set settings
		cbPort.setSelectedItem(ts.getPortName());
		cbBaudRate.setSelectedItem("" + ts.getBaudRate());
		cbDataBits.setSelectedItem("" + ts.getDataBits());
		switch (ts.getStopBits()) {
		case 1:
			cbStopBits.setSelectedItem("1");
			break;
		case 2:
			cbStopBits.setSelectedItem("2");
			break;
		case 3:
			cbStopBits.setSelectedItem("1.5");
			break;
		}
		cbParity.setSelectedIndex(ts.getParity());

		cbPort.requestFocus();
	}

	/**
	 * Create the dialog.
	 */
	public PortSetupDetails( TerminalSettings ts) {
		this.ts = ts;
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(false);
		setTitle("Setup Serial Connection");
		setBounds(100, 100, 612, 199);
		getContentPane().setLayout(null);
		contentPanel.setBounds(0, 0, 587, 148);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);

		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(49, 28, 46, 14);
		contentPanel.add(lblPort);

		cbPort = new JComboBox<String>();
		cbPort.setBounds(35, 58, 75, 20);
		contentPanel.add(cbPort);

		JLabel lblBaudRate = new JLabel("Baud rate");
		lblBaudRate.setBounds(151, 28, 62, 14);
		contentPanel.add(lblBaudRate);

		cbBaudRate = new JComboBox<Integer>();
		cbBaudRate.setModel(new DefaultComboBoxModel(new String[] { "110", "300", "600", "1200", "4800", "9600",
				"14400", "19200", "38400", "57600", "115200" }));
		cbBaudRate.setMaximumRowCount(13);
		cbBaudRate.setBounds(145, 58, 75, 20);
		contentPanel.add(cbBaudRate);

		cbDataBits = new JComboBox();
		cbDataBits.setModel(new DefaultComboBoxModel(new String[] { "5", "6", "7", "8" }));
		cbDataBits.setBounds(255, 58, 75, 20);
		contentPanel.add(cbDataBits);

		cbStopBits = new JComboBox();
		cbStopBits.setModel(new DefaultComboBoxModel(new String[] { "1", "1.5", "2" }));
		cbStopBits.setBounds(365, 58, 75, 20);
		contentPanel.add(cbStopBits);

		cbParity = new JComboBox();
		cbParity.setModel(new DefaultComboBoxModel(new String[] { "None", "Odd", "Even", "Mark", "Space" }));
		cbParity.setBounds(475, 58, 75, 20);
		contentPanel.add(cbParity);

		JLabel lblParity = new JLabel("Parity");
		lblParity.setBounds(489, 28, 46, 14);
		contentPanel.add(lblParity);

		JLabel lblDataBits = new JLabel("Data bits");
		lblDataBits.setBounds(269, 28, 46, 14);
		contentPanel.add(lblDataBits);

		JLabel lblStopBits = new JLabel("Stop bits");
		lblStopBits.setBounds(379, 28, 46, 14);
		contentPanel.add(lblStopBits);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBounds(43, 102, 512, 33);
			contentPanel.add(buttonPane);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			{
				JButton btnSave = new JButton("Save");
				btnSave.addActionListener(this);
				btnSave.setActionCommand("btnSave");
				buttonPane.add(btnSave);
				getRootPane().setDefaultButton(btnSave);
			}
			{
				JButton btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(this);
				btnCancel.setActionCommand("btnCancel");
				buttonPane.add(btnCancel);
			}
		}
		doAppInit();
	}

	private Terminal terminal;
	private JComboBox<String> cbPort;
	private JComboBox<Integer> cbBaudRate;
	private JComboBox<Integer> cbDataBits;
	private JComboBox<String> cbStopBits;
	private JComboBox<String> cbParity;

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "btnCancel":
			dispose();
			break;
		case "btnSave":
			ts.setPortName(cbPort.getSelectedItem().toString());
			ts.setBaudRate(Integer.valueOf(cbBaudRate.getSelectedItem().toString()));
			ts.setDataBits(Integer.valueOf(cbDataBits.getSelectedItem().toString()));
			
			switch(cbStopBits.getSelectedItem().toString()){
			case "1":
				ts.setStopBits(1);
				break;
			case "1.5":
				ts.setStopBits(3);
				break;
			case "2":
				ts.setStopBits(2);
			}//switch stopBits
			ts.setParity(cbParity.getSelectedIndex());
			break;
		}

	}
}
