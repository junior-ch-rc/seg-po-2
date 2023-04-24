package common;

import java.io.Serializable;

public class ContaBancaria implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;
	private String cpf;
	private String nome;
	private String endereco;
	private String nascimento;
	private String telefone;
	private String senha;
	private int saldo;
	private int saldo_poupanca;
	private int saldo_rendaFixa;
	
	public ContaBancaria() {
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCpf() {
		return cpf;
	}
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getEndereco() {
		return endereco;
	}
	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}
	public String getNascimento() {
		return nascimento;
	}
	public void setNascimento(String nascimento) {
		this.nascimento = nascimento;
	}
	public String getTelefone() {
		return telefone;
	}
	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}
	public int getSaldo() {
		return saldo;
	}
	public void setSaldo(int saldo) {
		this.saldo = saldo;
	}
	public int getSaldo_poupanca() {
		return saldo_poupanca;
	}
	public void setSaldo_poupanca(int saldo_poupanca) {
		this.saldo_poupanca = saldo_poupanca;
	}
	public int getSaldo_rendaFixa() {
		return saldo_rendaFixa;
	}
	public void setSaldo_rendaFixa(int saldo_rendaFixa) {
		this.saldo_rendaFixa = saldo_rendaFixa;
	}

}
