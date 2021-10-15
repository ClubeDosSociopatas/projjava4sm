import server.Controlador;
import java.util.Scanner;
import java.math.BigInteger; 
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 

public class App {
    private static String chaveS = "";
    private static Scanner ler = new Scanner(System.in);
    private static Controlador ctrl = new Controlador();
    public static void main(String[] args) throws Exception {
        String leitura;
        while(true){
            if(!chaveS.equals("")){menuLogado();}
            System.out.print("- Menu Inicial -\n"+
                             "1 - Criar Conta\n"+
                             "2 - Efetuar Login\n"+
                             "3 - Sair\n"+
                             "Entre com sua escolha: ");
            leitura = ler.nextLine();
            if(leitura.equals("1")){criarConta();}
            if(leitura.equals("2")){efetuarLogin();}
            if(leitura.equals("3")){
                System.out.print("Programa Finalizado...");
                break;
            }
        }
    }

    private static void criarConta(){
        String formulario[] = new String[4];
        System.out.print("Nome de Usuario: ");
        formulario[0] = ler.nextLine();
        System.out.print("Senha: ");
        while((formulario[1] = testarSenha(ler.nextLine())) == "erro"){
            System.out.print("Senha precisa ter 10 caracteres, letra minúsculas e maiúsculas e no mínimo 1 caractere especial e número!\nSenha: ");
        }
        System.out.print("CPF: ");
        while(testarCpf(formulario[2] = ler.nextLine())){
            System.out.print("CPF inválido!\nCPF: ");
        };
        System.out.print("Email: ");
        formulario[3] = ler.nextLine();

        System.out.println(ctrl.novoUsuario(formulario));
    }

    private static void efetuarLogin(){
        String formulario[] = new String[3];
        String resultado;
        System.out.print("Nome de Usuario: ");
        formulario[1] = ler.nextLine();
        System.out.print("Senha: ");
        formulario[2] = testarSenha(ler.nextLine());
        if((resultado = ctrl.usuarioLogar(formulario)) != "erro"){
            System.out.println("Logado com Sucesso!\n");
            chaveS = resultado;
            return;
        }
        System.out.println("Falha no Login.\n");
    }

    private static void menuLogado(){
        String leitura;
        while(true){
            System.out.print("- Menu Logado -\n"+
                             "1 - Sair\n"+
                             "Entre com sua escolha: ");
            leitura = ler.nextLine();
            if(leitura.equals("1")){
                System.out.println("Usuario Desconectado...");
                chaveS = "";
                break;
            }
        }
    }

    private static String testarSenha(String input){
        if(input.length() < 10){return "erro";}
        if(input.toUpperCase() == input || input.toLowerCase() == input){return "erro";}
        String chrSpecTest = "!@#$%&.,";
        int i, o;
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

    private static String hashing(String input){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "erro";
        }
        byte hash[] = md.digest(input.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, hash);
  
        StringBuilder hexString = new StringBuilder(number.toString(16)); 
        while (hexString.length() < 64) { 
            hexString.insert(0, '0'); 
        }
        return hexString.toString();
    }

    private static boolean testarCpf(String input){
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
}
