import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


import java.util.concurrent.ThreadLocalRandom;

/**
 * Cliente que gera Threads em que cada uma emvia um valor com a 
 * de um numero a ser retirado de Buffer (Servidor).
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
 * Consumidor apenas retira valores, dessa forma usando apenas REMOVE.
 * 
 * Inicializacao:
 * Ao inicializar o programa devem ser passados os argumentos:
 * 		[0] - IP do Servidor
 * 		[1] - Porta do Servidor
 * 		[2] - Numero de Threads a serem criadas
 */

public class Consumidor {

	private BufferedReader in;
    private PrintWriter out;

    /**
     * Construtor declarado para clareza
     */
    public Consumidor() {

    }
    
    private static class clientThread extends Thread{
    	int rngNum; 
    	Consumidor client;
    	String[] args;
    	String name;
    	
    	
    	public clientThread(Consumidor client, String[] args, String threadNumber){
    		this.rngNum = randomNum();
    		this.client = client;
    		this.args = args;
    		this.name = "Consumidor" + threadNumber;
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
        // Recebendo o tamanho do Buffer do Servidor
    	in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
    	int randomPosition = randomNum(Integer.parseInt(in.readLine()));
    	
    	// Gerando a mensagem seguindo o protocolo REMOVE
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println("REMOVE " + randomPosition);
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
    	Consumidor client;
    	String threadNumber;
    	for(int i = 0; i < numOfThreads; i++){
    		client = new Consumidor();
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
    
    /**
     * Gera um numero aleatorio entre [0..limit-1]
     */
    public static int randomNum(int limit){
    	if(limit == 0){
    		return 0;
    	}
    	return ThreadLocalRandom.current().nextInt(0, limit);
    }
}