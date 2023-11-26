// https://github.com/mbatinefe/Painting_Client_Server.git
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	ServerSocket srvSocket;
	DatagramSocket detectSocket;
	ArrayList<Socket> list = new ArrayList<Socket>();

	private int userNumber = 0;
	
	int serverPort = 56789;
	
	public Server() throws IOException {
		detectSocket = new DatagramSocket(45678);
		srvSocket = new ServerSocket(serverPort);
		
		new Thread(() -> {
			DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);	
			while(true) {
				try {
					detectSocket.receive(packet);
					byte[] data = packet.getData();
					String str = new String(data, 0, packet.getLength());
					int size = packet.getLength();
					String srcAddr = packet.getAddress().toString();
					int srcPort = packet.getPort();
					
					System.out.println("Received username: " + str);
					System.out.println("Sent by IP: " + srcAddr);
					System.out.println("Via port: " + srcPort);
					
					// Get the server's IP address and port number
					InetAddress serverAddress = InetAddress.getLocalHost(); // You can replace this with a more accurate way to obtain the server's IP address
					
					byte[] dataA = serverAddress.getAddress(); // InetAddress to byte array
					byte[] dataB = Integer.toString(serverPort).getBytes(); // int to byte array

					 // Combine dataA and dataB into a single byte array
		            byte[] combinedData = new byte[dataA.length + dataB.length];
		            System.arraycopy(dataA, 0, combinedData, 0, dataA.length);
		            System.arraycopy(dataB, 0, combinedData, dataA.length, dataB.length);
		            
		            DatagramPacket packet2 = new DatagramPacket(combinedData, combinedData.length, packet.getAddress(), srcPort);
	
					// Print the details for debugging
					System.out.println("\nSending IP and Port info to client...");
					System.out.println("Server Address: " + InetAddress.getByAddress(dataA));
					System.out.println("Server Port: " + serverPort);
	
					detectSocket.send(packet2);
					 
					System.out.println("\nSuccesfully sent.");
					

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}).start();
		
		while (true) {
			Socket cSocket = srvSocket.accept();
			System.out.println("\nTCP Connection established.");
			
			// Client connected to the server, implement its number
			userNumber++;
			System.out.println("User number:" + userNumber);
			
			DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());
			
			try {
				out.writeInt(4);
				out.writeInt(userNumber);
			} catch (IOException e4) {
				e4.printStackTrace(); 
			}

			// If it is not the first client
			if(userNumber > 1) {	
				askGridMessage();
				out.flush();
	    		askBoardMessage();
	    		out.flush();
			}
		    // Create a thread to handle continuous updates from the client
		    Thread t = new Thread(() -> {
			    // Implement logic to send the initial sketch data to the client
				synchronized (list) {
					list.add(cSocket);	
				}
		    	try {
		            serve(cSocket);
		        } catch (IOException e) {
		        	// User leaving the game
		        	userNumber--;			
		            e.printStackTrace(); // Handle exceptions appropriately
		        } finally {
		            synchronized (list) {
		                list.remove(cSocket);
		                
		            }
		        }
		    });
		    t.start();
		}
	
	}

	
	private void serve(Socket cSocket) throws IOException {
		DataInputStream in = new DataInputStream(cSocket.getInputStream());
		while (true) {
			
			// receiving data
			int type = in.readInt(); // type represents the message type

			switch (type) {
			case 0:
				// text message
				forwardTextMessage(in);
				break;
			case 1:
				// drawing message
				forwardDrawingMessage(in);
				break;
			case 2:
				forwardAreaMessage(in);
				break;
			case 3:
				forwardGridMessage(in);
				break;
			default:
				
				// others for extra
			}
		}
	}

	// Forward grid measure message to clients
	private void forwardGridMessage(DataInputStream in) throws IOException{
		int row = in.readInt();
		
		// Get the row information, col information is same with row.
		synchronized(list) {
			for (int i = 0; i < list.size(); i++) {
				
				Socket s = list.get(i);
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				
				// Send the row information to other clients
				out.writeInt(5);
				out.writeInt(row);
				out.flush();
			}
		}
	}
	
	// Forward chat message to clients
	private void forwardTextMessage(DataInputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.readInt();
		in.read(buffer, 0, len);
		System.out.println(new String(buffer, 0, len));

		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {
				Socket socket = list.get(i);

				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeInt(0);
				out.writeInt(len);
				out.write(buffer, 0, len);
				out.flush();
			}
		}
	}
	
	// Forward pen message to clients
	private void forwardDrawingMessage(DataInputStream in) throws IOException{
		int color = in.readInt();
		int x = in.readInt();
		int y = in.readInt();
		
		synchronized(list) {
			for (int i = 0; i < list.size(); i++) {
				
				Socket s = list.get(i);
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				
				out.writeInt(1);
				out.writeInt(color);
				out.writeInt(x);
				out.writeInt(y);
				out.flush();
			}
		}
	}
	
	// Forward bucket message to clients
	private void forwardAreaMessage(DataInputStream in) throws IOException{
		int color = in.readInt();
		int x = in.readInt();
		int y = in.readInt();
		
		synchronized(list) {
			for (int i = 0; i < list.size(); i++) {
				Socket s = list.get(i);
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				
				out.writeInt(2);
				out.writeInt(color);
				out.writeInt(x);
				out.writeInt(y);
				out.flush();
			}
		}
	}
	
	// Ask first users' grid
	private void askGridMessage() throws IOException{
        try {
    		synchronized(list) {

				Socket s = list.get(0);
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				
				// Send first user's board to others who joined later
				out.writeInt(3);
			}
    		
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
	}
	
	// Ask first users' board
	private void askBoardMessage() throws IOException{
        try {
    		synchronized(list) {

				Socket s = list.get(0);
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				
				// Send first user's board to others who joined later
				out.writeInt(3);
			}
    		
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
         
     }

	public static void main(String[] args) throws IOException {
		System.out.println("Waiting for Clients...\n");
		new Server();

	}
}
