# BPO - Sistema financeiro de usuário

## 📌 Sobre o Projeto
Projeto BPO, desenvolvimento de um assistente financeiro com foco no controle automatizado de lucros e gastos. Permitindo que clientes gerenciem cartões monitorem limites e acompanhem transações de forma mais fácil e inteligente.

## 🚀 Tecnologias
- Java 17
- Spring Boot 4
- MySQL (Banco de dados relacional)
- Lombok
- Maven
- Webhook Hotmart
- JWT
- MVC

## 🗄️ Banco de Dados
- cartão
- categoria
- dashboard
- usuario

### 📂 Model
Este pacote contém as classes que representam as entidades do sistema, definindo como os dados são armazenados no banco de dados e refletindo as regras de negócio.

### 📂 Dto
Inclui classes responsáveis por transportar dados entre as diferentes camadas da aplicação, especialmente entre o controller e a view. Esses objetos facilitam a transferência de dados de forma eficiente e segura.

### 📂 Repository
As interfaces neste pacote fazem a comunicação direta com o banco de dados através da JPA, gerenciando as operações de CRUD e outras consultas diretamente nas entidades.

### 📂 Controller
Contém as classes que lidam com as requisições HTTP recebidas pelo sistema. Essas classes processam as solicitações, executam a lógica de negócio necessária e retornam as respostas adequadas ao cliente.

###  👩‍💻  Responsável 
- **Vitor da Silva Pereira**
