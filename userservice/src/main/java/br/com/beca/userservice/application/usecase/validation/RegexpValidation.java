package br.com.beca.userservice.application.usecase.validation;

public class RegexpValidation {

    public static boolean cpf(String cpf){
        if (cpf.matches("^\\d{3}.\\d{3}.\\d{3}-\\d{2}$")){
            return true;
        } else {
            return false;
        }
    }

    public static boolean email(String email){
        if (email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,}$")){
            return true;
        } else {
            return false;
        }
    }

    public static boolean telefone(String telefone){
        if (telefone.matches("^\\+\\d{1,3}\\s\\d{2}\\s9\\d{4}-\\d{4}$")){
            return true;
        } else {
            return false;
        }
    }
}
