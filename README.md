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
  <img width="800" alt="Game board" src="https://github.com/user-attachments/assets/d58c21c7-664c-429a-ae3a-846807878443">
  <img width="800" alt="Game board" src="https://github.com/user-attachments/assets/ce002c0d-f440-4a47-965e-bfb3a525a29c">
</div>

### Lobby & Connection

<div align="center">
  <img width="260" alt="Connection" src="https://github.com/user-attachments/assets/6b655044-6d3b-48df-b2ae-468fbe6fcfe4">
  <img width="260" alt="Connection" src="https://github.com/user-attachments/assets/6c4ece93-71bf-49c9-818b-da16c9e7dc4a">
  <img width="260" alt="Lobby" src="https://github.com/user-attachments/assets/e3c6d462-c764-4f0c-b7c9-83ccc3a2f3f5">
</div>

### Events

<div align="center">
  <img width="340" alt="Events" src="https://github.com/user-attachments/assets/27ca1f89-5078-4892-946b-5fb50292fb62">
</div>

### Scoreboard & Leaderboard

<div align="center">
  <img width="320" alt="Scoreboard" src="https://github.com/user-attachments/assets/f6306b89-7d86-416d-a352-4d92157d5c56">
  <img width="320" alt="Leaderboard" src="https://github.com/user-attachments/assets/34709e7d-5885-448c-8fdb-b5cf8a9f2ab5">
</div>

<br><br>

## Disclaimer

Mesos is a board game developed and published by Cranio Creations S.r.l..
All graphical content of this project attributable to the board game is used with the approval of Cranio Creations S.r.l. exclusively for educational purposes. Distribution, copying, or reproduction of the contents and images in any form outside this project is prohibited, as is the redistribution and publication of the contents and images for purposes other than the aforementioned. Commercial use of the aforementioned content is also prohibited.
