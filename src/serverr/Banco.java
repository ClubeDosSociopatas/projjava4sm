// Classes Importadas
package serverr;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Classe Banco de Dados
class Banco {
    // Variaveis constantes para conexão ao banco
    private final String con_banco;
    private final String usuario_mysql;
    private final String senha_mysql;
    private Connection conn;

    // Método construtor
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

    // Método para inserir um novo usuario no banco
    String insereNoBanco(String[] form){
        try{
            PreparedStatement ps;
            String query =  "INSERT INTO user (id, nome, senha, cpf, email, tokenCEmail, tokenSenha) VALUES (0, ?, ?, ?, ?, ?, '0')";

            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            ps.setString(2, form[1]);
            ps.setString(3, form[2]);
            ps.setString(4, form[3]);
            ps.setString(5, form[4]);
            ps.executeUpdate();
            ps.close();

            return "Sucesso";
        }
        catch(SQLException ex) {
            return "Erro";
        }
    }

    // Método para inserir um token de sessão em um usuario
    int insereToken(String[] form){
        try{
            PreparedStatement ps;
            String query =  "UPDATE user SET tokenSessao=? WHERE nome=? AND senha=? AND tokenCEmail='0'";
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

    // Método para inserir um token de recuperação de senha em um usuario
    int insereRecuperarToken(String[] form){
        try{
            PreparedStatement ps;
            String query =  "UPDATE user SET tokenSenha=? WHERE email=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            ps.setString(2, form[1]);
            int retorno = ps.executeUpdate();
            ps.close();
            return retorno;
        }
        catch(SQLException ex) {
            return 0;
        }
    }

    // Método para mudar a senha de um usuario com o token de mudança
    int mudaSenha(String[] form){
        try{
            PreparedStatement ps;
            String query =  "UPDATE user SET tokenSenha='0', senha=? WHERE tokenSenha=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[1]);
            ps.setString(2, form[0]);
            int retorno = ps.executeUpdate();
            ps.close();
            return retorno;
        }
        catch(SQLException ex) {
            return 0;
        }
    }

    // Checa se o cpf ja esta cadastrado
    boolean checaCpf(String cpf){
        ResultSet result;
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

    // Confirma o token de e-mail de um usuario
    int confirmaTokenEmail(String token){
        try {
            PreparedStatement ps;
            String query =  "UPDATE user SET tokenCEmail='0' WHERE tokenCEmail=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, token);
            int retorno = ps.executeUpdate();
            ps.close();
            return retorno;
        } catch (SQLException e) {
            return 0;
        }
    }
}
