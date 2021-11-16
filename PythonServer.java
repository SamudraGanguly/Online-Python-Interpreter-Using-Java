import java.io.*;
import java.net.*;
class PythonServer {
    public static InetAddress inetAddress;
    public static String program;
    public static int port;
    public static InputServerThread inputThread;
    public static OutputServerThread outputThread;
    public static ErrorServerThread errorThread;
    public static void main(String[] args) {
	try {
	    System.out.println(InetAddress.getLocalHost().getHostAddress());
	    DatagramSocket datagramSocket = new DatagramSocket(194);
	    byte[] receiveData = new byte[1024 * 10];
	    DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
	    datagramSocket.receive(datagramPacket);
	    inetAddress = datagramPacket.getAddress();
	    System.out.println(inetAddress);
	    port = datagramPacket.getPort();
	    program = new String(datagramPacket.getData()).trim();
	    System.out.println(program);
	    FileWriter fileWriter = new FileWriter("testjava.py");
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write(program);
            writer.close();
	    Process process = Runtime.getRuntime().exec("python testjava.py");
	    inputThread = new InputServerThread(datagramSocket, process);
	    outputThread = new OutputServerThread(datagramSocket, process);
	    errorThread = new ErrorServerThread(datagramSocket, process);
	    inputThread.start();
	    outputThread.start();
	    errorThread.start();

	}
	catch (Exception e) {
	    System.out.println("Main: " + e.toString());
	}
    }
}

class InputServerThread extends Thread {
    DatagramSocket datagramSocket;
    Process process;
    OutputStreamWriter outputStreamWriter;
    BufferedWriter bufferedWriter;
    InputServerThread(DatagramSocket datagramSocket, Process process) {
	this.datagramSocket = datagramSocket;
	this.process = process;
	outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
	bufferedWriter = new BufferedWriter(outputStreamWriter);
    }
    @Override
    public void run() {
	try {
	    while (process.isAlive()) {
		byte[] receiveData = new byte[1024 * 10];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		datagramSocket.receive(receivePacket);
		String input = new String(receivePacket.getData()).trim();
		//System.out.println(input);
		bufferedWriter.write(input);
		bufferedWriter.newLine();
		bufferedWriter.flush();
	    }
	    bufferedWriter.close();
	    outputStreamWriter.close();
	}
	catch (SocketException socketException) {}
	catch (Exception e) {	
	    System.out.println("InputThread: " + e.toString());
	}
    }
}

class OutputServerThread extends Thread {
    DatagramSocket datagramSocket;
    Process process;
    InputStream inputStream;
    InputStreamReader inputStreamReader;
    BufferedReader bufferedReader;
    OutputServerThread(DatagramSocket datagramSocket, Process process) {
	this.datagramSocket = datagramSocket;
	this.process = process;
	inputStream = process.getInputStream();
	inputStreamReader = new InputStreamReader(process.getInputStream());
	bufferedReader = new BufferedReader(inputStreamReader);
    }
    @Override
    public void run() {
	try {
	    String output;
	    int i;
	    char ch;
	    /*
	    while ((output = bufferedReader.readLine()) != null) {
		System.out.println(output);
		byte[] sendData = output.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, PythonServer.inetAddress, PythonServer.port);
		datagramSocket.send(sendPacket);
	    }
	    */
	    while (process.isAlive())
	    {
	    	byte[] sendData = new byte[inputStream.available()];
		inputStream.read(sendData);
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, PythonServer.inetAddress, PythonServer.port);
		datagramSocket.send(sendPacket);
	    }

	    if (!process.isAlive()) {
		while (PythonServer.errorThread.isAlive());
		System.out.println("\nFinished");
		byte[] sendData = "0#GANGULY#".getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, PythonServer.inetAddress, PythonServer.port);
		datagramSocket.send(sendPacket);
		datagramSocket.close();
	    }
	}
	catch (Exception e) {
	    System.out.println("OutputThread: " + e.toString());
	    datagramSocket.close();
	}
    }
}

class ErrorServerThread extends Thread {
    DatagramSocket datagramSocket;
    Process process;
    InputStream errorStream;
    InputStreamReader errorStreamReader;
    BufferedReader bufferedReader;
    ErrorServerThread(DatagramSocket datagramSocket, Process process) {
	this.datagramSocket = datagramSocket;
	this.process = process;
	errorStream = process.getErrorStream();
	errorStreamReader = new InputStreamReader(process.getErrorStream());
        BufferedReader bufferedReader = new BufferedReader(errorStreamReader);
    }
    @Override
    public void run() {
	try {
	    while (process.isAlive());
	    byte[] sendData = new byte[errorStream.available()];
	    errorStream.read(sendData);
	    System.out.println(new String(sendData));
	    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, PythonServer.inetAddress, PythonServer.port);
	    datagramSocket.send(sendPacket);
	}

	catch (Exception e) {
	    System.out.println(e.toString());
	}
    }
}