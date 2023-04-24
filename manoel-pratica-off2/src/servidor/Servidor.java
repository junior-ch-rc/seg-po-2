package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import common.ContaBancaria;
import common.Mensagem;

public class Servidor {

	// Porta utilizada pelo servidor
	private static final int porta = 54321;
	
	// Mapa para armazenar os clientes conectados
	private static Map<String, Socket> clientes = new HashMap<>();
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	
	public static void main(String[] args) throws IOException {
		serverSocket = new ServerSocket(porta);
		System.out.println("Servidor rodando na porta: " + serverSocket.getLocalPort());
		
		while(true) {
			clientSocket = serverSocket.accept();
			Thread thread = new Thread(new ImplServidor(clientSocket));
			thread.start();
		}
	}
	
	// Inner Class para tratar as conexões dos clientes
	private static class ImplServidor implements Runnable {
		
		private Socket clientSocket;
		private ObjectInputStream entrada;
		private ObjectOutputStream saida;
		private String clientID;
		private BankImpl bank;
		
		public ImplServidor(Socket clientSocket) {
			this.clientSocket = clientSocket;
			
			// Conexão com o banco
			try {
				bank = new BankImpl("yuowtzxi", "2aNLFxtqy2IQTOXCsaHAXwH89XaFfmzR", "jdbc:postgresql://babar.db.elephantsql.com:5432/yuowtzxi");
				System.out.println("Conexão com Banco de Dados bem sucedida");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		@Override
		public void run() {
			try {
				entrada = new ObjectInputStream(clientSocket.getInputStream());
				
				// Troca de chaves e consolidação da conexão
				clientID = (String) entrada.readObject();
				System.out.println("Cliente " + clientID + " conectado.");
				
				// Adiciona o cliente ao mapa
				clientes.put(clientID, clientSocket);
				
				// Aguarda as mensagens do cliente
				String resposta = null;
				saida = new ObjectOutputStream(clientSocket.getOutputStream());
				
				Mensagem m = null;
				while ((m = (Mensagem) entrada.readObject()) != null) {
					String[] parts = m.getMensagem().split(" ");
					
					// Se não estiver autenticado
					if (parts[0].equals("0")) {
						
						switch(parts[1].charAt(0)) {
							
							// Autenticação
							case '1': {
								System.out.println(Integer.parseInt(parts[2]));
								boolean autenticacao = bank.authenticate(Integer.parseInt(parts[2]), parts[3]);
								resposta = autenticacao ? "1" : "0";
								break;
								
							}
							
							// Cadastro de nova conta
							case '2': {
								int cadastro = bank.createAccount((ContaBancaria) m.getObjeto());
								resposta = cadastro + "";
								break;
							}
						
						}
						
					}
					
					
					System.out.println("Mensagem do cliente: " + clientID + m.getMensagem());
					saida.writeObject(resposta);
					saida.flush();
						
				}	
				
			} catch (IOException e) {
				System.err.println("Erro de conexão com o cliente: " + clientID);
				e.printStackTrace();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				
				// Remove o cliente do mapa ao finalizar a conexão
				clientes.remove(clientID);
				try {
					clientSocket.close();
					entrada.close();
					saida.close();
				} catch (IOException e2) {
					System.err.println("Falha ao desconectar o cliente: " + clientID);
					e2.printStackTrace();
				}
				
				System.out.println("Cliente: " + clientID + " desconectado.");
				
			}
			
		}

	}
}