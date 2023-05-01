package servidor;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import common.ContaBancaria;
import common.MeuHash;

public class BankImpl {
    
    private Connection conn;
    
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
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT INTO accounts (cpf, nome, endereco, nascimento, telefone, senha) VALUES ('" + c.getCpf() + "', '" + c.getNome() + "', '" + c.getEndereco() + "', '" + nascimento + "', '" + c.getTelefone() + "', '" + MeuHash.resumo(c.getSenha().getBytes(), "SHA3-256") + "')", Statement.RETURN_GENERATED_KEYS);
        ResultSet generatedKeys = stmt.getGeneratedKeys();
    	
        if (generatedKeys.next()) {
            long idGerado = generatedKeys.getLong(1);
            return (int) idGerado;
        } else {
        	return -1;
        }
        
    }
    
    // Saque
    protected String withdraw(int accountNumber, int amount) throws SQLException {
    	try {
    		PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET saldo = saldo - ? WHERE id = ? AND saldo >= ?");
            stmt.setInt(1, amount);
            stmt.setInt(2, accountNumber);
            stmt.setInt(3, amount);
            stmt.executeUpdate();
            return "Operação bem sucedida!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Ocorreu algum erro!";
		}
        
    }
    
    // Depósito
    protected String deposit(int accountNumber, int amount) throws SQLException {
    	try {
    		PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET saldo = saldo + ? WHERE id = ?");
            stmt.setInt(1, amount);
            stmt.setInt(2, accountNumber);
            stmt.executeUpdate();
            return "Operação bem sucedida!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Ocorreu algum erro!";
		}
        
    }
    
    // Transferência
    protected String transfer(int accountOrigin, int accountTo, int amount) throws SQLException {
    	try {
    		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE id = ? AND saldo >= ?");
    		stmt.setInt(1, accountOrigin);
    		stmt.setInt(2, amount);
    		ResultSet rs = null;
    		rs = stmt.executeQuery();
    		
    		if (rs.next()) {
    			stmt = conn.prepareStatement("UPDATE accounts SET saldo = saldo - ? WHERE id = ? AND saldo >= ?");
    			stmt.setInt(1, amount);
    			stmt.setInt(2, accountOrigin);
    			stmt.setInt(3, amount);
    			stmt.executeUpdate();
    			
                stmt = conn.prepareStatement("UPDATE accounts SET saldo = saldo + ? WHERE id = ?");
                stmt.setInt(1, amount);
                stmt.setInt(2, accountTo);
                stmt.executeUpdate();
                return "Operação bem sucedida!";
    		}
    		return "Ocorreu algum erro!";
    		
		} catch (Exception e) {
			e.printStackTrace();
			return "Ocorreu algum erro!";
		}
        
    }
    
    // Saldo
    protected int getBalance(int accountNumber) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT saldo FROM accounts WHERE id = ?");
        stmt.setInt(1, accountNumber);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt("saldo");
    }
    
    // Investir em Poupanca
    protected int investSavings(int accountNumber, int amount) throws SQLException {
    	PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE id = ? AND saldo >= ?");
		stmt.setInt(1, accountNumber);
		stmt.setInt(2, amount);
		ResultSet rs = null;
		rs = stmt.executeQuery();
		
		if (rs.next()) {
			stmt = conn.prepareStatement("UPDATE accounts SET saldo = saldo - ? WHERE id = ?");
	        stmt.setInt(1, amount);
	        stmt.setInt(2, accountNumber);
	        stmt.executeUpdate();
	        
	        stmt = conn.prepareStatement("UPDATE accounts SET saldo_poupanca = saldo_poupanca + ? WHERE id = ?");
	        stmt.setInt(1, amount);
	        stmt.setInt(2, accountNumber);
	        stmt.executeUpdate();
	        
	        stmt = conn.prepareStatement("SELECT saldo_poupanca FROM accounts WHERE id = ?");
	        stmt.setInt(1, accountNumber);
	        rs = stmt.executeQuery();
	        rs.next();
	        return rs.getInt("saldo_poupanca");
		}
		return 0;
        
    }
    
    // Investir em Renda Fixa
    protected int investFixedIncome(int accountNumber, int amount) throws SQLException {
    	PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE id = ? AND saldo >= ?");
		stmt.setInt(1, accountNumber);
		stmt.setInt(2, amount);
		ResultSet rs = null;
		rs = stmt.executeQuery();
		
		if (rs.next()) {
			stmt = conn.prepareStatement("UPDATE accounts SET saldo = saldo - ? WHERE id = ?");
	        stmt.setInt(1, amount);
	        stmt.setInt(2, accountNumber);
	        stmt.executeUpdate();
	        
	        stmt = conn.prepareStatement("UPDATE accounts SET saldo_rendafixa = saldo_rendafixa + ? WHERE id = ?");
	        stmt.setInt(1, amount);
	        stmt.setInt(2, accountNumber);
	        stmt.executeUpdate();
	        
	        stmt = conn.prepareStatement("SELECT saldo_rendaFixa FROM accounts WHERE id = ?");
	        stmt.setInt(1, accountNumber);
	        rs = stmt.executeQuery();
	        rs.next();
	        return rs.getInt("saldo_rendaFixa");
		}
    	return 0; 
        
    }
    
}