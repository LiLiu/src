
//import statement
import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Toolkit;  

/**
	Author: LI LIU & ANDREW BILGORE & ANDREW STERNGLASS
	Description: The client will be able to select TCP or UDP protocol to establish a connection with the server,
	sends a message to the server and display the response.
	Last modified date: 11/10/2013.
*/

public class Client extends JFrame implements ActionListener
{
	/****************gloabal variables*****************************/
	Toolkit tk = Toolkit.getDefaultToolkit();
	private int screenWidth = (int) tk.getScreenSize().getWidth();
	private String ipAddress, userName;
	private String connectionStatus = "ON";
	private int PORT;
	private String connectionType;
	//TCP socket variables
	private Socket TCPsocket;
	private BufferedReader TCPin;
	private PrintWriter TCPout;
	
	//UDP socket variables
	private DatagramSocket UDPsocket;
	private InetAddress HOST;
	byte[] bin;
	
	/****************ConnectionFrame variables*********************/
	private JFrame connectionFrame;
	private JTextField nameField, ipField, portField;
	private JComboBox<String> connectionComBox;
	
	/****************Client GUI Frame Variables********************/
	private JTextArea chatArea, inputArea, userArea;
	private JTextField GUInameField;
	private JButton sendButton, reconnectButton;
	private JLabel statusState;

