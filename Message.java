import java.io.Serializable;

/*
 * 
 * Revanth Reddy Pamulapati
 * 
 * UTA ID: 1001717444
 * 
 * Reference: https://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 * 
 */

// the two types of messages exchanging between clients and server - MESSAGE, EXIT

public class Message implements Serializable {

	// MESSAGE an ordinary message

	// KILL to disconnect from the Server

	// USERS the clients who are connected to the server
	
	static final int USERS = 0, MESSAGE = 1, KILL = 2;

	//type variable stores the relating number as given above
	
	private int type;

	//message has the string which is being passed
	
	private String message;
 


	// ChatMessage constructor 

	Message(int type, String message) {

		this.message = message;
		
		this.type = type;

	}



	// getters of message and type

	String getMessage() {

		return message;

	}
	
	int getType() {

		return type;

	}

	

}

