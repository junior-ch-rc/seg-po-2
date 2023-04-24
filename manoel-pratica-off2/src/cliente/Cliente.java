package cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import common.ContaBancaria;
import common.Mensagem;

public class Cliente {

    private static final String servidorIP = "localhost";
    private static final int porta = 54321;

	public static void main(String[] args) {
		Scanner reader = new Scanner(System.in);
		
        try {
        	
        	Socket socket = new Socket(servidorIP, porta);
        	System.out.println("Cliente conectado ao servidor " + servidorIP + ":" + porta);
        	
    		ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
    		
    		// Troca de chaves e consolidação da conexão
    		System.out.print("Digite o id do terminal cliente: ");
    		String clientID = reader.nextLine();
    		saida.writeObject(clientID);
    		saida.flush();
    		
    		ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            
            System.out.println("Terminal pronto!");

            int opc = -1;
            
            while (opc != 0) {
            	Mensagem m = new Mensagem();
                exibirMenuClienteNaoAutenticado();
                opc = Integer.parseInt(reader.nextLine());
                
                switch(opc) {
                
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
	                
                }

                // Envia mensagem para o servidor
                saida.writeObject(m);
                saida.flush();

                // Recebe resposta do servidor
                String resposta = (String) entrada.readObject();
                System.out.println("> " + resposta);
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
		System.out.println("0. Sair");
	}
    
    static void exibirMenuClienteAutenticado() {
		System.out.println("--------------------------------------");
		System.out.println("1. Sacar");
		System.out.println("2. Depositar");
		System.out.println("3. Transferir");
		System.out.println("4. Consultar Saldo");
		System.out.println("5. Realizar Investimento");
		System.out.println("0. Sair");
	}
}
