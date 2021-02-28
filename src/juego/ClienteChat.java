package juego;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClienteChat extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	Socket socket;
	DataInputStream fentrada;
	DataOutputStream fsalida;
	String nombre;
	static JTextField mensaje = new JTextField();
	private JScrollPane scrollpane;
	static JTextArea textarea;
	JButton boton = new JButton("Enviar");
	JButton desconectar = new JButton("Salir");
	boolean repetir = true;

	public ClienteChat(Socket socket, String nombre) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
	{
		// Prepara la pantalla. Se recibe el socket creado y el nombre del cliente
		super(" Conexión del cliente chat: " + nombre);
		setLayout(null);
		mensaje.setBounds(10, 10, 400, 30);
		add(mensaje);
		textarea = new JTextArea();
		scrollpane = new JScrollPane(textarea);
		scrollpane.setBounds(10, 50, 400, 300);
		add(scrollpane);
		boton.setBounds(420, 10, 100, 30);
		add(boton);
		desconectar.setBounds(420, 50, 100, 30);
		add(desconectar);
		textarea.setEditable(false);
		boton.addActionListener(this);
		this.getRootPane().setDefaultButton(boton);
		desconectar.addActionListener(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.socket = socket;
		this.nombre = nombre;
		// Se crean los flujos de entrada y salida.
		// En el flujo de salida se escribe un mensaje
		// indicando que el cliente se ha unido al Chat.
		// El HiloServidor recibe este mensaje y
		// lo reenvía a todos los clientes conectados
		try
		{
			fentrada = new DataInputStream(socket.getInputStream());
			fsalida = new DataOutputStream(socket.getOutputStream());
			String texto = "SERVIDOR "+nombre+" entra en la partida.";
			fsalida.writeUTF(encriptar(texto));
		}
		catch (IOException ex)
		{
			System.out.println("Error de E/S");
			ex.printStackTrace();
			System.exit(0);
		}
	}
	// El método main es el que lanza el cliente,
	// para ello en primer lugar se solicita el nombre o nick del
	// cliente, una vez especificado el nombre
	// se crea la conexión al servidor y se crear la pantalla del Chat(ClientChat)
	// lanzando su ejecución (ejecutar()).
	public static void main(String[] args) throws Exception
	{
		int puerto = 44444;
		String nombre = JOptionPane.showInputDialog("Introduce tu nombre o nick:");
		nombre = nombre.toUpperCase();
		Socket socket = null;
		try
		{
			socket = new Socket("localhost", puerto);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" 
					+ ex.getMessage(), "<<Mensaje de Error:1>>", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		if(!nombre.trim().equals(""))
		{
			ClienteChat cliente = new ClienteChat(socket, nombre);
			cliente.setBounds(0,0,540,400);
			cliente.setVisible(true);
			cliente.ejecutar();
		}
		else
		{
			System.out.println("El nombre está vacío...");
		}
	}
	// Cuando se pulsa el botón Enviar,
	// el mensaje introducido se envía al servidor por el flujo de salida
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==boton)
		{
			String texto = nombre + " " + mensaje.getText();
			try
			{
				fsalida.writeUTF(encriptar(texto));
			}
			catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex)
			{
				ex.printStackTrace();
			}
			mensaje.setText("");
			mensaje.setEditable(false);
		}
		// Si se pulsa el botón Salir,
		// se envía un mensaje indicando que el cliente abandona el chat
		// y también se envía un * para indicar
		// al servidor que el cliente se ha cerrado
		else if(e.getSource()==desconectar)
		{
			String texto = "SERVIDOR "+nombre+" abandona la partida.";
			try
			{
				mensaje.setEditable(false);
				fsalida.writeUTF(encriptar(texto));
				fsalida.writeUTF(encriptar("*"));
				repetir = false;
			}
			catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	// Dentro del método ejecutar(), el cliente lee lo que el
	// hilo le manda (mensajes del Chat) y lo muestra en el textarea.
	// Esto se ejecuta en un bucle del que solo se sale
	// en el momento que el cliente pulse el botón Salir
	// y se modifique la variable repetir
	public void ejecutar()
	{
		String texto = "";
		try 
		{
			texto = fentrada.readUTF();
			textarea.setText(texto);
			mensaje.setEditable(true);
			if(texto.contains("Y HA ACERTADOOOO")) {
				mensaje.setEditable(false);
			}
		}
		catch (IOException ex)
		{
			JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" 
					+ ex.getMessage(), "<<Mensaje de Error:2>>", JOptionPane.ERROR_MESSAGE);
			repetir = false;
		}
		while(repetir)
		{
			try 
			{
				texto = fentrada.readUTF();
				textarea.setText(texto);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mensaje.setEditable(true);
				if(texto.contains("Y HA ACERTADOOOO")) {
					mensaje.setEditable(false);
				}
			}
			catch (IOException ex)
			{
				JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" 
						+ ex.getMessage(), "<<Mensaje de Error:2>>", JOptionPane.ERROR_MESSAGE);
				repetir = false;
			}
		}
		try
		{
			socket.close();
			System.exit(0);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public String encriptar(String texto) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] plainBytes = texto.getBytes();
		byte[] keySymme = {
				0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63,
				0x72, 0x65, 0x74, 0x4b, 0x65, 0x79
		};// ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
		// Crear objeto Cipher e inicializar modo encriptación
		Cipher cipher = Cipher.getInstance("AES"); // Transformación
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] EncryptedData = cipher.doFinal(plainBytes);		
		return new String(EncryptedData);
	}
}