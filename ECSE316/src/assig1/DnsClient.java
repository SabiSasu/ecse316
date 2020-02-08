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


		int retries = q.getMaxRetries();
		long startTime = new Date().getTime();
		
		while (retries > 0) { //TODO: find a way to retries-- whenever timeout occurs
		  try {
            DatagramSocket clientSocket = new DatagramSocket(); //initialize socket
            //set timeout, might be too short to get a reply for default value
      //      clientSocket.setSoTimeout(q.getTimeout()); //TODO: FIX THIS TIMEOUT PROBLEM
           
            try {
                //get DNS server raw ip address
                byte[] adress = q.getByteServer();
                InetAddress IPAddress = InetAddress.getByAddress(adress);
                System.out.println("IP address" + IPAddress.toString());
                
                byte[] sendData = q.generateQuery(); //data to send
                
                //printing query in hexadecimals for testing
                for(int i =0; i < sendData.length;i++) {
                    System.out.println(String.format("%02x", sendData[i]));
                }
                System.out.println("packet ready");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, q.getPort());
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
                long endTime = new Date().getTime();
                long totalTime = endTime - startTime;
                
                System.out.println("Response received after " + totalTime + " seconds (" + retries + " retries)");
                
                //format received information
                String modifiedSentence = new String(receivePacket.getData());
                System.out.println(modifiedSentence);
                ByteBuffer b = ByteBuffer.allocateDirect(1024);
                b.put(receivePacket.getData());
                b.flip();
                
                //process response
                //TODO: check id to make sure its the same and find a better way to generate error!
                
               int respID = b.get(0) + b.get(1); //gets the ID of response
               
               if (q.getRequestID() != respID) { //compare response and query IDs
                 System.out.println("Error! ID's dont match");
               }
               
               int anscount = b.get(6) + b.get(7); //number of records in Answer
               if (anscount > 0) {
                 
                 System.out.println("***Answer Section (" + anscount + " records)***");
                 
                 int offset = findEnd(b, 11); //gives us length in bytes of QNAME and NAME
                 int type = b.get(11 + offset + 4 + offset + 1) + b.get(11 + offset + 4 + 2); //get TYPE from ANSWER
                 int theclass = b.get(11 + offset + 4 + offset + 3) + b.get(11 + offset + 4 + offset + 4); //get CLASS from ANSWER
                
                 if (theclass != 0x0001) {//error
                   //TODO: error cuz it should be 0x0001                   
                 }
                 
                 if (type == 0x0001) { //A type query
                   //IP <tab> [ip address] <tab> [seconds can cache] <tab> [auth | nonauth]
                   int ip1 = b.get(11 + offset + 4 + offset + 11);
                   int ip2 = b.get(11 + offset + 4 + offset + 12);
                   int ip3 = b.get(11 + offset + 4 + offset + 13);
                   int ip4 = b.get(11 + offset + 4 + offset + 14); //4 components of ip address
                   int ttl = b.get(11 + offset + 4 + offset + 5) + b.get(11 + offset + 4 + offset + 6); //get TTL from ANSWER
                   int temp = b.get(2);   
                   //mask with 000001000 to obtain AA from ANSWER
                   int mask = 0b000001000; //mask
                   if ((temp & mask) == 8) { //is authority
                     System.out.println("IP \t" + ip1 + "." + ip2 + "." + ip3 + "." + ip4 + "\t" + ttl + "\t auth");
                   }
                   else { //not authority
                     System.out.println("IP \t" + ip1 + "." + ip2 + "." + ip3 + "." + ip4 + "\t" + ttl + "\t nonauth");
                   }                   
                 }
                 else if (type == 0x0002) {//ns
                   System.out.println();
                 }
                 else if (type == 0x000f) {//mx
                   System.out.println();
                 }
                 else if (type == 0x0005) {//cname
                   System.out.println();
                 }
                 else {
                   //TODO: error if its none of the types above
                 }
               }

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
	
	private static int findEnd(ByteBuffer b, int start) {
	  int counter = 0;
	  int result = start;
	  int curByte = b.get(result); //this is where QNAME starts
	  while (curByte != 0) {//we havent reached end of QNAME yet
	    result++;
	    counter++;
	    curByte = b.get(result);
	  }
	  
	  return counter;
	}
}
