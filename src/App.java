// Classes Importadas
import java.util.Scanner;

import serverr.Controlador;

import java.math.BigInteger; 
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 

// Classe App/Cliente
public class App {
    // Variavel para Sessão / leitura de teclado / classe de controle
    private static String chaveS = "";
    private static Scanner ler = new Scanner(System.in);
    private static Controlador ctrl = new Controlador();

    // Método main
    public static void main(String[] args) {
        String leitura;
        // * MENU PRINCIPAL *
        while(true){
            if(!chaveS.equals("")){menuLogado();}
            System.out.print("- Menu Inicial -\n"+
                             "1 - Criar Conta\n"+
                             "2 - Efetuar Login\n"+
                             "3 - Confirmar email\n"+
                             "4 - Recuperar Senha\n"+
                             "5 - Informações de Contato\n"+
                             "0 - Sair\n"+
                             "Entre com sua escolha: ");
            leitura = ler.nextLine();
            if(leitura.equals("1")){criarConta();}
            if(leitura.equals("2")){efetuarLogin();}
            if(leitura.equals("3")){confirmaEmail();}
            if(leitura.equals("4")){recuperaSenha();}
            if(leitura.equals("5")){System.out.println("-- VaxxBank --\n"+
            "Email para contato: rafazeteste@gmail.com\n"+
            "Telefone para atendimento ao cliente: (41) 40557-9353");
            }
            if(leitura.equals("0")){
            System.out.print("Programa Finalizado...");
            break;
            }
        }
    }

    // Método para criar conta
    private static void criarConta(){
        String[] formulario = new String[5];
        System.out.print("Nome de Usuario: ");
        formulario[0] = ler.nextLine();
        System.out.print("Senha: ");
        while((formulario[1] = testarSenha(ler.nextLine())).equals("erro")){
            System.out.print("Senha precisa ter 10 caracteres, letra minúsculas e maiúsculas e no mínimo 1 caractere especial e número!\nSenha: ");
        }
        System.out.print("CPF: ");
        while(testarCpf(formulario[2] = ler.nextLine())){
            System.out.print("CPF inválido!\nCPF: ");
        }
        System.out.print("Email: ");
        formulario[3] = ler.nextLine();

        System.out.println(ctrl.novoUsuario(formulario));
    }

    // Método para efetuar login na aplicação
    private static void efetuarLogin(){
        String[] formulario = new String[3];
        String resultado;
        System.out.print("Nome de Usuario: ");
        formulario[1] = ler.nextLine();
        System.out.print("Senha: ");
        formulario[2] = testarSenha(ler.nextLine());
        resultado = ctrl.usuarioLogar(formulario);
        if(resultado.charAt(0) == 'i' && resultado.charAt(1) == 'd'){
            chaveS = resultado.substring(resultado.indexOf("=")+1);
            System.out.println("Logado com Sucesso!\n");
            return;
        }
        System.out.println(resultado);
    }

    // Método para entrar com o token de confirmação de e-mail
    private static void confirmaEmail(){
        String[] formulario = new String[1];
        System.out.print("Digite o token recebido: ");
        formulario[0] = ler.nextLine();
        System.out.println(ctrl.confirmarTokenE(formulario));
    }

    // Método para recuperar a senha por e-mail
    private static void recuperaSenha(){
        String[] formulario = new String[2];
        formulario[1] = "";
        System.out.print("Digite seu email(ou digite 0 para sair): ");
        while(true){
            formulario[1] = ler.nextLine();
            if(formulario[1].equals("0")){return;}
            if((ctrl.iniciarRecuperarSenha(formulario)).equals("c")){break;}
            System.out.print("Digite seu email(ou digite 0 para sair): ");
        }
        System.out.print("Token recebido: ");
        formulario[0] = ler.nextLine();
        System.out.print("Nova Senha: ");
        while((formulario[1] = testarSenha(ler.nextLine())).equals("erro")){
            System.out.print("Senha precisa ter 10 caracteres, letra minúsculas e maiúsculas e no mínimo 1 caractere especial e número!\nNova Senha: ");
        }
        System.out.println(ctrl.recuperarSenha(formulario));
    }

