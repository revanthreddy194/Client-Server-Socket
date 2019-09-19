import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;

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

//the server class deals with the server side of the application including the server GUI and the socket creation 
//listens for the connections, message broadcasting and all of the server side functionalities  

public class Server extends JFrame implements ActionListener, WindowListener {

	//list of all cilents who gets registered

	public ArrayList<ClientThread> list;

	//the boolean that will be turned off to stop the server
	//to check if the server is on or off, and will be turned off by the boolean value 'false'

	private boolean serverOnOff;

	// to store the port number

	private int portNo;

	//each connection gets a unique id stored in uni_id

	private static int uni_id;

	//button for starting and stopping the server

	private JButton startStop;

	// JTextArea for the messages exchanged between client(s)/server, log messages and list of active clients

	private JTextArea conversation, log, clients;

	//Server variable for object

	Server server;


	//constructor of Server is called when the server class is invoked
	//establishing the connection

	Server() {

		//the title of the server's GUI 

		super("SERVER-Sockets and Thread Management");

		server = null;

		// the 'btn' JPanel has a start/stop button

		JPanel btn = new JPanel();

		/* 
		 * the start/stop button to start the server
		 * Initially the start button will be enabled as the
		 * server isn't started yet
		 */

		startStop = new JButton("Start");

		startStop.addActionListener(this);

		btn.add(startStop);

		//adding to the 'btn' JPanel on the north part of the GUI

		add(btn, BorderLayout.NORTH);



		/*
		 * 'texts' panel to handle all the textareas (conversation, log, clients)
		 * 'conversation' textarea for the messages exchanged between client(s)/server
		 * 'log' textarea for the log messages/events happen
		 * 'clients' textarea to display the list of all active clients connected to the server 
		 */


		JPanel texts = new JPanel(new GridLayout(3,1));

		conversation = new JTextArea(100,100);

		conversation.setEditable(false);

		//writting into the conversation textarea

		appendConvo("All messages between client(s) / server:\n");

		texts.add(new JScrollPane(conversation));

		log = new JTextArea(100,100);

		log.setEditable(false);

		//writting into the log textarea

		appendLog("Log messages:\n");

		texts.add(new JScrollPane(log));

		clients = new JTextArea(100,100);

		clients.setEditable(false);

		//writting into the clients textarea

		appendClients("ACTIVE CLIENTS:\n");

		texts.add(clients);

		add(texts);



		// need to be informed when the user click the close button on the frame

		addWindowListener(this);

		//sets the window size with width 400 and height 600

		setSize(400, 600);

		setVisible(true);

	}      

	//server constructor that will take port number for connection and the instance of Server

	public Server(int portNo, Server server) {

		//to access the Server methods

		this.server = server ;

		//gets the default port number, in this case it's 3438

		this.portNo = portNo;

		//to store the list of clients

		list = new ArrayList<ClientThread>();

	}

	//one instance of thread for one client
	//creates input and output streams

	class ClientThread extends Thread {

		//socket to listen

		Socket sock;

		ObjectInputStream input;

		ObjectOutputStream Output;

		//unique id for connection

		int id;

		//to store client's username

		String uname;

		//type of message

		Message chatMsg;


		//ClientThread Constructor taking the socket as parameter

		ClientThread(Socket socket) {

			// a unique id

			id = ++uni_id;

			this.sock = socket;

			/* Creating both Data Stream */

			try

			{

				// create output and input streams for communication

				Output = new ObjectOutputStream(socket.getOutputStream());

				input  = new ObjectInputStream(socket.getInputStream());

				// read the username

				uname = (String) input.readObject();

				printLog(uname + " just connected.");

				//updates the list of active clients on server GUI

				listOfClients();

			}

			catch (IOException e) {

				printLog("Exception creating new Input/output Streams: " + e);

				return;

			}

			catch (ClassNotFoundException e) {

			}


		}


		//returns the username

		public String getUsername() {
			return uname;
		}


		// what will run forever

		public void run() {

			// to loop until LOGOUT

			boolean keepGoing = true;

			while(keepGoing) {

				// read a String (which is an object)

				try {

					chatMsg = (Message) input.readObject();

				}

				catch (IOException e) {

					printLog(uname + " Exception reading Streams: " + e);

					break;             

				}

				catch(ClassNotFoundException e2) {

					break;

				}

				// the message part of the ChatMessage

				String message = chatMsg.getMessage();



				// Switch on the type of message received

				switch(chatMsg.getType()) {

				//sends the list of active clients to the requested client
				
				case Message.USERS:

					//StringBuilder saves the names of the clients from the for loop and convert it into a string and
					//send that string 'str' to the requesting client
					
					StringBuilder str = new StringBuilder();

					for(int i = 0; i < list.size(); ++i) {

						ClientThread ct = list.get(i);
						
						str.append("#");

						str.append(ct.uname);

					}
					
					writeToClient(str.toString());

					break;

				//sends the message received from the source client to the respective destination client 
					
				case Message.MESSAGE:

					broadcast(uname.toUpperCase() + ": " + message);

					break;

				case Message.KILL:

					//updates the list of active clients on server

					listOfClients();

					printLog(uname + " disconnected with a LOGOUT message.");

					//setting false which means logged out

					keepGoing = false;

					//updates the list of active clients on server

					listOfClients();

					break;
				}

			}

			// remove myself from the arrayList containing the list of the

			// connected Clients

			removeClient(id);

			close();

		}





