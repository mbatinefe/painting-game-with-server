# Multiplayer Painting Game with Server

## Overview
This project is a multiplayer collaborative painting game implemented using Java. The application supports real-time communication and drawing, utilizing a client-server architecture to maintain synchronization across connected clients. Users can chat, draw on a shared canvas, and customize their experience with various tools and features.

## Server Implementation
<img width="620" alt="Screenshot 2024-12-23 at 12 09 02 AM" src="https://github.com/user-attachments/assets/fc02925e-47ce-404a-88b4-81157f4a3d75" />

## Features
- **Real-time Chat**: Users can personalize messages with usernames.
- **Dynamic Canvas**: Adjustable grid sizes with a synchronized drawing environment.
- **Brush and Pen Options**: Supports different painting modes, pen sizes, and dynamic brush flow.
- **Eraser Tool**: Allows selective clearing of drawn content.
- **Clear Canvas**: Resets the entire canvas for a fresh start.
- **Server Discovery**: Uses UDP for server discovery and TCP for reliable communication.

## Technical Details
- **IDE**: Eclipse
- **Programming Language**: Java
- **Networking**: Combines TCP for reliable data transmission and UDP for server discovery.
- **Concurrency**: Threads handle multiple client connections.

## System Requirements
- Java Development Kit (JDK) 8 or higher.
- Eclipse IDE or any compatible Java IDE.
- All clients must be connected to the same subnet.

## Setup Instructions

### Open in IDE:
- Load the `KidPaint_Client` and `Server` projects in Eclipse.

### Run the Server:
- Start the server by running `Server.java` as a Java application.

### Run the Client:
- Run the `KidPaint_Client.java` file as a Java application on each client machine.

### Connect to Server:
- Clients will auto-discover the server using UDP and establish TCP connections for communication.

## How to Use

### Enter Username:
- Upon launching, assign a username in the designated field.
<img width="796" alt="Screenshot 2024-12-23 at 12 10 13 AM" src="https://github.com/user-attachments/assets/c3fdf271-f775-40ad-a136-75209cff9d26" />

### Select Grid Size:
- Adjust canvas size from the dropdown menu.
<img width="816" alt="Screenshot 2024-12-23 at 12 10 39 AM" src="https://github.com/user-attachments/assets/bd81d04f-9b58-43fc-bfa5-a5871286124d" />

### Draw with Tools:
- Choose from brush, pen, or eraser tools. Adjust pen size as needed.
<img width="659" alt="Screenshot 2024-12-23 at 12 11 28 AM" src="https://github.com/user-attachments/assets/e4758055-f586-4e69-8872-824168ef70b4" />

### Clear the Canvas:
- Use the "Clear" button for a fresh canvas.
<img width="659" alt="Screenshot 2024-12-23 at 12 11 45 AM" src="https://github.com/user-attachments/assets/1a2045ce-375b-4c81-ab69-0569a78751aa" />

### Upload/Download Embedded Drawings
<img width="646" alt="Screenshot 2024-12-23 at 12 13 31 AM" src="https://github.com/user-attachments/assets/fa6102f0-8cc8-4b88-ac9e-d59c91f51b7a" />


## Contribution

| Contributor         | Contributions                            |
|---------------------|------------------------------------------|
| Mustafa Batin Efe   | Server implementation, clear function    |
| Luka Babetzki       | Adjustable window size, erasing tool     |
| Valeria Escobar     | Drawing features, report writing         |

## License
This project is licensed under the GNU General Public License v3.0 (GPL-3.0).

## Acknowledgments
Special thanks to the team members for their contributions and collaboration on this project.
