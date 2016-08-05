import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


import java.util.concurrent.ThreadLocalRandom;

/**
 * Cliente que gera Threads em que cada uma emvia um valor inteiro a 
 * ser armazenado em um Buffer (Servidor).
 * 
 * Usando um protocolo proprio, os primeiros 6 caracteres da 
 * informacao recebida contem o tipo de operacao que sera efetuada.
 *
 * INSERT	---> Insere um valor no Buffer 
 * 						(Recebe um inteiro com o valor a ser 
 * 						inserido ao final do Buffer)
 * REMOVE 	---> Retira um valor do Buffer
 * 						(Recebe a posicao no Buffer do valor
 * 						a ser retirado)
 * 
 * Produtor apenas insere valores, dessa forma usando apenas INSERT.
 * 
 * Inicializacao:
 * Ao inicializar o programa devem ser passados os argumentos:
 * 		[0] - IP do Servidor
 * 		[1] - Porta do Servidor
 * 		[2] - Numero de Threads a serem criadas
 */

public class Produtor {

	private BufferedReader in;
    private PrintWriter out;
    
    /**
     * Construtor declarado para clareza
     */
    public Produtor() {

    }
    
    /**
     * Thread responsavel por gerar um numero aleatorio e conectar ao Servidor
     */
    private static class clientThread extends Thread{
    	int rngNum; 
    	Produtor client;
    	String[] args;
    	String name;
    	
    	
    	public clientThread(Produtor client, String[] args, String threadNumber){
    		this.rngNum = randomNum();
    		this.client = client;
    		this.args = args;
    		this.name = "Produtor" + threadNumber;
    	}
    	
    	public void run() {
    		synchronized(client){
	    		try{
	    			Socket socket = new Socket(this.args[0], Integer.parseInt(this.args[1]));
	    			client.connectToServer(
	    					socket,
	    					this.rngNum, // Numero aleatorio
	    					this.name); // Nome da Thread
	    			socket.close();
	    		} catch (IOException e){
	    			System.out.println("Erro conectando ao Servidor.");
	    		}
    		}
    	}
    }

    /**
     * Prepara o protocolo para se conectar ao Servidor
     */    
    public void connectToServer(Socket socket, int randomNumber, String name) throws IOException {
    	// Limpando o tamanho de Buffer enviado pelo Servidor
    	in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
    	in.readLine();
    	
    	// Gerando a mensagem seguindo o protocolo INSERT
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println("INSERT " + randomNumber);
        out.println(name);
    }

    /**
     * Roda a Aplicacao do Cliente.
	 * Inicializacao:
	 * Ao inicializar o programa devem ser passados os argumentos:
	 * 		[0] - IP do Servidor
	 * 		[1] - Porta do Servidor
	 * 		[2] - Numero de Threads a serem criadas
	 */
    public static void main(String[] args) throws Exception {
    	int numOfThreads = Integer.parseInt(args[2]);
    	System.out.println(numOfThreads);
    	Produtor client;
    	String threadNumber;
    	for(int i = 0; i < numOfThreads; i++){
    		client = new Produtor();
    		threadNumber = Integer.toString(i+1);
    		new clientThread(client,args, threadNumber).start();
    	}
    }
    
    /**
     * Gera um numero aleatorio entre [1..100]
     */
    public static int randomNum(){
    	return ThreadLocalRandom.current().nextInt(1, 100 + 1);
    }
}