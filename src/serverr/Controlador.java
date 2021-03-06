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

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Properties;

// Classe Controlador/Servidor
public class Controlador {
    // Variavel com a classe do banco
    private Banco bd = new Banco();
    // Variavel para salvar as vacinas que usuario tem acesso
    private ArrayList<String> vacinasDisponiveis = new ArrayList<>();
    private boolean nivelUsuario = false;

    // MÉTODOS PUBLICOS

    // Método para criar um novo usuario
    public String novoUsuario(String[] form){
        if(form.length != 5){return "Erro";}
        if(testeStrings(form)){
            return "Utilização de caracteres especiais fora do campo de senha é proibido.\n";
        }
        if(testarCpf(form[2])){return "CPF inválido.";}
        if(bd.checaDados(form)){return "CPF/E-mail ja cadastrados.";}
        form[1] = hashingSalt(form[1]);
        byte[] b = new byte[4];
        try {
            SecureRandom.getInstanceStrong().nextBytes(b);
        } catch (NoSuchAlgorithmException e) {
            return "Erro";
        }
        BigInteger number = new BigInteger(1, b);
        StringBuilder hexString = new StringBuilder(number.toString(16)); 
        while (hexString.length() < 8) { 
            hexString.insert(0, '0'); 
        }
        form[4] = hexString.toString();
        if(bd.insereNoBanco(form).equals("Sucesso")){
            enviarEmail(form[3], "Confirmacao de Email", "Utilize o codigo: "+hexString.toString()+"\nPara confirmar sua conta.");
            return "Sucesso";
        }
        return "Erro";
    }

