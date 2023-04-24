package servidor;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import common.ContaBancaria;
import common.MeuHash;

public class BankImpl {
    
    private Connection conn;
    private int accountNumber;
    
    public BankImpl(String username, String password, String databaseURL) throws SQLException, ClassNotFoundException {
    	Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(databaseURL, username, password);
    }
    
    // Autenticação
    public boolean authenticate(int numeroConta, String senha) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM accounts WHERE id = ? AND senha = ?");
        stmt.setInt(1, numeroConta);
        stmt.setString(2, MeuHash.resumo(senha.getBytes(), "SHA3-256"));
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        return count == 1;
    }
    
    // Criar conta corrente
    public int createAccount(ContaBancaria c) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO accounts (cpf, nome, endereco, nascimento, telefone, senha) VALUES (?, ?, ?, ?, ?, ?)");
        stmt.setString(1, c.getCpf());
        stmt.setString(2, c.getNome());
        stmt.setString(3, c.getEndereco());
        
        // conversão de String para Date
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        java.util.Date parsed = null;
		try {
			parsed = format.parse(c.getNascimento());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Date nascimento = new Date(parsed.getTime());
        stmt.setDate(4, nascimento);
        
        stmt.setString(5, c.getTelefone());
        stmt.setString(6, MeuHash.resumo(c.getSenha().getBytes(), "SHA3-256"));
        stmt.executeUpdate();
        

    	ResultSet generatedKeys = stmt.getGeneratedKeys();
    	
        if (generatedKeys.next()) {
            int idGerado = generatedKeys.getInt(1);
            return idGerado;
        } else {
        	return -1;
        }
        
    }
    
    // Saque
    protected void withdraw(int amount) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
        stmt.setInt(1, amount);
        stmt.setInt(2, accountNumber);
        stmt.executeUpdate();
    }
    
    // Depósito
    protected void deposit(int amount) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
        stmt.setInt(1, amount);
        stmt.setInt(2, accountNumber);
        stmt.executeUpdate();
    }
    
    // Transferência
    protected void transfer(int accountTo, int amount) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
        stmt.setInt(1, amount);
        stmt.setInt(2, accountNumber);
        stmt.executeUpdate();
        
        stmt = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
        stmt.setInt(1, amount);
        stmt.setInt(2, accountTo);
        stmt.executeUpdate();
    }
    
    // Saldo
    protected int getBalance() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT balance FROM accounts WHERE account_number = ?");
        stmt.setInt(1, accountNumber);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt("balance");
    }
    
}