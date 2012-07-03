	/**
	 * Copyright 2010 Neuroph Project http://neuroph.sourceforge.net
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *    http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */

	import java.io.IOException;
	import java.sql.*;
	import java.util.Arrays;
	import java.util.Hashtable;
	import org.neuroph.core.NeuralNetwork;
	import org.neuroph.core.learning.SupervisedTrainingElement;
	import org.neuroph.core.learning.TrainingSet;
	import org.neuroph.nnet.MultiLayerPerceptron;
	import org.neuroph.nnet.learning.MomentumBackpropagation;
	import org.neuroph.util.TransferFunctionType;


	/**
	 * This sample shows how to create, train, save and load simple Multi Layer Perceptron for the XOR problem.
	 * This sample shows basics of Neuroph API.
	 * @author Zoran Sevarac <sevarac@gmail.com>
	 */
	public class Try2 {
		
		   public static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";  
	  	   public static final String DB_URL = "jdbc:oracle:thin:@//10.76.85.107:1521/FNMT";
	  	   public static ConnectionPoolArray connectionPool;
	  	   
	  	 //  Database credentials
		   public static final String USER = "syslog";
		   public static final String PASS = "manager";
		   
		   public static double[] inputArray = new double[8];
		   
		   public static Hashtable<String, Integer> nodestable = new Hashtable<String, Integer>();;
		   public static Hashtable<Integer, String> links = new Hashtable<Integer, String>();;
	
	    /**
	     * Runs this sample
	     */
	    public static void main(String[] args) {
	    	
	    	
	        String path = "C:\\Users\\sbsv\\Desktop\\neural-input.txt";
	    	
	        TrainingSet trainingSet = TrainingSet.createFromFile(path, 8, 4, " ");
	        //public static TrainingSet createFromFile(java.lang.String filePath, int inputsCount, int outputsCount, java.lang.String delimiter)
	        
	        
	        // create multi layer perceptron
	        MultiLayerPerceptron myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 8, 6, 4);

	        // enable batch if using MomentumBackpropagation
	        if( myMlPerceptron.getLearningRule() instanceof MomentumBackpropagation )
	        	((MomentumBackpropagation)myMlPerceptron.getLearningRule()).setBatchMode(true);

	        // learn the training set
	        System.out.println("Training neural network...");
	        myMlPerceptron.learn(trainingSet);

	        // test perceptron
	 //       System.out.println("Testing trained neural network");
	        //testNeuralNetwork(myMlPerceptron, trainingSet);

	        // save trained neural network
	        myMlPerceptron.save("myMlPerceptron.nnet");

	        // load saved neural network
	        NeuralNetwork loadedMlPerceptron = NeuralNetwork.load("myMlPerceptron.nnet");

	        // test loaded neural network
	        System.out.println("Testing loaded neural network");
			
	        String IPS[] = {"172.28.255.227", "172.28.255.228", "172.28.255.229"}; 
	        int no = 0;
	        
	        for(String IP : IPS){
	        
	        	nodestable.put(IP, no);
	        	no++;
	        }
	        String Links[] = {"172.28.255.227", "172.28.255.228", "172.28.255.229"}; 
	        int no1 = 0;
	        
	        for(String IP : IPS){
	        
	        	links.put(no1, IP);
	        	no++;
	        }
	        getAlarms();
	        testNeuralNetwork(loadedMlPerceptron, trainingSet);
	    }

	    /**
	     * Prints network output for the each element from the specified training set.
	     * @param neuralNet neural network
	     * @param trainingSet training set
	     */
	    
	    public static void getAlarms(){
	    	
		       
	   	 //   	TrapReceiver receiver = new TrapReceiver();
	   	 //		receiver.start();
	    	int node;
	    	
	    	
	    	for(int j = 0; j<inputArray.length; j++){
	    			
	    			inputArray[j] = 0;
	    	}
	    		
	    	
	   	    	
	   	    	try {
	   	    		connectionPool = new ConnectionPoolArray(JDBC_DRIVER, DB_URL, USER, PASS, 10, 50, true);
	   	    		} catch(SQLException sqle) {
	   	    		System.out.println("Error making pool: " + sqle);
	   	    		connectionPool = null;
	   	    		}
	   	    	
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
	   			         
	   			        /* 
	   			         * remove commenting once u add type in the database
	   			         * presently removed
	   			         * 
	   			         * 
	   			         * 
	   			       //   */
	   			      int alarmType = rs.getInt("TYPE"); 
	   			        
	   			         //Display values
	   			       //  System.out.print(no + " ID: " + id);
	   			         System.out.print(" HOST:" + host);
	   			         
	   			         node = nodestable.get(host);
	   			         
	   			         inputArray[node*4 + alarmType + 1] = 1;
	   			    	//System.out.println(Arrays.toString(inputArray));
	   			         
	   			        // System.out.print(" ERRORNAME: " + errorname);
	   			        // System.out.println(" MESSAGE " + message);
	   			         no++;
	   			      }
	   			      
	   			      stmt.close();
	   			      conn.close();
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
	    public static void testNeuralNetwork(NeuralNetwork neuralNet, TrainingSet<SupervisedTrainingElement> trainingSet) {

	    	
	    	
	    	
	    	
	    	   neuralNet.setInput(inputArray);
	            neuralNet.calculate();
	            double[] networkOutput = neuralNet.getOutput();

	            System.out.print("Input: " + Arrays.toString( inputArray ) );
	            System.out.println(" Output: " + Arrays.toString( networkOutput) );

	            double max = 0;
	            int index = 0;
	            for(int k = 0; k < networkOutput.length; k++){
	            	
	            	if(networkOutput[k] > max)max = networkOutput[k];
	            	index = k;
	            }
	            
	            System.out.println("Fault :" + links.get(index));
	            
	     /*   for(SupervisedTrainingElement trainingElement : trainingSet.elements()) {
	            neuralNet.setInput(trainingElement.getInput());
	            neuralNet.calculate();
	            double[] networkOutput = neuralNet.getOutput();

	            System.out.print("Input: " + Arrays.toString( trainingElement.getInput() ) );
	            System.out.println(" Output: " + Arrays.toString( networkOutput) );
	        }
	       */
	    }

	}
