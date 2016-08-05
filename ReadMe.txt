Projetos de Buffer, Consumidor e Produtor feitos com Eclipse - Kepler, Java SE - 1.7

E necessario executar Buffer primeiro, com os seguintes 2 argumentos:
porta a ser ouvida
quantidade maxima de valores armazenados pelo buffer

Exemplo:
java -cp bin\ -jar Buffer.jar 9898 10

Consumidor e Produtor devem ser executados com os seguintes 3 argumentos:
IP do Servidor
Porta do Servidor
Numero de Threads a serem criadas

Exemplos(executando todos localmente):
java -cp bin\ -jar Produtor.jar localhost 9898 6
java -cp bin\ -jar Consumidor.jar localhost 9898 3