		// to close all the connections

		private void close() {


			try {

				if(Output != null) Output.close();

			}

			catch(Exception e) {}

			try {

				if(input != null) input.close();

			}

			catch(Exception e) {};

			try {

				if(sock != null) sock.close();

			}

			catch (Exception e) {}

		}



		//writing a string 'msg' to the client output stream

		private boolean writeToClient(String msg) {

			boolean returnVal;

			// if Client is still connected send the message to it

			if(!sock.isConnected()) {

				close();

				returnVal = false;

				return returnVal;

			}

			// write the message to the stream

			try {

				Output.writeObject(msg);
			}

			// if an error occurs, do not abort just inform the user

			catch(IOException e) {

				printLog("Error sending message to " + uname);

				printLog(e.toString());

			}

			returnVal = true;

			return returnVal;

		}

	}


	//start method to invoke

	public void start() {

		//if start() is called, then it means that the server got started and intialized with true

		serverOnOff = true;

		//socket server creation and will wait for new connection requests

		try

		{

			//the socket used by the server with the port number, i.e 3438

			ServerSocket servSoc = new ServerSocket(portNo);



			//infinite loop to wait for connections which will be in loop until serverOnOff is false
			//i.e server is stopped 

			while(serverOnOff)

			{

				// format message saying we are waiting
				//this string gets printed on the servers log area

				printLog("Server is waiting for the Clients on PORT: " + portNo + ".");

				//to accept the connection

				Socket soc = servSoc.accept();      

				//to stop the server

				if(!serverOnOff)

					break;

				//making a thread of the new connection

				ClientThread ct = new ClientThread(soc);  

				//adding it into the list of clients(arraylist)

				list.add(ct);                                  

				//starting 

				ct.start();

				//updates the active clients list text area on server

				listOfClients();


			}

			//closing the server and clients

			try {

				servSoc.close();

				for(int i = 0; i < list.size(); ++i) {

					ClientThread c = list.get(i);

					try {

						c.input.close();

						c.Output.close();

						c.sock.close();

					}

					catch(IOException ioE) {


					}

				}

			}

			catch(Exception e) {

				printLog("Exception while closing the server and clients: " + e);

			}

		}

		// something went bad

		catch (IOException e) {

			String message = getTime() + "Exception on the new ServerSocket: " + e + "\n";

			printLog(message);

		}

	}  


	//for stopping the server 

	public void stop() {

		//setting serverOnOff to false to stop the server

		serverOnOff = false;

		try {

			new Socket("localhost", portNo);

			//updates the list of active clients on server GUI
			listOfClients();

		}

		catch(Exception e) {


		}

	}

	//removes the client id from the array list
	//'id' will be the id of the client which is to be removed

	synchronized void removeClient(int id) {

		//scanning the arraylist to get the required Id

		for(int i = 0; i < list.size(); ++i) {

			ClientThread ct = list.get(i);

			//if found

			if(ct.id == id) {

				list.remove(i);

				//updates the list of active clients on server

				listOfClients();

				return;

			}

		}

	}

	//prints the 'msg' to the server log textarea 

	private void printLog(String msg) {

		String time = getTime() + " " + msg;

		//appends the time and message onto the server log textarea

		server.appendLog(time + "\n");

	}


	//prints the list of active clients on the server GUI when it gets invoked

	public void listOfClients()
	{
		//empties the active clients textarea to update it

		server.clearTextAreaCli();

		for(int i = 0; i < list.size(); ++i) {

			ClientThread ct = list.get(i);

			//System.out.println(ct.username);

			//prints list of active users on server GUI		

			server.appendClients(ct.uname.toUpperCase() + "\n");


		}

	}

	//returns the current time 

	//Reference: http://www.mkyong.com/java/java-how-to-get-current-date-time-date-and-calender/

	private String getTime() {

		Calendar cal = Calendar.getInstance();

		SimpleDateFormat dateForm = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

		return dateForm.format(cal.getTime());

	}

	//to send the message to all the connected clients, if there is no specification 
	//the 'message' is the stirng which is to be sent

