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
        // Inicializa o banco
        PreparedStatement ps = null;
        try {
            String query = "CREATE TABLE IF NOT EXISTS user(id INT(6) NOT NULL AUTO_INCREMENT,nome VARCHAR(40),senha VARCHAR(64),cpf VARCHAR(11),email VARCHAR(70),tokenSessao VARCHAR(64),tokenSenha VARCHAR(32) NOT NULL,tokenMudaEmail VARCHAR(32) NOT NULL,tokenCEmail VARCHAR(8) NOT NULL,adm BOOLEAN NOT NULL DEFAULT false,PRIMARY KEY(id));";
            ps = conn.prepareStatement(query);
            ps.executeUpdate();
            ps.close();
            query = "CREATE TABLE IF NOT EXISTS vacina(id INT(6) NOT NULL AUTO_INCREMENT PRIMARY KEY,nome VARCHAR(40),descricao VARCHAR(200),validade INT(5));";
            ps = conn.prepareStatement(query);
            ps.executeUpdate();
            ps.close();
            query = "CREATE TABLE IF NOT EXISTS pessoaVacina(id INT(8) NOT NULL AUTO_INCREMENT PRIMARY KEY,userId INT(6) NOT NULL,vacinaId INT(6) NOT NULL,dataAplicar VARCHAR(18) NOT NULL,vencida BOOLEAN,FOREIGN KEY(userId) REFERENCES user(id),FOREIGN KEY(vacinaId) REFERENCES vacina(id));";
            ps = conn.prepareStatement(query);
            ps.executeUpdate();
            ps.close();
            query = "CREATE TABLE IF NOT EXISTS comentarios(id INT(8) NOT NULL AUTO_INCREMENT PRIMARY KEY,userId INT(6) NOT NULL,referencia INT(8) NOT NULL DEFAULT 0,respostaId INT(6) NOT NULL DEFAULT 0,fechado BOOLEAN NOT NULL DEFAULT false,comentario VARCHAR(300),FOREIGN KEY(userId) REFERENCES user(id));";
            ps = conn.prepareStatement(query);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Checa se o cpf ja esta cadastrado
    boolean checaDados(String[] form){
        ResultSet result;
        PreparedStatement ps = null;
        try {
            String query =  "SELECT * FROM user WHERE cpf=? OR email=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[2]);
            ps.setString(2, form[3]);
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

    // Método para testar o nivel de um usuario
    boolean checaNivel(String sessao){
        ResultSet result;
        PreparedStatement ps = null;
        try {
            String query =  "SELECT * FROM user WHERE tokenSessao=? AND adm=true";
            ps = conn.prepareStatement(query);
            ps.setString(1, sessao);
            result = ps.executeQuery();
            if(result.next()){return true;}
        } catch (SQLException e) {
            return false;
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
        return false;
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

    // Método para limpar a sessão de um usuario
    int limpaSessao(String sessao){
        PreparedStatement ps = null;
        try {
            String query =  "UPDATE user SET tokenSessao='0' WHERE tokenSessao=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, sessao);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
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

    // Retorna comentarios ainda não respondidos
    ArrayList<String> retornaComentariosNovos(){
        ArrayList<String> coment = new ArrayList<>();
        ResultSet result;
        PreparedStatement ps = null;
        try {
            String query =  "SELECT c.id, u.nome, c.comentario FROM comentarios AS c INNER JOIN user AS u ON c.userId=u.id AND c.fechado=false AND c.referencia=0";
            ps = conn.prepareStatement(query);
            result = ps.executeQuery();
            while(result.next()){
                coment.add(Integer.toString(result.getInt("id"))+"&"+
                           result.getString("nome")+"&"+
                           result.getString("comentario"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
        return coment;
    }

    // Pega userId de um comentario
    int recebeUserId(String[] form){
        PreparedStatement ps = null;
        ResultSet result;
        try{
            int valor = 0;
            String query = "SELECT userId FROM comentarios WHERE id=?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, Integer.parseInt(form[1]));
            result = ps.executeQuery();
            while(result.next()){
                valor = result.getInt("userId");
            }
            return valor;
        }
        catch(SQLException ex) {
            return 0;
        }
        finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Insere Resposta
    String insereRespostaComentario(String[] form){
        PreparedStatement ps = null;
        try{
            String query =  "INSERT INTO comentarios(userId, comentario, referencia, respostaId) VALUES ((SELECT id FROM user WHERE tokenSessao=?), ?, ?, ?)";
            int valor = recebeUserId(form);
            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            ps.setString(2, form[2]);
            ps.setInt(3, Integer.parseInt(form[1]));
            ps.setInt(4, valor);
            ps.executeUpdate();

            fechaComent(Integer.parseInt(form[1]));

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

    // Fecha Comentario
    private void fechaComent(int id){
        PreparedStatement ps = null;
        try{
            String query =  "UPDATE comentarios SET fechado=true WHERE id=?";

            ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();

        }
        catch(SQLException ex) {
            ex.printStackTrace();
        }
        finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }

    // Insere comentario
    String insereComentarioUser(String[] form){
        PreparedStatement ps = null;
        try{
            String query =  "INSERT INTO comentarios(userId, comentario) VALUES ((SELECT id FROM user WHERE tokenSessao=?), ?)";

            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            ps.setString(2, form[1]);
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

    // Retorna comentarios
    ArrayList<String> retornaComentariosUsuario(String sessao){
        ArrayList<String> coment = new ArrayList<>();
        ResultSet result;
        PreparedStatement ps = null;
        try {
            String query =  "SELECT id, comentario, fechado, referencia FROM comentarios WHERE userId=(SELECT id FROM user WHERE tokenSessao=?) OR respostaId=(SELECT id FROM user WHERE tokenSessao=?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, sessao);
            ps.setString(2, sessao);
            result = ps.executeQuery();
            while(result.next()){
                String str = "&"+result.getString("comentario")+"&"+Integer.toString(result.getInt("id"))+"&"+Boolean.toString(result.getBoolean("fechado"));
                if(result.getInt("referencia") != 0){
                    str = str+":"+Integer.toString(result.getInt("referencia"));
                }
                coment.add(str);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
        return coment;
    }

    // Retorna as vacinas
    ArrayList<String> selecionaVacinas(String sessao){
        ArrayList<String> vacinas = new ArrayList<>();
        ResultSet result;
        PreparedStatement ps = null;
        try {
            String query =  "SELECT DISTINCT v.id, v.nome, v.validade, v.descricao, IFNULL(pv.vencida,true) AS disponivel FROM pessoaVacina AS pv RIGHT JOIN vacina AS v ON pv.vacinaId=v.id AND pv.vencida=0 AND pv.userId=(SELECT id FROM user WHERE tokenSessao=?);";
            ps = conn.prepareStatement(query);
            ps.setString(1, sessao);
            result = ps.executeQuery();
            while(result.next()){
                vacinas.add(Integer.toString(result.getInt("id"))+"&"+
                            result.getString("nome")+"&"+
                            Integer.toString(result.getInt("validade"))+"&"+
                            result.getString("descricao")+"'"+
                            Boolean.toString(result.getBoolean("disponivel")));
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
            String query =  "INSERT INTO pessoaVacina (vacinaId, dataAplicar, userId, vencida) VALUES (?, ?, (SELECT id FROM user WHERE tokenSessao=?), false)";

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
            String query =  "SELECT pv.id, va.nome, pv.dataAplicar, va.descricao, pv.vencida, va.validade FROM user AS us INNER JOIN pessoaVacina AS pv ON us.id = pv.userId AND us.tokenSessao=? INNER JOIN vacina AS va ON pv.vacinaId = va.id";
            ps = conn.prepareStatement(query);
            ps.setString(1, form[0]);
            result = ps.executeQuery();
            while(result.next()){
                vacinas.add(Integer.toString(result.getInt("id"))+"&"+
                            result.getString("nome")+"&"+
                            result.getString("dataAplicar")+"&"+
                            result.getString("descricao")+"'"+
                            Boolean.toString(result.getBoolean("vencida"))+"&"+
                            Integer.toString(result.getInt("validade")));
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

    // Marca uma vacina como vencida
    void marcaVacinaVencida(int id){
        PreparedStatement ps = null;
        try{
            String query =  "UPDATE pessoaVacina SET vencida=true WHERE id=?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        catch(SQLException ex) {
            ex.printStackTrace();
        }finally{try {
            if(ps != null){ps.close();}
        } catch (SQLException e) {
            e.printStackTrace();
        }}
    }
}