    // Método para logar um usuario, se tiver sucesso retorna a chave de sessão para o usuario
    public String usuarioLogar(String[] form){
        if(form.length != 3){return "Erro";}
        if(testeStrings(form)){
            return "Utilização de caracteres especiais fora do campo de senha é proibido.\n";
        }
        form[2] = hashingSalt(form[2]);
        byte[] b = new byte[32];
        try {
            SecureRandom.getInstanceStrong().nextBytes(b);
        } catch (NoSuchAlgorithmException e) {
            return "Erro";
        }
        BigInteger number = new BigInteger(1, b);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 64) { 
            hexString.insert(0, '0'); 
        }
        form[0] = hexString.toString();
        if(bd.insereToken(form) == 1){
            nivelUsuario = bd.checaNivel(form[0]);
            return "id="+form[0]+"&"+Boolean.toString(nivelUsuario);}
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
        byte[] b = new byte[16];
        try {
            SecureRandom.getInstanceStrong().nextBytes(b);
        } catch (NoSuchAlgorithmException e) {
            return "Erro";
        }
        BigInteger number = new BigInteger(1, b);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) { 
            hexString.insert(0, '0'); 
        } 
        form[0] = hexString.toString();
        if(bd.insereRecuperarToken(form) == 1){
            enviarEmail(form[1], "Recuperação de Senha", "Utilize o código: "+form[0]+"\nPara recuperar sua senha");
            return "c";
        }
        return "";
    }

    // Método para recuperar a senha
    public String recuperarSenha(String[] form){
        if(form.length != 2 || form[0].length() < 32){return "Erro";}
        if(testeStrings(form)){return "Utilização de caracteres especiais é proibido.\n";}
        form[1] = hashingSalt(form[1]);
        if(bd.mudaSenhaComToken(form) == 1){return "Sucesso!";}
        return "Erro";
    }

    // Método para criar avisos ao usuario
    public String[] criaAvisosUsuario(String[] form){
        if(form.length != 1 || form[0].length() < 64 || testeStrings(form)){return new String[1];}
        ArrayList<String> vacinas = bd.retornaCarteirinha(form);
        int i;
        for(i = 0; i < vacinas.size(); i++){
            if(vacinas.get(i).split("'")[1].split("&")[0].equals("false")){
                String data = vacinas.get(i).split("'")[0].split("&")[2].split("-")[0];
                LocalDateTime dataVacina = LocalDateTime.of(Integer.parseInt(data.split("/")[2]), 
                                                            Integer.parseInt(data.split("/")[1]), 
                                                            Integer.parseInt(data.split("/")[0]), 
                                                            0, 0);
                LocalDateTime dataAgora = LocalDateTime.now();
                dataAgora = LocalDateTime.of(dataAgora.getYear(), dataAgora.getMonth(), dataAgora.getDayOfMonth(), 0, 0);
                if(dataVacina.isEqual(dataAgora)){
                    vacinas.set(i, "Sua vacina de "+vacinas.get(i).split("'")[0].split("&")[1]+" é aplicada hoje!");
                }
                else{
                    if(dataAgora.isAfter(dataVacina.plusDays(Integer.parseInt(vacinas.get(i).split("'")[1].split("&")[1])))){
                        bd.marcaVacinaVencida(Integer.parseInt(vacinas.get(i).split("'")[0].split("&")[0]));
                        vacinas.set(i, "Sua vacina de "+vacinas.get(i).split("'")[0].split("&")[1]+" venceu!");
                        continue;
                    }
                    vacinas.remove(i);
                    i--;
                }
            }
            else{
                vacinas.remove(i);
                i--;
            }
        }
        String[] resposta = new String[vacinas.size()];
        for(i = 0; i < vacinas.size(); i++){
            resposta[i] = vacinas.get(i);
        }
        return resposta;
    }

    // Método para retornar dados do usuario
    public String[] dadosUsuario(String[] form){
        if(form.length != 1 || form[0].length() < 64 || testeStrings(form)){return new String[1];}
        return bd.infoUsuario(form[0]);
    }

    // Método para começar mudança de e-mail
    public String comecaMudancaEmail(String[] form){
        if(form.length != 2 || form[1].length() < 64 || testeStrings(form)){return "Erro";}
        byte[] b = new byte[16];
        try {
            SecureRandom.getInstanceStrong().nextBytes(b);
        } catch (NoSuchAlgorithmException e) {
            return "Erro";
        }
        BigInteger number = new BigInteger(1, b);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) { 
            hexString.insert(0, '0'); 
        } 
        form[0] = hexString.toString();
        if(bd.insereTokenMudaEmail(form) == 1){
            enviarEmail(form[1], "Mudança de email", "Utilize o código: "+form[0]+"\nPara mudar seu email");
            return "Sucesso!";
        }
        return "Erro";
    }

    // Método para mudar e-mail
    public String mudaEmail(String[] form){
        if(form.length != 2 || testeStrings(form) || form[0].length() < 32){return "Erro";}
        if(bd.mudaEmail(form) == 0){return "Erro";}
        return "Sucesso!";
    }

    // Método para mudar a senha
    public String mudaSenha(String[] form){
        if(form.length != 3 || testeStrings(form)){return "Erro";}
        form[0] = hashingSalt(form[0]);
        form[1] = hashingSalt(form[1]);
        if(bd.mudaSenha(form) == 0){return "Erro, senha incorreta";}
        return "Sucesso!";
    }

    // Método para um administrador responder um usuario
    public String escreverRespostaADM(String[] form){
        if(form.length != 4 || form[0].length() < 64 || testeStrings(form) || !nivelUsuario){return "Erro";}
        return bd.insereRespostaComentario(form);
    }

    // Método para escrever um comentario
    public String escreveComentario(String[] form){
        if(form.length != 2 || form[0].length() < 64 || testeStrings(form)){return "Erro";}
        return bd.insereComentarioUser(form);
    }

    // Método para retornar comentarios para um administrador
    public String[] recebeComentariosADM(){
        if(!nivelUsuario){return new String[0];}
        ArrayList<String> comentarios = bd.retornaComentariosNovos();
        int i;
        String[] comentRetorno = new String[comentarios.size()];
        for(i = 0; i < comentRetorno.length; i++){
            comentRetorno[i] = comentarios.get(i);
        }
        return comentRetorno;
    }

    // Método para retornar comentarios de um usuario
    public String[] obtemComentariosUsuario(String[] form){
        if(form.length != 1 || form[0].length() < 64 || testeStrings(form)){return new String[0];}
        ArrayList<String> comentarios = bd.retornaComentariosUsuario(form[0]);
        int i;
        String[] comentRetorno = new String[comentarios.size()];
        for(i = 0; i < comentRetorno.length; i++){
            comentRetorno[i] = comentarios.get(i);
        }
        return comentRetorno;
    }

    // Método para retornar vacinas
    public String[] recebeVacinas(String[] form){
        if(form.length != 3 || form[2].length() < 64 || testeStrings(form)){return new String[0];}
        ArrayList<String> vacinas = bd.selecionaVacinas(form[2]);
        vacinasDisponiveis.clear();
        int i;
        for(i = 0; i < vacinas.size(); i++){
            if(!Boolean.parseBoolean(vacinas.get(i).split("'")[1])){
                vacinas.remove(i);
                i--;
            }
        }
        String[] listaVacinas = new String[vacinas.size()];
        for(i = 0; i < vacinas.size(); i++){
            listaVacinas[i] = vacinas.get(i).split("'")[0];
            vacinasDisponiveis.add(vacinas.get(i).split("'")[0].split("&")[0]);
        }
        return listaVacinas;
    }

    // Método para agendar vacina
    public String agendarVacina(String[] form){
        if(form.length != 3 || form[2].length() < 64 || testeStrings(form) || !vacinasDisponiveis.contains(form[0])){return "Erro";}
        vacinasDisponiveis.clear();
        try{
            String[] data = form[1].split("-")[0].split("/");
            String[] horario = form[1].split("-")[1].split(":");
            LocalDateTime dataAgendada = LocalDateTime.of(Integer.parseInt(data[2]),
                                                          Integer.parseInt(data[1]),
                                                          Integer.parseInt(data[0]),
                                                          Integer.parseInt(horario[0]),
                                                          Integer.parseInt(horario[1]));
            LocalDateTime dataTeste = LocalDateTime.now().plusDays(1);
            if(dataAgendada.isBefore(dataTeste)){
                return "O agendamento só pode ser feito com no mínimo 1 dia de antecedência";
            }
            dataTeste = dataTeste.plusDays(29);
            if(dataAgendada.isAfter(dataTeste)){
                return "O agendamento só pode ser feito com no máximo 30 dias de antecedência";
            }
        } catch(Exception ex){
            return "Data inválida!";
        }
        return bd.agendaVacina(form);
    }

    // Método para receber a carteirinha de vacinação de um usuario
    public String[] recebeCarteirinha(String[] form){
        if(form.length != 1 || form[0].length() < 64 || testeStrings(form)){return new String[0];}
        ArrayList<String> vacinas = bd.retornaCarteirinha(form);
        String[] listaVacinas = new String[vacinas.size()];
        int i;
        for(i = 0; i < listaVacinas.length; i++){
            listaVacinas[i] = vacinas.get(i).split("'")[0];
        }
        return listaVacinas;
    }

    // Método para deslogar usuario
    public boolean usuarioSair(String[] form){
        if(form.length != 1 || form[0].length() < 64 || testeStrings(form)){return true;}
        return (bd.limpaSessao(form[0]) == 0);
    }

    // MÉTODOS LOCAIS //

    // Testa formularios recebidos, procura por caracteres especiais
    private boolean testeStrings(String[] form){
        int[] chrTeste = {34, 35, 38, 39, 59, 60, 62, 92, 96};
        int i;
        int o;
        int p;

        for(i = 0; i < form.length; i++){
            try{
                for(o = 0; o < form[i].length(); o++){
                    for(p = 0; p < chrTeste.length; p++){
                        if(form[i].charAt(o) == chrTeste[p]){
                            return true;
                        }
                    }
                }
            } catch(NullPointerException e){
                return false;
            }
        }
        return false;
    }

    // Método para hash de senha+salt
    private String hashingSalt(String input){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return "erro";
        }
        input = input + "4PROGRAMA-SEGURO_SSS4";
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
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
        int i;
        int o;
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
        return (!(((dig2 == 10 || dig2 == 11)&& Character.getNumericValue(input.charAt(10)) == 0) || dig2 == Character.getNumericValue(input.charAt(10))));
    }

    // Método para enviar e-mail
    private boolean enviarEmail(String emailD, String assunto, String conteudo){
        final String username_email = "rafazeteste@gmail.com";
        final String password_email = "TeStandoHU1234";

        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
        
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username_email, password_email);
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
            return false;
        }
        return true;
    }
}
