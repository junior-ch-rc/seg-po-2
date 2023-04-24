package common;

import java.io.Serializable;

public class Mensagem implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String mensagem;
	private Object objeto;
	
	public String getMensagem() {
		return mensagem;
	}
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}
	public Object getObjeto() {
		return objeto;
	}
	public void setObjeto(Object objeto) {
		this.objeto = objeto;
	}
	
}
