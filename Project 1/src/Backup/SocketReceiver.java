package Backup;

import sun.plugin.dom.exception.InvalidStateException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jose on 26-03-2014.
 */
public class SocketReceiver extends Thread {

	public HashSet<PBMessage> received;
	public String mcast_adrr;
	public int mcast_port;
	public Vector<String> addrs;
	public Vector<Integer> ports;

	SocketReceiver(Vector<String> a, Vector<Integer> p, int type) {
		mcast_adrr = a.get(type);
		mcast_port = p.get(type);
		received = new HashSet<PBMessage>();
	}

	public void run() {

		MulticastSocket mSocket = null;
		InetAddress iAddress;

		try {
			DatagramPacket packet;

			mSocket = new MulticastSocket(mcast_port);

			/* Join the Multicast Group */
			iAddress = InetAddress.getByName(mcast_adrr);
			mSocket.joinGroup(iAddress);

			while (true) {

				byte[] buf = new byte[PeerThread.packetSize];
				packet = new DatagramPacket(buf, buf.length);

				// receive the packets
				mSocket.receive(packet);

				try {

    				PBMessage temp_message = PBMessage.createMessageFromType(packet.getData(), packet.getLength());

                    if (temp_message != null) {
                        if (received.add(temp_message)) {
                            //ProtocolHandler temp_handler = new ProtocolHandler(addrs, ports, temp_message); // TODO FINISH THE HANDLER
							temp_handler.run();
							System.out.println("RECEIVED FROM " + packet.getAddress().getHostAddress() + " TYPE: " + temp_message.getType());
                        }
                    } else {
                        System.out.println("MESSAGE DISCARDED!");
                    }

		    		} catch(InvalidStateException e){
			    		e.printStackTrace();
			    	}

				if (false) break;
			}

			mSocket.leaveGroup(iAddress);

		} catch (IOException e) {
			e.printStackTrace();
			mSocket.close();
		}
		mSocket.close();
	}

	public int numMessagesByIP() {

		int num = 0;

		for (Map.Entry<String, PBMessage> entry : received.entrySet()) {

			if (entry.getValue().getType() == "STORED")
				num++;
		}
		return num;
	}

	public void clearMessages(){
		this.received.clear();
	}
}
