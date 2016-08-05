import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;

/**
 * Servidor que armazena numeros inteiros.
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
 * Inicializacao:
 * Ao inicializar o programa devem ser passados os argumentos:
 * 		[0] - porta a ser ouvida
 * 		[1] - quantidade maxima de valores armazenados pelo buffer
 */
public class Buffer {
	
    /**
     * Funcao principal para ouvir a porta dada
     */
    public static void main(String[] args) throws Exception {        
        int port = Integer.parseInt(args[0]);
        int numValues = Integer.parseInt(args[1]);
        int clientNumber[] = {0};
        ServerSocket listener = new ServerSocket(port);
        
        System.out.println("Server Buffer esta rodando.");
        
        BufferArray buffer = new BufferArray(numValues);
        
        boolean busy = false;
        
        try {
            while (true) {
            	// Limitando quantidade de Threads concorrentes
            	// O numero de clientes (clientNumber) acessando o servidor
            	// simultaneamente nao pode ser maior que o tamanho
            	// maximo de Buffer (numValues)
            	if (clientNumber[0] > numValues && !busy){
            		busy = true;
            		System.out.println("O servidor esta ocupado.");
            		continue;
            	} else if(clientNumber[0] <= numValues){
            		busy = false;
            	} else if (busy){
            		continue;
            	}
            	// Iniciando uma Thread para cada Client
                new BufferThread(listener.accept(), clientNumber, buffer).start();
            }
        } finally {
            listener.close();
        }
    }
    
    /**
     * Classe usada para gerenciar o armazenamento de dados do Buffer
     */
    public static class BufferArray{    	
    	private ArrayList<Integer> bufferArray = new ArrayList<Integer>();
    	private int maxSize;
    	
    	public BufferArray(int maxSize){
    		this.maxSize = maxSize;
    		System.out.println("Buffer preparado para receber ate: " + maxSize + " valores.");
    	}
    	
    	public void insertValue(int value){
    		if(maxSize > bufferArray.size()){
    			this.bufferArray.add(value);
    		}
    	}
    	
    	public int removeValueAt(int position){
    		if(this.bufferArray.size() > 0){
    			if(this.bufferArray.get(position) != null){
    				return this.bufferArray.remove(position);
    			}
    		}
    		return 0;

    	}    	
    }

    /**
     * Thread usada para gerenciar acesso ao buffer e comunicacao
     * 		Server <---> Client
     */
    private static class BufferThread extends Thread {
        private Socket socket;
        private int clientNumber[];
        BufferArray buffer;

        public BufferThread(Socket socket, int clientNumber[], BufferArray buffer) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            clientNumber[0]++;
            this.buffer = buffer;
        }

        /**
         * Reimplementacao de run() para execucao da Thread
         */
        public void run() {
        	synchronized(buffer){
        		String input;
                String threadName = "default";
        		
                long startTime = System.nanoTime();
                
	            try {
	            	// Gerando entradas e saidas do servidor
	                BufferedReader in = new BufferedReader(
	                        new InputStreamReader(socket.getInputStream()));
	                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

	                // Informa ao cliente quantos numeros estao no Buffer
	                out.println(buffer.bufferArray.size());
	                // Recebe do cliente o protocolo com o valor a ser tratado
	                input = in.readLine();
	                // Recebe do cliente o nome da Thread
	                threadName = in.readLine();
	                
	                // Inicia tratamento do protocolo
	                // Caso INSERT adiciona o valor ao Buffer
	                if(input.startsWith("INSERT")){
	                	if(buffer.bufferArray.size() < buffer.maxSize){
	                		try {
		                       	 this.buffer.insertValue(Integer.parseInt(input.substring(7)));
		                       	 log("Valor "
			                       	 + Integer.parseInt(input.substring(7))
			                       	 + " adicionado em Buffer pelo "
			                       	 + threadName
			                       	 + " em "
			                       	 + elapsedTime(startTime)
			                       	 + "ms.");
	                       } catch (NumberFormatException e){
	                    	   log("O valor entrado nao e um numero inteiro");
	                       }
	                	} else {
	                		log("O Buffer esta cheio.");
	                		for(Integer b : buffer.bufferArray){
	                			System.out.println("=>" + b);
	            			}
	                	}
	                } 
	                // Caso REMOVE retira o numero da posicao dada do Buffer
	                else if(input.startsWith("REMOVE")){
	                	if(buffer.bufferArray.size() > 0){
	                		int position = Integer.parseInt(input.substring(7));
	                		try {
		                       	 if(this.buffer.bufferArray.get(position) != null){
		                       		log("Valor "
		   		                       	 + buffer.removeValueAt(position)
		   		                       	 + " removido de Buffer pelo "
		   		                       	 + threadName
				                       	 + " em "
				                       	 + elapsedTime(startTime)
				                       	 + "ms.");
		                       	 } else {
		                       		 log("A posicao " 
		                       				 + position 
		                       				 + " desejada nao existe no Buffer");
		                       	 }
		                       	 
	                		} catch (NumberFormatException e){
	                   	   		log("O valor escolhido nao e um numero inteiro");
	                		}
	                	} else {
	                		log(threadName + " tentou retirar item do Buffer vazio");
	                	}
	                }
	            } catch (IOException e) {
	                log("Erro lidando com " + threadName + ": " + e);
	            } finally {
	                try {	                	
	                    socket.close();	                    
	                } catch (IOException e) {
	                    log("Nao foi possivel fechar o socket, o que houve?");
	                }
	                // Controle para o numero maximo de Threads atuando ao mesmo tempo
	                this.clientNumber[0]--;
	            }
        	}
        }

        /**
         * Organiza print de forma mais simples
         */
        private void log(String message) {
            System.out.println(message);
        }
        
        /**
         * Clareza para tempo de execucao
         */        
        private String elapsedTime(long startTime){
        	return String.format("%.2f",((System.nanoTime() - startTime)*0.000001));
        }
    }
}