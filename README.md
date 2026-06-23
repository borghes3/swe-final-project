<div align="center">
<img width="682" height="217" alt="Screenshot mesos" src="https://github.com/user-attachments/assets/6609e033-9491-451e-ac77-3fb54732468f" />
  <br><br>
</div>  

<div align="center">
Digital version of the board game Mesos.
</div>

<div align="center">
Final Project of Software Engineering at Polytechnic University of Milan - 2025/2026
</div>

<br><br>

## About the project

This project is a digital client-server implementation of the board game Mesos, originally published by Cranio Creations. 

Players take on the role of a tribal leader in the Mesolithic era, growing their community by drafting character cards, building specialized buildings, and preparing for events. The software system is built entirely in JavaSE and strictly follows the Model-View-Controller (MVC) architectural pattern.

### AM23 Team
* *Alessandro Brivio* 
* *Ivano Dalla Costa* 
* *Nyjil John Arackal*
* *Ilaria Annovazzi*
  
<br>

## Implemented Features

The project fully implements the core game logic alongside several technical requirements and advanced features:

| Feature | State |
| :--- | :---: |
| Simplified Rules | ✅ |
| Complete Rules | ✅ |
| Textual User Interface (TUI) | ✅ |
| Graphical User Interface (GUI) | ✅ |
| Socket Connection | ✅ |
| RMI Connection | ✅ |
| *FA:* DB Leaderboard | ✅ |
| *FA:* Multiple Games | ✅ |
| *FA:* Persistence | ❌ |
| *FA:* Resilience to Disconnections | ❌ |

<br>


## Software Requirements

* *OS:* Windows, MacOS, Linux
* *Java Runtime Environment:* Version 21
* *Build Tool:* Maven

<br>

## Database Setup (Leaderboard)

The leaderboard relies on a local *PostgreSQL* database. The game also runs without it (offline mode), but scores won't be saved persistently.

1. Install *PostgreSQL* and create the database:
   *CREATE DATABASE mesos_leaderboard;*
2. In src/main/resources, copy db.properties.example to a new file named *db.properties* and fill in your local credentials:
   * url: jdbc:postgresql://localhost:5432/mesos_leaderboard
   * user: your local username
   * password: your local password (if set)
3. Make sure the PostgreSQL service is running before starting the server. The required tables are created automatically on first launch.

<br>

## How to Compile

To compile the project and generate the executable JAR files, open a terminal in the root folder of the project and run:

*mvn clean package*

The compiled JAR files will be placed in the target directory.

<br>

## How to Run

### Server
The server manages the game logic and must be instantiated to host a match.

1. Navigate to the project root folder (where the target directory is).
2. Run the server:

   *java "-Djava.rmi.server.hostname=SERVER_IP" -cp target\IS26-AM23-1.0-SNAPSHOT.jar it.polimi.ingsw.am23.network.ServerLauncher*

   Replace *SERVER_IP* with the IP address of the machine hosting the server.
3. Set the ports for Socket and/or RMI connections when prompted.

<br>

### Client
The client allows players to join matches and requires the server's IP address to connect. It can be instantiated multiple times, even on the same machine.

1. Navigate to the project root folder (where the target directory is).
2. Run the client:

   *java -cp target\IS26-AM23-1.0-SNAPSHOT.jar it.polimi.ingsw.am23.view.ClientLauncher*

   If you are using RMI behind NAT/port forwarding, add the hostname and callback options:

   *java "-Djava.rmi.server.hostname=YOUR_IP" -cp target\IS26-AM23-1.0-SNAPSHOT.jar it.polimi.ingsw.am23.view.ClientLauncher --rmiCallback=FORWARDED_PORT*

3. Select your preferred network technology (Socket or RMI).
4. Select your preferred user interface (TUI or GUI).
5. Provide a unique nickname to join a lobby.

<br>

## Project Structure
The codebase strictly follows the *Model-View-Controller* pattern: the *model* holds the game logic, the *view* provides both the TUI and GUI, the *controller* coordinates the game flow, and the *network* layer handles Socket and RMI communication.

<br>

## Documentation

* *UML diagrams* are available in the umls folder.
* *JavaDoc* is available in the javadoc folder (open javadoc/index.html in a browser).

<br>

## Testing

Unit tests focus on the model and game logic. To run them:  *mvn test*

Coverage reports are available in the coverage folder.

<br>

## Screenshots

### Game Board

<div align="center">
  <img width="800" alt="GUI Game Board" src="https://github.com/user-attachments/assets/85c6272e-e70d-4e0c-92a3-5602c45cba76" />
  <img width="800" alt="CLI Game Board" src="https://github.com/user-attachments/assets/1d36d191-eda7-4691-8756-bcf906d3c184" />
</div>

### Lobby & Connection

<div align="center">
  <img width="260" alt="CLI Connection" src="https://github.com/user-attachments/assets/349fb434-6c01-4773-8723-70adfb0de27c" />
  <img width="260" alt="GUI Connection" src="https://github.com/user-attachments/assets/c48a2e03-6759-4b74-a8d0-765a1d2edff9" />
  <img width="260" alt="GUI Lobby" src="https://github.com/user-attachments/assets/69b73cea-5237-4e6e-a1e0-4858b22068b4" />

</div>

### Events

<div align="center">
  <img width="340" alt="GUI Events" src="https://github.com/user-attachments/assets/c70ef2cc-eecf-4d92-b090-f6cad3162feb" />
</div>

### Scoreboard & Leaderboard

<div align="center">
  <img width="320" alt="CLI Scoreboard" src="https://github.com/user-attachments/assets/e638638c-880d-4f52-8ad3-ca2ca1fa40ca" />
  <img width="320" alt="GUI Leaderboard" src="https://github.com/user-attachments/assets/1257bb73-60d7-47b2-b2c2-d9cb468d55fb" />
</div>

<br><br>

## Disclaimer

Mesos is a board game developed and published by Cranio Creations S.r.l..
All graphical content of this project attributable to the board game is used with the approval of Cranio Creations S.r.l. exclusively for educational purposes. Distribution, copying, or reproduction of the contents and images in any form outside this project is prohibited, as is the redistribution and publication of the contents and images for purposes other than the aforementioned. Commercial use of the aforementioned content is also prohibited.
