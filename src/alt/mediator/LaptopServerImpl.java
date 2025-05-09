package alt.mediator;

import model.logic.DataManager;
import model.models.Laptop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LaptopServerImpl implements Runnable {
  private static final Logger logger = Logger.getLogger(LaptopServerImpl.class.getName());
  private final Socket socket;
  private final DataManager dataManager; // Use your database-based DataManager
  private ObjectInputStream in;
  private ObjectOutputStream out;

  public LaptopServerImpl(Socket socket, DataManager dataManager) {
    this.socket = socket;
    this.dataManager = dataManager;
  }

  @Override
  public void run() {
    try {
      // Initialize streams
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());

      // Process client requests
      while (true) {
        String command = (String) in.readObject();
        logger.info("Received command: " + command);

        switch (command) {
          case "GET_ALL_LAPTOPS":
            handleGetAllLaptops();
            break;
          // Other commands...
          default:
            sendError("Unknown command: " + command);
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      logger.log(Level.INFO, "Client disconnected: " + e.getMessage());
    } finally {
      closeConnection();
    }
  }

  private void handleGetAllLaptops() {
    try {
      // Get laptops from the database-connected DataManager
      List<Laptop> laptops = dataManager.getAllLaptops();

      // Send success status
      out.writeObject("SUCCESS");

      // Send the list of laptops
      out.writeObject(laptops);
      out.flush();

      logger.info("Sent " + laptops.size() + " laptops to client");
    } catch (Exception e) {
      sendError("Error retrieving laptops: " + e.getMessage());
    }
  }

  private void sendError(String message) {
    try {
      out.writeObject("ERROR");
      out.writeObject(message);
      out.flush();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error sending error message: " + e.getMessage(), e);
    }
  }

  private void closeConnection() {
    try {
      if (in != null) in.close();
      if (out != null) out.close();
      if (socket != null) socket.close();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Error closing connection: " + e.getMessage(), e);
    }
  }
}