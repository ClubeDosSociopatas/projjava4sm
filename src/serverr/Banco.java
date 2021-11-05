// Classes Importadas
package serverr;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
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
    }

    // Método para inserir um novo usuario no banco
    String insereNoBanco(String[] form){
        PreparedStatement ps = null;
        try{
            String query =  "INSERT INTO user (id, nome, senha, cpf, email, tokenCEmail, tokenSenha, tokenMudaEmail) VALUES (0, ?, ?, ?, ?, ?, '0', '0')";

            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            ps.setString(2, form[1]);
            ps.setString(3, form[2]);
            ps.setString(4, form[3]);
            ps.setString(5, form[4]);
            ps.executeUpdate();

            return "Sucesso";
        }
        catch(SQLException ex) {
            return "Erro";
        }
        finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Método para inserir um token de sessão em um usuario
    int insereToken(String[] form){
        PreparedStatement ps = null;
        try{
            String query =  "UPDATE user SET tokenSessao=? WHERE nome=? AND senha=? AND tokenCEmail='0'";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            ps.setString(2, form[1]);
            ps.setString(3, form[2]);
            return ps.executeUpdate();
        }
        catch(SQLException ex) {
            return 0;
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
        
    }

    // Método para inserir um token de recuperação de senha em um usuario
    int insereRecuperarToken(String[] form){
        PreparedStatement ps = null;
        try{
            String query =  "UPDATE user SET tokenSenha=? WHERE email=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            ps.setString(2, form[1]);
            return ps.executeUpdate();
        }
        catch(SQLException ex) {
            return 0;
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Método para mudar a senha de um usuario com o token de mudança
    int mudaSenhaComToken(String[] form){
        PreparedStatement ps = null;
        try{
            String query =  "UPDATE user SET tokenSenha='0', senha=? WHERE tokenSenha=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[1]);
            ps.setString(2, form[0]);
            return ps.executeUpdate();
        }
        catch(SQLException ex) {
            return 0;
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Checa se o cpf ja esta cadastrado
    boolean checaCpf(String cpf){
        ResultSet result;
        PreparedStatement ps = null;
        try {
            String query =  "SELECT * FROM user WHERE cpf=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, cpf);
            result = ps.executeQuery();
            if(result.next()){return true;}
        } catch (SQLException e) {
            return true;
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
        return false;
    }

    // Confirma o token de e-mail de um usuario
    int confirmaTokenEmail(String token){
        PreparedStatement ps = null;
        try {
            String query =  "UPDATE user SET tokenCEmail='0' WHERE tokenCEmail=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, token);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Retorna informações do usuario
    String[] infoUsuario(String sessao){
        String[] infUser = new String[3];
        ResultSet result;
        PreparedStatement ps = null;
        try {
            String query =  "SELECT * FROM user WHERE tokenSessao=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, sessao);
            result = ps.executeQuery();
            if(result.next()){
                infUser[0] = result.getString("nome");
                infUser[1] = result.getString("email");
                infUser[2] = "*********"+result.getString("cpf").substring(9);
                return infUser;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
        return new String[0];
    }

    // Método para inserir um token de recuperação de senha em um usuario
    int insereTokenMudaEmail(String[] form){
        PreparedStatement ps = null;
        try{
            String query =  "UPDATE user SET tokenMudaEmail=? WHERE tokenSessao=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            ps.setString(2, form[1]);
            return ps.executeUpdate();
        }
        catch(SQLException ex) {
            return 0;
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Mudar email
    int mudaEmail(String[] form){
        PreparedStatement ps = null;
        try {
            String query =  "UPDATE user SET email=? WHERE tokenMudaEmail=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[1]);
            ps.setString(2, form[0]);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Mudar senha
    int mudaSenha(String[] form){
        PreparedStatement ps = null;
        try {
            String query =  "UPDATE user SET senha=? WHERE senha=? AND tokenSessao=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[1]);
            ps.setString(2, form[0]);
            ps.setString(3, form[2]);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Retorna as vacinas
    ArrayList<String> selecionaVacinas(){
        ArrayList<String> vacinas = new ArrayList<>();
        ResultSet result;
        PreparedStatement ps = null;
        try {
            String query =  "SELECT * FROM vacina";
            ps = conn.prepareStatement(query);
            result = ps.executeQuery();
            while(result.next()){
                vacinas.add(Integer.toString(result.getInt("id"))+"&"+
                            result.getString("nome")+"&"+
                            Integer.toString(result.getInt("validade"))+"&"+
                            result.getString("descricao"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
        return vacinas;
    }

    // Agenda uma vacina
    String agendaVacina(String[] form){
        PreparedStatement ps = null;
        try{
            String query =  "INSERT INTO pessoaVacina (vacinaId, dataAplicar, userId) VALUES (?, ?, (SELECT id FROM user WHERE tokenSessao=?))";

            ps = conn.prepareStatement(query);
            ps.setInt(1, Integer.parseInt(form[0]));
            ps.setString(2, form[1]);
            ps.setString(3, form[2]);
            ps.executeUpdate();

            return "Sucesso";
        }
        catch(SQLException ex) {
            return "Erro";
        }
        finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Retorna vacinas agendadas pelo usuario
    ArrayList<String> retornaCarteirinha(String[] form){
        ArrayList<String> vacinas = new ArrayList<>();
        ResultSet result;
        PreparedStatement ps = null;
        try {
            String query =  "SELECT pv.id, va.nome, pv.dataAplicar, va.descricao FROM user AS us INNER JOIN pessoaVacina AS pv ON us.id = pv.userId AND us.tokenSessao=? INNER JOIN vacina AS va ON pv.vacinaId = va.id";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            result = ps.executeQuery();
            while(result.next()){
                vacinas.add(Integer.toString(result.getInt("id"))+"&"+
                            result.getString("nome")+"&"+
                            result.getString("dataAplicar")+"&"+
                            result.getString("descricao"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
        return vacinas;
    }
}
