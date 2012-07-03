import java.io.IOException;
import java.sql.*;
import java.util.Date;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

public class TrapReceiver implements CommandResponder {
	
	   public static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";  
	   public static final String DB_URL = "jdbc:oracle:thin:@//10.76.85.107:1521/FNMT";
	   private static ConnectionPoolArray connectionPool;

	   //  Database credentials
	   public static final String USER = "syslog";
	   public static final String PASS = "manager";
	   
	   public void start() {
		
	    //TrapReceiver snmp4jTrapReceiver = new TrapReceiver();
		try {
		connectionPool = new ConnectionPoolArray(JDBC_DRIVER, DB_URL, USER, PASS, 10, 50, true);
		} catch(SQLException sqle) {
		System.out.println("Error making pool: " + sqle);
		connectionPool = null;
		}
		try {
			this.listen(new UdpAddress("localhost/162"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Trap Listner
	 */
	public synchronized void listen(TransportIpAddress address)
			throws IOException {
		AbstractTransportMapping transport;
		if (address instanceof TcpAddress) {
			transport = new DefaultTcpTransportMapping((TcpAddress) address);
		} else {
			transport = new DefaultUdpTransportMapping((UdpAddress) address);
		}

		ThreadPool threadPool = ThreadPool.create("DispatcherPool", 10);
		MessageDispatcher mDispathcher = new MultiThreadedMessageDispatcher(
				threadPool, new MessageDispatcherImpl());

		// add message processing models
		mDispathcher.addMessageProcessingModel(new MPv1());
		mDispathcher.addMessageProcessingModel(new MPv2c());

		// add all security protocols
		SecurityProtocols.getInstance().addDefaultProtocols();
		SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

		// Create Target
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));

		Snmp snmp = new Snmp(mDispathcher, transport);
		snmp.addCommandResponder(this);

		transport.listen();
		System.out.println("Listening on " + address);

		try {
			this.wait();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * This method will be called whenever a pdu is received on the given port
	 * specified in the listen() method
	 */
	public synchronized void processPdu(CommandResponderEvent cmdRespEvent) {
		System.out.println("Received PDU...");
		PDU pdu = cmdRespEvent.getPDU();
		if (pdu != null) {
			System.out.println("Trap Type = " + pdu.getType());
			System.out.println("Variables = " + pdu.getVariableBindings().firstElement().toValueString());
			System.out.println("Variables = " + pdu.getVariableBindings().get(1));
			
			int type = pdu.getType();
			String time = pdu.getVariableBindings().firstElement().toString();
			
			
			Connection conn = null;
			   Statement stmt = null;
			   try{
			      //STEP 2: Register JDBC driver
			      Class.forName("oracle.jdbc.driver.OracleDriver");

			      //STEP 3: Open a connection
			      System.out.println("Connecting to database...");
			      
			      //conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      conn = connectionPool.getConnection();
			      //STEP 4: Execute a query
			      System.out.println("Creating statement...");
			      stmt = conn.createStatement();
			      String sql;
			      int id = 1002;
			      
			      sql = "INSERT INTO SYSLOG_DATA (ID, TIMESTAMP) VALUES ('"+id+"', '09-MAY-12 03.52.28.000000000 PM +05:30')";
			      stmt.executeQuery(sql);
			      /*
			      String sql1 = "INSERT INTO SYSLOG_DATA VALUES (1002, '09-MAY-12 03.52.28.000000000 PM +05:30', '127.0.0.1')";
			      stmt.executeQuery(sql1);
			      */
			      
			      
			      /*
			      sql = "SELECT ID, HOST, ERRORNAME, MESSAGE FROM SYSLOG_DATA WHERE ROWNUM <= 10";
			      ResultSet rs = stmt.executeQuery(sql);
			      int no =1;
			      //STEP 5: Extract data from result set
			      while(rs.next()){
			         //Retrieve by column name
			         int id  = rs.getInt("ID");
			         String host = rs.getString("HOST");
			         String message = rs.getString("MESSAGE");
			         String errorname = rs.getString("ERRORNAME");
			         
			         //Display values
			         System.out.print(no + " ID: " + id);
			         System.out.print(" HOST:" + host);
			         System.out.print(" ERRORNAME: " + errorname);
			         System.out.println(" MESSAGE " + message);
			         no++;
			      }
			      
			      //STEP 6: Clean-up environment
			      rs.close();
			      */
			
			
			      stmt.close();
			      //conn.close();
			      connectionPool.free(conn);
			      
			   }catch(SQLException se){
			      //Handle errors for JDBC
			      se.printStackTrace();
			   }catch(Exception e){
			      //Handle errors for Class.forName
			      e.printStackTrace();
			   }finally{
			      //finally block used to close resources
			      try{
			         if(stmt!=null)
			            stmt.close();
			      }catch(SQLException se2){
			      }// nothing we can do
			      try{
			         if(conn!=null)
			            conn.close();
			      }catch(SQLException se){
			         se.printStackTrace();
			      }//end finally try
			   }//end try
			   System.out.println("Goodbye!");
			
			
			
			
			}
		}
	}

