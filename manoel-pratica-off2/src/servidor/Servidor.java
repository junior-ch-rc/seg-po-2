package servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.ContaBancaria;
import common.Mensagem;
import common.MeuHash;
import common.MeuRSA;

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
		private BigInteger[] chaves;
		private List<BigInteger> chavePublica; // {e, n}
		private List<BigInteger> chavePrivada; // {d, n}
		private List<BigInteger> chavePublicaCliente;
		
		public ImplServidor(Socket clientSocket) {
			
			chaves = MeuRSA.gerarChaves();
			
			chavePublica = new ArrayList<BigInteger>(); // {e, n}
			chavePublica.add(chaves[0]);
			chavePublica.add(chaves[2]);
			
			chavePrivada = new ArrayList<BigInteger>(); // {d, n}
			chavePrivada.add(chaves[1]);
			chavePrivada.add(chaves[2]);
			
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

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				entrada = new ObjectInputStream(clientSocket.getInputStream());
				
				// Identificação do terminal cliente
				clientID = (String) entrada.readObject();
				System.out.println("Cliente " + clientID + " conectado.");
				
				// Adiciona o cliente ao mapa
				clientes.put(clientID, clientSocket);
				
				// Aguarda as mensagens do cliente
				String resposta = null;
				saida = new ObjectOutputStream(clientSocket.getOutputStream());
				
				// Realizando troca de chaves pública - recebendo chave pública do cliente
				chavePublicaCliente = (List<BigInteger>) entrada.readObject();
				
				// Realizando troca de chaves pública - enviando chave pública do servidor
				saida.writeObject(chavePublica);
				saida.flush();
				
				List<BigInteger> mensagemCifradaRecebida = null;
				while ((mensagemCifradaRecebida = (List<BigInteger>) entrada.readObject()) != null) {
					
					System.out.println("Mensagem cifrada do cliente: "  + clientID + " " + mensagemCifradaRecebida);
					
					// Decifra e recebe bytes da mensagem
					byte[] mensagemDecifradaBytes = MeuRSA.decifrarBytes(mensagemCifradaRecebida, chavePrivada.get(0), chavePrivada.get(1));
					
					// Cria um ByteArrayInputStream para ler os bytes
					ByteArrayInputStream bais = new ByteArrayInputStream(mensagemDecifradaBytes);

					// Cria um ObjectInputStream para desserializar o objeto a partir dos bytes
					ObjectInputStream ois = new ObjectInputStream(bais);

					// Lê o objeto do ObjectInputStream
					Object obj = ois.readObject();

					// Fecha o ObjectInputStream
					ois.close();

					// Cast para o tipo Mensagem
					// Supondo que o objeto é uma instância de Mensagem
					Mensagem m = (Mensagem) obj;
					
					String[] parts = m.getMensagem().split(" ");
					
					// Se não estiver autenticado
					if (parts[0].equals("0")) {
						
						switch(parts[1].charAt(0)) {
							
							// Autenticação (com HMAC)
							case '1': {
								
								// Faz a leitura do hash assinado e verifica se é autentico
								List<BigInteger> hashAssinado = (List<BigInteger>) entrada.readObject();
								String hashAutenticado = new String(
										MeuRSA.decifrarBytes(hashAssinado, chavePublicaCliente.get(0), chavePublicaCliente.get(1))
								);
								
								// Gera o próprio hash da mensagem
								
								// Transformando mensagem cifrada em bytes para gerar o hash
			                	ByteArrayOutputStream baos2 = new ByteArrayOutputStream(); // Para armazenar os bytes
			                    ObjectOutputStream oos2 = new ObjectOutputStream(baos2); // Para serializar o objeto em bytes
			                    oos2.writeObject(mensagemCifradaRecebida); // Serializa o objeto
			                    oos2.close();
								
			                    // Gerando o hash
			                    String hashGerado = MeuHash.resumo(baos2.toByteArray(), "SHA-256");
			                    
			                    if (hashAutenticado.equals(hashGerado)) {
			                    	boolean autenticacao = bank.authenticate(Integer.parseInt(parts[2]), parts[3]);
			                    	resposta = autenticacao ? "Autenticado" : "Não Autenticado";			                    	
			                    } else {
			                    	resposta = "Requisição inválida";
			                    }
								
								break;
								
							}
							
							// Cadastro de nova conta
							case '2': {
								int cadastro = bank.createAccount((ContaBancaria) m.getObjeto());
								resposta = "Número da conta: " + cadastro;
								break;
							}
						
						}
						
					}
					else {
						switch(parts[1].charAt(0)) {
							// Sacar
							case '1':
								break;
								
							//Depositar
							case '2':
								break;
								
							//Transferir
							case '3':
								break;
							
							//Consultar saldo
							case '4':
								break;
							
							//Realizar investimento poupança
							case '5':
								break;
								
							//Realizar investimento fixa
							case '7':
								break;
						}
					}
					
					System.out.println("Mensagem aberta do cliente: " + clientID + " " + m.getMensagem());
					
					// Cifrando resposta com a chave pública do cliente
	                List<BigInteger> respostaCifrada = MeuRSA.cifrarBytes(resposta.getBytes(StandardCharsets.UTF_8), chavePublicaCliente.get(0), chavePublicaCliente.get(1));
					saida.writeObject(respostaCifrada);
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