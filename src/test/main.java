package test;

import sessionManager.SessionFactory;

public class main
{
    
    public static void main(String[] args)
    {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome("Teste OneToMany");
        
        SessionFactory session = new ConfigureSession().getMySQLSession();
       
        session.onID(pessoa, 1);
        
        System.out.println(pessoa.getNome());
        
        for(Object obj : pessoa.getProfissao().ResultList)
        {
            Profissao prof = (Profissao)obj;
            
            System.out.println(prof.getDescricao());
        }
        
        
        session.close();
    }
}
