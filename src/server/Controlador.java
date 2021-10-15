package server;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.math.BigInteger; 
import java.nio.charset.StandardCharsets;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Controlador {
    private Banco bd = new Banco();

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
        form[4] = hexString.toString();
        if((resultado = bd.insereNoBanco(form)).equals("Sucesso")){
            //enviarEmail(form[3], "Confirmacao de Email", "Utilize o codigo: "+hexString.toString()+"\nPara confirmar sua conta.");
        }
        return resultado;
    }

    public String usuarioLogar(String[] form){
        if(form.length != 3){return "Erro";}
        if(testeStrings(form)){
            return "Utilização de caracteres especiais fora do campo de senha é proibido.\n";
        }
        form[2] = hashingSalt(form[2]);
        System.out.println(form[2]);
        if(bd.checaTokenEmail(form)){return "Por favor, confirme seu email.";}
        byte b[] = new byte[32];
        try {
            SecureRandom.getInstanceStrong().nextBytes(b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        BigInteger number = new BigInteger(1, b);
        StringBuilder hexString = new StringBuilder(number.toString(16)); 
        form[0] = hexString.toString();
        if(bd.insereToken(form) == 1){return "id="+form[0];}
        return "Falha no Login.";
    }

    public String confirmarTokenE(String[] form){
        if(form.length != 1){return "Erro";}
        if(testeStrings(form)){return "Utilização de caracteres especiais é proibido.\n";}
        if(bd.confirmaTokenEmail(form[0]) == 1){return "Conta confirmada com sucesso";}
        return "Token inválido!";
    }

    public void usuarioSair(){}

    // FUNCOES LOCAIS //

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

            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
