package chatProject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class Multicast {
	
	public static void main(String args[]) {
		
		MulticastSocket mSocket = null;
		
		try {
			InetAddress groupIp = InetAddress.getByName(args[1]);
			
			mSocket = new MulticastSocket(6789);
			mSocket.joinGroup(groupIp);
			
			byte[] message = args[0].getBytes();
			DatagramPacket messageOut = new DatagramPacket(message, message.length, groupIp, 6789);
			mSocket.send(messageOut);
			byte[] buffer = new byte[1000];
			for (int i = 0; i < 3; i++) { // get messages from others in group
				DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
				mSocket.receive(messageIn);
				System.out.println("Recebido:" + new String(messageIn.getData()).trim());
			}
			mSocket.leaveGroup(groupIp);
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (mSocket != null)
				mSocket.close();
		}
	}


}
