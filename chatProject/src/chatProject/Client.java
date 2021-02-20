package chatProject;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import javax.swing.*;

public class Client extends JFrame implements ActionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private JTextArea texto;
    private JTextField txtMsg;
    private JButton btnSend;
    private JButton btnSair;
    private JLabel lblHistorico;
    private JLabel lblMsg;
    private JPanel pnlContent;
    private OutputStream ou ;
    private Writer ouw; 
    private BufferedWriter bfw;
    private JTextField txtNome;
    MulticastSocket mSocket = null;
    String args;

    public Client() throws IOException{
	    JLabel lblMessage = new JLabel("Digite o seu nome: ");
	    txtNome = new JTextField("");
	    Object[] texts = {lblMessage, txtNome };
	    JOptionPane.showMessageDialog(null, texts);
	    pnlContent = new JPanel();
	    texto= new JTextArea(10,30);
	    texto.setEditable(false);
	    texto.setBackground(new Color(240,240,240));
	    txtMsg = new JTextField(30);
	    lblHistorico = new JLabel("Histórico");
	    lblMsg= new JLabel("Mensagem");
	    btnSend = new JButton("Enviar");
	    btnSend.setToolTipText("Enviar Mensagem");
	    btnSair = new JButton("Sair");
	    btnSair.setToolTipText("Sair do Chat");
	    btnSend.addActionListener(this);
	    btnSair.addActionListener(this);
	    btnSend.addActionListener(this);
	    txtMsg.addKeyListener(this);
	    JScrollPane scroll = new JScrollPane(texto);
	    texto.setLineWrap(true);
	    pnlContent.add(lblHistorico);
	    pnlContent.add(scroll);
	    pnlContent.add(lblMsg);
	    pnlContent.add(txtMsg);
	    pnlContent.add(btnSair);
	    pnlContent.add(btnSend);
	    pnlContent.setBackground(Color.LIGHT_GRAY); 
	    texto.setBorder(BorderFactory.createEtchedBorder(Color.BLUE,Color.BLUE));
	    txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
	    setTitle(txtNome.getText());
	    setContentPane(pnlContent);
	    setLocationRelativeTo(null);
	    setResizable(false);
	    setSize(350,310);
	    setVisible(true);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
   

    public void conectar() throws IOException{
        
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


        ou = mSocket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        bfw.write(txtNome.getText()+"\r\n");
        bfw.flush();
    }

    public void enviarMensagem(String msg) throws IOException{
    	if(msg.equals("Sair")){
            bfw.write("Desconectado \r\n");
            texto.append("Desconectado \r\n");
        }else if(msg.equals("@getUserList@")){
        	String send = "getListUsers@" + txtNome.getText();
            bfw.write(send);
        }else{
            bfw.write(msg+"\r\n");
            texto.append( txtNome.getText() + " diz -> " + txtMsg.getText()+"\r\n");
        }
        bfw.flush();
        txtMsg.setText("");
    }

    public void escutar() throws IOException{
        InputStream in = mSocket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        int indexMessage = -1;
        String msg = "";
        
        while(!"Sair".equalsIgnoreCase(msg))
        	
        if(bfr.ready()){
            msg = bfr.readLine();
            if(msg.equals("Sair"))
                texto.append("Servidor caiu! \r\n");
            else if(msg.startsWith("nomes: ")) {
            	indexMessage = msg.indexOf("]");
            	String names = msg.substring(0, (indexMessage +1));
            	JOptionPane.showMessageDialog(null, names, "Lista de usuarios", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                texto.append(msg+"\r\n");
            }
        }
    }

    public void sair() throws IOException{
        enviarMensagem("Sair");
        bfw.close();
        ouw.close();
        ou.close();
        mSocket.close();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if(e.getActionCommand().equals(btnSend.getActionCommand()))
                enviarMensagem(txtMsg.getText());
            
            else if(e.getActionCommand().equals(btnSair.getActionCommand()))
                sair();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            try {
                enviarMensagem(txtMsg.getText());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } 
    }

    @Override
    public void keyReleased(KeyEvent arg0) {}

    @Override
    public void keyTyped(KeyEvent arg0) {}

    public static void main(String []args) throws IOException{
        Client app = new Client();
        app.conectar();
        app.escutar();
    }
}