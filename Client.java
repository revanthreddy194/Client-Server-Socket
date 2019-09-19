import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;


import java.lang.Math;

/*
 * 
 * Revanth Reddy Pamulapati
 * 
 * UTA ID: 1001717444
 * 
 * Reference: 
 * https://www.tutorialspoint.com/java/java_networking.htm
 * https://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 * 
 */

public class Client extends JFrame implements ActionListener {

	//creating an object for the ListenFromTheServer which listen to the server for all the messages
	
	ListenFromTheServer lFTS = new ListenFromTheServer();
	
	//an array which stores all the active clients connected to the server

	String[] arrOfStr;

	//an arraylist which has the list of all the clients connected to the server other than itself
	
	public ArrayList<String> otherUsers;

	//sec has a random number from 0-50 to start the logical clock with
	//sec2 stores the logical time at a particular point when a function is called
	//testRand1 is a dupe of 'sec'
	//receivedTime stores the logical time of remote client when it sends it's time
	
	int sec, sec2, testRand1, receivedTime;

	//a boolean variable to depict whether the client is connected or not
	
	boolean flag = false;

	// will first display "Username:", later on "Enter message"

	private JLabel label;

	// to hold the Username and later on the messages

	private JTextField tf;

	// to Logout and get the list of the users

	private JButton login, kill;
	
	// for the chat room and to display the logical clock

	private JTextArea ta, clock;

	// if it is for connection

	private boolean connected;

	// the Client variable for object

	private Client client;

	//for the input output streams

	//to read from the socket

	private ObjectInputStream Input;       

	//to write on the socket

	private ObjectOutputStream Output;     

	private Socket sock;

	// the server, the port number and the username

	private String server, uname;

	private int portNo;

	// the default port number and host

	private int defaultPort;

	private String defaultHost; 

	//client GUI empty constructor

	public Client() {

	}


	// Constructor connection receiving a socket number host and port number

	Client(String host, int port) {



		super("CLIENT-Sockets and Thread Management");

		defaultPort = port;

		defaultHost = host;



		// The NorthPanel- for top most panel

		JPanel northPanel = new JPanel(new GridLayout(3,1));


		// the Label and the TextField

		label = new JLabel("Enter your username below", SwingConstants.CENTER);

		northPanel.add(label);


		//by default it'll be anonymous

		tf = new JTextField("Client");

		tf.setBackground(Color.WHITE);

		northPanel.add(tf);

		add(northPanel, BorderLayout.NORTH);


		// The CenterPanel which is the chat room and clock

		ta = new JTextArea("", 80, 80);

		JPanel centerPanel = new JPanel(new GridLayout(2,1));

		centerPanel.add(new JScrollPane(ta));

		ta.setEditable(false);

		
		//to display the logical clock
		
		clock = new JTextArea("", 10, 10);
		
		centerPanel.add(new JScrollPane(clock));
		
		clock.setEditable(false);

		add(centerPanel, BorderLayout.CENTER);

		
		//south panel with login and kill buttons

		login = new JButton("Login");

		login.addActionListener(this);

		kill = new JButton("Kill");

		kill.addActionListener(this);

		// you have to login before being able to logout

		kill.setEnabled(false);      

		JPanel southPanel = new JPanel();

		southPanel.add(login);

		southPanel.add(kill);

		add(southPanel, BorderLayout.SOUTH);


		//close button

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setSize(600, 600);

		setVisible(true);

		tf.requestFocus();



	}

	// Constructor call with server name, port number, username, Client object
	// sets them with the rspective client

	Client(String server, int port, String username, Client client) {

		this.server = server;

		this.portNo = port;

		this.uname = username;

		this.client = client;

	}


	//to start which returns boolean value, if the returned value is false then it failed to start or 
	//if the returned value is true then it successfully started

	public boolean start() {

		boolean returnVal;

		// try to connect to the server

		try {

			sock = new Socket(server, portNo);

		}



		// if it failed, print display message

		catch(Exception ec) {

			printChat("Error connecting to server:" + ec);

			returnVal = false;

			return returnVal;

		}



		String message = this.uname.toUpperCase() + " - Connection accepted " + sock.getInetAddress() + ":" + sock.getPort() + "\n";

		printChat(message);



		// Creating both Data Streams

		try

		{

			Input  = new ObjectInputStream(sock.getInputStream());

			Output = new ObjectOutputStream(sock.getOutputStream());

		}

		catch (IOException eIO) {

			printChat("Exception creating new Input/output Streams: " + eIO);

			returnVal = false;

			return returnVal;

		}


		// creates and starts the Thread to listen from the server

		lFTS.start();

		// Send our username to the server this is the only message that we

		// will send as a String. All other messages will be ChatMessage objects( MESSAGE, KILL, USERS)

		try

		{

			Output.writeObject(uname);

		}

		catch (IOException eIO) {

			printChat("Exception doing login : " + eIO);

			disconnect();

			returnVal = false;

			return returnVal;

		}

		// success we inform the caller that it worked

		returnVal = true;

		return returnVal;

	}

