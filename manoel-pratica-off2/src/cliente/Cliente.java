package cliente;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import common.ContaBancaria;
import common.Mensagem;
import common.MeuHash;
import common.MeuRSA;

public class Cliente {

    private static final String servidorIP = "localhost";
    private static final int porta = 54321;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Scanner reader = new Scanner(System.in);
		BigInteger[] chaves = MeuRSA.gerarChaves();
		
		List<BigInteger> chavePublica = new ArrayList<BigInteger>(); // {e, n}
		chavePublica.add(chaves[0]);
		chavePublica.add(chaves[2]);
		
		List<BigInteger> chavePrivada = new ArrayList<BigInteger>(); // {d, n}
		chavePrivada.add(chaves[1]);
		chavePrivada.add(chaves[2]);
		
		List<BigInteger> chavePublicaServidor;
		
        try {
        	
        	Socket socket = new Socket(servidorIP, porta);
        	System.out.println("Cliente conectado ao servidor " + servidorIP + ":" + porta);
        	
    		ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
    		
    		// Identificação do terminal
    		System.out.print("Digite o id do terminal cliente: ");
    		String clientID = reader.nextLine();
    		saida.writeObject(clientID);
    		saida.flush();
    		
    		ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
    		
    		// Estabelecimento de troca de chaves públicas - enviando a do cliente
    		saida.writeObject(chavePublica);
    		saida.flush();
    		
    		// Estabelecimento de troca de chaves públicas - recebendo a do servidor
    		chavePublicaServidor = (List<BigInteger>) entrada.readObject();
    		
            System.out.println("Terminal pronto!");

            int opc = -1;
            boolean autenticado = false;
            
            while (opc != 0) {
            	
            	Mensagem m = new Mensagem();
            	
            	if (autenticado)
            		exibirMenuClienteAutenticado();
            	else
            		exibirMenuClienteNaoAutenticado();
            	
                opc = Integer.parseInt(reader.nextLine());
                
                if (autenticado) {
                	// Swich para autenticado
                	String numero = "";
                	String valor = "";
                	
                	switch(opc) {
                		case 1:
                			//Sacar
	                		System.out.println("Digite o valor para sacar: ");
	                		valor = reader.nextLine();
	                		
	                		m.setMensagem("1 1 " + numero + " " + valor);
	                		m.setObjeto(null);
	                		
                			break;
                		case 2:
                			//Depositar
	                		System.out.println("Digite o valor para depositar: ");
	                		valor = reader.nextLine();
	                		
	                		m.setMensagem("1 2 " + numero + " " + valor);
	                		m.setObjeto(null);
                			break;
                		case 3:
                			//Transferir
	                		System.out.println("Digite a conta bancária que quer transferir: ");
	                		String trasferTo = reader.nextLine();
	                		System.out.println("Digite o valor para transferir: ");
	                		valor = reader.nextLine();
	                		
	                		m.setMensagem("1 3 " + numero + " " + trasferTo + " " + valor);
	                		m.setObjeto(null);
                			break;
                		case 4:
                			//Consultar saldo
	                		
	                		m.setMensagem("1 4 " + numero);
	                		m.setObjeto(null);
                			break;
                		case 5:
                			//Realizar investimentos
                			System.out.println("--------------------------------------");
                			System.out.println("1. Poupança");
                			System.out.println("2. Renda Fixa");
                			System.out.println("0. Voltar");
                			switch(Integer.parseInt(reader.nextLine())) {
                				case 1:
        	                		System.out.println("Digite o valor para investir: ");
        	                		valor = reader.nextLine();
        	                		
        	                		m.setMensagem("1 5 " + numero + " " + valor);
        	                		m.setObjeto(null);
        	                		break;
        	                		
                				case 2:
        	                		System.out.println("Digite o valor para investir: ");
        	                		valor = reader.nextLine();
        	                		
        	                		m.setMensagem("1 7 " + numero + " " + valor);
        	                		m.setObjeto(null);
        	                		break;
        	                	
        	                	default: break;
                			}
                			break;
	                	case 6: {
	                		m = null;
	                		autenticado = false;
	                		break;
	                	}
                	}
                	
                } else {
                	switch(opc) {
                	
	                	// Essa função usa autenticação de mensagem (HMAC)
	                	case 1: {
	                		
	                		System.out.println("Digite sua conta bancária: ");
	                		String numero = reader.nextLine();
	                		System.out.println("Digite a sua senha: ");
	                		String senha = reader.nextLine();
	                		
	                		m.setMensagem("0 1 " + numero + " " + senha);
	                		m.setObjeto(null);
	                		
	                		break;
	                	}
	                	
	                	case 2: {
	                		
	                		ContaBancaria novaConta = new ContaBancaria();
	                		
	                		System.out.println("Digite seu cpf: ");
	                		novaConta.setCpf(reader.nextLine());
	                		System.out.println("Digite seu nome: ");
	                		novaConta.setNome(reader.nextLine());
	                		System.out.println("Digite seu endereço: ");
	                		novaConta.setEndereco(reader.nextLine());
	                		System.out.println("Digite sua data de nascimento: ");
	                		novaConta.setNascimento(reader.nextLine());
	                		System.out.println("Digite seu telefone: ");
	                		novaConta.setTelefone(reader.nextLine());
	                		System.out.println("Digite sua senha: ");
	                		novaConta.setSenha(reader.nextLine());
	                		
	                		m.setMensagem("0 2");
	                		m.setObjeto(novaConta);
	                		
	                		break;
	                	}
	                	
	                	case 0: {
	                		
	                		m = null;
	                		break;
	                		
	                	}
                	
                	}
                }
                
                List<BigInteger> mensagemCifrada = null;
                
                // Cifrando mensagem com a chave pública do servidor se mensagem não for null (ela é null quando o usuário sair)
                if (m != null) {
	                // Transformando o objeto mensagem em bytes
	                ByteArrayOutputStream baos = new ByteArrayOutputStream(); // Para armazenar os bytes
	                ObjectOutputStream oos = new ObjectOutputStream(baos); // Para serializar o objeto em bytes
	                oos.writeObject(m); // Serializa o objeto
	                oos.close();
	                
	                mensagemCifrada = MeuRSA.cifrarBytes(baos.toByteArray(), chavePublicaServidor.get(0), chavePublicaServidor.get(1));
                }
                
                // Envia mensagem cifrada para o servidor a menos que a operação não seja deslogar (não necessita enviar nada ao servidor)
                if (opc != 6) {
                	saida.writeObject(mensagemCifrada);
                	saida.flush();                	
                }
                
                // Verifica se o usuário está se autenticando para mandar o hash da mensagem em seguida
                if (opc == 1 && !autenticado) {
                	
                	// Transformando mensagem cifrada em bytes
                	ByteArrayOutputStream baos2 = new ByteArrayOutputStream(); // Para armazenar os bytes
                    ObjectOutputStream oos2 = new ObjectOutputStream(baos2); // Para serializar o objeto em bytes
                    oos2.writeObject(mensagemCifrada); // Serializa o objeto
                    oos2.close();
                	
                    // Tirando hash da mensagem cifrada
                	String hash = MeuHash.resumo(baos2.toByteArray(), "SHA-256");
                	
                	// Assinando o hash
                	List<BigInteger> hashAssinado = MeuRSA.cifrarBytes(hash.getBytes(StandardCharsets.UTF_8), chavePrivada.get(0), chavePrivada.get(1));
                	
                	// Envia o hash assinado
                	saida.writeObject(hashAssinado);
                	saida.flush();
                	
                }

                // Recebe resposta do servidor se não escolher fechar o programa ou estiver deslogando
                if (opc != 0 && opc != 6) {
                	List<BigInteger> respostaCifrada = (List<BigInteger>) entrada.readObject();
                	System.out.println("Resposta cifrada do servidor: " + respostaCifrada);
                	
                	// Decifra e recebe bytes da mensagem
                	byte[] respostaDecifradaBytes = MeuRSA.decifrarBytes(respostaCifrada, chavePrivada.get(0), chavePrivada.get(1));
                	
                	String resposta = new String(respostaDecifradaBytes, StandardCharsets.UTF_8);
                	
                	if (resposta.equals("Autenticado")) {
                		autenticado = true;
                	}
                	
                	System.out.println("Resposta decifrada do servidor > " + resposta);	
                }
            }
            
            socket.close();
            entrada.close();
            saida.close();
            reader.close();

        } catch (IOException e) {
            System.err.println("Erro de conexão com o servidor");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    static void exibirMenuClienteNaoAutenticado() {
		System.out.println("--------------------------------------");
		System.out.println("1. Autenticar");
		System.out.println("2. Criar conta");
		System.out.println("0. Fechar programa");
	}
    
    static void exibirMenuClienteAutenticado() {
		System.out.println("--------------------------------------");
		System.out.println("1. Sacar");
		System.out.println("2. Depositar");
		System.out.println("3. Transferir");
		System.out.println("4. Consultar Saldo");
		System.out.println("5. Realizar Investimento");
		System.out.println("6. Deslogar");
	}
}
