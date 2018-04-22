package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class Main {

	static BufferedWriter bufferedWriter = null;
	static BufferedReader bufferedReader = null;
	static HashMap<Integer, Socket> listSocket = new HashMap<>();
	static HashMap<Integer, String> listClient = new HashMap<>();
	static int count = 0;

	private static int port = 9999;

	private static boolean SendMsg(Socket socket, String content) {
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			bufferedWriter.write(content);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void sendClientOnline() {
		String clientOnline = "";
		Set<Integer> key = listClient.keySet();
		for (int i : key) {
			clientOnline += listClient.get(i) + "\n";
		}
		Set<Integer> key1 = listSocket.keySet();
		for (int x : key1) {
			SendMsg(listSocket.get(x), clientOnline);
		}
	}

	private static void checkClientOnline() {
		Set<Integer> key1 = listSocket.keySet();
		for (int x : key1) {
			if (!SendMsg(listSocket.get(x), ""))
				listClient.remove(x);
		}
	}

	public static void waitMessager() {
		Set<Integer> key = listSocket.keySet();
		for (int x : key) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						bufferedReader = new BufferedReader(new InputStreamReader(listSocket.get(x).getInputStream()));
						String content = bufferedReader.readLine();
						if (!content.startsWith("$$")) {
							String username = content.split(":")[0];
							String messager = content.split(":")[1];
							for (int y : key) {
								if (y != x) {
									SendMsg(listSocket.get(y), username + ": " + messager);
								}
							}
						}
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	public static void main(String[] args) throws IOException {

		System.out.println("************************");
		System.out.print("Please enter a port: ");
		port = new Scanner(System.in).nextInt();
		System.out.println("************************");
		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("Server waitting ...");

		while (true) {

			Socket socket = serverSocket.accept();
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String nameClient = bufferedReader.readLine();
			listClient.put(count, nameClient);
			listSocket.put(count, socket);

			
			System.out.println("client " + count + " contnected !");
			
			count++;

			new Thread(new Runnable() {

				@Override
				public void run() {
					waitMessager();
				}
			}).start();
			new Thread(new Runnable() {

				@Override
				public void run() {
					sendClientOnline();
				}
			}).start();
			new Thread(new Runnable() {

				@Override
				public void run() {
					checkClientOnline();
				}
			}).start();
			
		}
	}

}