	// append 'msg' to the ClientGUI JTextArea - chat box

	private void printChat(String msg) {

		client.append(msg + "\n");      

	}

	//to send message 'msg' to the server

	void sendMsg(Message msg) {

		try {

			Output.writeObject(msg);

		}

		catch(IOException e) {

			printChat("Exception writing to server: " + e);

		}

	}



	// When something goes wrong

	// Close the Input/Output streams and disconnect


	private void disconnect() {

		//close input stream

		try {

			if(Input != null) Input.close();

		}

		catch(Exception e) {}

		//close output stream

		try {

			if(Output != null) Output.close();

		}

		catch(Exception e) {} 

		//close the socket

		try {

			if(sock != null) sock.close();

		}

		catch(Exception e) {} 


		//informing the GUI

		if(client != null)

			client.connectionFailed();



	}


	//ListenFromTheServer waits for the message from the server and receives the messages

	class ListenFromTheServer extends Thread {

		public void run() {

			//To generate a random number from 0-50
			
			Random random = new Random();

			//genereted random number is stored in sec
			
			sec = random.nextInt(51);
			
			//testRand1 is a dupe of 'sec'
			
			testRand1 = sec;

			//sets the time sec
			
			setLTime2(sec);


			while(true) {

				try {

					//increments by 1
					
					sec++;

					//sets the time 'sec'
					
					setLTime2(sec);
					
					client.append2(sec);
					
					//stores the received message from the server as 'msg'

					String msg = (String) Input.readObject();
					
					//initiates the 'otherUsers' arraylist which stores all the active clients names except for itself

					otherUsers = new ArrayList<>();


					//if the received 'msg' starts with '#'
					//it means it has the list of active clients sent as a string
					
					if(msg.startsWith("#")) {

						//split the received string and store it into arrOfStr String
						//which are the usernames
						
						arrOfStr = msg.split("#"); 

						//determines the size of the arrOfStr and stores in 'size'

						int size = arrOfStr.length;

						//loop to eliminate 'this' client's name and store it into a new arraylist 'otherUsers
						
						for(int i=1; i<size ; i++) {

							boolean equalOrNot = arrOfStr[i].equalsIgnoreCase(uname);

							if(equalOrNot == false) {

								otherUsers.add(arrOfStr[i]);

							}

						}

						/*
						System.out.println("Array of arrOfStr::: ");

						for (String a : arrOfStr) 
							System.out.println(a);
						 

						System.out.println("Array List of OtherUsers::: ");

						for (String a : otherUsers) 
							System.out.println(a);
						*/
						
						//every 9 seconds when there is atleast one client other than the present client
						//it randomly chooses one client and sends its present time

						if((otherUsers.size() > 0) && (sec == testRand1 + 9)) {
							
							testRand1 = testRand1 + 9;

							//picks a random client and sends the message which has its local logical time
							
							Object randomItem = otherUsers.get(new Random().nextInt(otherUsers.size()));

							//System.out.println(randomItem + " = random user");

							String msgg = " Clock Time:" + getLTime2() + ">>";

							String temp = "@" + randomItem + " " + "\r\nPOST / HTTP/1.1 \r\n" + "Host: localhost/127.0.0.1:3438 \r\n" + "Date: " + getTime() + "\r\n" 

							+ "User-Agent: Java Client \r\n"

							+ "Content-Length: " + msgg.length() + "\r\n" 

							+ "Content-Type: Text/plain \r\n" + "MESSAGE: " + msgg + " >> \r\n";

							//sends parsed message to server
							
							client.sendMessageNew(temp);
						

							String str = "SEND LOCAL TIME TO: " + randomItem + "; Clock Time: " + getLTime2() + "\n\n";

							//System.out.println(sec);

							client.append(str);
						}

					}
					
					//if the received 'msg' starts with '@'
					//it means it has the message for them from other clients
					
					if(msg.startsWith("@")) {

						//parse the msg recieved from server and display

						String parsedMsg1 ="";

						String parsedMsg2 ="";

						String parsedMsg2Ext ="";
						
						String rcvmsg ="";
						
						String parsedMsg ="";

						//parsing the recieved HTTP message using pattern and matcher

						//getting sender's name

						Pattern pattern1 = Pattern.compile("CDT(.*?):");

						Matcher matcher1 = pattern1.matcher(msg);

						while (matcher1.find()) {

							parsedMsg1 = matcher1.group(1);

						}

						//getting the message

						Pattern pattern2 = Pattern.compile("MESSAGE:(.*?)>>");

						Matcher matcher2 = pattern2.matcher(msg);

						while (matcher2.find()) {

							parsedMsg2 = matcher2.group(1);

						}
						
						
						//received logical time
						
						parsedMsg2Ext = parsedMsg2 + ">>"; 
						
						Pattern pattern3 = Pattern.compile("Time:(.*?)>>");

						Matcher matcher3 = pattern3.matcher(parsedMsg2Ext);

						while (matcher3.find()) {

							rcvmsg = matcher3.group(1);

						}
					
						
						parsedMsg = parsedMsg1 + ": " + parsedMsg2;
						
						String finalMsg = parsedMsg + " No adjustment necessary.\n\n";
						
						receivedTime = Integer.parseInt(rcvmsg);

						//if the received time from other clients is more than its local time
						//then the local time gets updated
						
						if(receivedTime > sec) {
							
							int tempSec = sec;
							
							sec = receivedTime + 1;
							
							client.append2(sec);
						
							testRand1 = sec;
							
							//secString = Integer.toString(sec);
							
							//clock.setText(secString);

							
							finalMsg = parsedMsg + " Local Clock adjusted from " + tempSec + " to " + sec + ". \n\n";
						}


						//printing onto the client GUI by sending the 'parsedMsg'

						client.append(finalMsg);
					}

				}

				catch(IOException e) {

					printChat("Server has close the connection: " + e);

					// if(cg != null)

					client.connectionFailed();

					break;

				}

				// can't happen with a String object but need the catch anyhow

				catch(ClassNotFoundException e2) {

				}

			}

		}

	}



