# SimpleMultipersonChat
Simple multiperson chat with RSA encryption.
The project includes a server and a client. The client is made as a desktop application.

### Server commands
The table below shows what commands the server can execute.
| command | parameters | description |
|---------|------------|-------------|
| CONNECT | client public key | Set a secure connection between client and server. |
| REGISTER | login nickname password | Register new user. |
| LOGIN | login password | Login user. |
| SEND | type(only TEXT works) nickname | Send a text message to all avaliable users. |

### Security
The process of communication between the client and the server is as follows:
1. Client generates public and private keys.
2. Client sends public key to the server.
3. Server gets key and generates its own public and private keys.
4. Server sends public key to the client.
5. Client encrypts server passowrd with server public key and sends it.
6. Server decrypts password and checks if it's correct.
7. If all the preceding points were successful, server will assume that the connection is secure.
8. From now on, all messages between the server and the client will be encrypted using each other's public keys.

> Without establishing a secure connection, the server will not process requests containing personal data.

Each new user is saved to the database and stored as a login, nickname, salt, password hash.
Each user password encrypts with a random 16-bit salt and a PBKDF2WithHmacSHA1 algorithm.

The server stores the user's message in a separate file. All messages are encrypted with an AES key before being stored. Also, if the "massagesStorageBackupFile.backup" field is set to true in the server config file, once every "massagesStorageBackupFile.backupFrequency" seconds, a backup will be performed.

The server also knows how to make logs, in order to make it do logs, you must specify the "logs.on" parameter as true in the configuration file (the option is already enabled by default).

### Config file
The server contains a config file with all server settings.

> The server config file will be automatically created after the first start of the server. The path will be: "./". If you want to change the default file creation path, start the server with the argument: "-config-path newPath".

| setting | default value | description |
|---------|---------------|-------------|
| server.ip | localhost | IP address where the server will run. |
| server.port | 8080 | Port where server will run. |
| server.backlog | 50 | Max number of clients waiting for accepting connection. |
| server.password | 12345 | Server password. |
| secure.RSAKeyLength | 2048 | This setting affects the length of the private and public keys. |
| secure.serverKeyLength | 256 | This setting affects the length of AES key by which the server encrypts client messages before storing. |
| secure.serverKeyFileDir | ./keys/ | AES key storage dir. |
| secure.serverKeyFileName | serverKey.txt | AES key file name. |
| logs.on | true | If true, server creates a new log file every time when it starts, then logs all server actions. |
| logs.dir | ./logs/ | Directory where all server logs will be stored. |
| userDB.dir | ./ | Directory where user database will be stored. |
| userDB.name | user_db.db | The name of user database. |
| userDB.fullPath | userDB.dir+userDB.name | Full user database path. |
| messagesStorageFile.dir | ./ | Directory where client messages will be stored. |
| messagesStorageFile.name | messages.txt | Client messages file name. |
| messagesStorageFile.fullPath | messagesStorageFile.dir+messagesStorageFile.name | Full client messages file path. |
| messagesStorageBackupFile.backup | true | If true, server creates client messages backup. |
| messagesStorageBackupFile.backupFrequency | 1800000 | Backup freequency in ms. |
| messagesStorageBackupFile.dir | ./backup/ | Backup files dir. |

### Launch
1. Create a javaFx project, then replace the src folder with the one provided by this project.
2. Download and connect SQLITE JDBC, you can download it [here](https://github.com/xerial/sqlite-jdbc/releases).
3. Download and connect the JAVAFX SDK, you can download it [here](https://gluonhq.com/products/javafx/). If you don't know how to connect JAVAFX SDK, here a [helpful video](https://youtu.be/9XJicRt_FaI?t=494).
4. If you want to start a server, run src.Server.java.
5. If you want to start a desktop client, run src.Client.java.

> Don't forget, if you want to specify the path to create the config file, specify the argument: "-config-path newPath" when starting the server.
