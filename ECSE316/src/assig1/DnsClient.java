package assig1;

import java.util.Arrays;
import java.util.Date;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class DnsClient {

	public static void main(String[] args) {
		//java DnsClient [-t timeout] [-r max-retries] [-p port] [-mx|-ns] @server name 
		
		//create query, only valid if @ character is not missing for mandatory fields
		Query q = new Query();
		if(((args[args.length - 2]).charAt(0) == '@')) {
			q = new Query(args);
		}
		else {
			System.out.println("ERROR \t Incorrect input syntax: '@' character missing or misplaced \t ERROR");
			System.exit(0);
		}

		System.out.println("DnsClient sending request for " + q.getName());
		System.out.println("Server: " + q.getServer()); 
		System.out.println("Request type: " + q.getQuery().toString());

		
		long startTime = new Date().getTime();
		
		try {
			DatagramSocket clientSocket = new DatagramSocket(); //initialize socket
			//set timeout, might be too short to get a reply for default value
			clientSocket.setSoTimeout(q.getTimeout());
			
			try {
				//get DNS server raw ip address
				byte[] adress = q.getByteServer();
				InetAddress IPAddress = InetAddress.getByAddress(adress);
				System.out.println(IPAddress.toString());
				
				byte[] sendData = q.generateQuery(); //data to send
				
				//printing query in hexadecimals for testing
				for(int i =0; i < sendData.length;i++) {
					System.out.println(String.format("%02x", sendData[i]));
				}
				System.out.println("packet ready");
				byte[] sendData2 = null;
				DatagramPacket sendPacket = new DatagramPacket(sendData2, sendData2.length, IPAddress, q.getPort());
				System.out.println("sending");
				//IO Exception, send packet
				clientSocket.send(sendPacket);
				
				System.out.println("receiving");
				//receive data
				byte[] receiveData = new byte[1024]; //data to send
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				System.out.println("receiving");
				clientSocket.receive(receivePacket);
				

		        
				/* test code from the main.java file, also doesnt work lol
				byte[] inBuf = new byte[8192];
		        ByteArrayInputStream inBufArray = new ByteArrayInputStream(inBuf);
		        DataInputStream input = new DataInputStream(inBufArray);
		        DatagramPacket response = new DatagramPacket(inBuf, inBuf.length);

		        clientSocket.receive(response);
				*/
				
				System.out.println("received, treating info");
				//format received information
				String modifiedSentence = new String(receivePacket.getData());
				System.out.println(modifiedSentence);
				ByteBuffer b = ByteBuffer.allocateDirect(1024);
				b.put(receivePacket.getData());
				b.flip();
				
				//process response, TO DO
				//check id to make sure its the same
				
				
				//printing response
				System.out.println(b.array());
				

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} finally{
				//closing socket no matter if we get an exception or not
				clientSocket.close();
			}

		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		long endTime = new Date().getTime();

	}

}
