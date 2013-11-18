
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
	Description: The multithreaded server will be able to concurrently handle both 
	TCP & UDP client connections, reply messages back to clients, and establish new client connections.
	Last modified date: 11/10/2013.
*/

public class Server extends JFrame implements ActionListener
{
	
	/********************gloabal variables*************************/
	Toolkit tk = Toolkit.getDefaultToolkit();
	private int screenWidth = (int) tk.getScreenSize().getWidth();
	private int screenHeight = (int) tk.getScreenSize().getHeight();
	private int PORT;
	private String ipAddress;
	private JTextArea chatArea, inputArea, TCParea, UDParea;
	
	//port frame
	private JFrame portFrame;
	private JTextField portField;
	
	//data structure variables
	private ArrayList<Socket> TCPsocketArray = new ArrayList<Socket>();
	private ArrayList<DatagramPacket> UDPpacketArray = new ArrayList<DatagramPacket>();
	private ArrayList<String> TCPuserArray = new ArrayList<String>();
	private ArrayList<String> UDPuserArray = new ArrayList<String>();
	
	//GUI variables
	private JLabel upTimeLabel;
	private JLabel status;
	
	//server variables
	private ServerSocket TCPServerSocket;
	private DatagramSocket UDPSocket;
	
	/**
		Create server GUI and start server connection
	*/
	public Server()
	{	
		portFrame = new JFrame();
		JLabel portLabel = new JLabel("Please input a server port number (1024 - 49151):");			portFrame.add(portLabel);		portLabel.setBounds(20, 15, 400, 25);
		portField = new JTextField(15);										portFrame.add(portField);								portField.setBounds(310, 18, 100, 20);	portField.setText("16888");
		JButton portConfirm = new JButton("Run Server");					portFrame.add(portConfirm);								portConfirm.setBounds(160, 60, 100, 25);
		portConfirm.addActionListener(this);
		//-------------------------------------------------------Frame Attribute---------------------------------------------------------
		portFrame.setLayout(null); portFrame.setVisible(true); portFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); portFrame.setSize(450, 150);  	  
		portFrame.setTitle("Server Port Number?"); portFrame.setLocation((screenWidth-400)/2, (screenHeight-200)/2);
	}
	
	public void GUI()
	{
		//create 																//add												//set
		//-------------------------------------------------------Top Panel---------------------------------------------------------------
		JMenuBar mb = new JMenuBar();										setJMenuBar(mb);
		//Server
		JMenu mn1 = new JMenu("Server");									mn1.setMnemonic('S');									mb.add(mn1);
		JMenuItem mi1 = new JMenuItem("Reconnect");							mi1.setMnemonic('R');									mn1.add(mi1);			mi1.addActionListener(this);
		JMenuItem mi2 = new JMenuItem("Disconnect");						mi2.setMnemonic('D');									mn1.add(mi2);			mi2.addActionListener(this);
		JMenuItem mi3 = new JMenuItem("Exit");								mi3.setMnemonic('E');									mn1.add(mi3);			mi3.addActionListener(this);
		
		//Settings
		JMenu mn2 = new JMenu("Settings");									mn2.setMnemonic('g');									mb.add(mn2);
		JMenuItem mi4 = new JMenuItem("Server setting");					mi4.setMnemonic('m');									mn2.add(mi4);			mi4.addActionListener(this);
		JMenuItem mi5 = new JMenuItem("Broadcast");							mi5.setMnemonic('F');									mn2.add(mi5);			mi5.addActionListener(this);
		//Tool
		JMenu mn3 = new JMenu("Tools");										mn3.setMnemonic('T');									mb.add(mn3);
		JMenuItem mi6 = new JMenuItem("Statistics");						mi6.setMnemonic('a');									mn3.add(mi6);			mi6.addActionListener(this);
		//About
		JMenu mn4 = new JMenu("Help");										mn4.setMnemonic('H');									mb.add(mn4);
		JMenuItem mi7 = new JMenuItem("About");								mi7.setMnemonic('A');									mn4.add(mi7);			mi7.addActionListener(this);
		JMenuItem mi8 = new JMenuItem("Contact");							mi8.setMnemonic('C');									mn4.add(mi8);			mi8.addActionListener(this);
		//mi8.setEnabled(false);
		//--------------------------------------------------------Main GUI----------------------------------------------------------------
		Font font = new Font(Font.SERIF, Font.BOLD, 20);
		//up time label
		JLabel serverUpLabel = new JLabel("Server Up Time: ");				add(serverUpLabel);										serverUpLabel.setBounds((screenWidth-960)/2 + 100, 20, 200, 25);
		serverUpLabel.setFont(font);										serverUpLabel.setForeground(Color.white);
		upTimeLabel = new JLabel("00:00");							add(upTimeLabel);										upTimeLabel.setBounds((screenWidth-960)/2 + 260, 20, 60, 25); upTimeLabel.setFont(font); upTimeLabel.setForeground(Color.white);
		
		//server status
		JLabel serverStatus = new JLabel("Server Status:");					add(serverStatus);										serverStatus.setBounds((screenWidth-960)/2 + 460, 20, 200, 25);
		status = new JLabel("ON");									add(status);											status.setFont(new Font(Font.SERIF, Font.BOLD, 20)); status.setBounds((screenWidth-960)/2 + 590, 20, 100, 25);
		serverStatus.setForeground(Color.white);							serverStatus.setFont(font);								status.setForeground(Color.green);
		//server log label
		JLabel serverLog = new JLabel("Server log");						add(serverLog);											serverLog.setBounds((screenWidth-960)/2, 53, 100, 25);		serverLog.setForeground(Color.white);	
		//chat area
		chatArea = new JTextArea(12,20);									add(chatArea);											chatArea.setBackground(Color.yellow);
		JScrollPane chatScroll = new JScrollPane(chatArea);					add(chatScroll);										chatScroll.setBounds((screenWidth-960)/2, 80, 500, 500); 		chatArea.setEditable(false);
		//input area
		JLabel inputLabel = new JLabel("Input Area");						add(inputLabel);										inputLabel.setBounds((screenWidth-960)/2, 585, 100, 25);		inputLabel.setForeground(Color.white);
		inputArea = new JTextArea(12, 20);									add(inputArea);											inputArea.setBackground(Color.white);							
		JScrollPane inputScroll = new JScrollPane(inputArea);				add(inputScroll);										inputScroll.setBounds((screenWidth-960)/2, 610, 500, 200);
		//Broadcast button
		JButton sendButton = new JButton("Broadcast");						add(sendButton);										sendButton.setBounds((screenWidth-560)/2, 830, 100, 25);   sendButton.addActionListener(this);
		sendButton.requestFocus(true);				
		//TCP area
		JLabel TCPlabel = new JLabel("TCP Users");							add(TCPlabel);											TCPlabel.setBounds((screenWidth-960)/2+530, 53, 100, 25); 	TCPlabel.setForeground(Color.white);
		TCParea = new JTextArea(20, 17);									add(TCParea);											TCParea.setBackground(new Color(205,51,51));					TCParea.setForeground(Color.white);
		JScrollPane TCPscroll = new JScrollPane(TCParea);					add(TCPscroll);											TCPscroll.setBounds((screenWidth-960)/2+530, 80, 200, 730);		TCParea.setEditable(false);
		
		//UDP area
		JLabel UDPlabel = new JLabel("UDP Users");							add(UDPlabel);											UDPlabel.setBounds((screenWidth-960)/2+760, 53, 100, 25); 	UDPlabel.setForeground(Color.white);
		UDParea = new JTextArea(20, 17);									add(UDParea);											UDParea.setBackground(new Color(138,43,226));				UDParea.setForeground(Color.white);
		JScrollPane UDPscroll = new JScrollPane(UDParea);					add(UDPscroll);											UDPscroll.setBounds((screenWidth-960)/2+760, 80, 200, 730); 	UDParea.setEditable(false);
		
		//-------------------------------------------------------Frame Attribute---------------------------------------------------------
		setLayout(null); setVisible(true); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setSize(screenWidth, screenHeight);  	getContentPane().setBackground(new Color(105,105,105));  
		setTitle("Server");
	}
	
	/*
	 * TCP Server loop
	 */
	class TCPServer extends Thread 
	{
		public void run()
		{
			try
			{
				//display server message to chatArea
				chatArea.append(">>>TCP server is running. Wait for connections at PORT: " + PORT + " and IP: " + ipAddress + ".\n");
				TCPServerSocket = new ServerSocket(PORT); 
				
				while(true)
				{
					//TCP
					Socket TCPsocket = TCPServerSocket.accept();				//wait and accept new client
					TCPThreadConnection thread = new TCPThreadConnection(TCPsocket);
					thread.start();	
				}
			}
			catch(Exception e)
			{
				System.out.println("TCP SERVER has problem: " + e);
			}
		}
	}
	
	class TCPThreadConnection extends Thread 
	{
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out; 
		private String msg = null;		
		
		public TCPThreadConnection(Socket _soc)
		{
			socket = _soc;
			TCPsocketArray.add(socket);
		}
		
		public void run()
		{
			try
			{
				in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //get the input method from the TCP socket
				out = new PrintWriter(socket.getOutputStream());	//get the output method from the TCP socket
				
				while(socket != null) //if socket is open    !(socket.isClosed()
				{
					try
					{
						msg = in.readLine();
						
						if(msg.indexOf("*") != -1) // if new name is added
						{
							//TCPout.println(userName + "*\n>>> " + userName + " connected with the server via " + connectionType + ".");
							int i = msg.indexOf("*");  //find the name symbol index in the string
							String newName = msg.substring(0,i); //get name   
							TCPuserArray.add(newName);
							
							updateTCPArea(); //update current tcp user list
							
							//sendUserList();   //send current user list to all clients
											
							
							
							
							//JOptionPane.showMessageDialog(null, "new user: " + TCPuserArray.get(0));
							
						}/*
						else if(msg.indexOf("!-!") != -1) //if detect remove symbol received
						{
							int e = msg.indexOf("!-!");									//get the remove symbol index	
							String nameToRemove = msg.substring(0,e);					//get remove name
							int removeIndex = userArray.indexOf(nameToRemove);  		//find remove name index in userArray	
							
							userArray.remove(removeIndex); 								//remove name from array List	
							socketArray.remove(removeIndex);							//remove socket from socketArray
							in.close();
							
							updateUserArea();
							sendUserList();

							for(Socket one : socketArray)		//tell other client someone exited
							{	
								PrintWriter writer = new PrintWriter(one.getOutputStream());
								writer.println(">>> " + nameToRemove + " exited the chatting room."); //send name to client
								writer.flush(); 
							}
							chatArea.append(">>> " + nameToRemove + " exited the chatting room.\n"); //notice current client the exited user
							break;
						}
						else //normal chat messages
						{   
							chatArea.append(msg + "\n");
							out.println("Server: " + msg.toUpperCase());
							out.flush();
						}
						*/
						else
						{
							chatArea.append(msg + "\n"); 
							
							int s = msg.indexOf(">");
							
							out.println("Server>>> " + msg.substring(s+3).toUpperCase());
							out.flush();
							
							/*
							for(Socket one : TCPsocketArray)
							{
								PrintWriter writer = new PrintWriter(one.getOutputStream());
								writer.println(msg); //send name to client
								writer.flush();
							}*/
						}
						 
					}
					catch(Exception ex)
					{
						System.out.println("error: " + ex);
					}	
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				System.out.println("Error occured in the tcp thread run(): " + ex);
			}
		}	
	}
	
	/**
	* server thread
	
	class ThreadServer extends Thread 
	{
		private Socket soc;
		
		public ThreadServer(Socket s)
		{
			soc = s;
			socketArray.add(s); //add new socket into arraylist
		}
		
		public void run()
		{
			BufferedReader in;
			String msg = null;
			
			try
			{
				in = new BufferedReader(new InputStreamReader(soc.getInputStream())); //get the reader
					
				while(soc != null) //if socket is open
				{	
					if(socketArray.size() == 0) // if no socket is in the array
					{
						System.out.println("There is no user currently on line.");
						break;
					}
					if(!(soc.isClosed())) //if socket is open
					{
						try
						{
							msg = in.readLine(); //from socket

							if(msg == null) 
							{		
								return;			
							} 
							else
							{
								if(msg.indexOf("*") != -1) // if new name is added
								{
									int i = msg.indexOf("*");  //find the name symbol index in the string
									String newName = msg.substring(0,i); //get name   
									userArray.add(newName);
									
									updateUserArea(); //update current user list
									sendUserList();   //send current user list to all clients
								}
								else if(msg.indexOf("!-!") != -1) //if detect remove symbol received
								{
									int e = msg.indexOf("!-!");									//get the remove symbol index	
									String nameToRemove = msg.substring(0,e);					//get remove name
									int removeIndex = userArray.indexOf(nameToRemove);  		//find remove name index in userArray	
									
									userArray.remove(removeIndex); 								//remove name from array List	
									socketArray.remove(removeIndex);							//remove socket from socketArray
									in.close();
									
									updateUserArea();
									sendUserList();

									for(Socket one : socketArray)		//tell other client someone exited
									{	
										PrintWriter writer = new PrintWriter(one.getOutputStream());
										writer.println(">>> " + nameToRemove + " exited the chatting room."); //send name to client
										writer.flush(); 
									}
									chatArea.append(">>> " + nameToRemove + " exited the chatting room.\n"); //notice current client the exited user
									break;
								}
								else //normal chat messages
								{
									chatArea.append(msg + "\n"); 
									for(Socket one : socketArray)
									{
										PrintWriter writer = new PrintWriter(one.getOutputStream());
										writer.println(msg); //send name to client
										writer.flush();
									}
								}
							}	
						}					
						catch(Exception ex) //handles client interrupted exception eg. exit incorrectly
						{
							int curSocIndex = socketArray.indexOf(soc); //find the current socket
							socketArray.remove(curSocIndex); //remove current socket
							
							for(Socket soc : socketArray)
							{
								PrintWriter out = new PrintWriter(soc.getOutputStream());
								out.println("A connection problem had occured in " + userArray.get(curSocIndex) + "'s computer"); //notify other client
								out.flush();	
							}
							chatArea.append(">>> A connection problem had occured in " + userArray.get(curSocIndex) + "'s computer\n"); //update current chat area
							userArray.remove(curSocIndex); //remove the broken socket from array list
							updateUserArea();
							sendUserList();		
							System.out.println("server expceiton");
						}
					}	
					else//if socket is closed
					{
						break;	
					}
				}// end of while loop
			}	
			catch(Exception ex)
			{
				if(socketArray.size() == 0)
				{
					System.out.println("No user is online");
					return;
				}
			}
		}
	}*/

	/**
	* UDP server loop
	*/
	class UDPServer extends Thread 
	{
		public void run()
		{
			try
			{
				chatArea.append(">>>UDP server is running. Wait for connections at PORT: " + PORT + " and IP: " + ipAddress + ".\n");
				UDPSocket = new DatagramSocket(PORT);
				
				while(true)
				{
					DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
					UDPSocket.receive(packet);
					//JOptionPane.showMessageDialog(null, "udpserver run(): received a packet");
					UDPThreadConnection thread = new UDPThreadConnection(packet);
					thread.start();	
				}
			}
			catch(Exception ex){
				System.out.println("Exception occured in UDPServer run()");
			}
		}
	}
	
	class UDPThreadConnection extends Thread 
	{
		private DatagramPacket packet;
		
		public UDPThreadConnection(DatagramPacket _packet)
		{
			packet = _packet;
			UDPpacketArray.add(packet);
			//JOptionPane.showMessageDialog(null, "inside UDPtrheadCOnnection cosntructor");
		}
		
		public void run()
		{
			String msg; 
			while(packet != null) //if socket is open    !(socket.isClosed()
			{
				try
				{
					//JOptionPane.showMessageDialog(null, "server udp while(packet!=null");
					//UDPSocket.receive(packet);
					msg = new String(packet.getData()); 
					
					
					
					//JOptionPane.showMessageDialog(null, "server msg: " + msg);
					if(msg.indexOf("*") != -1) // if new name is added
					{
						//JOptionPane.showMessageDialog(null, "inside server indexOf(*)");
						//TCPout.println(userName + "*\n>>> " + userName + " connected with the server via " + connectionType + ".");
						int i = msg.indexOf("*");  //find the name symbol index in the string
						String newName = msg.substring(0,i); //get name   
						UDPuserArray.add(newName);
						JOptionPane.showMessageDialog(null, "server NAME: " + newName);
						updateUDPArea(); //update current tcp user list
						
						//sendUserList();   //send current user list to all clients
										
						
						
						
						//JOptionPane.showMessageDialog(null, "new user: " + TCPuserArray.get(0));
						
					}/*
					else if(msg.indexOf("!-!") != -1) //if detect remove symbol received
					{
						int e = msg.indexOf("!-!");									//get the remove symbol index	
						String nameToRemove = msg.substring(0,e);					//get remove name
						int removeIndex = userArray.indexOf(nameToRemove);  		//find remove name index in userArray	
						
						userArray.remove(removeIndex); 								//remove name from array List	
						socketArray.remove(removeIndex);							//remove socket from socketArray
						in.close();
						
						updateUserArea();
						sendUserList();

						for(Socket one : socketArray)		//tell other client someone exited
						{	
							PrintWriter writer = new PrintWriter(one.getOutputStream());
							writer.println(">>> " + nameToRemove + " exited the chatting room."); //send name to client
							writer.flush(); 
						}
						chatArea.append(">>> " + nameToRemove + " exited the chatting room.\n"); //notice current client the exited user
						break;
					}
					*/
					else
					{						
						chatArea.append(msg + "\n"); 
						//JOptionPane.showMessageDialog(null, "server msg: " + msg);
						int s = msg.indexOf(">");
						
						byte[] bin = (("Server>>> " + msg.substring(s+3)).toUpperCase()).getBytes();
						UDPSocket.send(new DatagramPacket(bin, bin.length, packet.getAddress(), packet.getPort()));
						
						/**
						 *
						  	DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
							System.out.println("here");
							soc.receive(request);
							
							byte[] msg = ("Server reply: " + new String(request.getData())).getBytes();
							DatagramPacket reply = new DatagramPacket(msg, msg.length, request.getAddress(), request.getPort());
							soc.send(reply);
						 */
						
						/*
						UDPSocket
						out.println("Server>>> " + msg.substring(s+3).toUpperCase());
						out.flush();
						*/
						/*
						for(Socket one : TCPsocketArray)
						{
							PrintWriter writer = new PrintWriter(one.getOutputStream());
							writer.println(msg); //send name to client
							writer.flush();
						}*/
					}
				}
				catch(Exception ex)
				{
					System.out.println("error: " + ex);
				}	
			}
		}
	}
	
	/**
		update current user list
	*/
	public void updateTCPArea()
	{
		TCParea.setText("");
		for(int i = 0; i < TCPuserArray.size(); i ++)
		{
			TCParea.append(TCPuserArray.get(i) + "\n");
		}
	}

	/**
		update current user list
	*/
	public void updateUDPArea()
	{
		UDParea.setText("");
		for(int i = 0; i < UDPuserArray.size(); i ++)
		{
			UDParea.append(UDPuserArray.get(i) + "\n");
		}
	}
	
	/**
	 * send current user list to all clients
	 */
	public void sendUserList()
	{
		/*
		String userList = "[";
		for(int k = 0; k < userArray.size(); k ++)
		{
			userList += userArray.get(k) + ",";
		}userList += "]";
		
		for(Socket one : socketArray)
		{	
			try
			{
				PrintWriter writer = new PrintWriter(one.getOutputStream());
				writer.println(userList); //send name to client
				writer.flush();
			}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage() + " ---- 1");
			}
		}
		*/
	}
	
	/**
		@param ActionEvent ae
		listener method implementation
	*/
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getActionCommand().equals("Run Server"))
		{
			if(validatePort())
			{
				portFrame.setVisible(false);
				GUI();
				connect();
			}
		}
		
		/*
		if(ae.getActionCommand().equals("Broadcast")) //send messages to other clients
		{
			String str = inputArea.getText().trim(); //get string from input area
			
			if(str.length() != 0)
			{						
				for(Socket one : socketArray)	
				{	
					try
					{
						PrintWriter writer = new PrintWriter(one.getOutputStream()); 
						writer.println("Host >>> " + str); //send message to clients
						writer.flush();
					}
					catch(Exception ex)
					{
						System.out.println(ex.getMessage() + " send method");
					}
				}
				inputArea.setText("");	
				chatArea.append("Host >>> " + str + "\n");
			}
		}
		else if(ae.getActionCommand().equals("Disconnect")) //disconnect connection
		{
			try
			{		
				disconnectButton.setEnabled(false);
				statusField.setText("Disconnected");
				for(Socket soc : socketArray)
				{
					PrintWriter out = new PrintWriter(soc.getOutputStream());
					out.println("DISCONNECTserver"); //send messages to clients
					out.flush();
				}			
				System.out.println("Disconnect clicked");
			}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage() + " disconnect ex");
			} 	
		}
		*/
	}
	
	/**
		connect and wait for new clinet connection
	*/
	public void connect()
	{
		try
		{
			//get host ip
			String ipStr = InetAddress.getLocalHost().toString();
			int ipIndex = ipStr.indexOf("/");
			ipAddress = ipStr.substring(ipIndex+1);
		}
		catch(Exception ex)
		{
			System.out.println("Error occured in connet()");
		}
		
		UDPServer udp = new UDPServer();
		udp.start();
		
		TCPServer tcp = new TCPServer();
		tcp.start();
	}
	
	/**
	 * @return true if port is valide
	 */
	public boolean validatePort()
	{
		boolean validate = false;
		try
		{
			PORT = Integer.parseInt(portField.getText().trim());
			
			if((PORT > 1024) && (PORT < 49151))
			{
				validate = true;
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Port number must be an integer between 1024 and 49151. Please try again.");
				validate = false;
			}
		}
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(null, "Port number must be an integer between 1024 and 49151. Please try again");
			validate = false;
		}
		return validate;
	}
	
	public static void main(String[] args)
	{
		new Server();		
	}
}	