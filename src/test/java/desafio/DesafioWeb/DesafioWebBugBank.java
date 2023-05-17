package desafio.DesafioWeb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import desafio.beans.Conta;

public class DesafioWebBugBank {

	private WebDriver driver = new ChromeDriver();
	Conta contaTransfere;
	Conta contaRecebe;
	private String msgDescricaoTransferencia = "Teste transferência desafio web";
	private String msgTransferenciaRealizada = "Transferencia realizada com sucesso";
	private String msgTransferenciaEnviada = "Transferência enviada";
	private String msgTransferenciaRecebida = "Transferência recebida";

	/**
	 * Este método de teste chama todos os métodos necessários para realizar as
	 * operações de criação de contas, login, transferência e validação das
	 * operações pelo extrato.
	 */
	@Test
	public void testTransferenciaEntreContas() {
		System.setProperty("webdriver.chrome.driver", "./target/webdriver/chromedriver.exe");
		driver.get("https://bugbank.netlify.app/");
		driver.manage().window().maximize();

		String contaEDigito1 = registrarConta("primeiraconta@teste.com", "Primeira Conta", "primeirasenha123", true);
		contaTransfere = new Conta("primeiraconta@teste.com", "primeirasenha123", contaEDigito1);

		driver.navigate().refresh();
		String contaEDigito2 = registrarConta("segundaconta@teste.com", "Segunda Conta", "segundasenha123", true);
		contaRecebe = new Conta("segundaconta@teste.com", "segundasenha123", contaEDigito2);

		realizarLogin(contaTransfere);

		String valorTransferencia = "500";
		fazerTransferencia(contaTransfere, contaRecebe.getConta(), contaRecebe.getDigito(), valorTransferencia);
		validarExtrato(valorTransferencia, "enviada");

		sair();

		realizarLogin(contaRecebe);
		validarExtrato(valorTransferencia, "recebida");

		driver.quit();

	}

	/**
	 * método para registrar e criar uma nova conta passando como parâmetro os
	 * valores dos campos necessários para que o registro seja salvo com sucesso.
	 * 
	 * Este método retorna ao final o numero da conta criada em uma unida string
	 * (incluindo o dígito)
	 * 
	 * @param email
	 * @param nome
	 * @param senha
	 * @param contaComSaldo
	 * @return numero da conta com o dígito
	 */
	private String registrarConta(String email, String nome, String senha, Boolean contaComSaldo) {
		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div[1]/form/div[3]/button[2]")).click();

		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div[2]/form/div[2]/input")).sendKeys(email);
		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div[2]/form/div[3]/input")).sendKeys(nome);
		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div[2]/form/div[4]/div/input")).sendKeys(senha);
		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div[2]/form/div[5]/div/input")).sendKeys(senha);

		if (contaComSaldo) {
			do {
				driver.findElement(By.id("toggleAddBalance")).click();
			} while (!driver.findElement(By.id("toggleAddBalance")).isEnabled());
			assertTrue(driver.findElement(By.id("toggleAddBalance")).isEnabled());
		}

		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div[2]/form/button")).click();

		aguardarElemento(By.id("modalText"));

		assertTrue(driver.findElement(By.id("modalText")).isDisplayed());

		String contaComDigito = pegarNumeroDaContaCriada();

		driver.findElement(By.id("btnCloseModal")).click();

		return contaComDigito;

	}

	/**
	 * Método para aguardar um elemento ser exibido na tela. Como o selenium realiza
	 * as ações muito rápido então muitas vezes os elementos não estão todos
	 * carregados na tela e acabam dando erro em um assert ou em outra chamada do
	 * selenium. Este método serve para, enquanto o elemento não estiver carregado e
	 * exibido na tela então ele deverá continuar esperando.
	 * 
	 * @param locator
	 */
	private void aguardarElemento(By locator) {
		WebElement elemento;
		do {
			elemento = (new WebDriverWait(driver, Duration.ofSeconds(30)))
					.until(ExpectedConditions.presenceOfElementLocated(locator));
		} while (!elemento.isDisplayed());

	}

