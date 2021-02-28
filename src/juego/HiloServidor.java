package juego;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
public class HiloServidor extends Thread
{
	DataInputStream fentrada;
	Socket socket;
	boolean fin = false;

	public HiloServidor(Socket socket)
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
			String cadena = "";
			String txtCadena = "";
			try
			{
				txtCadena = fentrada.readUTF();
				cadena = desencriptar(txtCadena);
				if(cadena=="") {
					ServidorChat.textarea.append("HOLA");
				}
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
					if(fin!=true) {
						if(cadena.contains("SERVIDOR")) {
							ServidorChat.textarea.append(cadena + "\n");
						} else {
							try {
								int num = Integer.parseInt(cadena.split(" ")[1].replaceAll(" ", ""));
								String jugador = cadena.split(" ")[0];
								ServidorChat.textarea.append(cadena + "\n");
								if (num == ServidorChat.numPremio) {
									ServidorChat.textarea.append("SERVIDOR " + jugador + 
											" piensa que el número es el " + num + ". "
											+ "\nSERVIDOR Y HA ACERTADOOOO!!!!" 
											+ "\nSERVIDOR Fin de la partida. \n");
									fin = true;
								} else if (num > 100 || num < 1) {
									ServidorChat.textarea.append("SERVIDOR " + jugador + " piensa que el número es el " 
											+ num + ".\nSERVIDOR El número puede ser cualquiera entra 1 y 100.\n");
								} else if (num > ServidorChat.numPremio) {
									ServidorChat.textarea.append("SERVIDOR " + jugador + " piensa que el número es el " 
											+ num + ".\nSERVIDOR El número es menor a " + num + ".\n");
								} else if (num < ServidorChat.numPremio) {
									ServidorChat.textarea.append("SERVIDOR " + jugador + " piensa que el número es el " 
											+ num + ".\nSERVIDOR El número es mayor a " + num + ".\n");
								}
							} catch (ArrayIndexOutOfBoundsException ex)
							{
								if(cadena.contains("SERVIDOR")==false) {
									ServidorChat.textarea.append("SERVIDOR ¡¡¡ERROR!!! Sólo es posible introducir números.\n");
								}
							}catch (NumberFormatException excepcion) {
								if(cadena.contains("SERVIDOR")==false) {
									ServidorChat.textarea.append("SERVIDOR ¡¡¡ERROR!!! Sólo es posible introducir números.\n");
								}
							}
						}
					}
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

	private String desencriptar(String texto) {
		String res = "";
		byte[] keySymme = {
				0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65,
				0x74, 0x4b, 0x65, 0x79
		}; // ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
		byte[] txt = texto.getBytes();
		DatagramPacket packet = new DatagramPacket(txt, txt.length);
		try
		{
			Cipher cipher = Cipher.getInstance("AES");
			// Reiniciar Cipher al modo desencriptado
			cipher.init(Cipher.DECRYPT_MODE,secretKey, cipher.getParameters());
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
