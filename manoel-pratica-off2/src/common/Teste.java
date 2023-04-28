package common;

import java.math.BigInteger;

public class Teste {

	public static void main(String[] args) {
		
		// Gerando chaves
		BigInteger[] chaves = MeuRSA.gerarChaves();

		// Mensagem a ser cifrada
		int mensagem = 111;

		System.out.println("Mensagem aberta: " + mensagem);

		// Cifrando a mensagem
		BigInteger mensagemCifrada = BigInteger.valueOf(mensagem).modPow(chaves[0], chaves[2]);

		System.out.println("Mensagem cifrada: " + mensagemCifrada);

		// Decifrando a mensagem
		BigInteger mensagemDecifrada = mensagemCifrada.modPow(chaves[1], chaves[2]);

		System.out.println("Mensagem decifrada: " + mensagemDecifrada);

	}

}