	private synchronized boolean broadcast(String message) {

		boolean val;

		//System.out.println(message);

		//getTime method gives the current time 

		String time = getTime();

		// to check if message is private i.e. client to client message(one-one)

		String[] splt = message.split(" ",3);

		boolean isOneToOne = false;

		//if '@' is found, then it is a one-to-one message 
		//as in the case of one-one, the destination client name is prefixed with '@'

		try {
			if(splt[1].charAt(0)=='@') 

				isOneToOne=true;
		}
		catch (Exception e) {

		}


		// if private message, send message to mentioned username only

		if(isOneToOne==true)
		{
			//gets the destination client name 

			String tocheck=splt[1].substring(1, splt[1].length());

			message=splt[0]+splt[2];


			//messageLF is unparsed HTTP message

			String unparsedMsg = "@" + time + " " + message + "\n";


			// display message on Server GUI

			server.appendConvo(unparsedMsg);     

			//System.out.println("server-broadcast-1_1-");



			boolean found=false;

			//reverse order looping helps to find the mentioned client

			for(int i=list.size(); --i>=0;)
			{
				
				
				ClientThread ct1=list.get(i);

				//to compare both the usernames

				String check=ct1.getUsername();

				//if it finds the required client

				if(check.equalsIgnoreCase(tocheck))
				{
					//write and check if the client can take it or not
					//if fails remove the client from the list

					if(!ct1.writeToClient(unparsedMsg)) {

						list.remove(i);

						//updates the list of active clients on server

						listOfClients();

						//prints the message on server GUI

						printLog("Disconnected Client " + ct1.uname + " removed from list.");

					}
					//client found and delivered the message

					found=true;
					break;
				}



			}

			//if the required client not found, then return false

			if(found!=true)
			{
				val = false;

				return val; 
			}
		}

		// if message is a broadcast message to all, i.e one-to-n

		else
		{
			String unparsedMsg = time + " " + message + "\n";

			// display conversation on Server GUI

			server.appendConvo(unparsedMsg);     

			// we loop in reverse order in case we would have to remove a Client
			// because it has disconnected

			for(int i = list.size(); --i >= 0;) {
				ClientThread ct = list.get(i);

				//write and check if the client can take it or not
				//if fails remove the client from the list

				if(!ct.writeToClient(unparsedMsg)) {

					list.remove(i);

					//updates the list of active clients on server

					listOfClients();

					//log message printing to the server GUI

					printLog("Disconnected Client " + ct.uname + " removed from list.");
				}
			}
		}
		val = true;

		return val;
	}


	// append messages to the textareas

	// and the positions mus be at the ends

	// all the messages exchanged btwn each other are appended through this method

	//'str' is the message to be printed

	void appendConvo(String str) {

		conversation.append(str);

		//sets the insertion position to the end

		conversation.setCaretPosition(conversation.getText().length() - 1);

	}

	// the log messages passes through the string 'str' are printed

	void appendLog(String str) {

		log.append(str);

		//sets the insertion position to the end

		try {

			log.setCaretPosition(conversation.getText().length() - 1);

		}
		catch (Exception e) {

		}


	}

	// active clients on the connection are listed 
	// gets the active clients names through 'str'

	void appendClients(String str) {

		clients.append(str);

		//sets the insertion position to the end 

		try {

			clients.setCaretPosition(clients.getText().length() - 1);

		}
		catch (Exception e) {

		}


	}

	// empties the list of active clients
	// and adds the updated list

	public void clearTextAreaCli() {

		clients.setText("");

		//heading of that text area

		appendClients("List of Active Clients.\n");

	}

	// starts or stops the server when clicked

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the server is running, then we have to stop

		if(server != null) {

			server.stop();

			server = null;

			//buttton text is changed to start as the server has stopped and is ready to get started again

			startStop.setText("Start");

			return;

		}

		// ceating a new Server with 3438 as default port number
		// which invokes two parametered constructor of Server Class 
		// sending the port number and the instance of the serverGUI

		server = new Server(3438, this);

		// and starting it as a thread

		new ServerRunning().start();

		//setting the button as 'stop' as the server has started and the same button
		//used to start the server is used for stopping

		startStop.setText("Stop");

	}

	// when X button is used for closing/exiting

	//then connection with server has to be closed to free the port 3438


	@Override
	public void windowClosing(WindowEvent e) {
		// if my Server exist

		if(server != null) {

			try {

				server.stop();          // closing the connection

			}

			catch(Exception weClose) {

			}

			//giving null which means the server is not running

			server = null;

		}

		// dispose the frame

		dispose();

		System.exit(0);

	}


	// other listners are not needed in this case
	//implementing them as they cannot be left unimplemented

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}


	//to run the server, thread is needed

	class ServerRunning extends Thread {

		public void run() {

			//runs until the server fails

			server.start();         

			// when server fails
			// replacing stop with start and send crash message to the log textarea

			startStop.setText("Start");

			//displaying that the server has crashed

			appendLog("Server crashed\n");

			server = null;

		}

	}


	// main is the entry point to start server

	public static void main(String[] arg) {

		// creating an object of ServerGUI invokes the parameterless constructor of ServerGUI

		new Server();

	}



}