	/**
	 * Método criado para retornar o numero da conta criada capturando através do
	 * modal que aparece na tela após registrar a conta com sucesso.
	 * 
	 * @return numero da conta em uma string
	 */
	private String pegarNumeroDaContaCriada() {
		String msgContaCriada = driver.findElement(By.id("modalText")).getText();
		int indexFoiCriadaComSucesso = msgContaCriada.indexOf("foi");
		String numeroConta = msgContaCriada.substring(7, indexFoiCriadaComSucesso - 1);

		return numeroConta;

	}

	/**
	 * Método para realizar login na aplicação. Ao realizar login com sucesso o
	 * valor do saldo é capturado e salvo na conta para validações futuras.
	 * 
	 * @param conta
	 */
	private void realizarLogin(Conta conta) {
		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div[1]/form/div[1]/input"))
				.sendKeys(conta.getEmail());
		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div[1]/form/div[2]/div/input"))
				.sendKeys(conta.getSenha());

		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div/div[1]/form/div[3]/button[1]")).click();

		aguardarElemento(By.id("textBalance"));

		conta.setSaldo(driver.findElement(By.id("textBalance")).getText(), "credito");

	}

	/**
	 * Método para realizar uma transferência, onde é passado como parâmetro a conta
	 * que realizará a transferência, o numero e dígito da conta a receber e o valor
	 * a ser transferido.
	 * 
	 * @param conta
	 * @param numeroConta
	 * @param digito
	 * @param valorTransferencia
	 */
	private void fazerTransferencia(Conta conta, String numeroConta, String digito, String valorTransferencia) {
		driver.findElement(By.id("btn-TRANSFERÊNCIA")).click();

		aguardarElemento(By.xpath("//*[@id=\"__next\"]/div/div[3]/form/button"));

		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[3]/form/div[1]/div[1]/input")).sendKeys(numeroConta);
		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[3]/form/div[1]/div[2]/input")).sendKeys(digito);

		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[3]/form/div[2]/input")).sendKeys(valorTransferencia);
		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[3]/form/div[3]/input"))
				.sendKeys(msgDescricaoTransferencia);

		driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[3]/form/button")).click();

		aguardarElemento(By.id("modalText"));

		assertEquals(msgTransferenciaRealizada, driver.findElement(By.id("modalText")).getText());
		conta.setSaldo(valorTransferencia + "00", "debito");

		driver.findElement(By.id("btnCloseModal")).click();

		driver.findElement(By.id("btnBack")).click();
	}

	/**
	 * Método para realizar logout na aplicação clicando no botão Sair.
	 */
	private void sair() {
		driver.findElement(By.id("btnExit")).click();
	}

	/**
	 * Método para realizar validação de extrato como descrição, tipo da operação
	 * realizada e valor
	 * 
	 * @param valor
	 * @param tipo
	 */
	private void validarExtrato(String valor, String tipo) {
		driver.findElement(By.id("btn-EXTRATO")).click();

		aguardarElemento(By.xpath("//*[@id=\"textTypeTransaction\"]"));

		String msgOperacao = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[3]/div/div[2]/div[2]"))
				.findElement(By.id("textTypeTransaction")).getText();
		String valorOperacao = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[3]/div/div[2]/div[2]"))
				.findElement(By.id("textTransferValue")).getText();
		String descricaoOperacao = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[3]/div/div[2]/div[2]"))
				.findElement(By.id("textDescription")).getText();

		if (tipo.equals("recebida")) {
			assertEquals(msgTransferenciaRecebida, msgOperacao);
		} else if (tipo.equals("enviada")) {
			assertEquals(msgTransferenciaEnviada, msgOperacao);
		}
		assertEquals(msgDescricaoTransferencia, descricaoOperacao);
		assertEquals(valor + "00", valorOperacao.replaceAll("(?:[^0-9])", ""));

	}

}