	/**
		Create client connection GUI
	*/
	public Client()
	{	
		//Client connection GUI
		connectionFrame = new JFrame();
		//title panel
		JLabel titleLabel = new JLabel("TCP/UDP Networking Program");			connectionFrame.add(titleLabel);					titleLabel.setBounds(110, 10, 500, 50);		//titlePanel.setBackground(Color.green);
		titleLabel.setForeground(Color.blue);									titleLabel.setFont(new Font(Font.SERIF, Font.BOLD, 30));
		//userName 
		JLabel userName = new JLabel("Username:");								connectionFrame.add(userName);						userName.setBounds(50, 60, 100, 50);		
		nameField = new JTextField(25);											connectionFrame.add(nameField);						nameField.setBounds(130, 76, 150, 20); 
		//ip address
		JLabel ipAddress = new JLabel("IP Address:");							connectionFrame.add(ipAddress);						ipAddress.setBounds(320, 76, 100, 20);
		ipField = new JTextField(20);											connectionFrame.add(ipField);						ipField.setBounds(400, 76, 150, 20);  	ipField.setText("localhost");
		//connection type
		JLabel connectionTypeLabel = new JLabel("Connection Type:");			connectionFrame.add(connectionTypeLabel); 			connectionTypeLabel.setBounds(50, 110, 100, 30);
		String[] connectType = {"TCP protocol", "UDP protocol"};
		connectionComBox = new JComboBox<String>(connectType);					connectionFrame.add(connectionComBox); 				connectionComBox.setBounds(158, 115, 120, 20);
		//port number
		JLabel portLabel = new JLabel("Port Number:");							connectionFrame.add(portLabel);						portLabel.setBounds(320, 115, 100, 20);
		portField = new JTextField(20);											connectionFrame.add(portField); 					portField.setBounds(400, 115, 150, 20);	 portField.setText("16888");
		
		//connect button
		JButton connectButton = new JButton("Connect");							connectionFrame.add(connectButton);					connectButton.setBounds(250, 180, 100, 30);			connectButton.addActionListener(this);
		//-------------------------------------------------------Frame Attribute---------------------------------------------------------
		connectionFrame.setLayout(null); connectionFrame.setVisible(true); connectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); connectionFrame.setSize(630,280); 
		connectionFrame.setLocation((screenWidth-630)/2,350); connectionFrame.setTitle("Client");
	}
	
	/**
	 * Client main GUI frame
	 */
	public void GUI()
	{
		//-------------------------------------------------------Top Panel---------------------------------------------------------------
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));								add(topPanel); 								topPanel.setBounds(5,10,800,35);	//topPanel.setBackground(Color.red);						
		
		JLabel nameLabel = new JLabel("User Name");					topPanel.add(nameLabel);									
		GUInameField = new JTextField(10);							topPanel.add(GUInameField);						GUInameField.setEditable(false);			GUInameField.setText(userName);					
		reconnectButton = new JButton("Disconnect");				topPanel.add(reconnectButton);					reconnectButton.addActionListener(this);
		reconnectButton.setActionCommand("reconnect");
		JLabel status = new JLabel("Connection Status: ");			topPanel.add(status);
		statusState = new JLabel("ON");								topPanel.add(statusState);						statusState.setForeground(Color.red);
		
		//-------------------------------------------------------User List Area----------------------------------------------------------
		JLabel userLabel = new JLabel("Current Users");				add(userLabel);									userLabel.setBounds(520, -140, 150, 380);
		userArea = new JTextArea(20,20);									userArea.setEditable(false);				userArea.setBackground(new Color(135, 206, 250));							
		JScrollPane userScroll = new JScrollPane(userArea);			add(userScroll);								userScroll.setBounds(520, 60, 150, 350);  	
		//-------------------------------------------------------Chat Area---------------------------------------------------------
		JLabel chatLabel = new JLabel("Chat Area");					add(chatLabel);									chatLabel.setBounds(5, 35, 100, 35);
		chatArea = new JTextArea(12,20);																		 	chatArea.setBackground(new Color(152,251,152));																																			chatArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(chatArea);				add(scroll);									scroll.setBounds(5, 60, 500, 220);
		JLabel inputLabel = new JLabel("Input Area");				add(inputLabel);								inputLabel.setBounds(5, 270, 100, 35);	
		inputArea = new JTextArea(50,20);								add(inputArea);								inputArea.setBounds(5, 300, 500, 110);	 inputArea.setBackground(new Color(205,201,201));		
		sendButton = new JButton("Send");								add(sendButton);							sendButton.setBounds(220, 420, 80, 25); sendButton.addActionListener(this);
		sendButton.requestFocus(true);
		//-------------------------------------------------------Frame Attribute---------------------------------------------------------
		setLayout(null); setVisible(true); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setSize(700,500); setLocation((screenWidth-700)/2,250); 
		setTitle("Client");
	}

	/**
		Thread that handles input messages from server.
	*/
	class ThreadClient extends Thread
	{
		public void run()
		{
			if(isTCP())
			{
				String line;
				while(TCPsocket != null)
				{
					if(TCPsocket.isClosed()) //if socket is closed
					{
						break;
					}
					else
					{	
						try
						{
							if((line = TCPin.readLine()) != null) //if message is not empty
							{
								if(line.indexOf("[") != -1) //user joined in
								{
									String userList = line.substring(1, line.length() - 2); //split messages
									String[] userListSplit = userList.split(",");			//get user list
									
									userArea.setText(""); 
									for(int i = 0; i < userListSplit.length; i++)
									{
										userArea.append(userListSplit[i]+"\n");		//update user list content
									}					
								}
								else if(line.equals("DISCONNECTserver")) // if disconnect message is detected
								{
									try
									{
										userArea.setText("Server disconnected!");
										TCPsocket.close();	
										chatArea.append("Server disconnected!!");
									}
									catch(IOException ex)
									{
										System.out.println("Exception error occured when disconnecting the socket");
									}	
								}	
								else //normal message comes in
								{
									chatArea.append(line + "\n");
								}
							}
						}
						catch(Exception ex)
						{	
							chatArea.append("Server/Client disconnected!\n");
							userArea.setText("Offline");
							break;
						}
					}			
				}//end of while
			}//end of TCP 
			else//if it is UDP protocol
			{
				String line;
				while(UDPsocket != null)
				{
					if(UDPsocket.isClosed()) //if socket is closed
					{
						break;
					}
					else
					{	
						try
						{
							//RECEIVE
							DatagramPacket reply = new DatagramPacket(new byte[1024], 1024);
							UDPsocket.receive(reply);
							
							line = new String(reply.getData()); 
							
							if(line != null) //if message is not empty
							{
								if(line.indexOf("[") != -1) //user joined in
								{
									String userList = line.substring(1, line.length() - 2); //split messages
									String[] userListSplit = userList.split(",");			//get user list
									
									userArea.setText(""); 
									for(int i = 0; i < userListSplit.length; i++)
									{
										userArea.append(userListSplit[i]+"\n");		//update user list content
									}					
								}
								else if(line.equals("DISCONNECTserver")) // if disconnect message is detected
								{
									try
									{
										userArea.setText("Server disconnected!");
										TCPsocket.close();	
										chatArea.append("Server disconnected!!");
									}
									catch(IOException ex)
									{
										System.out.println("Exception error occured when disconnecting the socket");
									}	
								}	
								else //normal message comes in
								{
									chatArea.append(line + "\n");
								}
							}
						}
						catch(Exception ex)
						{	
							chatArea.append("Server/Client disconnected!\n");
							userArea.setText("Offline");
							break;
						}
					}			
				}//end of while
			}
		}
	}
	
	/**
		Action listener implemenation
	*/
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getActionCommand().equals("Send"))
		{
			send();
		}
		else if(ae.getActionCommand().equals("Connect"))
		{
			if(inputValidation())
			{
				connectionFrame.setVisible(false);
				GUI();
				connect();
			}	
		}
		else if(ae.getActionCommand().equals("reconnect"))
		{
			//to disconnect
			if(connectionStatus.equals("ON"))
			{
				reconnectButton.setText("Reconnect");
				connectionStatus = "OFF";
				statusState.setText(connectionStatus);
				disconnect();
			}
			//to reconnect
			else{
				reconnectButton.setText("Disconnect");
				connectionStatus = "ON";
				statusState.setText(connectionStatus);
				connect();
			}
		}
	}	
	
	/**
	*	send messages to server
	*/
	public void send()
	{
		String str = inputArea.getText().trim();
		
		if(str.length() != 0)
		{
			if(isTCP())
			{
				chatArea.append(userName + ">>> " + str + "\n");
				TCPout.println(userName + ">>> " + str);
				TCPout.flush();
				inputArea.setText("");
			}
			else
			{
				try
				{
					chatArea.append(userName + ">>> " + str + "\n");
					bin = (userName + ">>> " + str).getBytes();
					
					//SEND 
					UDPsocket.send(new DatagramPacket(bin, bin.length, HOST, PORT));
					inputArea.setText("");
				}
				catch(Exception ex)
				{}
			}
		}
	}

	/**
	 * Establish connection
	 */
	public void connect()
	{
		//if it is TCP protocol
		if(isTCP())
		{
			try
			{
				TCPsocket = new Socket(ipAddress, PORT); 	//build a client socket using current ip address
				TCPin = new BufferedReader(new InputStreamReader(TCPsocket.getInputStream()));
				TCPout = new PrintWriter(TCPsocket.getOutputStream());
				ThreadClient thc = new ThreadClient();  		thc.start(); //start thread
				
				TCPout.println(userName + "*\n>>> " + userName + " connected with the server via " + connectionType + ".");		//send new name to server
				TCPout.flush();	
			}
			catch(Exception ex)
			{
				System.out.println("Exception occured in connect() TCP protocol");
			}
		}
		//if it is UDP protocol
		else
		{
			try
			{
				//JOptionPane.showMessageDialog(null, "connect() UDP");
				UDPsocket = new DatagramSocket();
				bin = (userName + "*").getBytes();
				HOST = InetAddress.getByName(ipAddress);
				ThreadClient thc = new ThreadClient();  		thc.start(); //start thread
				
				//SEND 
				UDPsocket.send(new DatagramPacket(bin, bin.length, HOST, PORT));
				bin = (">>> " + userName + " connected with the server via " + connectionType + ".").getBytes();
				UDPsocket.send(new DatagramPacket(bin, bin.length, HOST, PORT));
				//JOptionPane.showMessageDialog(null, "connect() END");
			}
			catch(Exception ex)
			{
				System.out.println("Exception occured in connect() UDP protocol");
			}
		}
	}
	
	/**
	*	disconnect current client from server
	*/
	public void disconnect()
	{
		/*
		try
		{
			userArea.setText("You exited the chat room\n");
			disconnectButton.setEnabled(false);	
			connectButton.setEnabled(true);
			out.println(name + "!-!"); 									//send the removed name from user list  
			out.flush(); out.close();
			socket.close();	
		
		}
		catch(IOException ex)
		{
			System.out.println("Exception error occured when disconnect the socket");
		}	*/
	}
	
	/**
	 * Validate user input field and prompt error message
	 */
	public boolean inputValidation()
	{
		boolean validate = false;
		
		//validate user name input, can't be empty
		if(nameField.getText().length() > 0)
		{
			//validate ip address
			userName = nameField.getText().trim();
			if(ipField.getText().length() > 0)
			{
				String[] ipsection = ipField.getText().split("\\.");
				
				if(ipField.getText().trim().equals("localhost") || ipsection.length==4)
				{
					ipAddress = ipField.getText().trim();
					
					//validate port number
					try{
						PORT = Integer.parseInt(portField.getText().trim());
						
						if((PORT > 1024) && (PORT <= 49151))
						{
							validate = true;
							connectionType = connectionComBox.getSelectedItem().toString();
						}
						else
						{
							JOptionPane.showMessageDialog(null, "Port number must be an integer between 1024 and 49151");
							validate = false;
						}
					}
					catch(Exception ex)
					{
						JOptionPane.showMessageDialog(null, "Port number must be an integer between 1024 and 49151. Please try again");
						validate = false;
					}					
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Sorry, IP field must be 'localhost' or follow xxx.xxx.xxx.xxx format. Please try again.");
					
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Sorry, IP field can not be empty, please try again.");
				validate = false;
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Sorry, user name can not be empty, please try again.");
			validate = false;
		}
		return validate;
	}
	
	/**
	 * @return true if the connection protocol is TCP, false if it is UDP protocol.
	 */
	public boolean isTCP()
	{
		if(connectionComBox.getSelectedItem().equals("TCP protocol"))
		{
			return true;			
		}
		else
		{
			return false;			
		}		
	}
	
	public static void main(String[] args)
	{
		new Client();
	}
}