package common;

import java.math.BigInteger;
import java.util.List;

public class Teste {

	public static void main(String[] args) {
		
		// Gerando chaves
		BigInteger[] chaves = MeuRSA.gerarChaves();

		// Mensagem a ser cifrada
		String mensagem = "Seguranca eh massa";

		System.out.println("Mensagem aberta: " + mensagem);

		// Cifrando a mensagem
		List<BigInteger> mensagemCifrada = MeuRSA.cifrarBytes(mensagem.getBytes(), chaves[0], chaves[2]);

		System.out.println("Mensagem cifrada: " + mensagemCifrada);

		// Decifrando a mensagem
		byte[] mensagemDecifrada = MeuRSA.decifrarBytes(mensagemCifrada, chaves[1], chaves[2]);

		System.out.println("Mensagem decifrada: " + new String(mensagemDecifrada));
		
		//System.out.println(MeuRSA.cifrarByte(new BigInteger("-84"), new BigInteger("8470986124517641043"), new BigInteger("16141463751357015311")));
		//System.out.println(MeuRSA.decifrarByteInt(new BigInteger("10606103634199933972"), new BigInteger("8198335354829965739"), new BigInteger("16141463751357015311")));

	}

}
