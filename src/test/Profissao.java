package test;

import abstractClasses.Entity;
import annotations.PrimaryKey;

public class Profissao extends Entity
{
	private int id;
	private String descricao;
	
	@PrimaryKey
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
