package br.com.persistor.test;

import br.com.persistor.enums.ResultType;
import br.com.persistor.sessionManager.SessionFactory;
import java.util.List;

public class main {

    public static void main(String[] args) 
    {
        SessionFactory session = new ConfigureSession().getFbSession();
        
//        Pessoa pessoa = new Pessoa();
//        pessoa.setNome("Marcos");
//        pessoa.setEmail("marcos8154@gmail.com");
//        pessoa.setTelefone("999486444");
//        pessoa.setEndereco("Rua vogue, 166");
//        
//        Veiculo veiculo = new Veiculo();
//        veiculo.setNome("Volkswagen Golf");
//        
//        pessoa.setVeiculo(veiculo);
//        
//        session.save(pessoa);
//        
//        Profissao profissao = new Profissao();
//        profissao.setDescricao("Programador");
//        
//        profissao.setPessoa_id(1);
//        
//        session.save(profissao);
//        
//        session.commit();
//
//        Pessoa pessoa = (Pessoa)session.onID(Pessoa.class, 1);

//teste mais um comentario
        session.close(); 
    }
}
