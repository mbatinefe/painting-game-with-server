# Multiplayer Painting Game with Server

## Overview
This project is a multiplayer collaborative painting game implemented using Java. The application supports real-time communication and drawing, utilizing a client-server architecture to maintain synchronization across connected clients. Users can chat, draw on a shared canvas, and customize their experience with various tools and features.

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

### Select Grid Size:
- Adjust canvas size from the dropdown menu.

### Draw with Tools:
- Choose from brush, pen, or eraser tools. Adjust pen size as needed.

### Clear the Canvas:
- Use the "Clear" button for a fresh canvas.

## Contribution

| Contributor         | Contributions                            |
|---------------------|------------------------------------------|
| Mustafa Batin Efe   | Server implementation, Clear button      |
| Luka Babetzki       | Adjustable window size, erasing tool     |
| Valeria Escobar     | Drawing features, report writing         |

## License
This project is licensed under the GNU General Public License v3.0 (GPL-3.0).

## Acknowledgments
Special thanks to the team members for their contributions and collaboration on this project.