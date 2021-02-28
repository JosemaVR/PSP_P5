package claveSimetrica;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
public class HiloServidor extends Thread
{
	DatagramSocket fentrada;
	Socket socket;
	boolean fin = false;
	public HiloServidor(Socket socket)
	{
		this.socket = socket;
		try
		{
			fentrada = new DatagramSocket(socket.getLocalPort());
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
		ServidorChat.mensaje.setText("Número de conexiones actuales: " +
				ServidorChat.ACTUALES);
		String texto = ServidorChat.textarea.getText();
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
				DatagramPacket packet = new DatagramPacket(data, data.length);
				fentrada.receive(packet);
				String cadena = desencriptar(packet);
				String txtCadena = new String(packet.getData());
				ServidorChat.textarea.append("ENCRIPTADO> " + txtCadena.replace("\n", "").trim() + "\n");
				if(cadena.trim().equals("*"))
				{
					ServidorChat.ACTUALES--;
					ServidorChat.mensaje.setText("Número de conexiones actuales: "
							+ ServidorChat.ACTUALES);
					fin=true;
				}
				// El texto que el cliente escribe en el chat,
				// se añade al textarea del servidor y se reenvía a todos los clientes
				else
				{
					ServidorChat.textarea.append(cadena + "\n");
					texto = ServidorChat.textarea.getText();
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
		for(int i=0; i<ServidorChat.CONEXIONES; i++)
		{
			Socket socket = ServidorChat.tabla[i];
			try
			{
				DataOutputStream fsalida = new
						DataOutputStream(socket.getOutputStream());
				fsalida.writeUTF(texto);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}


	private String desencriptar(DatagramPacket packet) throws UnsupportedEncodingException {
		String res = "";
		byte[] keySymme = {0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79}; // ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
		try
		{
			Cipher cipher = Cipher.getInstance("AES");
			// Reiniciar Cipher al modo desencriptado
			cipher.init(Cipher.DECRYPT_MODE, secretKey, cipher.getParameters());
			byte[] plainBytesDecrypted = cipher.doFinal(packet.getData(),
					packet.getOffset(), packet.getLength());
			res = new String(plainBytesDecrypted);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
}
