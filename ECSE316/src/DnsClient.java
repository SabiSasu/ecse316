

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
	int pointer;
	public static void main(String[] args) {
		//java DnsClient [-t timeout] [-r max-retries] [-p port] [-mx|-ns] @server name 

		//create query, only valid if @ character is not missing for mandatory fields
		Query q = new Query();
		if(((args[args.length - 2]).charAt(0) == '@')) {
			try {
				q = new Query(args);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.exit(0);
			}
		}
		else {
			System.out.println("ERROR \t Incorrect input syntax: '@' character missing or misplaced \t ERROR");
			System.exit(0);
		}

		System.out.println("DnsClient sending request for " + q.getName());
		System.out.println("Server: " + q.getServer()); 
		System.out.println("Request type: " + q.getQuery().toString());


		int retries = 0;

		while (retries < q.getMaxRetries()) {
			long startTime = new Date().getTime();

			try {
				DatagramSocket clientSocket = new DatagramSocket(); //initialize socket
				//set timeout, might be too short to get a reply for default value
				clientSocket.setSoTimeout(q.getTimeout()*1000); //TODO: FIX THIS TIMEOUT PROBLEM

				try {
					//get DNS server raw ip address
					byte[] adress = q.getByteServer();
					InetAddress IPAddress = InetAddress.getByAddress(adress);
					byte[] sendData = q.generateQuery(); //data to send

					//printing query in hexadecimals for testing
					/*for(int i =0; i < sendData.length;i++) {
						System.out.println(String.format("%02x", sendData[i]));
					}*/

					//send packet
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, q.getPort());
					clientSocket.send(sendPacket);
					//receive data
					byte[] receiveData = new byte[1024]; //data to send
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					clientSocket.receive(receivePacket);//throws timeout if times out

					long endTime = new Date().getTime();
					long totalTime = endTime - startTime;

					System.out.println("Response received after " + totalTime + " seconds (" + retries + " retries)");

					//format received information
					ByteBuffer b = ByteBuffer.allocateDirect(1024);
					b.put(receivePacket.getData());
					b.flip();

					//process response
					int respID = b.get(0) + b.get(1); //gets the ID of response

					if (q.getRequestID() != respID) { //TODO: DEAL WITH ERROR compare response and query IDs
						System.out.println("ERROR \t Unexpected response: ID in reply does not match query ID");
						System.exit(0);
					}

					int anscount = b.get(6) + b.get(7); //number of records in Answer
					int offset=10;
					
					System.out.println("***Answer Section (" + anscount + " records)***");
					for (int i = anscount; i > 0; i--) {
						offset = findEnd(b, offset+2);
						int type = b.get(offset + 1) + b.get(offset + 2); //get TYPE from ANSWER
						int theclass = b.get(offset + 3) + b.get(offset + 4); //get CLASS from ANSWER

						if (theclass != 0x0001) {//error
							System.out.println("ERROR \t Unexpected response: Response is not of internet class");
							System.exit(0);
						}

						printAnswer(offset, b, type);

					}
					
					int arcount = b.get(10) + b.get(11); //get ARCOUNT from HEADER
					if(arcount>0)
					System.out.println("***Additional Section (" + arcount + " records)***");
					while (arcount > 0) { //there are things in additional
						offset = findEnd(b, offset+2);
						int type = b.get(offset + 1) + b.get(offset + 2); //get TYPE from ANSWER
						int theclass = b.get(offset + 3) + b.get(offset + 4); //get CLASS from ANSWER

						if (theclass != 0x0001) {//error
							System.out.println("ERROR \t Unexpected response: Response is not of internet class");
							System.exit(0);
						}

						//printAnswer(offset, b, type);
						arcount--;
					}

					//we're done, so exit
					System.exit(0);

				}catch (SocketTimeoutException ex) {
					retries++;
					continue;
				} catch (SocketException e) {
					System.out.println("ERROR\tSocket Exception");
					e.printStackTrace();
				} catch (UnknownHostException e) {
					System.out.println("ERROR\tIncorrect host name or IP");
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("ERROR\tIO Exception");
					e.printStackTrace();
				} finally{
					//closing socket no matter if we get an exception or not
					clientSocket.close();
				}
			} catch (SocketException e1) {
				System.out.println("ERROR\tException when creating socket");
				e1.printStackTrace();
			}


		}
		if(retries >= q.getMaxRetries())
			System.out.println("ERROR \t Maximum number of retries "+ q.getMaxRetries() + " exceeded ");
	}

	private static void printAnswer(int offset, ByteBuffer b, int type) {
		if (type == 0x0001) { //A type query
			//IP <tab> [ip address] <tab> [seconds can cache] <tab> [auth | nonauth]
			int ip1 = byteToInt(b.get(offset   + 11));
			int ip2 = byteToInt(b.get(offset   + 12));
			int ip3 = byteToInt(b.get(offset   + 13));
			int ip4 = byteToInt(b.get(offset   + 14));
			int ttl = byteToInt((byte) (b.get(offset   + 5) + b.get(offset   + 6) + b.get(offset   + 7) + b.get(offset   + 8)));
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
			//NS <tab> [alias] <tab> [seconds can cache] <tab> [auth | nonauth]
			int ttl = byteToInt((byte)(b.get(offset + 5) + b.get(offset + 6) + b.get(offset   + 7) + b.get(offset   + 8))); //get TTL from ANSWER
			int temp = b.get(2);   
			//mask with 000001000 to obtain AA from ANSWER
			int mask = 0b000001000; //mask
			String alias = readRData(b,offset   + 11);

			if ((temp & mask) == 8) { //is auth
				System.out.println("NS \t" + alias + "\t" + ttl + "\t auth");
			}
			else { //not auth
				System.out.println("NS \t" + alias + "\t" + ttl + "\t nonauth");
			}
		}
		else if (type == 0x000f) {//mx
			//MX <tab> [alias] <tab> [pref] <tab> [seconds can cache] <tab> [auth | nonauth]
			int pref = b.get(offset   + 11) + b.get(11 + offset   + 12); //get PREFERENCE
			String alias = readRData(b,offset + 13); //get EXCHANGE
			int ttl = byteToInt((byte)(b.get(offset   + 5) + b.get(offset   + 6) + b.get(offset   + 7) + b.get(offset   + 8))); 
			int temp = b.get(2);   
			//mask with 000001000 to obtain AA from ANSWER
			int mask = 0b000001000; //mask

			if ((temp & mask) == 8) { //is auth
				System.out.println("MX \t" + alias + "\t" + pref + "\t" + ttl + "\t auth");
			}
			else { //not auth
				System.out.println("MX \t" + alias + "\t" + pref + "\t" + ttl + "\t nonauth");
			}
		}
		else if (type == 0x0005) {//cname
			//CNAME <tab> [alias] <tab> [seconds can cache] <tab> [auth | nonauth]
			int ttl = byteToInt((byte)(b.get(offset   + 5) + b.get(offset   + 6) + b.get(offset   + 7) + b.get(offset   + 8))); 
			String alias = readRData(b, offset   + 11); //TODO: im not sure if alias is formatted the same way for cname as it is for ns
			int temp = b.get(2);   
			//mask with 000001000 to obtain AA from ANSWER
			int mask = 0b000001000; //mask

			if ((temp & mask) == 8) { //is auth
				System.out.println("CNAME \t" + alias + "\t" + ttl + "\t auth");
			}
			else { //not auth
				System.out.println("CNAME \t" + alias + "\t" + ttl + "\t nonauth");
			}
		}
		else {
			System.out.println("NOTFOUND");
			System.exit(0);
		}

	}

	private static int findEnd(ByteBuffer b, int start) {
		//int counter = 1;
		int result = start;
		//int curByte1 = b.get(result); //this is where QNAME starts
		int curByte1 = b.get(result);
		int curByte2 = b.get(result-1);
		byte co = (byte) 192;
		byte oc = (byte) 12;
		
		while (curByte1!=oc || curByte2!=co) {//we havent reached end of QNAME yet
			result++;
			curByte1 = b.get(result);
			curByte2 = b.get(result-1);
		}
		return result++;
	}

	private static String readRData(ByteBuffer b, int start) {
		StringBuilder mysb = new StringBuilder(100);
		String result;
		int num = b.get(start);
		int index = start;

		while (num != 0) {
			if (num == -64) { //pointer
				int offset = b.get(index + 1);
				result = mysb.toString().concat(readRData(b, offset));
				return result;
			}
			else {
				for(int i = 1; i <= num; i++) {
					mysb.append(Character.toString((char) b.get(index + i)));   
					//System.out.println(Character.toString((char) b.get(count + i)));
				}
				index = index + num + 1;
				mysb.append('.');
				num = b.get(index);
			}			
		}  
		//	result = result.substring(0, result.length() - 2);
		result = mysb.toString().substring(0, mysb.toString().length() - 1);
		return result;
	}

	private static void helper(ByteBuffer b) { //to help debug and look through byte buffer
		int start = 0;
		while (true) {
			int curByte = b.get(start);
			start++;
		}
	}

	private static int byteToInt(byte b) {
		return Integer.parseInt(String.format("%02X", b), 16);
	}
}
