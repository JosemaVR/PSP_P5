package claveSimetrica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
public class HiloServidorSimetrico extends Thread
{
	DataInputStream fentrada;
	Socket socket;
	boolean fin = false;
	public HiloServidorSimetrico(Socket socket)
	{
		this.socket = socket;
		try
		{
			fentrada = new DataInputStream(socket.getInputStream());
		}
		catch (IOException e)
		{
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}
	// En el método run() lo primero que hacemos
	// es enviar todos los mensajes actuales al cliente que se
	// acaba de incorporar
	public void run()
	{
		ServidorSimetrico.mensaje.setText("Número de conexiones actuales: " +
				ServidorSimetrico.ACTUALES);
		String texto = ServidorSimetrico.textarea.getText();
		EnviarMensajes(texto);
		// Seguidamente, se crea un bucle en el que se recibe lo que el cliente escribe en el chat.
		// Cuando un cliente finaliza con el botón Salir, se envía un * al servidor del Chat,
		// entonces se sale del bucle while, ya que termina el proceso del cliente,
		// de esta manera se controlan las conexiones actuales
		while(!fin)
		{
			try
			{
				byte[] data = new byte[1024];
				fentrada.read(data);
				String cadena = desencriptar(data);
				if(cadena.trim().equals("*"))
				{
					ServidorSimetrico.ACTUALES--;
					ServidorSimetrico.mensaje.setText("Número de conexiones actuales: "
							+ ServidorSimetrico.ACTUALES);
					fin=true;
				}
				// El texto que el cliente escribe en el chat,
				// se añade al textarea del servidor y se reenvía a todos los clientes
				else
				{
					ServidorSimetrico.textarea.append(cadena + "\n");
					texto = ServidorSimetrico.textarea.getText();
					EnviarMensajes(texto);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				fin=true;
			}
		}
	}
	// El método EnviarMensajes() envía el texto del textarea a
	// todos los sockets que están en la tabla de sockets,
	// de esta forma todos ven la conversación.
	// El programa abre un stream de salida para escribir el texto en el socket
	private void EnviarMensajes(String texto)
	{
		for(int i=0; i<ServidorSimetrico.CONEXIONES; i++)
		{
			Socket socket = ServidorSimetrico.tabla[i];
			try
			{
				DataOutputStream fsalida = new
						DataOutputStream(socket.getOutputStream());
				fsalida.write(encriptar(texto));
			}
			catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e)
			{
				e.printStackTrace();
			}
		}
	}


	private String desencriptar(byte[] EncryptedData) throws UnsupportedEncodingException {
		String res = "";
		byte[] keySymme = {0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79}; // ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
		try
		{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			// Reiniciar Cipher al modo desencriptado
			cipher.init(Cipher.DECRYPT_MODE, secretKey, cipher.getParameters());
			res = new String(cipher.update(EncryptedData));
			res = res.replaceAll("[^a-zA-Z0-9ñÑ <>.;:,¿?¡!ºª#$€()/&%*-+]", ""); 
			res = res.replaceAll("QGj", "");
			System.out.println("DESENCRIPTANDO");
			System.out.println("Mensaje encriptado: " + (new String(EncryptedData)).replaceAll("\n", ""));
			System.out.println("Mensaje sin encriptar: " + res);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
	
	public byte[] encriptar(String texto) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnknownHostException {
		if(texto.contains("\n")) {
			int j = texto.split("\n").length - 1;
			texto = texto.split("\n")[j];
		}
		byte[] plainBytes = texto.getBytes();
		byte[] keySymme = {0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79}; // ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
		// Crear objeto Cipher e inicializar modo encriptación
		Cipher cipher = Cipher.getInstance("AES"); // Transformación
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] EncryptedData = cipher.doFinal(plainBytes);	
		System.out.println("ENCRIPTANDO");
		System.out.println("Mensaje encriptado: " + new String(EncryptedData));
		System.out.println("Mensaje sin encriptar: " + texto);
		return EncryptedData;
	}
}