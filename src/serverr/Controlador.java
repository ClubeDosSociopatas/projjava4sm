// Classes Importadas
package serverr;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.math.BigInteger; 
import java.nio.charset.StandardCharsets;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;

// Classe Controlador/Servidor
public class Controlador {
    // Variavel com a classe do banco
    private Banco bd = new Banco();

    // MÉTODOS PUBLICOS

    // Método para criar um novo usuario
    public String novoUsuario(String[] form){
        String resultado;
        if(form.length != 5){return "Erro";}
        if(testeStrings(form)){
            return "Utilização de caracteres especiais fora do campo de senha é proibido.\n";
        }
        if(testarCpf(form[2])){return "CPF inválido.";}
        if(bd.checaCpf(form[2])){return "CPF ja cadastrado.";}
        form[1] = hashingSalt(form[1]);
        byte b[] = new byte[4];
        try {
            SecureRandom.getInstanceStrong().nextBytes(b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        BigInteger number = new BigInteger(1, b);
        StringBuilder hexString = new StringBuilder(number.toString(16)); 
        while (hexString.length() < 8) { 
            hexString.insert(0, '0'); 
        }
        form[4] = hexString.toString();
        if((resultado = bd.insereNoBanco(form)).equals("Sucesso")){
            enviarEmail(form[3], "Confirmacao de Email", "Utilize o codigo: "+hexString.toString()+"\nPara confirmar sua conta.");
        }
        return resultado;
    }

    // Método para logar um usuario, se tiver sucesso retorna a chave de sessão para o usuario
    public String usuarioLogar(String[] form){
        if(form.length != 3){return "Erro";}
        if(testeStrings(form)){
            return "Utilização de caracteres especiais fora do campo de senha é proibido.\n";
        }
        form[2] = hashingSalt(form[2]);
        byte b[] = new byte[32];
        try {
            SecureRandom.getInstanceStrong().nextBytes(b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        BigInteger number = new BigInteger(1, b);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 64) { 
            hexString.insert(0, '0'); 
        }
        form[0] = hexString.toString();
        if(bd.insereToken(form) == 1){return "id="+form[0];}
        return "Falha no Login, conta pode não existir ou falta confirmar seu e-mail.";
    }

    // Método para receber o token de confirmação de e-mail de usuarios
    public String confirmarTokenE(String[] form){
        if(form.length != 1){return "Erro";}
        if(form[0].length() < 8){return "Código inválido!";}
        if(testeStrings(form)){return "Utilização de caracteres especiais é proibido.\n";}
        if(bd.confirmaTokenEmail(form[0]) == 1){return "Conta confirmada com sucesso";}
        return "Token inválido!";
    }

    // Método para começar a recuperação de senha, insere o token de recuperação no banco
    public String iniciarRecuperarSenha(String[] form){
        if(form.length != 2){return "Erro";}
        if(form[1].equals("")){return "";}
        if(testeStrings(form)){return "Utilização de caracteres especiais é proibido.\n";}
        byte b[] = new byte[16];
        try {
            SecureRandom.getInstanceStrong().nextBytes(b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        BigInteger number = new BigInteger(1, b);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) { 
            hexString.insert(0, '0'); 
        } 
        form[0] = hexString.toString();
        if(bd.insereRecuperarToken(form) == 1){
            //enviarEmail(form[1], "Recuperação de Senha", "Utilize o código: "+form[0]+"\nPara recuperar sua senha");
            return "c";
        }
        return "";
    }

    // Método para recuperar a senha
    public String recuperarSenha(String[] form){
        if(form.length != 2){return "Erro";}
        if(form[0].length() < 32){return "Erro";}
        if(testeStrings(form)){return "Utilização de caracteres especiais é proibido.\n";}
        form[1] = hashingSalt(form[1]);
        if(bd.mudaSenha(form) == 1){return "Sucesso!";}
        return "Erro";
    }

    public void usuarioSair(){}

    // MÉTODOS LOCAIS //

    // Testa formularios recebidos, procura por caracteres especiais
    private boolean testeStrings(String[] form){
        int chrTeste[] = {39, 34, 61, 59, 92};
        int i;
        int o;
        int p;

        for(i = 0; i < form.length; i++){
            try{
                for(o = 0; o < form[i].length(); o++){
                    for(p = 0; p < chrTeste.length; p++){
                        if((int)form[i].charAt(o) == chrTeste[p]){
                            return true;
                        }
                    }
                }
            } catch(NullPointerException e){}
        }
        return false;
    }

    // Método para hash de senha+salt
    private String hashingSalt(String input){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "erro";
        }
        input = input + "4PROGRAMA-SEGURO_SSS4";
        byte hash[] = md.digest(input.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, hash);
  
        StringBuilder hexString = new StringBuilder(number.toString(16)); 
        while (hexString.length() < 64) { 
            hexString.insert(0, '0'); 
        }
        return hexString.toString();
    }

    // Método para validar cpf
    private boolean testarCpf(String input){
        if(input.length() != 11){return true;}
        String testeStr = "1234567890";
        boolean teste;
        int i, o;
        for(i = 0; i < input.length(); i++){
            teste = false;
            for(o = 0; o < testeStr.length(); o++){
                if(input.charAt(i) == testeStr.charAt(o)){teste = true;}
            }
            if(!teste){return true;}
        }

        int peso = 10;
        int soma = 0;
        for(i = 0; i < 9; i++){
            int num = Character.getNumericValue(input.charAt(i));
            soma+= num*peso;
            peso--;
        }
        int dig1 = (11 - (soma % 11));
        if(!(((dig1 == 10 || dig1 == 11)&& Character.getNumericValue(input.charAt(9)) == 0) || dig1 == Character.getNumericValue(input.charAt(9)))){return true;}
        peso = 11;
        soma = 0;
        for(i = 0; i < 10; i++){
            int num = Character.getNumericValue(input.charAt(i));
            soma+= num*peso;
            peso--;
        }
        int dig2 = (11 - (soma % 11));
        if(!(((dig2 == 10 || dig2 == 11)&& Character.getNumericValue(input.charAt(10)) == 0) || dig2 == Character.getNumericValue(input.charAt(10)))){return true;}
        return false;
    }

    // Método para enviar e-mail
    private void enviarEmail(String emailD, String assunto, String conteudo){
        final String username = "rafazeteste@gmail.com";
        final String password = "TeStandoHU1234";

        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
        
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("rafazeteste@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(emailD)
            );
            message.setSubject(assunto);
            message.setText(conteudo);

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
