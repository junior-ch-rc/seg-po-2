package common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MeuHash {
	static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	public static String resumo(byte[] bytesEntrada, String alg) {
		
	       MessageDigest md = null;
	        try {
	        	md = MessageDigest.getInstance(alg);
	        } catch (NoSuchAlgorithmException e) {
	            e.printStackTrace();
	        }
	        byte[] bytesResumo = md.digest(bytesEntrada);
	        return bytes2Hex(bytesResumo);
	}
	
    public static String bytes2Hex(byte[] bytes) {
    	StringBuilder sb = new StringBuilder();
       
		for (byte b : bytes) { 
			sb.append(String.format("%02x", b));
		}
		
		return sb.toString();
    }
   
}
