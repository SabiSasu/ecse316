package assig1;

import java.util.Arrays;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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


		try {
			DatagramSocket clientSocket = new DatagramSocket(); //initialize socket

			try {
				//get DNS server raw ip address
				byte[] adress = q.getByteServer();
				InetAddress IPAddress = InetAddress.getByAddress(adress);

				//prepare packet to send
				byte[] sendData = new byte[1024]; //data to send
				sendData = q.generateQuery(); 
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, q.getPort());

				//IO Exception, send packet
				clientSocket.send(sendPacket);

				//receive data
				byte[] receiveData = new byte[1024]; //data to send
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);
				
				//format received information
				String modifiedSentence = new String(receivePacket.getData());
				System.out.println(modifiedSentence);
				

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

	}

}
