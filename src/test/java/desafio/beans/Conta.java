package desafio.beans;

public class Conta {

	private String email;
	private String senha;
	private String conta;
	private String digito;
	private int saldo;

	public Conta(String email, String senha, String contaEDigito) {
		super();
		int index = contaEDigito.indexOf("-");
		this.email = email;
		this.senha = senha;
		this.conta = contaEDigito.substring(1, index);
		;
		this.digito = contaEDigito.substring(index + 1);
		;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getConta() {
		return conta;
	}

	public void setConta(String conta) {
		this.conta = conta;
	}

	public String getDigito() {
		return digito;
	}

	public void setDigito(String digito) {
		this.digito = digito;
	}

	public int getSaldo() {
		return saldo;
	}

	public void setSaldo(String valor, String tipo) {
		int valorNovo = Integer.parseInt(valor.replaceAll("(?:[^0-9])", ""));
		if (tipo.equals("credito"))
			this.saldo = getSaldo() + valorNovo;
		if (tipo.equals("debito"))
			this.saldo = getSaldo() - valorNovo;
	}

}