    // * MENU USUARIO LOGADO *
    private static void menuLogado(){
        String leitura;
        while(true){
            System.out.print("- Menu Logado -\n"+
                            "1 - Informações de usuario\n"+
                            "2 - Informações para contato\n"+
                            "0 - Sair\n"+
                            "Entre com sua escolha: ");
            leitura = ler.nextLine();
            if(leitura.equals("1")){menuInfoUsu();}
            if(leitura.equals("2")){System.out.println("-- VaxxBank --\n"+
                                                    "Email para contato: rafazeteste@gmail.com\n"+
                                                    "Telefone para atendimento ao cliente: (41) 40557-9353");
            }
            if(leitura.equals("0")){
                System.out.println("Usuario Desconectado...");
                chaveS = "";
                break;
            }
        }
    }

    // * MENU INFORMAÇÕES USUARIO *
    private static void menuInfoUsu(){
        String[] form = new String[1];
        form[0] = chaveS;
        String[] dados = ctrl.dadosUsuario(form);
        String leitura = "";
        while(true){
            System.out.print("- Dados de Usuario -\n"+
                            "Nome: "+dados[0]+"\n"+
                            "email: "+dados[1]+"\n"+
                            "CPF: "+dados[2]+"\n"+
                            "1 - Mudar seu email\n"+
                            "2 - Mudar sua senha\n"+
                            "0 - Sair\n"+
                            "Entre com sua escolha: ");
            leitura = ler.nextLine();
            if(leitura.equals("1")){
                mudarEmail();
            }
            if(leitura.equals("2")){
                mudarSenha();
            }
            if(leitura.equals("0")){
                break;
            }
        }
    }

    // Método para mudar email
    private static void mudarEmail(){
        String[] formulario = new String[2];
        formulario[1] = chaveS;
        if(ctrl.comecaMudancaEmail(formulario).equals("Erro")){return;}
        System.out.print("Token recebido: ");
        formulario[0] = ler.nextLine();
        System.out.print("Nova e-mail: ");
        formulario[1] = ler.nextLine();
        System.out.println(ctrl.mudaEmail(formulario));
    }

    // Método para mudar senha
    private static void mudarSenha(){
        String[] formulario = new String[3];
        String resultado;
        System.out.print("Senha atual: ");
        formulario[0] = testarSenha(ler.nextLine());
        System.out.print("Nova senha: ");
        while((formulario[1] = testarSenha(ler.nextLine())).equals("erro")){
            System.out.print("Senha precisa ter 10 caracteres, letra minúsculas e maiúsculas e no mínimo 1 caractere especial e número!\nNova senha: ");
        }
        formulario[2] = chaveS;
        resultado = ctrl.mudaSenha(formulario);
        if(resultado.charAt(0) == 'i' && resultado.charAt(1) == 'd'){
            chaveS = resultado.substring(resultado.indexOf("=")+1);
            System.out.println("Logado com Sucesso!\n");
            return;
        }
        System.out.println(resultado);
    }


    // Testa a senha pelo seu tamanho, se tem ambos caracteres maisculos e minusculos, se tem caracteres especiais e numeros,
    // retorna o hash da senha
    private static String testarSenha(String input){
        if(input.length() < 10){return "erro";}
        if(input.toUpperCase().equals(input) || input.toLowerCase().equals(input)){return "erro";}
        String chrSpecTest = "!@#$%&.,";
        int i;
        int o;
        boolean testeStr = true;
        for(i = 0; i < input.length(); i++){
            for(o = 0; o < chrSpecTest.length(); o++){
                if(input.charAt(i) == chrSpecTest.charAt(o)){testeStr = false;}
            }
        }
        if(testeStr){return "erro";}
        chrSpecTest = "0123456789";
        testeStr = true;
        for(i = 0; i < input.length(); i++){
            for(o = 0; o < chrSpecTest.length(); o++){
                if(input.charAt(i) == chrSpecTest.charAt(o)){testeStr = false;}
            }
        }
        if(testeStr){return "erro";}
        return hashing(input);
    }

    // Método para o hashing de senha
    private static String hashing(String input){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "erro";
        }
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, hash);
  
        StringBuilder hexString = new StringBuilder(number.toString(16)); 
        while (hexString.length() < 64) { 
            hexString.insert(0, '0'); 
        }
        return hexString.toString();
    }

    // Método para testar se o cpf é verdadeiro
    private static boolean testarCpf(String input){
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
}
