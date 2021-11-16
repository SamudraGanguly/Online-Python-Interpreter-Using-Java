import java.io.*;
import java.net.*;
import java.util.*;
class RunPython {
    public static boolean key = true;
    public static void main(String[] args) {
	try {
	    DatagramSocket datagramSocket = new DatagramSocket();
	    System.out.println("Enter the InetAddress of the server as shown in the server code");
	    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	    InetAddress inetAddress = InetAddress.getByName(input.readLine());
	    DatagramPacket sendPacket;
	    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
	    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	    String line, program, output;
	    line = "";
	    program = "";
	    System.out.println("Write the program. End with \"End\" statement.");
	    while (true) {
	    	line = bufferedReader.readLine();
		if (line.equalsIgnoreCase("End"))
		    break;
	    	program = program + line + "\n";
		
	    }
	    byte[] sendData = program.getBytes();
	    sendPacket = new DatagramPacket(sendData, sendData.length, inetAddress, 194);
	    datagramSocket.send(sendPacket);
	    InputClientThread inputThread = new InputClientThread(datagramSocket, inetAddress, bufferedReader);
	    OutputClientThread outputThread = new OutputClientThread(datagramSocket);
	    inputThread.start();
	    outputThread.start();
	    System.out.println();
	}
	catch (Exception e) {
	    System.out.println(e.getMessage());
	}
    }
}

class InputClientThread extends Thread {
    DatagramSocket datagramSocket;
    InetAddress inetAddress;
    BufferedReader bufferedReader;
    DatagramPacket sendPacket;
    InputClientThread(DatagramSocket datagramSocket, InetAddress inetAddress, BufferedReader bufferedReader) {
	this.datagramSocket = datagramSocket;
	this.inetAddress = inetAddress;
	this.bufferedReader = bufferedReader;
    }
    @Override
    public void run() {
	try {
	    String input;
	    byte[] sendData;
	    while (RunPython.key) {
		input = bufferedReader.readLine();
		if (!RunPython.key)
		    break;
		sendData = input.getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, inetAddress, 194);
		datagramSocket.send(sendPacket);
	    }
	}
	catch (SocketException e) {}
	catch (Exception e) {
	    System.out.println(e.getMessage());
	}
    }
}

class OutputClientThread extends Thread {
    DatagramSocket datagramSocket;
    DatagramPacket receivePacket;
    OutputClientThread(DatagramSocket datagramSocket) {
	this.datagramSocket = datagramSocket;
    }
    @Override
    public void run() {
	try {
	    String output;
	    byte[] receiveData;
	    while (RunPython.key) {
		receiveData = new byte[10*1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		datagramSocket.receive(receivePacket);
		output = f(receivePacket.getData());
		if (!output.equals("0#GANGULY#")) {
		    System.out.print(output);
		}
		else
		    System.out.println("\nEnter any key to exit");
		RunPython.key = !output.equals("0#GANGULY#");
	    }
	    datagramSocket.close();
	}
	catch (SocketException e) {}
	catch (Exception e) {
	    System.out.println(e.getMessage());
	}
    }
    public String f(byte[] b) {
	byte[] d;
	int i, c = 0;
	for (i = 0; b[i] != 0; i++)
	    c++;
	d = new byte[c];
	for (i = 0; i < c; i++)
	    d[i] = b[i];
	return new String(d);
    }
}