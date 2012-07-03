
import java.sql.*;
import java.util.*;

/** A class for preallocating, recycling, and managing
 *  JDBC connections.
 *  <P>
 *  Taken from Core Servlets and JavaServer Pages
 *  from Prentice Hall and Sun Microsystems Press,
 *  http://www.coreservlets.com/.
 *  &copy; 2000 Marty Hall; may be freely used or adapted.
 */

public class ConnectionPoolArray implements Runnable {
  private String driver, url, username, password;
  private int maxConnections;
  private boolean waitIfBusy;
  private ArrayList availableConnectionsArray, busyConnectionsArray;
  //private Vector availableConnectionsArray, busyConnectionsArray;
  private boolean connectionPending = false;

  public ConnectionPoolArray(String driver, String url,
                        String username, String password,
                        int initialConnections,
                        int maxConnections,
                        boolean waitIfBusy)
      throws SQLException {
    this.driver = driver;
    this.url = url;
    this.username = username;
    this.password = password;
    this.maxConnections = maxConnections;
    this.waitIfBusy = waitIfBusy;
    if (initialConnections > maxConnections) {
      initialConnections = maxConnections;
    }
    //availableConnectionsArray = new Vector(initialConnections);
    //busyConnectionsArray = new Vector();
    availableConnectionsArray = new ArrayList(initialConnections);
    busyConnectionsArray = new ArrayList();
    for(int i=0; i<initialConnections; i++) {
      //availableConnectionsArray.addElement(makeNewConnection());
      availableConnectionsArray.add(makeNewConnection());
      
    }
  }
  
  public synchronized Connection getConnection()
      throws SQLException {
    if (!availableConnectionsArray.isEmpty()) {
    	int last = availableConnectionsArray.size() -1;
      Connection existingConnection =
        (Connection)availableConnectionsArray.get(last);
      
      availableConnectionsArray.remove(last);
      // If connection on available list is closed (e.g.,
      // it timed out), then remove it from available list
      // and repeat the process of obtaining a connection.
      // Also wake up threads that were waiting for a
      // connection because maxConnection limit was reached.
      if (existingConnection.isClosed()) {
        notifyAll(); // Freed up a spot for anybody waiting
        return(getConnection());
      } else {
        busyConnectionsArray.add(existingConnection);
        return(existingConnection);
      }
    } else {
      
      // Three possible cases:
      // 1) You haven't reached maxConnections limit. So
      //    establish one in the background if there isn't
      //    already one pending, then wait for
      //    the next available connection (whether or not
      //    it was the newly established one).
      // 2) You reached maxConnections limit and waitIfBusy
      //    flag is false. Throw SQLException in such a case.
      // 3) You reached maxConnections limit and waitIfBusy
      //    flag is true. Then do the same thing as in second
      //    part of step 1: wait for next available connection.
      
      if ((totalConnections() < maxConnections) &&
          !connectionPending) {
        makeBackgroundConnection();
      } else if (!waitIfBusy) {
        throw new SQLException("Connection limit reached");
      }
      // Wait for either a new connection to be established
      // (if you called makeBackgroundConnection) or for
      // an existing connection to be freed up.
      try {
        wait();
      } catch(InterruptedException ie) {}
      // Someone freed up a connection, so try again.
      return(getConnection());
    }
  }

  // You can't just make a new connection in the foreground
  // when none are available, since this can take several
  // seconds with a slow network connection. Instead,
  // start a thread that establishes a new connection,
  // then wait. You get woken up either when the new connection
  // is established or if someone finishes with an existing
  // connection.

  private void makeBackgroundConnection() {
    connectionPending = true;
    try {
      Thread connectThread = new Thread(this);
      connectThread.start();
    } catch(OutOfMemoryError oome) {
      // Give up on new connection
    }
  }

  public void run() {
    try {
      Connection connection = makeNewConnection();
      synchronized(this) {
        availableConnectionsArray.add(connection);
        connectionPending = false;
        notifyAll();
      }
    } catch(Exception e) { // SQLException or OutOfMemory
      // Give up on new connection and wait for existing one
      // to free up.
    }
  }

  // This explicitly makes a new connection. Called in
  // the foreground when initializing the ConnectionPool,
  // and called in the background when running.
  
  private Connection makeNewConnection()
      throws SQLException {
    try {
      // Load database driver if not already loaded
      Class.forName(driver);
      // Establish network connection to database
      Connection connection =
        DriverManager.getConnection(url, username, password);
      return(connection);
    } catch(ClassNotFoundException cnfe) {
      // Simplify try/catch blocks of people using this by
      // throwing only one exception type.
      throw new SQLException("Can't find class for driver: " +
                             driver);
    }
  }

  public synchronized void free(Connection connection) {
    busyConnectionsArray.remove(connection);
    availableConnectionsArray.add(connection);
    // Wake up threads that are waiting for a connection
    notifyAll(); 
  }
    
  public synchronized int totalConnections() {
    return(availableConnectionsArray.size() +
           busyConnectionsArray.size());
  }

  /** Close all the connections. Use with caution:
   *  be sure no connections are in use before
   *  calling. Note that you are not <I>required</I> to
   *  call this when done with a ConnectionPool, since
   *  connections are guaranteed to be closed when
   *  garbage collected. But this method gives more control
   *  regarding when the connections are closed.
   */

  public synchronized void closeAllConnections() {
    closeConnections(availableConnectionsArray);
    availableConnectionsArray = new ArrayList();
    closeConnections(busyConnectionsArray);
    busyConnectionsArray = new ArrayList();
  }

  private void closeConnections(ArrayList connections) {
    try {
      for(int i=0; i<connections.size(); i++) {
        Connection connection =
          (Connection)connections.get(i);
        if (!connection.isClosed()) {
          connection.close();
        }
      }
    } catch(SQLException sqle) {
      // Ignore errors; garbage collect anyhow
    }
  }
  
  public synchronized String toString() {
    String info =
      "ConnectionPool(" + url + "," + username + ")" +
      ", available=" + availableConnectionsArray.size() +
      ", busy=" + busyConnectionsArray.size() +
      ", max=" + maxConnections;
    return(info);
  }
}
