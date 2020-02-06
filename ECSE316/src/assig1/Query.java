package assig1;

import java.nio.ByteBuffer;

public class Query {

	private int timeout = 5;
	private int maxRetries = 3;
	private int port = 53;
	private String server;
	private String name;
	private queryType query = queryType.A;
	enum queryType {MX, NS, A};
	private int requestID;

	Query(String server, String name){
		this.server = server;
		this.name = name;
	}

	Query(String[] args){
		
		try{
			this.server = args[args.length-2].substring(1);
			this.name = args[args.length-1];
			
			for(int i = 0; i < args.length-2; i++) {
				switch(args[i].trim()) {
				case "-t": this.timeout = Integer.parseInt(args[i+1]); i++; break;
				case "-r": this.maxRetries = Integer.parseInt(args[i+1]); i++; break;
				case "-p": this.port = Integer.parseInt(args[i+1]); i++; break;
				case "-mx": this.query = queryType.MX; i++; break; 
				case "-ns": this.query = queryType.NS; i++; break;
				default: System.out.println("Command unknown"); break;
				}
			}
		}
		catch(Exception ex) {
			throw ex;
		}
	}

	public Query() {
	}

	public String toString() {
		return (this.server + ", " + this.name + ", -t " + this.timeout + ", -r " + this.maxRetries + ", -p " + this.port);
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public queryType getQuery() {
		return query;
	}

	public void setQuery(queryType query) {
		this.query = query;
	}
	
	public int getRequestID() {
		return requestID;
	}

	public byte[] getByteServer() {
		
		String[] s = this.getServer().split("\\.");
		byte[] b = new byte[s.length];
		
		for(int i = 0; i < b.length; i++) {
			b[i] = (byte)(Integer.parseInt(s[i]));
			//System.out.println(b[i]);
		}
		return b;
	}

	public byte[] generateQuery() {
		//request id
		byte id1 = (byte) (Math.random()*254);
		byte id2 = (byte) (Math.random()*254);
		this.requestID = id1 + id2;
		byte[] b= {(byte)01, (byte)00, (byte)00, (byte)01, (byte)00, (byte)00, (byte)00, (byte)00, (byte)00, (byte)00};
		
		//create buffer with header
		ByteBuffer buff = ByteBuffer.allocateDirect(1024);
		buff.put(id1);
		buff.put(id2);
		buff.put(b);
		
		//adding name in bytes
		String[] s = this.name.split("\\.");
		for(int i = 0; i < s.length; i++) {
			buff.put((byte)(s[i].length()));
			//traverse string and place each char in the buffer
			for(int j = 0; j < s[i].length(); j++) {
				//buff.asCharBuffer().put("a"); 
				buff.put((byte)(s[i].charAt(j)));
				//System.out.println(s[i].charAt(j));
			}
		}
		//marking end of label
		buff.put((byte)(0));
		switch(this.query) {
		case A: buff.put((byte)(00)); buff.put((byte)(01)); break;
		case NS: buff.put((byte)(00)); buff.put((byte)(02)); break;
		case MX: buff.put((byte)(00)); buff.put((byte)(15)); break;
		//default: throws ("Error writing query");
		}
		
		//adding internet label 00 01 at the end
		buff.put((byte)(00)); 
		buff.put((byte)(01)); 
		buff.flip();
		byte[] result = new byte[buff.remaining()];
		buff.get(result, 0, result.length);
		return result;
	}

}
