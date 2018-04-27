package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Main {

	static BufferedWriter bufferedWriter = null;
	static BufferedReader bufferedReader = null;
	//khai báo hasmap để lưu các socket được kết nối với từng client
	static HashMap<Integer, Socket> listSocket = new HashMap<>();
	//khai báo hasmap để lưu thông tin của các client kết nối tới server
	static HashMap<Integer, String> listClient = new HashMap<>();
	//Khai báo hàng đợi để lưu nội dung tin nhắn từ các client
	static Queue<Messages> listMessages = new LinkedList<>();
	//biến để tăng dần giá trị cho các id socket
	static int count = 0;

	//Khai báo port mặc đinh
	private static int port = 9999;

	//hàm gửi tin nhắn đến một client với các tham số là socket và nội dung tin nhắn
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

	//Hàm gửi thông tin các client online đến tất cả các client
	private static void sendClientOnline() {
		String clientOnline = "";
		Set<Integer> key = listClient.keySet();
		for (int i : key) {
			clientOnline += listClient.get(i) + ";";
		}
		System.out.println(clientOnline);
		Set<Integer> key1 = listSocket.keySet();
		for (int x : key1) {
			SendMsg(listSocket.get(x), clientOnline);
		}
	}

	//Hàm kiểm tra client online
	private static void checkClientOnline() {
		Set<Integer> key1 = listSocket.keySet();
		for (int x : key1) {
			//server sẽ gửi về cho client một tin nhắn trống nếu không kết nối tới client được thì client đã offline
			if (!SendMsg(listSocket.get(x), ""))
				listClient.remove(x);
		}
	}

	public static void waitMessager() {
		//key là mảng id của các socket trong list
		Set<Integer> key = listSocket.keySet();

		//lặp lần lượt các socket để đợi tin nhắn
		for (int x : key) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						bufferedReader = new BufferedReader(new InputStreamReader(listSocket.get(x).getInputStream()));
						//Đọc tin nhắn từ client gửi
						String content = bufferedReader.readLine();
						//Tách tin nhắn từ client để được các thông tin cần thiết
						//Nếu bắt đều là $$ thì là tin nhắn gửi user name từ client đã được đọc lúc vừa kết nối
						if (!content.startsWith("$$")) {
							//Tách lấy usernamw
							String username = content.split(":")[0];
							//Tách lấy nội dung tin nhắn
							String messager = content.split(":")[1];

							Messages msg = new Messages(username,messager,x);

							listMessages.add(msg);
						}
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
			}).start();
		}

		//Lấy lần lượt tin nhắn trong hàng đợi để gửi cho các client
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!listMessages.isEmpty()){
					Messages msg = listMessages.poll();
					if(msg != null){
						for (int y : key) {
							if (y != msg.getIdSocket()) {
								SendMsg(listSocket.get(y), msg.getUsername() + ": " + msg.getContent());
							}
						}
					}

				}
			}
		}).start();
	}

	public static void main(String[] args) throws IOException {

		//Nhập port cho server
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
					while(true){
						//Kiểm tra client online và gửi về thông tin cho các client
						checkClientOnline();
						sendClientOnline();

						//chờ nhận tin nhắn và gửi cho các client
						new Thread(new Runnable() {
							@Override
							public void run() {
								waitMessager();
							}
						}).start();
					}

				}
			}).start();
		}
	}

}