	//called from server and prompts when the msg sent by the client is reached to server
	//the boolean 'val' tells if it is reached or not

	/*
	void prompt(boolean val) {

		//if the val is true, then the message has successfully sent to the server, else din't reach the server 

		if(val == true) {

			JOptionPane.showMessageDialog(null, "Message sent successfully to the server", "InfoBox: " + "Notify", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	*/


	// called by the Client to append text 'str' to the TextArea

	public void sendMessageNew(String temp) {
		
		client.sendMsg(new Message(Message.MESSAGE, temp)); 

	}


	void append(String str) {

		ta.append(str);

		//sets the insertion position to the end 

		ta.setCaretPosition(ta.getText().length() - 1);

	}

	void append2(int sec3) {

		clock.setText("");
		
		clock.setText("Logical Clock: ");
		
		//clock.setCaretPosition(clock.getText().length() - 1);
		
		clock.append(Integer.toString(sec3));

		//sets the insertion position to the end 

		//clock.setCaretPosition(clock.getText().length() - 1);

	}

	// called by the GUI if the connection failed

	// we reset our buttons, label, textfield

	void connectionFailed() {

		login.setEnabled(true);

		kill.setEnabled(false);

		label.setText("Enter your username below");

		tf.setText("Client");

		tf.removeActionListener(this);

		connected = false;

	}


	//when the buttons are clicked, this method listens and performs related actions

	@Override
	public void actionPerformed(ActionEvent e) {

		Object obj = e.getSource();

		// if it is the Logout button

		if(obj == kill) {



			client.sendMsg(new Message(Message.KILL, ""));

			//oneToOne.setVisible(false);

			//oneToN.setVisible(false);

			//send.setVisible(false);

			//clockLabel.setVisible(false);

			//clock.setVisible(false);

			flag = false;

			tf.setVisible(true);

			label.setVisible(true);


			return;

		}




		//if login is pressed

		if(obj == login) {

			// ok it is a connection request

			String username = tf.getText().trim();

			// empty username ignore it

			if(username.length() == 0)

				return;

			// default server and port number

			String server = "localhost";

			int port = 3438;


			// try creating a new Client with GUI taking server, port number, 
			//username, and instance of ClientGUI as param

			client = new Client(server, port, username, this);

			// test if we can start the Client

			if(!client.start())

				return;



			//clock.setVisible(true);


			flag = true;


			tf.setVisible(false);

			label.setVisible(false);


			//clockLabel.setVisible(true);

			connected = true;

			//new Thread(new LogicalTime()).start();

			//System.out.println(lt.getLTime());





			//picks a random client and sends present time

			Timer timer = new Timer();
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {



					if (flag == false) {
						timer.cancel();
						timer.purge();
						return;
					}

					//String s = String.valueOf(sec);
					//System.out.println(s + "string");

					//System.out.println(sec + "int");

					//clock.setText(s);

					//sec++;
					client.sendMsg(new Message(Message.USERS, ""));



				}
			};

			// scheduleAtFixedRate(TimerTask task, long delay, long period)
			timer.scheduleAtFixedRate(timerTask, 0, 1000);




			// disable login button

			login.setEnabled(false);

			// enable the 2 buttons

			kill.setEnabled(true);

			tf.addActionListener(this);

		}

	}

	//returns the present system time

	private String getTime() {

		Calendar cal = Calendar.getInstance();

		SimpleDateFormat dateForm = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

		return dateForm.format(cal.getTime());

	}

	//returns the username

	public String getUsername()
	{
		return this.uname;


	}

	
	//sets the timer(counter)

	public void setLTime2(int secc) {

		sec2 = secc;

		//System.out.println(secc + "setltime2" + sec2);

	}

	
	//returns the counter time
	
	public int getLTime2() {


		return sec2;
	}

	//entry point of client, when client.java is runned

	public static void main(String[] args) {

		//creating the object of ClientGUI() will invoke the 2 parametered constructor 
		//with the serever and port number

		new Client("localhost", 3438);

	}



}

