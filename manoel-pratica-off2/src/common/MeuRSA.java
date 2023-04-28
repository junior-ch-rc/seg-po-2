package common;

import java.math.BigInteger;
import java.util.Random;

public class MeuRSA {
	
	private static BigInteger gerarPrimo() {
        Random rnd = new Random();
        BigInteger primo = BigInteger.probablePrime(32, rnd);
        return primo;
    }
	
	public static BigInteger[] gerarChaves() {
		
		// Primeiro passo - Gerar p e q
		BigInteger p = gerarPrimo();
		BigInteger q = gerarPrimo();

		// Segundo passo - Gerar n
		BigInteger n = p.multiply(q);
		System.out.println("n: " + n);
		
		// Terceiro passo - Calcular phi
		BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)); // (p - 1) * (q - 1)
		System.out.println("phi:" + phi);
		
		// Quarto passo - Gerar e
		BigInteger e = calcularE(phi);
		System.out.println("e: " + e);
		
		// Quinto passo - Gerar d
		BigInteger d = calcularD(e, phi);
		System.out.println("d: " + d);
		
		BigInteger[] result = { e, d, n };
		
		return result;
		
	}
	
	// Função que calcula o valor de e, dado o valor de phi(n)
    private static BigInteger calcularE(BigInteger phi) {
    	
    	Random rnd = new Random();
        BigInteger e;
        
        do {
        	
            e = new BigInteger(phi.bitLength() - 1, rnd);
            
        } while (e.compareTo(BigInteger.ONE) <= 0 || e.gcd(phi).compareTo(BigInteger.ONE) != 0);
        
        return e;
        
    }
    
    private static BigInteger calcularD(BigInteger e, BigInteger totiente) {
        BigInteger[] resultados = euclidesEstendido(e, totiente);
        if (resultados[0].compareTo(BigInteger.ZERO) < 0) {
            resultados[0] = resultados[0].add(totiente);
        }
        return resultados[0];
    }

    private static BigInteger[] euclidesEstendido(BigInteger a, BigInteger b) {
        BigInteger[] resultados;
        if (b.compareTo(BigInteger.ZERO) == 0) {
            resultados = new BigInteger[] { BigInteger.ONE, BigInteger.ZERO };
        } else {
            BigInteger[] temp = euclidesEstendido(b, a.mod(b));
            resultados = new BigInteger[] { temp[1], temp[0].subtract(a.divide(b).multiply(temp[1])) };
        }
        return resultados;
    }

}
