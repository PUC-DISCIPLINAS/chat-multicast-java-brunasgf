package chatProject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server extends Thread {
	private static HashMap<String, BufferedWriter> clientes;
	private String nome;
	private Socket clientSocket;
	private InputStream in;  
	private InputStreamReader inr;  
	private BufferedReader bfr;
	
	public Server(Socket clientSocket){
        this.clientSocket = clientSocket;
        try {
            in  = clientSocket.getInputStream();
            inr = new InputStreamReader(in);
            bfr = new BufferedReader(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String msg;
            OutputStream ou =  this.clientSocket.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw); 
            nome = msg = bfr.readLine();
            clientes.put(nome, bfw);

            while(!"Sair".equalsIgnoreCase(msg) && msg != null) {
                msg = bfr.readLine();
                if(msg.startsWith("@")) {
                    int indexOfSpace = msg.indexOf(" ");
                    String name = msg.substring(1, indexOfSpace);
                    String message = msg.substring(indexOfSpace, msg.length());

                    sendEspecifc(bfw, message, name);
                } else if (msg.startsWith("getListUsers")) {
                	String nome = msg.split("@")[1].trim();
                	sendUsersName(nome);
                } else {
                    sendToAll(bfw, msg);
                }
            }
            
            clientes.remove(nome);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToAll(BufferedWriter bwSaida, String msg) throws  IOException {
        clientes.forEach((k,v) -> {
            BufferedWriter bwS;
            bwS = (BufferedWriter)v;
            if(!(bwSaida == bwS)) {
                try {
                    v.write(nome + " -> " + msg+"\r\n");
                    v.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
        });
    }

    public void sendUsersName(String nome) throws  IOException {
    	BufferedWriter bw = clientes.get(nome);
    	bw.write("nomes: " + clientes.keySet().toString());
    	bw.flush(); 
    }
    
    public void sendEspecifc(BufferedWriter bwSaida, String msg, String nome) throws  IOException {
        BufferedWriter bw = clientes.get(nome);
        bw.write(nome + " -> " + msg+"\r\n");
        bw.flush(); 
    }

    public static void main(String []args) {

    	ServerSocket listenSocket = null;
        
		try {
			// Porta do servidor
			int serverPort = 7896;
            clientes = new HashMap<String, BufferedWriter>();
			
			// Fica ouvindo a porta do servidor esperando uma conexao.
			listenSocket = new ServerSocket(serverPort);
			System.out.println("Servidor: ouvindo porta TCP/7896.");

			while (true) {
				Socket clientSocket = listenSocket.accept();
				new Connection(clientSocket);
                Thread t = new Server(clientSocket);
                t.start();
			}
		} catch (IOException e) {
			System.out.println("Listen socket:" + e.getMessage());
		} finally {
			if (listenSocket != null)
				try {
					listenSocket.close();
					System.out.println("Servidor: liberando porta TCP/7896.");
				} catch (IOException e) {
					/* close falhou */
				}
		}

    }
}

class Connection extends Thread {
	DataInputStream in;
	DataOutputStream out;
	Socket clientSocket;

	public Connection(Socket aClientSocket) {
		try {
			clientSocket = aClientSocket;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			this.start();
		} catch (IOException e) {
			System.out.println("Conex�o:" + e.getMessage());
		}
	}

	public void run() {
		try { // servidor de repeti��o

			String data = in.readUTF(); // le a linha da entrada
			System.out.println("Recebido: " + data);
			out.writeUTF(data);
		} catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
		} catch (IOException e) {
			System.out.println("readline:" + e.getMessage());
		} finally {
			try {
				clientSocket.close();
				System.out.println("Servidor: fechando conex�o com cliente.");
			} catch (IOException e) {
				/* close falhou */
			}
		}

	}
}