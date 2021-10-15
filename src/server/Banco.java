package server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


class Banco {
    private final String con_banco;
    private final String usuario_mysql;
    private final String senha_mysql;
    private Connection conn;

    Banco(){

        usuario_mysql = "root";
        senha_mysql = "toor";

        con_banco = "jdbc:mysql://localhost:3306/softseguro";

        try{
            conn = DriverManager.getConnection(con_banco, usuario_mysql, senha_mysql);
        }

        catch(SQLException ex) {
            ex.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    String insereNoBanco(String[] form){
        try{
            PreparedStatement ps;
            String query =  "INSERT INTO user (id, nome, senha, cpf, email) VALUES (0, ?, ?, ?, ?)";

            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            ps.setString(2, form[1]);
            ps.setString(3, form[2]);
            ps.setString(4, form[3]);
            ps.executeUpdate();
            ps.close();

            return "Sucesso";
        }
        catch(SQLException ex) {
            return "Erro";
        }
    }

    int insereToken(String[] form){
        try{
            PreparedStatement ps;
            String query =  "UPDATE user SET tokenSessao=? WHERE nome=? AND senha=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            ps.setString(2, form[1]);
            ps.setString(3, form[2]);
            int retorno = ps.executeUpdate();
            ps.close();
            return retorno;
        }
        catch(SQLException ex) {
            return 0;
        }
    }

    boolean checaCpf(String cpf){
        ResultSet result;
        System.out.println(cpf);
        try {
            PreparedStatement ps;
            String query =  "SELECT * FROM user WHERE cpf=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, cpf);
            result = ps.executeQuery();
            if(result.next()){return true;}
        } catch (SQLException e) {
            return true;
        }
        return false;
    }
}
